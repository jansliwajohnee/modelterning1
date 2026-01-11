package ioioi.it.mltraining.service.pattern.detector;

import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.service.pattern.PatternDetector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detector for Hanging Man pattern.
 * Bearish reversal pattern with:
 * - Small body at the top of the candle
 * - Long lower shadow (at least 2x body size)
 * - Little or no upper shadow
 * - Appears at top of uptrend
 * - Very similar to Hammer but in different context
 */
@Component
public class HangingManDetector implements PatternDetector {

    @Override
    public CandlestickPattern getPatternType() {
        return CandlestickPattern.HANGING_MAN;
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

        // Lower shadow should be at least 2x body size
        Double lowerToBodyRatio = lowerShadow / bodySize;
        if (lowerToBodyRatio < 2.0) {
            return false;
        }

        // Upper shadow should be small (less than 50% of body)
        Double upperToBodyRatio = upperShadow / bodySize;
        if (upperToBodyRatio > 0.5) {
            return false;
        }

        // Body should be in upper part of the candle range
        Double totalRange = high - low;
        Double bodyTop = Math.max(open, close);
        Double bodyPositionFromTop = high - bodyTop;
        Double bodyPositionRatio = bodyPositionFromTop / totalRange;

        return bodyPositionRatio < 0.3; // Body within top 30%
    }
}
