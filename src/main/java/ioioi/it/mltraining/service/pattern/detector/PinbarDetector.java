package ioioi.it.mltraining.service.pattern.detector;

import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.service.pattern.PatternDetector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detector for Pinbar (Pin Bar) pattern.
 * Reversal pattern with:
 * - Small body (less than 25% of range)
 * - Long wick/tail (at least 2/3 of range) on one side
 * - Very small or no wick on opposite side
 * - Indicates rejection of price level
 */
@Component
public class PinbarDetector implements PatternDetector {

    @Override
    public CandlestickPattern getPatternType() {
        return CandlestickPattern.PINBAR;
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

        // Body should be small (less than 25% of range)
        Double bodyRatio = bodySize / totalRange;
        if (bodyRatio > 0.25) {
            return false;
        }

        // One shadow should be long (at least 66% of range)
        Double upperRatio = upperShadow / totalRange;
        Double lowerRatio = lowerShadow / totalRange;

        return upperRatio >= 0.66 || lowerRatio >= 0.66;
    }
}
