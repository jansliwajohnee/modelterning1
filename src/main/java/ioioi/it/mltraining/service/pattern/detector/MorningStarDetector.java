package ioioi.it.mltraining.service.pattern.detector;

import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.service.pattern.PatternDetector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detector for Morning Star pattern.
 * Strong bullish reversal with:
 * - First candle: large bearish
 * - Second candle: small body (star) that gaps down
 * - Third candle: large bullish that closes above midpoint of first
 * - Appears at bottom of downtrend
 */
@Component
public class MorningStarDetector implements PatternDetector {

    @Override
    public CandlestickPattern getPatternType() {
        return CandlestickPattern.MORNING_STAR;
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

        // First candle must be bearish
        if (first.getOpen() <= first.getClose()) {
            return false;
        }

        // Third candle must be bullish
        if (third.getClose() <= third.getOpen()) {
            return false;
        }

        // Second candle should be small (star)
        Double firstBody = first.getOpen() - first.getClose();
        Double secondBody = Math.abs(second.getOpen() - second.getClose());
        if (secondBody > firstBody * 0.3) {
            return false;
        }

        // Second should gap down from first
        if (second.getHigh() >= first.getLow()) {
            return false;
        }

        // Third closes above midpoint of first
        Double firstMidpoint = (first.getOpen() + first.getClose()) / 2.0;
        return third.getClose() > firstMidpoint;
    }
}
