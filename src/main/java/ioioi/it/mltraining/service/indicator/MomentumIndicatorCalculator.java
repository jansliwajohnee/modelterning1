package ioioi.it.mltraining.service.indicator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CCIIndicator;
import org.ta4j.core.indicators.CMOIndicator;
import org.ta4j.core.indicators.ROCIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;
import org.ta4j.core.indicators.StochasticOscillatorDIndicator;
import org.ta4j.core.indicators.StochasticRSIIndicator;
import org.ta4j.core.indicators.WilliamsRIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import java.util.Optional;

/**
 * Service for calculating momentum technical indicators using Ta4j library.
 * All indicators are normalized/scaled for LightGBM model training.
 *
 * <p>Momentum indicators measure the speed of price movements and help identify
 * overbought/oversold conditions. All calculations use relative values suitable
 * for machine learning models.</p>
 */
@Service
@Slf4j
public class MomentumIndicatorCalculator {

    private static final int RSI_7_PERIOD = 7;
    private static final int RSI_14_PERIOD = 14;
    private static final int RSI_21_PERIOD = 21;
    private static final int STOCHASTIC_K_PERIOD = 14;
    private static final int STOCHASTIC_D_PERIOD = 3;
    private static final int WILLIAMS_R_PERIOD = 14;
    private static final int CCI_PERIOD = 20;
    private static final int MFI_PERIOD = 14;
    private static final int CMO_PERIOD = 14;
    private static final int ROC_PERIOD = 12;
    private static final int ULTIMATE_OSC_SHORT = 7;
    private static final int ULTIMATE_OSC_MEDIUM = 14;
    private static final int ULTIMATE_OSC_LONG = 28;
    private static final double CCI_NORMALIZATION_FACTOR = 100.0;

