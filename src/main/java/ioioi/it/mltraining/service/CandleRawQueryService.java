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
        return limit != null
                ? fetchWithLimit(symbol, interval, limit)
                : fetchAll(symbol, interval);
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