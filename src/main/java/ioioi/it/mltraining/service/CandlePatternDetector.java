package ioioi.it.mltraining.service;

import ioioi.it.mltraining.entity.CandlePattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;

import java.util.HashSet;
import java.util.Set;

/**
 * Service for detecting candlestick patterns in price data.
 * Implements detection logic for common bullish and bearish patterns.
 *
 * <p>Pattern strength ranges from -100 (strongly bearish) to +100 (strongly bullish).</p>
 */
@Service
@Slf4j
public class CandlePatternDetector {

    private static final double DOJI_THRESHOLD = 0.1; // Body < 10% of range
    private static final double LONG_SHADOW_RATIO = 2.0; // Shadow >= 2x body
    private static final double LARGE_BODY_THRESHOLD = 0.7; // Body >= 70% of range

    /**
     * Detects all candlestick patterns for a specific bar index.
     *
     * @param series the bar series
     * @param index the bar index to analyze
     * @param candleId the Candle entity ID (for FK relationship)
     * @return set of detected patterns with their strengths
     */
    public Set<CandlePattern> detectPatterns(BarSeries series, int index, Long candleId) {
        if (series == null || index < 0 || index >= series.getBarCount()) {
            log.warn("Invalid series or index for pattern detection: index={}, barCount={}",
                    index, series != null ? series.getBarCount() : 0);
            return new HashSet<>();
        }

        Set<CandlePattern> patterns = new HashSet<>();

        // Single candle patterns
        detectDoji(series, index, candleId).ifPresent(patterns::add);
        detectDragonflyDoji(series, index, candleId).ifPresent(patterns::add);
        detectGravestoneDoji(series, index, candleId).ifPresent(patterns::add);
        detectHammer(series, index, candleId).ifPresent(patterns::add);
        detectHangingMan(series, index, candleId).ifPresent(patterns::add);
        detectShootingStar(series, index, candleId).ifPresent(patterns::add);
        detectInvertedHammer(series, index, candleId).ifPresent(patterns::add);
        detectMarubozu(series, index, candleId).ifPresent(patterns::add);
        detectSpinningTop(series, index, candleId).ifPresent(patterns::add);
        detectHighWave(series, index, candleId).ifPresent(patterns::add);
        detectBeltHold(series, index, candleId).ifPresent(patterns::add);

        // Two candle patterns
        if (index >= 1) {
            detectEngulfing(series, index, candleId).ifPresent(patterns::add);
            detectHarami(series, index, candleId).ifPresent(patterns::add);
            detectPiercingPattern(series, index, candleId).ifPresent(patterns::add);
            detectDarkCloudCover(series, index, candleId).ifPresent(patterns::add);
            detectTweezer(series, index, candleId).ifPresent(patterns::add);
            detectKicking(series, index, candleId).ifPresent(patterns::add);
            detectMeetingLines(series, index, candleId).ifPresent(patterns::add);
            detectSeparatingLines(series, index, candleId).ifPresent(patterns::add);
            detectCounterattack(series, index, candleId).ifPresent(patterns::add);
        }

        // Three candle patterns
        if (index >= 2) {
            detectMorningStar(series, index, candleId).ifPresent(patterns::add);
            detectEveningStar(series, index, candleId).ifPresent(patterns::add);
            detectThreeWhiteSoldiers(series, index, candleId).ifPresent(patterns::add);
            detectThreeBlackCrows(series, index, candleId).ifPresent(patterns::add);
            detectThreeInsideUpDown(series, index, candleId).ifPresent(patterns::add);
            detectThreeOutsideUpDown(series, index, candleId).ifPresent(patterns::add);
            detectAbandonedBaby(series, index, candleId).ifPresent(patterns::add);
            detectTriStar(series, index, candleId).ifPresent(patterns::add);
        }

        // Five candle patterns
        if (index >= 4) {
            detectRisingThreeMethods(series, index, candleId).ifPresent(patterns::add);
            detectFallingThreeMethods(series, index, candleId).ifPresent(patterns::add);
        }

        return patterns;
    }

    /**
     * Doji: Open ≈ Close (small body)
     * Strength: 0 (neutral, indicates indecision)
     */
    private Optional<CandlePattern> detectDoji(BarSeries series, int index, Long candleId) {
        Bar bar = series.getBar(index);
        double open = bar.getOpenPrice().doubleValue();
        double close = bar.getClosePrice().doubleValue();
        double high = bar.getHighPrice().doubleValue();
        double low = bar.getLowPrice().doubleValue();
        double range = high - low;

        if (range == 0) return Optional.empty();

        double bodySize = Math.abs(close - open);
        double bodyRatio = bodySize / range;

        if (bodyRatio <= DOJI_THRESHOLD) {
            return Optional.of(new CandlePattern(null, candleId, "doji", 0.0));
        }

        return Optional.empty();
    }

    /**
     * Hammer: Small body at top, long lower shadow (bullish reversal)
     * Strength: +60 to +80
     */
    private Optional<CandlePattern> detectHammer(BarSeries series, int index, Long candleId) {
        Bar bar = series.getBar(index);
        double open = bar.getOpenPrice().doubleValue();
        double close = bar.getClosePrice().doubleValue();
        double high = bar.getHighPrice().doubleValue();
        double low = bar.getLowPrice().doubleValue();
        double range = high - low;

        if (range == 0) return Optional.empty();

        double bodySize = Math.abs(close - open);
        double lowerShadow = Math.min(open, close) - low;
        double upperShadow = high - Math.max(open, close);

        // Hammer: long lower shadow, small upper shadow, small body
        if (lowerShadow >= LONG_SHADOW_RATIO * bodySize &&
            upperShadow < bodySize &&
            bodySize / range < 0.3) {

            // Check if in downtrend (requires context)
            boolean inDowntrend = index >= 5 && isDowntrend(series, index, 5);
            double strength = inDowntrend ? 80.0 : 60.0;

            return Optional.of(new CandlePattern(null, candleId, "hammer", strength));
        }

        return Optional.empty();
    }

