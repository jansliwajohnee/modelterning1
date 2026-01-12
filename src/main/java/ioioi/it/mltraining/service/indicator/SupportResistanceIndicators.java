package ioioi.it.mltraining.service.indicator;

/**
 * Record holding all calculated support/resistance indicators for a single candle.
 * All values are normalized/scaled appropriately for LightGBM model training.
 * Support/Resistance indicators measure price levels that may act as barriers.
 *
 * @param closePivotDistancePct distance from pivot point as percentage
 * @param closeS1DistancePct distance from support level 1 as percentage
 * @param closeR1DistancePct distance from resistance level 1 as percentage
 * @param closePrevDayHighPct distance from previous day high as percentage
 * @param closePrevDayLowPct distance from previous day low as percentage
 * @param closePrevWeekHighPct distance from previous week high as percentage
 * @param closeRoundNumberDistancePct distance from nearest round number as percentage
 * @param closeVwapDistancePct distance from VWAP as percentage
 */
public record SupportResistanceIndicators(
        Double closePivotDistancePct,
        Double closeS1DistancePct,
        Double closeR1DistancePct,
        Double closePrevDayHighPct,
        Double closePrevDayLowPct,
        Double closePrevWeekHighPct,
        Double closeRoundNumberDistancePct,
        Double closeVwapDistancePct
) {
}
