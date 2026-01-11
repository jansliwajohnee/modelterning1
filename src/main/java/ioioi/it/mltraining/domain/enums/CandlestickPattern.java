package ioioi.it.mltraining.domain.enums;

import ioioi.it.mltraining.utils.LocaleUtils;

/**
 * Candlestick Pattern Enum with hierarchical strength and reliability.
 *
 * Each pattern is classified by:
 * - Strength: EXTREME (100), STRONG (75), MODERATE (50), WEAK (25), NEUTRAL (0)
 * - SignalType: Type of market signal (bullish/bearish reversal/continuation)
 * - CandleCount: Number of candles required (1-5+)
 * - Reliability: Historical accuracy percentage (0-100%)
 */
public enum CandlestickPattern {

    // ============================================
    // 🔥 EXTREME - Najsilniejsze (100 punktów)
    // ============================================

    THREE_WHITE_SOLDIERS(StrengthLevel.EXTREME, SignalType.BULLISH_REVERSAL, 3, 85),
    THREE_BLACK_CROWS(StrengthLevel.EXTREME, SignalType.BEARISH_REVERSAL, 3, 85),
    BULLISH_KICKER(StrengthLevel.EXTREME, SignalType.BULLISH_REVERSAL, 2, 80),
    BEARISH_KICKER(StrengthLevel.EXTREME, SignalType.BEARISH_REVERSAL, 2, 80),
    MORNING_STAR(StrengthLevel.EXTREME, SignalType.BULLISH_REVERSAL, 3, 78),
    EVENING_STAR(StrengthLevel.EXTREME, SignalType.BEARISH_REVERSAL, 3, 78),

    // ============================================
    // 💪 STRONG - Silne (75 punktów)
    // ============================================

    BULLISH_ENGULFING(StrengthLevel.STRONG, SignalType.BULLISH_REVERSAL, 2, 75),
    BEARISH_ENGULFING(StrengthLevel.STRONG, SignalType.BEARISH_REVERSAL, 2, 75),
    MORNING_DOJI_STAR(StrengthLevel.STRONG, SignalType.BULLISH_REVERSAL, 3, 74),
    EVENING_DOJI_STAR(StrengthLevel.STRONG, SignalType.BEARISH_REVERSAL, 3, 74),
    PIERCING_PATTERN(StrengthLevel.STRONG, SignalType.BULLISH_REVERSAL, 2, 70),
    DARK_CLOUD_COVER(StrengthLevel.STRONG, SignalType.BEARISH_REVERSAL, 2, 70),
    THREE_INSIDE_UP(StrengthLevel.STRONG, SignalType.BULLISH_REVERSAL, 3, 72),
    THREE_INSIDE_DOWN(StrengthLevel.STRONG, SignalType.BEARISH_REVERSAL, 3, 72),
    THREE_OUTSIDE_UP(StrengthLevel.STRONG, SignalType.BULLISH_REVERSAL, 3, 73),
    THREE_OUTSIDE_DOWN(StrengthLevel.STRONG, SignalType.BEARISH_REVERSAL, 3, 73),
    ABANDONED_BABY_TOP(StrengthLevel.STRONG, SignalType.BEARISH_REVERSAL, 3, 76),
    ABANDONED_BABY_BOTTOM(StrengthLevel.STRONG, SignalType.BULLISH_REVERSAL, 3, 76),

    // ============================================
    // ⚖️ MODERATE - Umiarkowane (50 punktów)
    // ============================================

