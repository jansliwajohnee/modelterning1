package ioioi.it.mltraining.service.pattern.detector;

import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.service.pattern.PatternDetector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detector for Outside Bar pattern.
 * Engulfing/volatility expansion pattern with:
 * - Current candle's range completely engulfs previous candle's range
 * - High higher than previous high
 * - Low lower than previous low
 * - Indicates increased volatility and potential breakout
 */
@Component
public class OutsideBarDetector implements PatternDetector {

    @Override
    public CandlestickPattern getPatternType() {
        return CandlestickPattern.OUTSIDE_BAR;
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

        // Current candle must completely engulf previous candle
        return current.getHigh() > previous.getHigh() &&
               current.getLow() < previous.getLow();
    }
}
