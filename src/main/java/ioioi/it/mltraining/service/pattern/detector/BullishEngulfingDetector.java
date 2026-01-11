package ioioi.it.mltraining.service.pattern.detector;

import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.service.pattern.PatternDetector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detector for Bullish Engulfing pattern.
 * Strong bullish reversal with:
 * - First candle: bearish (red)
 * - Second candle: bullish (green) that completely engulfs first candle's body
 * - Second open < first close, second close > first open
 * - Appears at bottom of downtrend
 */
@Component
public class BullishEngulfingDetector implements PatternDetector {

    @Override
    public CandlestickPattern getPatternType() {
        return CandlestickPattern.BULLISH_ENGULFING;
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

        // Second candle must engulf first candle's body
        // Second open <= first close AND second close >= first open
        return second.getOpen() <= first.getClose() &&
                second.getClose() >= first.getOpen();
    }
}
