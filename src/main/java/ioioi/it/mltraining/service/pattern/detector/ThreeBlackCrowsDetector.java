package ioioi.it.mltraining.service.pattern.detector;

import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.service.pattern.PatternDetector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detector for Three Black Crows pattern.
 * Strong bearish continuation/reversal with:
 * - Three consecutive bearish candles
 * - Each opens within previous candle's body
 * - Each closes lower than previous
 * - Long bodies, small shadows
 * - Indicates strong selling pressure
 */
@Component
public class ThreeBlackCrowsDetector implements PatternDetector {

    @Override
    public CandlestickPattern getPatternType() {
        return CandlestickPattern.THREE_BLACK_CROWS;
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

        // All three must be bearish
        if (first.getOpen() <= first.getClose() ||
            second.getOpen() <= second.getClose() ||
            third.getOpen() <= third.getClose()) {
            return false;
        }

        // Each closes lower than previous
        if (second.getClose() >= first.getClose() ||
            third.getClose() >= second.getClose()) {
            return false;
        }

        // Each opens within previous candle's body
        if (second.getOpen() < first.getClose() ||
            second.getOpen() > first.getOpen() ||
            third.getOpen() < second.getClose() ||
            third.getOpen() > second.getOpen()) {
            return false;
        }

        // Bodies should be significant (each at least 60% of range)
        for (CandleRaw candle : List.of(first, second, third)) {
            Double body = candle.getOpen() - candle.getClose();
            Double range = candle.getHigh() - candle.getLow();
            if (range == 0.0) {
                return false;
            }
            Double bodyRatio = body / range;
            if (bodyRatio < 0.60) {
                return false;
            }
        }

        return true;
    }
}
