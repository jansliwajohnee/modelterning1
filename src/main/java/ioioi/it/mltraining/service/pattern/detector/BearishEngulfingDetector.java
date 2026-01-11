package ioioi.it.mltraining.service.pattern.detector;

import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.service.pattern.PatternDetector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detector for Bearish Engulfing pattern.
 * Strong bearish reversal with:
 * - First candle: bullish (green)
 * - Second candle: bearish (red) that completely engulfs first candle's body
 * - Second open > first close, second close < first open
 * - Appears at top of uptrend
 */
@Component
public class BearishEngulfingDetector implements PatternDetector {

    @Override
    public CandlestickPattern getPatternType() {
        return CandlestickPattern.BEARISH_ENGULFING;
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

        // First candle must be bullish
        if (first.getClose() <= first.getOpen()) {
            return false;
        }

        // Second candle must be bearish
        if (second.getOpen() <= second.getClose()) {
            return false;
        }

        // Second candle must engulf first candle's body
        // Second open >= first close AND second close <= first open
        return second.getOpen() >= first.getClose() &&
               second.getClose() <= first.getOpen();
    }
}
