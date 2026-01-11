package ioioi.it.mltraining.service.pattern.detector;

import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.service.pattern.PatternDetector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detector for Rising Three Methods pattern.
 * Bullish continuation with:
 * - First candle: long bullish
 * - Next 3 candles: small bearish (consolidation) within first's range
 * - Fifth candle: long bullish closes above first
 * - Indicates temporary pullback before continuation
 */
@Component
public class RisingThreeMethodsDetector implements PatternDetector {

    @Override
    public CandlestickPattern getPatternType() {
        return CandlestickPattern.RISING_THREE_METHODS;
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

        // First must be long bullish
        if (first.getClose() <= first.getOpen()) {
            return false;
        }

        // Middle three should be bearish and small
        if (second.getOpen() <= second.getClose() ||
            third.getOpen() <= third.getClose() ||
            fourth.getOpen() <= fourth.getClose()) {
            return false;
        }

        // Middle three should stay within first's range
        for (CandleRaw candle : List.of(second, third, fourth)) {
            if (candle.getHigh() > first.getHigh() ||
                candle.getLow() < first.getLow()) {
                return false;
            }
        }

        // Fifth must be bullish and close above first
        return fifth.getClose() > fifth.getOpen() &&
               fifth.getClose() > first.getClose();
    }
}
