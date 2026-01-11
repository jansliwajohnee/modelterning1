package ioioi.it.mltraining.domain;

import java.time.ZonedDateTime;

/**
 * Immutable DTO for candle data transfer.
 * Uses Java 17 record for concise, type-safe representation.
 *
 * @param symbol Symbol identifier
 * @param interval Candlestick interval (e.g., 1m, 5m, 1h)
 * @param openTime Candle opening timestamp
 * @param closeTime Candle closing timestamp
 * @param open Opening price
 * @param close Closing price
 * @param low Lowest price in the period
 * @param high Highest price in the period
 * @param volume Trading volume
 * @param turnover Trading turnover
 * @param isClosed Whether the candle is finalized
 */
public record CandleDTO(
        String symbol,
        CandlestickInterval interval,
        ZonedDateTime openTime,
        ZonedDateTime closeTime,
        Double open,
        Double close,
        Double low,
        Double high,
        Double volume,
        Double turnover,
        Boolean isClosed
) {
    /**
     * Compact constructor for validation.
     * Ensures all required fields are non-null and prices are valid.
     */
    public CandleDTO {
        if (interval == null || openTime == null || closeTime == null) {
            throw new IllegalArgumentException("Interval, openTime, and closeTime cannot be null");
        }
        if (open != null && close != null && low != null && high != null) {
            if (low > high) {
                throw new IllegalArgumentException("Low price cannot exceed high price");
            }
        }
    }

    /**
     * Determines if the candle represents bullish movement.
     *
     * @return true if close > open, false otherwise
     */
    public boolean isBullish() {
        return close != null && open != null && close > open;
    }

    /**
     * Determines if the candle represents bearish movement.
     *
     * @return true if close < open, false otherwise
     */
    public boolean isBearish() {
        return close != null && open != null && close < open;
    }

    /**
     * Calculates the body size (absolute difference between open and close).
     *
     * @return body size, or 0.0 if prices are null
     */
    public double bodySize() {
        return (open != null && close != null) ? Math.abs(close - open) : 0.0;
    }

    /**
     * Calculates the total range (difference between high and low).
     *
     * @return range size, or 0.0 if prices are null
     */
    public double rangeSize() {
        return (high != null && low != null) ? high - low : 0.0;
    }
}