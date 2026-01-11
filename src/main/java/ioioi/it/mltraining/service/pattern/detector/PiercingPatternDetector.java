package ioioi.it.mltraining.service.pattern.detector;

import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.service.pattern.PatternDetector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detector for Piercing Pattern.
 * Bullish reversal with:
 * - First candle: bearish (red)
 * - Second candle: bullish (green) that opens below first's low
 * - Second closes above midpoint of first candle's body
 * - Appears at bottom of downtrend
 */
@Component
public class PiercingPatternDetector implements PatternDetector {

    @Override
    public CandlestickPattern getPatternType() {
        return CandlestickPattern.PIERCING_PATTERN;
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

        // First candle must be bearish
        if (first.getOpen() <= first.getClose()) {
            return false;
        }

        // Second candle must be bullish
        if (second.getClose() <= second.getOpen()) {
            return false;
        }

        // Second opens below first's low (gap down)
        if (second.getOpen() >= first.getLow()) {
            return false;
        }

        // Calculate midpoint of first candle's body
        Double firstMidpoint = (first.getOpen() + first.getClose()) / 2.0;

        // Second close must be above midpoint of first's body
        return second.getClose() > firstMidpoint &&
               second.getClose() < first.getOpen(); // But not fully engulfing
    }
}
