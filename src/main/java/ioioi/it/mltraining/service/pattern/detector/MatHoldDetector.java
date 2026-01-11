package ioioi.it.mltraining.service.pattern.detector;

import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.service.pattern.PatternDetector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detector for Mat Hold pattern.
 * Rare bullish continuation with:
 * - First candle: long bullish
 * - Second candle: small bullish gaps up
 * - Third and fourth: small bearish (consolidation)
 * - Fifth candle: bullish closes above first
 * - Similar to Rising Three Methods but starts with gap
 */
@Component
public class MatHoldDetector implements PatternDetector {

    @Override
    public CandlestickPattern getPatternType() {
        return CandlestickPattern.MAT_HOLD;
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

        // Second must be bullish and gap up
        if (second.getClose() <= second.getOpen()) {
            return false;
        }
        if (second.getLow() <= first.getHigh()) {
            return false;
        }

        // Third and fourth should be small bearish
        if (third.getOpen() <= third.getClose() ||
            fourth.getOpen() <= fourth.getClose()) {
            return false;
        }

        // Third and fourth should not fall below first's close
        if (third.getLow() < first.getClose() ||
            fourth.getLow() < first.getClose()) {
            return false;
        }

        // Fifth must be bullish and close above first
        return fifth.getClose() > fifth.getOpen() &&
               fifth.getClose() > first.getClose();
    }
}