    /**
     * Calculates all momentum indicators for a specific bar index in the series.
     *
     * @param series the bar series with OHLCV data
     * @param index the index of the bar to calculate indicators for
     * @return MomentumIndicators record with all calculated values
     */
    public MomentumIndicators calculate(BarSeries series, int index) {
        if (series == null || index < 0 || index >= series.getBarCount()) {
            log.warn("Invalid series or index: index={}, barCount={}", index,
                    series != null ? series.getBarCount() : 0);
            return createEmptyIndicators();
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        // RSI calculations (0-100)
        Double rsi7 = calculateRSI(series, index, RSI_7_PERIOD);
        Double rsi14 = calculateRSI(series, index, RSI_14_PERIOD);
        Double rsi21 = calculateRSI(series, index, RSI_21_PERIOD);

        // Stochastic Oscillator (0-100)
        Double stochasticK = calculateStochasticK(series, index);
        Double stochasticD = calculateStochasticD(series, index);

        // Stochastic RSI (0-100)
        Double stochasticRsi = calculateStochasticRSI(series, index);

        // Williams %R (-100 to 0)
        Double williamsR = calculateWilliamsR(series, index);

        // CCI normalized (divide by 100 for scaling)
        Double cciNormalized = calculateCCINormalized(series, index);

        // Money Flow Index (0-100) - TODO: Manual implementation (not in Ta4j 0.16)
        Double mfi = null;

        // Chande Momentum Oscillator (-100 to 100)
        Double cmo = calculateCMO(series, index);

        // Ultimate Oscillator (0-100) - TODO: Manual implementation (not in Ta4j 0.16)
        Double ultimateOscillator = null;

        // Rate of Change (percent)
        Double rocPercent = calculateROC(series, index);

        // RSI-derived features
        Double rsiDistanceFrom50 = Optional.ofNullable(rsi14)
                .map(rsi -> rsi - 50.0)
                .orElse(null);

        Double rsiSlope = calculateRSISlope(series, index, rsi14);

        return new MomentumIndicators(
                rsi7, rsi14, rsi21,
                stochasticK, stochasticD,
                stochasticRsi,
                williamsR,
                cciNormalized,
                mfi,
                cmo,
                ultimateOscillator,
                rocPercent,
                rsiDistanceFrom50,
                rsiSlope
        );
    }

    private Double calculateRSI(BarSeries series, int index, int period) {
        if (!hasEnoughBars(series, index, period)) {
            return null;
        }
        try {
            ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
            RSIIndicator rsi = new RSIIndicator(closePrice, period);
            return rsi.getValue(index).doubleValue();
        } catch (Exception e) {
            log.debug("Failed to calculate RSI-{} at index {}: {}", period, index, e.getMessage());
            return null;
        }
    }

    private Double calculateStochasticK(BarSeries series, int index) {
        if (!hasEnoughBars(series, index, STOCHASTIC_K_PERIOD)) {
            return null;
        }
        try {
            StochasticOscillatorKIndicator stochK = new StochasticOscillatorKIndicator(series, STOCHASTIC_K_PERIOD);
            return stochK.getValue(index).doubleValue();
        } catch (Exception e) {
            log.debug("Failed to calculate Stochastic K at index {}: {}", index, e.getMessage());
            return null;
        }
    }

    private Double calculateStochasticD(BarSeries series, int index) {
        if (!hasEnoughBars(series, index, STOCHASTIC_K_PERIOD + STOCHASTIC_D_PERIOD)) {
            return null;
        }
        try {
            StochasticOscillatorKIndicator stochK = new StochasticOscillatorKIndicator(series, STOCHASTIC_K_PERIOD);
            StochasticOscillatorDIndicator stochD = new StochasticOscillatorDIndicator(stochK);
            return stochD.getValue(index).doubleValue();
        } catch (Exception e) {
            log.debug("Failed to calculate Stochastic D at index {}: {}", index, e.getMessage());
            return null;
        }
    }

    private Double calculateStochasticRSI(BarSeries series, int index) {
        if (!hasEnoughBars(series, index, RSI_14_PERIOD + STOCHASTIC_K_PERIOD)) {
            return null;
        }
        try {
            ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
            RSIIndicator rsi = new RSIIndicator(closePrice, RSI_14_PERIOD);
            StochasticRSIIndicator stochRsi = new StochasticRSIIndicator(rsi, STOCHASTIC_K_PERIOD);
            return stochRsi.getValue(index).doubleValue();
        } catch (Exception e) {
            log.debug("Failed to calculate Stochastic RSI at index {}: {}", index, e.getMessage());
            return null;
        }
    }

    private Double calculateWilliamsR(BarSeries series, int index) {
        if (!hasEnoughBars(series, index, WILLIAMS_R_PERIOD)) {
            return null;
        }
        try {
            WilliamsRIndicator williamsR = new WilliamsRIndicator(series, WILLIAMS_R_PERIOD);
            return williamsR.getValue(index).doubleValue();
        } catch (Exception e) {
            log.debug("Failed to calculate Williams R at index {}: {}", index, e.getMessage());
            return null;
        }
    }

    private Double calculateCCINormalized(BarSeries series, int index) {
        if (!hasEnoughBars(series, index, CCI_PERIOD)) {
            return null;
        }
        try {
            CCIIndicator cci = new CCIIndicator(series, CCI_PERIOD);
            double cciValue = cci.getValue(index).doubleValue();
            return cciValue / CCI_NORMALIZATION_FACTOR;
        } catch (Exception e) {
            log.debug("Failed to calculate CCI at index {}: {}", index, e.getMessage());
            return null;
        }
    }

    private Double calculateCMO(BarSeries series, int index) {
        if (!hasEnoughBars(series, index, CMO_PERIOD)) {
            return null;
        }
        try {
            ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
            CMOIndicator cmo = new CMOIndicator(closePrice, CMO_PERIOD);
            return cmo.getValue(index).doubleValue();
        } catch (Exception e) {
            log.debug("Failed to calculate CMO at index {}: {}", index, e.getMessage());
            return null;
        }
    }

    private Double calculateROC(BarSeries series, int index) {
        if (!hasEnoughBars(series, index, ROC_PERIOD)) {
            return null;
        }
        try {
            ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
            ROCIndicator roc = new ROCIndicator(closePrice, ROC_PERIOD);
            return roc.getValue(index).doubleValue();
        } catch (Exception e) {
            log.debug("Failed to calculate ROC at index {}: {}", index, e.getMessage());
            return null;
        }
    }

    private Double calculateRSISlope(BarSeries series, int index, Double currentRsi) {
        if (currentRsi == null || index < 1) {
            return null;
        }
        try {
            Double previousRsi = calculateRSI(series, index - 1, RSI_14_PERIOD);
            return Optional.ofNullable(previousRsi)
                    .map(prev -> currentRsi - prev)
                    .orElse(null);
        } catch (Exception e) {
            log.debug("Failed to calculate RSI slope at index {}: {}", index, e.getMessage());
            return null;
        }
    }

    private boolean hasEnoughBars(BarSeries series, int index, int requiredPeriod) {
        return index >= requiredPeriod - 1;
    }

    private MomentumIndicators createEmptyIndicators() {
        return new MomentumIndicators(
                null, null, null, null, null, null, null,
                null, null, null, null, null, null, null
        );
    }
}
