package ioioi.it.mltraining.service;

import ioioi.it.mltraining.entity.Candle;
import ioioi.it.mltraining.entity.CandlePattern;
import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.service.indicator.*;
import ioioi.it.mltraining.service.indicator.BarSeriesConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
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
 * - Fetching only new CandleRaw records after the latest processed openTime (in batches of 1000)
 * - Converting to Ta4j BarSeries for indicator calculations
 * - Calculating technical indicators (momentum, trend, volatility, volume, etc.)
 * - Detecting candlestick patterns (31 patterns)
 * - Persisting the enhanced Candle records with all indicators and patterns
 *
 * <p><b>Batch Processing:</b> Processes in batches of 1000 candles. The first batch skips the
 * first 550 candles (starts saving from candle 550) to ensure all saved candles have complete
 * historical data for indicators (close_percentile_500, EMA/SMA_200). Each batch fetches 550
 * lookback buffer, 1000 new candles, and 50 lookhead buffer (for forward returns). All candles
 * are processed for indicator calculations, but only the middle 1000 are saved. Each batch
 * commits independently to the database.</p>
 */
@Service
@Slf4j
public class CandleGeneratorService {

    private final CandleRawQueryService candleRawQueryService;
    private final CandleQueryService candleQueryService;
    private final CandlePersistenceService candlePersistenceService;
    private final CandlePatternDetector candlePatternDetector;

    // Self-injection to enable @Transactional on processBatch() called from generateCandles()
    private final CandleGeneratorService self;

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

    // Constructor with self-injection (@Lazy to avoid circular dependency)
    public CandleGeneratorService(
            CandleRawQueryService candleRawQueryService,
            CandleQueryService candleQueryService,
            CandlePersistenceService candlePersistenceService,
            CandlePatternDetector candlePatternDetector,
            MomentumIndicatorCalculator momentumCalculator,
            TrendIndicatorCalculator trendCalculator,
            VolatilityIndicatorCalculator volatilityCalculator,
            VolumeIndicatorCalculator volumeCalculator,
            PriceActionIndicatorCalculator priceActionCalculator,
            StatisticalIndicatorCalculator statisticalCalculator,
            MACDIndicatorCalculator macdCalculator,
            SupportResistanceIndicatorCalculator supportResistanceCalculator,
            LagFeaturesCalculator lagFeaturesCalculator,
            CompositeScoresCalculator compositeScoresCalculator,
            VolumeProfileCalculator volumeProfileCalculator,
            @Lazy CandleGeneratorService self) {
        this.candleRawQueryService = candleRawQueryService;
        this.candleQueryService = candleQueryService;
        this.candlePersistenceService = candlePersistenceService;
        this.candlePatternDetector = candlePatternDetector;
        this.momentumCalculator = momentumCalculator;
        this.trendCalculator = trendCalculator;
        this.volatilityCalculator = volatilityCalculator;
        this.volumeCalculator = volumeCalculator;
        this.priceActionCalculator = priceActionCalculator;
        this.statisticalCalculator = statisticalCalculator;
        this.macdCalculator = macdCalculator;
        this.supportResistanceCalculator = supportResistanceCalculator;
        this.lagFeaturesCalculator = lagFeaturesCalculator;
        this.compositeScoresCalculator = compositeScoresCalculator;
        this.volumeProfileCalculator = volumeProfileCalculator;
        this.self = self;
    }

