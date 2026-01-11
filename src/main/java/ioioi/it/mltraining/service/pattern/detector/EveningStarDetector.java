package ioioi.it.mltraining.service.pattern.detector;

import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.service.pattern.PatternDetector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detector for Evening Star pattern.
 * Strong bearish reversal with:
 * - First candle: large bullish
 * - Second candle: small body (star) that gaps up
 * - Third candle: large bearish that closes below midpoint of first
 * - Appears at top of uptrend
 */
@Component
public class EveningStarDetector implements PatternDetector {

    @Override
    public CandlestickPattern getPatternType() {
        return CandlestickPattern.EVENING_STAR;
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

        // First candle must be bullish
        if (first.getClose() <= first.getOpen()) {
            return false;
        }

        // Third candle must be bearish
        if (third.getOpen() <= third.getClose()) {
            return false;
        }

        // Second candle should be small (star)
        Double firstBody = first.getClose() - first.getOpen();
        Double secondBody = Math.abs(second.getOpen() - second.getClose());
        if (secondBody > firstBody * 0.3) {
            return false;
        }

        // Second should gap up from first
        if (second.getLow() <= first.getHigh()) {
            return false;
        }

        // Third closes below midpoint of first
        Double firstMidpoint = (first.getOpen() + first.getClose()) / 2.0;
        return third.getClose() < firstMidpoint;
    }
}
