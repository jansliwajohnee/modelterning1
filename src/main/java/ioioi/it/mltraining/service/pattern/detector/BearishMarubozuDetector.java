package ioioi.it.mltraining.service.pattern.detector;

import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.service.pattern.PatternDetector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detector for Bearish Marubozu pattern.
 * Strong bearish pattern with:
 * - Long bearish body (open > close)
 * - Little or no shadows on either side
 * - Open near high, close near low
 * - Indicates strong selling pressure
 */
@Component
public class BearishMarubozuDetector implements PatternDetector {

    @Override
    public CandlestickPattern getPatternType() {
        return CandlestickPattern.BEARISH_MARUBOZU;
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

        // Body should be at least 80% of total range
        Double bodyToRangeRatio = bodySize / totalRange;
        if (bodyToRangeRatio < 0.80) {
            return false;
        }

        // Both shadows should be very small (each less than 10% of total range)
        Double upperToRangeRatio = upperShadow / totalRange;
        Double lowerToRangeRatio = lowerShadow / totalRange;

        return upperToRangeRatio < 0.10 &&
               lowerToRangeRatio < 0.10;
    }
}
