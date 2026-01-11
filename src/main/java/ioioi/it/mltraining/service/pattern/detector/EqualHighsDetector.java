package ioioi.it.mltraining.service.pattern.detector;

import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.service.pattern.PatternDetector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detector for Equal Highs pattern.
 * Resistance pattern with:
 * - Two or more candles with matching highs
 * - Highs are within 0.2% of each other
 * - Indicates resistance level
 */
@Component
public class EqualHighsDetector implements PatternDetector {

    @Override
    public CandlestickPattern getPatternType() {
        return CandlestickPattern.EQUAL_HIGHS;
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

        // Highs should be very close (within 0.2%)
        Double highDiff = Math.abs(previous.getHigh() - current.getHigh());
        Double avgHigh = (previous.getHigh() + current.getHigh()) / 2.0;

        if (avgHigh == 0.0) {
            return false;
        }

        Double diffRatio = highDiff / avgHigh;

        return diffRatio < 0.002;
    }
}
