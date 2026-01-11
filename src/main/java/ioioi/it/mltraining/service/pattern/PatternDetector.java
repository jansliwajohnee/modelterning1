package ioioi.it.mltraining.service.pattern;

import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.entity.CandleRaw;

import java.util.List;

/**
 * Interface for detecting candlestick patterns.
 * Each detector is responsible for identifying a specific pattern.
 */
public interface PatternDetector {

    /**
     * Returns the pattern type this detector identifies
     */
    CandlestickPattern getPatternType();

    /**
     * Detects if the pattern exists in the given candles.
     *
     * @param candles List of candles ordered from oldest to newest.
     *                The last candle is the one being analyzed.
     *                Maximum 7 candles will be provided (current + 6 previous).
     * @return true if pattern is detected, false otherwise
     */
    boolean detect(List<CandleRaw> candles);

    /**
     * Returns the number of candles required for this pattern (1-7)
     */
    default int getRequiredCandleCount() {
        return 1; // Most patterns are single-candle
    }
}
