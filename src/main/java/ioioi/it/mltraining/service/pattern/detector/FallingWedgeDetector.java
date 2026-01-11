package ioioi.it.mltraining.service.pattern.detector;

import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.service.pattern.PatternDetector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detector for Falling Wedge pattern.
 * Bullish continuation/reversal with:
 * - Price making lower highs and lower lows
 * - But range is narrowing (converging trendlines)
 * - Typically requires 5+ candles
 * - Simplified detection: last 5 candles show narrowing range with lower highs
 */
@Component
public class FallingWedgeDetector implements PatternDetector {

    @Override
    public CandlestickPattern getPatternType() {
        return CandlestickPattern.FALLING_WEDGE;
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
        CandleRaw fifth = candles.get(candles.size() - 1);

        // Price should be generally falling (lower highs)
        if (fifth.getHigh() >= first.getHigh()) {
            return false;
        }

        // Range should be narrowing
        Double firstRange = first.getHigh() - first.getLow();
        Double fifthRange = fifth.getHigh() - fifth.getLow();

        return fifthRange < (firstRange * 0.7);
    }
}
