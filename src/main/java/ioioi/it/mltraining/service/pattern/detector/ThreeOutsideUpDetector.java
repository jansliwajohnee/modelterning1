package ioioi.it.mltraining.service.pattern.detector;

import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.service.pattern.PatternDetector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detector for Three Outside Up pattern.
 * Bullish reversal combining Bullish Engulfing + confirmation:
 * - First candle: bearish
 * - Second candle: bullish engulfing first
 * - Third candle: bullish closes above second's close
 */
@Component
public class ThreeOutsideUpDetector implements PatternDetector {

    @Override
    public CandlestickPattern getPatternType() {
        return CandlestickPattern.THREE_OUTSIDE_UP;
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

        // First must be bearish
        if (first.getOpen() <= first.getClose()) {
            return false;
        }

        // Second must be bullish and engulf first
        if (second.getClose() <= second.getOpen()) {
            return false;
        }
        if (second.getOpen() > first.getClose() ||
            second.getClose() < first.getOpen()) {
            return false;
        }

        // Third must be bullish and close above second
        return third.getClose() > third.getOpen() &&
               third.getClose() > second.getClose();
    }
}
