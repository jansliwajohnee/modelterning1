package ioioi.it.mltraining.service.indicator;

/**
 * Record holding all calculated composite score indicators for a single candle.
 * All values are normalized/scaled appropriately for LightGBM model training.
 * Composite scores combine multiple indicators to measure overall market conditions.
 *
 * @param trendScore composite score measuring trend strength and direction (-1 to 1)
 * @param bullishPatternsRatio ratio of bullish vs bearish patterns (0-1)
 * @param momentumAgreement agreement between multiple momentum indicators (0-1)
 * @param volatilityVsTrend ratio of volatility to trend strength
 * @param volumeConfirmation true if volume confirms the price movement
 */
public record CompositeScores(
        Double trendScore,
        Double bullishPatternsRatio,
        Double momentumAgreement,
        Double volatilityVsTrend,
        Boolean volumeConfirmation
) {
}
