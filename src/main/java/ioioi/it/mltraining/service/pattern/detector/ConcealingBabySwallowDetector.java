package ioioi.it.mltraining.service.pattern.detector;

import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.service.pattern.PatternDetector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detector for Concealing Baby Swallow pattern.
 * Rare bullish reversal with:
 * - Four bearish candles
 * - Third and fourth form matching low
 * - Fourth engulfs third
 * - Indicates selling exhaustion
 */
@Component
public class ConcealingBabySwallowDetector implements PatternDetector {

    @Override
    public CandlestickPattern getPatternType() {
        return CandlestickPattern.CONCEALING_BABY_SWALLOW;
    }

    @Override
    public int getRequiredCandleCount() {
        return 4;
    }

    @Override
    public boolean detect(List<CandleRaw> candles) {
        if (candles == null || candles.size() < 4) {
            return false;
        }

        CandleRaw first = candles.get(candles.size() - 4);
        CandleRaw second = candles.get(candles.size() - 3);
        CandleRaw third = candles.get(candles.size() - 2);
        CandleRaw fourth = candles.get(candles.size() - 1);

        // All four must be bearish
        if (first.getOpen() <= first.getClose() ||
            second.getOpen() <= second.getClose() ||
            third.getOpen() <= third.getClose() ||
            fourth.getOpen() <= fourth.getClose()) {
            return false;
        }

        // Third should gap down from second
        if (third.getHigh() >= second.getLow()) {
            return false;
        }

        // Fourth should engulf third
        return fourth.getOpen() > third.getOpen() &&
               fourth.getClose() < third.getClose();
    }
}
