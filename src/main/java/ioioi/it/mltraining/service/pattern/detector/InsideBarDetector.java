package ioioi.it.mltraining.service.pattern.detector;

import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.service.pattern.PatternDetector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detector for Inside Bar pattern.
 * Consolidation/indecision pattern with:
 * - Current candle's range completely inside previous candle's range
 * - High lower than previous high
 * - Low higher than previous low
 * - Indicates consolidation before breakout
 */
@Component
public class InsideBarDetector implements PatternDetector {

    @Override
    public CandlestickPattern getPatternType() {
        return CandlestickPattern.INSIDE_BAR;
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

        CandleRaw previous = candles.get(candles.size() - 2);
        CandleRaw current = candles.get(candles.size() - 1);

        // Current candle must be completely inside previous candle
        return current.getHigh() < previous.getHigh() &&
               current.getLow() > previous.getLow();
    }
}
