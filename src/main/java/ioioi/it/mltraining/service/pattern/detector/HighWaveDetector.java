package ioioi.it.mltraining.service.pattern.detector;

import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.service.pattern.PatternDetector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detector for High Wave pattern.
 * Extreme indecision pattern with:
 * - Small body (can be bullish or bearish)
 * - Very long upper and lower shadows
 * - Indicates extreme volatility and indecision
 * - Similar to Long-Legged Doji but can have visible body
 */
@Component
public class HighWaveDetector implements PatternDetector {

    @Override
    public CandlestickPattern getPatternType() {
        return CandlestickPattern.HIGH_WAVE;
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

        Double totalRange = high - low;
        if (totalRange == 0.0) {
            return false;
        }

        Double bodySize = Math.abs(open - close);
        Double upperShadow = high - Math.max(open, close);
        Double lowerShadow = Math.min(open, close) - low;

        // Body should be small (less than 15% of total range)
        Double bodyToRangeRatio = bodySize / totalRange;
        if (bodyToRangeRatio > 0.15) {
            return false;
        }

        // Both shadows should be very long (each at least 35% of total range)
        Double upperToRangeRatio = upperShadow / totalRange;
        Double lowerToRangeRatio = lowerShadow / totalRange;

        return upperToRangeRatio >= 0.35 &&
               lowerToRangeRatio >= 0.35;
    }
}
