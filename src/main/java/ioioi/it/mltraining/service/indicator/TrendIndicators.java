package ioioi.it.mltraining.service.indicator;

/**
 * Record holding all calculated trend indicators for a single candle.
 * All values are normalized/scaled appropriately for LightGBM model training.
 * Trend indicators measure the direction and strength of price movements.
 *
 * @param closeEma8DistancePct distance from EMA(8) in percent
 * @param closeEma21DistancePct distance from EMA(21) in percent
 * @param closeEma50DistancePct distance from EMA(50) in percent
 * @param closeEma100DistancePct distance from EMA(100) in percent
 * @param closeEma200DistancePct distance from EMA(200) in percent
 * @param closeSma20DistancePct distance from SMA(20) in percent
 * @param closeSma50DistancePct distance from SMA(50) in percent
 * @param closeSma200DistancePct distance from SMA(200) in percent
 * @param closeVwmaDistancePct distance from VWMA in percent
 * @param closeHmaDistancePct distance from HMA in percent
 * @param ema8Ema21SpreadPct spread between EMA(8) and EMA(21) in percent
 * @param ema21Ema50SpreadPct spread between EMA(21) and EMA(50) in percent
 * @param ema50Ema200SpreadPct spread between EMA(50) and EMA(200) in percent
 * @param adx Average Directional Index (0-100)
 * @param plusDi Plus Directional Indicator (0-100)
 * @param minusDi Minus Directional Indicator (0-100)
 * @param diSpreadNormalized normalized spread between +DI and -DI
 * @param aroonUp Aroon Up (0-100)
 * @param aroonDown Aroon Down (0-100)
 * @param aroonOscillator Aroon Oscillator (-100 to 100)
 * @param linearRegressionSlopePct linear regression slope in percent
 * @param maSlopePct moving average slope in percent
 */
public record TrendIndicators(
        Double closeEma8DistancePct,
        Double closeEma21DistancePct,
        Double closeEma50DistancePct,
        Double closeEma100DistancePct,
        Double closeEma200DistancePct,
        Double closeSma20DistancePct,
        Double closeSma50DistancePct,
        Double closeSma200DistancePct,
        Double closeVwmaDistancePct,
        Double closeHmaDistancePct,
        Double ema8Ema21SpreadPct,
        Double ema21Ema50SpreadPct,
        Double ema50Ema200SpreadPct,
        Double adx,
        Double plusDi,
        Double minusDi,
        Double diSpreadNormalized,
        Double aroonUp,
        Double aroonDown,
        Double aroonOscillator,
        Double linearRegressionSlopePct,
        Double maSlopePct
) {
}
