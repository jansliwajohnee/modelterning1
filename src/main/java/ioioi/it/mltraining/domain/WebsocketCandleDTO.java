package ioioi.it.mltraining.domain;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

/**
 * @author jan.sliwa777@gmail.com
 * @createdAt 25/10/2024
 */

@Data
@NoArgsConstructor
public class WebsocketCandleDTO {

    private String symbol;
    private CandlestickInterval interval;
    private Boolean bearish;
    private Boolean bullish;

    private ZonedDateTime openTime;
    private ZonedDateTime closeTime;

    private Double open;
    private Double close;
    private Double low;
    private Double high;

    private Double turnover;
    private Double volume;

    private Boolean isFinal;

    public WebsocketCandleDTO(String symbol, CandlestickInterval interval,
                              Boolean bearish, Boolean bullish,
                              ZonedDateTime openTime, ZonedDateTime closeTime,
                              Double open, Double close, Double low, Double high,
                              Double turnover, Double volume,
                              Boolean isFinal) {
        this.symbol = symbol;
        this.interval = interval;
        this.bearish = bearish;
        this.bullish = bullish;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.open = open;
        this.close = close;
        this.low = low;
        this.high = high;
        this.turnover = turnover;
        this.volume = volume;
        this.isFinal = isFinal;
    }
}
