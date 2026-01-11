package ioioi.it.mltraining.service.pattern.detector;

import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.service.pattern.PatternDetector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detector for Bearish Three Line Strike pattern.
 * Strong bearish continuation with:
 * - Three consecutive bearish candles (like Three Black Crows)
 * - Fourth candle: large bullish that opens below third's close
 *   and closes above first's open
 * - Bearish continuation despite bullish fourth candle
 */
@Component
public class BearishThreeLineStrikeDetector implements PatternDetector {

    @Override
    public CandlestickPattern getPatternType() {
        return CandlestickPattern.BEARISH_THREE_LINE_STRIKE;
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

        // First three must be bearish and descending
        if (first.getOpen() <= first.getClose() ||
            second.getOpen() <= second.getClose() ||
            third.getOpen() <= third.getClose()) {
            return false;
        }

        if (second.getClose() >= first.getClose() ||
            third.getClose() >= second.getClose()) {
            return false;
        }

        // Fourth must be bullish
        if (fourth.getClose() <= fourth.getOpen()) {
            return false;
        }

        // Fourth opens below third's close
        if (fourth.getOpen() >= third.getClose()) {
            return false;
        }

        // Fourth closes above first's open
        return fourth.getClose() > first.getOpen();
    }
}
