package ioioi.it.mltraining.service.indicator;

/**
 * Record holding all calculated momentum indicators for a single candle.
 * All values are normalized/scaled appropriately for LightGBM model training.
 *
 * @param rsi7 RSI with 7-period lookback (0-100)
 * @param rsi14 RSI with 14-period lookback (0-100)
 * @param rsi21 RSI with 21-period lookback (0-100)
 * @param stochasticK Stochastic %K (0-100)
 * @param stochasticD Stochastic %D (0-100)
 * @param stochasticRsi Stochastic RSI (0-100)
 * @param williamsR Williams %R (-100 to 0)
 * @param cciNormalized Commodity Channel Index normalized
 * @param mfi Money Flow Index (0-100)
 * @param cmo Chande Momentum Oscillator (-100 to 100)
 * @param ultimateOscillator Ultimate Oscillator (0-100)
 * @param rocPercent Rate of Change in percent
 * @param rsiDistanceFrom50 RSI distance from neutral level 50
 * @param rsiSlope RSI slope (rate of change of RSI)
 */
public record MomentumIndicators(
        Double rsi7,
        Double rsi14,
        Double rsi21,
        Double stochasticK,
        Double stochasticD,
        Double stochasticRsi,
        Double williamsR,
        Double cciNormalized,
        Double mfi,
        Double cmo,
        Double ultimateOscillator,
        Double rocPercent,
        Double rsiDistanceFrom50,
        Double rsiSlope
) {
}
