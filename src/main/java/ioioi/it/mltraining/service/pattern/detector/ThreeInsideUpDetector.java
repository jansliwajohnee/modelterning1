package ioioi.it.mltraining.service.pattern.detector;

import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.service.pattern.PatternDetector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detector for Three Inside Up pattern.
 * Bullish reversal combining Bullish Harami + confirmation:
 * - First candle: large bearish
 * - Second candle: small bullish inside first's body (Bullish Harami)
 * - Third candle: bullish closes above first's high
 */
@Component
public class ThreeInsideUpDetector implements PatternDetector {

    @Override
    public CandlestickPattern getPatternType() {
        return CandlestickPattern.THREE_INSIDE_UP;
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

        // Second must be bullish and inside first
        if (second.getClose() <= second.getOpen()) {
            return false;
        }
        if (second.getOpen() < first.getClose() ||
            second.getClose() > first.getOpen()) {
            return false;
        }

        // Third must be bullish and close above first's high
        return third.getClose() > third.getOpen() &&
               third.getClose() > first.getHigh();
    }
}