    /**
     * Generates enhanced Candle records with technical indicators from CandleRaw data.
     * Only processes new candles that haven't been processed yet (based on openTime).
     *
     * <p><b>Batch Processing Strategy:</b></p>
     * <ul>
     *   <li><b>First batch:</b> Skips first 550 candles (saved from candle 550 onwards)</li>
     *   <li>Fetches 550 candles as lookback buffer (for indicator context)</li>
     *   <li>Fetches 1050 new candles per batch (1000 + 50 lookhead buffer)</li>
     *   <li>Calculates indicators for all candles (lookback + batch + lookhead = up to 1600 candles)</li>
     *   <li>Saves only the middle 1000 candles (excluding lookback and lookhead buffers)</li>
     *   <li>Each batch commits independently to database</li>
     * </ul>
     *
     * <p><b>Why skip first 550 candles?</b> To ensure all saved candles have complete
     * historical data for indicators: close_percentile_500 needs 500, EMA/SMA_200 need 200.
     * The lookhead buffer (50 candles) ensures that forward-looking indicators (return_1
     * through return_50) are calculated correctly for all saved candles.</p>
     *
     * <p><b>Example:</b> Batch #1 processes candles 0-1599 but saves only 550-1549
     * (skipping first 550 for history, last 50 for lookhead).</p>
     *
     * @param symbol the trading symbol (e.g., "BTCUSDT")
     * @param interval the candle interval (e.g., "1m", "5m", "1h")
     * @param limit optional limit of candles to process (null = process all available)
     * @return generation result with statistics
     */
    public CandleGenerationResult generateCandles(String symbol, String interval, Integer limit) {
        log.info("Starting candle generation for symbol={}, interval={}, limit={}", symbol, interval, limit);

        String intervalRaw = interval + "m";

        // 1. Check the latest processed candle's openTime
        Optional<ZonedDateTime> latestOpenTime = candleQueryService.findLatestOpenTime(symbol, intervalRaw);

        ZonedDateTime afterTime = null;
        if (latestOpenTime.isPresent()) {
            afterTime = latestOpenTime.get();
            log.info("Found latest processed candle at openTime={} for {}/{}, fetching only newer candles",
                    afterTime, symbol, intervalRaw);
        } else {
            log.info("No processed candles found for {}/{}, starting from the oldest CandleRaw", symbol, intervalRaw);
        }

        // Process in batches to avoid memory issues with large datasets
        // We fetch extra candles as buffers for indicators
        final int BATCH_SIZE = 1000;
        final int LOOKBACK_BUFFER = 550;  // Previous candles for historical context (when resuming)
                                          // Max required: 500 for close_percentile_500, 200 for EMA/SMA_200
        final int LOOKHEAD_BUFFER = 50;   // Future candles for forward returns (return_1 to return_50)
        int totalProcessed = 0;
        int totalSaved = 0;
        int batchNumber = 1;
        ZonedDateTime currentAfterTime = afterTime;

        while (true) {
            log.info("═══ Batch #{} ═══ Starting after openTime={}", batchNumber, currentAfterTime);

            // 2a. Fetch lookback buffer for indicator context
            // - When resuming: fetch from database (previous processed candles)
            // - First batch: fetch from candle_raw but don't save them (need historical context)
            List<CandleRaw> lookbackBuffer = List.of();
            boolean isFirstBatch = (currentAfterTime == null);

            if (isFirstBatch) {
                // First batch: fetch LOOKBACK_BUFFER candles from candle_raw for context
                // We'll process them but NOT save them (they'll be processed later with full history)
                lookbackBuffer = candleRawQueryService.fetchCandleRaws(
                        symbol,
                        intervalRaw,
                        LOOKBACK_BUFFER,
                        null  // from beginning
                );
                log.info("Batch #{}: First batch - fetched {} candles for lookback context (will not be saved)",
                        batchNumber, lookbackBuffer.size());
            } else {
                // Resuming: fetch from database (already processed candles)
                lookbackBuffer = candleRawQueryService.fetchCandleRawsBefore(
                        symbol,
                        intervalRaw,
                        LOOKBACK_BUFFER,
                        currentAfterTime
                );
                log.info("Batch #{}: Fetched {} lookback buffer candles from database (for indicator context)",
                        batchNumber, lookbackBuffer.size());
            }

            // 2b. Fetch new CandleRaw records from database (batch + lookhead buffer)
            // For first batch, start AFTER the lookback buffer
            ZonedDateTime fetchAfterTime = isFirstBatch && !lookbackBuffer.isEmpty()
                    ? lookbackBuffer.get(lookbackBuffer.size() - 1).getOpenTime()
                    : currentAfterTime;

            List<CandleRaw> newCandleRaws = candleRawQueryService.fetchCandleRaws(
                    symbol,
                    intervalRaw,
                    BATCH_SIZE + LOOKHEAD_BUFFER,
                    fetchAfterTime
            );

            if (newCandleRaws.isEmpty()) {
                log.info("Batch #{}: No more CandleRaw records to process", batchNumber);
                break;
            }

            log.info("Batch #{}: Fetched {} new CandleRaw records (includes {} lookhead buffer)",
                    batchNumber, newCandleRaws.size(), LOOKHEAD_BUFFER);

            // 2c. Combine lookback buffer + new candles for processing
            List<CandleRaw> allCandleRaws = new java.util.ArrayList<>(lookbackBuffer.size() + newCandleRaws.size());
            allCandleRaws.addAll(lookbackBuffer);
            allCandleRaws.addAll(newCandleRaws);

            log.info("Batch #{}: Total candles for processing: {} (lookback: {}, new: {})",
                    batchNumber, allCandleRaws.size(), lookbackBuffer.size(), newCandleRaws.size());

            // Determine how many candles to actually save
            // We skip the lookback buffer and save only new candles (up to BATCH_SIZE)
            int candlesToSave;
            int skipLookback = lookbackBuffer.size();

            // If this is the last batch (fetched less than full batch + lookhead buffer),
            // don't save the last LOOKHEAD_BUFFER candles because they won't have forward returns
            boolean isLastBatch = newCandleRaws.size() < BATCH_SIZE + LOOKHEAD_BUFFER;
            if (isLastBatch) {
                // Save only candles that have full lookhead buffer
                // Example: if we have 1030 new candles, save only 1030 - 50 = 980
                candlesToSave = Math.max(0, newCandleRaws.size() - LOOKHEAD_BUFFER);
                log.info("Batch #{}: Last batch detected - will skip last {} candles (waiting for more data for forward returns)",
                        batchNumber, LOOKHEAD_BUFFER);
            } else {
                // Normal batch - save up to BATCH_SIZE
                candlesToSave = Math.min(BATCH_SIZE, newCandleRaws.size());
            }

            // 3. Process and save batch in separate transaction (commits immediately)
            // Using self.processBatch() to ensure Spring proxy intercepts and applies @Transactional
            int savedCount = self.processBatch(symbol, allCandleRaws, candlesToSave, skipLookback, batchNumber);
            totalProcessed += savedCount;
            totalSaved += savedCount;

            log.info("Batch #{}: ✓ COMMITTED {} candles to database | Progress: {} processed, {} saved",
                    batchNumber, savedCount, totalProcessed, totalSaved);

            // Update afterTime to the last SAVED candle's openTime (not the last fetched)
            // We saved from index [skipLookback] to [skipLookback + savedCount - 1]
            if (savedCount > 0) {
                currentAfterTime = allCandleRaws.get(skipLookback + savedCount - 1).getOpenTime();
            }

            batchNumber++;

            // If we specified a limit and reached it, stop
            if (limit != null && totalProcessed >= limit) {
                log.info("Reached specified limit of {} candles", limit);
                break;
            }

            // If we saved 0 candles (last batch with insufficient lookhead buffer), stop
            if (savedCount == 0) {
                log.info("Batch #{}: Saved 0 candles - reached end of available data (waiting for more data for lookhead buffer)", batchNumber);
                break;
            }

            // If this was the last batch, stop
            if (isLastBatch) {
                log.info("Batch #{}: Processed last batch (fetched {} candles, less than full batch + lookhead buffer)",
                        batchNumber, newCandleRaws.size());
                break;
            }
        }

        log.info("═══════════════════════════════════════════════");
        log.info("Candle generation COMPLETED");
        log.info("Total batches: {}", batchNumber - 1);
        log.info("Total processed: {} candles", totalProcessed);
        log.info("Total saved: {} candles", totalSaved);
        log.info("NOTE: First {} candles are skipped (need full historical data)", LOOKBACK_BUFFER);
        log.info("      Last {} candles may remain unprocessed (waiting for lookhead buffer data)", LOOKHEAD_BUFFER);
        log.info("      Add more CandleRaw data and re-run to process remaining candles");
        log.info("═══════════════════════════════════════════════");

        return new CandleGenerationResult(
                totalProcessed,
                totalSaved,
                0, // We don't distinguish between insert/update in upsert
                symbol,
                intervalRaw
        );
    }

