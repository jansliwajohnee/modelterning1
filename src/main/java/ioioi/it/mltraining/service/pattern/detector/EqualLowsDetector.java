package ioioi.it.mltraining.service.pattern.detector;

import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.service.pattern.PatternDetector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detector for Equal Lows pattern.
 * Support pattern with:
 * - Two or more candles with matching lows
 * - Lows are within 0.2% of each other
 * - Indicates support level
 */
@Component
public class EqualLowsDetector implements PatternDetector {

    @Override
    public CandlestickPattern getPatternType() {
        return CandlestickPattern.EQUAL_LOWS;
    }

    @Override
    public int getRequiredCandleCount() {
        return 2;
    }

    @Override
    public boolean detect(List<CandleRaw> candles) {
        if (candles == null || candles.size() < 2) {
            return false;
        }

        CandleRaw previous = candles.get(candles.size() - 2);
        CandleRaw current = candles.get(candles.size() - 1);

        // Lows should be very close (within 0.2%)
        Double lowDiff = Math.abs(previous.getLow() - current.getLow());
        Double avgLow = (previous.getLow() + current.getLow()) / 2.0;

        if (avgLow == 0.0) {
            return false;
        }

        Double diffRatio = lowDiff / avgLow;

        return diffRatio < 0.002;
    }
}
