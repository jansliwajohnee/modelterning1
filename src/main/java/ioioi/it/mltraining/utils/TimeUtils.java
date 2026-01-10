package ioioi.it.mltraining.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class TimeUtils {

    public static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    public static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static ZonedDateTime longToZonedDateTime(String timestamp) {
        return longToZonedDateTime(Long.parseLong(timestamp));
    }

    public static ZonedDateTime longToZonedDateTime(long timestamp) {
//        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp * 1000), ZoneId.of("UTC"));
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.of("UTC"));
    }

    public static ZonedDateTime addDuration(ZonedDateTime dateTime, Duration duration) {
        return dateTime.plus(duration);
    }

    public static ZonedDateTime dateToZonedDateTime(Date date) {
        Instant instant = date.toInstant();
        ZoneId zoneId = TimeZone.getTimeZone("UTC").toZoneId();

        return ZonedDateTime.ofInstant(instant, zoneId);
    }

    public static Date zoneToDate(ZonedDateTime zoned) {
        return Date.from(zoned.toInstant());
//        return Date.from(zoned.withZoneSameInstant(java.time.ZoneOffset.UTC).toInstant());
    }

    public static Date roundToNearestMinute(ZonedDateTime zonedDateTime) {
        // Konwersja ZonedDateTime na UTC
        ZonedDateTime utcTime = zonedDateTime.withZoneSameInstant(ZoneId.of("UTC"));

        // Konwersja ZonedDateTime na GregorianCalendar
        GregorianCalendar calendar = GregorianCalendar.from(utcTime);

        // Zaokrąglenie sekund
        int seconds = calendar.get(Calendar.SECOND);
        if (seconds < 30) {
            calendar.set(Calendar.SECOND, 0);
        } else {
            calendar.add(Calendar.MINUTE, 1);
            calendar.set(Calendar.SECOND, 0);
        }

        return calendar.getTime();
    }

    public static Date roundToNearestMinute(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);

        int seconds = calendar.get(Calendar.SECOND);
        if (seconds < 30) {
            calendar.set(Calendar.SECOND, 0);
        } else {
            calendar.add(Calendar.MINUTE, 1);
            calendar.set(Calendar.SECOND, 0);
        }

        return calendar.getTime();
    }

    public static Date requestDate(String stringDate) {
        try {
            return SDF.parse(stringDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        throw new RuntimeException();
    }

    public static ZonedDateTime plusSeconds(ZonedDateTime time, long seconds) {
        return time.plusSeconds(seconds);
    }

    public static Long getDateMinusMinutesInMilliseconds(int minutes) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime dateFrom2000MinutesAgo = now.minusMinutes(minutes).truncatedTo(ChronoUnit.MINUTES);

        return dateFrom2000MinutesAgo.toEpochSecond(ZoneOffset.UTC) * 1000;
    }

    public static Date longToDate(Long time) {
        return new Date(time * 1000);
    }
}
