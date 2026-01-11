package ioioi.it.mltraining.service.pattern.detector;

import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.service.pattern.PatternDetector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detector for Unique Three River Bottom pattern.
 * Rare bullish reversal with:
 * - First candle: long bearish
 * - Second candle: bearish hammer (long lower shadow)
 * - Third candle: small bullish that closes below second's close
 * - Indicates potential bottom
 */
@Component
public class UniqueThreeRiverBottomDetector implements PatternDetector {

    @Override
    public CandlestickPattern getPatternType() {
        return CandlestickPattern.UNIQUE_THREE_RIVER_BOTTOM;
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

        // First must be long bearish
        if (first.getOpen() <= first.getClose()) {
            return false;
        }

        // Second must be bearish with long lower shadow (hammer-like)
        if (second.getOpen() <= second.getClose()) {
            return false;
        }
        // Check for long lower shadow
        var lowerShadow = second.getClose() - second.getLow();
        var body = second.getOpen() - second.getClose();
        if (lowerShadow < body * 2.0) {
            return false;
        }

        // Third must be small bullish
        if (third.getClose() <= third.getOpen()) {
            return false;
        }

        // Third closes below second's close
        return third.getClose() < second.getClose();
    }
}
