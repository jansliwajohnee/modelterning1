package ioioi.it.mltraining.service.indicator;

/**
 * Volume Profile indicators calculated from price-volume distribution.
 *
 * <p>Volume Profile analyzes the distribution of volume across different price levels
 * within the candle range (high to low). The candle is divided into 21 price levels
 * and volume is distributed across these levels.</p>
 *
 * @param pocPrice Point of Control - price level with highest volume
 * @param pocIndex Index of POC level (0-20, where 0=lowest level, 20=highest level)
 * @param pocVolumePct Percentage of total volume at POC level
 * @param pocPositionInRange Position of POC in candle range (0-1, where 0=low, 1=high)
 * @param vahPrice Value Area High - upper boundary of value area (70% volume)
 * @param valPrice Value Area Low - lower boundary of value area (70% volume)
 * @param valueAreaPct Percentage of price range covered by value area
 * @param valueAreaVolumePct Actual percentage of volume within value area (should be ~70%)
 * @param volumeAbovePocPct Percentage of volume above POC
 * @param volumeBelowPocPct Percentage of volume below POC
 * @param volumeImbalance Imbalance between volume above and below POC (-1 to 1)
 * @param highVolumeNodesCount Number of price levels with volume above average
 * @param lowVolumeNodesCount Number of price levels with volume below average
 * @param volumeConcentration Measure of how concentrated volume is (0-1, higher = more concentrated)
 */
public record VolumeProfileIndicators(
        Double pocPrice,
        Integer pocIndex,
        Double pocVolumePct,
        Double pocPositionInRange,
        Double vahPrice,
        Double valPrice,
        Double valueAreaPct,
        Double valueAreaVolumePct,
        Double volumeAbovePocPct,
        Double volumeBelowPocPct,
        Double volumeImbalance,
        Integer highVolumeNodesCount,
        Integer lowVolumeNodesCount,
        Double volumeConcentration
) {}
