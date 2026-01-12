package ioioi.it.mltraining.service.indicator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.averages.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

/**
 * Service for calculating MACD technical indicators using Ta4j library.
 * All indicators are normalized/scaled for LightGBM model training.
 *
 * <p>MACD (Moving Average Convergence Divergence) measures trend momentum and direction.
 * All calculations are normalized as percentages of current price.</p>
 */
@Service
@Slf4j
public class MACDIndicatorCalculator {

    // Standard MACD parameters
    private static final int FAST_PERIOD = 12;
    private static final int SLOW_PERIOD = 26;
    private static final int SIGNAL_PERIOD = 9;

    /**
     * Calculates all MACD indicators for a specific bar index in the series.
     *
     * @param series the bar series with OHLCV data
     * @param index the index of the bar to calculate indicators for
     * @return MACDIndicators record with all calculated values (normalized for LightGBM)
     */
    public MACDIndicators calculate(BarSeries series, int index) {
        if (series == null || index < 0 || index >= series.getBarCount()) {
            log.warn("Invalid series or index: index={}, barCount={}", index,
                    series != null ? series.getBarCount() : 0);
            return createEmptyIndicators();
        }

        double currentClose = series.getBar(index).getClosePrice().doubleValue();

        // Calculate MACD components
        Double macdPct = calculateMACDPct(series, index, currentClose);
        Double macdSignalPct = calculateMACDSignalPct(series, index, currentClose);

        // MACD Histogram
        Double macdHistogramPct = null;
        if (macdPct != null && macdSignalPct != null) {
            macdHistogramPct = macdPct - macdSignalPct;
        }

        // MACD Histogram change
        Double macdHistogramChange = calculateMACDHistogramChange(series, index, currentClose);

        // Boolean indicators
        Boolean macdGtSignal = null;
        if (macdPct != null && macdSignalPct != null) {
            macdGtSignal = macdPct > macdSignalPct;
        }

        Boolean macdGtZero = null;
        if (macdPct != null) {
            macdGtZero = macdPct > 0.0;
        }

        return new MACDIndicators(
                macdPct, macdSignalPct, macdHistogramPct,
                macdHistogramChange,
                macdGtSignal, macdGtZero
        );
    }

    /**
     * Calculates MACD line as percentage of current price.
     * MACD = EMA(12) - EMA(26)
     * MACD% = (MACD / Close) * 100
     */
    private Double calculateMACDPct(BarSeries series, int index, double currentClose) {
        if (index < SLOW_PERIOD - 1) {
            return null;
        }
        try {
            ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
            MACDIndicator macd = new MACDIndicator(closePrice, FAST_PERIOD, SLOW_PERIOD);

            double macdValue = macd.getValue(index).doubleValue();
            return (macdValue / currentClose) * 100.0;
        } catch (Exception e) {
            log.debug("Failed to calculate MACD at index {}: {}", index, e.getMessage());
            return null;
        }
    }

    /**
     * Calculates MACD signal line as percentage of current price.
     * Signal = EMA(9) of MACD
     * Signal% = (Signal / Close) * 100
     */
    private Double calculateMACDSignalPct(BarSeries series, int index, double currentClose) {
        if (index < SLOW_PERIOD + SIGNAL_PERIOD - 2) {
            return null;
        }
        try {
            ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
            MACDIndicator macd = new MACDIndicator(closePrice, FAST_PERIOD, SLOW_PERIOD);
            EMAIndicator signal = new EMAIndicator(macd, SIGNAL_PERIOD);

            double signalValue = signal.getValue(index).doubleValue();
            return (signalValue / currentClose) * 100.0;
        } catch (Exception e) {
            log.debug("Failed to calculate MACD signal at index {}: {}", index, e.getMessage());
            return null;
        }
    }

    /**
     * Calculates change in MACD histogram from previous bar.
     * Histogram Change = Histogram(current) - Histogram(previous)
     * Positive = Increasing bullish momentum
     * Negative = Decreasing bullish momentum (or increasing bearish)
     */
    private Double calculateMACDHistogramChange(BarSeries series, int index, double currentClose) {
        if (index < 1 || index < SLOW_PERIOD + SIGNAL_PERIOD - 1) {
            return null;
        }
        try {
            ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
            MACDIndicator macd = new MACDIndicator(closePrice, FAST_PERIOD, SLOW_PERIOD);
            EMAIndicator signal = new EMAIndicator(macd, SIGNAL_PERIOD);

            double currentMacd = macd.getValue(index).doubleValue();
            double currentSignal = signal.getValue(index).doubleValue();
            double currentHistogram = currentMacd - currentSignal;

            double previousMacd = macd.getValue(index - 1).doubleValue();
            double previousSignal = signal.getValue(index - 1).doubleValue();
            double previousHistogram = previousMacd - previousSignal;

            double histogramChange = currentHistogram - previousHistogram;
            return (histogramChange / currentClose) * 100.0;
        } catch (Exception e) {
            log.debug("Failed to calculate MACD histogram change at index {}: {}", index, e.getMessage());
            return null;
        }
    }

    private MACDIndicators createEmptyIndicators() {
        return new MACDIndicators(
                null, null, null, null, null, null
        );
    }
}
