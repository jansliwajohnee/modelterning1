package ioioi.it.mltraining.service.pattern.detector;

import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.service.pattern.PatternDetector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detector for Dark Cloud Cover pattern.
 * Bearish reversal with:
 * - First candle: bullish (green)
 * - Second candle: bearish (red) that opens above first's high
 * - Second closes below midpoint of first candle's body
 * - Appears at top of uptrend
 */
@Component
public class DarkCloudCoverDetector implements PatternDetector {

    @Override
    public CandlestickPattern getPatternType() {
        return CandlestickPattern.DARK_CLOUD_COVER;
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

        // Second opens above first's high (gap up)
        if (second.getOpen() <= first.getHigh()) {
            return false;
        }

        // Calculate midpoint of first candle's body
        Double firstMidpoint = (first.getOpen() + first.getClose()) / 2.0;

        // Second close must be below midpoint of first's body
        return second.getClose() < firstMidpoint &&
               second.getClose() > first.getOpen(); // But not fully engulfing
    }
}
