package ioioi.it.mltraining.service.pattern.detector;

import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.service.pattern.PatternDetector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detector for Flag pattern.
 * Continuation pattern with:
 * - Small consolidation after strong move
 * - Parallel trendlines (rectangular channel)
 * - Counter-trend drift
 * - Typically requires 5+ candles
 * - Simplified: consistent narrow range
 */
@Component
public class FlagDetector implements PatternDetector {

    @Override
    public CandlestickPattern getPatternType() {
        return CandlestickPattern.FLAG;
    }

    @Override
    public int getRequiredCandleCount() {
        return 5;
    }

    @Override
    public boolean detect(List<CandleRaw> candles) {
        if (candles == null || candles.size() < 5) {
            return false;
        }

        // Check if last 5 candles have consistent narrow ranges (flag consolidation)
        Double sumRange = 0.0;
        Double avgPrice = 0.0;

        for (int i = candles.size() - 5; i < candles.size(); i++) {
            CandleRaw candle = candles.get(i);
            Double range = candle.getHigh() - candle.getLow();
            Double midPrice = (candle.getHigh() + candle.getLow()) / 2.0;
            sumRange = sumRange + range;
            avgPrice = avgPrice + midPrice;
        }

        Double avgRange = sumRange / 5.0;
        avgPrice = avgPrice / 5.0;

        if (avgPrice == 0.0) {
            return false;
        }

        // Range should be consistent and small (less than 2% of price)
        Double rangePercent = avgRange / avgPrice;
        return rangePercent < 0.02;
    }
}
