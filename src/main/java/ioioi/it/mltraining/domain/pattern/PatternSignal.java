package ioioi.it.mltraining.domain.pattern;

/**
 * Sealed interface hierarchy for candlestick pattern signals.
 * Uses Java 17 sealed interfaces to create an exhaustive type hierarchy
 * that enables pattern matching and compile-time safety.
 *
 * This interface guarantees all possible pattern signal types are known
 * at compile time, allowing the compiler to verify switch expression exhaustiveness.
 */
public sealed interface PatternSignal {

    /**
     * Returns the signal strength multiplier (0.0 - 1.5).
     *
     * @return strength multiplier for scoring
     */
    double getStrengthMultiplier();

    /**
     * Returns whether this signal indicates a reversal pattern.
     *
     * @return true if reversal, false if continuation or neutral
     */
    boolean isReversal();

    /**
     * Bullish reversal patterns - indicate potential upward price movement.
     */
    sealed interface BullishReversal extends PatternSignal permits
            StrongBullishReversal,
            ModerateBullishReversal,
            WeakBullishReversal {

        @Override
        default boolean isReversal() {
            return true;
        }
    }

    /**
     * Bearish reversal patterns - indicate potential downward price movement.
     */
    sealed interface BearishReversal extends PatternSignal permits
            StrongBearishReversal,
            ModerateBearishReversal,
            WeakBearishReversal {

        @Override
        default boolean isReversal() {
            return true;
        }
    }

    /**
     * Continuation patterns - indicate trend is likely to continue.
     */
    sealed interface Continuation extends PatternSignal permits
            BullishContinuation,
            BearishContinuation {

        @Override
        default boolean isReversal() {
            return false;
        }
    }

    /**
     * Indecision patterns - market uncertainty, no clear direction.
     */
    non-sealed interface Indecision extends PatternSignal {
        @Override
        default boolean isReversal() {
            return false;
        }

        @Override
        default double getStrengthMultiplier() {
            return 0.5;
        }
    }

    // ============================================
    // STRONG PATTERNS (1.2-1.5x multiplier)
    // ============================================

    /**
     * Strong bullish reversal - very high confidence upward reversal.
     */
    record StrongBullishReversal() implements BullishReversal {
        @Override
        public double getStrengthMultiplier() {
            return 1.5;
        }
    }

    /**
     * Strong bearish reversal - very high confidence downward reversal.
     */
    record StrongBearishReversal() implements BearishReversal {
        @Override
        public double getStrengthMultiplier() {
            return 1.5;
        }
    }

    // ============================================
    // MODERATE PATTERNS (1.0x multiplier)
    // ============================================

    /**
     * Moderate bullish reversal - medium confidence upward reversal.
     */
    record ModerateBullishReversal() implements BullishReversal {
        @Override
        public double getStrengthMultiplier() {
            return 1.0;
        }
    }

    /**
     * Moderate bearish reversal - medium confidence downward reversal.
     */
    record ModerateBearishReversal() implements BearishReversal {
        @Override
        public double getStrengthMultiplier() {
            return 1.0;
        }
    }

    // ============================================
    // WEAK PATTERNS (0.7x multiplier)
    // ============================================

    /**
     * Weak bullish reversal - low confidence upward reversal.
     */
    record WeakBullishReversal() implements BullishReversal {
        @Override
        public double getStrengthMultiplier() {
            return 0.7;
        }
    }

    /**
     * Weak bearish reversal - low confidence downward reversal.
     */
    record WeakBearishReversal() implements BearishReversal {
        @Override
        public double getStrengthMultiplier() {
            return 0.7;
        }
    }

    // ============================================
    // CONTINUATION PATTERNS
    // ============================================

    /**
     * Bullish continuation - trend continuation upward.
     */
    record BullishContinuation(double strengthMultiplier) implements Continuation {
        @Override
        public double getStrengthMultiplier() {
            return strengthMultiplier;
        }
    }

    /**
     * Bearish continuation - trend continuation downward.
     */
    record BearishContinuation(double strengthMultiplier) implements Continuation {
        @Override
        public double getStrengthMultiplier() {
            return strengthMultiplier;
        }
    }

    // ============================================
    // HELPER METHODS FOR PATTERN MATCHING
    // ============================================

    /**
     * Checks if signal is bullish (reversal or continuation).
     * Uses instanceof pattern matching (Java 17 compatible).
     *
     * @param signal The pattern signal to check
     * @return true if bullish, false otherwise
     */
    static boolean isBullish(PatternSignal signal) {
        return signal instanceof BullishReversal || signal instanceof BullishContinuation;
    }

    /**
     * Checks if signal is bearish (reversal or continuation).
     * Uses instanceof pattern matching (Java 17 compatible).
     *
     * @param signal The pattern signal to check
     * @return true if bearish, false otherwise
     */
    static boolean isBearish(PatternSignal signal) {
        return signal instanceof BearishReversal || signal instanceof BearishContinuation;
    }

    /**
     * Gets a descriptive label for the signal type.
     * Uses instanceof with pattern variables for type-safe casting.
     *
     * @param signal The pattern signal
     * @return human-readable signal description
     */
    static String getLabel(PatternSignal signal) {
        if (signal instanceof StrongBullishReversal) {
            return "Strong Bullish Reversal";
        }
        if (signal instanceof ModerateBullishReversal) {
            return "Moderate Bullish Reversal";
        }
        if (signal instanceof WeakBullishReversal) {
            return "Weak Bullish Reversal";
        }
        if (signal instanceof StrongBearishReversal) {
            return "Strong Bearish Reversal";
        }
        if (signal instanceof ModerateBearishReversal) {
            return "Moderate Bearish Reversal";
        }
        if (signal instanceof WeakBearishReversal) {
            return "Weak Bearish Reversal";
        }
        if (signal instanceof BullishContinuation) {
            return "Bullish Continuation";
        }
        if (signal instanceof BearishContinuation) {
            return "Bearish Continuation";
        }
        if (signal instanceof Indecision) {
            return "Indecision";
        }
        return "Unknown Signal";
    }
}