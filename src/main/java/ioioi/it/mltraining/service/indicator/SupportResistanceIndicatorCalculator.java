package ioioi.it.mltraining.service.indicator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;

/**
 * Service for calculating support/resistance technical indicators.
 * All indicators are normalized/scaled for LightGBM model training.
 *
 * <p>Support/Resistance indicators identify key price levels that may act as barriers.
 * All calculations are normalized as percentages of current price.</p>
 */
@Service
@Slf4j
public class SupportResistanceIndicatorCalculator {

    // Lookback periods (proxies for "day" and "week")
    private static final int DAY_LOOKBACK = 24; // Assumes hourly data
    private static final int WEEK_LOOKBACK = 168; // 24 * 7
    private static final int VWAP_PERIOD = 20;

    // Round number threshold (e.g., multiples of 100, 1000, etc.)
    private static final double[] ROUND_FACTORS = {1.0, 5.0, 10.0, 50.0, 100.0, 500.0, 1000.0, 5000.0, 10000.0};

    /**
     * Calculates all support/resistance indicators for a specific bar index in the series.
     *
     * @param series the bar series with OHLCV data
     * @param index the index of the bar to calculate indicators for
     * @return SupportResistanceIndicators record with all calculated values (normalized for LightGBM)
     */
    public SupportResistanceIndicators calculate(BarSeries series, int index) {
        if (series == null || index < 0 || index >= series.getBarCount()) {
            log.warn("Invalid series or index: index={}, barCount={}", index,
                    series != null ? series.getBarCount() : 0);
            return createEmptyIndicators();
        }

        double currentClose = series.getBar(index).getClosePrice().doubleValue();

        // Pivot point and support/resistance levels
        PivotLevels pivotLevels = calculatePivotLevels(series, index);
        Double closePivotDistancePct = pivotLevels != null && pivotLevels.pivot > 0.0
                ? ((currentClose - pivotLevels.pivot) / currentClose) * 100.0
                : null;
        Double closeS1DistancePct = pivotLevels != null && pivotLevels.s1 > 0.0
                ? ((currentClose - pivotLevels.s1) / currentClose) * 100.0
                : null;
        Double closeR1DistancePct = pivotLevels != null && pivotLevels.r1 > 0.0
                ? ((currentClose - pivotLevels.r1) / currentClose) * 100.0
                : null;

        // Previous period high/low distances
        Double closePrevDayHighPct = calculatePreviousPeriodHighDistance(series, index, currentClose, DAY_LOOKBACK);
        Double closePrevDayLowPct = calculatePreviousPeriodLowDistance(series, index, currentClose, DAY_LOOKBACK);
        Double closePrevWeekHighPct = calculatePreviousPeriodHighDistance(series, index, currentClose, WEEK_LOOKBACK);

        // Round number distance
        Double closeRoundNumberDistancePct = calculateRoundNumberDistance(currentClose);

        // VWAP distance
        Double closeVwapDistancePct = calculateVWAPDistance(series, index, currentClose);

        return new SupportResistanceIndicators(
                closePivotDistancePct,
                closeS1DistancePct, closeR1DistancePct,
                closePrevDayHighPct, closePrevDayLowPct,
                closePrevWeekHighPct,
                closeRoundNumberDistancePct,
                closeVwapDistancePct
        );
    }

    /**
     * Calculates pivot point and support/resistance levels using classic pivot formula.
     * Pivot = (High + Low + Close) / 3
     * R1 = 2 * Pivot - Low
     * S1 = 2 * Pivot - High
     * Uses previous period (lookback) data
     */
    private PivotLevels calculatePivotLevels(BarSeries series, int index) {
        if (index < DAY_LOOKBACK) {
            return null;
        }
        try {
            // Use previous period (DAY_LOOKBACK bars ago to current)
            int startIdx = Math.max(0, index - DAY_LOOKBACK);
            int endIdx = index - 1;

            double high = Double.MIN_VALUE;
            double low = Double.MAX_VALUE;
            double close = series.getBar(endIdx).getClosePrice().doubleValue();

            for (int i = startIdx; i <= endIdx; i++) {
                double barHigh = series.getBar(i).getHighPrice().doubleValue();
                double barLow = series.getBar(i).getLowPrice().doubleValue();
                if (barHigh > high) high = barHigh;
                if (barLow < low) low = barLow;
            }

            double pivot = (high + low + close) / 3.0;
            double r1 = 2.0 * pivot - low;
            double s1 = 2.0 * pivot - high;

            return new PivotLevels(pivot, s1, r1);
        } catch (Exception e) {
            log.debug("Failed to calculate pivot levels at index {}: {}", index, e.getMessage());
            return null;
        }
    }

