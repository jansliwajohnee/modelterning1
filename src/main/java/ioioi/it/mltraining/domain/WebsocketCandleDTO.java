package ioioi.it.mltraining.domain;

import java.time.ZonedDateTime;

/**
 * Immutable DTO for real-time WebSocket candle data.
 * Uses Java 17 record for thread-safe, immutable data transfer.
 *
 * @param symbol Symbol identifier
 * @param interval Candlestick interval
 * @param bearish Whether the candle is bearish (close < open)
 * @param bullish Whether the candle is bullish (close > open)
 * @param openTime Candle opening timestamp
 * @param closeTime Candle closing timestamp
 * @param open Opening price
 * @param close Closing price
 * @param low Lowest price in the period
 * @param high Highest price in the period
 * @param turnover Trading turnover
 * @param volume Trading volume
 * @param isFinal Whether the candle is finalized (not updating)
 *
 * @author jan.sliwa777@gmail.com
 * @createdAt 25/10/2024
 */
public record WebsocketCandleDTO(
        String symbol,
        CandlestickInterval interval,
        Boolean bearish,
        Boolean bullish,
        ZonedDateTime openTime,
        ZonedDateTime closeTime,
        Double open,
        Double close,
        Double low,
        Double high,
        Double turnover,
        Double volume,
        Boolean isFinal
) {
    /**
     * Compact constructor for validation and consistency checks.
     */
    public WebsocketCandleDTO {
        if (symbol == null || interval == null) {
            throw new IllegalArgumentException("Symbol and interval cannot be null");
        }

        // Validate price consistency
        if (open != null && close != null && low != null && high != null) {
            if (low > high) {
                throw new IllegalArgumentException("Low price cannot exceed high price");
            }
            if (open < low || open > high) {
                throw new IllegalArgumentException("Open price must be within low-high range");
            }
            if (close < low || close > high) {
                throw new IllegalArgumentException("Close price must be within low-high range");
            }
        }

        // Validate bearish/bullish consistency
        if (open != null && close != null && bullish != null && bearish != null) {
            boolean expectedBullish = close > open;
            boolean expectedBearish = close < open;

            if (bullish != expectedBullish || bearish != expectedBearish) {
                throw new IllegalArgumentException("Bullish/bearish flags inconsistent with prices");
            }
        }
    }

    /**
     * Creates a DTO with auto-calculated bullish/bearish flags.
     *
     * @param symbol Symbol identifier
     * @param interval Candlestick interval
     * @param openTime Opening timestamp
     * @param closeTime Closing timestamp
     * @param open Opening price
     * @param close Closing price
     * @param low Lowest price
     * @param high Highest price
     * @param turnover Trading turnover
     * @param volume Trading volume
     * @param isFinal Whether candle is finalized
     * @return new WebsocketCandleDTO with calculated flags
     */
    public static WebsocketCandleDTO of(
            String symbol,
            CandlestickInterval interval,
            ZonedDateTime openTime,
            ZonedDateTime closeTime,
            Double open,
            Double close,
            Double low,
            Double high,
            Double turnover,
            Double volume,
            Boolean isFinal
    ) {
        boolean isBullish = close != null && open != null && close > open;
        boolean isBearish = close != null && open != null && close < open;

        return new WebsocketCandleDTO(
                symbol, interval, isBearish, isBullish,
                openTime, closeTime, open, close, low, high,
                turnover, volume, isFinal
        );
    }

    /**
     * Converts to standard CandleDTO.
     *
     * @return immutable CandleDTO representation
     */
    public CandleDTO toCandleDTO() {
        return new CandleDTO(
                symbol, interval, openTime, closeTime,
                open, close, low, high, volume, turnover, isFinal
        );
    }
}