    /**
     * Hanging Man: Same as Hammer but appears in uptrend (bearish)
     * Strength: -60 to -80
     */
    private Optional<CandlePattern> detectHangingMan(BarSeries series, int index, Long candleId) {
        Bar bar = series.getBar(index);
        double open = bar.getOpenPrice().doubleValue();
        double close = bar.getClosePrice().doubleValue();
        double high = bar.getHighPrice().doubleValue();
        double low = bar.getLowPrice().doubleValue();
        double range = high - low;

        if (range == 0) return Optional.empty();

        double bodySize = Math.abs(close - open);
        double lowerShadow = Math.min(open, close) - low;
        double upperShadow = high - Math.max(open, close);

        if (lowerShadow >= LONG_SHADOW_RATIO * bodySize &&
            upperShadow < bodySize &&
            bodySize / range < 0.3) {

            boolean inUptrend = index >= 5 && isUptrend(series, index, 5);

            if (inUptrend) {
                return Optional.of(new CandlePattern(null, candleId, "hanging_man", -70.0));
            }
        }

        return Optional.empty();
    }

    /**
     * Shooting Star: Small body at bottom, long upper shadow (bearish)
     * Strength: -70 to -85
     */
    private Optional<CandlePattern> detectShootingStar(BarSeries series, int index, Long candleId) {
        Bar bar = series.getBar(index);
        double open = bar.getOpenPrice().doubleValue();
        double close = bar.getClosePrice().doubleValue();
        double high = bar.getHighPrice().doubleValue();
        double low = bar.getLowPrice().doubleValue();
        double range = high - low;

        if (range == 0) return Optional.empty();

        double bodySize = Math.abs(close - open);
        double upperShadow = high - Math.max(open, close);
        double lowerShadow = Math.min(open, close) - low;

        if (upperShadow >= LONG_SHADOW_RATIO * bodySize &&
            lowerShadow < bodySize &&
            bodySize / range < 0.3) {

            boolean inUptrend = index >= 5 && isUptrend(series, index, 5);
            double strength = inUptrend ? -85.0 : -70.0;

            return Optional.of(new CandlePattern(null, candleId, "shooting_star", strength));
        }

        return Optional.empty();
    }

    /**
     * Inverted Hammer: Same as Shooting Star but in downtrend (bullish)
     * Strength: +65 to +75
     */
    private Optional<CandlePattern> detectInvertedHammer(BarSeries series, int index, Long candleId) {
        Bar bar = series.getBar(index);
        double open = bar.getOpenPrice().doubleValue();
        double close = bar.getClosePrice().doubleValue();
        double high = bar.getHighPrice().doubleValue();
        double low = bar.getLowPrice().doubleValue();
        double range = high - low;

        if (range == 0) return Optional.empty();

        double bodySize = Math.abs(close - open);
        double upperShadow = high - Math.max(open, close);
        double lowerShadow = Math.min(open, close) - low;

        if (upperShadow >= LONG_SHADOW_RATIO * bodySize &&
            lowerShadow < bodySize &&
            bodySize / range < 0.3) {

            boolean inDowntrend = index >= 5 && isDowntrend(series, index, 5);

            if (inDowntrend) {
                return Optional.of(new CandlePattern(null, candleId, "inverted_hammer", 70.0));
            }
        }

        return Optional.empty();
    }

    /**
     * Marubozu: Large body with little or no shadows
     * Strength: +90 (bullish) or -90 (bearish)
     */
    private Optional<CandlePattern> detectMarubozu(BarSeries series, int index, Long candleId) {
        Bar bar = series.getBar(index);
        double open = bar.getOpenPrice().doubleValue();
        double close = bar.getClosePrice().doubleValue();
        double high = bar.getHighPrice().doubleValue();
        double low = bar.getLowPrice().doubleValue();
        double range = high - low;

        if (range == 0) return Optional.empty();

        double bodySize = Math.abs(close - open);
        double bodyRatio = bodySize / range;

        if (bodyRatio >= 0.95) {
            boolean bullish = close > open;
            double strength = bullish ? 90.0 : -90.0;
            String name = bullish ? "marubozu_bullish" : "marubozu_bearish";

            return Optional.of(new CandlePattern(null, candleId, name, strength));
        }

        return Optional.empty();
    }

    /**
     * Spinning Top: Small body with long shadows on both sides (indecision)
     * Strength: 0 (neutral)
     */
    private Optional<CandlePattern> detectSpinningTop(BarSeries series, int index, Long candleId) {
        Bar bar = series.getBar(index);
        double open = bar.getOpenPrice().doubleValue();
        double close = bar.getClosePrice().doubleValue();
        double high = bar.getHighPrice().doubleValue();
        double low = bar.getLowPrice().doubleValue();
        double range = high - low;

        if (range == 0) return Optional.empty();

        double bodySize = Math.abs(close - open);
        double upperShadow = high - Math.max(open, close);
        double lowerShadow = Math.min(open, close) - low;

        double bodyRatio = bodySize / range;

        if (bodyRatio < 0.3 && upperShadow > bodySize && lowerShadow > bodySize) {
            return Optional.of(new CandlePattern(null, candleId, "spinning_top", 0.0));
        }

        return Optional.empty();
    }

