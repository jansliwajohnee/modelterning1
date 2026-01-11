package ioioi.it.mltraining.service.pattern.detector;

import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.service.pattern.PatternDetector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detector for Fakey pattern.
 * False breakout pattern with:
 * - First candle: mother bar
 * - Second candle: inside bar (false breakout setup)
 * - Third candle: breaks out opposite direction
 * - Indicates false breakout and reversal
 */
@Component
public class FakeyDetector implements PatternDetector {

    @Override
    public CandlestickPattern getPatternType() {
        return CandlestickPattern.FAKEY;
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

        // Second must be inside bar (inside first)
        if (second.getHigh() >= first.getHigh() ||
            second.getLow() <= first.getLow()) {
            return false;
        }

        // Third should break out in opposite direction
        // Bullish fakey: third breaks below first's low but closes above
        boolean bullishFakey = third.getLow() < first.getLow() &&
                               third.getClose() > first.getHigh();

        // Bearish fakey: third breaks above first's high but closes below
        boolean bearishFakey = third.getHigh() > first.getHigh() &&
                               third.getClose() < first.getLow();

        return bullishFakey || bearishFakey;
    }
}
