package ioioi.it.mltraining.service;

import ioioi.it.mltraining.domain.CandleDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CandleRawService {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final HistoricalCandleService historicalCandleService;

    private static final String INSERT_SQL = """
            INSERT INTO candle_raw (symbol, interval, open_time, close_time, open, close, low, high, volume, turnover, is_closed)
            VALUES (:symbol, :interval, :openTime, :closeTime, :open, :close, :low, :high, :volume, :turnover, :isClosed)
            ON CONFLICT (symbol, interval, open_time) DO NOTHING
            """;

    private static final String GET_LATEST_CANDLE_SQL = """
            SELECT open_time
            FROM candle_raw
            WHERE symbol = :symbol AND interval = :interval
            ORDER BY open_time DESC
            LIMIT 1
            """;

    public ZonedDateTime getLatestCandleTime(String symbol, String interval) {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("symbol", symbol)
                    .addValue("interval", interval);

            Timestamp timestamp = namedParameterJdbcTemplate.queryForObject(
                    GET_LATEST_CANDLE_SQL,
                    params,
                    Timestamp.class
            );

            return timestamp != null ? ZonedDateTime.ofInstant(timestamp.toInstant(), java.time.ZoneOffset.UTC) : null;
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Transactional
    public int saveCandles(List<CandleDTO> candles) {
        if (candles == null || candles.isEmpty()) {
            return 0;
        }

        long startTime = System.currentTimeMillis();

        SqlParameterSource[] batchParams = candles.stream()
                .map(candle -> new MapSqlParameterSource()
                        .addValue("symbol", candle.getSymbol())
                        .addValue("interval", candle.getInterval().toString())
                        .addValue("openTime", Timestamp.from(candle.getOpenTime().toInstant()))
                        .addValue("closeTime", Timestamp.from(candle.getCloseTime().toInstant()))
                        .addValue("open", candle.getOpen())
                        .addValue("close", candle.getClose())
                        .addValue("low", candle.getLow())
                        .addValue("high", candle.getHigh())
                        .addValue("volume", candle.getVolume())
                        .addValue("turnover", candle.getTurnover())
                        .addValue("isClosed", candle.getIsClosed()))
                .toArray(SqlParameterSource[]::new);

        int[] results = namedParameterJdbcTemplate.batchUpdate(INSERT_SQL, batchParams);

        long endTime = System.currentTimeMillis();
        int totalInserted = 0;
        for (int result : results) {
            // -2 = Statement.SUCCESS_NO_INFO (sukces, ale driver nie podaje liczby wierszy)
            // -3 = Statement.EXECUTE_FAILED (błąd)
            // >= 0 = liczba zmienionych wierszy
            if (result >= 0) {
                totalInserted += result;
            } else if (result == -2) {
                // SUCCESS_NO_INFO - liczymy jako sukces
                totalInserted += 1;
            }
            // result == -3 oznacza błąd - nie liczymy
        }

        log.info("Batch insert completed: {} candles inserted (out of {} total) in {} ms ({} candles/sec)",
                totalInserted,
                results.length,
                (endTime - startTime),
                totalInserted * 1000.0 / Math.max(1, endTime - startTime));

        return totalInserted;
    }

    public FetchAndSaveResult fetchAndSaveCandles(String symbol, String interval, Integer numberOfCandles) {
        ZonedDateTime latestCandleTime = getLatestCandleTime(symbol, interval);
        List<CandleDTO> candles;
        String mode;

        if (latestCandleTime != null) {
            // Mamy już świece w bazie - pobierz tylko nowe od ostatniej świecy
            log.info("Found latest candle at {} for {}/{}, fetching incremental data",
                    latestCandleTime, symbol, interval);

            long intervalMs = Long.parseLong(interval) * 60 * 1000L;
            ZonedDateTime fromTime = latestCandleTime.plus(java.time.Duration.ofMillis(intervalMs));

            candles = historicalCandleService.getHistoricalCandlesFromTime(symbol, interval, fromTime);
            mode = "incremental";
        } else {
            // Brak świec w bazie - pobierz określoną liczbę lub domyślnie 1000
            int candlesToFetch = numberOfCandles != null ? numberOfCandles : 1000;
            log.info("No candles found for {}/{}, fetching initial {} candles",
                    symbol, interval, candlesToFetch);

            candles = historicalCandleService.getHistoricalCandles(symbol, interval, candlesToFetch);
            mode = "initial";
        }

        log.info("Fetched {} candles from API for {}/{}", candles.size(), symbol, interval);
        if (!candles.isEmpty()) {
            log.info("First candle: {}", candles.get(0));
            log.info("Last candle: {}", candles.get(candles.size() - 1));
        }

        int savedCount = saveCandles(candles);

        log.info("Saved {} out of {} candles", savedCount, candles.size());

        return new FetchAndSaveResult(
                candles.size(),
                savedCount,
                symbol,
                interval,
                latestCandleTime,
                mode
        );
    }

    public record FetchAndSaveResult(
            int fetched,
            int saved,
            String symbol,
            String interval,
            ZonedDateTime latestCandleTimeBefore,
            String mode
    ) {}
}
