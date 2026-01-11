package ioioi.it.mltraining.service.pattern.detector;

import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.service.pattern.PatternDetector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detector for Doji pattern.
 * Indecision pattern with:
 * - Open equals close (or very close)
 * - Can have upper and lower shadows of any length
 * - Indicates market indecision
 */
@Component
public class DojiDetector implements PatternDetector {

    @Override
    public CandlestickPattern getPatternType() {
        return CandlestickPattern.DOJI;
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

        // Body should be very small (less than 5% of total range)
        Double bodyToRangeRatio = bodySize / totalRange;
        return bodyToRangeRatio < 0.05;
    }
}
