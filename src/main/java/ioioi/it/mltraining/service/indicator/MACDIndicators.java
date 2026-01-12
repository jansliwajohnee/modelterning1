package ioioi.it.mltraining.service.indicator;

/**
 * Record holding all calculated MACD indicators for a single candle.
 * All values are normalized/scaled appropriately for LightGBM model training.
 * MACD (Moving Average Convergence Divergence) indicators measure trend momentum.
 *
 * @param macdPct MACD line as percentage of price
 * @param macdSignalPct MACD signal line as percentage of price
 * @param macdHistogramPct MACD histogram (MACD - Signal) as percentage of price
 * @param macdHistogramChange change in MACD histogram from previous bar
 * @param macdGtSignal true if MACD line is above signal line (bullish)
 * @param macdGtZero true if MACD line is above zero (bullish)
 */
public record MACDIndicators(
        Double macdPct,
        Double macdSignalPct,
        Double macdHistogramPct,
        Double macdHistogramChange,
        Boolean macdGtSignal,
        Boolean macdGtZero
) {
}
