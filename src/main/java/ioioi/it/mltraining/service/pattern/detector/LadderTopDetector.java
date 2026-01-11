package ioioi.it.mltraining.service.pattern.detector;

import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.service.pattern.PatternDetector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detector for Ladder Top pattern.
 * Rare bearish reversal with:
 * - Three consecutive bullish candles with higher highs
 * - Fourth: bullish but with long upper shadow
 * - Fifth: bearish that closes below fourth's open
 * - Indicates strong reversal from overbought
 */
@Component
public class LadderTopDetector implements PatternDetector {

    @Override
    public CandlestickPattern getPatternType() {
        return CandlestickPattern.LADDER_TOP;
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
        CandleRaw second = candles.get(candles.size() - 4);
        CandleRaw third = candles.get(candles.size() - 3);
        CandleRaw fourth = candles.get(candles.size() - 2);
        CandleRaw fifth = candles.get(candles.size() - 1);

        // First three must be bullish with higher highs
        if (first.getClose() <= first.getOpen() ||
            second.getClose() <= second.getOpen() ||
            third.getClose() <= third.getOpen()) {
            return false;
        }

        if (second.getHigh() <= first.getHigh() ||
            third.getHigh() <= second.getHigh()) {
            return false;
        }

        // Fourth must be bullish
        if (fourth.getClose() <= fourth.getOpen()) {
            return false;
        }

        // Fourth should have long upper shadow
        var upperShadow = fourth.getHigh() - fourth.getClose();
        var body = fourth.getClose() - fourth.getOpen();
        if (upperShadow < body) {
            return false;
        }

        // Fifth must be bearish and close below fourth's open
        return fifth.getOpen() > fifth.getClose() &&
               fifth.getClose() < fourth.getOpen();
    }
}
