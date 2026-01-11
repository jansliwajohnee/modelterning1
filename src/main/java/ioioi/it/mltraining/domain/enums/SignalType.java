package ioioi.it.mltraining.domain.enums;

/**
 * Signal type for candlestick patterns.
 */
public enum SignalType {
    BULLISH_REVERSAL("Bullish Reversal", true, true),
    BEARISH_REVERSAL("Bearish Reversal", false, true),
    BULLISH_CONTINUATION("Bullish Continuation", true, false),
    BEARISH_CONTINUATION("Bearish Continuation", false, false),
    INDECISION("Indecision", null, false),
    NEUTRAL("Neutral", null, false);

    private final String label;
    private final Boolean bullish; // null = neutral
    private final boolean reversal;

    SignalType(String label, Boolean bullish, boolean reversal) {
        this.label = label;
        this.bullish = bullish;
        this.reversal = reversal;
    }

    public String getLabel() { return label; }
    public Boolean isBullish() { return bullish; }
    public boolean isReversal() { return reversal; }
    public boolean isContinuation() { return !reversal && bullish != null; }

    /**
     * Parse signal type from localized string.
     * Uses guard clauses for cleaner, more maintainable logic.
     *
     * @param signal The signal string to parse (can be null)
     * @return corresponding SignalType, or NEUTRAL if not recognized
     */
    public static SignalType fromString(String signal) {
        if (signal == null) {
            return NEUTRAL;
        }

        String normalized = signal.toLowerCase().strip();

        if (normalized.contains("byczy") || normalized.contains("bullish")) {
            if (normalized.contains("odwrócenie") || normalized.contains("reversal")) {
                return BULLISH_REVERSAL;
            }
            if (normalized.contains("kontynuacja") || normalized.contains("continuation")) {
                return BULLISH_CONTINUATION;
            }
            return BULLISH_REVERSAL;
        }

        if (normalized.contains("niedźwiedzi") || normalized.contains("bearish")) {
            if (normalized.contains("odwrócenie") || normalized.contains("reversal")) {
                return BEARISH_REVERSAL;
            }
            if (normalized.contains("kontynuacja") || normalized.contains("continuation")) {
                return BEARISH_CONTINUATION;
            }
            return BEARISH_REVERSAL;
        }

        if (normalized.contains("niezdecydowanie") || normalized.contains("indecision")) {
            return INDECISION;
        }

        return NEUTRAL;
    }

    /**
     * Gets the directional bias as an integer value.
     * Uses switch expression for exhaustive matching.
     *
     * @return 1 for bullish, -1 for bearish, 0 for neutral/indecision
     */
    public int getDirectionalBias() {
        return switch (this) {
            case BULLISH_REVERSAL, BULLISH_CONTINUATION -> 1;
            case BEARISH_REVERSAL, BEARISH_CONTINUATION -> -1;
            case INDECISION, NEUTRAL -> 0;
        };
    }

    /**
     * Checks if this signal type matches the given trend direction.
     *
     * @param isBullish true for bullish trend, false for bearish
     * @return true if signal aligns with trend direction
     */
    public boolean matchesTrend(boolean isBullish) {
        return switch (this) {
            case BULLISH_REVERSAL, BULLISH_CONTINUATION -> isBullish;
            case BEARISH_REVERSAL, BEARISH_CONTINUATION -> !isBullish;
            case INDECISION, NEUTRAL -> false;
        };
    }
}
