package ioioi.it.mltraining.service;

import ioioi.it.mltraining.entity.Candle;
import ioioi.it.mltraining.entity.CandlePattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Service for persisting Candle entities with technical indicators to the database.
 * Handles batch insert operations for optimal performance.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CandlePersistenceService {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    /**
     * Saves multiple Candle records in batch using INSERT with ON CONFLICT UPDATE.
     * Returns the count of successfully saved records.
     *
     * @param candles list of Candle entities to save
     * @return number of records saved
     */
    public int saveAll(List<Candle> candles) {
        if (candles == null || candles.isEmpty()) {
            log.warn("No candles to save");
            return 0;
        }

        log.info("Saving {} candles with patterns to database", candles.size());

        int saved = 0;
        int totalPatterns = 0;

        for (Candle candle : candles) {
            Long candleId = saveCandle(candle);
            if (candleId != null) {
                saved++;

                // Save candle patterns if present
                if (candle.getCandlePatterns() != null && !candle.getCandlePatterns().isEmpty()) {
                    int patternsCount = saveCandlePatterns(candleId, candle.getCandlePatterns());
                    totalPatterns += patternsCount;
                }
            }
        }

        log.info("Successfully saved {} candles with {} patterns", saved, totalPatterns);
        return saved;
    }

    /**
     * Saves a single Candle record using INSERT ON CONFLICT UPDATE (upsert).
     * Uses symbol, interval, and open_time as unique constraint.
     *
     * @param candle the Candle entity to save
     * @return Candle ID if saved successfully, null otherwise
     */
    private Long saveCandle(Candle candle) {
        String sql = """
                INSERT INTO candle (
                    symbol, interval, open_time, close_time, open, close, low, high, volume, turnover,
                    rsi_7, rsi_14, rsi_21, stochastic_k, stochastic_d, stochastic_rsi, williams_r,
                    cci_normalized, mfi, cmo, ultimate_oscillator, roc_percent,
                    rsi_distance_from_50, rsi_slope,
                    close_ema8_distance_pct, close_ema21_distance_pct, close_ema50_distance_pct,
                    close_ema100_distance_pct, close_ema200_distance_pct,
                    close_sma20_distance_pct, close_sma50_distance_pct, close_sma200_distance_pct,
                    close_vwma_distance_pct, close_hma_distance_pct,
                    ema8_ema21_spread_pct, ema21_ema50_spread_pct, ema50_ema200_spread_pct,
                    adx, plus_di, minus_di, di_spread_normalized,
                    aroon_up, aroon_down, aroon_oscillator,
                    linear_regression_slope_pct, ma_slope_pct,
                    atr_pct, atr_7_21_ratio,
                    bb_percent_b, bb_width_pct, bb_position,
                    keltner_percent_k, keltner_width_pct,
                    range_pct, range_atr_ratio, atr_percentile,
                    stddev_pct, stddev_14_50_ratio,
                    rvol_sma20, rvol_sma50, volume_percentile, volume_normalized,
                    obv_change_pct, cmf, volume_roc_pct, up_volume_ratio, volume_pressure,
                    body_range_ratio, upper_wick_range_ratio, lower_wick_range_ratio,
                    close_position_in_range, body_pct, gap_pct, is_bullish,
                    return_1, return_3, return_5, return_10, return_20, return_50,
                    highest_high_distance_pct, lowest_low_distance_pct, position_in_range_n,
                    consecutive_bullish_ratio, consecutive_bearish_ratio,
                    higher_highs_ratio, higher_closes_ratio,
                    close_percentile_100, close_percentile_500, close_zscore_20,
                    rsi_percentile, volume_percentile_stat, atr_percentile_stat,
                    skewness_returns, kurtosis_returns, drawdown_pct, drawup_pct,
                    macd_pct, macd_signal_pct, macd_histogram_pct, macd_histogram_change,
                    macd_gt_signal, macd_gt_zero,
                    close_pivot_distance_pct, close_s1_distance_pct, close_r1_distance_pct,
                    close_prev_day_high_pct, close_prev_day_low_pct, close_prev_week_high_pct,
                    close_round_number_distance_pct, close_vwap_distance_pct,
                    return_lag_1, return_lag_2, return_lag_3, return_lag_5, return_lag_10,
                    rsi_lag_1, rsi_lag_2, rsi_lag_3, rsi_change,
                    atr_pct_lag_1, atr_pct_lag_2, rvol_lag_1, rvol_lag_2,
                    bb_percent_b_lag_1, bb_percent_b_lag_2, adx_lag_1, adx_lag_2,
                    body_pct_lag_1, body_pct_lag_2,
                    trend_score, bullish_patterns_ratio, momentum_agreement,
                    volatility_vs_trend, volume_confirmation,
                    vp_poc_price, vp_poc_index, vp_poc_volume_pct, vp_poc_position_in_range,
                    vp_vah_price, vp_val_price, vp_value_area_pct, vp_value_area_volume_pct,
                    vp_volume_above_poc_pct, vp_volume_below_poc_pct, vp_volume_imbalance,
                    vp_high_volume_nodes_count, vp_low_volume_nodes_count, vp_volume_concentration
                ) VALUES (
                    :symbol, :interval, :openTime, :closeTime, :open, :close, :low, :high, :volume, :turnover,
                    :rsi7, :rsi14, :rsi21, :stochasticK, :stochasticD, :stochasticRsi, :williamsR,
                    :cciNormalized, :mfi, :cmo, :ultimateOscillator, :rocPercent,
                    :rsiDistanceFrom50, :rsiSlope,
                    :closeEma8DistancePct, :closeEma21DistancePct, :closeEma50DistancePct,
                    :closeEma100DistancePct, :closeEma200DistancePct,
                    :closeSma20DistancePct, :closeSma50DistancePct, :closeSma200DistancePct,
                    :closeVwmaDistancePct, :closeHmaDistancePct,
                    :ema8Ema21SpreadPct, :ema21Ema50SpreadPct, :ema50Ema200SpreadPct,
                    :adx, :plusDi, :minusDi, :diSpreadNormalized,
                    :aroonUp, :aroonDown, :aroonOscillator,
                    :linearRegressionSlopePct, :maSlopePct,
                    :atrPct, :atr721Ratio,
                    :bbPercentB, :bbWidthPct, :bbPosition,
                    :keltnerPercentK, :keltnerWidthPct,
                    :rangePct, :rangeAtrRatio, :atrPercentile,
                    :stddevPct, :stddev1450Ratio,
                    :rvolSma20, :rvolSma50, :volumePercentile, :volumeNormalized,
                    :obvChangePct, :cmf, :volumeRocPct, :upVolumeRatio, :volumePressure,
                    :bodyRangeRatio, :upperWickRangeRatio, :lowerWickRangeRatio,
                    :closePositionInRange, :bodyPct, :gapPct, :isBullish,
                    :return1, :return3, :return5, :return10, :return20, :return50,
                    :highestHighDistancePct, :lowestLowDistancePct, :positionInRangeN,
                    :consecutiveBullishRatio, :consecutiveBearishRatio,
                    :higherHighsRatio, :higherClosesRatio,
                    :closePercentile100, :closePercentile500, :closeZscore20,
                    :rsiPercentile, :volumePercentileStat, :atrPercentileStat,
                    :skewnessReturns, :kurtosisReturns, :drawdownPct, :drawupPct,
                    :macdPct, :macdSignalPct, :macdHistogramPct, :macdHistogramChange,
                    :macdGtSignal, :macdGtZero,
                    :closePivotDistancePct, :closeS1DistancePct, :closeR1DistancePct,
                    :closePrevDayHighPct, :closePrevDayLowPct, :closePrevWeekHighPct,
                    :closeRoundNumberDistancePct, :closeVwapDistancePct,
                    :returnLag1, :returnLag2, :returnLag3, :returnLag5, :returnLag10,
                    :rsiLag1, :rsiLag2, :rsiLag3, :rsiChange,
                    :atrPctLag1, :atrPctLag2, :rvolLag1, :rvolLag2,
                    :bbPercentBLag1, :bbPercentBLag2, :adxLag1, :adxLag2,
                    :bodyPctLag1, :bodyPctLag2,
                    :trendScore, :bullishPatternsRatio, :momentumAgreement,
                    :volatilityVsTrend, :volumeConfirmation,
                    :vpPocPrice, :vpPocIndex, :vpPocVolumePct, :vpPocPositionInRange,
                    :vpVahPrice, :vpValPrice, :vpValueAreaPct, :vpValueAreaVolumePct,
                    :vpVolumeAbovePocPct, :vpVolumeBelowPocPct, :vpVolumeImbalance,
                    :vpHighVolumeNodesCount, :vpLowVolumeNodesCount, :vpVolumeConcentration
                )
                ON CONFLICT (symbol, interval, open_time)
                DO UPDATE SET
                    close_time = EXCLUDED.close_time,
                    open = EXCLUDED.open,
                    close = EXCLUDED.close,
                    low = EXCLUDED.low,
                    high = EXCLUDED.high,
                    volume = EXCLUDED.volume,
                    turnover = EXCLUDED.turnover,
                    rsi_7 = EXCLUDED.rsi_7,
                    rsi_14 = EXCLUDED.rsi_14,
                    rsi_21 = EXCLUDED.rsi_21,
                    stochastic_k = EXCLUDED.stochastic_k,
                    stochastic_d = EXCLUDED.stochastic_d,
                    stochastic_rsi = EXCLUDED.stochastic_rsi,
                    williams_r = EXCLUDED.williams_r,
                    cci_normalized = EXCLUDED.cci_normalized,
                    mfi = EXCLUDED.mfi,
                    cmo = EXCLUDED.cmo,
                    ultimate_oscillator = EXCLUDED.ultimate_oscillator,
                    roc_percent = EXCLUDED.roc_percent,
                    rsi_distance_from_50 = EXCLUDED.rsi_distance_from_50,
                    rsi_slope = EXCLUDED.rsi_slope,
                    close_ema8_distance_pct = EXCLUDED.close_ema8_distance_pct,
                    close_ema21_distance_pct = EXCLUDED.close_ema21_distance_pct,
                    close_ema50_distance_pct = EXCLUDED.close_ema50_distance_pct,
                    close_ema100_distance_pct = EXCLUDED.close_ema100_distance_pct,
                    close_ema200_distance_pct = EXCLUDED.close_ema200_distance_pct,
                    close_sma20_distance_pct = EXCLUDED.close_sma20_distance_pct,
                    close_sma50_distance_pct = EXCLUDED.close_sma50_distance_pct,
                    close_sma200_distance_pct = EXCLUDED.close_sma200_distance_pct,
                    close_vwma_distance_pct = EXCLUDED.close_vwma_distance_pct,
                    close_hma_distance_pct = EXCLUDED.close_hma_distance_pct,
                    ema8_ema21_spread_pct = EXCLUDED.ema8_ema21_spread_pct,
                    ema21_ema50_spread_pct = EXCLUDED.ema21_ema50_spread_pct,
                    ema50_ema200_spread_pct = EXCLUDED.ema50_ema200_spread_pct,
                    adx = EXCLUDED.adx,
                    plus_di = EXCLUDED.plus_di,
                    minus_di = EXCLUDED.minus_di,
                    di_spread_normalized = EXCLUDED.di_spread_normalized,
                    aroon_up = EXCLUDED.aroon_up,
                    aroon_down = EXCLUDED.aroon_down,
                    aroon_oscillator = EXCLUDED.aroon_oscillator,
                    linear_regression_slope_pct = EXCLUDED.linear_regression_slope_pct,
                    ma_slope_pct = EXCLUDED.ma_slope_pct,
                    atr_pct = EXCLUDED.atr_pct,
                    atr_7_21_ratio = EXCLUDED.atr_7_21_ratio,
                    bb_percent_b = EXCLUDED.bb_percent_b,
                    bb_width_pct = EXCLUDED.bb_width_pct,
                    bb_position = EXCLUDED.bb_position,
                    keltner_percent_k = EXCLUDED.keltner_percent_k,
                    keltner_width_pct = EXCLUDED.keltner_width_pct,
                    range_pct = EXCLUDED.range_pct,
                    range_atr_ratio = EXCLUDED.range_atr_ratio,
                    atr_percentile = EXCLUDED.atr_percentile,
                    stddev_pct = EXCLUDED.stddev_pct,
                    stddev_14_50_ratio = EXCLUDED.stddev_14_50_ratio,
                    rvol_sma20 = EXCLUDED.rvol_sma20,
                    rvol_sma50 = EXCLUDED.rvol_sma50,
                    volume_percentile = EXCLUDED.volume_percentile,
                    volume_normalized = EXCLUDED.volume_normalized,
                    obv_change_pct = EXCLUDED.obv_change_pct,
                    cmf = EXCLUDED.cmf,
                    volume_roc_pct = EXCLUDED.volume_roc_pct,
                    up_volume_ratio = EXCLUDED.up_volume_ratio,
                    volume_pressure = EXCLUDED.volume_pressure,
                    body_range_ratio = EXCLUDED.body_range_ratio,
                    upper_wick_range_ratio = EXCLUDED.upper_wick_range_ratio,
                    lower_wick_range_ratio = EXCLUDED.lower_wick_range_ratio,
                    close_position_in_range = EXCLUDED.close_position_in_range,
                    body_pct = EXCLUDED.body_pct,
                    gap_pct = EXCLUDED.gap_pct,
                    is_bullish = EXCLUDED.is_bullish,
                    return_1 = EXCLUDED.return_1,
                    return_3 = EXCLUDED.return_3,
                    return_5 = EXCLUDED.return_5,
                    return_10 = EXCLUDED.return_10,
                    return_20 = EXCLUDED.return_20,
                    return_50 = EXCLUDED.return_50,
                    highest_high_distance_pct = EXCLUDED.highest_high_distance_pct,
                    lowest_low_distance_pct = EXCLUDED.lowest_low_distance_pct,
                    position_in_range_n = EXCLUDED.position_in_range_n,
                    consecutive_bullish_ratio = EXCLUDED.consecutive_bullish_ratio,
                    consecutive_bearish_ratio = EXCLUDED.consecutive_bearish_ratio,
                    higher_highs_ratio = EXCLUDED.higher_highs_ratio,
                    higher_closes_ratio = EXCLUDED.higher_closes_ratio,
                    close_percentile_100 = EXCLUDED.close_percentile_100,
                    close_percentile_500 = EXCLUDED.close_percentile_500,
                    close_zscore_20 = EXCLUDED.close_zscore_20,
                    rsi_percentile = EXCLUDED.rsi_percentile,
                    volume_percentile_stat = EXCLUDED.volume_percentile_stat,
                    atr_percentile_stat = EXCLUDED.atr_percentile_stat,
                    skewness_returns = EXCLUDED.skewness_returns,
                    kurtosis_returns = EXCLUDED.kurtosis_returns,
                    drawdown_pct = EXCLUDED.drawdown_pct,
                    drawup_pct = EXCLUDED.drawup_pct,
                    macd_pct = EXCLUDED.macd_pct,
                    macd_signal_pct = EXCLUDED.macd_signal_pct,
                    macd_histogram_pct = EXCLUDED.macd_histogram_pct,
                    macd_histogram_change = EXCLUDED.macd_histogram_change,
                    macd_gt_signal = EXCLUDED.macd_gt_signal,
                    macd_gt_zero = EXCLUDED.macd_gt_zero,
                    close_pivot_distance_pct = EXCLUDED.close_pivot_distance_pct,
                    close_s1_distance_pct = EXCLUDED.close_s1_distance_pct,
                    close_r1_distance_pct = EXCLUDED.close_r1_distance_pct,
                    close_prev_day_high_pct = EXCLUDED.close_prev_day_high_pct,
                    close_prev_day_low_pct = EXCLUDED.close_prev_day_low_pct,
                    close_prev_week_high_pct = EXCLUDED.close_prev_week_high_pct,
                    close_round_number_distance_pct = EXCLUDED.close_round_number_distance_pct,
                    close_vwap_distance_pct = EXCLUDED.close_vwap_distance_pct,
                    return_lag_1 = EXCLUDED.return_lag_1,
                    return_lag_2 = EXCLUDED.return_lag_2,
                    return_lag_3 = EXCLUDED.return_lag_3,
                    return_lag_5 = EXCLUDED.return_lag_5,
                    return_lag_10 = EXCLUDED.return_lag_10,
                    rsi_lag_1 = EXCLUDED.rsi_lag_1,
                    rsi_lag_2 = EXCLUDED.rsi_lag_2,
                    rsi_lag_3 = EXCLUDED.rsi_lag_3,
                    rsi_change = EXCLUDED.rsi_change,
                    atr_pct_lag_1 = EXCLUDED.atr_pct_lag_1,
                    atr_pct_lag_2 = EXCLUDED.atr_pct_lag_2,
                    rvol_lag_1 = EXCLUDED.rvol_lag_1,
                    rvol_lag_2 = EXCLUDED.rvol_lag_2,
                    bb_percent_b_lag_1 = EXCLUDED.bb_percent_b_lag_1,
                    bb_percent_b_lag_2 = EXCLUDED.bb_percent_b_lag_2,
                    adx_lag_1 = EXCLUDED.adx_lag_1,
                    adx_lag_2 = EXCLUDED.adx_lag_2,
                    body_pct_lag_1 = EXCLUDED.body_pct_lag_1,
                    body_pct_lag_2 = EXCLUDED.body_pct_lag_2,
                    trend_score = EXCLUDED.trend_score,
                    bullish_patterns_ratio = EXCLUDED.bullish_patterns_ratio,
                    momentum_agreement = EXCLUDED.momentum_agreement,
                    volatility_vs_trend = EXCLUDED.volatility_vs_trend,
                    volume_confirmation = EXCLUDED.volume_confirmation,
                    vp_poc_price = EXCLUDED.vp_poc_price,
                    vp_poc_index = EXCLUDED.vp_poc_index,
                    vp_poc_volume_pct = EXCLUDED.vp_poc_volume_pct,
                    vp_poc_position_in_range = EXCLUDED.vp_poc_position_in_range,
                    vp_vah_price = EXCLUDED.vp_vah_price,
                    vp_val_price = EXCLUDED.vp_val_price,
                    vp_value_area_pct = EXCLUDED.vp_value_area_pct,
                    vp_value_area_volume_pct = EXCLUDED.vp_value_area_volume_pct,
                    vp_volume_above_poc_pct = EXCLUDED.vp_volume_above_poc_pct,
                    vp_volume_below_poc_pct = EXCLUDED.vp_volume_below_poc_pct,
                    vp_volume_imbalance = EXCLUDED.vp_volume_imbalance,
                    vp_high_volume_nodes_count = EXCLUDED.vp_high_volume_nodes_count,
                    vp_low_volume_nodes_count = EXCLUDED.vp_low_volume_nodes_count,
                    vp_volume_concentration = EXCLUDED.vp_volume_concentration
                RETURNING id
                """;

        try {
            MapSqlParameterSource params = mapCandleToParams(candle);
            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(sql, params, keyHolder, new String[]{"id"});

            Number key = keyHolder.getKey();
            return key != null ? key.longValue() : null;
        } catch (Exception e) {
            log.error("Failed to save candle for {}/{} at {}: {}",
                    candle.getSymbol(), candle.getInterval(), candle.getOpenTime(), e.getMessage());
            return null;
        }
    }

    /**
     * Saves candle patterns for a specific candle.
     *
     * @param candleId the Candle ID (FK)
     * @param patterns set of patterns to save
     * @return number of patterns saved
     */
    private int saveCandlePatterns(Long candleId, Set<CandlePattern> patterns) {
        if (patterns == null || patterns.isEmpty()) {
            return 0;
        }

        String sql = """
                INSERT INTO candle_pattern (candle_indicators_id, pattern_name, pattern_strength)
                VALUES (:candleId, :patternName, :patternStrength)
                ON CONFLICT (candle_indicators_id, pattern_name)
                DO UPDATE SET pattern_strength = EXCLUDED.pattern_strength
                """;

        int saved = 0;
        for (CandlePattern pattern : patterns) {
            try {
                MapSqlParameterSource params = new MapSqlParameterSource()
                        .addValue("candleId", candleId)
                        .addValue("patternName", pattern.getPatternName())
                        .addValue("patternStrength", pattern.getPatternStrength());

                jdbcTemplate.update(sql, params);
                saved++;
            } catch (Exception e) {
                log.error("Failed to save pattern {} for candle {}: {}",
                        pattern.getPatternName(), candleId, e.getMessage());
            }
        }

        return saved;
    }

    /**
     * Maps Candle entity fields to SQL parameters.
     */
    private MapSqlParameterSource mapCandleToParams(Candle candle) {
        return new MapSqlParameterSource()
                // Raw data
                .addValue("symbol", candle.getSymbol())
                .addValue("interval", candle.getInterval())
                .addValue("openTime", toTimestamp(candle.getOpenTime()))
                .addValue("closeTime", toTimestamp(candle.getCloseTime()))
                .addValue("open", candle.getOpen())
                .addValue("close", candle.getClose())
                .addValue("low", candle.getLow())
                .addValue("high", candle.getHigh())
                .addValue("volume", candle.getVolume())
                .addValue("turnover", candle.getTurnover())
                // Momentum
                .addValue("rsi7", candle.getRsi7())
                .addValue("rsi14", candle.getRsi14())
                .addValue("rsi21", candle.getRsi21())
                .addValue("stochasticK", candle.getStochasticK())
                .addValue("stochasticD", candle.getStochasticD())
                .addValue("stochasticRsi", candle.getStochasticRsi())
                .addValue("williamsR", candle.getWilliamsR())
                .addValue("cciNormalized", candle.getCciNormalized())
                .addValue("mfi", candle.getMfi())
                .addValue("cmo", candle.getCmo())
                .addValue("ultimateOscillator", candle.getUltimateOscillator())
                .addValue("rocPercent", candle.getRocPercent())
                .addValue("rsiDistanceFrom50", candle.getRsiDistanceFrom50())
                .addValue("rsiSlope", candle.getRsiSlope())
                // Trend
                .addValue("closeEma8DistancePct", candle.getCloseEma8DistancePct())
                .addValue("closeEma21DistancePct", candle.getCloseEma21DistancePct())
                .addValue("closeEma50DistancePct", candle.getCloseEma50DistancePct())
                .addValue("closeEma100DistancePct", candle.getCloseEma100DistancePct())
                .addValue("closeEma200DistancePct", candle.getCloseEma200DistancePct())
                .addValue("closeSma20DistancePct", candle.getCloseSma20DistancePct())
                .addValue("closeSma50DistancePct", candle.getCloseSma50DistancePct())
                .addValue("closeSma200DistancePct", candle.getCloseSma200DistancePct())
                .addValue("closeVwmaDistancePct", candle.getCloseVwmaDistancePct())
                .addValue("closeHmaDistancePct", candle.getCloseHmaDistancePct())
                .addValue("ema8Ema21SpreadPct", candle.getEma8Ema21SpreadPct())
                .addValue("ema21Ema50SpreadPct", candle.getEma21Ema50SpreadPct())
                .addValue("ema50Ema200SpreadPct", candle.getEma50Ema200SpreadPct())
                .addValue("adx", candle.getAdx())
                .addValue("plusDi", candle.getPlusDi())
                .addValue("minusDi", candle.getMinusDi())
                .addValue("diSpreadNormalized", candle.getDiSpreadNormalized())
                .addValue("aroonUp", candle.getAroonUp())
                .addValue("aroonDown", candle.getAroonDown())
                .addValue("aroonOscillator", candle.getAroonOscillator())
                .addValue("linearRegressionSlopePct", candle.getLinearRegressionSlopePct())
                .addValue("maSlopePct", candle.getMaSlopePct())
                // Volatility
                .addValue("atrPct", candle.getAtrPct())
                .addValue("atr721Ratio", candle.getAtr721Ratio())
                .addValue("bbPercentB", candle.getBbPercentB())
                .addValue("bbWidthPct", candle.getBbWidthPct())
                .addValue("bbPosition", candle.getBbPosition())
                .addValue("keltnerPercentK", candle.getKeltnerPercentK())
                .addValue("keltnerWidthPct", candle.getKeltnerWidthPct())
                .addValue("rangePct", candle.getRangePct())
                .addValue("rangeAtrRatio", candle.getRangeAtrRatio())
                .addValue("atrPercentile", candle.getAtrPercentile())
                .addValue("stddevPct", candle.getStddevPct())
                .addValue("stddev1450Ratio", candle.getStddev1450Ratio())
                // Volume
                .addValue("rvolSma20", candle.getRvolSma20())
                .addValue("rvolSma50", candle.getRvolSma50())
                .addValue("volumePercentile", candle.getVolumePercentile())
                .addValue("volumeNormalized", candle.getVolumeNormalized())
                .addValue("obvChangePct", candle.getObvChangePct())
                .addValue("cmf", candle.getCmf())
                .addValue("volumeRocPct", candle.getVolumeRocPct())
                .addValue("upVolumeRatio", candle.getUpVolumeRatio())
                .addValue("volumePressure", candle.getVolumePressure())
                // Price Action
                .addValue("bodyRangeRatio", candle.getBodyRangeRatio())
                .addValue("upperWickRangeRatio", candle.getUpperWickRangeRatio())
                .addValue("lowerWickRangeRatio", candle.getLowerWickRangeRatio())
                .addValue("closePositionInRange", candle.getClosePositionInRange())
                .addValue("bodyPct", candle.getBodyPct())
                .addValue("gapPct", candle.getGapPct())
                .addValue("isBullish", candle.getIsBullish())
                .addValue("return1", candle.getReturn1())
                .addValue("return3", candle.getReturn3())
                .addValue("return5", candle.getReturn5())
                .addValue("return10", candle.getReturn10())
                .addValue("return20", candle.getReturn20())
                .addValue("return50", candle.getReturn50())
                .addValue("highestHighDistancePct", candle.getHighestHighDistancePct())
                .addValue("lowestLowDistancePct", candle.getLowestLowDistancePct())
                .addValue("positionInRangeN", candle.getPositionInRangeN())
                .addValue("consecutiveBullishRatio", candle.getConsecutiveBullishRatio())
                .addValue("consecutiveBearishRatio", candle.getConsecutiveBearishRatio())
                .addValue("higherHighsRatio", candle.getHigherHighsRatio())
                .addValue("higherClosesRatio", candle.getHigherClosesRatio())
                // Statistical
                .addValue("closePercentile100", candle.getClosePercentile100())
                .addValue("closePercentile500", candle.getClosePercentile500())
                .addValue("closeZscore20", candle.getCloseZscore20())
                .addValue("rsiPercentile", candle.getRsiPercentile())
                .addValue("volumePercentileStat", candle.getVolumePercentileStat())
                .addValue("atrPercentileStat", candle.getAtrPercentileStat())
                .addValue("skewnessReturns", candle.getSkewnessReturns())
                .addValue("kurtosisReturns", candle.getKurtosisReturns())
                .addValue("drawdownPct", candle.getDrawdownPct())
                .addValue("drawupPct", candle.getDrawupPct())
                // MACD
                .addValue("macdPct", candle.getMacdPct())
                .addValue("macdSignalPct", candle.getMacdSignalPct())
                .addValue("macdHistogramPct", candle.getMacdHistogramPct())
                .addValue("macdHistogramChange", candle.getMacdHistogramChange())
                .addValue("macdGtSignal", candle.getMacdGtSignal())
                .addValue("macdGtZero", candle.getMacdGtZero())
                // Support/Resistance
                .addValue("closePivotDistancePct", candle.getClosePivotDistancePct())
                .addValue("closeS1DistancePct", candle.getCloseS1DistancePct())
                .addValue("closeR1DistancePct", candle.getCloseR1DistancePct())
                .addValue("closePrevDayHighPct", candle.getClosePrevDayHighPct())
                .addValue("closePrevDayLowPct", candle.getClosePrevDayLowPct())
                .addValue("closePrevWeekHighPct", candle.getClosePrevWeekHighPct())
                .addValue("closeRoundNumberDistancePct", candle.getCloseRoundNumberDistancePct())
                .addValue("closeVwapDistancePct", candle.getCloseVwapDistancePct())
                // Lag Features
                .addValue("returnLag1", candle.getReturnLag1())
                .addValue("returnLag2", candle.getReturnLag2())
                .addValue("returnLag3", candle.getReturnLag3())
                .addValue("returnLag5", candle.getReturnLag5())
                .addValue("returnLag10", candle.getReturnLag10())
                .addValue("rsiLag1", candle.getRsiLag1())
                .addValue("rsiLag2", candle.getRsiLag2())
                .addValue("rsiLag3", candle.getRsiLag3())
                .addValue("rsiChange", candle.getRsiChange())
                .addValue("atrPctLag1", candle.getAtrPctLag1())
                .addValue("atrPctLag2", candle.getAtrPctLag2())
                .addValue("rvolLag1", candle.getRvolLag1())
                .addValue("rvolLag2", candle.getRvolLag2())
                .addValue("bbPercentBLag1", candle.getBbPercentBLag1())
                .addValue("bbPercentBLag2", candle.getBbPercentBLag2())
                .addValue("adxLag1", candle.getAdxLag1())
                .addValue("adxLag2", candle.getAdxLag2())
                .addValue("bodyPctLag1", candle.getBodyPctLag1())
                .addValue("bodyPctLag2", candle.getBodyPctLag2())
                // Composite
                .addValue("trendScore", candle.getTrendScore())
                .addValue("bullishPatternsRatio", candle.getBullishPatternsRatio())
                .addValue("momentumAgreement", candle.getMomentumAgreement())
                .addValue("volatilityVsTrend", candle.getVolatilityVsTrend())
                .addValue("volumeConfirmation", candle.getVolumeConfirmation())
                // Volume Profile
                .addValue("vpPocPrice", candle.getVpPocPrice())
                .addValue("vpPocIndex", candle.getVpPocIndex())
                .addValue("vpPocVolumePct", candle.getVpPocVolumePct())
                .addValue("vpPocPositionInRange", candle.getVpPocPositionInRange())
                .addValue("vpVahPrice", candle.getVpVahPrice())
                .addValue("vpValPrice", candle.getVpValPrice())
                .addValue("vpValueAreaPct", candle.getVpValueAreaPct())
                .addValue("vpValueAreaVolumePct", candle.getVpValueAreaVolumePct())
                .addValue("vpVolumeAbovePocPct", candle.getVpVolumeAbovePocPct())
                .addValue("vpVolumeBelowPocPct", candle.getVpVolumeBelowPocPct())
                .addValue("vpVolumeImbalance", candle.getVpVolumeImbalance())
                .addValue("vpHighVolumeNodesCount", candle.getVpHighVolumeNodesCount())
                .addValue("vpLowVolumeNodesCount", candle.getVpLowVolumeNodesCount())
                .addValue("vpVolumeConcentration", candle.getVpVolumeConcentration());
    }

    private Timestamp toTimestamp(ZonedDateTime zdt) {
        return Optional.ofNullable(zdt)
                .map(z -> Timestamp.from(z.toInstant()))
                .orElse(null);
    }
}
