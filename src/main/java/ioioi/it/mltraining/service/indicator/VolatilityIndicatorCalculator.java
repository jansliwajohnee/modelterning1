package ioioi.it.mltraining.service.indicator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.averages.SMAIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelLowerIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelMiddleIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelUpperIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.num.DecimalNum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service for calculating volatility technical indicators using Ta4j library.
 * All indicators are normalized/scaled for LightGBM model training.
 *
 * <p>Volatility indicators measure price variation and market uncertainty.
 * All calculations use percentage or ratio normalization for scale independence.</p>
 */
@Service
@Slf4j
public class VolatilityIndicatorCalculator {

    // ATR periods
    private static final int ATR_14_PERIOD = 14;
    private static final int ATR_7_PERIOD = 7;
    private static final int ATR_21_PERIOD = 21;

    // Bollinger Bands
    private static final int BB_PERIOD = 20;
    private static final double BB_MULTIPLIER = 2.0;

    // Keltner Channels
    private static final int KELTNER_PERIOD = 20;
    private static final double KELTNER_MULTIPLIER = 2.0;

    // Standard Deviation periods
    private static final int STDDEV_14_PERIOD = 14;
    private static final int STDDEV_50_PERIOD = 50;
    private static final int STDDEV_DEFAULT_PERIOD = 20;

    // Percentile lookback
    private static final int PERCENTILE_LOOKBACK = 100;

    /**
     * Calculates all volatility indicators for a specific bar index in the series.
     *
     * @param series the bar series with OHLCV data
     * @param index the index of the bar to calculate indicators for
     * @return VolatilityIndicators record with all calculated values (normalized for LightGBM)
     */
    public VolatilityIndicators calculate(BarSeries series, int index) {
        if (series == null || index < 0 || index >= series.getBarCount()) {
            log.warn("Invalid series or index: index={}, barCount={}", index,
                    series != null ? series.getBarCount() : 0);
            return createEmptyIndicators();
        }

        double currentClose = series.getBar(index).getClosePrice().doubleValue();
        double currentHigh = series.getBar(index).getHighPrice().doubleValue();
        double currentLow = series.getBar(index).getLowPrice().doubleValue();
        double currentRange = currentHigh - currentLow;

        // ATR calculations
        Double atrPct = calculateATRPct(series, index, currentClose);
        Double atr721Ratio = calculateATRRatio(series, index);

        // Bollinger Bands
        Double bbPercentB = calculateBBPercentB(series, index);
        Double bbWidthPct = calculateBBWidthPct(series, index, currentClose);
        Double bbPosition = bbPercentB; // Same as %B in this context

        // Keltner Channels
        Double keltnerPercentK = calculateKeltnerPercentK(series, index);
        Double keltnerWidthPct = calculateKeltnerWidthPct(series, index, currentClose);

        // Range calculations
        Double rangePct = (currentRange / currentClose) * 100.0;
        Double rangeAtrRatio = calculateRangeATRRatio(series, index, currentRange);

        // ATR Percentile
        Double atrPercentile = calculateATRPercentile(series, index);

        // Standard Deviation
        Double stddevPct = calculateStdDevPct(series, index, currentClose);
        Double stddev1450Ratio = calculateStdDevRatio(series, index);

        return new VolatilityIndicators(
                atrPct, atr721Ratio,
                bbPercentB, bbWidthPct, bbPosition,
                keltnerPercentK, keltnerWidthPct,
                rangePct, rangeAtrRatio,
                atrPercentile,
                stddevPct, stddev1450Ratio
        );
    }

    /**
     * Calculates ATR as percentage of current price.
     * ATR% = (ATR / Close) * 100
     */
    private Double calculateATRPct(BarSeries series, int index, double currentClose) {
        if (!hasEnoughBars(series, index, ATR_14_PERIOD)) {
            return null;
        }
        try {
            ATRIndicator atr = new ATRIndicator(series, ATR_14_PERIOD);
            double atrValue = atr.getValue(index).doubleValue();
            return (atrValue / currentClose) * 100.0;
        } catch (Exception e) {
            log.debug("Failed to calculate ATR% at index {}: {}", index, e.getMessage());
            return null;
        }
    }

    /**
     * Calculates ratio of ATR(7) to ATR(21).
     * Ratio > 1: Increasing volatility
     * Ratio < 1: Decreasing volatility
     */
    private Double calculateATRRatio(BarSeries series, int index) {
        if (!hasEnoughBars(series, index, ATR_21_PERIOD)) {
            return null;
        }
        try {
            ATRIndicator atr7 = new ATRIndicator(series, ATR_7_PERIOD);
            ATRIndicator atr21 = new ATRIndicator(series, ATR_21_PERIOD);

            double atr7Value = atr7.getValue(index).doubleValue();
            double atr21Value = atr21.getValue(index).doubleValue();

            return atr21Value > 0.0 ? atr7Value / atr21Value : null;
        } catch (Exception e) {
            log.debug("Failed to calculate ATR ratio at index {}: {}", index, e.getMessage());
            return null;
        }
    }

