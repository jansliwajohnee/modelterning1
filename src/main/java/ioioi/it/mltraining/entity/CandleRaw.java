package ioioi.it.mltraining.entity;

import ioioi.it.mltraining.domain.CandlestickInterval;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("candle_raw")
public class CandleRaw {

    @Id
    private Long id;

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

    @Column("is_closed")
    private Boolean isClosed;
}
