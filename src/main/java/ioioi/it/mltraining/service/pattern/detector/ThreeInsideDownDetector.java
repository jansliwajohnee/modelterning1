package ioioi.it.mltraining.service.pattern.detector;

import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.service.pattern.PatternDetector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detector for Three Inside Down pattern.
 * Bearish reversal combining Bearish Harami + confirmation:
 * - First candle: large bullish
 * - Second candle: small bearish inside first's body (Bearish Harami)
 * - Third candle: bearish closes below first's low
 */
@Component
public class ThreeInsideDownDetector implements PatternDetector {

    @Override
    public CandlestickPattern getPatternType() {
        return CandlestickPattern.THREE_INSIDE_DOWN;
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

        // Second must be bearish and inside first
        if (second.getOpen() <= second.getClose()) {
            return false;
        }
        if (second.getOpen() > first.getClose() ||
            second.getClose() < first.getOpen()) {
            return false;
        }

        // Third must be bearish and close below first's low
        return third.getOpen() > third.getClose() &&
               third.getClose() < first.getLow();
    }
}
