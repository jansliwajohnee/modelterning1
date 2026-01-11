package ioioi.it.mltraining.domain.enums;

/**
 * Reliability level for candlestick patterns.
 */
public enum ReliabilityLevel {
    VERY_HIGH(4, "Very High", 85),
    HIGH(3, "High", 72),
    MEDIUM(2, "Medium", 60),
    LOW(1, "Low", 45),
    NONE(0, "None", 0);

    private final int level;
    private final String label;
    private final int baseAccuracyPercent;

    ReliabilityLevel(int level, String label, int baseAccuracyPercent) {
        this.level = level;
        this.label = label;
        this.baseAccuracyPercent = baseAccuracyPercent;
    }

    public int getLevel() { return level; }
    public String getLabel() { return label; }
    public int getBaseAccuracyPercent() { return baseAccuracyPercent; }

    /**
     * Parse reliability level from localized string.
     */
    public static ReliabilityLevel fromString(String reliability) {
        if (reliability == null) return NONE;
        String s = reliability.toLowerCase();

        if (s.contains("bardzo wysok") || s.contains("very high")) return VERY_HIGH;
        if (s.contains("wysok") || s.contains("high")) return HIGH;
        if (s.contains("średni") || s.contains("medium")) return MEDIUM;
        if (s.contains("nisk") || s.contains("low")) return LOW;

        return NONE;
    }
}
