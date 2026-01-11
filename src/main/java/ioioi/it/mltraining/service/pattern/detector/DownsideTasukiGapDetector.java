package ioioi.it.mltraining.service.pattern.detector;

import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.service.pattern.PatternDetector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detector for Downside Tasuki Gap pattern.
 * Bearish continuation with:
 * - First candle: bearish
 * - Second candle: bearish with gap down from first
 * - Third candle: bullish that fills gap but closes below first
 * - Gap holds as resistance
 */
@Component
public class DownsideTasukiGapDetector implements PatternDetector {

    @Override
    public CandlestickPattern getPatternType() {
        return CandlestickPattern.DOWNSIDE_TASUKI_GAP;
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

        // First must be bearish
        if (first.getOpen() <= first.getClose()) {
            return false;
        }

        // Second must be bearish with gap down
        if (second.getOpen() <= second.getClose()) {
            return false;
        }
        if (second.getHigh() >= first.getLow()) {
            return false;
        }

        // Third must be bullish
        if (third.getClose() <= third.getOpen()) {
            return false;
        }

        // Third opens within second's body
        if (third.getOpen() < second.getClose() ||
            third.getOpen() > second.getOpen()) {
            return false;
        }

        // Third closes in the gap (between first and second) but below first's close
        return third.getClose() < first.getClose() &&
               third.getClose() > second.getHigh();
    }
}
