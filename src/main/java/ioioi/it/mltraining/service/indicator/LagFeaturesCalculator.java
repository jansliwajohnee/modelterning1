package ioioi.it.mltraining.service.indicator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;

/**
 * Service for calculating lag features from historical indicator values.
 * All indicators are normalized/scaled for LightGBM model training.
 *
 * <p>Lag features capture temporal patterns by storing historical values
 * of key indicators. This allows the model to learn from recent history.</p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LagFeaturesCalculator {

    private final MomentumIndicatorCalculator momentumCalculator;
    private final VolatilityIndicatorCalculator volatilityCalculator;
    private final VolumeIndicatorCalculator volumeCalculator;
    private final TrendIndicatorCalculator trendCalculator;

    /**
     * Calculates all lag features for a specific bar index in the series.
     *
     * @param series the bar series with OHLCV data
     * @param index the index of the bar to calculate indicators for
     * @return LagFeatures record with all calculated values (normalized for LightGBM)
     */
    public LagFeatures calculate(BarSeries series, int index) {
        if (series == null || index < 0 || index >= series.getBarCount()) {
            log.warn("Invalid series or index: index={}, barCount={}", index,
                    series != null ? series.getBarCount() : 0);
            return createEmptyFeatures();
        }

        // Return lags
        Double returnLag1 = calculateReturnLag(series, index, 1);
        Double returnLag2 = calculateReturnLag(series, index, 2);
        Double returnLag3 = calculateReturnLag(series, index, 3);
        Double returnLag5 = calculateReturnLag(series, index, 5);
        Double returnLag10 = calculateReturnLag(series, index, 10);

        // RSI lags
        Double rsiLag1 = calculateRSILag(series, index, 1);
        Double rsiLag2 = calculateRSILag(series, index, 2);
        Double rsiLag3 = calculateRSILag(series, index, 3);

        // RSI change
        Double rsiChange = calculateRSIChange(series, index);

        // ATR% lags
        Double atrPctLag1 = calculateATRPctLag(series, index, 1);
        Double atrPctLag2 = calculateATRPctLag(series, index, 2);

        // RVol lags
        Double rvolLag1 = calculateRVolLag(series, index, 1);
        Double rvolLag2 = calculateRVolLag(series, index, 2);

        // BB %B lags
        Double bbPercentBLag1 = calculateBBPercentBLag(series, index, 1);
        Double bbPercentBLag2 = calculateBBPercentBLag(series, index, 2);

        // ADX lags
        Double adxLag1 = calculateADXLag(series, index, 1);
        Double adxLag2 = calculateADXLag(series, index, 2);

        // Body% lags
        Double bodyPctLag1 = calculateBodyPctLag(series, index, 1);
        Double bodyPctLag2 = calculateBodyPctLag(series, index, 2);

        return new LagFeatures(
                returnLag1, returnLag2, returnLag3, returnLag5, returnLag10,
                rsiLag1, rsiLag2, rsiLag3,
                rsiChange,
                atrPctLag1, atrPctLag2,
                rvolLag1, rvolLag2,
                bbPercentBLag1, bbPercentBLag2,
                adxLag1, adxLag2,
                bodyPctLag1, bodyPctLag2
        );
    }

    /**
     * Calculates return percentage from N bars ago.
     * Return = ((Close[index-lag] - Close[index-lag-1]) / Close[index-lag-1]) * 100
     */
    private Double calculateReturnLag(BarSeries series, int index, int lag) {
        int lagIndex = index - lag;
        if (lagIndex < 1) {
            return null;
        }
        try {
            double close = series.getBar(lagIndex).getClosePrice().doubleValue();
            double prevClose = series.getBar(lagIndex - 1).getClosePrice().doubleValue();
            return prevClose > 0.0 ? ((close - prevClose) / prevClose) * 100.0 : null;
        } catch (Exception e) {
            log.debug("Failed to calculate return lag {} at index {}: {}", lag, index, e.getMessage());
            return null;
        }
    }

    /**
     * Calculates RSI value from N bars ago.
     */
    private Double calculateRSILag(BarSeries series, int index, int lag) {
        int lagIndex = index - lag;
        if (lagIndex < 0) {
            return null;
        }
        try {
            MomentumIndicators indicators = momentumCalculator.calculate(series, lagIndex);
            return indicators.rsi14();
        } catch (Exception e) {
            log.debug("Failed to calculate RSI lag {} at index {}: {}", lag, index, e.getMessage());
            return null;
        }
    }

    /**
     * Calculates change in RSI from previous bar.
     * RSI Change = RSI(current) - RSI(previous)
     */
    private Double calculateRSIChange(BarSeries series, int index) {
        if (index < 1) {
            return null;
        }
        try {
            MomentumIndicators current = momentumCalculator.calculate(series, index);
            MomentumIndicators previous = momentumCalculator.calculate(series, index - 1);

            if (current.rsi14() != null && previous.rsi14() != null) {
                return current.rsi14() - previous.rsi14();
            }
            return null;
        } catch (Exception e) {
            log.debug("Failed to calculate RSI change at index {}: {}", index, e.getMessage());
            return null;
        }
    }

    /**
     * Calculates ATR% from N bars ago.
     */
    private Double calculateATRPctLag(BarSeries series, int index, int lag) {
        int lagIndex = index - lag;
        if (lagIndex < 0) {
            return null;
        }
        try {
            VolatilityIndicators indicators = volatilityCalculator.calculate(series, lagIndex);
            return indicators.atrPct();
        } catch (Exception e) {
            log.debug("Failed to calculate ATR% lag {} at index {}: {}", lag, index, e.getMessage());
            return null;
        }
    }

    /**
     * Calculates relative volume from N bars ago.
     */
    private Double calculateRVolLag(BarSeries series, int index, int lag) {
        int lagIndex = index - lag;
        if (lagIndex < 0) {
            return null;
        }
        try {
            VolumeIndicators indicators = volumeCalculator.calculate(series, lagIndex);
            return indicators.rvolSma20();
        } catch (Exception e) {
            log.debug("Failed to calculate RVol lag {} at index {}: {}", lag, index, e.getMessage());
            return null;
        }
    }

    /**
     * Calculates Bollinger %B from N bars ago.
     */
    private Double calculateBBPercentBLag(BarSeries series, int index, int lag) {
        int lagIndex = index - lag;
        if (lagIndex < 0) {
            return null;
        }
        try {
            VolatilityIndicators indicators = volatilityCalculator.calculate(series, lagIndex);
            return indicators.bbPercentB();
        } catch (Exception e) {
            log.debug("Failed to calculate BB %B lag {} at index {}: {}", lag, index, e.getMessage());
            return null;
        }
    }

    /**
     * Calculates ADX from N bars ago.
     */
    private Double calculateADXLag(BarSeries series, int index, int lag) {
        int lagIndex = index - lag;
        if (lagIndex < 0) {
            return null;
        }
        try {
            TrendIndicators indicators = trendCalculator.calculate(series, lagIndex);
            return indicators.adx();
        } catch (Exception e) {
            log.debug("Failed to calculate ADX lag {} at index {}: {}", lag, index, e.getMessage());
            return null;
        }
    }

    /**
     * Calculates body percentage from N bars ago.
     */
    private Double calculateBodyPctLag(BarSeries series, int index, int lag) {
        int lagIndex = index - lag;
        if (lagIndex < 0) {
            return null;
        }
        try {
            Bar bar = series.getBar(lagIndex);
            double open = bar.getOpenPrice().doubleValue();
            double close = bar.getClosePrice().doubleValue();
            double bodySize = Math.abs(close - open);
            return close > 0.0 ? (bodySize / close) * 100.0 : null;
        } catch (Exception e) {
            log.debug("Failed to calculate body% lag {} at index {}: {}", lag, index, e.getMessage());
            return null;
        }
    }

    private LagFeatures createEmptyFeatures() {
        return new LagFeatures(
                null, null, null, null, null,
                null, null, null, null,
                null, null, null, null,
                null, null, null, null,
                null, null
        );
    }
}
