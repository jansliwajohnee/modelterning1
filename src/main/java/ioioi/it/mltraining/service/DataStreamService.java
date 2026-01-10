package ioioi.it.mltraining.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.util.stream.Stream;

/**
 * Example service showing how to stream millions of records efficiently
 */
@Service
public class DataStreamService {

    private final JdbcTemplate jdbcTemplate;

    public DataStreamService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Stream 10 million records efficiently
     * The transaction must stay open while streaming
     */
    @Transactional(readOnly = true)
    public void processLargeDataset() {
        String sql = "SELECT * FROM training_data ORDER BY id";

        // Use queryForStream for memory-efficient processing
        try (Stream<TrainingRecord> stream = jdbcTemplate.queryForStream(
                sql,
                (rs, rowNum) -> mapResultSet(rs)
        )) {
            stream
                .parallel() // Process in parallel for speed
                .forEach(record -> {
                    // Your ML processing logic here
                    processRecord(record);
                });
        }
    }

    /**
     * Batch insert for maximum write performance
     */
    public void batchInsert(java.util.List<TrainingRecord> records) {
        String sql = "INSERT INTO training_data (feature1, feature2, label) VALUES (?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, records, 10000, (ps, record) -> {
            ps.setDouble(1, record.feature1());
            ps.setDouble(2, record.feature2());
            ps.setInt(3, record.label());
        });
    }

    private TrainingRecord mapResultSet(ResultSet rs) throws java.sql.SQLException {
        return new TrainingRecord(
            rs.getLong("id"),
            rs.getDouble("feature1"),
            rs.getDouble("feature2"),
            rs.getInt("label")
        );
    }

    private void processRecord(TrainingRecord record) {
        // Your processing logic
    }

    // Simple record class for example
    public record TrainingRecord(Long id, Double feature1, Double feature2, Integer label) {}
}