    /**
     * Processes a single batch of candles in its own transaction.
     * This method is called by generateCandles() and ensures each batch is committed
     * independently to the database.
     *
     * <p>This method processes ALL candleRaws (including lookback and lookhead buffers)
     * to calculate indicators correctly, but only saves the middle candlesToSave candles.</p>
     *
     * @param symbol the trading symbol
     * @param candleRaws list of CandleRaw records for this batch (includes lookback + new + lookhead)
     * @param candlesToSave number of candles to actually save (excluding buffers)
     * @param skipLookback number of lookback buffer candles to skip (0 for first batch, 550 for resume)
     * @param batchNumber the current batch number (for logging)
     * @return number of candles saved
     */
    @Transactional
    public int processBatch(String symbol, List<CandleRaw> candleRaws, int candlesToSave, int skipLookback, int batchNumber) {
        // Convert to Ta4j BarSeries for indicator calculations
        // This includes the lookback and lookhead buffers, so all indicators work correctly
        BarSeries series = BarSeriesConverter.toBarSeries(candleRaws, symbol);

        // Calculate all technical indicators for each bar
        // We calculate for all bars (including buffers) so that indicators work correctly
        List<Candle> allCandles = IntStream.range(0, series.getBarCount())
                .mapToObj(index -> calculateCandleWithIndicators(series, candleRaws, index))
                .toList();

        log.info("Batch #{}: Calculated indicators for {} candles (total with buffers)",
                batchNumber, allCandles.size());

        // Save only the middle candlesToSave candles (skip lookback buffer, exclude lookhead buffer)
        // Range: [skipLookback, skipLookback + candlesToSave)
        List<Candle> candlesToPersist = allCandles.subList(skipLookback, skipLookback + candlesToSave);

        log.info("Batch #{}: Saving {} candles (skipped {} lookback, excluding {} lookhead buffer)",
                batchNumber, candlesToPersist.size(), skipLookback, allCandles.size() - skipLookback - candlesToSave);

        // Persist candles to database (INSERT or UPDATE) - will commit at end of this method
        int savedCount = candlePersistenceService.saveAll(candlesToPersist);

        return savedCount;
    }

