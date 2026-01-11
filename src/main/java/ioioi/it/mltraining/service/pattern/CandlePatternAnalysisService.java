package ioioi.it.mltraining.service.pattern;

import ioioi.it.mltraining.domain.enums.CandleInterval;
import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.entity.CandleRaw;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for analyzing candlestick patterns.
 * Maintains a cache of last 120 candles (+30 buffer) per symbol/interval for efficient pattern detection.
 * Supports patterns from 1 to 7 candles (single, double, triple, and complex patterns).
 */
@Service
@Slf4j
public class CandlePatternAnalysisService {

    private static final int MAX_CACHE_SIZE = 120;
    private static final int BUFFER_SIZE = 30;
    private static final int MAX_LOOKBACK = 7; // Increased to support complex patterns (5+ candles)

    // Default patterns to check - includes single, double, triple, and complex patterns
    private static final List<CandlestickPattern> DEFAULT_PATTERNS = List.of(
            // ============================================
            // SINGLE CANDLESTICK PATTERNS (1 candle)
            // ============================================
            // Bullish reversal
            CandlestickPattern.HAMMER,
            CandlestickPattern.INVERTED_HAMMER,
            CandlestickPattern.DRAGONFLY_DOJI,
            CandlestickPattern.BULLISH_MARUBOZU,
            CandlestickPattern.BULLISH_SPINNING_TOP,
            // Bearish reversal
            CandlestickPattern.SHOOTING_STAR,
            CandlestickPattern.HANGING_MAN,
            CandlestickPattern.GRAVESTONE_DOJI,
            CandlestickPattern.BEARISH_MARUBOZU,
            CandlestickPattern.BEARISH_SPINNING_TOP,

            // ============================================
            // DOUBLE CANDLESTICK PATTERNS (2 candles)
            // ============================================
            // Bullish reversal
            CandlestickPattern.BULLISH_ENGULFING,
            CandlestickPattern.PIERCING_PATTERN,
            CandlestickPattern.TWEEZER_BOTTOM,
            CandlestickPattern.BULLISH_HARAMI,
            CandlestickPattern.BULLISH_KICKER,
            // Bearish reversal
            CandlestickPattern.BEARISH_ENGULFING,
            CandlestickPattern.DARK_CLOUD_COVER,
            CandlestickPattern.TWEEZER_TOP,
            CandlestickPattern.BEARISH_HARAMI,
            CandlestickPattern.BEARISH_KICKER,

            // ============================================
            // TRIPLE CANDLESTICK PATTERNS (3 candles)
            // ============================================
            // Bullish reversal
            CandlestickPattern.MORNING_STAR,
            CandlestickPattern.MORNING_DOJI_STAR,
            CandlestickPattern.THREE_WHITE_SOLDIERS,
            CandlestickPattern.BULLISH_THREE_LINE_STRIKE,
            CandlestickPattern.THREE_INSIDE_UP,
            CandlestickPattern.THREE_OUTSIDE_UP,
            CandlestickPattern.UPSIDE_TASUKI_GAP,
            // Bearish reversal
            CandlestickPattern.EVENING_STAR,
            CandlestickPattern.EVENING_DOJI_STAR,
            CandlestickPattern.THREE_BLACK_CROWS,
            CandlestickPattern.BEARISH_THREE_LINE_STRIKE,
            CandlestickPattern.THREE_INSIDE_DOWN,
            CandlestickPattern.THREE_OUTSIDE_DOWN,
            CandlestickPattern.DOWNSIDE_TASUKI_GAP,
            CandlestickPattern.ABANDONED_BABY_TOP,
            CandlestickPattern.ABANDONED_BABY_BOTTOM,

            // ============================================
            // COMPLEX PATTERNS (4-5+ candles)
            // ============================================
            // Bullish
            CandlestickPattern.RISING_THREE_METHODS,
            CandlestickPattern.MAT_HOLD,
            CandlestickPattern.LADDER_BOTTOM,
            // Bearish
            CandlestickPattern.FALLING_THREE_METHODS,
            CandlestickPattern.LADDER_TOP,

            // ============================================
            // RARE/ADVANCED PATTERNS
            // ============================================
            CandlestickPattern.THREE_STARS_IN_THE_SOUTH,
            CandlestickPattern.UNIQUE_THREE_RIVER_BOTTOM,
            CandlestickPattern.CONCEALING_BABY_SWALLOW,
            CandlestickPattern.BREAKAWAY_BULLISH,
            CandlestickPattern.BREAKAWAY_BEARISH
    );

