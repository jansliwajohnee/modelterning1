package ioioi.it.mltraining.domain.enums;

/**
 * Enum representing the accuracy of AI predictions
 */
public enum PredictionAccuracy {
    CORRECT,      // Prediction was correct
    INCORRECT,    // Prediction was incorrect
    PARTIAL,      // Prediction was partially correct
    NO_PREVIOUS   // No previous prediction to evaluate
}
