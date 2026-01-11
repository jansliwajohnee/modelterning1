package ioioi.it.mltraining.service.pattern.detector;

import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.service.pattern.PatternDetector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detector for Falling Three Methods pattern.
 * Bearish continuation with:
 * - First candle: long bearish
 * - Next 3 candles: small bullish (consolidation) within first's range
 * - Fifth candle: long bearish closes below first
 * - Indicates temporary bounce before continuation
 */
@Component
public class FallingThreeMethodsDetector implements PatternDetector {

    @Override
    public CandlestickPattern getPatternType() {
        return CandlestickPattern.FALLING_THREE_METHODS;
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

        // First must be long bearish
        if (first.getOpen() <= first.getClose()) {
            return false;
        }

        // Middle three should be bullish and small
        if (second.getClose() <= second.getOpen() ||
            third.getClose() <= third.getOpen() ||
            fourth.getClose() <= fourth.getOpen()) {
            return false;
        }

        // Middle three should stay within first's range
        for (CandleRaw candle : List.of(second, third, fourth)) {
            if (candle.getHigh() > first.getHigh() ||
                candle.getLow() < first.getLow()) {
                return false;
            }
        }

        // Fifth must be bearish and close below first
        return fifth.getOpen() > fifth.getClose() &&
               fifth.getClose() < first.getClose();
    }
}
