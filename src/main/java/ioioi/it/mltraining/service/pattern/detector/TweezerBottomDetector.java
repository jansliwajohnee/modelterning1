package ioioi.it.mltraining.service.pattern.detector;

import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.service.pattern.PatternDetector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detector for Tweezer Bottom pattern.
 * Bullish reversal with:
 * - Two candles with matching lows
 * - First candle: bearish
 * - Second candle: bullish
 * - Both lows are very close to each other
 * - Indicates support level
 */
@Component
public class TweezerBottomDetector implements PatternDetector {

    @Override
    public CandlestickPattern getPatternType() {
        return CandlestickPattern.TWEEZER_BOTTOM;
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

        // First candle should be bearish
        if (first.getOpen() <= first.getClose()) {
            return false;
        }

        // Second candle should be bullish
        if (second.getClose() <= second.getOpen()) {
            return false;
        }

        // Lows should be very close (within 0.2% of each other)
        Double lowDiff = Math.abs(first.getLow() - second.getLow());
        Double avgLow = (first.getLow() + second.getLow()) / 2.0;
        Double diffRatio = lowDiff / avgLow;

        return diffRatio < 0.002;
    }
}
