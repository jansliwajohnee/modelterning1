package ioioi.it.mltraining.controller;

import ioioi.it.mltraining.domain.CandleDTO;
import ioioi.it.mltraining.service.CandleRawService;
import ioioi.it.mltraining.service.HistoricalCandleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/candles")
@RequiredArgsConstructor
public class HistoricalCandleController {

    private final HistoricalCandleService historicalCandleService;
    private final CandleRawService candleRawService;

    @GetMapping("/historical")
    public ResponseEntity<List<CandleDTO>> getHistoricalCandles(
            @RequestParam String symbol,
            @RequestParam String interval,
            @RequestParam int numberOfCandles) {

        List<CandleDTO> candles = historicalCandleService.getHistoricalCandles(symbol, interval, numberOfCandles);
        return ResponseEntity.ok(candles);
    }

    @PostMapping("/historical/save")
    public ResponseEntity<CandleRawService.FetchAndSaveResult> fetchAndSaveHistoricalCandles(
            @RequestParam String symbol,
            @RequestParam String interval,
            @RequestParam(required = false) Integer numberOfCandles) {

        CandleRawService.FetchAndSaveResult result =
                candleRawService.fetchAndSaveCandles(symbol, interval, numberOfCandles);

        return ResponseEntity.ok(result);
    }
}
