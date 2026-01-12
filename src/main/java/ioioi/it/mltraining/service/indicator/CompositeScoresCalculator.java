package ioioi.it.mltraining.service.indicator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

/**
 * Service for calculating composite score indicators.
 * All indicators are normalized/scaled for LightGBM model training.
 *
 * <p>Composite scores combine multiple indicators to measure overall market conditions
 * like trend strength, pattern confirmation, and momentum agreement.</p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CompositeScoresCalculator {

    private final TrendIndicatorCalculator trendCalculator;
    private final MomentumIndicatorCalculator momentumCalculator;
    private final VolatilityIndicatorCalculator volatilityCalculator;
    private final VolumeIndicatorCalculator volumeCalculator;
    private final PriceActionIndicatorCalculator priceActionCalculator;

    /**
     * Calculates all composite scores for a specific bar index in the series.
     *
     * @param series the bar series with OHLCV data
     * @param index the index of the bar to calculate indicators for
     * @return CompositeScores record with all calculated values (normalized for LightGBM)
     */
    public CompositeScores calculate(BarSeries series, int index) {
        if (series == null || index < 0 || index >= series.getBarCount()) {
            log.warn("Invalid series or index: index={}, barCount={}", index,
                    series != null ? series.getBarCount() : 0);
            return createEmptyScores();
        }

        // Calculate component indicators
        TrendIndicators trend = trendCalculator.calculate(series, index);
        MomentumIndicators momentum = momentumCalculator.calculate(series, index);
        VolatilityIndicators volatility = volatilityCalculator.calculate(series, index);
        VolumeIndicators volume = volumeCalculator.calculate(series, index);
        PriceActionIndicators priceAction = priceActionCalculator.calculate(series, index);

        // Calculate composite scores
        Double trendScore = calculateTrendScore(trend, momentum);
        Double bullishPatternsRatio = calculateBullishPatternsRatio(trend, momentum, priceAction);
        Double momentumAgreement = calculateMomentumAgreement(momentum);
        Double volatilityVsTrend = calculateVolatilityVsTrend(trend, volatility);
        Boolean volumeConfirmation = calculateVolumeConfirmation(volume, priceAction);

        return new CompositeScores(
                trendScore,
                bullishPatternsRatio,
                momentumAgreement,
                volatilityVsTrend,
                volumeConfirmation
        );
    }

    /**
     * Calculates composite trend score from multiple trend indicators.
     * Range: -1 to 1 (negative = bearish, positive = bullish)
     * Combines: EMA alignment, ADX, Aroon, MA slope
     */
    private Double calculateTrendScore(TrendIndicators trend, MomentumIndicators momentum) {
        int indicators = 0;
        double score = 0.0;

        // EMA 8 vs 21 (bullish if 8 > 21)
        if (trend.ema8Ema21SpreadPct() != null) {
            score += normalize(trend.ema8Ema21SpreadPct(), -5.0, 5.0);
            indicators++;
        }

        // ADX strength combined with DI direction
        if (trend.adx() != null && trend.diSpreadNormalized() != null) {
            double adxWeight = trend.adx() / 100.0; // 0-1
            score += trend.diSpreadNormalized() * adxWeight; // -1 to 1 weighted by strength
            indicators++;
        }

        // Aroon oscillator
        if (trend.aroonOscillator() != null) {
            score += trend.aroonOscillator() / 100.0; // -1 to 1
            indicators++;
        }

        // MA slope
        if (trend.maSlopePct() != null) {
            score += normalize(trend.maSlopePct(), -2.0, 2.0);
            indicators++;
        }

        return indicators > 0 ? score / indicators : null;
    }

    /**
     * Calculates ratio of bullish vs bearish patterns.
     * Range: 0-1 (0 = all bearish, 0.5 = neutral, 1 = all bullish)
     */
    private Double calculateBullishPatternsRatio(TrendIndicators trend, MomentumIndicators momentum, PriceActionIndicators priceAction) {
        int totalPatterns = 0;
        int bullishPatterns = 0;

        // Check bullish candle
        if (priceAction.isBullish() != null) {
            totalPatterns++;
            if (priceAction.isBullish()) bullishPatterns++;
        }

        // RSI above 50
        if (momentum.rsi14() != null) {
            totalPatterns++;
            if (momentum.rsi14() > 50.0) bullishPatterns++;
        }

        // Stochastic K above 50
        if (momentum.stochasticK() != null) {
            totalPatterns++;
            if (momentum.stochasticK() > 50.0) bullishPatterns++;
        }

        // Price above EMA 50
        if (trend.closeEma50DistancePct() != null) {
            totalPatterns++;
            if (trend.closeEma50DistancePct() > 0.0) bullishPatterns++;
        }

        // Aroon Up > Aroon Down
        if (trend.aroonUp() != null && trend.aroonDown() != null) {
            totalPatterns++;
            if (trend.aroonUp() > trend.aroonDown()) bullishPatterns++;
        }

        // Higher closes ratio
        if (priceAction.higherClosesRatio() != null) {
            totalPatterns++;
            if (priceAction.higherClosesRatio() > 0.5) bullishPatterns++;
        }

        return totalPatterns > 0 ? (double) bullishPatterns / totalPatterns : null;
    }

    /**
     * Calculates momentum agreement score.
     * Range: 0-1 (0 = diverging indicators, 1 = all agree)
     * Checks if multiple momentum indicators point in same direction
     */
    private Double calculateMomentumAgreement(MomentumIndicators momentum) {
        int indicators = 0;
        int agreements = 0;

        // Determine primary direction from RSI
        Boolean bullishDirection = null;
        if (momentum.rsi14() != null) {
            bullishDirection = momentum.rsi14() > 50.0;
        }

        if (bullishDirection == null) return null;

        // Check RSI
        if (momentum.rsi14() != null) {
            indicators++;
            if ((momentum.rsi14() > 50.0) == bullishDirection) agreements++;
        }

        // Check Stochastic K
        if (momentum.stochasticK() != null) {
            indicators++;
            if ((momentum.stochasticK() > 50.0) == bullishDirection) agreements++;
        }

        // Check Williams R
        if (momentum.williamsR() != null) {
            indicators++;
            if ((momentum.williamsR() > -50.0) == bullishDirection) agreements++;
        }

        // Check MFI
        if (momentum.mfi() != null) {
            indicators++;
            if ((momentum.mfi() > 50.0) == bullishDirection) agreements++;
        }

        // Check ROC
        if (momentum.rocPercent() != null) {
            indicators++;
            if ((momentum.rocPercent() > 0.0) == bullishDirection) agreements++;
        }

        return indicators > 0 ? (double) agreements / indicators : null;
    }

    /**
     * Calculates volatility vs trend ratio.
     * High ratio = High volatility with weak trend (choppy market)
     * Low ratio = Low volatility with strong trend (trending market)
     */
    private Double calculateVolatilityVsTrend(TrendIndicators trend, VolatilityIndicators volatility) {
        if (volatility.atrPct() == null || trend.adx() == null) {
            return null;
        }

        // Normalize ATR% (typical range 0-5%)
        double normalizedATR = Math.min(volatility.atrPct() / 5.0, 1.0);

        // Normalize ADX (0-100)
        double normalizedADX = trend.adx() / 100.0;

        // Avoid division by zero
        if (normalizedADX < 0.01) {
            return normalizedATR * 10.0; // High ratio when no trend
        }

        return normalizedATR / normalizedADX;
    }

    /**
     * Determines if volume confirms the price movement.
     * True if price moves with above-average volume
     */
    private Boolean calculateVolumeConfirmation(VolumeIndicators volume, PriceActionIndicators priceAction) {
        // Check if volume is above average (RVol > 1.0)
        if (volume.rvolSma20() == null || volume.rvolSma20() <= 1.0) {
            return false;
        }

        // Check if there's significant price movement
        if (priceAction.bodyPct() == null || priceAction.bodyPct() < 0.5) {
            return false;
        }

        // Volume confirms if volume is high and there's meaningful price movement
        return true;
    }

    /**
     * Normalizes value to range [-1, 1] given expected min/max.
     */
    private double normalize(double value, double min, double max) {
        if (max <= min) return 0.0;
        double normalized = (value - min) / (max - min); // 0 to 1
        return (normalized * 2.0) - 1.0; // -1 to 1
    }

    private CompositeScores createEmptyScores() {
        return new CompositeScores(null, null, null, null, null);
    }
}
