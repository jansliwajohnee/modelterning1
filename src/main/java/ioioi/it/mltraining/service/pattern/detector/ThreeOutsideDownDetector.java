package ioioi.it.mltraining.service.pattern.detector;

import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.service.pattern.PatternDetector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detector for Three Outside Down pattern.
 * Bearish reversal combining Bearish Engulfing + confirmation:
 * - First candle: bullish
 * - Second candle: bearish engulfing first
 * - Third candle: bearish closes below second's close
 */
@Component
public class ThreeOutsideDownDetector implements PatternDetector {

    @Override
    public CandlestickPattern getPatternType() {
        return CandlestickPattern.THREE_OUTSIDE_DOWN;
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

        // First must be bullish
        if (first.getClose() <= first.getOpen()) {
            return false;
        }

        // Second must be bearish and engulf first
        if (second.getOpen() <= second.getClose()) {
            return false;
        }
        if (second.getOpen() < first.getClose() ||
            second.getClose() > first.getOpen()) {
            return false;
        }

        // Third must be bearish and close below second
        return third.getOpen() > third.getClose() &&
               third.getClose() < second.getClose();
    }
}
