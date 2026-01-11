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
     *
     * @param signal The pattern signal to check
     * @return true if bullish, false otherwise
     */
    static boolean isBullish(PatternSignal signal) {
        return switch (signal) {
            case BullishReversal br -> true;
            case BullishContinuation bc -> true;
            case BearishReversal br -> false;
            case BearishContinuation bc -> false;
            case Indecision i -> false;
        };
    }

    /**
     * Checks if signal is bearish (reversal or continuation).
     *
     * @param signal The pattern signal to check
     * @return true if bearish, false otherwise
     */
    static boolean isBearish(PatternSignal signal) {
        return switch (signal) {
            case BearishReversal br -> true;
            case BearishContinuation bc -> true;
            case BullishReversal br -> false;
            case BullishContinuation bc -> false;
            case Indecision i -> false;
        };
    }

    /**
     * Gets a descriptive label for the signal type.
     *
     * @param signal The pattern signal
     * @return human-readable signal description
     */
    static String getLabel(PatternSignal signal) {
        return switch (signal) {
            case StrongBullishReversal sbr -> "Strong Bullish Reversal";
            case ModerateBullishReversal mbr -> "Moderate Bullish Reversal";
            case WeakBullishReversal wbr -> "Weak Bullish Reversal";
            case StrongBearishReversal sbr -> "Strong Bearish Reversal";
            case ModerateBearishReversal mbr -> "Moderate Bearish Reversal";
            case WeakBearishReversal wbr -> "Weak Bearish Reversal";
            case BullishContinuation bc -> "Bullish Continuation";
            case BearishContinuation bc -> "Bearish Continuation";
            case Indecision i -> "Indecision";
        };
    }
}