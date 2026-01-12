package ioioi.it.mltraining.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.time.ZonedDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("candle")
public class Candle {

    @Id
    private Long id;

    // ========== CANDLE RAW DATA ==========
    @Column("symbol")
    private String symbol;

    @Column("interval")
    private String interval;

    @Column("open_time")
    private ZonedDateTime openTime;

    @Column("close_time")
    private ZonedDateTime closeTime;

    @Column("open")
    private Double open;

    @Column("close")
    private Double close;

    @Column("low")
    private Double low;

    @Column("high")
    private Double high;

    @Column("volume")
    private Double volume;

    @Column("turnover")
    private Double turnover;

    // ========== MOMENTUM ==========
    @Column("rsi_7")
    private Double rsi7;

    @Column("rsi_14")
    private Double rsi14;

    @Column("rsi_21")
    private Double rsi21;

    @Column("stochastic_k")
    private Double stochasticK;

    @Column("stochastic_d")
    private Double stochasticD;

    @Column("stochastic_rsi")
    private Double stochasticRsi;

    @Column("williams_r")
    private Double williamsR;

    @Column("cci_normalized")
    private Double cciNormalized;

    @Column("mfi")
    private Double mfi;

    @Column("cmo")
    private Double cmo;

    @Column("ultimate_oscillator")
    private Double ultimateOscillator;

    @Column("roc_percent")
    private Double rocPercent;

    @Column("rsi_distance_from_50")
    private Double rsiDistanceFrom50;

    @Column("rsi_slope")
    private Double rsiSlope;

    // ========== TREND ==========
    @Column("close_ema8_distance_pct")
    private Double closeEma8DistancePct;

    @Column("close_ema21_distance_pct")
    private Double closeEma21DistancePct;

    @Column("close_ema50_distance_pct")
    private Double closeEma50DistancePct;

    @Column("close_ema100_distance_pct")
    private Double closeEma100DistancePct;

    @Column("close_ema200_distance_pct")
    private Double closeEma200DistancePct;

    @Column("close_sma20_distance_pct")
    private Double closeSma20DistancePct;

    @Column("close_sma50_distance_pct")
    private Double closeSma50DistancePct;

    @Column("close_sma200_distance_pct")
    private Double closeSma200DistancePct;

    @Column("close_vwma_distance_pct")
    private Double closeVwmaDistancePct;

    @Column("close_hma_distance_pct")
    private Double closeHmaDistancePct;

    @Column("ema8_ema21_spread_pct")
    private Double ema8Ema21SpreadPct;

    @Column("ema21_ema50_spread_pct")
    private Double ema21Ema50SpreadPct;

    @Column("ema50_ema200_spread_pct")
    private Double ema50Ema200SpreadPct;

    @Column("adx")
    private Double adx;

    @Column("plus_di")
    private Double plusDi;

    @Column("minus_di")
    private Double minusDi;

    @Column("di_spread_normalized")
    private Double diSpreadNormalized;

    @Column("aroon_up")
    private Double aroonUp;

    @Column("aroon_down")
    private Double aroonDown;

    @Column("aroon_oscillator")
    private Double aroonOscillator;

    @Column("linear_regression_slope_pct")
    private Double linearRegressionSlopePct;

    @Column("ma_slope_pct")
    private Double maSlopePct;

    // ========== VOLATILITY ==========
    @Column("atr_pct")
    private Double atrPct;

    @Column("atr_7_21_ratio")
    private Double atr721Ratio;

    @Column("bb_percent_b")
    private Double bbPercentB;

    @Column("bb_width_pct")
    private Double bbWidthPct;

    @Column("bb_position")
    private Double bbPosition;

    @Column("keltner_percent_k")
    private Double keltnerPercentK;

    @Column("keltner_width_pct")
    private Double keltnerWidthPct;

    @Column("range_pct")
    private Double rangePct;

    @Column("range_atr_ratio")
    private Double rangeAtrRatio;

    @Column("atr_percentile")
    private Double atrPercentile;

    @Column("stddev_pct")
    private Double stddevPct;

    @Column("stddev_14_50_ratio")
    private Double stddev1450Ratio;

    // ========== VOLUME ==========
    @Column("rvol_sma20")
    private Double rvolSma20;

    @Column("rvol_sma50")
    private Double rvolSma50;

    @Column("volume_percentile")
    private Double volumePercentile;

    @Column("volume_normalized")
    private Double volumeNormalized;

    @Column("obv_change_pct")
    private Double obvChangePct;

    @Column("cmf")
    private Double cmf;

    @Column("volume_roc_pct")
    private Double volumeRocPct;

    @Column("up_volume_ratio")
    private Double upVolumeRatio;

    @Column("volume_pressure")
    private Double volumePressure;

    // ========== PRICE ACTION ==========
    @Column("body_range_ratio")
    private Double bodyRangeRatio;

    @Column("upper_wick_range_ratio")
    private Double upperWickRangeRatio;

    @Column("lower_wick_range_ratio")
    private Double lowerWickRangeRatio;

    @Column("close_position_in_range")
    private Double closePositionInRange;

    @Column("body_pct")
    private Double bodyPct;

    @Column("gap_pct")
    private Double gapPct;

    @Column("is_bullish")
    private Boolean isBullish;

    @Column("return_1")
    private Double return1;

    @Column("return_3")
    private Double return3;

    @Column("return_5")
    private Double return5;

    @Column("return_10")
    private Double return10;

    @Column("return_20")
    private Double return20;

    @Column("return_50")
    private Double return50;

