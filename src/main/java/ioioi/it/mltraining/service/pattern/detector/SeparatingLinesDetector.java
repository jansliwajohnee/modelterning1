package ioioi.it.mltraining.service.pattern.detector;

import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.service.pattern.PatternDetector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detector for Separating Lines pattern.
 * Continuation pattern with:
 * - Two candles of opposite colors
 * - Both open at same level
 * - First bearish, second bullish (bullish continuation)
 * - OR first bullish, second bearish (bearish continuation)
 */
@Component
public class SeparatingLinesDetector implements PatternDetector {

    @Override
    public CandlestickPattern getPatternType() {
        return CandlestickPattern.SEPARATING_LINES;
    }

    @Override
    public int getRequiredCandleCount() {
        return 2;
    }

    @Override
    public boolean detect(List<CandleRaw> candles) {
        if (candles == null || candles.size() < 2) {
            return false;
        }

        CandleRaw first = candles.get(candles.size() - 2);
        CandleRaw second = candles.get(candles.size() - 1);

        boolean firstBullish = first.getClose() > first.getOpen();
        boolean secondBullish = second.getClose() > second.getOpen();

        // Candles must be opposite colors
        if (firstBullish == secondBullish) {
            return false;
        }

        // Opens should be at same level (within 0.2%)
        Double openDiff = Math.abs(first.getOpen() - second.getOpen());
        Double avgOpen = (first.getOpen() + second.getOpen()) / 2.0;
        Double diffRatio = openDiff / avgOpen;

        return diffRatio < 0.002;
    }
}
