package ioioi.it.mltraining.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("candle_pattern")
public class CandlePattern {

    @Id
    private Long id;

    @Column("candle_indicators_id")
    private Long candleIndicatorsId;

    /**
     * Nazwa wzorca, np: "doji", "hammer", "engulfing", "morning_star"
     */
    @Column("pattern_name")
    private String patternName;

    /**
     * Siła wzorca: -100 do 100
     * -100 = silnie bearish
     * 0 = brak
     * 100 = silnie bullish
     */
    @Column("pattern_strength")
    private Double patternStrength;
}