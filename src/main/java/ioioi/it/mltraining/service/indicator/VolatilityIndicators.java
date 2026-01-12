package ioioi.it.mltraining.service.indicator;

/**
 * Record holding all calculated volatility indicators for a single candle.
 * All values are normalized/scaled appropriately for LightGBM model training.
 * Volatility indicators measure price variation and market uncertainty.
 *
 * @param atrPct ATR as percentage of current price (normalized)
 * @param atr721Ratio ratio of ATR(7) to ATR(21) for volatility trend
 * @param bbPercentB Bollinger Bands %B (0-1 range, position within bands)
 * @param bbWidthPct Bollinger Bands width as percentage of price
 * @param bbPosition position within Bollinger Bands (0-1)
 * @param keltnerPercentK Keltner Channel %K (position within channel)
 * @param keltnerWidthPct Keltner Channel width as percentage of price
 * @param rangePct current candle range as percentage of price
 * @param rangeAtrRatio ratio of current range to ATR
 * @param atrPercentile ATR percentile over lookback period (0-100)
 * @param stddevPct standard deviation as percentage of price
 * @param stddev1450Ratio ratio of StdDev(14) to StdDev(50) for volatility trend
 */
public record VolatilityIndicators(
        Double atrPct,
        Double atr721Ratio,
        Double bbPercentB,
        Double bbWidthPct,
        Double bbPosition,
        Double keltnerPercentK,
        Double keltnerWidthPct,
        Double rangePct,
        Double rangeAtrRatio,
        Double atrPercentile,
        Double stddevPct,
        Double stddev1450Ratio
) {
}