    // Cache: Symbol -> Interval -> Deque of candles (ordered by time, oldest first)
    private final Map<String, Map<CandleInterval, Deque<CandleRaw>>> candleCache = new ConcurrentHashMap<>();

    // Pattern detectors registry: Pattern -> Detector
    private final Map<CandlestickPattern, PatternDetector> detectors = new ConcurrentHashMap<>();

    /**
     * Register a pattern detector.
     */
    public void registerDetector(PatternDetector detector) {
        detectors.put(detector.getPatternType(), detector);
        log.info("Registered pattern detector for: {}", detector.getPatternType());
    }

    /**
     * Analyze candle using default patterns.
     * Updates candle.patterns with detected patterns.
     *
     * @param candle The candle to analyze (must be already persisted in DB)
     */
    public void analyzeCandle(CandleRaw candle) {
        analyzeCandle(candle, DEFAULT_PATTERNS);
    }

    /**
     * Analyze candle using specified patterns.
     * Updates candle.patterns with detected patterns.
     *
     * @param candle The candle to analyze (must be already persisted in DB)
     * @param patternsToCheck List of patterns to check for
     */
    public void analyzeCandle(CandleRaw candle, List<CandlestickPattern> patternsToCheck) {
        if (candle == null || candle.getSymbol() == null || candle.getInterval() == null) {
            log.warn("Cannot analyze null candle or candle with missing symbol/interval");
            return;
        }

        String symbol = candle.getSymbol();
        CandleInterval interval = candle.getInterval();

        // 1. Add candle to cache
        addToCache(symbol, interval, candle);

        // 2. Get historical candles for analysis (up to 7 candles including current)
        List<CandleRaw> historicalCandles = getHistoricalCandles(symbol, interval, MAX_LOOKBACK);

        if (historicalCandles.isEmpty()) {
            log.debug("No historical candles available for analysis: {} {}", symbol, interval);
            return;
        }

        // 3. Detect patterns using functional stream operations
        List<CandlestickPattern> detectedPatterns = patternsToCheck.stream()
                .filter(pattern -> detectors.containsKey(pattern))
                .filter(pattern -> hasEnoughCandles(historicalCandles, pattern))
                .filter(pattern -> detectPattern(pattern, historicalCandles, symbol, interval, candle))
                .toList(); // Java 17 feature: .toList() instead of .collect(Collectors.toList())

        log.debug("Analysis complete for {} {} at {}: {} patterns detected",
                symbol, interval, candle.getOpenTime(), detectedPatterns.size());
    }

    /**
     * Checks if we have enough historical candles for the pattern.
     *
     * @param historicalCandles Available historical candles
     * @param pattern Pattern to check
     * @return true if sufficient candles available
     */
    private boolean hasEnoughCandles(List<CandleRaw> historicalCandles, CandlestickPattern pattern) {
        PatternDetector detector = detectors.get(pattern);
        return detector != null && historicalCandles.size() >= detector.getRequiredCandleCount();
    }

    /**
     * Detects a specific pattern in the historical candles.
     *
     * @param pattern Pattern to detect
     * @param historicalCandles Historical candles
     * @param symbol Symbol being analyzed
     * @param interval Interval being analyzed
     * @param candle Current candle
     * @return true if pattern was detected
     */
    private boolean detectPattern(
            CandlestickPattern pattern,
            List<CandleRaw> historicalCandles,
            String symbol,
            CandleInterval interval,
            CandleRaw candle
    ) {
        PatternDetector detector = detectors.get(pattern);
        if (detector == null) {
            log.warn("No detector registered for pattern: {}", pattern);
            return false;
        }

        int requiredCount = detector.getRequiredCandleCount();
        List<CandleRaw> candlesForPattern = historicalCandles.subList(
                Math.max(0, historicalCandles.size() - requiredCount),
                historicalCandles.size()
        );

        boolean detected = detector.detect(candlesForPattern);
        if (detected) {
            log.debug("Pattern detected: {} for {} {} at {}",
                    pattern, symbol, interval, candle.getOpenTime());
        }

        return detected;
    }