    /**
     * Engulfing Pattern: Current candle completely engulfs previous candle
     * Strength: +85 (bullish) or -85 (bearish)
     */
    private Optional<CandlePattern> detectEngulfing(BarSeries series, int index, Long candleId) {
        if (index < 1) return Optional.empty();

        Bar prev = series.getBar(index - 1);
        Bar curr = series.getBar(index);

        double prevOpen = prev.getOpenPrice().doubleValue();
        double prevClose = prev.getClosePrice().doubleValue();
        double currOpen = curr.getOpenPrice().doubleValue();
        double currClose = curr.getClosePrice().doubleValue();

        // Bullish engulfing
        if (prevClose < prevOpen && currClose > currOpen) {
            if (currOpen <= prevClose && currClose >= prevOpen) {
                return Optional.of(new CandlePattern(null, candleId, "engulfing_bullish", 85.0));
            }
        }

        // Bearish engulfing
        if (prevClose > prevOpen && currClose < currOpen) {
            if (currOpen >= prevClose && currClose <= prevOpen) {
                return Optional.of(new CandlePattern(null, candleId, "engulfing_bearish", -85.0));
            }
        }

        return Optional.empty();
    }

    /**
     * Harami: Small candle within previous large candle's body
     * Strength: +70 (bullish) or -70 (bearish)
     */
    private Optional<CandlePattern> detectHarami(BarSeries series, int index, Long candleId) {
        if (index < 1) return Optional.empty();

        Bar prev = series.getBar(index - 1);
        Bar curr = series.getBar(index);

        double prevOpen = prev.getOpenPrice().doubleValue();
        double prevClose = prev.getClosePrice().doubleValue();
        double currOpen = curr.getOpenPrice().doubleValue();
        double currClose = curr.getClosePrice().doubleValue();

        double prevBodySize = Math.abs(prevClose - prevOpen);
        double currBodySize = Math.abs(currClose - currOpen);

        // Current candle is small and within previous body
        if (currBodySize < prevBodySize * 0.5) {
            double prevHigh = Math.max(prevOpen, prevClose);
            double prevLow = Math.min(prevOpen, prevClose);
            double currHigh = Math.max(currOpen, currClose);
            double currLow = Math.min(currOpen, currClose);

            if (currHigh <= prevHigh && currLow >= prevLow) {
                // Bullish harami: prev bearish, curr bullish
                if (prevClose < prevOpen && currClose > currOpen) {
                    return Optional.of(new CandlePattern(null, candleId, "harami_bullish", 70.0));
                }
                // Bearish harami: prev bullish, curr bearish
                if (prevClose > prevOpen && currClose < currOpen) {
                    return Optional.of(new CandlePattern(null, candleId, "harami_bearish", -70.0));
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Piercing Pattern: Bullish reversal - current candle closes above midpoint of previous bearish candle
     * Strength: +80
     */
    private Optional<CandlePattern> detectPiercingPattern(BarSeries series, int index, Long candleId) {
        if (index < 1) return Optional.empty();

        Bar prev = series.getBar(index - 1);
        Bar curr = series.getBar(index);

        double prevOpen = prev.getOpenPrice().doubleValue();
        double prevClose = prev.getClosePrice().doubleValue();
        double currOpen = curr.getOpenPrice().doubleValue();
        double currClose = curr.getClosePrice().doubleValue();

        // Previous candle bearish, current bullish
        if (prevClose < prevOpen && currClose > currOpen) {
            double prevMidpoint = (prevOpen + prevClose) / 2.0;

            // Current opens below previous close and closes above midpoint
            if (currOpen < prevClose && currClose > prevMidpoint && currClose < prevOpen) {
                return Optional.of(new CandlePattern(null, candleId, "piercing_pattern", 80.0));
            }
        }

        return Optional.empty();
    }

    /**
     * Dark Cloud Cover: Bearish reversal - opposite of piercing pattern
     * Strength: -80
     */
    private Optional<CandlePattern> detectDarkCloudCover(BarSeries series, int index, Long candleId) {
        if (index < 1) return Optional.empty();

        Bar prev = series.getBar(index - 1);
        Bar curr = series.getBar(index);

        double prevOpen = prev.getOpenPrice().doubleValue();
        double prevClose = prev.getClosePrice().doubleValue();
        double currOpen = curr.getOpenPrice().doubleValue();
        double currClose = curr.getClosePrice().doubleValue();

        // Previous candle bullish, current bearish
        if (prevClose > prevOpen && currClose < currOpen) {
            double prevMidpoint = (prevOpen + prevClose) / 2.0;

            // Current opens above previous close and closes below midpoint
            if (currOpen > prevClose && currClose < prevMidpoint && currClose > prevOpen) {
                return Optional.of(new CandlePattern(null, candleId, "dark_cloud_cover", -80.0));
            }
        }

        return Optional.empty();
    }

    /**
     * Tweezer Top/Bottom: Two candles with matching highs (top) or lows (bottom)
     * Strength: +75 (bottom) or -75 (top)
     */
    private Optional<CandlePattern> detectTweezer(BarSeries series, int index, Long candleId) {
        if (index < 1) return Optional.empty();

        Bar prev = series.getBar(index - 1);
        Bar curr = series.getBar(index);

        double prevHigh = prev.getHighPrice().doubleValue();
        double prevLow = prev.getLowPrice().doubleValue();
        double currHigh = curr.getHighPrice().doubleValue();
        double currLow = curr.getLowPrice().doubleValue();

        double tolerance = (prevHigh - prevLow) * 0.01; // 1% tolerance

        // Tweezer Top
        if (Math.abs(prevHigh - currHigh) <= tolerance) {
            boolean inUptrend = index >= 5 && isUptrend(series, index, 5);
            if (inUptrend) {
                return Optional.of(new CandlePattern(null, candleId, "tweezer_top", -75.0));
            }
        }

        // Tweezer Bottom
        if (Math.abs(prevLow - currLow) <= tolerance) {
            boolean inDowntrend = index >= 5 && isDowntrend(series, index, 5);
            if (inDowntrend) {
                return Optional.of(new CandlePattern(null, candleId, "tweezer_bottom", 75.0));
            }
        }

        return Optional.empty();
    }

    /**
     * Morning Star: Three candle bullish reversal pattern
     * Strength: +90
     */
    private Optional<CandlePattern> detectMorningStar(BarSeries series, int index, Long candleId) {
        if (index < 2) return Optional.empty();

        Bar first = series.getBar(index - 2);
        Bar second = series.getBar(index - 1);
        Bar third = series.getBar(index);

        double firstOpen = first.getOpenPrice().doubleValue();
        double firstClose = first.getClosePrice().doubleValue();
        double secondOpen = second.getOpenPrice().doubleValue();
        double secondClose = second.getClosePrice().doubleValue();
        double thirdOpen = third.getOpenPrice().doubleValue();
        double thirdClose = third.getClosePrice().doubleValue();

        // First: large bearish
        if (firstClose >= firstOpen) return Optional.empty();

        // Second: small body (star)
        double secondBody = Math.abs(secondClose - secondOpen);
        double firstBody = Math.abs(firstClose - firstOpen);
        if (secondBody >= firstBody * 0.3) return Optional.empty();

        // Third: large bullish closing above first candle midpoint
        if (thirdClose <= thirdOpen) return Optional.empty();
        double firstMidpoint = (firstOpen + firstClose) / 2.0;
        if (thirdClose <= firstMidpoint) return Optional.empty();

        return Optional.of(new CandlePattern(null, candleId, "morning_star", 90.0));
    }

    /**
     * Evening Star: Three candle bearish reversal pattern
     * Strength: -90
     */
    private Optional<CandlePattern> detectEveningStar(BarSeries series, int index, Long candleId) {
        if (index < 2) return Optional.empty();

        Bar first = series.getBar(index - 2);
        Bar second = series.getBar(index - 1);
        Bar third = series.getBar(index);

        double firstOpen = first.getOpenPrice().doubleValue();
        double firstClose = first.getClosePrice().doubleValue();
        double secondOpen = second.getOpenPrice().doubleValue();
        double secondClose = second.getClosePrice().doubleValue();
        double thirdOpen = third.getOpenPrice().doubleValue();
        double thirdClose = third.getClosePrice().doubleValue();

        // First: large bullish
        if (firstClose <= firstOpen) return Optional.empty();

        // Second: small body (star)
        double secondBody = Math.abs(secondClose - secondOpen);
        double firstBody = Math.abs(firstClose - firstOpen);
        if (secondBody >= firstBody * 0.3) return Optional.empty();

        // Third: large bearish closing below first candle midpoint
        if (thirdClose >= thirdOpen) return Optional.empty();
        double firstMidpoint = (firstOpen + firstClose) / 2.0;
        if (thirdClose >= firstMidpoint) return Optional.empty();

        return Optional.of(new CandlePattern(null, candleId, "evening_star", -90.0));
    }

    /**
     * Three White Soldiers: Three consecutive bullish candles with higher closes
     * Strength: +95
     */
    private Optional<CandlePattern> detectThreeWhiteSoldiers(BarSeries series, int index, Long candleId) {
        if (index < 2) return Optional.empty();

        for (int i = index - 2; i <= index; i++) {
            Bar bar = series.getBar(i);
            double open = bar.getOpenPrice().doubleValue();
            double close = bar.getClosePrice().doubleValue();

            // All three must be bullish
            if (close <= open) return Optional.empty();

            // Each close should be higher than previous
            if (i > index - 2) {
                double prevClose = series.getBar(i - 1).getClosePrice().doubleValue();
                if (close <= prevClose) return Optional.empty();
            }
        }

        return Optional.of(new CandlePattern(null, candleId, "three_white_soldiers", 95.0));
    }

    /**
     * Three Black Crows: Three consecutive bearish candles with lower closes
     * Strength: -95
     */
    private Optional<CandlePattern> detectThreeBlackCrows(BarSeries series, int index, Long candleId) {
        if (index < 2) return Optional.empty();

        for (int i = index - 2; i <= index; i++) {
            Bar bar = series.getBar(i);
            double open = bar.getOpenPrice().doubleValue();
            double close = bar.getClosePrice().doubleValue();

            // All three must be bearish
            if (close >= open) return Optional.empty();

            // Each close should be lower than previous
            if (i > index - 2) {
                double prevClose = series.getBar(i - 1).getClosePrice().doubleValue();
                if (close >= prevClose) return Optional.empty();
            }
        }

        return Optional.of(new CandlePattern(null, candleId, "three_black_crows", -95.0));
    }

    /**
     * Dragonfly Doji: Doji with long lower shadow, no upper shadow (bullish reversal)
     * Strength: +75
     */
    private Optional<CandlePattern> detectDragonflyDoji(BarSeries series, int index, Long candleId) {
        Bar bar = series.getBar(index);
        double open = bar.getOpenPrice().doubleValue();
        double close = bar.getClosePrice().doubleValue();
        double high = bar.getHighPrice().doubleValue();
        double low = bar.getLowPrice().doubleValue();
        double range = high - low;

        if (range == 0) return Optional.empty();

        double bodySize = Math.abs(close - open);
        double lowerShadow = Math.min(open, close) - low;
        double upperShadow = high - Math.max(open, close);

        // Very small body, long lower shadow, minimal upper shadow
        if (bodySize / range <= DOJI_THRESHOLD &&
            lowerShadow >= range * 0.7 &&
            upperShadow <= range * 0.1) {

            boolean inDowntrend = index >= 5 && isDowntrend(series, index, 5);
            double strength = inDowntrend ? 75.0 : 60.0;

            return Optional.of(new CandlePattern(null, candleId, "dragonfly_doji", strength));
        }

        return Optional.empty();
    }

    /**
     * Gravestone Doji: Doji with long upper shadow, no lower shadow (bearish reversal)
     * Strength: -75
     */
    private Optional<CandlePattern> detectGravestoneDoji(BarSeries series, int index, Long candleId) {
        Bar bar = series.getBar(index);
        double open = bar.getOpenPrice().doubleValue();
        double close = bar.getClosePrice().doubleValue();
        double high = bar.getHighPrice().doubleValue();
        double low = bar.getLowPrice().doubleValue();
        double range = high - low;

        if (range == 0) return Optional.empty();

        double bodySize = Math.abs(close - open);
        double upperShadow = high - Math.max(open, close);
        double lowerShadow = Math.min(open, close) - low;

        // Very small body, long upper shadow, minimal lower shadow
        if (bodySize / range <= DOJI_THRESHOLD &&
            upperShadow >= range * 0.7 &&
            lowerShadow <= range * 0.1) {

            boolean inUptrend = index >= 5 && isUptrend(series, index, 5);
            double strength = inUptrend ? -75.0 : -60.0;

            return Optional.of(new CandlePattern(null, candleId, "gravestone_doji", strength));
        }

        return Optional.empty();
    }

    /**
     * High Wave Candle: Long upper and lower shadows with small body (extreme volatility)
     * Strength: 0 (neutral but significant)
     */
    private Optional<CandlePattern> detectHighWave(BarSeries series, int index, Long candleId) {
        Bar bar = series.getBar(index);
        double open = bar.getOpenPrice().doubleValue();
        double close = bar.getClosePrice().doubleValue();
        double high = bar.getHighPrice().doubleValue();
        double low = bar.getLowPrice().doubleValue();
        double range = high - low;

        if (range == 0) return Optional.empty();

        double bodySize = Math.abs(close - open);
        double upperShadow = high - Math.max(open, close);
        double lowerShadow = Math.min(open, close) - low;

        // Small body with both long shadows
        if (bodySize / range < 0.2 &&
            upperShadow >= range * 0.3 &&
            lowerShadow >= range * 0.3) {

            return Optional.of(new CandlePattern(null, candleId, "high_wave", 0.0));
        }

        return Optional.empty();
    }

    /**
     * Belt Hold: Large marubozu-like candle (opening on extreme)
     * Strength: +85 (bullish) or -85 (bearish)
     */
    private Optional<CandlePattern> detectBeltHold(BarSeries series, int index, Long candleId) {
        Bar bar = series.getBar(index);
        double open = bar.getOpenPrice().doubleValue();
        double close = bar.getClosePrice().doubleValue();
        double high = bar.getHighPrice().doubleValue();
        double low = bar.getLowPrice().doubleValue();
        double range = high - low;

        if (range == 0) return Optional.empty();

        double bodySize = Math.abs(close - open);

        // Bullish Belt Hold: opens on low, minimal lower shadow
        if (close > open) {
            double lowerShadow = open - low;
            if (bodySize / range >= 0.8 && lowerShadow / range <= 0.05) {
                boolean inDowntrend = index >= 5 && isDowntrend(series, index, 5);
                double strength = inDowntrend ? 85.0 : 70.0;
                return Optional.of(new CandlePattern(null, candleId, "belt_hold_bullish", strength));
            }
        }

        // Bearish Belt Hold: opens on high, minimal upper shadow
        if (close < open) {
            double upperShadow = high - open;
            if (bodySize / range >= 0.8 && upperShadow / range <= 0.05) {
                boolean inUptrend = index >= 5 && isUptrend(series, index, 5);
                double strength = inUptrend ? -85.0 : -70.0;
                return Optional.of(new CandlePattern(null, candleId, "belt_hold_bearish", strength));
            }
        }

        return Optional.empty();
    }

    /**
     * Kicking Pattern: Two opposite marubozu candles with gap
     * Strength: +95 (bullish) or -95 (bearish)
     */
    private Optional<CandlePattern> detectKicking(BarSeries series, int index, Long candleId) {
        if (index < 1) return Optional.empty();

        Bar prev = series.getBar(index - 1);
        Bar curr = series.getBar(index);

        double prevOpen = prev.getOpenPrice().doubleValue();
        double prevClose = prev.getClosePrice().doubleValue();
        double prevHigh = prev.getHighPrice().doubleValue();
        double prevLow = prev.getLowPrice().doubleValue();
        double prevRange = prevHigh - prevLow;

        double currOpen = curr.getOpenPrice().doubleValue();
        double currClose = curr.getClosePrice().doubleValue();
        double currHigh = curr.getHighPrice().doubleValue();
        double currLow = curr.getLowPrice().doubleValue();
        double currRange = currHigh - currLow;

        if (prevRange == 0 || currRange == 0) return Optional.empty();

        // Both must be marubozu-like
        double prevBody = Math.abs(prevClose - prevOpen);
        double currBody = Math.abs(currClose - currOpen);

        if (prevBody / prevRange < 0.9 || currBody / currRange < 0.9) {
            return Optional.empty();
        }

        // Bullish Kicking: prev bearish, curr bullish with gap up
        if (prevClose < prevOpen && currClose > currOpen && currOpen > prevClose) {
            return Optional.of(new CandlePattern(null, candleId, "kicking_bullish", 95.0));
        }

        // Bearish Kicking: prev bullish, curr bearish with gap down
        if (prevClose > prevOpen && currClose < currOpen && currOpen < prevClose) {
            return Optional.of(new CandlePattern(null, candleId, "kicking_bearish", -95.0));
        }

        return Optional.empty();
    }

    /**
     * Meeting Lines: Two candles of opposite colors meeting at same close price
     * Strength: +70 (bullish) or -70 (bearish)
     */
    private Optional<CandlePattern> detectMeetingLines(BarSeries series, int index, Long candleId) {
        if (index < 1) return Optional.empty();

        Bar prev = series.getBar(index - 1);
        Bar curr = series.getBar(index);

        double prevClose = prev.getClosePrice().doubleValue();
        double currClose = curr.getClosePrice().doubleValue();
        double tolerance = Math.abs(prevClose * 0.002); // 0.2% tolerance

        if (Math.abs(prevClose - currClose) > tolerance) {
            return Optional.empty();
        }

        boolean prevBullish = prev.getClosePrice().doubleValue() > prev.getOpenPrice().doubleValue();
        boolean currBullish = curr.getClosePrice().doubleValue() > curr.getOpenPrice().doubleValue();

        // Bullish Meeting Lines: prev bearish, curr bullish
        if (!prevBullish && currBullish) {
            boolean inDowntrend = index >= 5 && isDowntrend(series, index, 5);
            if (inDowntrend) {
                return Optional.of(new CandlePattern(null, candleId, "meeting_lines_bullish", 70.0));
            }
        }

        // Bearish Meeting Lines: prev bullish, curr bearish
        if (prevBullish && !currBullish) {
            boolean inUptrend = index >= 5 && isUptrend(series, index, 5);
            if (inUptrend) {
                return Optional.of(new CandlePattern(null, candleId, "meeting_lines_bearish", -70.0));
            }
        }

        return Optional.empty();
    }

    /**
     * Separating Lines: Two candles of same color opening at same price
     * Strength: +75 (bullish) or -75 (bearish)
     */
    private Optional<CandlePattern> detectSeparatingLines(BarSeries series, int index, Long candleId) {
        if (index < 1) return Optional.empty();

        Bar prev = series.getBar(index - 1);
        Bar curr = series.getBar(index);

        double prevOpen = prev.getOpenPrice().doubleValue();
        double currOpen = curr.getOpenPrice().doubleValue();
        double tolerance = Math.abs(prevOpen * 0.002);

        if (Math.abs(prevOpen - currOpen) > tolerance) {
            return Optional.empty();
        }

        boolean prevBullish = prev.getClosePrice().doubleValue() > prev.getOpenPrice().doubleValue();
        boolean currBullish = curr.getClosePrice().doubleValue() > curr.getOpenPrice().doubleValue();

        // Bullish Separating: prev bearish, curr bullish, same open
        if (!prevBullish && currBullish) {
            return Optional.of(new CandlePattern(null, candleId, "separating_lines_bullish", 75.0));
        }

        // Bearish Separating: prev bullish, curr bearish, same open
        if (prevBullish && !currBullish) {
            return Optional.of(new CandlePattern(null, candleId, "separating_lines_bearish", -75.0));
        }

        return Optional.empty();
    }

    /**
     * Counterattack Lines: Similar to Meeting Lines but with stronger reversal implication
     * Strength: +75 (bullish) or -75 (bearish)
     */
    private Optional<CandlePattern> detectCounterattack(BarSeries series, int index, Long candleId) {
        if (index < 1) return Optional.empty();

        Bar prev = series.getBar(index - 1);
        Bar curr = series.getBar(index);

        double prevOpen = prev.getOpenPrice().doubleValue();
        double prevClose = prev.getClosePrice().doubleValue();
        double currOpen = curr.getOpenPrice().doubleValue();
        double currClose = curr.getClosePrice().doubleValue();

        double prevBody = Math.abs(prevClose - prevOpen);
        double currBody = Math.abs(currClose - currOpen);
        double tolerance = Math.abs(prevClose * 0.002);

        // Both must have significant bodies
        if (prevBody < (prev.getHighPrice().doubleValue() - prev.getLowPrice().doubleValue()) * 0.6 ||
            currBody < (curr.getHighPrice().doubleValue() - curr.getLowPrice().doubleValue()) * 0.6) {
            return Optional.empty();
        }

        // Closes must be approximately equal
        if (Math.abs(prevClose - currClose) > tolerance) {
            return Optional.empty();
        }

        // Bullish Counterattack: prev bearish, curr bullish
        if (prevClose < prevOpen && currClose > currOpen) {
            return Optional.of(new CandlePattern(null, candleId, "counterattack_bullish", 75.0));
        }

        // Bearish Counterattack: prev bullish, curr bearish
        if (prevClose > prevOpen && currClose < currOpen) {
            return Optional.of(new CandlePattern(null, candleId, "counterattack_bearish", -75.0));
        }

        return Optional.empty();
    }

    /**
     * Three Inside Up/Down: Harami followed by confirmation candle
     * Strength: +85 (up) or -85 (down)
     */
    private Optional<CandlePattern> detectThreeInsideUpDown(BarSeries series, int index, Long candleId) {
        if (index < 2) return Optional.empty();

        Bar first = series.getBar(index - 2);
        Bar second = series.getBar(index - 1);
        Bar third = series.getBar(index);

        double firstOpen = first.getOpenPrice().doubleValue();
        double firstClose = first.getClosePrice().doubleValue();
        double secondOpen = second.getOpenPrice().doubleValue();
        double secondClose = second.getClosePrice().doubleValue();
        double thirdClose = third.getClosePrice().doubleValue();
        double thirdOpen = third.getOpenPrice().doubleValue();

        // Three Inside Up: first bearish, second bullish harami, third bullish confirmation
        if (firstClose < firstOpen && secondClose > secondOpen) {
            // Check harami relationship
            double firstBody = Math.abs(firstClose - firstOpen);
            double secondBody = Math.abs(secondClose - secondOpen);

            if (secondBody < firstBody * 0.7 &&
                secondClose < Math.max(firstOpen, firstClose) &&
                secondOpen > Math.min(firstOpen, firstClose)) {

                // Third candle confirms
                if (thirdClose > thirdOpen && thirdClose > secondClose) {
                    return Optional.of(new CandlePattern(null, candleId, "three_inside_up", 85.0));
                }
            }
        }

        // Three Inside Down: first bullish, second bearish harami, third bearish confirmation
        if (firstClose > firstOpen && secondClose < secondOpen) {
            double firstBody = Math.abs(firstClose - firstOpen);
            double secondBody = Math.abs(secondClose - secondOpen);

            if (secondBody < firstBody * 0.7 &&
                secondClose > Math.min(firstOpen, firstClose) &&
                secondOpen < Math.max(firstOpen, firstClose)) {

                if (thirdClose < thirdOpen && thirdClose < secondClose) {
                    return Optional.of(new CandlePattern(null, candleId, "three_inside_down", -85.0));
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Three Outside Up/Down: Engulfing followed by confirmation
     * Strength: +90 (up) or -90 (down)
     */
    private Optional<CandlePattern> detectThreeOutsideUpDown(BarSeries series, int index, Long candleId) {
        if (index < 2) return Optional.empty();

        Bar first = series.getBar(index - 2);
        Bar second = series.getBar(index - 1);
        Bar third = series.getBar(index);

        double firstOpen = first.getOpenPrice().doubleValue();
        double firstClose = first.getClosePrice().doubleValue();
        double secondOpen = second.getOpenPrice().doubleValue();
        double secondClose = second.getClosePrice().doubleValue();
        double thirdClose = third.getClosePrice().doubleValue();
        double thirdOpen = third.getOpenPrice().doubleValue();

        // Three Outside Up: first bearish, second bullish engulfing, third bullish confirmation
        if (firstClose < firstOpen && secondClose > secondOpen) {
            // Check engulfing
            if (secondOpen <= firstClose && secondClose >= firstOpen) {
                // Third confirms
                if (thirdClose > thirdOpen && thirdClose > secondClose) {
                    return Optional.of(new CandlePattern(null, candleId, "three_outside_up", 90.0));
                }
            }
        }

        // Three Outside Down
        if (firstClose > firstOpen && secondClose < secondOpen) {
            if (secondOpen >= firstClose && secondClose <= firstOpen) {
                if (thirdClose < thirdOpen && thirdClose < secondClose) {
                    return Optional.of(new CandlePattern(null, candleId, "three_outside_down", -90.0));
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Abandoned Baby: Rare reversal pattern with doji gap
     * Strength: +95 (bullish) or -95 (bearish)
     */
    private Optional<CandlePattern> detectAbandonedBaby(BarSeries series, int index, Long candleId) {
        if (index < 2) return Optional.empty();

        Bar first = series.getBar(index - 2);
        Bar second = series.getBar(index - 1);
        Bar third = series.getBar(index);

        double firstHigh = first.getHighPrice().doubleValue();
        double firstLow = first.getLowPrice().doubleValue();
        double secondOpen = second.getOpenPrice().doubleValue();
        double secondClose = second.getClosePrice().doubleValue();
        double secondHigh = second.getHighPrice().doubleValue();
        double secondLow = second.getLowPrice().doubleValue();
        double secondRange = secondHigh - secondLow;
        double thirdHigh = third.getHighPrice().doubleValue();
        double thirdLow = third.getLowPrice().doubleValue();

        if (secondRange == 0) return Optional.empty();

        // Second must be doji
        double secondBody = Math.abs(secondClose - secondOpen);
        if (secondBody / secondRange > DOJI_THRESHOLD) {
            return Optional.empty();
        }

        // Bullish Abandoned Baby
        if (first.getClosePrice().doubleValue() < first.getOpenPrice().doubleValue() &&
            third.getClosePrice().doubleValue() > third.getOpenPrice().doubleValue()) {

            // Gap down then gap up
            if (secondHigh < firstLow && thirdLow > secondHigh) {
                return Optional.of(new CandlePattern(null, candleId, "abandoned_baby_bullish", 95.0));
            }
        }

        // Bearish Abandoned Baby
        if (first.getClosePrice().doubleValue() > first.getOpenPrice().doubleValue() &&
            third.getClosePrice().doubleValue() < third.getOpenPrice().doubleValue()) {

            // Gap up then gap down
            if (secondLow > firstHigh && thirdHigh < secondLow) {
                return Optional.of(new CandlePattern(null, candleId, "abandoned_baby_bearish", -95.0));
            }
        }

        return Optional.empty();
    }

    /**
     * Tri-Star: Three doji in a row (rare, extreme reversal)
     * Strength: +98 (bullish) or -98 (bearish)
     */
    private Optional<CandlePattern> detectTriStar(BarSeries series, int index, Long candleId) {
        if (index < 2) return Optional.empty();

        for (int i = index - 2; i <= index; i++) {
            Bar bar = series.getBar(i);
            double range = bar.getHighPrice().doubleValue() - bar.getLowPrice().doubleValue();
            if (range == 0) return Optional.empty();

            double body = Math.abs(bar.getClosePrice().doubleValue() - bar.getOpenPrice().doubleValue());
            if (body / range > DOJI_THRESHOLD) {
                return Optional.empty();
            }
        }

        // All three are doji
        Bar middle = series.getBar(index - 1);
        double middleClose = middle.getClosePrice().doubleValue();

        boolean inUptrend = index >= 5 && isUptrend(series, index, 5);
        boolean inDowntrend = index >= 5 && isDowntrend(series, index, 5);

        // Check if middle doji is above/below the others
        double firstClose = series.getBar(index - 2).getClosePrice().doubleValue();
        double thirdClose = series.getBar(index).getClosePrice().doubleValue();

        // Bearish Tri-Star: middle doji higher (at top)
        if (inUptrend && middleClose > firstClose && middleClose > thirdClose) {
            return Optional.of(new CandlePattern(null, candleId, "tri_star_bearish", -98.0));
        }

        // Bullish Tri-Star: middle doji lower (at bottom)
        if (inDowntrend && middleClose < firstClose && middleClose < thirdClose) {
            return Optional.of(new CandlePattern(null, candleId, "tri_star_bullish", 98.0));
        }

        return Optional.empty();
    }

    /**
     * Rising Three Methods: Bullish continuation - small bearish candles within long bullish
     * Strength: +80
     */
    private Optional<CandlePattern> detectRisingThreeMethods(BarSeries series, int index, Long candleId) {
        if (index < 4) return Optional.empty();

        Bar first = series.getBar(index - 4);
        Bar fifth = series.getBar(index);

        // First and fifth must be long bullish
        double firstOpen = first.getOpenPrice().doubleValue();
        double firstClose = first.getClosePrice().doubleValue();
        double fifthOpen = fifth.getOpenPrice().doubleValue();
        double fifthClose = fifth.getClosePrice().doubleValue();

        if (firstClose <= firstOpen || fifthClose <= fifthOpen) {
            return Optional.empty();
        }

        double firstBody = firstClose - firstOpen;
        double firstRange = first.getHighPrice().doubleValue() - first.getLowPrice().doubleValue();
        double fifthBody = fifthClose - fifthOpen;

        if (firstBody / firstRange < 0.6 || fifthBody < firstBody * 0.8) {
            return Optional.empty();
        }

        // Middle three should be small and stay within first candle's range
        for (int i = index - 3; i <= index - 1; i++) {
            Bar bar = series.getBar(i);
            double close = bar.getClosePrice().doubleValue();
            double open = bar.getOpenPrice().doubleValue();

            // Preferably bearish or small
            if (close > open) {
                double body = close - open;
                if (body > firstBody * 0.3) return Optional.empty();
            }

            // Stay within first candle range
            if (close > firstClose || open < firstOpen) {
                return Optional.empty();
            }
        }

        // Fifth closes above first
        if (fifthClose > firstClose) {
            return Optional.of(new CandlePattern(null, candleId, "rising_three_methods", 80.0));
        }

        return Optional.empty();
    }

    /**
     * Falling Three Methods: Bearish continuation - small bullish candles within long bearish
     * Strength: -80
     */
    private Optional<CandlePattern> detectFallingThreeMethods(BarSeries series, int index, Long candleId) {
        if (index < 4) return Optional.empty();

        Bar first = series.getBar(index - 4);
        Bar fifth = series.getBar(index);

        // First and fifth must be long bearish
        double firstOpen = first.getOpenPrice().doubleValue();
        double firstClose = first.getClosePrice().doubleValue();
        double fifthOpen = fifth.getOpenPrice().doubleValue();
        double fifthClose = fifth.getClosePrice().doubleValue();

        if (firstClose >= firstOpen || fifthClose >= fifthOpen) {
            return Optional.empty();
        }

        double firstBody = firstOpen - firstClose;
        double firstRange = first.getHighPrice().doubleValue() - first.getLowPrice().doubleValue();
        double fifthBody = fifthOpen - fifthClose;

        if (firstBody / firstRange < 0.6 || fifthBody < firstBody * 0.8) {
            return Optional.empty();
        }

        // Middle three should be small and stay within first candle's range
        for (int i = index - 3; i <= index - 1; i++) {
            Bar bar = series.getBar(i);
            double close = bar.getClosePrice().doubleValue();
            double open = bar.getOpenPrice().doubleValue();

            // Preferably bullish or small
            if (close < open) {
                double body = open - close;
                if (body > firstBody * 0.3) return Optional.empty();
            }

            // Stay within first candle range
            if (close < firstClose || open > firstOpen) {
                return Optional.empty();
            }
        }

        // Fifth closes below first
        if (fifthClose < firstClose) {
            return Optional.of(new CandlePattern(null, candleId, "falling_three_methods", -80.0));
        }

        return Optional.empty();
    }

    /**
     * Helper: Check if series is in uptrend
     */
    private boolean isUptrend(BarSeries series, int index, int lookback) {
        if (index < lookback) return false;

        double startClose = series.getBar(index - lookback).getClosePrice().doubleValue();
        double endClose = series.getBar(index).getClosePrice().doubleValue();

        return endClose > startClose;
    }

    /**
     * Helper: Check if series is in downtrend
     */
    private boolean isDowntrend(BarSeries series, int index, int lookback) {
        if (index < lookback) return false;

        double startClose = series.getBar(index - lookback).getClosePrice().doubleValue();
        double endClose = series.getBar(index).getClosePrice().doubleValue();

        return endClose < startClose;
    }

    /**
     * Helper class to wrap Optional pattern
     */
    private static class Optional<T> {
        private final T value;

        private Optional(T value) {
            this.value = value;
        }

        static <T> Optional<T> of(T value) {
            return new Optional<>(value);
        }

        static <T> Optional<T> empty() {
            return new Optional<>(null);
        }

        void ifPresent(java.util.function.Consumer<T> consumer) {
            if (value != null) {
                consumer.accept(value);
            }
        }
    }
}