    /**
     * Converts null Double values to 0.0 to prevent null fields in database.
     * Used for all indicator calculations where division by zero or insufficient data may occur.
     *
     * @param value the Double value that may be null
     * @return 0.0 if value is null, otherwise the original value
     */
    private Double nvl(Double value) {
        return value != null ? value : 0.0;
    }

    /**
     * Converts null Integer values to 0 to prevent null fields in database.
     *
     * @param value the Integer value that may be null
     * @return 0 if value is null, otherwise the original value
     */
    private Integer nvl(Integer value) {
        return value != null ? value : 0;
    }

    /**
     * Converts null Boolean values to false to prevent null fields in database.
     *
     * @param value the Boolean value that may be null
     * @return false if value is null, otherwise the original value
     */
    private Boolean nvl(Boolean value) {
        return value != null ? value : false;
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

        // Map all indicators to Candle entity (all null values converted to 0)
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
                // Momentum indicators (14 fields)
                nvl(momentum.rsi7()),
                nvl(momentum.rsi14()),
                nvl(momentum.rsi21()),
                nvl(momentum.stochasticK()),
                nvl(momentum.stochasticD()),
                nvl(momentum.stochasticRsi()),
                nvl(momentum.williamsR()),
                nvl(momentum.cciNormalized()),
                nvl(momentum.mfi()),
                nvl(momentum.cmo()),
                nvl(momentum.ultimateOscillator()),
                nvl(momentum.rocPercent()),
                nvl(momentum.rsiDistanceFrom50()),
                nvl(momentum.rsiSlope()),
                // Trend indicators (22 fields)
                nvl(trend.closeEma8DistancePct()),
                nvl(trend.closeEma21DistancePct()),
                nvl(trend.closeEma50DistancePct()),
                nvl(trend.closeEma100DistancePct()),
                nvl(trend.closeEma200DistancePct()),
                nvl(trend.closeSma20DistancePct()),
                nvl(trend.closeSma50DistancePct()),
                nvl(trend.closeSma200DistancePct()),
                nvl(trend.closeVwmaDistancePct()),
                nvl(trend.closeHmaDistancePct()),
                nvl(trend.ema8Ema21SpreadPct()),
                nvl(trend.ema21Ema50SpreadPct()),
                nvl(trend.ema50Ema200SpreadPct()),
                nvl(trend.adx()),
                nvl(trend.plusDi()),
                nvl(trend.minusDi()),
                nvl(trend.diSpreadNormalized()),
                nvl(trend.aroonUp()),
                nvl(trend.aroonDown()),
                nvl(trend.aroonOscillator()),
                nvl(trend.linearRegressionSlopePct()),
                nvl(trend.maSlopePct()),
                // Volatility indicators (12 fields)
                nvl(volatility.atrPct()),
                nvl(volatility.atr721Ratio()),
                nvl(volatility.bbPercentB()),
                nvl(volatility.bbWidthPct()),
                nvl(volatility.bbPosition()),
                nvl(volatility.keltnerPercentK()),
                nvl(volatility.keltnerWidthPct()),
                nvl(volatility.rangePct()),
                nvl(volatility.rangeAtrRatio()),
                nvl(volatility.atrPercentile()),
                nvl(volatility.stddevPct()),
                nvl(volatility.stddev1450Ratio()),
                // Volume indicators (9 fields)
                nvl(volume.rvolSma20()),
                nvl(volume.rvolSma50()),
                nvl(volume.volumePercentile()),
                nvl(volume.volumeNormalized()),
                nvl(volume.obvChangePct()),
                nvl(volume.cmf()),
                nvl(volume.volumeRocPct()),
                nvl(volume.upVolumeRatio()),
                nvl(volume.volumePressure()),
                // Price action indicators (20 fields)
                nvl(priceAction.bodyRangeRatio()),
                nvl(priceAction.upperWickRangeRatio()),
                nvl(priceAction.lowerWickRangeRatio()),
                nvl(priceAction.closePositionInRange()),
                nvl(priceAction.bodyPct()),
                nvl(priceAction.gapPct()),
                nvl(priceAction.isBullish()),
                nvl(priceAction.return1()),
                nvl(priceAction.return3()),
                nvl(priceAction.return5()),
                nvl(priceAction.return10()),
                nvl(priceAction.return20()),
                nvl(priceAction.return50()),
                nvl(priceAction.highestHighDistancePct()),
                nvl(priceAction.lowestLowDistancePct()),
                nvl(priceAction.positionInRangeN()),
                nvl(priceAction.consecutiveBullishRatio()),
                nvl(priceAction.consecutiveBearishRatio()),
                nvl(priceAction.higherHighsRatio()),
                nvl(priceAction.higherClosesRatio()),
                // Statistical indicators (10 fields)
                nvl(statistical.closePercentile100()),
                nvl(statistical.closePercentile500()),
                nvl(statistical.closeZscore20()),
                nvl(statistical.rsiPercentile()),
                nvl(statistical.volumePercentileStat()),
                nvl(statistical.atrPercentileStat()),
                nvl(statistical.skewnessReturns()),
                nvl(statistical.kurtosisReturns()),
                nvl(statistical.drawdownPct()),
                nvl(statistical.drawupPct()),
                // MACD indicators (6 fields)
                nvl(macd.macdPct()),
                nvl(macd.macdSignalPct()),
                nvl(macd.macdHistogramPct()),
                nvl(macd.macdHistogramChange()),
                nvl(macd.macdGtSignal()),
                nvl(macd.macdGtZero()),
                // Support/Resistance indicators (8 fields)
                nvl(supportResistance.closePivotDistancePct()),
                nvl(supportResistance.closeS1DistancePct()),
                nvl(supportResistance.closeR1DistancePct()),
                nvl(supportResistance.closePrevDayHighPct()),
                nvl(supportResistance.closePrevDayLowPct()),
                nvl(supportResistance.closePrevWeekHighPct()),
                nvl(supportResistance.closeRoundNumberDistancePct()),
                nvl(supportResistance.closeVwapDistancePct()),
                // Lag features (19 fields)
                nvl(lagFeatures.returnLag1()),
                nvl(lagFeatures.returnLag2()),
                nvl(lagFeatures.returnLag3()),
                nvl(lagFeatures.returnLag5()),
                nvl(lagFeatures.returnLag10()),
                nvl(lagFeatures.rsiLag1()),
                nvl(lagFeatures.rsiLag2()),
                nvl(lagFeatures.rsiLag3()),
                nvl(lagFeatures.rsiChange()),
                nvl(lagFeatures.atrPctLag1()),
                nvl(lagFeatures.atrPctLag2()),
                nvl(lagFeatures.rvolLag1()),
                nvl(lagFeatures.rvolLag2()),
                nvl(lagFeatures.bbPercentBLag1()),
                nvl(lagFeatures.bbPercentBLag2()),
                nvl(lagFeatures.adxLag1()),
                nvl(lagFeatures.adxLag2()),
                nvl(lagFeatures.bodyPctLag1()),
                nvl(lagFeatures.bodyPctLag2()),
                // Composite scores (5 fields)
                nvl(compositeScores.trendScore()),
                nvl(compositeScores.bullishPatternsRatio()),
                nvl(compositeScores.momentumAgreement()),
                nvl(compositeScores.volatilityVsTrend()),
                nvl(compositeScores.volumeConfirmation()),
                // Volume Profile indicators (14 fields)
                nvl(volumeProfile.pocPrice()),
                nvl(volumeProfile.pocIndex()),
                nvl(volumeProfile.pocVolumePct()),
                nvl(volumeProfile.pocPositionInRange()),
                nvl(volumeProfile.vahPrice()),
                nvl(volumeProfile.valPrice()),
                nvl(volumeProfile.valueAreaPct()),
                nvl(volumeProfile.valueAreaVolumePct()),
                nvl(volumeProfile.volumeAbovePocPct()),
                nvl(volumeProfile.volumeBelowPocPct()),
                nvl(volumeProfile.volumeImbalance()),
                nvl(volumeProfile.highVolumeNodesCount()),
                nvl(volumeProfile.lowVolumeNodesCount()),
                nvl(volumeProfile.volumeConcentration()),
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
