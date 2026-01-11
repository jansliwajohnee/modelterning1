package ioioi.it.mltraining.domain.enums;

public enum CandleInterval {
    M1(60_000L, "1m"),           // 1 minute
    M3(180_000L, "3m"),          // 3 minutes
    M5(300_000L, "5m"),          // 5 minutes
    M15(900_000L, "15m"),        // 15 minutes
    M30(1_800_000L, "30m"),      // 30 minutes
    H1(3_600_000L, "1h");        // 1 hour

    private final Long milliseconds;
    private final String label;

    CandleInterval(Long milliseconds, String label) {
        this.milliseconds = milliseconds;
        this.label = label;
    }

    public Long getMilliseconds() {
        return milliseconds;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Zwraca timestamp wyrównany do początku interwału
     * Np. dla M15 i timestamp 10:17:34 -> 10:15:00
     */
    public long alignToInterval(long timestamp) {
        return (timestamp / milliseconds) * milliseconds;
    }

    /**
     * Converts a string label to CandleInterval enum.
     * Uses Java 17 switch expression for clean matching.
     *
     * @param label String label (e.g., "1m", "5m", "1h")
     * @return corresponding CandleInterval enum
     * @throws IllegalArgumentException if label is not recognized
     */
    public static CandleInterval fromString(String label) {
        if (label == null) {
            throw new IllegalArgumentException("Candle interval label cannot be null");
        }

        String normalized = label.toLowerCase().strip();

        return switch (normalized) {
            case "1m" -> M1;
            case "3m" -> M3;
            case "5m" -> M5;
            case "15m" -> M15;
            case "30m" -> M30;
            case "1h" -> H1;
            default -> throw new IllegalArgumentException("Unknown candle interval: " + label);
        };
    }

    /**
     * Safely converts a string to CandleInterval, returning null if invalid.
     *
     * @param label String label (e.g., "1m", "5m", "1h")
     * @return corresponding CandleInterval enum, or null if not recognized
     */
    public static CandleInterval fromStringOrNull(String label) {
        try {
            return fromString(label);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
