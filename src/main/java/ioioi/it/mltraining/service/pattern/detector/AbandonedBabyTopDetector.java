package ioioi.it.mltraining.service.pattern.detector;

import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.service.pattern.PatternDetector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detector for Abandoned Baby Top pattern.
 * Strong bearish reversal with:
 * - First candle: large bullish
 * - Second candle: doji that gaps up (island)
 * - Third candle: bearish that gaps down
 * - Second candle is isolated (abandoned)
 */
@Component
public class AbandonedBabyTopDetector implements PatternDetector {

    @Override
    public CandlestickPattern getPatternType() {
        return CandlestickPattern.ABANDONED_BABY_TOP;
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

        // Third must be bearish
        if (third.getOpen() <= third.getClose()) {
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

        // Second gaps up from first (isolated above)
        if (second.getLow() <= first.getHigh()) {
            return false;
        }

        // Third gaps down from second (isolated below)
        return third.getHigh() < second.getLow();
    }
}
