package ioioi.it.mltraining.service.indicator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;

/**
 * Service for calculating price action technical indicators.
 * All indicators are normalized/scaled for LightGBM model training.
 *
 * <p>Price action indicators measure candle patterns, gaps, momentum and price position
 * within ranges. All calculations use ratios and percentages for normalization.</p>
 */
@Service
@Slf4j
public class PriceActionIndicatorCalculator {

    // Lookback periods
    private static final int HIGHEST_LOWEST_PERIOD = 50;
    private static final int POSITION_RANGE_PERIOD = 20;
    private static final int CONSECUTIVE_PERIOD = 10;
    private static final int HIGHER_PATTERN_PERIOD = 20;

    /**
     * Calculates all price action indicators for a specific bar index in the series.
     *
     * @param series the bar series with OHLCV data
     * @param index the index of the bar to calculate indicators for
     * @return PriceActionIndicators record with all calculated values (normalized for LightGBM)
     */
    public PriceActionIndicators calculate(BarSeries series, int index) {
        if (series == null || index < 0 || index >= series.getBarCount()) {
            log.warn("Invalid series or index: index={}, barCount={}", index,
                    series != null ? series.getBarCount() : 0);
            return createEmptyIndicators();
        }

        Bar bar = series.getBar(index);
        double open = bar.getOpenPrice().doubleValue();
        double high = bar.getHighPrice().doubleValue();
        double low = bar.getLowPrice().doubleValue();
        double close = bar.getClosePrice().doubleValue();
        double range = high - low;

        // Candle body and wick calculations
        double bodySize = Math.abs(close - open);
        Double bodyRangeRatio = range > 0.0 ? bodySize / range : 0.0;

        double upperWick = high - Math.max(open, close);
        Double upperWickRangeRatio = range > 0.0 ? upperWick / range : 0.0;

        double lowerWick = Math.min(open, close) - low;
        Double lowerWickRangeRatio = range > 0.0 ? lowerWick / range : 0.0;

        Double closePositionInRange = range > 0.0 ? (close - low) / range : 0.0;

        // Body as percentage of price
        Double bodyPct = close > 0.0 ? (bodySize / close) * 100.0 : 0.0;

        // Gap from previous close
        Double gapPct = calculateGapPct(series, index, open);

        // Bullish or bearish
        Boolean isBullish = close >= open;

        // Forward returns
        Double return1 = calculateForwardReturn(series, index, close, 1);
        Double return3 = calculateForwardReturn(series, index, close, 3);
        Double return5 = calculateForwardReturn(series, index, close, 5);
        Double return10 = calculateForwardReturn(series, index, close, 10);
        Double return20 = calculateForwardReturn(series, index, close, 20);
        Double return50 = calculateForwardReturn(series, index, close, 50);

        // Distance from highest/lowest
        Double highestHighDistancePct = calculateHighestHighDistance(series, index, close);
        Double lowestLowDistancePct = calculateLowestLowDistance(series, index, close);

        // Position in N-period range
        Double positionInRangeN = calculatePositionInRange(series, index, close);

        // Consecutive patterns
        Double consecutiveBullishRatio = calculateConsecutiveRatio(series, index, true);
        Double consecutiveBearishRatio = calculateConsecutiveRatio(series, index, false);

        // Higher patterns
        Double higherHighsRatio = calculateHigherHighsRatio(series, index);
        Double higherClosesRatio = calculateHigherClosesRatio(series, index);

        return new PriceActionIndicators(
                bodyRangeRatio, upperWickRangeRatio, lowerWickRangeRatio,
                closePositionInRange, bodyPct, gapPct,
                isBullish,
                return1, return3, return5, return10, return20, return50,
                highestHighDistancePct, lowestLowDistancePct,
                positionInRangeN,
                consecutiveBullishRatio, consecutiveBearishRatio,
                higherHighsRatio, higherClosesRatio
        );
    }

    /**
     * Calculates gap percentage from previous close.
     * Gap % = ((Open - Previous Close) / Previous Close) * 100
     */
    private Double calculateGapPct(BarSeries series, int index, double open) {
        if (index < 1) {
            return null;
        }
        try {
            double previousClose = series.getBar(index - 1).getClosePrice().doubleValue();
            return previousClose > 0.0 ? ((open - previousClose) / previousClose) * 100.0 : null;
        } catch (Exception e) {
            log.debug("Failed to calculate gap at index {}: {}", index, e.getMessage());
            return null;
        }
    }

    /**
     * Calculates forward return percentage.
     * Return % = ((Future Close - Current Close) / Current Close) * 100
     * Returns null if future data not available
     */
    private Double calculateForwardReturn(BarSeries series, int index, double currentClose, int periods) {
        int futureIndex = index + periods;
        if (futureIndex >= series.getBarCount()) {
            return null; // Future data not available
        }
        try {
            double futureClose = series.getBar(futureIndex).getClosePrice().doubleValue();
            return currentClose > 0.0 ? ((futureClose - currentClose) / currentClose) * 100.0 : null;
        } catch (Exception e) {
            log.debug("Failed to calculate forward return {} at index {}: {}", periods, index, e.getMessage());
            return null;
        }
    }

    /**
     * Calculates distance from highest high in period as percentage.
     * Distance % = ((Highest High - Current Close) / Current Close) * 100
     * Positive = Below highest high
     */
    private Double calculateHighestHighDistance(BarSeries series, int index, double currentClose) {
        int lookback = Math.min(HIGHEST_LOWEST_PERIOD, index + 1);
        if (lookback < 2) {
            return null;
        }
        try {
            double highestHigh = Double.MIN_VALUE;
            for (int i = index - lookback + 1; i <= index; i++) {
                double high = series.getBar(i).getHighPrice().doubleValue();
                if (high > highestHigh) {
                    highestHigh = high;
                }
            }
            return currentClose > 0.0 ? ((highestHigh - currentClose) / currentClose) * 100.0 : null;
        } catch (Exception e) {
            log.debug("Failed to calculate highest high distance at index {}: {}", index, e.getMessage());
            return null;
        }
    }

