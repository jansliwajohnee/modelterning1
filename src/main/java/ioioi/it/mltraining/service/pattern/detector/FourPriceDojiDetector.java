package ioioi.it.mltraining.service.pattern.detector;

import ioioi.it.mltraining.entity.CandleRaw;
import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.service.pattern.PatternDetector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detector for Four Price Doji pattern.
 * Extreme indecision/low volume pattern with:
 * - Open = High = Low = Close
 * - No body, no shadows
 * - Forms a horizontal line
 * - Very rare, indicates extremely low trading activity
 */
@Component
public class FourPriceDojiDetector implements PatternDetector {

    @Override
    public CandlestickPattern getPatternType() {
        return CandlestickPattern.FOUR_PRICE_DOJI;
    }

    @Override
    public boolean detect(List<CandleRaw> candles) {
        if (candles == null || candles.isEmpty()) {
            return false;
        }

        CandleRaw candle = candles.get(candles.size() - 1);

        Double open = candle.getOpen();
        Double close = candle.getClose();
        Double high = candle.getHigh();
        Double low = candle.getLow();

        // All four prices must be equal (or extremely close due to rounding)
        return open == close &&
               open == high &&
               open == low;
    }
}
