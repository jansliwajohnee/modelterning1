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
}