    /**
     * Calculates distance from lowest low in period as percentage.
     * Distance % = ((Current Close - Lowest Low) / Current Close) * 100
     * Positive = Above lowest low
     */
    private Double calculateLowestLowDistance(BarSeries series, int index, double currentClose) {
        int lookback = Math.min(HIGHEST_LOWEST_PERIOD, index + 1);
        if (lookback < 2) {
            return null;
        }
        try {
            double lowestLow = Double.MAX_VALUE;
            for (int i = index - lookback + 1; i <= index; i++) {
                double low = series.getBar(i).getLowPrice().doubleValue();
                if (low < lowestLow) {
                    lowestLow = low;
                }
            }
            return currentClose > 0.0 ? ((currentClose - lowestLow) / currentClose) * 100.0 : null;
        } catch (Exception e) {
            log.debug("Failed to calculate lowest low distance at index {}: {}", index, e.getMessage());
            return null;
        }
    }

    /**
     * Calculates position in N-period range.
     * Position = (Close - Lowest Low) / (Highest High - Lowest Low)
     * Range: 0-1 (0 = at lowest, 1 = at highest)
     */
    private Double calculatePositionInRange(BarSeries series, int index, double currentClose) {
        int lookback = Math.min(POSITION_RANGE_PERIOD, index + 1);
        if (lookback < 2) {
            return null;
        }
        try {
            double highestHigh = Double.MIN_VALUE;
            double lowestLow = Double.MAX_VALUE;

            for (int i = index - lookback + 1; i <= index; i++) {
                double high = series.getBar(i).getHighPrice().doubleValue();
                double low = series.getBar(i).getLowPrice().doubleValue();
                if (high > highestHigh) highestHigh = high;
                if (low < lowestLow) lowestLow = low;
            }

            double rangeSize = highestHigh - lowestLow;
            return rangeSize > 0.0 ? (currentClose - lowestLow) / rangeSize : null;
        } catch (Exception e) {
            log.debug("Failed to calculate position in range at index {}: {}", index, e.getMessage());
            return null;
        }
    }

    /**
     * Calculates ratio of consecutive bullish or bearish candles.
     * Counts consecutive candles in same direction up to lookback period.
     * Ratio = Consecutive Count / Lookback Period
     */
    private Double calculateConsecutiveRatio(BarSeries series, int index, boolean bullish) {
        int lookback = Math.min(CONSECUTIVE_PERIOD, index + 1);
        if (lookback < 1) {
            return null;
        }
        try {
            int consecutiveCount = 0;

            // Count consecutive candles backwards
            for (int i = index; i >= Math.max(0, index - lookback + 1); i--) {
                Bar bar = series.getBar(i);
                boolean isBullishCandle = bar.getClosePrice().doubleValue() >= bar.getOpenPrice().doubleValue();

                if (isBullishCandle == bullish) {
                    consecutiveCount++;
                } else {
                    break; // Stop counting when pattern breaks
                }
            }

            return consecutiveCount / (double) lookback;
        } catch (Exception e) {
            log.debug("Failed to calculate consecutive ratio at index {}: {}", index, e.getMessage());
            return null;
        }
    }

    /**
     * Calculates ratio of higher highs in lookback period.
     * Higher High = High[i] > High[i-1]
     * Ratio = Count of Higher Highs / (Lookback - 1)
     */
    private Double calculateHigherHighsRatio(BarSeries series, int index) {
        int lookback = Math.min(HIGHER_PATTERN_PERIOD, index + 1);
        if (lookback < 2) {
            return null;
        }
        try {
            int higherHighsCount = 0;

            for (int i = index - lookback + 2; i <= index; i++) {
                double currentHigh = series.getBar(i).getHighPrice().doubleValue();
                double previousHigh = series.getBar(i - 1).getHighPrice().doubleValue();

                if (currentHigh > previousHigh) {
                    higherHighsCount++;
                }
            }

            return higherHighsCount / (double) (lookback - 1);
        } catch (Exception e) {
            log.debug("Failed to calculate higher highs ratio at index {}: {}", index, e.getMessage());
            return null;
        }
    }

    /**
     * Calculates ratio of higher closes in lookback period.
     * Higher Close = Close[i] > Close[i-1]
     * Ratio = Count of Higher Closes / (Lookback - 1)
     */
    private Double calculateHigherClosesRatio(BarSeries series, int index) {
        int lookback = Math.min(HIGHER_PATTERN_PERIOD, index + 1);
        if (lookback < 2) {
            return null;
        }
        try {
            int higherClosesCount = 0;

            for (int i = index - lookback + 2; i <= index; i++) {
                double currentClose = series.getBar(i).getClosePrice().doubleValue();
                double previousClose = series.getBar(i - 1).getClosePrice().doubleValue();

                if (currentClose > previousClose) {
                    higherClosesCount++;
                }
            }

            return higherClosesCount / (double) (lookback - 1);
        } catch (Exception e) {
            log.debug("Failed to calculate higher closes ratio at index {}: {}", index, e.getMessage());
            return null;
        }
    }

    private PriceActionIndicators createEmptyIndicators() {
        return new PriceActionIndicators(
                null, null, null, null, null, null,
                null, null, null, null, null, null, null,
                null, null, null, null, null, null, null
        );
    }
}
