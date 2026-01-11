package ioioi.it.mltraining.service.pattern.detector;

import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.service.pattern.PatternDetector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detector for Dragonfly Doji pattern.
 * Bullish reversal pattern with:
 * - Open equals close (or very close)
 * - Long lower shadow
 * - No (or very small) upper shadow
 * - T-shaped appearance
 */
@Component
public class DragonflyDojiDetector implements PatternDetector {

    @Override
    public CandlestickPattern getPatternType() {
        return CandlestickPattern.DRAGONFLY_DOJI;
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

        // Body should be very small (less than 5% of total range) - doji characteristic
        Double bodyToRangeRatio = bodySize / totalRange;
        if (bodyToRangeRatio > 0.05) {
            return false;
        }

        // Upper shadow should be very small (less than 10% of total range)
        Double upperToRangeRatio = upperShadow / totalRange;
        if (upperToRangeRatio > 0.10) {
            return false;
        }

        // Lower shadow should be significant (at least 60% of total range)
        Double lowerToRangeRatio = lowerShadow / totalRange;
        return lowerToRangeRatio >= 0.60;
    }
}
