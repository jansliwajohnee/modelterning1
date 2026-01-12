package ioioi.it.mltraining.service;

import ioioi.it.mltraining.entity.Candle;
import ioioi.it.mltraining.entity.CandlePattern;
import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.service.indicator.*;
import ioioi.it.mltraining.service.indicator.BarSeriesConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ta4j.core.BarSeries;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * Service responsible for generating enhanced Candle records with technical indicators
 * from raw CandleRaw data stored in the database.
 *
 * This service orchestrates the process of:
 * - Finding the latest processed candle by openTime
 * - Fetching only new CandleRaw records after the latest processed openTime
 * - Converting to Ta4j BarSeries for indicator calculations
 * - Calculating technical indicators (momentum, trend, volatility, volume, etc.)
 * - Detecting candlestick patterns
 * - Persisting the enhanced Candle records with all indicators and patterns
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CandleGeneratorService {

    private final CandleRawQueryService candleRawQueryService;
    private final CandleQueryService candleQueryService;
    private final CandlePersistenceService candlePersistenceService;
    private final CandlePatternDetector candlePatternDetector;

    // Indicator calculators
    private final MomentumIndicatorCalculator momentumCalculator;
    private final TrendIndicatorCalculator trendCalculator;
    private final VolatilityIndicatorCalculator volatilityCalculator;
    private final VolumeIndicatorCalculator volumeCalculator;
    private final PriceActionIndicatorCalculator priceActionCalculator;
    private final StatisticalIndicatorCalculator statisticalCalculator;
    private final MACDIndicatorCalculator macdCalculator;
    private final SupportResistanceIndicatorCalculator supportResistanceCalculator;
    private final LagFeaturesCalculator lagFeaturesCalculator;
    private final CompositeScoresCalculator compositeScoresCalculator;
    private final VolumeProfileCalculator volumeProfileCalculator;

    /**
     * Generates enhanced Candle records with technical indicators from CandleRaw data.
     * Only processes new candles that haven't been processed yet (based on openTime).
     *
     * @param symbol the trading symbol (e.g., "BTCUSDT")
     * @param interval the candle interval (e.g., "1m", "5m", "1h")
     * @param limit optional limit of candles to process (null = process all available)
     * @return generation result with statistics
     */
    @Transactional
    public CandleGenerationResult generateCandles(String symbol, String interval, Integer limit) {
        log.info("Starting candle generation for symbol={}, interval={}, limit={}", symbol, interval, limit);

        // 1. Check the latest processed candle's openTime
        Optional<ZonedDateTime> latestOpenTime = candleQueryService.findLatestOpenTime(symbol, interval);

        ZonedDateTime afterTime = null;
        if (latestOpenTime.isPresent()) {
            afterTime = latestOpenTime.get();
            log.info("Found latest processed candle at openTime={} for {}/{}, fetching only newer candles",
                    afterTime, symbol, interval);
        } else {
            log.info("No processed candles found for {}/{}, starting from the oldest CandleRaw", symbol, interval);
        }

        String intervalRaw = interval + "m";

        // 2. Fetch CandleRaw records from database
        // - If afterTime is null: fetch ALL candles from beginning (oldest to newest)
        // - If afterTime is set: fetch only candles AFTER that time
        List<CandleRaw> candleRaws = candleRawQueryService.fetchCandleRaws(
                symbol,
                intervalRaw,
                limit,
                afterTime
        );

        if (candleRaws.isEmpty()) {
            if (afterTime == null) {
                log.warn("No CandleRaw records found in database for symbol={}, interval={}", symbol, intervalRaw);
            } else {
                log.info("No new CandleRaw records found after {} for symbol={}, interval={}", afterTime, symbol, intervalRaw);
            }
            return new CandleGenerationResult(0, 0, 0, symbol, intervalRaw);
        }

        log.info("Processing {} new CandleRaw records", candleRaws.size());

        // 3. Convert to Ta4j BarSeries for indicator calculations
        BarSeries series = BarSeriesConverter.toBarSeries(candleRaws, symbol);
        log.debug("Converted to BarSeries with {} bars", series.getBarCount());

        // 4. Calculate all technical indicators for each bar
        List<Candle> candles = IntStream.range(0, series.getBarCount())
                .mapToObj(index -> calculateCandleWithIndicators(series, candleRaws, index))
                .toList();

        log.info("Calculated indicators for {} candles", candles.size());

        // 5. Persist candles to database (INSERT or UPDATE)
        int savedCount = candlePersistenceService.saveAll(candles);

        log.info("Candle generation completed: processed={}, saved={}", candleRaws.size(), savedCount);

        return new CandleGenerationResult(
                candleRaws.size(),
                savedCount,
                0, // We don't distinguish between insert/update in upsert
                symbol,
                interval
        );
    }

    /**
     * Calculates all technical indicators for a single candle and creates Candle entity.
     *
     * @param series the Ta4j BarSeries
     * @param candleRaws original CandleRaw data
     * @param index the bar index in the series
     * @return Candle entity with all indicators populated
     */
    private Candle calculateCandleWithIndicators(BarSeries series, List<CandleRaw> candleRaws, int index) {
        CandleRaw raw = candleRaws.get(index);

        // Calculate all indicator groups
        MomentumIndicators momentum = momentumCalculator.calculate(series, index);
        TrendIndicators trend = trendCalculator.calculate(series, index);
        VolatilityIndicators volatility = volatilityCalculator.calculate(series, index);
        VolumeIndicators volume = volumeCalculator.calculate(series, index);
        PriceActionIndicators priceAction = priceActionCalculator.calculate(series, index);
        StatisticalIndicators statistical = statisticalCalculator.calculate(series, index);
        MACDIndicators macd = macdCalculator.calculate(series, index);
        SupportResistanceIndicators supportResistance = supportResistanceCalculator.calculate(series, index);
        LagFeatures lagFeatures = lagFeaturesCalculator.calculate(series, index);
        CompositeScores compositeScores = compositeScoresCalculator.calculate(series, index);
        VolumeProfileIndicators volumeProfile = volumeProfileCalculator.calculate(series, index);

        // Detect candlestick patterns (FK will be set automatically by Spring Data JDBC)
        Set<CandlePattern> patterns = candlePatternDetector.detectPatterns(series, index, null);

        // Map all indicators to Candle entity
        return new Candle(
                null, // id will be generated or updated
                // Raw data
                raw.getSymbol(),
                raw.getInterval(),
                raw.getOpenTime(),
                raw.getCloseTime(),
                raw.getOpen(),
                raw.getClose(),
                raw.getLow(),
                raw.getHigh(),
                raw.getVolume(),
                raw.getTurnover(),
                raw.getIsClosed(),
                // Momentum indicators (14 fields)
                momentum.rsi7(),
                momentum.rsi14(),
                momentum.rsi21(),
                momentum.stochasticK(),
                momentum.stochasticD(),
                momentum.stochasticRsi(),
                momentum.williamsR(),
                momentum.cciNormalized(),
                momentum.mfi(),
                momentum.cmo(),
                momentum.ultimateOscillator(),
                momentum.rocPercent(),
                momentum.rsiDistanceFrom50(),
                momentum.rsiSlope(),
                // Trend indicators (22 fields)
                trend.closeEma8DistancePct(),
                trend.closeEma21DistancePct(),
                trend.closeEma50DistancePct(),
                trend.closeEma100DistancePct(),
                trend.closeEma200DistancePct(),
                trend.closeSma20DistancePct(),
                trend.closeSma50DistancePct(),
                trend.closeSma200DistancePct(),
                trend.closeVwmaDistancePct(),
                trend.closeHmaDistancePct(),
                trend.ema8Ema21SpreadPct(),
                trend.ema21Ema50SpreadPct(),
                trend.ema50Ema200SpreadPct(),
                trend.adx(),
                trend.plusDi(),
                trend.minusDi(),
                trend.diSpreadNormalized(),
                trend.aroonUp(),
                trend.aroonDown(),
                trend.aroonOscillator(),
                trend.linearRegressionSlopePct(),
                trend.maSlopePct(),
                // Volatility indicators (12 fields)
                volatility.atrPct(),
                volatility.atr721Ratio(),
                volatility.bbPercentB(),
                volatility.bbWidthPct(),
                volatility.bbPosition(),
                volatility.keltnerPercentK(),
                volatility.keltnerWidthPct(),
                volatility.rangePct(),
                volatility.rangeAtrRatio(),
                volatility.atrPercentile(),
                volatility.stddevPct(),
                volatility.stddev1450Ratio(),
                // Volume indicators (9 fields)
                volume.rvolSma20(),
                volume.rvolSma50(),
                volume.volumePercentile(),
                volume.volumeNormalized(),
                volume.obvChangePct(),
                volume.cmf(),
                volume.volumeRocPct(),
                volume.upVolumeRatio(),
                volume.volumePressure(),
                // Price action indicators (20 fields)
                priceAction.bodyRangeRatio(),
                priceAction.upperWickRangeRatio(),
                priceAction.lowerWickRangeRatio(),
                priceAction.closePositionInRange(),
                priceAction.bodyPct(),
                priceAction.gapPct(),
                priceAction.isBullish(),
                priceAction.return1(),
                priceAction.return3(),
                priceAction.return5(),
                priceAction.return10(),
                priceAction.return20(),
                priceAction.return50(),
                priceAction.highestHighDistancePct(),
                priceAction.lowestLowDistancePct(),
                priceAction.positionInRangeN(),
                priceAction.consecutiveBullishRatio(),
                priceAction.consecutiveBearishRatio(),
                priceAction.higherHighsRatio(),
                priceAction.higherClosesRatio(),
                // Statistical indicators (10 fields)
                statistical.closePercentile100(),
                statistical.closePercentile500(),
                statistical.closeZscore20(),
                statistical.rsiPercentile(),
                statistical.volumePercentileStat(),
                statistical.atrPercentileStat(),
                statistical.skewnessReturns(),
                statistical.kurtosisReturns(),
                statistical.drawdownPct(),
                statistical.drawupPct(),
                // MACD indicators (6 fields)
                macd.macdPct(),
                macd.macdSignalPct(),
                macd.macdHistogramPct(),
                macd.macdHistogramChange(),
                macd.macdGtSignal(),
                macd.macdGtZero(),
                // Support/Resistance indicators (8 fields)
                supportResistance.closePivotDistancePct(),
                supportResistance.closeS1DistancePct(),
                supportResistance.closeR1DistancePct(),
                supportResistance.closePrevDayHighPct(),
                supportResistance.closePrevDayLowPct(),
                supportResistance.closePrevWeekHighPct(),
                supportResistance.closeRoundNumberDistancePct(),
                supportResistance.closeVwapDistancePct(),
                // Lag features (19 fields)
                lagFeatures.returnLag1(),
                lagFeatures.returnLag2(),
                lagFeatures.returnLag3(),
                lagFeatures.returnLag5(),
                lagFeatures.returnLag10(),
                lagFeatures.rsiLag1(),
                lagFeatures.rsiLag2(),
                lagFeatures.rsiLag3(),
                lagFeatures.rsiChange(),
                lagFeatures.atrPctLag1(),
                lagFeatures.atrPctLag2(),
                lagFeatures.rvolLag1(),
                lagFeatures.rvolLag2(),
                lagFeatures.bbPercentBLag1(),
                lagFeatures.bbPercentBLag2(),
                lagFeatures.adxLag1(),
                lagFeatures.adxLag2(),
                lagFeatures.bodyPctLag1(),
                lagFeatures.bodyPctLag2(),
                // Composite scores (5 fields)
                compositeScores.trendScore(),
                compositeScores.bullishPatternsRatio(),
                compositeScores.momentumAgreement(),
                compositeScores.volatilityVsTrend(),
                compositeScores.volumeConfirmation(),
                // Volume Profile indicators (14 fields)
                volumeProfile.pocPrice(),
                volumeProfile.pocIndex(),
                volumeProfile.pocVolumePct(),
                volumeProfile.pocPositionInRange(),
                volumeProfile.vahPrice(),
                volumeProfile.valPrice(),
                volumeProfile.valueAreaPct(),
                volumeProfile.valueAreaVolumePct(),
                volumeProfile.volumeAbovePocPct(),
                volumeProfile.volumeBelowPocPct(),
                volumeProfile.volumeImbalance(),
                volumeProfile.highVolumeNodesCount(),
                volumeProfile.lowVolumeNodesCount(),
                volumeProfile.volumeConcentration(),
                // Candle patterns
                patterns
        );
    }

    /**
     * Result record for candle generation operations.
     *
     * @param processed total number of CandleRaw records processed
     * @param generated number of new Candle records created
     * @param updated number of existing Candle records updated
     * @param symbol the trading symbol
     * @param interval the candle interval
     */
    public record CandleGenerationResult(
            int processed,
            int generated,
            int updated,
            String symbol,
            String interval
    ) {}
}
