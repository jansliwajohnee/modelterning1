package ioioi.it.mltraining.service.indicator;

/**
 * Record holding all calculated statistical indicators for a single candle.
 * All values are normalized/scaled appropriately for LightGBM model training.
 * Statistical indicators measure price distribution, percentiles, and moments.
 *
 * @param closePercentile100 close percentile over 100-period lookback (0-100)
 * @param closePercentile500 close percentile over 500-period lookback (0-100)
 * @param closeZscore20 close z-score over 20-period (standard deviations from mean)
 * @param rsiPercentile RSI percentile over lookback period (0-100)
 * @param volumePercentileStat volume percentile for statistical analysis (0-100)
 * @param atrPercentileStat ATR percentile for statistical analysis (0-100)
 * @param skewnessReturns skewness of returns distribution (measure of asymmetry)
 * @param kurtosisReturns kurtosis of returns distribution (measure of tail weight)
 * @param drawdownPct maximum drawdown percentage from peak
 * @param drawupPct maximum drawup percentage from trough
 */
public record StatisticalIndicators(
        Double closePercentile100,
        Double closePercentile500,
        Double closeZscore20,
        Double rsiPercentile,
        Double volumePercentileStat,
        Double atrPercentileStat,
        Double skewnessReturns,
        Double kurtosisReturns,
        Double drawdownPct,
        Double drawupPct
) {
}
