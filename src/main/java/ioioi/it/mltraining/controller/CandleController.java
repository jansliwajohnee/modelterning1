package ioioi.it.mltraining.controller;

import ioioi.it.mltraining.service.CandleGeneratorService;
import ioioi.it.mltraining.service.CandleGeneratorService.CandleGenerationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Candle generation operations.
 * Handles endpoints for generating enhanced Candle records with technical indicators
 * from raw CandleRaw data.
 */
@RestController
@RequestMapping("/api/candles")
@RequiredArgsConstructor
public class CandleController {

    private final CandleGeneratorService candleGeneratorService;

    /**
     * Generates enhanced Candle records with technical indicators from CandleRaw data.
     *
     * @param symbol symbol to generate candles for (e.g., "BTCUSDT")
     * @param interval candle interval (e.g., "1m", "5m", "1h")
     * @param limit optional limit of candles to process (defaults to all available)
     * @return generation result with statistics
     */
    @PostMapping("/generate")
    public ResponseEntity<CandleGenerationResult> generateCandles(
            @RequestParam String symbol,
            @RequestParam String interval,
            @RequestParam(required = false) Integer limit) {

        CandleGenerationResult result = candleGeneratorService.generateCandles(symbol, interval, limit);
        return ResponseEntity.ok(result);
    }
}