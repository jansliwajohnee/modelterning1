package ioioi.it.mltraining.service.pattern.detector;

import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.service.pattern.PatternDetector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detector for Hammer pattern using Java 17 features.
 *
 * <p>Bullish reversal pattern characteristics:
 * <ul>
 *   <li>Small body at the top of the candle</li>
 *   <li>Long lower shadow (at least 2x body size)</li>
 *   <li>Little or no upper shadow (less than 50% of body)</li>
 *   <li>Body within top 30% of candle range</li>
 *   <li>Appears at bottom of downtrend</li>
 * </ul>
 *
 * <p>Uses record pattern for cleaner price data extraction
 * and functional validation approach.
 */
@Component
public class HammerDetector implements PatternDetector {

    private static final double LOWER_SHADOW_MIN_RATIO = 2.0;
    private static final double UPPER_SHADOW_MAX_RATIO = 0.5;
    private static final double BODY_POSITION_MAX_RATIO = 0.3;

    /**
     * Immutable candle metrics for pattern analysis.
     * Uses Java 17 record for concise, type-safe representation.
     *
     * @param bodySize Absolute difference between open and close
     * @param upperShadow Distance from body top to high
     * @param lowerShadow Distance from low to body bottom
     * @param totalRange Full candle range (high - low)
     * @param bodyTop Higher value between open and close
     */
    private record CandleMetrics(
            double bodySize,
            double upperShadow,
            double lowerShadow,
            double totalRange,
            double bodyTop
    ) {
        /**
         * Calculates metrics from candle data.
         *
         * @param candle The candle to analyze
         * @return calculated metrics, or null if candle data is invalid
         */
        static CandleMetrics from(CandleRaw candle) {
            Double open = candle.getOpen();
            Double close = candle.getClose();
            Double high = candle.getHigh();
            Double low = candle.getLow();

            // Guard clause: validate all prices present
            if (open == null || close == null || high == null || low == null) {
                return null;
            }

            double bodySize = Math.abs(close - open);
            double bodyTop = Math.max(open, close);
            double bodyBottom = Math.min(open, close);

            return new CandleMetrics(
                    bodySize,
                    high - bodyTop,
                    bodyBottom - low,
                    high - low,
                    bodyTop
            );
        }

        boolean hasValidBody() {
            return bodySize > 0.0 && totalRange > 0.0;
        }

        double lowerToBodyRatio() {
            return lowerShadow / bodySize;
        }

        double upperToBodyRatio() {
            return upperShadow / bodySize;
        }

        double bodyPositionRatio() {
            return (totalRange - bodyTop) / totalRange;
        }
    }

    @Override
    public CandlestickPattern getPatternType() {
        return CandlestickPattern.HAMMER;
    }

    @Override
    public boolean detect(List<CandleRaw> candles) {
        // Guard clause: validate input
        if (candles == null || candles.isEmpty()) {
            return false;
        }

        CandleRaw currentCandle = candles.getLast(); // Java 21 feature (or candles.get(candles.size() - 1))
        CandleMetrics metrics = CandleMetrics.from(currentCandle);

        // Guard clause: validate metrics
        if (metrics == null || !metrics.hasValidBody()) {
            return false;
        }

        // Use functional approach with method references
        return hasLongLowerShadow(metrics)
                && hasSmallUpperShadow(metrics)
                && isBodyAtTop(metrics);
    }

    /**
     * Validates lower shadow length requirement.
     * Lower shadow must be at least 2x the body size.
     *
     * @param metrics Candle metrics
     * @return true if lower shadow is sufficiently long
     */
    private boolean hasLongLowerShadow(CandleMetrics metrics) {
        return metrics.lowerToBodyRatio() >= LOWER_SHADOW_MIN_RATIO;
    }

    /**
     * Validates upper shadow length requirement.
     * Upper shadow must be small (less than 50% of body).
     *
     * @param metrics Candle metrics
     * @return true if upper shadow is sufficiently small
     */
    private boolean hasSmallUpperShadow(CandleMetrics metrics) {
        return metrics.upperToBodyRatio() <= UPPER_SHADOW_MAX_RATIO;
    }

    /**
     * Validates body position requirement.
     * Body must be in the upper 30% of the candle range.
     *
     * @param metrics Candle metrics
     * @return true if body is positioned at top
     */
    private boolean isBodyAtTop(CandleMetrics metrics) {
        return metrics.bodyPositionRatio() <= BODY_POSITION_MAX_RATIO;
    }
}
