package ioioi.it.mltraining.service.indicator;

/**
 * Record holding all calculated price action indicators for a single candle.
 * All values are normalized/scaled appropriately for LightGBM model training.
 * Price action indicators measure candle patterns, gaps, and price momentum.
 *
 * @param bodyRangeRatio body as ratio of full candle range (0-1)
 * @param upperWickRangeRatio upper wick as ratio of full range (0-1)
 * @param lowerWickRangeRatio lower wick as ratio of full range (0-1)
 * @param closePositionInRange close position in candle range (0-1)
 * @param bodyPct body size as percentage of price
 * @param gapPct gap percentage from previous close
 * @param isBullish true if close >= open (bullish candle)
 * @param return1 1-bar forward return percentage
 * @param return3 3-bar forward return percentage
 * @param return5 5-bar forward return percentage
 * @param return10 10-bar forward return percentage
 * @param return20 20-bar forward return percentage
 * @param return50 50-bar forward return percentage
 * @param highestHighDistancePct distance from highest high in period (%)
 * @param lowestLowDistancePct distance from lowest low in period (%)
 * @param positionInRangeN position in N-period range (0-1)
 * @param consecutiveBullishRatio ratio of consecutive bullish candles
 * @param consecutiveBearishRatio ratio of consecutive bearish candles
 * @param higherHighsRatio ratio of higher highs in lookback period
 * @param higherClosesRatio ratio of higher closes in lookback period
 */
public record PriceActionIndicators(
        Double bodyRangeRatio,
        Double upperWickRangeRatio,
        Double lowerWickRangeRatio,
        Double closePositionInRange,
        Double bodyPct,
        Double gapPct,
        Boolean isBullish,
        Double return1,
        Double return3,
        Double return5,
        Double return10,
        Double return20,
        Double return50,
        Double highestHighDistancePct,
        Double lowestLowDistancePct,
        Double positionInRangeN,
        Double consecutiveBullishRatio,
        Double consecutiveBearishRatio,
        Double higherHighsRatio,
        Double higherClosesRatio
) {
}
