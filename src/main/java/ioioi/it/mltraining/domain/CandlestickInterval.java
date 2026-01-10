package ioioi.it.mltraining.domain;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Enum dla przedziałów czasowych świec: 1min, 3min, 5min, 15min, 30min, 60min, 1day, 1mon, 1week, 1year
 */
public enum CandlestickInterval {
    ONE_MINUTE("1m", 1),
    THREE_MINUTES("3m", 3),
    FIVE_MINUTES("5m", 5),
    FIFTEEN_MINUTES("15m", 15),
    HALF_HOURLY("30m", 30),
    HOURLY("1h", 60),
    TWO_HOURLY("2h", 120),
    FOUR_HOURLY("4h", 240),
    SIX_HOURLY("6h", 360),
    EIGHT_HOURLY("8h", 480),
    TWELVE_HOURLY("12h", 720),
    DAILY("1d", 1440),
    THREE_DAILY("3d", 4320),
    WEEKLY("1w", 10080),
    MONTHLY("1M", 43200);

    private final String code;
    private final int durationInMinutes;

    // Mapa dla szybkiego wyszukiwania po kodzie i czasie trwania
    private static final Map<String, CandlestickInterval> BY_CODE = new HashMap<>();
    private static final Map<Integer, CandlestickInterval> BY_DURATION = new HashMap<>();

    static {
        // Wypełniamy mapy kodów i czasów trwania dla szybkiego wyszukiwania
        for (CandlestickInterval interval : values()) {
            BY_CODE.put(interval.code, interval);
            BY_DURATION.put(interval.durationInMinutes, interval);
        }
    }

    CandlestickInterval(String code, int durationInMinutes) {
        this.code = code;
        this.durationInMinutes = durationInMinutes;
    }

    /**
     * Metoda zwracająca długość przedziału czasowego świecy jako obiekt Duration.
     * @return obiekt Duration odpowiadający danemu przedziałowi czasowemu.
     */
    public Duration toDuration() {
        return Duration.ofMinutes(durationInMinutes);
    }

    @Override
    public String toString() {
        return code;
    }

    /**
     * Metoda statyczna do uzyskania CandlestickInterval z kodu ("1m") lub z wartości liczbowej ("1").
     * @param input wartość jako String, np. "1m" lub "1"
     * @return odpowiedni CandlestickInterval
     * @throws IllegalArgumentException jeśli nie ma dopasowania dla kodu lub czasu trwania
     */
    public static CandlestickInterval fromCodeOrDuration(String input) {
        // Najpierw próbujemy znaleźć po kodzie (np. "1m")
        CandlestickInterval interval = BY_CODE.get(input);
        if (interval != null) {
            return interval;
        }

        // Następnie próbujemy znaleźć po czasie trwania (np. "1")
        try {
            int duration = Integer.parseInt(input);
            interval = BY_DURATION.get(duration);
            if (interval != null) {
                return interval;
            }
        } catch (NumberFormatException e) {
            // Ignorujemy, jeśli nie udało się przekonwertować na liczbę
        }

        // Jeśli nie znaleziono, rzucamy wyjątek
        throw new IllegalArgumentException("Nieznany przedział czasowy: " + input);
    }

    public static int getDurationInMinutes(CandlestickInterval interval) {
        return interval.durationInMinutes;
    }
}