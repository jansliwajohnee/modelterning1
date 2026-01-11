package ioioi.it.mltraining.service.pattern.detector;

import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.service.pattern.PatternDetector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detector for Ladder Bottom pattern.
 * Rare bullish reversal with:
 * - Three consecutive bearish candles with lower lows
 * - Fourth: bearish but with long lower shadow
 * - Fifth: bullish that closes above fourth's open
 * - Indicates strong reversal from oversold
 */
@Component
public class LadderBottomDetector implements PatternDetector {

    @Override
    public CandlestickPattern getPatternType() {
        return CandlestickPattern.LADDER_BOTTOM;
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

        // First three must be bearish with lower lows
        if (first.getOpen() <= first.getClose() ||
            second.getOpen() <= second.getClose() ||
            third.getOpen() <= third.getClose()) {
            return false;
        }

        if (second.getLow() >= first.getLow() ||
            third.getLow() >= second.getLow()) {
            return false;
        }

        // Fourth must be bearish
        if (fourth.getOpen() <= fourth.getClose()) {
            return false;
        }

        // Fourth should have long lower shadow
        var lowerShadow = fourth.getClose() - fourth.getLow();
        var body = fourth.getOpen() - fourth.getClose();
        if (lowerShadow < body) {
            return false;
        }

        // Fifth must be bullish and close above fourth's open
        return fifth.getClose() > fifth.getOpen() &&
               fifth.getClose() > fourth.getOpen();
    }
}