    HAMMER(StrengthLevel.MODERATE, SignalType.BULLISH_REVERSAL, 1, 60),
    SHOOTING_STAR(StrengthLevel.MODERATE, SignalType.BEARISH_REVERSAL, 1, 60),
    INVERTED_HAMMER(StrengthLevel.MODERATE, SignalType.BULLISH_REVERSAL, 1, 55),
    HANGING_MAN(StrengthLevel.MODERATE, SignalType.BEARISH_REVERSAL, 1, 55),
    BULLISH_HARAMI(StrengthLevel.MODERATE, SignalType.BULLISH_REVERSAL, 2, 58),
    BEARISH_HARAMI(StrengthLevel.MODERATE, SignalType.BEARISH_REVERSAL, 2, 58),
    TWEEZER_BOTTOM(StrengthLevel.MODERATE, SignalType.BULLISH_REVERSAL, 2, 62),
    TWEEZER_TOP(StrengthLevel.MODERATE, SignalType.BEARISH_REVERSAL, 2, 62),
    DRAGONFLY_DOJI(StrengthLevel.MODERATE, SignalType.BULLISH_REVERSAL, 1, 55),
    GRAVESTONE_DOJI(StrengthLevel.MODERATE, SignalType.BEARISH_REVERSAL, 1, 55),
    BULLISH_MARUBOZU(StrengthLevel.MODERATE, SignalType.BULLISH_CONTINUATION, 1, 65),
    BEARISH_MARUBOZU(StrengthLevel.MODERATE, SignalType.BEARISH_CONTINUATION, 1, 65),
    RISING_THREE_METHODS(StrengthLevel.MODERATE, SignalType.BULLISH_CONTINUATION, 5, 68),
    FALLING_THREE_METHODS(StrengthLevel.MODERATE, SignalType.BEARISH_CONTINUATION, 5, 68),
    PINBAR(StrengthLevel.MODERATE, SignalType.BULLISH_REVERSAL, 1, 62),
    BULLISH_THREE_LINE_STRIKE(StrengthLevel.MODERATE, SignalType.BULLISH_REVERSAL, 3, 66),
    BEARISH_THREE_LINE_STRIKE(StrengthLevel.MODERATE, SignalType.BEARISH_REVERSAL, 3, 66),
    UPSIDE_TASUKI_GAP(StrengthLevel.MODERATE, SignalType.BULLISH_CONTINUATION, 3, 60),
    DOWNSIDE_TASUKI_GAP(StrengthLevel.MODERATE, SignalType.BEARISH_CONTINUATION, 3, 60),

    // ============================================
    // 🔻 WEAK - Słabe (25 punktów)
    // ============================================

    DOJI(StrengthLevel.WEAK, SignalType.INDECISION, 1, 40),
    LONG_LEGGED_DOJI(StrengthLevel.WEAK, SignalType.INDECISION, 1, 42),
    BULLISH_SPINNING_TOP(StrengthLevel.WEAK, SignalType.BULLISH_REVERSAL, 1, 35),
    BEARISH_SPINNING_TOP(StrengthLevel.WEAK, SignalType.BEARISH_REVERSAL, 1, 35),
    HIGH_WAVE(StrengthLevel.WEAK, SignalType.INDECISION, 1, 38),
    INSIDE_BAR(StrengthLevel.WEAK, SignalType.NEUTRAL, 2, 45),
    FOUR_PRICE_DOJI(StrengthLevel.WEAK, SignalType.INDECISION, 1, 30),
    RISING_WINDOW(StrengthLevel.WEAK, SignalType.BULLISH_CONTINUATION, 2, 48),
    FALLING_WINDOW(StrengthLevel.WEAK, SignalType.BEARISH_CONTINUATION, 2, 48),
    SEPARATING_LINES(StrengthLevel.WEAK, SignalType.NEUTRAL, 2, 42),
    OUTSIDE_BAR(StrengthLevel.WEAK, SignalType.NEUTRAL, 2, 40),
    EQUAL_HIGHS(StrengthLevel.WEAK, SignalType.NEUTRAL, 2, 38),
    EQUAL_LOWS(StrengthLevel.WEAK, SignalType.NEUTRAL, 2, 38),
    FAKEY(StrengthLevel.WEAK, SignalType.NEUTRAL, 2, 44),

    // ============================================
    // RARE/ADVANCED PATTERNS (Various strengths)
    // ============================================

    MAT_HOLD(StrengthLevel.STRONG, SignalType.BULLISH_CONTINUATION, 5, 72),
    LADDER_BOTTOM(StrengthLevel.STRONG, SignalType.BULLISH_REVERSAL, 5, 74),
    LADDER_TOP(StrengthLevel.STRONG, SignalType.BEARISH_REVERSAL, 5, 74),
    THREE_STARS_IN_THE_SOUTH(StrengthLevel.MODERATE, SignalType.BULLISH_REVERSAL, 3, 64),
    UNIQUE_THREE_RIVER_BOTTOM(StrengthLevel.MODERATE, SignalType.BULLISH_REVERSAL, 3, 62),
    CONCEALING_BABY_SWALLOW(StrengthLevel.MODERATE, SignalType.BULLISH_REVERSAL, 4, 66),
    BREAKAWAY_BULLISH(StrengthLevel.MODERATE, SignalType.BULLISH_REVERSAL, 5, 65),
    BREAKAWAY_BEARISH(StrengthLevel.MODERATE, SignalType.BEARISH_REVERSAL, 5, 65),

    // ============================================
    // CONTINUATION PATTERNS
    // ============================================

    RISING_WEDGE(StrengthLevel.WEAK, SignalType.BEARISH_REVERSAL, 5, 52),
    FALLING_WEDGE(StrengthLevel.WEAK, SignalType.BULLISH_REVERSAL, 5, 52),
    PENNANT(StrengthLevel.WEAK, SignalType.NEUTRAL, 5, 50),
    FLAG(StrengthLevel.WEAK, SignalType.NEUTRAL, 4, 48),

