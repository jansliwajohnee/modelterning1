package ioioi.it.mltraining.service.indicator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.averages.SMAIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.indicators.volume.ChaikinMoneyFlowIndicator;
import org.ta4j.core.indicators.volume.OnBalanceVolumeIndicator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service for calculating volume technical indicators using Ta4j library.
 * All indicators are normalized/scaled for LightGBM model training.
 *
 * <p>Volume indicators measure trading activity and strength of price movements.
 * All calculations use ratios, percentages or standardized scores for normalization.</p>
 */
@Service
@Slf4j
public class VolumeIndicatorCalculator {

    // Volume SMA periods
    private static final int VOLUME_SMA_20_PERIOD = 20;
    private static final int VOLUME_SMA_50_PERIOD = 50;

    // CMF period
    private static final int CMF_PERIOD = 20;

    // Percentile lookback
    private static final int PERCENTILE_LOOKBACK = 100;

    // Z-score period
    private static final int Z_SCORE_PERIOD = 20;

    /**
     * Calculates all volume indicators for a specific bar index in the series.
     *
     * @param series the bar series with OHLCV data
     * @param index the index of the bar to calculate indicators for
     * @return VolumeIndicators record with all calculated values (normalized for LightGBM)
     */
    public VolumeIndicators calculate(BarSeries series, int index) {
        if (series == null || index < 0 || index >= series.getBarCount()) {
            log.warn("Invalid series or index: index={}, barCount={}", index,
                    series != null ? series.getBarCount() : 0);
            return createEmptyIndicators();
        }

        double currentVolume = series.getBar(index).getVolume().doubleValue();

        // Relative volume ratios
        Double rvolSma20 = calculateRelativeVolume(series, index, currentVolume, VOLUME_SMA_20_PERIOD);
        Double rvolSma50 = calculateRelativeVolume(series, index, currentVolume, VOLUME_SMA_50_PERIOD);

        // Volume percentile
        Double volumePercentile = calculateVolumePercentile(series, index);

        // Volume z-score normalization
        Double volumeNormalized = calculateVolumeZScore(series, index, currentVolume);

        // OBV change percentage
        Double obvChangePct = calculateOBVChangePct(series, index);

        // Chaikin Money Flow
        Double cmf = calculateCMF(series, index);

        // Volume Rate of Change
        Double volumeRocPct = calculateVolumeROC(series, index, currentVolume);

        // Up volume ratio
        Double upVolumeRatio = calculateUpVolumeRatio(series, index);

        // Volume pressure
        Double volumePressure = calculateVolumePressure(series, index, currentVolume);

        return new VolumeIndicators(
                rvolSma20, rvolSma50,
                volumePercentile, volumeNormalized,
                obvChangePct, cmf,
                volumeRocPct,
                upVolumeRatio, volumePressure
        );
    }

    /**
     * Calculates relative volume as ratio to moving average.
     * RVol = Current Volume / SMA(Volume)
     * Ratio > 1: Above average volume
     * Ratio < 1: Below average volume
     */
    private Double calculateRelativeVolume(BarSeries series, int index, double currentVolume, int period) {
        if (!hasEnoughBars(series, index, period)) {
            return null;
        }
        try {
            VolumeIndicator volume = new VolumeIndicator(series);
            SMAIndicator volumeSma = new SMAIndicator(volume, period);

            double smaValue = volumeSma.getValue(index).doubleValue();
            return smaValue > 0.0 ? currentVolume / smaValue : null;
        } catch (Exception e) {
            log.debug("Failed to calculate relative volume at index {}: {}", index, e.getMessage());
            return null;
        }
    }

    /**
     * Calculates volume percentile over lookback period.
     * Range: 0-100
     * High percentile = High current volume relative to history
     */
    private Double calculateVolumePercentile(BarSeries series, int index) {
        int lookback = Math.min(PERCENTILE_LOOKBACK, index + 1);
        if (lookback < 20) {
            return null;
        }
        try {
            VolumeIndicator volume = new VolumeIndicator(series);
            double currentVolume = volume.getValue(index).doubleValue();

            List<Double> volumeValues = new ArrayList<>();
            for (int i = index - lookback + 1; i <= index; i++) {
                volumeValues.add(volume.getValue(i).doubleValue());
            }

            if (volumeValues.isEmpty()) return null;

            Collections.sort(volumeValues);
            int position = 0;
            for (int i = 0; i < volumeValues.size(); i++) {
                if (volumeValues.get(i) <= currentVolume) {
                    position = i;
                }
            }

            return (position / (double) volumeValues.size()) * 100.0;
        } catch (Exception e) {
            log.debug("Failed to calculate volume percentile at index {}: {}", index, e.getMessage());
            return null;
        }
    }

    /**
     * Calculates volume z-score normalization.
     * Z-Score = (Current Volume - Mean Volume) / StdDev Volume
     * Measures how many standard deviations current volume is from average
     */
    private Double calculateVolumeZScore(BarSeries series, int index, double currentVolume) {
        if (!hasEnoughBars(series, index, Z_SCORE_PERIOD)) {
            return null;
        }
        try {
            VolumeIndicator volume = new VolumeIndicator(series);
            SMAIndicator volumeMean = new SMAIndicator(volume, Z_SCORE_PERIOD);
            StandardDeviationIndicator volumeStdDev = new StandardDeviationIndicator(volume, Z_SCORE_PERIOD);

            double mean = volumeMean.getValue(index).doubleValue();
            double stdDev = volumeStdDev.getValue(index).doubleValue();

            return stdDev > 0.0 ? (currentVolume - mean) / stdDev : 0.0;
        } catch (Exception e) {
            log.debug("Failed to calculate volume z-score at index {}: {}", index, e.getMessage());
            return null;
        }
    }

