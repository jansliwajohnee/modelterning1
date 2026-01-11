package ioioi.it.mltraining.service.pattern.detector;

import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.service.pattern.PatternDetector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detector for Bearish Spinning Top pattern.
 * Indecision pattern with:
 * - Small bearish body (open > close)
 * - Upper and lower shadows of similar length
 * - Shadows longer than body
 * - Indicates market indecision
 */
@Component
public class BearishSpinningTopDetector implements PatternDetector {

    @Override
    public CandlestickPattern getPatternType() {
        return CandlestickPattern.BEARISH_SPINNING_TOP;
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

        // Must be bearish
        if (open <= close) {
            return false;
        }

        Double bodySize = open - close;
        Double totalRange = high - low;

        if (totalRange == 0.0) {
            return false;
        }

        Double upperShadow = high - open;
        Double lowerShadow = close - low;

        // Body should be small (15-40% of total range)
        Double bodyToRangeRatio = bodySize / totalRange;
        if (bodyToRangeRatio < 0.15 ||
            bodyToRangeRatio > 0.40) {
            return false;
        }

        // Both shadows should be present and significant
        if (upperShadow < bodySize || lowerShadow < bodySize) {
            return false;
        }

        // Shadows should be relatively balanced (within 50% of each other)
        Double shadowRatio = upperShadow > lowerShadow ?
                lowerShadow / upperShadow :
                upperShadow / lowerShadow;

        return shadowRatio >= 0.50;
    }
}
