package ioioi.it.mltraining.service.pattern.detector;

import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.service.pattern.PatternDetector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detector for Bearish Breakaway pattern.
 * Bearish reversal with:
 * - First candle: long bullish
 * - Second candle: bullish gaps up
 * - Third & fourth: small bullish continuing up
 * - Fifth: large bearish closes gap (into first candle's body)
 */
@Component
public class BreakawayBearishDetector implements PatternDetector {

    @Override
    public CandlestickPattern getPatternType() {
        return CandlestickPattern.BREAKAWAY_BEARISH;
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

        // First must be long bullish
        if (first.getClose() <= first.getOpen()) {
            return false;
        }

        // Second gaps up
        if (second.getLow() <= first.getHigh()) {
            return false;
        }

        // Fifth must be bearish and close in first's body
        if (fifth.getOpen() <= fifth.getClose()) {
            return false;
        }

        return fifth.getClose() > first.getOpen() &&
               fifth.getClose() < first.getClose();
    }
}
