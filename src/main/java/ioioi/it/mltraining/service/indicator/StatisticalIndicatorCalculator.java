package ioioi.it.mltraining.service.indicator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.averages.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service for calculating statistical technical indicators.
 * All indicators are normalized/scaled for LightGBM model training.
 *
 * <p>Statistical indicators measure price distribution, percentiles, and statistical moments
 * like skewness and kurtosis. All calculations provide normalized measures suitable for ML.</p>
 */
@Service
@Slf4j
public class StatisticalIndicatorCalculator {

    // Percentile lookback periods
    private static final int PERCENTILE_100_PERIOD = 100;
    private static final int PERCENTILE_500_PERIOD = 500;
    private static final int PERCENTILE_DEFAULT = 100;

    // Z-score period
    private static final int ZSCORE_PERIOD = 20;

    // RSI period
    private static final int RSI_PERIOD = 14;

    // ATR period
    private static final int ATR_PERIOD = 14;

    // Distribution moments period
    private static final int RETURNS_PERIOD = 20;

    // Drawdown lookback
    private static final int DRAWDOWN_PERIOD = 50;

    /**
     * Calculates all statistical indicators for a specific bar index in the series.
     *
     * @param series the bar series with OHLCV data
     * @param index the index of the bar to calculate indicators for
     * @return StatisticalIndicators record with all calculated values (normalized for LightGBM)
     */
    public StatisticalIndicators calculate(BarSeries series, int index) {
        if (series == null || index < 0 || index >= series.getBarCount()) {
            log.warn("Invalid series or index: index={}, barCount={}", index,
                    series != null ? series.getBarCount() : 0);
            return createEmptyIndicators();
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        double currentClose = closePrice.getValue(index).doubleValue();

        // Close percentiles
        Double closePercentile100 = calculatePercentile(closePrice, index, PERCENTILE_100_PERIOD, currentClose);
        Double closePercentile500 = calculatePercentile(closePrice, index, PERCENTILE_500_PERIOD, currentClose);

        // Close z-score
        Double closeZscore20 = calculateZScore(closePrice, index, currentClose);

        // RSI percentile
        Double rsiPercentile = calculateRSIPercentile(series, index);

        // Volume percentile
        Double volumePercentileStat = calculateVolumePercentile(series, index);

        // ATR percentile
        Double atrPercentileStat = calculateATRPercentile(series, index);

        // Distribution moments
        Double skewnessReturns = calculateSkewness(series, index);
        Double kurtosisReturns = calculateKurtosis(series, index);

        // Drawdown and drawup
        Double drawdownPct = calculateDrawdown(series, index);
        Double drawupPct = calculateDrawup(series, index);

        return new StatisticalIndicators(
                closePercentile100, closePercentile500,
                closeZscore20,
                rsiPercentile,
                volumePercentileStat, atrPercentileStat,
                skewnessReturns, kurtosisReturns,
                drawdownPct, drawupPct
        );
    }

    /**
     * Calculates percentile rank for a given value over lookback period.
     * Range: 0-100
     */
    private Double calculatePercentile(ClosePriceIndicator indicator, int index, int period, double currentValue) {
        int lookback = Math.min(period, index + 1);
        if (lookback < 20) {
            return null;
        }
        try {
            List<Double> values = new ArrayList<>();
            for (int i = index - lookback + 1; i <= index; i++) {
                values.add(indicator.getValue(i).doubleValue());
            }

            if (values.isEmpty()) return null;

            Collections.sort(values);
            int position = 0;
            for (int i = 0; i < values.size(); i++) {
                if (values.get(i) <= currentValue) {
                    position = i;
                }
            }

            return (position / (double) values.size()) * 100.0;
        } catch (Exception e) {
            log.debug("Failed to calculate percentile at index {}: {}", index, e.getMessage());
            return null;
        }
    }

    /**
     * Calculates z-score for close price.
     * Z-Score = (Current - Mean) / StdDev
     * Measures how many standard deviations from mean
     */
    private Double calculateZScore(ClosePriceIndicator closePrice, int index, double currentClose) {
        if (index < ZSCORE_PERIOD - 1) {
            return null;
        }
        try {
            SMAIndicator mean = new SMAIndicator(closePrice, ZSCORE_PERIOD);
            StandardDeviationIndicator stdDev = new StandardDeviationIndicator(closePrice, ZSCORE_PERIOD);

            double meanValue = mean.getValue(index).doubleValue();
            double stdDevValue = stdDev.getValue(index).doubleValue();

            return stdDevValue > 0.0 ? (currentClose - meanValue) / stdDevValue : 0.0;
        } catch (Exception e) {
            log.debug("Failed to calculate z-score at index {}: {}", index, e.getMessage());
            return null;
        }
    }

    /**
     * Calculates RSI percentile over lookback period.
     */
    private Double calculateRSIPercentile(BarSeries series, int index) {
        int lookback = Math.min(PERCENTILE_DEFAULT, index + 1);
        if (lookback < RSI_PERIOD + 20) {
            return null;
        }
        try {
            ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
            RSIIndicator rsi = new RSIIndicator(closePrice, RSI_PERIOD);
            double currentRSI = rsi.getValue(index).doubleValue();

            List<Double> rsiValues = new ArrayList<>();
            for (int i = index - lookback + 1; i <= index; i++) {
                if (i >= RSI_PERIOD - 1) {
                    rsiValues.add(rsi.getValue(i).doubleValue());
                }
            }

            if (rsiValues.isEmpty()) return null;

            Collections.sort(rsiValues);
            int position = 0;
            for (int i = 0; i < rsiValues.size(); i++) {
                if (rsiValues.get(i) <= currentRSI) {
                    position = i;
                }
            }

            return (position / (double) rsiValues.size()) * 100.0;
        } catch (Exception e) {
            log.debug("Failed to calculate RSI percentile at index {}: {}", index, e.getMessage());
            return null;
        }
    }

    /**
     * Calculates volume percentile over lookback period.
     */
    private Double calculateVolumePercentile(BarSeries series, int index) {
        int lookback = Math.min(PERCENTILE_DEFAULT, index + 1);
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
     * Calculates ATR percentile over lookback period.
     */
    private Double calculateATRPercentile(BarSeries series, int index) {
        int lookback = Math.min(PERCENTILE_DEFAULT, index + 1);
        if (lookback < ATR_PERIOD + 20) {
            return null;
        }
        try {
            ATRIndicator atr = new ATRIndicator(series, ATR_PERIOD);
            double currentATR = atr.getValue(index).doubleValue();

            List<Double> atrValues = new ArrayList<>();
            for (int i = index - lookback + 1; i <= index; i++) {
                if (i >= ATR_PERIOD - 1) {
                    atrValues.add(atr.getValue(i).doubleValue());
                }
            }

            if (atrValues.isEmpty()) return null;

            Collections.sort(atrValues);
            int position = 0;
            for (int i = 0; i < atrValues.size(); i++) {
                if (atrValues.get(i) <= currentATR) {
                    position = i;
                }
            }

            return (position / (double) atrValues.size()) * 100.0;
        } catch (Exception e) {
            log.debug("Failed to calculate ATR percentile at index {}: {}", index, e.getMessage());
            return null;
        }
    }

    /**
     * Calculates skewness of returns distribution.
     * Skewness measures asymmetry of distribution.
     * Positive: Right-skewed (more positive outliers)
     * Negative: Left-skewed (more negative outliers)
     */
    private Double calculateSkewness(BarSeries series, int index) {
        if (index < RETURNS_PERIOD) {
            return null;
        }
        try {
            List<Double> returns = calculateReturns(series, index, RETURNS_PERIOD);
            if (returns.isEmpty()) return null;

            double mean = returns.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            double n = returns.size();

            double sumCubedDeviations = returns.stream()
                    .mapToDouble(r -> Math.pow(r - mean, 3))
                    .sum();

            double sumSquaredDeviations = returns.stream()
                    .mapToDouble(r -> Math.pow(r - mean, 2))
                    .sum();

            double stdDev = Math.sqrt(sumSquaredDeviations / n);
            if (stdDev == 0.0) return 0.0;

            return (sumCubedDeviations / n) / Math.pow(stdDev, 3);
        } catch (Exception e) {
            log.debug("Failed to calculate skewness at index {}: {}", index, e.getMessage());
            return null;
        }
    }

    /**
     * Calculates kurtosis of returns distribution.
     * Kurtosis measures "tailedness" of distribution.
     * Excess kurtosis = Kurtosis - 3
     * Positive: Heavy tails (more extreme values)
     * Negative: Light tails (fewer extreme values)
     */
    private Double calculateKurtosis(BarSeries series, int index) {
        if (index < RETURNS_PERIOD) {
            return null;
        }
        try {
            List<Double> returns = calculateReturns(series, index, RETURNS_PERIOD);
            if (returns.isEmpty()) return null;

            double mean = returns.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            double n = returns.size();

            double sumQuarticDeviations = returns.stream()
                    .mapToDouble(r -> Math.pow(r - mean, 4))
                    .sum();

            double sumSquaredDeviations = returns.stream()
                    .mapToDouble(r -> Math.pow(r - mean, 2))
                    .sum();

            double variance = sumSquaredDeviations / n;
            if (variance == 0.0) return 0.0;

            double kurtosis = (sumQuarticDeviations / n) / Math.pow(variance, 2);
            return kurtosis - 3.0; // Excess kurtosis
        } catch (Exception e) {
            log.debug("Failed to calculate kurtosis at index {}: {}", index, e.getMessage());
            return null;
        }
    }

    /**
     * Calculates returns over period.
     */
    private List<Double> calculateReturns(BarSeries series, int index, int period) {
        List<Double> returns = new ArrayList<>();
        for (int i = index - period + 1; i <= index; i++) {
            if (i > 0) {
                double currentClose = series.getBar(i).getClosePrice().doubleValue();
                double previousClose = series.getBar(i - 1).getClosePrice().doubleValue();
                if (previousClose > 0.0) {
                    double ret = ((currentClose - previousClose) / previousClose) * 100.0;
                    returns.add(ret);
                }
            }
        }
        return returns;
    }

    /**
     * Calculates maximum drawdown percentage from peak over lookback period.
     * Drawdown = ((Trough - Peak) / Peak) * 100
     * Always negative or zero
     */
    private Double calculateDrawdown(BarSeries series, int index) {
        int lookback = Math.min(DRAWDOWN_PERIOD, index + 1);
        if (lookback < 2) {
            return null;
        }
        try {
            double maxDrawdown = 0.0;
            double peak = Double.MIN_VALUE;

            for (int i = index - lookback + 1; i <= index; i++) {
                double close = series.getBar(i).getClosePrice().doubleValue();

                if (close > peak) {
                    peak = close;
                }

                if (peak > 0.0) {
                    double drawdown = ((close - peak) / peak) * 100.0;
                    if (drawdown < maxDrawdown) {
                        maxDrawdown = drawdown;
                    }
                }
            }

            return maxDrawdown;
        } catch (Exception e) {
            log.debug("Failed to calculate drawdown at index {}: {}", index, e.getMessage());
            return null;
        }
    }

    /**
     * Calculates maximum drawup percentage from trough over lookback period.
     * Drawup = ((Peak - Trough) / Trough) * 100
     * Always positive or zero
     */
    private Double calculateDrawup(BarSeries series, int index) {
        int lookback = Math.min(DRAWDOWN_PERIOD, index + 1);
        if (lookback < 2) {
            return null;
        }
        try {
            double maxDrawup = 0.0;
            double trough = Double.MAX_VALUE;

            for (int i = index - lookback + 1; i <= index; i++) {
                double close = series.getBar(i).getClosePrice().doubleValue();

                if (close < trough) {
                    trough = close;
                }

                if (trough > 0.0) {
                    double drawup = ((close - trough) / trough) * 100.0;
                    if (drawup > maxDrawup) {
                        maxDrawup = drawup;
                    }
                }
            }

            return maxDrawup;
        } catch (Exception e) {
            log.debug("Failed to calculate drawup at index {}: {}", index, e.getMessage());
            return null;
        }
    }

    private StatisticalIndicators createEmptyIndicators() {
        return new StatisticalIndicators(
                null, null, null, null, null,
                null, null, null, null, null
        );
    }
}
