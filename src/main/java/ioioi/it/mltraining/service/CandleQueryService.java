package ioioi.it.mltraining.service;

import ioioi.it.mltraining.dto.CandleCheckReport;
import ioioi.it.mltraining.dto.CandleCheckReport.MissingCandleInfo;
import ioioi.it.mltraining.dto.CandleCheckReport.NullFieldInfo;
import ioioi.it.mltraining.entity.Candle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
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
                .addValue("interval", interval+"m");

        List<Candle> candles = jdbcTemplate.query(
                FIND_ALL_CANDLES_SQL,
                params,
                new BeanPropertyRowMapper<>(Candle.class)
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
