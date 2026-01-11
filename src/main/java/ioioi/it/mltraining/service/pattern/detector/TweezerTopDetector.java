package ioioi.it.mltraining.service.pattern.detector;

import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.service.pattern.PatternDetector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detector for Tweezer Top pattern.
 * Bearish reversal with:
 * - Two candles with matching highs
 * - First candle: bullish
 * - Second candle: bearish
 * - Both highs are very close to each other
 * - Indicates resistance level
 */
@Component
public class TweezerTopDetector implements PatternDetector {

    @Override
    public CandlestickPattern getPatternType() {
        return CandlestickPattern.TWEEZER_TOP;
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

        // First candle should be bullish
        if (first.getClose() <= first.getOpen()) {
            return false;
        }

        // Second candle should be bearish
        if (second.getOpen() <= second.getClose()) {
            return false;
        }

        // Highs should be very close (within 0.2% of each other)
        Double highDiff = Math.abs(first.getHigh() - second.getHigh());
        Double avgHigh = (first.getHigh() + second.getHigh()) / 2.0;
        Double diffRatio = highDiff / avgHigh;

        return diffRatio < 0.002;
    }
}