    /**
     * Calculates Bollinger Bands %B.
     * %B = (Close - Lower Band) / (Upper Band - Lower Band)
     * Range: 0-1 (normalized position within bands)
     * %B > 1: Above upper band
     * %B < 0: Below lower band
     */
    private Double calculateBBPercentB(BarSeries series, int index) {
        if (!hasEnoughBars(series, index, BB_PERIOD)) {
            return null;
        }
        try {
            ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
            BollingerBandsMiddleIndicator bbMiddle = new BollingerBandsMiddleIndicator(
                    new SMAIndicator(closePrice, BB_PERIOD));
            StandardDeviationIndicator stdDev = new StandardDeviationIndicator(closePrice, BB_PERIOD);

            BollingerBandsLowerIndicator bbLower = new BollingerBandsLowerIndicator(bbMiddle, stdDev, DecimalNum.valueOf(BB_MULTIPLIER));
            BollingerBandsUpperIndicator bbUpper = new BollingerBandsUpperIndicator(bbMiddle, stdDev, DecimalNum.valueOf(BB_MULTIPLIER));

            double close = closePrice.getValue(index).doubleValue();
            double lower = bbLower.getValue(index).doubleValue();
            double upper = bbUpper.getValue(index).doubleValue();
            double width = upper - lower;

            return width > 0.0 ? (close - lower) / width : 0.5;
        } catch (Exception e) {
            log.debug("Failed to calculate BB %B at index {}: {}", index, e.getMessage());
            return null;
        }
    }

    /**
     * Calculates Bollinger Bands width as percentage of price.
     * BB Width% = ((Upper Band - Lower Band) / Middle Band) * 100
     */
    private Double calculateBBWidthPct(BarSeries series, int index, double currentClose) {
        if (!hasEnoughBars(series, index, BB_PERIOD)) {
            return null;
        }
        try {
            ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
            BollingerBandsMiddleIndicator bbMiddle = new BollingerBandsMiddleIndicator(
                    new SMAIndicator(closePrice, BB_PERIOD));
            StandardDeviationIndicator stdDev = new StandardDeviationIndicator(closePrice, BB_PERIOD);

            BollingerBandsLowerIndicator bbLower = new BollingerBandsLowerIndicator(bbMiddle, stdDev, DecimalNum.valueOf(BB_MULTIPLIER));
            BollingerBandsUpperIndicator bbUpper = new BollingerBandsUpperIndicator(bbMiddle, stdDev, DecimalNum.valueOf(BB_MULTIPLIER));

            double lower = bbLower.getValue(index).doubleValue();
            double upper = bbUpper.getValue(index).doubleValue();
            double middle = bbMiddle.getValue(index).doubleValue();

            return middle > 0.0 ? ((upper - lower) / middle) * 100.0 : null;
        } catch (Exception e) {
            log.debug("Failed to calculate BB width% at index {}: {}", index, e.getMessage());
            return null;
        }
    }

    /**
     * Calculates Keltner Channel %K (position within channel).
     * Similar to BB %B but uses ATR instead of StdDev.
     */
    private Double calculateKeltnerPercentK(BarSeries series, int index) {
        if (!hasEnoughBars(series, index, KELTNER_PERIOD)) {
            return null;
        }
        try {
            ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
            KeltnerChannelMiddleIndicator kcMiddle = new KeltnerChannelMiddleIndicator(
                    new SMAIndicator(closePrice, KELTNER_PERIOD), KELTNER_PERIOD);
            ATRIndicator atr = new ATRIndicator(series, KELTNER_PERIOD);

            KeltnerChannelLowerIndicator kcLower = new KeltnerChannelLowerIndicator(kcMiddle, atr, KELTNER_MULTIPLIER);
            KeltnerChannelUpperIndicator kcUpper = new KeltnerChannelUpperIndicator(kcMiddle, atr, KELTNER_MULTIPLIER);

            double close = closePrice.getValue(index).doubleValue();
            double lower = kcLower.getValue(index).doubleValue();
            double upper = kcUpper.getValue(index).doubleValue();
            double width = upper - lower;

            return width > 0.0 ? (close - lower) / width : 0.5;
        } catch (Exception e) {
            log.debug("Failed to calculate Keltner %K at index {}: {}", index, e.getMessage());
            return null;
        }
    }

