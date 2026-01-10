package ioioi.it.mltraining.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.ZonedDateTime;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class CandleDTO {

    private String symbol;
    private final CandlestickInterval interval;
    private final ZonedDateTime openTime;
    private final ZonedDateTime closeTime;

    private final Double open;
    private final Double close;
    private final Double low;
    private final Double high;

    private final Double volume;
    private final Double turnover;
    private final Boolean isClosed;
}
