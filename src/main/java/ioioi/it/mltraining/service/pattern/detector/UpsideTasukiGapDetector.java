package ioioi.it.mltraining.service.pattern.detector;

import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.service.pattern.PatternDetector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detector for Upside Tasuki Gap pattern.
 * Bullish continuation with:
 * - First candle: bullish
 * - Second candle: bullish with gap up from first
 * - Third candle: bearish that fills gap but closes above first
 * - Gap holds as support
 */
@Component
public class UpsideTasukiGapDetector implements PatternDetector {

    @Override
    public CandlestickPattern getPatternType() {
        return CandlestickPattern.UPSIDE_TASUKI_GAP;
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

        // First must be bullish
        if (first.getClose() <= first.getOpen()) {
            return false;
        }

        // Second must be bullish with gap up
        if (second.getClose() <= second.getOpen()) {
            return false;
        }
        if (second.getLow() <= first.getHigh()) {
            return false;
        }

        // Third must be bearish
        if (third.getOpen() <= third.getClose()) {
            return false;
        }

        // Third opens within second's body
        if (third.getOpen() < second.getOpen() ||
            third.getOpen() > second.getClose()) {
            return false;
        }

        // Third closes in the gap (between first and second) but above first's close
        return third.getClose() > first.getClose() &&
               third.getClose() < second.getLow();
    }
}
