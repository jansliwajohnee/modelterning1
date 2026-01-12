package ioioi.it.mltraining.service;

import ioioi.it.mltraining.entity.CandleRaw;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for querying CandleRaw data from the database.
 * Handles read operations for raw candle data.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CandleRawQueryService {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final String COUNT_CANDLES_SQL = """
            SELECT COUNT(*) FROM candle_raw
            WHERE symbol = :symbol AND interval = :interval
            """;

    private static final String FETCH_CANDLES_SQL = """
            SELECT id, symbol, interval, open_time, close_time, open, close, low, high, volume, turnover, is_closed
            FROM candle_raw
            WHERE symbol = :symbol AND interval = :interval
            ORDER BY open_time ASC
            LIMIT :limit
            """;

    private static final String FETCH_ALL_CANDLES_SQL = """
            SELECT id, symbol, interval, open_time, close_time, open, close, low, high, volume, turnover, is_closed
            FROM candle_raw
            WHERE symbol = :symbol AND interval = :interval
            ORDER BY open_time ASC
            """;

    private static final String FETCH_CANDLES_AFTER_SQL = """
            SELECT id, symbol, interval, open_time, close_time, open, close, low, high, volume, turnover, is_closed
            FROM candle_raw
            WHERE symbol = :symbol AND interval = :interval AND open_time > :afterOpenTime
            ORDER BY open_time ASC
            LIMIT :limit
            """;

    private static final String FETCH_ALL_CANDLES_AFTER_SQL = """
            SELECT id, symbol, interval, open_time, close_time, open, close, low, high, volume, turnover, is_closed
            FROM candle_raw
            WHERE symbol = :symbol AND interval = :interval AND open_time > :afterOpenTime
            ORDER BY open_time ASC
            """;

    /**
     * Fetches CandleRaw records from the database ordered from oldest to newest.
     * If limit is null, fetches all records. If limit is provided, fetches up to that many records.
     *
     * @param symbol the trading symbol (e.g., "BTCUSDT")
     * @param interval the candle interval (e.g., "1m", "5m", "1h")
     * @param limit optional maximum number of records to fetch (null = all records)
     * @return list of CandleRaw records ordered by open_time ascending
     */
    public List<CandleRaw> fetchCandleRaws(String symbol, String interval, Integer limit) {
        return fetchCandleRaws(symbol, interval, limit, null);
    }

    /**
     * Fetches CandleRaw records from the database ordered from oldest to newest.
     * Optionally fetches only records after a specific openTime.
     *
     * @param symbol the trading symbol (e.g., "BTCUSDT")
     * @param interval the candle interval (e.g., "1m", "5m", "1h")
     * @param limit optional maximum number of records to fetch (null = all records)
     * @param afterOpenTime optional timestamp to fetch records after (null = from beginning)
     * @return list of CandleRaw records ordered by open_time ascending
     */
    public List<CandleRaw> fetchCandleRaws(String symbol, String interval, Integer limit, ZonedDateTime afterOpenTime) {
        log.info("fetchCandleRaws called with: symbol={}, interval={}, limit={}, afterOpenTime={}",
                symbol, interval, limit, afterOpenTime);

        // First, check how many records exist
        long totalCount = countCandleRaws(symbol, interval);
        log.info("Total CandleRaw records in database for {}/{}: {}", symbol, interval, totalCount);

        if (totalCount == 0) {
            log.warn("No CandleRaw records found in database for symbol={}, interval={}. Please populate candle_raw table first.",
                    symbol, interval);
            return List.of();
        }

        if (afterOpenTime != null) {
            log.debug("Using AFTER branch (afterOpenTime is set)");
            return limit != null
                    ? fetchAfterWithLimit(symbol, interval, limit, afterOpenTime)
                    : fetchAfterAll(symbol, interval, afterOpenTime);
        } else {
            log.debug("Using FROM BEGINNING branch (afterOpenTime is null)");
            return limit != null
                    ? fetchWithLimit(symbol, interval, limit)
                    : fetchAll(symbol, interval);
        }
    }

    /**
     * Counts total number of CandleRaw records for given symbol and interval.
     */
    private long countCandleRaws(String symbol, String interval) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("symbol", symbol)
                .addValue("interval", interval);

        Long count = jdbcTemplate.queryForObject(COUNT_CANDLES_SQL, params, Long.class);
        return count != null ? count : 0L;
    }

    /**
     * Fetches limited number of CandleRaw records from the database ordered from oldest to newest.
     *
     * @param symbol the trading symbol (e.g., "BTCUSDT")
     * @param interval the candle interval (e.g., "1m", "5m", "1h")
     * @param limit maximum number of records to fetch
     * @return list of CandleRaw records ordered by open_time ascending
     */
    private List<CandleRaw> fetchWithLimit(String symbol, String interval, int limit) {
        log.debug("Fetching {} CandleRaw records for symbol={}, interval={}", limit, symbol, interval);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("symbol", symbol)
                .addValue("interval", interval)
                .addValue("limit", limit);

        List<CandleRaw> candles = jdbcTemplate.query(FETCH_CANDLES_SQL, params, new CandleRawRowMapper());

        log.info("Fetched {} CandleRaw records for {}/{}", candles.size(), symbol, interval);
        return candles;
    }

    /**
     * Fetches all CandleRaw records from the database ordered from oldest to newest.
     *
     * @param symbol the trading symbol (e.g., "BTCUSDT")
     * @param interval the candle interval (e.g., "1m", "5m", "1h")
     * @return list of all CandleRaw records ordered by open_time ascending
     */
    private List<CandleRaw> fetchAll(String symbol, String interval) {
        log.debug("Fetching all CandleRaw records for symbol={}, interval={}", symbol, interval);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("symbol", symbol)
                .addValue("interval", interval);

        List<CandleRaw> candles = jdbcTemplate.query(FETCH_ALL_CANDLES_SQL, params, new CandleRawRowMapper());

        log.info("Fetched {} CandleRaw records for {}/{}", candles.size(), symbol, interval);
        return candles;
    }

    /**
     * Fetches limited number of CandleRaw records after a specific openTime.
     *
     * @param symbol the trading symbol (e.g., "BTCUSDT")
     * @param interval the candle interval (e.g., "1m", "5m", "1h")
     * @param limit maximum number of records to fetch
     * @param afterOpenTime fetch records with open_time greater than this
     * @return list of CandleRaw records ordered by open_time ascending
     */
    private List<CandleRaw> fetchAfterWithLimit(String symbol, String interval, int limit, ZonedDateTime afterOpenTime) {
        log.debug("Fetching {} CandleRaw records after {} for symbol={}, interval={}",
                limit, afterOpenTime, symbol, interval);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("symbol", symbol)
                .addValue("interval", interval)
                .addValue("limit", limit)
                .addValue("afterOpenTime", Timestamp.from(afterOpenTime.toInstant()));

        List<CandleRaw> candles = jdbcTemplate.query(FETCH_CANDLES_AFTER_SQL, params, new CandleRawRowMapper());

        log.info("Fetched {} CandleRaw records after {} for {}/{}", candles.size(), afterOpenTime, symbol, interval);
        return candles;
    }

    /**
     * Fetches all CandleRaw records after a specific openTime.
     *
     * @param symbol the trading symbol (e.g., "BTCUSDT")
     * @param interval the candle interval (e.g., "1m", "5m", "1h")
     * @param afterOpenTime fetch records with open_time greater than this
     * @return list of all CandleRaw records ordered by open_time ascending
     */
    private List<CandleRaw> fetchAfterAll(String symbol, String interval, ZonedDateTime afterOpenTime) {
        log.debug("Fetching all CandleRaw records after {} for symbol={}, interval={}",
                afterOpenTime, symbol, interval);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("symbol", symbol)
                .addValue("interval", interval)
                .addValue("afterOpenTime", Timestamp.from(afterOpenTime.toInstant()));

        List<CandleRaw> candles = jdbcTemplate.query(FETCH_ALL_CANDLES_AFTER_SQL, params, new CandleRawRowMapper());

        log.info("Fetched {} CandleRaw records after {} for {}/{}", candles.size(), afterOpenTime, symbol, interval);
        return candles;
    }

    /**
     * RowMapper for converting database rows to CandleRaw entities.
     */
    private static final class CandleRawRowMapper implements RowMapper<CandleRaw> {

        @Override
        public CandleRaw mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new CandleRaw(
                    rs.getLong("id"),
                    rs.getString("symbol"),
                    rs.getString("interval"),
                    mapTimestamp(rs.getTimestamp("open_time")),
                    mapTimestamp(rs.getTimestamp("close_time")),
                    getDouble(rs, "open"),
                    getDouble(rs, "close"),
                    getDouble(rs, "low"),
                    getDouble(rs, "high"),
                    getDouble(rs, "volume"),
                    getDouble(rs, "turnover"),
                    rs.getBoolean("is_closed")
            );
        }

        private ZonedDateTime mapTimestamp(Timestamp timestamp) {
            return Optional.ofNullable(timestamp)
                    .map(ts -> ZonedDateTime.ofInstant(ts.toInstant(), ZoneOffset.UTC))
                    .orElse(null);
        }

        private Double getDouble(ResultSet rs, String columnName) throws SQLException {
            double value = rs.getDouble(columnName);
            return rs.wasNull() ? null : value;
        }
    }
}