package ioioi.it.mltraining.service.indicator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;

/**
 * Service for calculating Volume Profile indicators.
 * All indicators are normalized/scaled for LightGBM model training.
 *
 * <p>Volume Profile divides the candle's price range (high to low) into 21 equal levels
 * and distributes volume across these levels. This reveals price levels with highest
 * trading activity (POC) and the value area where most trading occurred.</p>
 *
 * <p>Since we only have OHLCV data (not tick data), we approximate volume distribution
 * using a weighted approach based on OHLC prices.</p>
 */
@Service
@Slf4j
public class VolumeProfileCalculator {

    private static final int PRICE_LEVELS = 21; // Number of price levels to divide candle into
    private static final double VALUE_AREA_VOLUME_PCT = 0.70; // 70% of volume defines value area

    /**
     * Calculates all volume profile indicators for a specific bar index in the series.
     *
     * @param series the bar series with OHLCV data
     * @param index the index of the bar to calculate indicators for
     * @return VolumeProfileIndicators record with all calculated values
     */
    public VolumeProfileIndicators calculate(BarSeries series, int index) {
        if (series == null || index < 0 || index >= series.getBarCount()) {
            log.warn("Invalid series or index: index={}, barCount={}", index,
                    series != null ? series.getBarCount() : 0);
            return createEmptyIndicators();
        }

        Bar bar = series.getBar(index);
        double open = bar.getOpenPrice().doubleValue();
        double close = bar.getClosePrice().doubleValue();
        double high = bar.getHighPrice().doubleValue();
        double low = bar.getLowPrice().doubleValue();
        double volume = bar.getVolume().doubleValue();

        if (volume <= 0 || high <= low) {
            return createEmptyIndicators();
        }

        // Calculate volume distribution across 21 price levels
        double[] volumeProfile = calculateVolumeDistribution(open, high, low, close, volume);

        // Find POC (Point of Control) - price level with maximum volume
        int pocIndex = findPOCIndex(volumeProfile);
        double pocPrice = calculatePriceAtLevel(low, high, pocIndex);
        double pocVolumePct = volumeProfile[pocIndex] / volume * 100.0;
        double pocPositionInRange = (double) pocIndex / (PRICE_LEVELS - 1);

        // Calculate Value Area (70% of volume)
        ValueArea valueArea = calculateValueArea(volumeProfile, volume, low, high, pocIndex);

        // Calculate volume distribution around POC
        double volumeAbovePoc = 0.0;
        double volumeBelowPoc = 0.0;
        for (int i = 0; i < PRICE_LEVELS; i++) {
            if (i > pocIndex) {
                volumeAbovePoc += volumeProfile[i];
            } else if (i < pocIndex) {
                volumeBelowPoc += volumeProfile[i];
            }
        }

        double volumeAbovePocPct = volume > 0 ? (volumeAbovePoc / volume) * 100.0 : null;
        double volumeBelowPocPct = volume > 0 ? (volumeBelowPoc / volume) * 100.0 : null;
        double volumeImbalance = volume > 0 ? (volumeAbovePoc - volumeBelowPoc) / volume : null;

        // Count high and low volume nodes
        double avgVolume = volume / PRICE_LEVELS;
        int highVolumeNodes = 0;
        int lowVolumeNodes = 0;
        for (double v : volumeProfile) {
            if (v > avgVolume) highVolumeNodes++;
            if (v < avgVolume * 0.5) lowVolumeNodes++;
        }

        // Calculate volume concentration (how concentrated is the volume)
        double volumeConcentration = calculateVolumeConcentration(volumeProfile, volume);

        return new VolumeProfileIndicators(
                pocPrice,
                pocIndex,
                pocVolumePct,
                pocPositionInRange,
                valueArea.vahPrice,
                valueArea.valPrice,
                valueArea.valueAreaPct,
                valueArea.valueAreaVolumePct,
                volumeAbovePocPct,
                volumeBelowPocPct,
                volumeImbalance,
                highVolumeNodes,
                lowVolumeNodes,
                volumeConcentration
        );
    }

    /**
     * Distributes volume across 21 price levels using OHLC-weighted approximation.
     * Since we don't have tick data, we estimate volume distribution based on OHLC prices.
     *
     * Approach:
     * - Close gets highest weight (40%) - most recent price action
     * - Open gets medium weight (25%) - starting point
     * - High and Low get lower weights (17.5% each) - extremes
     * - Volume is distributed using Gaussian around each OHLC point
     */
    private double[] calculateVolumeDistribution(double open, double high, double low, double close, double volume) {
        double[] profile = new double[PRICE_LEVELS];
        double range = high - low;

        if (range == 0) {
            // All volume at single price
            int midLevel = PRICE_LEVELS / 2;
            profile[midLevel] = volume;
            return profile;
        }

        // Weights for OHLC points
        double closeWeight = 0.40;
        double openWeight = 0.25;
        double highWeight = 0.175;
        double lowWeight = 0.175;

        // Distribute volume around each OHLC point using Gaussian
        distributeVolumeGaussian(profile, close, low, high, volume * closeWeight, range);
        distributeVolumeGaussian(profile, open, low, high, volume * openWeight, range);
        distributeVolumeGaussian(profile, high, low, high, volume * highWeight, range);
        distributeVolumeGaussian(profile, low, low, high, volume * lowWeight, range);

        return profile;
    }