    @Column("highest_high_distance_pct")
    private Double highestHighDistancePct;

    @Column("lowest_low_distance_pct")
    private Double lowestLowDistancePct;

    @Column("position_in_range_n")
    private Double positionInRangeN;

    @Column("consecutive_bullish_ratio")
    private Double consecutiveBullishRatio;

    @Column("consecutive_bearish_ratio")
    private Double consecutiveBearishRatio;

    @Column("higher_highs_ratio")
    private Double higherHighsRatio;

    @Column("higher_closes_ratio")
    private Double higherClosesRatio;

    // ========== STATISTICAL ==========
    @Column("close_percentile_100")
    private Double closePercentile100;

    @Column("close_percentile_500")
    private Double closePercentile500;

    @Column("close_zscore_20")
    private Double closeZscore20;

    @Column("rsi_percentile")
    private Double rsiPercentile;

    @Column("volume_percentile_stat")
    private Double volumePercentileStat;

    @Column("atr_percentile_stat")
    private Double atrPercentileStat;

    @Column("skewness_returns")
    private Double skewnessReturns;

    @Column("kurtosis_returns")
    private Double kurtosisReturns;

    @Column("drawdown_pct")
    private Double drawdownPct;

    @Column("drawup_pct")
    private Double drawupPct;

    // ========== MACD ==========
    @Column("macd_pct")
    private Double macdPct;

    @Column("macd_signal_pct")
    private Double macdSignalPct;

    @Column("macd_histogram_pct")
    private Double macdHistogramPct;

    @Column("macd_histogram_change")
    private Double macdHistogramChange;

    @Column("macd_gt_signal")
    private Boolean macdGtSignal;

    @Column("macd_gt_zero")
    private Boolean macdGtZero;

    // ========== SUPPORT/RESISTANCE ==========
    @Column("close_pivot_distance_pct")
    private Double closePivotDistancePct;

    @Column("close_s1_distance_pct")
    private Double closeS1DistancePct;

    @Column("close_r1_distance_pct")
    private Double closeR1DistancePct;

    @Column("close_prev_day_high_pct")
    private Double closePrevDayHighPct;

    @Column("close_prev_day_low_pct")
    private Double closePrevDayLowPct;

    @Column("close_prev_week_high_pct")
    private Double closePrevWeekHighPct;

    @Column("close_round_number_distance_pct")
    private Double closeRoundNumberDistancePct;

    @Column("close_vwap_distance_pct")
    private Double closeVwapDistancePct;

    // ========== LAG FEATURES ==========
    @Column("return_lag_1")
    private Double returnLag1;

    @Column("return_lag_2")
    private Double returnLag2;

    @Column("return_lag_3")
    private Double returnLag3;

    @Column("return_lag_5")
    private Double returnLag5;

    @Column("return_lag_10")
    private Double returnLag10;

    @Column("rsi_lag_1")
    private Double rsiLag1;

    @Column("rsi_lag_2")
    private Double rsiLag2;

    @Column("rsi_lag_3")
    private Double rsiLag3;

    @Column("rsi_change")
    private Double rsiChange;

    @Column("atr_pct_lag_1")
    private Double atrPctLag1;

    @Column("atr_pct_lag_2")
    private Double atrPctLag2;

    @Column("rvol_lag_1")
    private Double rvolLag1;

    @Column("rvol_lag_2")
    private Double rvolLag2;

    @Column("bb_percent_b_lag_1")
    private Double bbPercentBLag1;

    @Column("bb_percent_b_lag_2")
    private Double bbPercentBLag2;

    @Column("adx_lag_1")
    private Double adxLag1;

    @Column("adx_lag_2")
    private Double adxLag2;

    @Column("body_pct_lag_1")
    private Double bodyPctLag1;

    @Column("body_pct_lag_2")
    private Double bodyPctLag2;

    // ========== COMPOSITE ==========
    @Column("trend_score")
    private Double trendScore;

    @Column("bullish_patterns_ratio")
    private Double bullishPatternsRatio;

    @Column("momentum_agreement")
    private Double momentumAgreement;

    @Column("volatility_vs_trend")
    private Double volatilityVsTrend;

    @Column("volume_confirmation")
    private Boolean volumeConfirmation;

    // ========== VOLUME PROFILE ==========
    @Column("vp_poc_price")
    private Double vpPocPrice;

    @Column("vp_poc_index")
    private Integer vpPocIndex;

    @Column("vp_poc_volume_pct")
    private Double vpPocVolumePct;

    @Column("vp_poc_position_in_range")
    private Double vpPocPositionInRange;

    @Column("vp_vah_price")
    private Double vpVahPrice;

    @Column("vp_val_price")
    private Double vpValPrice;

    @Column("vp_value_area_pct")
    private Double vpValueAreaPct;

    @Column("vp_value_area_volume_pct")
    private Double vpValueAreaVolumePct;

    @Column("vp_volume_above_poc_pct")
    private Double vpVolumeAbovePocPct;

    @Column("vp_volume_below_poc_pct")
    private Double vpVolumeBelowPocPct;

    @Column("vp_volume_imbalance")
    private Double vpVolumeImbalance;

    @Column("vp_high_volume_nodes_count")
    private Integer vpHighVolumeNodesCount;

    @Column("vp_low_volume_nodes_count")
    private Integer vpLowVolumeNodesCount;

    @Column("vp_volume_concentration")
    private Double vpVolumeConcentration;

    // ========== CANDLE PATTERNS (one-to-many) ==========
    @MappedCollection(idColumn = "candle_indicators_id")
    private Set<CandlePattern> candlePatterns;
}