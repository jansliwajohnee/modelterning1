package ioioi.it.mltraining.service.pattern.detector;

import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.service.pattern.PatternDetector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detector for Bullish Breakaway pattern.
 * Bullish reversal with:
 * - First candle: long bearish
 * - Second candle: bearish gaps down
 * - Third & fourth: small bearish continuing down
 * - Fifth: large bullish closes gap (into first candle's body)
 */
@Component
public class BreakawayBullishDetector implements PatternDetector {

    @Override
    public CandlestickPattern getPatternType() {
        return CandlestickPattern.BREAKAWAY_BULLISH;
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
        CandleRaw fifth = candles.get(candles.size() - 1);

        // First must be long bearish
        if (first.getOpen() <= first.getClose()) {
            return false;
        }

        // Second gaps down
        if (second.getHigh() >= first.getLow()) {
            return false;
        }

        // Fifth must be bullish and close in first's body
        if (fifth.getClose() <= fifth.getOpen()) {
            return false;
        }

        return fifth.getClose() > first.getClose() &&
               fifth.getClose() < first.getOpen();
    }
}
