package ioioi.it.mltraining.controller;

import ioioi.it.mltraining.service.TrainingDataService;
import ioioi.it.mltraining.service.TrainingDataService.TrainingDataResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/training")
@RequiredArgsConstructor
public class TrainingDataController {

    private final TrainingDataService trainingDataService;

    @PostMapping("/generate")
    public ResponseEntity<TrainingDataResult> generateTrainingData(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "10") int featuresCount,
            @RequestParam(required = false) List<String> targets,
            @RequestParam(defaultValue = "10") int recordsCount) {

        TrainingDataResult result = trainingDataService.generateTrainingData(
                symbol, featuresCount, targets, recordsCount);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/features")
    public ResponseEntity<List<String>> getAvailableFeatures() {
        return ResponseEntity.ok(trainingDataService.getAvailableFeatures());
    }

    @GetMapping("/targets")
    public ResponseEntity<List<String>> getAvailableTargets() {
        return ResponseEntity.ok(trainingDataService.getAvailableTargets());
    }
}
