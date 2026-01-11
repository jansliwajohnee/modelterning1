package ioioi.it.mltraining.service.pattern.detector;

import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.service.pattern.PatternDetector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detector for Bearish Kicker pattern.
 * Very strong bearish reversal with:
 * - First candle: bullish
 * - Second candle: bearish that opens with gap down
 * - Second opens at or below first's open
 * - Indicates strong sentiment shift
 */
@Component
public class BearishKickerDetector implements PatternDetector {

    @Override
    public CandlestickPattern getPatternType() {
        return CandlestickPattern.BEARISH_KICKER;
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

        // Second opens at or below first's open (gap down)
        return second.getOpen() <= first.getOpen();
    }
}