    /**
     * Add candle to cache and manage cache size.
     *
     * @param symbol Symbol identifier
     * @param interval Candle interval
     * @param candle Candle to add
     */
    private void addToCache(String symbol, CandleInterval interval, CandleRaw candle) {
        candleCache
                .computeIfAbsent(symbol, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(interval, k -> new LinkedList<>())
                .addLast(candle);

        // Trim cache if exceeded buffer size
        Deque<CandleRaw> cache = candleCache.get(symbol).get(interval);
        int maxSize = MAX_CACHE_SIZE + BUFFER_SIZE;

        while (cache.size() > maxSize) {
            CandleRaw removed = cache.removeFirst();
            log.trace("Removed old candle from cache: {} {} at {}",
                symbol, interval, removed.getOpenTime());
        }

        log.trace("Cache size for {} {}: {}", symbol, interval, cache.size());
    }

    /**
     * Get historical candles for pattern analysis.
     *
     * @param symbol Symbol
     * @param interval Interval
     * @param maxCount Maximum number of candles to return (including most recent)
     * @return List of candles ordered from oldest to newest
     */
    private List<CandleRaw> getHistoricalCandles(String symbol, CandleInterval interval, int maxCount) {
        Deque<CandleRaw> cache = candleCache
                .getOrDefault(symbol, Collections.emptyMap())
                .getOrDefault(interval, new LinkedList<>());

        if (cache.isEmpty()) {
            return Collections.emptyList();
        }

        // Get last N candles
        int size = cache.size();
        int fromIndex = Math.max(0, size - maxCount);

        return new ArrayList<>(cache).subList(fromIndex, size);
    }

    /**
     * Clear cache for specific symbol and interval.
     *
     * @param symbol Symbol to clear
     * @param interval Interval to clear
     */
    public void clearCache(String symbol, CandleInterval interval) {
        Map<CandleInterval, Deque<CandleRaw>> symbolCache = candleCache.get(symbol);
        if (symbolCache != null) {
            symbolCache.remove(interval);
            log.info("Cleared cache for {} {}", symbol, interval);
        }
    }

    /**
     * Clear all cache.
     */
    public void clearAllCache() {
        candleCache.clear();
        log.info("Cleared all candle cache");
    }

    /**
     * Get cache statistics using Java 17 functional streams.
     * Returns a map of symbol -> interval -> candle count.
     *
     * @return immutable cache statistics
     */
    public Map<String, Map<CandleInterval, Integer>> getCacheStats() {
        return candleCache.entrySet().stream()
                .collect(HashMap::new,
                        (map, entry) -> map.put(
                                entry.getKey(),
                                entry.getValue().entrySet().stream()
                                        .collect(HashMap::new,
                                                (innerMap, innerEntry) -> innerMap.put(
                                                        innerEntry.getKey(),
                                                        innerEntry.getValue().size()
                                                ),
                                                HashMap::putAll)
                        ),
                        HashMap::putAll);
    }

    /**
     * Gets total cache size across all symbols and intervals.
     *
     * @return total number of cached candles
     */
    public int getTotalCacheSize() {
        return candleCache.values().stream()
                .flatMap(intervalMap -> intervalMap.values().stream())
                .mapToInt(Deque::size)
                .sum();
    }

    /**
     * Pattern detection result record for better type safety.
     * Uses Java 17 record for immutable result representation.
     *
     * @param pattern The detected pattern
     * @param confidence Confidence score (0.0-1.0)
     * @param candleIndex Index of the candle where pattern was detected
     */
    public record PatternDetectionResult(
            CandlestickPattern pattern,
            double confidence,
            int candleIndex
    ) {
        public PatternDetectionResult {
            if (confidence < 0.0 || confidence > 1.0) {
                throw new IllegalArgumentException("Confidence must be between 0.0 and 1.0");
            }
        }

        /**
         * Checks if this detection meets minimum confidence threshold.
         *
         * @param threshold Minimum confidence (0.0-1.0)
         * @return true if confidence >= threshold
         */
        public boolean meetsThreshold(double threshold) {
            return confidence >= threshold;
        }
    }
}
