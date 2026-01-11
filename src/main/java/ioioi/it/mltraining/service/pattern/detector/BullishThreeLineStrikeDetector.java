package ioioi.it.mltraining.service.pattern.detector;

import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.service.pattern.PatternDetector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detector for Bullish Three Line Strike pattern.
 * Strong bullish continuation with:
 * - Three consecutive bullish candles (like Three White Soldiers)
 * - Fourth candle: large bearish that opens above third's close
 *   and closes below first's open
 * - Bullish continuation despite bearish fourth candle
 */
@Component
public class BullishThreeLineStrikeDetector implements PatternDetector {

    @Override
    public CandlestickPattern getPatternType() {
        return CandlestickPattern.BULLISH_THREE_LINE_STRIKE;
    }

    @Override
    public int getRequiredCandleCount() {
        return 4;
    }

    @Override
    public boolean detect(List<CandleRaw> candles) {
        if (candles == null || candles.size() < 4) {
            return false;
        }

        CandleRaw first = candles.get(candles.size() - 4);
        CandleRaw second = candles.get(candles.size() - 3);
        CandleRaw third = candles.get(candles.size() - 2);
        CandleRaw fourth = candles.get(candles.size() - 1);

        // First three must be bullish and ascending
        if (first.getClose() <= first.getOpen() ||
            second.getClose() <= second.getOpen() ||
            third.getClose() <= third.getOpen()) {
            return false;
        }

        if (second.getClose() <= first.getClose() ||
            third.getClose() <= second.getClose()) {
            return false;
        }

        // Fourth must be bearish
        if (fourth.getOpen() <= fourth.getClose()) {
            return false;
        }

        // Fourth opens above third's close
        if (fourth.getOpen() <= third.getClose()) {
            return false;
        }

        // Fourth closes below first's open
        return fourth.getClose() < first.getOpen();
    }
}
