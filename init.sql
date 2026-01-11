-- Example schema for ML training data
-- Run this manually or add to docker-compose with init script

CREATE TABLE IF NOT EXISTS training_data (
    id BIGSERIAL PRIMARY KEY,
    feature1 DOUBLE PRECISION NOT NULL,
    feature2 DOUBLE PRECISION NOT NULL,
    feature3 DOUBLE PRECISION,
    feature4 DOUBLE PRECISION,
    label INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_training_data_label ON training_data(label);
CREATE INDEX idx_training_data_created_at ON training_data(created_at);

-- For even better performance with billions of rows, consider partitioning:
-- CREATE TABLE training_data_2024_01 PARTITION OF training_data
--     FOR VALUES FROM ('2024-01-01') TO ('2024-02-01');

-- Candle raw data table for historical and real-time market data
CREATE TABLE IF NOT EXISTS candle_raw (
    id BIGSERIAL PRIMARY KEY,
    symbol VARCHAR(20) NOT NULL,
    interval VARCHAR(10) NOT NULL,
    open_time TIMESTAMP WITH TIME ZONE NOT NULL,
    close_time TIMESTAMP WITH TIME ZONE NOT NULL,
    open DOUBLE PRECISION NOT NULL,
    close DOUBLE PRECISION NOT NULL,
    low DOUBLE PRECISION NOT NULL,
    high DOUBLE PRECISION NOT NULL,
    volume DOUBLE PRECISION NOT NULL,
    turnover DOUBLE PRECISION,
    is_closed BOOLEAN NOT NULL
);

-- Indexes for maximum query performance
CREATE INDEX idx_candle_raw_symbol_interval_time ON candle_raw(symbol, interval, open_time DESC);
CREATE INDEX idx_candle_raw_open_time ON candle_raw(open_time DESC);

-- Unique constraint to prevent duplicate candles
CREATE UNIQUE INDEX idx_candle_raw_unique ON candle_raw(symbol, interval, open_time);

-- Candle table with all technical indicators
CREATE TABLE IF NOT EXISTS candle (
    id BIGSERIAL PRIMARY KEY,

    -- ========== CANDLE RAW DATA ==========
    symbol VARCHAR(20) NOT NULL,
    interval VARCHAR(10) NOT NULL,
    open_time TIMESTAMP WITH TIME ZONE NOT NULL,
    close_time TIMESTAMP WITH TIME ZONE NOT NULL,
    open DOUBLE PRECISION,
    close DOUBLE PRECISION,
    low DOUBLE PRECISION,
    high DOUBLE PRECISION,
    volume DOUBLE PRECISION,
    turnover DOUBLE PRECISION,
    is_closed BOOLEAN,

    -- ========== MOMENTUM ==========
    rsi_7 DOUBLE PRECISION,
    rsi_14 DOUBLE PRECISION,
    rsi_21 DOUBLE PRECISION,
    stochastic_k DOUBLE PRECISION,
    stochastic_d DOUBLE PRECISION,
    stochastic_rsi DOUBLE PRECISION,
    williams_r DOUBLE PRECISION,
    cci_normalized DOUBLE PRECISION,
    mfi DOUBLE PRECISION,
    cmo DOUBLE PRECISION,
    ultimate_oscillator DOUBLE PRECISION,
    roc_percent DOUBLE PRECISION,
    rsi_distance_from_50 DOUBLE PRECISION,
    rsi_slope DOUBLE PRECISION,

    -- ========== TREND ==========
    close_ema8_distance_pct DOUBLE PRECISION,
    close_ema21_distance_pct DOUBLE PRECISION,
    close_ema50_distance_pct DOUBLE PRECISION,
    close_ema100_distance_pct DOUBLE PRECISION,
    close_ema200_distance_pct DOUBLE PRECISION,
    close_sma20_distance_pct DOUBLE PRECISION,
    close_sma50_distance_pct DOUBLE PRECISION,
    close_sma200_distance_pct DOUBLE PRECISION,
    close_vwma_distance_pct DOUBLE PRECISION,
    close_hma_distance_pct DOUBLE PRECISION,
    ema8_ema21_spread_pct DOUBLE PRECISION,
    ema21_ema50_spread_pct DOUBLE PRECISION,
    ema50_ema200_spread_pct DOUBLE PRECISION,
    adx DOUBLE PRECISION,
    plus_di DOUBLE PRECISION,
    minus_di DOUBLE PRECISION,
    di_spread_normalized DOUBLE PRECISION,
    aroon_up DOUBLE PRECISION,
    aroon_down DOUBLE PRECISION,
    aroon_oscillator DOUBLE PRECISION,
    linear_regression_slope_pct DOUBLE PRECISION,
    ma_slope_pct DOUBLE PRECISION,

    -- ========== VOLATILITY ==========
    atr_pct DOUBLE PRECISION,
    atr_7_21_ratio DOUBLE PRECISION,
    bb_percent_b DOUBLE PRECISION,
    bb_width_pct DOUBLE PRECISION,
    bb_position DOUBLE PRECISION,
    keltner_percent_k DOUBLE PRECISION,
    keltner_width_pct DOUBLE PRECISION,
    range_pct DOUBLE PRECISION,
    range_atr_ratio DOUBLE PRECISION,
    atr_percentile DOUBLE PRECISION,
    stddev_pct DOUBLE PRECISION,
    stddev_14_50_ratio DOUBLE PRECISION,

    -- ========== VOLUME ==========
    rvol_sma20 DOUBLE PRECISION,
    rvol_sma50 DOUBLE PRECISION,
    volume_percentile DOUBLE PRECISION,
    volume_normalized DOUBLE PRECISION,
    obv_change_pct DOUBLE PRECISION,
    cmf DOUBLE PRECISION,
    volume_roc_pct DOUBLE PRECISION,
    up_volume_ratio DOUBLE PRECISION,
    volume_pressure DOUBLE PRECISION,

    -- ========== PRICE ACTION ==========
    body_range_ratio DOUBLE PRECISION,
    upper_wick_range_ratio DOUBLE PRECISION,
    lower_wick_range_ratio DOUBLE PRECISION,
    close_position_in_range DOUBLE PRECISION,
    body_pct DOUBLE PRECISION,
    gap_pct DOUBLE PRECISION,
    is_bullish BOOLEAN,
    return_1 DOUBLE PRECISION,
    return_3 DOUBLE PRECISION,
    return_5 DOUBLE PRECISION,
    return_10 DOUBLE PRECISION,
    return_20 DOUBLE PRECISION,
    return_50 DOUBLE PRECISION,
    highest_high_distance_pct DOUBLE PRECISION,
    lowest_low_distance_pct DOUBLE PRECISION,
    position_in_range_n DOUBLE PRECISION,
    consecutive_bullish_ratio DOUBLE PRECISION,
    consecutive_bearish_ratio DOUBLE PRECISION,
    higher_highs_ratio DOUBLE PRECISION,
    higher_closes_ratio DOUBLE PRECISION,

    -- ========== STATISTICAL ==========
    close_percentile_100 DOUBLE PRECISION,
    close_percentile_500 DOUBLE PRECISION,
    close_zscore_20 DOUBLE PRECISION,
    rsi_percentile DOUBLE PRECISION,
    volume_percentile_stat DOUBLE PRECISION,
    atr_percentile_stat DOUBLE PRECISION,
    skewness_returns DOUBLE PRECISION,
    kurtosis_returns DOUBLE PRECISION,
    drawdown_pct DOUBLE PRECISION,
    drawup_pct DOUBLE PRECISION,

    -- ========== MACD ==========
    macd_pct DOUBLE PRECISION,
    macd_signal_pct DOUBLE PRECISION,
    macd_histogram_pct DOUBLE PRECISION,
    macd_histogram_change DOUBLE PRECISION,
    macd_gt_signal BOOLEAN,
    macd_gt_zero BOOLEAN,

    -- ========== SUPPORT/RESISTANCE ==========
    close_pivot_distance_pct DOUBLE PRECISION,
    close_s1_distance_pct DOUBLE PRECISION,
    close_r1_distance_pct DOUBLE PRECISION,
    close_prev_day_high_pct DOUBLE PRECISION,
    close_prev_day_low_pct DOUBLE PRECISION,
    close_prev_week_high_pct DOUBLE PRECISION,
    close_round_number_distance_pct DOUBLE PRECISION,
    close_vwap_distance_pct DOUBLE PRECISION,

    -- ========== LAG FEATURES ==========
    return_lag_1 DOUBLE PRECISION,
    return_lag_2 DOUBLE PRECISION,
    return_lag_3 DOUBLE PRECISION,
    return_lag_5 DOUBLE PRECISION,
    return_lag_10 DOUBLE PRECISION,
    rsi_lag_1 DOUBLE PRECISION,
    rsi_lag_2 DOUBLE PRECISION,
    rsi_lag_3 DOUBLE PRECISION,
    rsi_change DOUBLE PRECISION,
    atr_pct_lag_1 DOUBLE PRECISION,
    atr_pct_lag_2 DOUBLE PRECISION,
    rvol_lag_1 DOUBLE PRECISION,
    rvol_lag_2 DOUBLE PRECISION,
    bb_percent_b_lag_1 DOUBLE PRECISION,
    bb_percent_b_lag_2 DOUBLE PRECISION,
    adx_lag_1 DOUBLE PRECISION,
    adx_lag_2 DOUBLE PRECISION,
    body_pct_lag_1 DOUBLE PRECISION,
    body_pct_lag_2 DOUBLE PRECISION,

    -- ========== COMPOSITE ==========
    trend_score DOUBLE PRECISION,
    bullish_patterns_ratio DOUBLE PRECISION,
    momentum_agreement DOUBLE PRECISION,
    volatility_vs_trend DOUBLE PRECISION,
    volume_confirmation BOOLEAN
);

-- Indexes for candle table
CREATE INDEX idx_candle_symbol_interval_time ON candle(symbol, interval, open_time DESC);
CREATE INDEX idx_candle_open_time ON candle(open_time DESC);
CREATE UNIQUE INDEX idx_candle_unique ON candle(symbol, interval, open_time);

-- Candle pattern table for pattern recognition
CREATE TABLE IF NOT EXISTS candle_pattern (
    id BIGSERIAL PRIMARY KEY,
    candle_indicators_id BIGINT NOT NULL,
    pattern_name VARCHAR(50) NOT NULL,
    pattern_strength DOUBLE PRECISION,
    CONSTRAINT fk_candle_pattern_candle FOREIGN KEY (candle_indicators_id) REFERENCES candle(id) ON DELETE CASCADE
);

-- Index for candle pattern lookups
CREATE INDEX idx_candle_pattern_candle_id ON candle_pattern(candle_indicators_id);
CREATE INDEX idx_candle_pattern_name ON candle_pattern(pattern_name);
