package ioioi.it.mltraining.service.pattern.detector;

import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.service.pattern.PatternDetector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detector for Abandoned Baby Bottom pattern.
 * Strong bullish reversal with:
 * - First candle: large bearish
 * - Second candle: doji that gaps down (island)
 * - Third candle: bullish that gaps up
 * - Second candle is isolated (abandoned)
 */
@Component
public class AbandonedBabyBottomDetector implements PatternDetector {

    @Override
    public CandlestickPattern getPatternType() {
        return CandlestickPattern.ABANDONED_BABY_BOTTOM;
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

        // Third must be bullish
        if (third.getClose() <= third.getOpen()) {
            return false;
        }

        // Second must be doji
        Double secondRange = second.getHigh() - second.getLow();
        if (secondRange == 0.0) {
            return false;
        }
        Double secondBody = Math.abs(second.getOpen() - second.getClose());
        Double bodyRatio = secondBody / secondRange;
        if (bodyRatio > 0.05) {
            return false;
        }

        // Second gaps down from first (isolated below)
        if (second.getHigh() >= first.getLow()) {
            return false;
        }

        // Third gaps up from second (isolated above)
        return third.getLow() > second.getHigh();
    }
}
