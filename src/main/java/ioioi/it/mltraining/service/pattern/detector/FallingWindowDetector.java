package ioioi.it.mltraining.service.pattern.detector;

import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.service.pattern.PatternDetector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detector for Falling Window pattern.
 * Bearish continuation with:
 * - Gap between two candles
 * - Second candle's high < first candle's low
 * - Both candles typically bearish
 * - Indicates strong bearish momentum
 */
@Component
public class FallingWindowDetector implements PatternDetector {

    @Override
    public CandlestickPattern getPatternType() {
        return CandlestickPattern.FALLING_WINDOW;
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

        // Gap down: second's high < first's low
        return second.getHigh() < first.getLow();
    }
}