    /**
     * Calculates OBV (On Balance Volume) change percentage.
     * OBV Change % = (OBV(current) - OBV(previous)) / abs(OBV(previous)) * 100
     */
    private Double calculateOBVChangePct(BarSeries series, int index) {
        if (index < 1) {
            return null;
        }
        try {
            OnBalanceVolumeIndicator obv = new OnBalanceVolumeIndicator(series);

            double currentOBV = obv.getValue(index).doubleValue();
            double previousOBV = obv.getValue(index - 1).doubleValue();

            double absOBV = Math.abs(previousOBV);
            return absOBV > 0.0 ? ((currentOBV - previousOBV) / absOBV) * 100.0 : 0.0;
        } catch (Exception e) {
            log.debug("Failed to calculate OBV change at index {}: {}", index, e.getMessage());
            return null;
        }
    }

    /**
     * Calculates Chaikin Money Flow.
     * CMF measures buying/selling pressure over period.
     * Range: -1 to 1
     * Positive = Buying pressure, Negative = Selling pressure
     */
    private Double calculateCMF(BarSeries series, int index) {
        if (!hasEnoughBars(series, index, CMF_PERIOD)) {
            return null;
        }
        try {
            ChaikinMoneyFlowIndicator cmf = new ChaikinMoneyFlowIndicator(series, CMF_PERIOD);
            return cmf.getValue(index).doubleValue();
        } catch (Exception e) {
            log.debug("Failed to calculate CMF at index {}: {}", index, e.getMessage());
            return null;
        }
    }

    /**
     * Calculates Volume Rate of Change percentage.
     * Volume ROC % = ((Current Volume - Previous Volume) / Previous Volume) * 100
     */
    private Double calculateVolumeROC(BarSeries series, int index, double currentVolume) {
        if (index < 1) {
            return null;
        }
        try {
            double previousVolume = series.getBar(index - 1).getVolume().doubleValue();
            return previousVolume > 0.0 ? ((currentVolume - previousVolume) / previousVolume) * 100.0 : null;
        } catch (Exception e) {
            log.debug("Failed to calculate volume ROC at index {}: {}", index, e.getMessage());
            return null;
        }
    }

    /**
     * Calculates up volume ratio over lookback period.
     * Up Volume Ratio = Sum(Volume on up bars) / Sum(Total Volume)
     * Range: 0-1
     * Ratio > 0.5: More volume on up moves (bullish)
     * Ratio < 0.5: More volume on down moves (bearish)
     */
    private Double calculateUpVolumeRatio(BarSeries series, int index) {
        int lookback = Math.min(VOLUME_SMA_20_PERIOD, index + 1);
        if (lookback < 2) {
            return null;
        }
        try {
            double upVolume = 0.0;
            double totalVolume = 0.0;

            for (int i = index - lookback + 1; i <= index; i++) {
                double close = series.getBar(i).getClosePrice().doubleValue();
                double open = series.getBar(i).getOpenPrice().doubleValue();
                double volume = series.getBar(i).getVolume().doubleValue();

                totalVolume += volume;
                if (close >= open) {
                    upVolume += volume;
                }
            }

            return totalVolume > 0.0 ? upVolume / totalVolume : null;
        } catch (Exception e) {
            log.debug("Failed to calculate up volume ratio at index {}: {}", index, e.getMessage());
            return null;
        }
    }

    /**
     * Calculates volume pressure indicator.
     * Volume Pressure = (Close - Open) / (High - Low) * Relative Volume
     * Measures the strength of price movement relative to volume
     * Positive = Buying pressure, Negative = Selling pressure
     */
    private Double calculateVolumePressure(BarSeries series, int index, double currentVolume) {
        if (!hasEnoughBars(series, index, VOLUME_SMA_20_PERIOD)) {
            return null;
        }
        try {
            double close = series.getBar(index).getClosePrice().doubleValue();
            double open = series.getBar(index).getOpenPrice().doubleValue();
            double high = series.getBar(index).getHighPrice().doubleValue();
            double low = series.getBar(index).getLowPrice().doubleValue();
            double range = high - low;

            if (range == 0.0) return 0.0;

            // Calculate relative volume
            Double rvol = calculateRelativeVolume(series, index, currentVolume, VOLUME_SMA_20_PERIOD);
            if (rvol == null) return null;

            // Price direction normalized by range, multiplied by relative volume
            double priceDirection = (close - open) / range;
            return priceDirection * rvol;
        } catch (Exception e) {
            log.debug("Failed to calculate volume pressure at index {}: {}", index, e.getMessage());
            return null;
        }
    }

    private boolean hasEnoughBars(BarSeries series, int index, int requiredPeriod) {
        return index >= requiredPeriod - 1;
    }

    private VolumeIndicators createEmptyIndicators() {
        return new VolumeIndicators(
                null, null, null, null, null,
                null, null, null, null
        );
    }
}