    // ============================================
    // ⚪ NEUTRAL (0 punktów)
    // ============================================

    NONE(StrengthLevel.NONE, SignalType.NEUTRAL, 0, 0);

    private final StrengthLevel strength;
    private final SignalType signalType;
    private final int candleCount;
    private final int reliabilityPercent; // 0-100%

    CandlestickPattern(StrengthLevel strength, SignalType signalType, int candleCount, int reliabilityPercent) {
        this.strength = strength;
        this.signalType = signalType;
        this.candleCount = candleCount;
        this.reliabilityPercent = reliabilityPercent;
    }

    /**
     * Returns the strength level of this pattern.
     */
    public StrengthLevel getStrengthLevel() {
        return strength;
    }

    /**
     * Returns the signal type of this pattern.
     */
    public SignalType getSignalType() {
        return signalType;
    }

    /**
     * Returns the number of candles required for this pattern.
     */
    public int getCandleCount() {
        return candleCount;
    }

    /**
     * Returns the reliability percentage (0-100).
     */
    public int getReliabilityPercent() {
        return reliabilityPercent;
    }

    /**
     * Returns the weight/score for this pattern based on strength.
     * Uses the StrengthLevel's built-in weight calculation.
     *
     * @return weight score (0-100)
     */
    public int getWeight() {
        return strength.getWeight();
    }

    /**
     * Returns the base score: Strength × Reliability.
     */
    public double getBaseScore() {
        return getWeight() * (reliabilityPercent / 100.0);
    }

    /**
     * Returns the localized description of the candlestick pattern.
     * The description is retrieved from messages.properties based on the current locale.
     *
     * @return localized pattern description
     */
    public String getInfo() {
        return LocaleUtils.getMessage("candlestick.pattern." + this.name());
    }

    /**
     * Returns the strength level of the candlestick pattern (localized string).
     * Possible values: Extreme, Strong, Moderate, Weak, None
     *
     * @return pattern strength (localized)
     */
    public String getStrength() {
        return strength.getLabel();
    }

    /**
     * Returns the signal type of the candlestick pattern (localized string).
     * Examples: Bullish Reversal, Bearish Reversal, Continuation, Indecision
     *
     * @return pattern signal type (localized)
     */
    public String getSignal() {
        return signalType.getLabel();
    }

    /**
     * Returns the reliability level of the candlestick pattern (mapped from percent).
     * Uses guard clauses for cleaner categorization.
     * Possible values: Very High, High, Medium, Low, None
     *
     * @return pattern reliability label
     */
    public String getReliability() {
        if (reliabilityPercent >= 75) return "Very High";
        if (reliabilityPercent >= 60) return "High";
        if (reliabilityPercent >= 45) return "Medium";
        if (reliabilityPercent >= 30) return "Low";
        return "None";
    }

    /**
     * Determines if this pattern is bullish (reversal or continuation).
     *
     * @return true if pattern signals upward movement
     */
    public boolean isBullish() {
        Boolean bullishFlag = signalType.isBullish();
        return bullishFlag != null && bullishFlag;
    }

    /**
     * Determines if this pattern is bearish (reversal or continuation).
     *
     * @return true if pattern signals downward movement
     */
    public boolean isBearish() {
        Boolean bullishFlag = signalType.isBullish();
        return bullishFlag != null && !bullishFlag;
    }

    /**
     * Checks if this pattern is significant (moderate strength or higher).
     *
     * @return true if pattern has moderate, strong, or extreme strength
     */
    public boolean isSignificant() {
        return strength.isSignificant();
    }

    /**
     * Gets pattern category for grouping and filtering.
     * Uses switch expression for exhaustive matching.
     *
     * @return category name (REVERSAL, CONTINUATION, INDECISION, NEUTRAL)
     */
    public String getCategory() {
        return switch (signalType) {
            case BULLISH_REVERSAL, BEARISH_REVERSAL -> "REVERSAL";
            case BULLISH_CONTINUATION, BEARISH_CONTINUATION -> "CONTINUATION";
            case INDECISION -> "INDECISION";
            case NEUTRAL -> "NEUTRAL";
        };
    }

    /**
     * Returns the prediction/forecast associated with the candlestick pattern.
     * Describes what the pattern typically indicates for price movement.
     *
     * @return pattern prediction (localized)
     */
    public String getPrediction() {
        return LocaleUtils.getMessage("candlestick.pattern." + this.name() + ".prediction");
    }
}
