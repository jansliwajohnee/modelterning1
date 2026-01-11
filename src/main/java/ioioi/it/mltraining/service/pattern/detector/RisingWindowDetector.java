package ioioi.it.mltraining.service.pattern.detector;

import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.service.pattern.PatternDetector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detector for Rising Window pattern.
 * Bullish continuation with:
 * - Gap between two candles
 * - Second candle's low > first candle's high
 * - Both candles typically bullish
 * - Indicates strong bullish momentum
 */
@Component
public class RisingWindowDetector implements PatternDetector {

    @Override
    public CandlestickPattern getPatternType() {
        return CandlestickPattern.RISING_WINDOW;
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

        // Gap up: second's low > first's high
        return second.getLow() > first.getHigh();
    }
}
