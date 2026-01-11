package ioioi.it.mltraining.service.pattern.detector;

import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.service.pattern.PatternDetector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detector for Rising Wedge pattern.
 * Bearish continuation/reversal with:
 * - Price making higher highs and higher lows
 * - But range is narrowing (converging trendlines)
 * - Typically requires 5+ candles
 * - Simplified detection: last 5 candles show narrowing range with higher lows
 */
@Component
public class RisingWedgeDetector implements PatternDetector {

    @Override
    public CandlestickPattern getPatternType() {
        return CandlestickPattern.RISING_WEDGE;
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

        // Price should be generally rising (higher lows)
        if (fifth.getLow() <= first.getLow()) {
            return false;
        }

        // Range should be narrowing
        var firstRange = first.getHigh() - first.getLow();
        var fifthRange = fifth.getHigh() - fifth.getLow();

        return fifthRange < firstRange * 0.7;
    }
}
