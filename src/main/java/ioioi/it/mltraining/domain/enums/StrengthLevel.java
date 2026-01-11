package ioioi.it.mltraining.domain.enums;

/**
 * Strength level for candlestick patterns.
 */
public enum StrengthLevel {
    EXTREME(4, "Extreme", 1.5),
    STRONG(3, "Strong", 1.2),
    MODERATE(2, "Moderate", 1.0),
    WEAK(1, "Weak", 0.7),
    NONE(0, "None", 0.0);

    private final int level;
    private final String label;
    private final double multiplier;

    StrengthLevel(int level, String label, double multiplier) {
        this.level = level;
        this.label = label;
        this.multiplier = multiplier;
    }

    public int getLevel() { return level; }
    public String getLabel() { return label; }
    public double getMultiplier() { return multiplier; }

    /**
     * Parse strength level from localized string.
     * Uses Java 17 enhanced pattern matching for cleaner, more maintainable code.
     *
     * @param strength The strength string to parse (can be null)
     * @return corresponding StrengthLevel, or NONE if not recognized
     */
    public static StrengthLevel fromString(String strength) {
        if (strength == null) {
            return NONE;
        }

        String normalized = strength.toLowerCase().strip();

        if (normalized.contains("ekstrem") || normalized.contains("extreme")) {
            return EXTREME;
        }
        if (normalized.contains("siln") || normalized.contains("strong")) {
            return STRONG;
        }
        if (normalized.contains("umiark") || normalized.contains("moderate")) {
            return MODERATE;
        }
        if (normalized.contains("słab") || normalized.contains("weak")) {
            return WEAK;
        }

        return NONE;
    }

    /**
     * Gets the weight score for this strength level.
     * Uses switch expression for compile-time exhaustiveness checking.
     *
     * @return weight value (0-100)
     */
    public int getWeight() {
        return switch (this) {
            case EXTREME -> 100;
            case STRONG -> 75;
            case MODERATE -> 50;
            case WEAK -> 25;
            case NONE -> 0;
        };
    }

    /**
     * Checks if this strength level is significant (moderate or higher).
     *
     * @return true if MODERATE, STRONG, or EXTREME
     */
    public boolean isSignificant() {
        return switch (this) {
            case EXTREME, STRONG, MODERATE -> true;
            case WEAK, NONE -> false;
        };
    }
}
