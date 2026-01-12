package ioioi.it.mltraining.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

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
}
