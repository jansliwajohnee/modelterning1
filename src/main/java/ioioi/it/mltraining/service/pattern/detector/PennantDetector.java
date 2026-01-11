package ioioi.it.mltraining.service.pattern.detector;

import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.service.pattern.PatternDetector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detector for Pennant pattern.
 * Continuation pattern with:
 * - Small consolidation after strong move
 * - Symmetrical triangle-like formation
 * - Converging price action
 * - Typically requires 5+ candles
 * - Simplified: narrowing range with decreasing volatility
 */
@Component
public class PennantDetector implements PatternDetector {

    @Override
    public CandlestickPattern getPatternType() {
        return CandlestickPattern.PENNANT;
    }

    @Override
    public int getRequiredCandleCount() {
        return 5;
    }

    @Override
    public boolean detect(List<CandleRaw> candles) {
        if (candles == null || candles.size() < 5) {
            return false;
        }

        CandleRaw first = candles.get(candles.size() - 5);
        CandleRaw third = candles.get(candles.size() - 3);
        CandleRaw fifth = candles.get(candles.size() - 1);

        // Calculate ranges
        Double firstRange = first.getHigh() - first.getLow();
        Double thirdRange = third.getHigh() - third.getLow();
        Double fifthRange = fifth.getHigh() - fifth.getLow();

        if (firstRange == 0.0) {
            return false;
        }

        // Range should be progressively narrowing
        return thirdRange < firstRange * 0.8 &&
               fifthRange < thirdRange * 0.8;
    }
}