    /**
     * Distributes volume around a price point using Gaussian distribution.
     */
    private void distributeVolumeGaussian(double[] profile, double price, double low, double high,
                                          double volumeToDistribute, double range) {
        double sigma = range / (PRICE_LEVELS / 3.0); // Standard deviation
        double[] weights = new double[PRICE_LEVELS];
        double totalWeight = 0.0;

        // Calculate Gaussian weights for each level
        for (int i = 0; i < PRICE_LEVELS; i++) {
            double levelPrice = calculatePriceAtLevel(low, high, i);
            double distance = Math.abs(levelPrice - price);
            double weight = Math.exp(-0.5 * Math.pow(distance / sigma, 2));
            weights[i] = weight;
            totalWeight += weight;
        }

        // Normalize and distribute volume
        for (int i = 0; i < PRICE_LEVELS; i++) {
            if (totalWeight > 0) {
                profile[i] += volumeToDistribute * (weights[i] / totalWeight);
            }
        }
    }

    /**
     * Calculates price at a specific level index.
     */
    private double calculatePriceAtLevel(double low, double high, int levelIndex) {
        double range = high - low;
        return low + (range * levelIndex / (PRICE_LEVELS - 1));
    }

    /**
     * Finds the index of POC (level with maximum volume).
     */
    private int findPOCIndex(double[] volumeProfile) {
        int pocIndex = 0;
        double maxVolume = volumeProfile[0];
        for (int i = 1; i < PRICE_LEVELS; i++) {
            if (volumeProfile[i] > maxVolume) {
                maxVolume = volumeProfile[i];
                pocIndex = i;
            }
        }
        return pocIndex;
    }

    /**
     * Calculates Value Area - the price range that contains 70% of the volume.
     * Starts from POC and expands up/down to include levels until reaching 70% of total volume.
     */
    private ValueArea calculateValueArea(double[] volumeProfile, double totalVolume,
                                         double low, double high, int pocIndex) {
        double targetVolume = totalVolume * VALUE_AREA_VOLUME_PCT;
        double accumulatedVolume = volumeProfile[pocIndex];

        int vaHighIndex = pocIndex;
        int vaLowIndex = pocIndex;

        // Expand value area from POC until we capture 70% of volume
        while (accumulatedVolume < targetVolume && (vaHighIndex < PRICE_LEVELS - 1 || vaLowIndex > 0)) {
            double volumeAbove = (vaHighIndex < PRICE_LEVELS - 1) ? volumeProfile[vaHighIndex + 1] : 0;
            double volumeBelow = (vaLowIndex > 0) ? volumeProfile[vaLowIndex - 1] : 0;

            if (volumeAbove >= volumeBelow && vaHighIndex < PRICE_LEVELS - 1) {
                vaHighIndex++;
                accumulatedVolume += volumeAbove;
            } else if (vaLowIndex > 0) {
                vaLowIndex--;
                accumulatedVolume += volumeBelow;
            } else {
                break;
            }
        }

        double vahPrice = calculatePriceAtLevel(low, high, vaHighIndex);
        double valPrice = calculatePriceAtLevel(low, high, vaLowIndex);
        double valueAreaPct = ((double) (vaHighIndex - vaLowIndex) / (PRICE_LEVELS - 1)) * 100.0;
        double valueAreaVolumePct = (accumulatedVolume / totalVolume) * 100.0;

        return new ValueArea(vahPrice, valPrice, valueAreaPct, valueAreaVolumePct);
    }

    /**
     * Calculates how concentrated the volume is (Herfindahl index).
     * Higher value = more concentrated (volume on fewer levels)
     * Lower value = more distributed (volume spread across many levels)
     */
    private Double calculateVolumeConcentration(double[] volumeProfile, double totalVolume) {
        if (totalVolume <= 0) return null;

        double sumOfSquares = 0.0;
        for (double vol : volumeProfile) {
            double proportion = vol / totalVolume;
            sumOfSquares += proportion * proportion;
        }

        // Normalize to 0-1 range
        // Max concentration = 1.0 (all volume on one level)
        // Min concentration = 1/PRICE_LEVELS (evenly distributed)
        double minConcentration = 1.0 / PRICE_LEVELS;
        return (sumOfSquares - minConcentration) / (1.0 - minConcentration);
    }

    private VolumeProfileIndicators createEmptyIndicators() {
        return new VolumeProfileIndicators(
                null, null, null, null, null, null, null, null,
                null, null, null, null, null, null
        );
    }

    /**
     * Helper record for Value Area calculation.
     */
    private record ValueArea(
            Double vahPrice,
            Double valPrice,
            Double valueAreaPct,
            Double valueAreaVolumePct
    ) {}
}
