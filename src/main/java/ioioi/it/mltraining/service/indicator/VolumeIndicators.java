package ioioi.it.mltraining.service.indicator;

/**
 * Record holding all calculated volume indicators for a single candle.
 * All values are normalized/scaled appropriately for LightGBM model training.
 * Volume indicators measure trading activity and strength of price movements.
 *
 * @param rvolSma20 relative volume to SMA(20) - ratio
 * @param rvolSma50 relative volume to SMA(50) - ratio
 * @param volumePercentile volume percentile over lookback period (0-100)
 * @param volumeNormalized normalized volume using z-score
 * @param obvChangePct OBV (On Balance Volume) change percentage
 * @param cmf Chaikin Money Flow (-1 to 1)
 * @param volumeRocPct Volume Rate of Change percentage
 * @param upVolumeRatio ratio of up volume to total volume (0-1)
 * @param volumePressure volume pressure indicator (normalized)
 */
public record VolumeIndicators(
        Double rvolSma20,
        Double rvolSma50,
        Double volumePercentile,
        Double volumeNormalized,
        Double obvChangePct,
        Double cmf,
        Double volumeRocPct,
        Double upVolumeRatio,
        Double volumePressure
) {
}
