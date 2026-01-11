package ioioi.it.mltraining.service.pattern.detector;

import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.service.pattern.PatternDetector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detector for Bullish Harami pattern.
 * Bullish reversal with:
 * - First candle: large bearish (mother)
 * - Second candle: small bullish (baby) contained within first's body
 * - Second's body completely inside first's body
 * - Indicates potential reversal
 */
@Component
public class BullishHaramiDetector implements PatternDetector {

    @Override
    public CandlestickPattern getPatternType() {
        return CandlestickPattern.BULLISH_HARAMI;
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

        // Second candle's body must be completely inside first candle's body
        // Second open >= first close AND second close <= first open
        return second.getOpen() >= first.getClose() &&
               second.getClose() <= first.getOpen();
    }
}
