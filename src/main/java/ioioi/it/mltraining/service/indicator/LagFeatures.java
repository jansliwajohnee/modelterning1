package ioioi.it.mltraining.service.indicator;

/**
 * Record holding all calculated lag features for a single candle.
 * All values are normalized/scaled appropriately for LightGBM model training.
 * Lag features capture historical values of key indicators for temporal patterns.
 *
 * @param returnLag1 return percentage from 1 bar ago
 * @param returnLag2 return percentage from 2 bars ago
 * @param returnLag3 return percentage from 3 bars ago
 * @param returnLag5 return percentage from 5 bars ago
 * @param returnLag10 return percentage from 10 bars ago
 * @param rsiLag1 RSI value from 1 bar ago (0-100)
 * @param rsiLag2 RSI value from 2 bars ago (0-100)
 * @param rsiLag3 RSI value from 3 bars ago (0-100)
 * @param rsiChange change in RSI from previous bar
 * @param atrPctLag1 ATR percentage from 1 bar ago
 * @param atrPctLag2 ATR percentage from 2 bars ago
 * @param rvolLag1 relative volume from 1 bar ago
 * @param rvolLag2 relative volume from 2 bars ago
 * @param bbPercentBLag1 Bollinger %B from 1 bar ago
 * @param bbPercentBLag2 Bollinger %B from 2 bars ago
 * @param adxLag1 ADX from 1 bar ago (0-100)
 * @param adxLag2 ADX from 2 bars ago (0-100)
 * @param bodyPctLag1 body percentage from 1 bar ago
 * @param bodyPctLag2 body percentage from 2 bars ago
 */
public record LagFeatures(
        Double returnLag1,
        Double returnLag2,
        Double returnLag3,
        Double returnLag5,
        Double returnLag10,
        Double rsiLag1,
        Double rsiLag2,
        Double rsiLag3,
        Double rsiChange,
        Double atrPctLag1,
        Double atrPctLag2,
        Double rvolLag1,
        Double rvolLag2,
        Double bbPercentBLag1,
        Double bbPercentBLag2,
        Double adxLag1,
        Double adxLag2,
        Double bodyPctLag1,
        Double bodyPctLag2
) {
}
