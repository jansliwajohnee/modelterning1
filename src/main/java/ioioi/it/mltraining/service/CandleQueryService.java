package ioioi.it.mltraining.service;

import ioioi.it.mltraining.dto.CandleCheckReport;
import ioioi.it.mltraining.dto.CandleCheckReport.MissingCandleInfo;
import ioioi.it.mltraining.dto.CandleCheckReport.NullFieldInfo;
import ioioi.it.mltraining.entity.Candle;
import ioioi.it.mltraining.entity.CandleRaw;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for querying Candle (processed indicators) data from the database.
 * Handles read operations for processed candle data with indicators.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CandleQueryService {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    /**
     * Custom RowMapper for Candle entity that properly handles PostgreSQL timestamptz conversion to ZonedDateTime
     */
    private static final RowMapper<Candle> CANDLE_ROW_MAPPER = (rs, rowNum) -> {
        Candle candle = new Candle();

        candle.setId(getLong(rs, "id"));
        candle.setSymbol(rs.getString("symbol"));
        candle.setInterval(rs.getString("interval"));
        candle.setOpenTime(getZonedDateTime(rs, "open_time"));
        candle.setCloseTime(getZonedDateTime(rs, "close_time"));
        candle.setOpen(getDouble(rs, "open"));
        candle.setClose(getDouble(rs, "close"));
        candle.setLow(getDouble(rs, "low"));
        candle.setHigh(getDouble(rs, "high"));
        candle.setVolume(getDouble(rs, "volume"));
        candle.setTurnover(getDouble(rs, "turnover"));

        // Momentum indicators
        candle.setRsi7(getDouble(rs, "rsi_7"));
        candle.setRsi14(getDouble(rs, "rsi_14"));
        candle.setRsi21(getDouble(rs, "rsi_21"));
        candle.setStochasticK(getDouble(rs, "stochastic_k"));
        candle.setStochasticD(getDouble(rs, "stochastic_d"));
        candle.setStochasticRsi(getDouble(rs, "stochastic_rsi"));
        candle.setWilliamsR(getDouble(rs, "williams_r"));
        candle.setCciNormalized(getDouble(rs, "cci_normalized"));
        candle.setMfi(getDouble(rs, "mfi"));
        candle.setCmo(getDouble(rs, "cmo"));
        candle.setUltimateOscillator(getDouble(rs, "ultimate_oscillator"));
        candle.setRocPercent(getDouble(rs, "roc_percent"));
        candle.setRsiDistanceFrom50(getDouble(rs, "rsi_distance_from_50"));
        candle.setRsiSlope(getDouble(rs, "rsi_slope"));

        // Trend indicators
        candle.setCloseEma8DistancePct(getDouble(rs, "close_ema8_distance_pct"));
        candle.setCloseEma21DistancePct(getDouble(rs, "close_ema21_distance_pct"));
        candle.setCloseEma50DistancePct(getDouble(rs, "close_ema50_distance_pct"));
        candle.setCloseEma100DistancePct(getDouble(rs, "close_ema100_distance_pct"));
        candle.setCloseEma200DistancePct(getDouble(rs, "close_ema200_distance_pct"));
        candle.setCloseSma20DistancePct(getDouble(rs, "close_sma20_distance_pct"));
        candle.setCloseSma50DistancePct(getDouble(rs, "close_sma50_distance_pct"));
        candle.setCloseSma200DistancePct(getDouble(rs, "close_sma200_distance_pct"));
        candle.setCloseVwmaDistancePct(getDouble(rs, "close_vwma_distance_pct"));
        candle.setCloseHmaDistancePct(getDouble(rs, "close_hma_distance_pct"));
        candle.setEma8Ema21SpreadPct(getDouble(rs, "ema8_ema21_spread_pct"));
        candle.setEma21Ema50SpreadPct(getDouble(rs, "ema21_ema50_spread_pct"));
        candle.setEma50Ema200SpreadPct(getDouble(rs, "ema50_ema200_spread_pct"));
        candle.setAdx(getDouble(rs, "adx"));
        candle.setPlusDi(getDouble(rs, "plus_di"));
        candle.setMinusDi(getDouble(rs, "minus_di"));
        candle.setDiSpreadNormalized(getDouble(rs, "di_spread_normalized"));
        candle.setAroonUp(getDouble(rs, "aroon_up"));
        candle.setAroonDown(getDouble(rs, "aroon_down"));
        candle.setAroonOscillator(getDouble(rs, "aroon_oscillator"));
        candle.setLinearRegressionSlopePct(getDouble(rs, "linear_regression_slope_pct"));
        candle.setMaSlopePct(getDouble(rs, "ma_slope_pct"));

        // Volatility indicators
        candle.setAtrPct(getDouble(rs, "atr_pct"));
        candle.setAtr721Ratio(getDouble(rs, "atr_7_21_ratio"));
        candle.setBbPercentB(getDouble(rs, "bb_percent_b"));
        candle.setBbWidthPct(getDouble(rs, "bb_width_pct"));
        candle.setBbPosition(getDouble(rs, "bb_position"));
        candle.setKeltnerPercentK(getDouble(rs, "keltner_percent_k"));
        candle.setKeltnerWidthPct(getDouble(rs, "keltner_width_pct"));
        candle.setRangePct(getDouble(rs, "range_pct"));
        candle.setRangeAtrRatio(getDouble(rs, "range_atr_ratio"));
        candle.setAtrPercentile(getDouble(rs, "atr_percentile"));
        candle.setStddevPct(getDouble(rs, "stddev_pct"));
        candle.setStddev1450Ratio(getDouble(rs, "stddev_14_50_ratio"));

        // Volume indicators
        candle.setRvolSma20(getDouble(rs, "rvol_sma20"));
        candle.setRvolSma50(getDouble(rs, "rvol_sma50"));
        candle.setVolumePercentile(getDouble(rs, "volume_percentile"));
        candle.setVolumeNormalized(getDouble(rs, "volume_normalized"));
        candle.setObvChangePct(getDouble(rs, "obv_change_pct"));
        candle.setCmf(getDouble(rs, "cmf"));
        candle.setVolumeRocPct(getDouble(rs, "volume_roc_pct"));
        candle.setUpVolumeRatio(getDouble(rs, "up_volume_ratio"));
        candle.setVolumePressure(getDouble(rs, "volume_pressure"));

        // Price action indicators
        candle.setBodyRangeRatio(getDouble(rs, "body_range_ratio"));
        candle.setUpperWickRangeRatio(getDouble(rs, "upper_wick_range_ratio"));
        candle.setLowerWickRangeRatio(getDouble(rs, "lower_wick_range_ratio"));
        candle.setClosePositionInRange(getDouble(rs, "close_position_in_range"));
        candle.setBodyPct(getDouble(rs, "body_pct"));
        candle.setGapPct(getDouble(rs, "gap_pct"));
        candle.setIsBullish(getBoolean(rs, "is_bullish"));
        candle.setReturn1(getDouble(rs, "return_1"));
        candle.setReturn3(getDouble(rs, "return_3"));
        candle.setReturn5(getDouble(rs, "return_5"));
        candle.setReturn10(getDouble(rs, "return_10"));
        candle.setReturn20(getDouble(rs, "return_20"));
        candle.setReturn50(getDouble(rs, "return_50"));
        candle.setHighestHighDistancePct(getDouble(rs, "highest_high_distance_pct"));
        candle.setLowestLowDistancePct(getDouble(rs, "lowest_low_distance_pct"));
        candle.setPositionInRangeN(getDouble(rs, "position_in_range_n"));
        candle.setConsecutiveBullishRatio(getDouble(rs, "consecutive_bullish_ratio"));
        candle.setConsecutiveBearishRatio(getDouble(rs, "consecutive_bearish_ratio"));
        candle.setHigherHighsRatio(getDouble(rs, "higher_highs_ratio"));
        candle.setHigherClosesRatio(getDouble(rs, "higher_closes_ratio"));

        // Statistical indicators
        candle.setClosePercentile100(getDouble(rs, "close_percentile_100"));
        candle.setClosePercentile500(getDouble(rs, "close_percentile_500"));
        candle.setCloseZscore20(getDouble(rs, "close_zscore_20"));
        candle.setRsiPercentile(getDouble(rs, "rsi_percentile"));
        candle.setVolumePercentileStat(getDouble(rs, "volume_percentile_stat"));
        candle.setAtrPercentileStat(getDouble(rs, "atr_percentile_stat"));
        candle.setSkewnessReturns(getDouble(rs, "skewness_returns"));
        candle.setKurtosisReturns(getDouble(rs, "kurtosis_returns"));
        candle.setDrawdownPct(getDouble(rs, "drawdown_pct"));
        candle.setDrawupPct(getDouble(rs, "drawup_pct"));

        // MACD indicators
        candle.setMacdPct(getDouble(rs, "macd_pct"));
        candle.setMacdSignalPct(getDouble(rs, "macd_signal_pct"));
        candle.setMacdHistogramPct(getDouble(rs, "macd_histogram_pct"));
        candle.setMacdHistogramChange(getDouble(rs, "macd_histogram_change"));
        candle.setMacdGtSignal(getBoolean(rs, "macd_gt_signal"));
        candle.setMacdGtZero(getBoolean(rs, "macd_gt_zero"));

        // Support/Resistance indicators
        candle.setClosePivotDistancePct(getDouble(rs, "close_pivot_distance_pct"));
        candle.setCloseS1DistancePct(getDouble(rs, "close_s1_distance_pct"));
        candle.setCloseR1DistancePct(getDouble(rs, "close_r1_distance_pct"));
        candle.setClosePrevDayHighPct(getDouble(rs, "close_prev_day_high_pct"));
        candle.setClosePrevDayLowPct(getDouble(rs, "close_prev_day_low_pct"));
        candle.setClosePrevWeekHighPct(getDouble(rs, "close_prev_week_high_pct"));
        candle.setCloseRoundNumberDistancePct(getDouble(rs, "close_round_number_distance_pct"));
        candle.setCloseVwapDistancePct(getDouble(rs, "close_vwap_distance_pct"));

        // Lag features
        candle.setReturnLag1(getDouble(rs, "return_lag_1"));
        candle.setReturnLag2(getDouble(rs, "return_lag_2"));
        candle.setReturnLag3(getDouble(rs, "return_lag_3"));
        candle.setReturnLag5(getDouble(rs, "return_lag_5"));
        candle.setReturnLag10(getDouble(rs, "return_lag_10"));
        candle.setRsiLag1(getDouble(rs, "rsi_lag_1"));
        candle.setRsiLag2(getDouble(rs, "rsi_lag_2"));
        candle.setRsiLag3(getDouble(rs, "rsi_lag_3"));
        candle.setRsiChange(getDouble(rs, "rsi_change"));
        candle.setAtrPctLag1(getDouble(rs, "atr_pct_lag_1"));
        candle.setAtrPctLag2(getDouble(rs, "atr_pct_lag_2"));
        candle.setRvolLag1(getDouble(rs, "rvol_lag_1"));
        candle.setRvolLag2(getDouble(rs, "rvol_lag_2"));
        candle.setBbPercentBLag1(getDouble(rs, "bb_percent_b_lag_1"));
        candle.setBbPercentBLag2(getDouble(rs, "bb_percent_b_lag_2"));
        candle.setAdxLag1(getDouble(rs, "adx_lag_1"));
        candle.setAdxLag2(getDouble(rs, "adx_lag_2"));
        candle.setBodyPctLag1(getDouble(rs, "body_pct_lag_1"));
        candle.setBodyPctLag2(getDouble(rs, "body_pct_lag_2"));

        // Composite indicators
        candle.setTrendScore(getDouble(rs, "trend_score"));
        candle.setBullishPatternsRatio(getDouble(rs, "bullish_patterns_ratio"));
        candle.setMomentumAgreement(getDouble(rs, "momentum_agreement"));
        candle.setVolatilityVsTrend(getDouble(rs, "volatility_vs_trend"));
        candle.setVolumeConfirmation(getBoolean(rs, "volume_confirmation"));

        // Volume profile indicators
        candle.setVpPocPrice(getDouble(rs, "vp_poc_price"));
        candle.setVpPocIndex(getInteger(rs, "vp_poc_index"));
        candle.setVpPocVolumePct(getDouble(rs, "vp_poc_volume_pct"));
        candle.setVpPocPositionInRange(getDouble(rs, "vp_poc_position_in_range"));
        candle.setVpVahPrice(getDouble(rs, "vp_vah_price"));
        candle.setVpValPrice(getDouble(rs, "vp_val_price"));
        candle.setVpValueAreaPct(getDouble(rs, "vp_value_area_pct"));
        candle.setVpValueAreaVolumePct(getDouble(rs, "vp_value_area_volume_pct"));
        candle.setVpVolumeAbovePocPct(getDouble(rs, "vp_volume_above_poc_pct"));
        candle.setVpVolumeBelowPocPct(getDouble(rs, "vp_volume_below_poc_pct"));
        candle.setVpVolumeImbalance(getDouble(rs, "vp_volume_imbalance"));
        candle.setVpHighVolumeNodesCount(getInteger(rs, "vp_high_volume_nodes_count"));
        candle.setVpLowVolumeNodesCount(getInteger(rs, "vp_low_volume_nodes_count"));
        candle.setVpVolumeConcentration(getDouble(rs, "vp_volume_concentration"));

        return candle;
    };

    /**
     * Custom RowMapper for CandleRaw entity
     */
    private static final RowMapper<CandleRaw> CANDLE_RAW_ROW_MAPPER = (rs, rowNum) -> {
        CandleRaw candleRaw = new CandleRaw();

        candleRaw.setId(getLong(rs, "id"));
        candleRaw.setSymbol(rs.getString("symbol"));
        candleRaw.setInterval(rs.getString("interval"));
        candleRaw.setOpenTime(getZonedDateTime(rs, "open_time"));
        candleRaw.setCloseTime(getZonedDateTime(rs, "close_time"));
        candleRaw.setOpen(getDouble(rs, "open"));
        candleRaw.setClose(getDouble(rs, "close"));
        candleRaw.setLow(getDouble(rs, "low"));
        candleRaw.setHigh(getDouble(rs, "high"));
        candleRaw.setVolume(getDouble(rs, "volume"));
        candleRaw.setTurnover(getDouble(rs, "turnover"));
        candleRaw.setIsClosed(getBoolean(rs, "is_closed"));

        return candleRaw;
    };

    /**
     * Helper method to safely get ZonedDateTime from ResultSet
     */
    private static ZonedDateTime getZonedDateTime(ResultSet rs, String columnName) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(columnName);
        return timestamp != null ? ZonedDateTime.ofInstant(timestamp.toInstant(), ZoneOffset.UTC) : null;
    }

    /**
     * Helper method to safely get Double from ResultSet
     */
    private static Double getDouble(ResultSet rs, String columnName) throws SQLException {
        double value = rs.getDouble(columnName);
        return rs.wasNull() ? null : value;
    }

    /**
     * Helper method to safely get Long from ResultSet
     */
    private static Long getLong(ResultSet rs, String columnName) throws SQLException {
        long value = rs.getLong(columnName);
        return rs.wasNull() ? null : value;
    }

    /**
     * Helper method to safely get Integer from ResultSet
     */
    private static Integer getInteger(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return rs.wasNull() ? null : value;
    }

    /**
     * Helper method to safely get Boolean from ResultSet
     */
    private static Boolean getBoolean(ResultSet rs, String columnName) throws SQLException {
        boolean value = rs.getBoolean(columnName);
        return rs.wasNull() ? null : value;
    }

    private static final String FIND_LATEST_OPEN_TIME_SQL = """
            SELECT MAX(open_time) as latest_open_time
            FROM candle
            WHERE symbol = :symbol AND interval = :interval
            """;

    /**
     * Finds the latest (most recent) openTime for processed candles.
     * This is used to determine where to resume processing from CandleRaw.
     *
     * @param symbol the trading symbol (e.g., "BTCUSDT")
     * @param interval the candle interval (e.g., "1m", "5m", "1h")
     * @return Optional containing the latest openTime, or empty if no candles exist
     */
    public Optional<ZonedDateTime> findLatestOpenTime(String symbol, String interval) {
        log.debug("Finding latest openTime for symbol={}, interval={}", symbol, interval);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("symbol", symbol)
                .addValue("interval", interval);

        try {
            Timestamp latestTimestamp = jdbcTemplate.queryForObject(
                    FIND_LATEST_OPEN_TIME_SQL,
                    params,
                    Timestamp.class
            );

            if (latestTimestamp == null) {
                log.info("No processed candles found for {}/{}", symbol, interval);
                return Optional.empty();
            }

            ZonedDateTime latestOpenTime = ZonedDateTime.ofInstant(
                    latestTimestamp.toInstant(),
                    ZoneOffset.UTC
            );

            log.info("Latest processed candle openTime for {}/{}: {}", symbol, interval, latestOpenTime);
            return Optional.of(latestOpenTime);

        } catch (Exception e) {
            log.debug("Failed to find latest openTime for {}/{}: {}", symbol, interval, e.getMessage());
            return Optional.empty();
        }
    }

    private static final String FIND_ALL_CANDLES_SQL = """
            SELECT * FROM candle
            WHERE symbol = :symbol AND interval = :interval
            ORDER BY open_time ASC
            """;

    private static final String FIND_ALL_CANDLES_RAW_SQL = """
            SELECT * FROM candle_raw
            WHERE symbol = :symbol AND interval = :interval
            ORDER BY open_time ASC
            """;

    /**
     * Checks candle data integrity for a given symbol and interval.
     * Validates:
     * 1. Continuity - checks if all candles are in sequence based on openTime and interval
     * 2. Null fields - checks if any fields in candles have null values
     *
     * @param symbol the trading symbol (e.g., "BTCUSDT")
     * @param interval the candle interval (e.g., "1m", "5m", "1h")
     * @return CandleCheckReport containing validation results
     */
    public CandleCheckReport checkCandles(String symbol, String interval) {
        log.info("Checking candles for symbol={}, interval={}", symbol, interval);

        CandleCheckReport report = new CandleCheckReport();
        report.setSymbol(symbol);
        report.setInterval(interval);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("symbol", symbol)
                .addValue("interval", interval);

        List<Candle> candles = jdbcTemplate.query(
                FIND_ALL_CANDLES_SQL,
                params,
                CANDLE_ROW_MAPPER
        );

        report.setTotalCandles(candles.size());

        if (candles.isEmpty()) {
            log.info("No candles found for {}/{}", symbol, interval);
            return report;
        }

        // Check continuity
        Duration expectedDuration = parseInterval(interval);
        checkContinuity(candles, expectedDuration, report);

        // Check null fields
        checkNullFields(candles, report);

        log.info("Check complete for {}/{}. Missing: {}, With nulls: {}",
                symbol, interval, report.getMissingCandlesCount(), report.getCandlesWithNullFieldsCount());

        return report;
    }

    public CandleCheckReport checkCandlesRaw(String symbol, String interval) {
        log.info("Checking candles raw for symbol={}, interval={}", symbol, interval);

        CandleCheckReport report = new CandleCheckReport();
        report.setSymbol(symbol);
        report.setInterval(interval);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("symbol", symbol)
                .addValue("interval", interval);

        List<CandleRaw> candlesRaw = jdbcTemplate.query(
                FIND_ALL_CANDLES_RAW_SQL,
                params,
                CANDLE_RAW_ROW_MAPPER
        );

        report.setTotalCandles(candlesRaw.size());

        if (candlesRaw.isEmpty()) {
            log.info("No candles found for {}/{}", symbol, interval);
            return report;
        }

        // Check continuity
        Duration expectedDuration = parseInterval(interval);
        checkContinuityRaw(candlesRaw, expectedDuration, report);

        log.info("Check complete for {}/{}. Missing: {}, With nulls: {}",
                symbol, interval, report.getMissingCandlesCount(), report.getCandlesWithNullFieldsCount());

        return report;
    }

    /**
     * Parses interval string to Duration.
     * Supported formats: 1m, 5m, 15m, 30m, 1h, 4h, 1d, 1w
     */
    private Duration parseInterval(String interval) {
        Pattern pattern = Pattern.compile("(\\d+)([mhdw])");
        Matcher matcher = pattern.matcher(interval.toLowerCase());

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid interval format: " + interval);
        }

        int value = Integer.parseInt(matcher.group(1));
        String unit = matcher.group(2);

        return switch (unit) {
            case "m" -> Duration.ofMinutes(value);
            case "h" -> Duration.ofHours(value);
            case "d" -> Duration.ofDays(value);
            case "w" -> Duration.ofDays(value * 7L);
            default -> throw new IllegalArgumentException("Unsupported time unit: " + unit);
        };
    }

    /**
     * Checks if candles are continuous (no gaps in time sequence).
     */
    private void checkContinuity(List<Candle> candles, Duration expectedDuration, CandleCheckReport report) {
        for (int i = 0; i < candles.size() - 1; i++) {
            Candle current = candles.get(i);
            Candle next = candles.get(i + 1);

            ZonedDateTime expectedNextOpenTime = current.getOpenTime().plus(expectedDuration);
            ZonedDateTime actualNextOpenTime = next.getOpenTime();

            // If there's a gap, calculate how many candles are missing
            if (!expectedNextOpenTime.isEqual(actualNextOpenTime)) {
                long minutesDiff = Duration.between(expectedNextOpenTime, actualNextOpenTime).toMinutes();
                long expectedMinutes = expectedDuration.toMinutes();

                if (minutesDiff > 0 && minutesDiff % expectedMinutes == 0) {
                    // There are missing candles
                    long missingCount = minutesDiff / expectedMinutes;

                    for (int j = 0; j < missingCount; j++) {
                        ZonedDateTime missingOpenTime = expectedNextOpenTime.plus(expectedDuration.multipliedBy(j));
                        MissingCandleInfo info = new MissingCandleInfo();
                        info.setExpectedOpenTime(missingOpenTime);
                        info.setPreviousOpenTime(current.getOpenTime());
                        info.setNextOpenTime(actualNextOpenTime);
                        report.getMissingCandles().add(info);
                    }
                }
            }
        }
        report.setMissingCandlesCount(report.getMissingCandles().size());
    }

    /**
     * Checks if CandleRaw instances are continuous (no gaps in time sequence).
     */
    private void checkContinuityRaw(List<CandleRaw> candlesRaw, Duration expectedDuration, CandleCheckReport report) {
        for (int i = 0; i < candlesRaw.size() - 1; i++) {
            CandleRaw current = candlesRaw.get(i);
            CandleRaw next = candlesRaw.get(i + 1);

            ZonedDateTime expectedNextOpenTime = current.getOpenTime().plus(expectedDuration);
            ZonedDateTime actualNextOpenTime = next.getOpenTime();

            // If there's a gap, calculate how many candles are missing
            if (!expectedNextOpenTime.isEqual(actualNextOpenTime)) {
                long minutesDiff = Duration.between(expectedNextOpenTime, actualNextOpenTime).toMinutes();
                long expectedMinutes = expectedDuration.toMinutes();

                if (minutesDiff > 0 && minutesDiff % expectedMinutes == 0) {
                    // There are missing candles
                    long missingCount = minutesDiff / expectedMinutes;

                    for (int j = 0; j < missingCount; j++) {
                        ZonedDateTime missingOpenTime = expectedNextOpenTime.plus(expectedDuration.multipliedBy(j));
                        MissingCandleInfo info = new MissingCandleInfo();
                        info.setExpectedOpenTime(missingOpenTime);
                        info.setPreviousOpenTime(current.getOpenTime());
                        info.setNextOpenTime(actualNextOpenTime);
                        report.getMissingCandles().add(info);
                    }
                }
            }
        }
        report.setMissingCandlesCount(report.getMissingCandles().size());
    }

    /**
     * Checks for null values in candle fields using reflection.
     * Excludes: id, candlePatterns (collection), and known optional fields
     */
    private void checkNullFields(List<Candle> candles, CandleCheckReport report) {
        for (Candle candle : candles) {
            List<String> nullFields = new ArrayList<>();

            Field[] fields = Candle.class.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);

                // Skip id and candlePatterns collection
                if ("id".equals(field.getName()) || "candlePatterns".equals(field.getName())) {
                    continue;
                }

                try {
                    Object value = field.get(candle);
                    if (value == null) {
                        nullFields.add(field.getName());
                    }
                } catch (IllegalAccessException e) {
                    log.warn("Failed to access field {} for candle {}", field.getName(), candle.getId());
                }
            }

            if (!nullFields.isEmpty()) {
                NullFieldInfo info = new NullFieldInfo();
                info.setCandleId(candle.getId());
                info.setOpenTime(candle.getOpenTime());
                info.setNullFieldNames(nullFields);
                report.getNullFields().add(info);
            }
        }
        report.setCandlesWithNullFieldsCount(report.getNullFields().size());
    }
}
