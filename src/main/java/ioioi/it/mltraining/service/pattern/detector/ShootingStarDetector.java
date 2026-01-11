package ioioi.it.mltraining.service.pattern.detector;

import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.service.pattern.PatternDetector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detector for Shooting Star pattern.
 * Bearish reversal pattern with:
 * - Small body at the bottom of the candle
 * - Long upper shadow (at least 2x body size)
 * - Little or no lower shadow
 * - Appears at top of uptrend
 */
@Component
public class ShootingStarDetector implements PatternDetector {

    @Override
    public CandlestickPattern getPatternType() {
        return CandlestickPattern.SHOOTING_STAR;
    }

    @Override
    public boolean detect(List<CandleRaw> candles) {
        if (candles == null || candles.isEmpty()) {
            return false;
        }

        CandleRaw candle = candles.get(candles.size() - 1);

        Double open = candle.getOpen();
        Double close = candle.getClose();
        Double high = candle.getHigh();
        Double low = candle.getLow();

        Double bodySize = Math.abs(open - close);
        Double upperShadow = high - Math.max(open, close);
        Double lowerShadow = Math.min(open, close) - low;

        if (bodySize == 0.0) {
            return false;
        }

        // Upper shadow should be at least 2x body size
        Double upperToBodyRatio = upperShadow / bodySize;
        if (upperToBodyRatio < 2.0) {
            return false;
        }

        // Lower shadow should be small (less than 50% of body)
        Double lowerToBodyRatio = lowerShadow / bodySize;
        if (lowerToBodyRatio > 0.5) {
            return false;
        }

        // Body should be in lower part of the candle range
        Double totalRange = high - low;
        Double bodyBottom = Math.min(open, close);
        Double bodyPositionFromBottom = bodyBottom - low;
        Double bodyPositionRatio = bodyPositionFromBottom / totalRange;

        return bodyPositionRatio < 0.3; // Body within bottom 30%
    }
}
