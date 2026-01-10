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