    /**
     * Calculates Keltner Channel width as percentage of price.
     */
    private Double calculateKeltnerWidthPct(BarSeries series, int index, double currentClose) {
        if (!hasEnoughBars(series, index, KELTNER_PERIOD)) {
            return null;
        }
        try {
            ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
            KeltnerChannelMiddleIndicator kcMiddle = new KeltnerChannelMiddleIndicator(
                    new SMAIndicator(closePrice, KELTNER_PERIOD), KELTNER_PERIOD);
            ATRIndicator atr = new ATRIndicator(series, KELTNER_PERIOD);

            KeltnerChannelLowerIndicator kcLower = new KeltnerChannelLowerIndicator(kcMiddle, atr, KELTNER_MULTIPLIER);
            KeltnerChannelUpperIndicator kcUpper = new KeltnerChannelUpperIndicator(kcMiddle, atr, KELTNER_MULTIPLIER);

            double lower = kcLower.getValue(index).doubleValue();
            double upper = kcUpper.getValue(index).doubleValue();
            double middle = kcMiddle.getValue(index).doubleValue();

            return middle > 0.0 ? ((upper - lower) / middle) * 100.0 : null;
        } catch (Exception e) {
            log.debug("Failed to calculate Keltner width% at index {}: {}", index, e.getMessage());
            return null;
        }
    }

    /**
     * Calculates ratio of current range to ATR.
     * Ratio > 1: Larger than average range (high volatility day)
     * Ratio < 1: Smaller than average range (low volatility day)
     */
    private Double calculateRangeATRRatio(BarSeries series, int index, double currentRange) {
        if (!hasEnoughBars(series, index, ATR_14_PERIOD)) {
            return null;
        }
        try {
            ATRIndicator atr = new ATRIndicator(series, ATR_14_PERIOD);
            double atrValue = atr.getValue(index).doubleValue();

            return atrValue > 0.0 ? currentRange / atrValue : null;
        } catch (Exception e) {
            log.debug("Failed to calculate Range/ATR ratio at index {}: {}", index, e.getMessage());
            return null;
        }
    }

    /**
     * Calculates ATR percentile over lookback period.
     * Range: 0-100
     * High percentile = High current volatility relative to history
     */
    private Double calculateATRPercentile(BarSeries series, int index) {
        int lookback = Math.min(PERCENTILE_LOOKBACK, index + 1);
        if (!hasEnoughBars(series, index, ATR_14_PERIOD) || lookback < 20) {
            return null;
        }
        try {
            ATRIndicator atr = new ATRIndicator(series, ATR_14_PERIOD);
            double currentATR = atr.getValue(index).doubleValue();

            List<Double> atrValues = new ArrayList<>();
            for (int i = index - lookback + 1; i <= index; i++) {
                if (i >= ATR_14_PERIOD - 1) {
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
     * Calculates standard deviation as percentage of price.
     */
    private Double calculateStdDevPct(BarSeries series, int index, double currentClose) {
        if (!hasEnoughBars(series, index, STDDEV_DEFAULT_PERIOD)) {
            return null;
        }
        try {
            ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
            StandardDeviationIndicator stdDev = new StandardDeviationIndicator(closePrice, STDDEV_DEFAULT_PERIOD);

            double stdDevValue = stdDev.getValue(index).doubleValue();
            return (stdDevValue / currentClose) * 100.0;
        } catch (Exception e) {
            log.debug("Failed to calculate StdDev% at index {}: {}", index, e.getMessage());
            return null;
        }
    }

    /**
     * Calculates ratio of StdDev(14) to StdDev(50).
     * Ratio > 1: Increasing volatility
     * Ratio < 1: Decreasing volatility
     */
    private Double calculateStdDevRatio(BarSeries series, int index) {
        if (!hasEnoughBars(series, index, STDDEV_50_PERIOD)) {
            return null;
        }
        try {
            ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
            StandardDeviationIndicator stdDev14 = new StandardDeviationIndicator(closePrice, STDDEV_14_PERIOD);
            StandardDeviationIndicator stdDev50 = new StandardDeviationIndicator(closePrice, STDDEV_50_PERIOD);

            double stdDev14Value = stdDev14.getValue(index).doubleValue();
            double stdDev50Value = stdDev50.getValue(index).doubleValue();

            return stdDev50Value > 0.0 ? stdDev14Value / stdDev50Value : null;
        } catch (Exception e) {
            log.debug("Failed to calculate StdDev ratio at index {}: {}", index, e.getMessage());
            return null;
        }
    }

    private boolean hasEnoughBars(BarSeries series, int index, int requiredPeriod) {
        return index >= requiredPeriod - 1;
    }

    private VolatilityIndicators createEmptyIndicators() {
        return new VolatilityIndicators(
                null, null, null, null, null,
                null, null, null, null, null,
                null, null
        );
    }
}
