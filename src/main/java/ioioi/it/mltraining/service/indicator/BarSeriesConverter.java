package ioioi.it.mltraining.service.indicator;

import ioioi.it.mltraining.entity.CandleRaw;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Converter utility for transforming CandleRaw entities into Ta4j BarSeries.
 * Ta4j BarSeries is required for technical indicator calculations.
 * Compatible with Ta4j 0.22.0 API (requires Java 21+).
 */
public final class BarSeriesConverter {

    private BarSeriesConverter() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Converts a list of CandleRaw entities to Ta4j BarSeries.
     * The list must be ordered by openTime ascending (oldest first).
     *
     * @param candleRaws list of CandleRaw records ordered by openTime ascending
     * @param symbol the trading symbol for the series name
     * @return Ta4j BarSeries ready for technical indicator calculations
     * @throws IllegalArgumentException if candleRaws is null or empty
     */
    public static BarSeries toBarSeries(List<CandleRaw> candleRaws, String symbol) {
        if (candleRaws == null || candleRaws.isEmpty()) {
            throw new IllegalArgumentException("CandleRaw list cannot be null or empty");
        }

        // Ta4j 0.22.0: Use BaseBarSeriesBuilder
        BaseBarSeries series = new BaseBarSeriesBuilder().withName(symbol).build();

        candleRaws.stream()
                .map(BarSeriesConverter::convertToBar)
                .forEach(series::addBar);

        return series;
    }

    private static Bar convertToBar(CandleRaw candle) {
        Instant beginTime = candle.getOpenTime().toInstant();
        Instant endTime = candle.getCloseTime().toInstant();
        Duration duration = Duration.between(beginTime, endTime);

        Num open = DecimalNum.valueOf(candle.getOpen());
        Num high = DecimalNum.valueOf(candle.getHigh());
        Num low = DecimalNum.valueOf(candle.getLow());
        Num close = DecimalNum.valueOf(candle.getClose());
        Num volume = DecimalNum.valueOf(candle.getVolume());
        Num amount = DecimalNum.valueOf(candle.getTurnover());

        return new BaseBar(duration, beginTime, endTime, open, high, low, close, volume, amount, 0L);
    }
}
