package ioioi.it.mltraining.service;

import ioioi.it.mltraining.domain.CandleDTO;
import ioioi.it.mltraining.mapper.CandleMapper;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HistoricalCandleService {

    private final OkHttpClient httpClient = new OkHttpClient();

    public List<CandleDTO> getHistoricalCandles(String symbol, String interval, int numberOfCandles) {
        String baseUrl = "https://api.bybit.com/v5/market/kline";
        List<CandleDTO> allCandles = new ArrayList<>();

        // Konwersja interwału na milisekundy (np. 60 minut = 3600 * 1000 ms)
        long intervalMs = Long.parseLong(interval) * 60 * 1000L;

        // Obliczamy ile świec może się zmieścić w jednym zapytaniu (max 200)
        int maxCandlesPerRequest = 200;

        // Pobieramy aktualny czas - startujemy od teraz

        ZonedDateTime startTime = ZonedDateTime.now(ZoneOffset.UTC).minus(Duration.ofMillis(intervalMs * numberOfCandles));

        while (allCandles.size() < numberOfCandles) {
            if(allCandles.size() % 10000 == 0) {
                System.out.println("Candles: " + allCandles.size());
            }
            // Obliczamy endTime dla tego zapytania: startTime + 200 * interval (jeśli potrzebujemy mniej, to dostosujemy liczbę świec)
            ZonedDateTime endTime = startTime.plus(Duration.ofMillis(intervalMs * Math.min(maxCandlesPerRequest, numberOfCandles - allCandles.size())));

            // Tworzenie URL z odpowiednimi parametrami
            String url = String.format("%s?symbol=%s&interval=%s&startTime=%d&endTime=%d",
                    baseUrl, symbol, interval, startTime.toInstant().toEpochMilli(), endTime.toInstant().toEpochMilli());

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

                List<CandleDTO> candles = CandleMapper.map(response.body().string(), interval);
                allCandles.addAll(candles);

                // Jeśli dostaliśmy mniej świec niż maxCandlesPerRequest, to znaczy, że doszliśmy do końca danych
                if (candles.size() < maxCandlesPerRequest) {
                    break;
                }

                // Ustaw nowy startTime na endTime + 1 ms, żeby kontynuować pobieranie świec
                startTime = endTime.plusNanos(1);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Sortowanie i zwracanie tylko żądanej liczby świec
        return allCandles.stream()
                .sorted(Comparator.comparing(CandleDTO::openTime))
                .limit(numberOfCandles)
                .toList();
    }

    public List<CandleDTO> getHistoricalCandlesFromTime(String symbol, String interval, ZonedDateTime fromTime) {
        String baseUrl = "https://api.bybit.com/v5/market/kline";
        List<CandleDTO> allCandles = new ArrayList<>();

        // Konwersja interwału na milisekundy (np. 60 minut = 3600 * 1000 ms)
        long intervalMs = Long.parseLong(interval) * 60 * 1000L;

        // Obliczamy ile świec może się zmieścić w jednym zapytaniu (max 200)
        int maxCandlesPerRequest = 200;

        ZonedDateTime startTime = fromTime;
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);

        while (startTime.isBefore(now)) {
            if(allCandles.size() % 10000 == 0) {
                System.out.println("Candles: " + allCandles.size());
            }

            // Obliczamy endTime dla tego zapytania
            ZonedDateTime endTime = startTime.plus(Duration.ofMillis(intervalMs * maxCandlesPerRequest));
            if (endTime.isAfter(now)) {
                endTime = now;
            }

            // Tworzenie URL z odpowiednimi parametrami
            String url = String.format("%s?symbol=%s&interval=%s&startTime=%d&endTime=%d",
                    baseUrl, symbol, interval, startTime.toInstant().toEpochMilli(), endTime.toInstant().toEpochMilli());

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

                List<CandleDTO> candles = CandleMapper.map(response.body().string(), interval);
                allCandles.addAll(candles);

                // Jeśli dostaliśmy mniej świec niż maxCandlesPerRequest, to znaczy, że doszliśmy do końca danych
                if (candles.isEmpty() || candles.size() < maxCandlesPerRequest) {
                    break;
                }

                // Ustaw nowy startTime na endTime + 1 interwał, żeby kontynuować pobieranie świec
                startTime = endTime.plus(Duration.ofMillis(intervalMs));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Sortowanie
        return allCandles.stream()
                .sorted(Comparator.comparing(CandleDTO::openTime))
                .toList();
    }
}
