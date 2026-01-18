package ioioi.it.mltraining.service;

import ioioi.it.mltraining.entity.Candle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainingDataService {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Value("${ml.training.data-path:/app/ml-data}")
    private String dataPath;

    private final AtomicInteger datasetIdCounter = new AtomicInteger(0);

    @PostConstruct
    public void initializeIdCounter() {
        Path basePath = Path.of(dataPath);
        if (!Files.exists(basePath)) {
            log.info("ML data directory does not exist yet, starting ID counter at 0");
            return;
        }

        try (Stream<Path> dirs = Files.list(basePath)) {
            int maxId = dirs
                    .filter(Files::isDirectory)
                    .map(p -> p.getFileName().toString())
                    .filter(name -> name.matches("\\d+_.*"))
                    .mapToInt(name -> {
                        try {
                            return Integer.parseInt(name.split("_")[0]);
                        } catch (NumberFormatException e) {
                            return 0;
                        }
                    })
                    .max()
                    .orElse(0);
            datasetIdCounter.set(maxId);
            log.info("Initialized dataset ID counter to {}", maxId);
        } catch (IOException e) {
            log.warn("Failed to scan existing directories, starting ID counter at 0: {}", e.getMessage());
        }
    }

    // Base OHLCV columns (always included first)
    private static final List<String> BASE_COLUMNS = List.of(
            "open", "close", "low", "high", "volume", "turnover"
    );

    // Numeric feature columns (Double type)
    private static final List<String> NUMERIC_FEATURE_COLUMNS = List.of(
            // Momentum
            "rsi_7", "rsi_14", "rsi_21", "stochastic_k", "stochastic_d", "stochastic_rsi",
            "williams_r", "cci_normalized", "mfi", "cmo", "ultimate_oscillator", "roc_percent",
            "rsi_distance_from_50", "rsi_slope",
            // Trend
            "close_ema8_distance_pct", "close_ema21_distance_pct", "close_ema50_distance_pct",
            "close_ema100_distance_pct", "close_ema200_distance_pct", "close_sma20_distance_pct",
            "close_sma50_distance_pct", "close_sma200_distance_pct", "close_vwma_distance_pct",
            "close_hma_distance_pct", "ema8_ema21_spread_pct", "ema21_ema50_spread_pct",
            "ema50_ema200_spread_pct", "adx", "plus_di", "minus_di", "di_spread_normalized",
            "aroon_up", "aroon_down", "aroon_oscillator", "linear_regression_slope_pct", "ma_slope_pct",
            // Volatility
            "atr_pct", "atr_7_21_ratio", "bb_percent_b", "bb_width_pct", "bb_position",
            "keltner_percent_k", "keltner_width_pct", "range_pct", "range_atr_ratio",
            "atr_percentile", "stddev_pct", "stddev_14_50_ratio",
            // Volume
            "rvol_sma20", "rvol_sma50", "volume_percentile", "volume_normalized",
            "obv_change_pct", "cmf", "volume_roc_pct", "up_volume_ratio", "volume_pressure",
            // Price Action (numeric only)
            "body_range_ratio", "upper_wick_range_ratio", "lower_wick_range_ratio",
            "close_position_in_range", "body_pct", "gap_pct",
            "highest_high_distance_pct", "lowest_low_distance_pct", "position_in_range_n",
            "consecutive_bullish_ratio", "consecutive_bearish_ratio", "higher_highs_ratio", "higher_closes_ratio",
            // Statistical
            "close_percentile_100", "close_percentile_500", "close_zscore_20",
            "rsi_percentile", "volume_percentile_stat", "atr_percentile_stat",
            "skewness_returns", "kurtosis_returns", "drawdown_pct", "drawup_pct",
            // MACD (numeric only)
            "macd_pct", "macd_signal_pct", "macd_histogram_pct", "macd_histogram_change",
            // Support/Resistance
            "close_pivot_distance_pct", "close_s1_distance_pct", "close_r1_distance_pct",
            "close_prev_day_high_pct", "close_prev_day_low_pct", "close_prev_week_high_pct",
            "close_round_number_distance_pct", "close_vwap_distance_pct",
            // Lag features
            "return_lag_1", "return_lag_2", "return_lag_3", "return_lag_5", "return_lag_10",
            "rsi_lag_1", "rsi_lag_2", "rsi_lag_3", "rsi_change",
            "atr_pct_lag_1", "atr_pct_lag_2", "rvol_lag_1", "rvol_lag_2",
            "bb_percent_b_lag_1", "bb_percent_b_lag_2", "adx_lag_1", "adx_lag_2",
            "body_pct_lag_1", "body_pct_lag_2",
            // Composite (numeric only)
            "trend_score", "bullish_patterns_ratio", "momentum_agreement", "volatility_vs_trend",
            // Volume profile
            "vp_poc_volume_pct", "vp_poc_position_in_range", "vp_value_area_pct",
            "vp_value_area_volume_pct", "vp_volume_above_poc_pct", "vp_volume_below_poc_pct",
            "vp_volume_imbalance", "vp_volume_concentration"
    );

    // Boolean feature columns (will be cast to 0/1 in SQL)
    private static final List<String> BOOLEAN_FEATURE_COLUMNS = List.of(
            "is_bullish", "macd_gt_signal", "macd_gt_zero", "volume_confirmation"
    );

    // Combined feature columns for API response
    private static final List<String> FEATURE_COLUMNS;
    static {
        List<String> combined = new ArrayList<>(NUMERIC_FEATURE_COLUMNS);
        combined.addAll(BOOLEAN_FEATURE_COLUMNS);
        FEATURE_COLUMNS = List.copyOf(combined);
    }

    // Available target columns that can be requested
    private static final List<String> AVAILABLE_TARGET_COLUMNS = List.of(
            "return_1", "return_3", "return_5", "return_10", "return_20", "return_50"
    );

    private static final List<String> DEFAULT_TARGETS = List.of("return_1");

    public record TrainingDataResult(
            int datasetId,
            String directoryPath,
            String filePath,
            String symbol,
            int featuresCount,
            List<String> targets,
            int recordsCount
    ) {}

    public TrainingDataResult generateTrainingData(String symbol, int featuresCount, List<String> targets, int recordsCount) {
        List<String> selectedTargets = (targets == null || targets.isEmpty()) ? List.of() : targets;

        // Validate targets
        for (String target : selectedTargets) {
            if (!AVAILABLE_TARGET_COLUMNS.contains(target)) {
                throw new IllegalArgumentException("Invalid target column: " + target +
                        ". Available: " + AVAILABLE_TARGET_COLUMNS);
            }
        }

        log.info("Generating training data for symbol={}, features={}, targets={}, records={}",
                symbol, featuresCount, selectedTargets, recordsCount);

        int datasetId = datasetIdCounter.incrementAndGet();
        int targetsCountForDir = selectedTargets.size() + 1; // +1 for fixed "target" column
        String dirName = String.format("%d_%s_%d_%d_%d", datasetId, symbol, featuresCount, targetsCountForDir, recordsCount);
        Path dirPath = Path.of(dataPath, dirName);
        Path filePath = dirPath.resolve("train.csv");

        try {
            Files.createDirectories(dirPath);
            log.info("Created directory: {}", dirPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create directory: " + dirPath, e);
        }

        // Select features: first from numeric, then from boolean if needed
        List<String> selectedNumericFeatures = new ArrayList<>();
        List<String> selectedBooleanFeatures = new ArrayList<>();

        int remaining = featuresCount;
        if (remaining > 0) {
            int numericToTake = Math.min(remaining, NUMERIC_FEATURE_COLUMNS.size());
            selectedNumericFeatures = NUMERIC_FEATURE_COLUMNS.subList(0, numericToTake);
            remaining -= numericToTake;
        }
        if (remaining > 0) {
            int booleanToTake = Math.min(remaining, BOOLEAN_FEATURE_COLUMNS.size());
            selectedBooleanFeatures = BOOLEAN_FEATURE_COLUMNS.subList(0, booleanToTake);
        }

        // Build SQL with CAST for boolean columns
        StringBuilder columnsSql = new StringBuilder();
        List<String> csvColumnNames = new ArrayList<>();

        // Add base OHLCV columns first (always)
        for (String baseCol : BASE_COLUMNS) {
            if (columnsSql.length() > 0) columnsSql.append(", ");
            columnsSql.append(baseCol);
            csvColumnNames.add(baseCol);
        }

        // Add numeric features
        for (int i = 0; i < selectedNumericFeatures.size(); i++) {
            if (columnsSql.length() > 0) columnsSql.append(", ");
            columnsSql.append(selectedNumericFeatures.get(i));
            csvColumnNames.add(selectedNumericFeatures.get(i));
        }

        // Add boolean features with CAST to int
        for (String boolCol : selectedBooleanFeatures) {
            if (columnsSql.length() > 0) columnsSql.append(", ");
            columnsSql.append("CASE WHEN ").append(boolCol).append(" = true THEN 1 ELSE 0 END AS ").append(boolCol);
            csvColumnNames.add(boolCol);
        }

        // Add fixed "target" column with value 1 (always)
        if (columnsSql.length() > 0) columnsSql.append(", ");
        columnsSql.append("1 AS target");
        csvColumnNames.add("target");

        // Add additional target columns from request (only if specified)
        for (String target : selectedTargets) {
            if (columnsSql.length() > 0) columnsSql.append(", ");
            columnsSql.append(target);
            csvColumnNames.add(target);
        }

        String sql = String.format("""
                SELECT %s
                FROM candle
                WHERE symbol = :symbol
                ORDER BY open_time ASC
                LIMIT :limit
                """, columnsSql);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("symbol", symbol)
                .addValue("limit", recordsCount);

        int totalColumns = csvColumnNames.size();
        List<double[]> rows = jdbcTemplate.query(sql, params, (rs, rowNum) -> {
            double[] row = new double[totalColumns];
            for (int i = 0; i < totalColumns; i++) {
                double val = rs.getDouble(i + 1);
                row[i] = rs.wasNull() ? Double.NaN : val;
            }
            return row;
        });

        log.info("Fetched {} records from database", rows.size());

        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            writer.write(String.join(",", csvColumnNames));
            writer.newLine();

            for (double[] row : rows) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < row.length; i++) {
                    if (i > 0) sb.append(",");
                    if (Double.isNaN(row[i])) {
                        sb.append("");
                    } else {
                        sb.append(row[i]);
                    }
                }
                writer.write(sb.toString());
                writer.newLine();
            }
            log.info("Written training data to: {}", filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write training data to: " + filePath, e);
        }

        int actualFeaturesCount = selectedNumericFeatures.size() + selectedBooleanFeatures.size();
        return new TrainingDataResult(
                datasetId,
                dirPath.toString(),
                filePath.toString(),
                symbol,
                actualFeaturesCount,
                selectedTargets,
                rows.size()
        );
    }

    public List<String> getAvailableFeatures() {
        return FEATURE_COLUMNS;
    }

    public List<String> getAvailableTargets() {
        return AVAILABLE_TARGET_COLUMNS;
    }
}
