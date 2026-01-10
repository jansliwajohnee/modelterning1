package ioioi.it.mltraining.mapper;


import ioioi.it.mltraining.domain.CandlestickInterval;

import java.time.Duration;

public class IntervalMapper {

    public static CandlestickInterval map(String intervalCode) {
        return switch (intervalCode) {
            case "1m", "1" -> CandlestickInterval.ONE_MINUTE;
            case "3m", "3" -> CandlestickInterval.THREE_MINUTES;
            case "5m", "5" -> CandlestickInterval.FIVE_MINUTES;
            case "15m", "15" -> CandlestickInterval.FIFTEEN_MINUTES;
            case "30m", "30" -> CandlestickInterval.HALF_HOURLY;
            case "1h", "60" -> CandlestickInterval.HOURLY;
            case "2h", "120" -> CandlestickInterval.TWO_HOURLY;
            case "4h", "240" -> CandlestickInterval.FOUR_HOURLY;
            case "6h", "360" -> CandlestickInterval.SIX_HOURLY;
            case "8h", "480" -> CandlestickInterval.EIGHT_HOURLY;
            case "12h", "720" -> CandlestickInterval.TWELVE_HOURLY;
            case "1d", "1440" -> CandlestickInterval.DAILY;
            default -> CandlestickInterval.ONE_MINUTE;
        };
    }

    public static int getMinutes(String intervalCode) {
        return switch (intervalCode) {
            case "1m" -> 1;
            case "3m" -> 3;
            case "5m" -> 5;
            case "30m" -> 30;
            case "1h" -> 60;
            case "2h" -> 120;
            case "3h" -> 180;
            case "4h" -> 240;
            case "6h" -> 360;
            case "8h" -> 480;
            case "12h" -> 720;
            case "1d" -> 1440;
            default -> 15;
        };
    }

    public static int getMinutes(CandlestickInterval interval) {
        return switch (interval) {
            case THREE_MINUTES -> 3;
            case FIVE_MINUTES -> 5;
            case FIFTEEN_MINUTES -> 15;
            case HALF_HOURLY -> 30;
            case HOURLY -> 60;
            default -> 1;
        };
    }

    public static Duration mapToDuration(CandlestickInterval interval) {
        if (CandlestickInterval.ONE_MINUTE.equals(interval)) {
            return Duration.ofMinutes(1);
        }

        return Duration.ofMinutes(getMinutes(interval));
    }
}
