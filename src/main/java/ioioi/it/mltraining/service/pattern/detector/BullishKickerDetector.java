package ioioi.it.mltraining.service.pattern.detector;

import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.service.pattern.PatternDetector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detector for Bullish Kicker pattern.
 * Very strong bullish reversal with:
 * - First candle: bearish
 * - Second candle: bullish that opens with gap up
 * - Second opens at or above first's open
 * - Indicates strong sentiment shift
 */
@Component
public class BullishKickerDetector implements PatternDetector {

    @Override
    public CandlestickPattern getPatternType() {
        return CandlestickPattern.BULLISH_KICKER;
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

        // Second opens at or above first's open (gap up)
        return second.getOpen() >= first.getOpen();
    }
}
