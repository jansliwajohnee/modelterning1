package ioioi.it.mltraining.service.pattern.detector;

import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.service.pattern.PatternDetector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detector for Three Stars In The South pattern.
 * Rare bullish reversal with:
 * - Three consecutive bearish candles
 * - Each has lower shadow
 * - Each progressively smaller
 * - Third has smallest range and shadows
 * - Indicates selling exhaustion
 */
@Component
public class ThreeStarsInTheSouthDetector implements PatternDetector {

    @Override
    public CandlestickPattern getPatternType() {
        return CandlestickPattern.THREE_STARS_IN_THE_SOUTH;
    }

    @Override
    public int getRequiredCandleCount() {
        return 3;
    }

    @Override
    public boolean detect(List<CandleRaw> candles) {
        if (candles == null || candles.size() < 3) {
            return false;
        }

        CandleRaw first = candles.get(candles.size() - 3);
        CandleRaw second = candles.get(candles.size() - 2);
        CandleRaw third = candles.get(candles.size() - 1);

        // All three must be bearish
        if (first.getOpen() <= first.getClose() ||
            second.getOpen() <= second.getClose() ||
            third.getOpen() <= third.getClose()) {
            return false;
        }

        // Calculate ranges
        Double firstRange = first.getHigh() - first.getLow();
        Double secondRange = second.getHigh() - second.getLow();
        Double thirdRange = third.getHigh() - third.getLow();

        // Each progressively smaller
        if (secondRange >= firstRange ||
            thirdRange >= secondRange) {
            return false;
        }

        // All must have lower shadows
        Double firstLowerShadow = first.getClose() - first.getLow();
        Double secondLowerShadow = second.getClose() - second.getLow();
        Double thirdLowerShadow = third.getClose() - third.getLow();

        return firstLowerShadow > 0.0 &&
               secondLowerShadow > 0.0 &&
               thirdLowerShadow > 0.0;
    }
}