    /**
     * Calculates distance from previous period high.
     * Distance % = ((High - Current Close) / Current Close) * 100
     * Positive = Below high (price has room to go up)
     */
    private Double calculatePreviousPeriodHighDistance(BarSeries series, int index, double currentClose, int lookback) {
        if (index < lookback) {
            return null;
        }
        try {
            int startIdx = Math.max(0, index - lookback);
            int endIdx = index - 1;

            double high = Double.MIN_VALUE;
            for (int i = startIdx; i <= endIdx; i++) {
                double barHigh = series.getBar(i).getHighPrice().doubleValue();
                if (barHigh > high) high = barHigh;
            }

            return currentClose > 0.0 ? ((high - currentClose) / currentClose) * 100.0 : null;
        } catch (Exception e) {
            log.debug("Failed to calculate previous period high distance at index {}: {}", index, e.getMessage());
            return null;
        }
    }

    /**
     * Calculates distance from previous period low.
     * Distance % = ((Current Close - Low) / Current Close) * 100
     * Positive = Above low (price has moved up from low)
     */
    private Double calculatePreviousPeriodLowDistance(BarSeries series, int index, double currentClose, int lookback) {
        if (index < lookback) {
            return null;
        }
        try {
            int startIdx = Math.max(0, index - lookback);
            int endIdx = index - 1;

            double low = Double.MAX_VALUE;
            for (int i = startIdx; i <= endIdx; i++) {
                double barLow = series.getBar(i).getLowPrice().doubleValue();
                if (barLow < low) low = barLow;
            }

            return currentClose > 0.0 ? ((currentClose - low) / currentClose) * 100.0 : null;
        } catch (Exception e) {
            log.debug("Failed to calculate previous period low distance at index {}: {}", index, e.getMessage());
            return null;
        }
    }

    /**
     * Calculates distance from nearest round number.
     * Round numbers act as psychological support/resistance.
     * Distance % = ((Close - Nearest Round) / Close) * 100
     */
    private Double calculateRoundNumberDistance(double currentClose) {
        if (currentClose <= 0.0) {
            return null;
        }

        // Find appropriate round factor based on price magnitude
        double roundFactor = 1.0;
        for (double factor : ROUND_FACTORS) {
            if (currentClose >= factor) {
                roundFactor = factor;
            }
        }

        // Find nearest round number
        double nearestRound = Math.round(currentClose / roundFactor) * roundFactor;
        return ((currentClose - nearestRound) / currentClose) * 100.0;
    }

    /**
     * Calculates distance from VWAP (Volume Weighted Average Price).
     * VWAP = Sum(Price * Volume) / Sum(Volume)
     * Distance % = ((Close - VWAP) / Close) * 100
     */
    private Double calculateVWAPDistance(BarSeries series, int index, double currentClose) {
        if (index < VWAP_PERIOD - 1) {
            return null;
        }
        try {
            ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
            VolumeIndicator volume = new VolumeIndicator(series);

            double sumPriceVolume = 0.0;
            double sumVolume = 0.0;

            for (int i = index - VWAP_PERIOD + 1; i <= index; i++) {
                double price = closePrice.getValue(i).doubleValue();
                double vol = volume.getValue(i).doubleValue();
                sumPriceVolume += price * vol;
                sumVolume += vol;
            }

            if (sumVolume == 0.0) return null;

            double vwap = sumPriceVolume / sumVolume;
            return ((currentClose - vwap) / currentClose) * 100.0;
        } catch (Exception e) {
            log.debug("Failed to calculate VWAP distance at index {}: {}", index, e.getMessage());
            return null;
        }
    }

    private SupportResistanceIndicators createEmptyIndicators() {
        return new SupportResistanceIndicators(
                null, null, null, null, null, null, null, null
        );
    }

    /**
     * Helper record to hold pivot levels.
     */
    private record PivotLevels(double pivot, double s1, double r1) {
    }
}
