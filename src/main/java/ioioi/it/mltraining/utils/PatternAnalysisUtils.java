package ioioi.it.mltraining.utils;

import ioioi.it.mltraining.domain.enums.CandlestickPattern;
import ioioi.it.mltraining.domain.enums.SignalType;
import ioioi.it.mltraining.domain.enums.StrengthLevel;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class for pattern analysis using Java 17 features.
 * Demonstrates functional programming, stream operations, and pattern matching.
 */
public final class PatternAnalysisUtils {

    private PatternAnalysisUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Filters patterns by signal type using functional streams.
     * Uses Java 17 .toList() for concise immutable list creation.
     *
     * @param patterns List of patterns to filter
     * @param signalType Desired signal type
     * @return filtered list of patterns
     */
    public static List<CandlestickPattern> filterBySignalType(
            List<CandlestickPattern> patterns,
            SignalType signalType
    ) {
        return patterns.stream()
                .filter(pattern -> pattern.getSignalType() == signalType)
                .toList(); // Java 17: cleaner than .collect(Collectors.toList())
    }

    /**
     * Filters patterns by minimum strength level.
     * Uses method reference for cleaner code.
     *
     * @param patterns List of patterns to filter
     * @param minStrength Minimum strength level
     * @return filtered list of patterns
     */
    public static List<CandlestickPattern> filterByMinStrength(
            List<CandlestickPattern> patterns,
            StrengthLevel minStrength
    ) {
        return patterns.stream()
                .filter(pattern -> pattern.getStrengthLevel().getLevel() >= minStrength.getLevel())
                .toList();
    }

    /**
     * Groups patterns by their signal type.
     * Uses Collectors.groupingBy for efficient grouping.
     *
     * @param patterns List of patterns to group
     * @return map of signal type to patterns
     */
    public static Map<SignalType, List<CandlestickPattern>> groupBySignalType(
            List<CandlestickPattern> patterns
    ) {
        return patterns.stream()
                .collect(Collectors.groupingBy(CandlestickPattern::getSignalType));
    }

    /**
     * Groups patterns by their strength level.
     *
     * @param patterns List of patterns to group
     * @return map of strength level to patterns
     */
    public static Map<StrengthLevel, List<CandlestickPattern>> groupByStrength(
            List<CandlestickPattern> patterns
    ) {
        return patterns.stream()
                .collect(Collectors.groupingBy(CandlestickPattern::getStrengthLevel));
    }

    /**
     * Finds the strongest pattern in a list.
     * Uses Stream.max() with custom comparator.
     *
     * @param patterns List of patterns
     * @return strongest pattern, or null if list is empty
     */
    public static CandlestickPattern findStrongestPattern(List<CandlestickPattern> patterns) {
        return patterns.stream()
                .max((p1, p2) -> Double.compare(p1.getBaseScore(), p2.getBaseScore()))
                .orElse(null);
    }

    /**
     * Calculates the average reliability of patterns.
     * Uses mapToInt and average() for efficient calculation.
     *
     * @param patterns List of patterns
     * @return average reliability percentage, or 0.0 if list is empty
     */
    public static double calculateAverageReliability(List<CandlestickPattern> patterns) {
        return patterns.stream()
                .mapToInt(CandlestickPattern::getReliabilityPercent)
                .average()
                .orElse(0.0);
    }

    /**
     * Filters patterns that are both significant and highly reliable.
     * Uses method references and functional composition.
     *
     * @param patterns List of patterns
     * @param minReliability Minimum reliability percentage
     * @return filtered list of high-quality patterns
     */
    public static List<CandlestickPattern> findHighQualityPatterns(
            List<CandlestickPattern> patterns,
            int minReliability
    ) {
        return patterns.stream()
                .filter(CandlestickPattern::isSignificant)
                .filter(pattern -> pattern.getReliabilityPercent() >= minReliability)
                .toList();
    }

    /**
     * Counts patterns by category using switch expression.
     * Demonstrates Java 17 switch expression with pattern grouping.
     *
     * @param patterns List of patterns to analyze
     * @return map of category to count
     */
    public static Map<String, Long> countByCategory(List<CandlestickPattern> patterns) {
        return patterns.stream()
                .collect(Collectors.groupingBy(
                        CandlestickPattern::getCategory,
                        Collectors.counting()
                ));
    }

    /**
     * Generates a text summary of pattern analysis using text blocks.
     * Demonstrates Java 17 text blocks for multi-line strings.
     *
     * @param patterns List of analyzed patterns
     * @return formatted summary string
     */
    public static String generateSummary(List<CandlestickPattern> patterns) {
        if (patterns.isEmpty()) {
            return "No patterns detected.";
        }

        long bullishCount = patterns.stream().filter(CandlestickPattern::isBullish).count();
        long bearishCount = patterns.stream().filter(CandlestickPattern::isBearish).count();
        CandlestickPattern strongest = findStrongestPattern(patterns);
        double avgReliability = calculateAverageReliability(patterns);

        // Java 17 text block for clean multi-line string formatting
        return """
                ┌─────────────────────────────────────────┐
                │     Pattern Analysis Summary            │
                ├─────────────────────────────────────────┤
                │ Total Patterns:      %-18d │
                │ Bullish Patterns:    %-18d │
                │ Bearish Patterns:    %-18d │
                │ Strongest Pattern:   %-18s │
                │ Average Reliability: %.2f%%%-13s │
                └─────────────────────────────────────────┘
                """.formatted(
                patterns.size(),
                bullishCount,
                bearishCount,
                strongest != null ? strongest.name() : "N/A",
                avgReliability,
                ""
        );
    }

    /**
     * Creates a detailed pattern report using text blocks.
     * Shows advanced formatting with Java 17 features.
     *
     * @param pattern Pattern to report on
     * @return formatted pattern details
     */
    public static String generatePatternReport(CandlestickPattern pattern) {
        return """
                Pattern Details:
                ═══════════════════════════════════════════
                Name:           %s
                Strength:       %s (%d/100)
                Signal Type:    %s
                Reliability:    %s (%d%%)
                Candle Count:   %d
                Category:       %s
                Base Score:     %.2f
                ═══════════════════════════════════════════
                Description:    %s
                Prediction:     %s
                """.formatted(
                pattern.name(),
                pattern.getStrength(),
                pattern.getWeight(),
                pattern.getSignal(),
                pattern.getReliability(),
                pattern.getReliabilityPercent(),
                pattern.getCandleCount(),
                pattern.getCategory(),
                pattern.getBaseScore(),
                pattern.getInfo(),
                pattern.getPrediction()
        );
    }

    /**
     * Pattern matching example using instanceof pattern matching (Java 17).
     * This is a conceptual example showing how pattern matching could be used.
     *
     * @param obj Object to analyze
     * @return description of the object type
     */
    public static String describeObject(Object obj) {
        return switch (obj) {
            case null -> "Object is null";
            case CandlestickPattern pattern -> "Pattern: " + pattern.name() +
                    " (Strength: " + pattern.getStrength() + ")";
            case SignalType signal -> "Signal: " + signal.getLabel();
            case StrengthLevel strength -> "Strength: " + strength.getLabel() +
                    " (" + strength.getWeight() + "/100)";
            case String s when s.isEmpty() -> "Empty string";
            case String s -> "String with length: " + s.length();
            case Integer i when i > 0 -> "Positive integer: " + i;
            case Integer i when i < 0 -> "Negative integer: " + i;
            case Integer i -> "Zero";
            default -> "Unknown type: " + obj.getClass().getSimpleName();
        };
    }
}
