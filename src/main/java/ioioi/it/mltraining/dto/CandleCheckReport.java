package ioioi.it.mltraining.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandleCheckReport {

    private String symbol;
    private String interval;
    private int totalCandles;
    private int missingCandlesCount;
    private int candlesWithNullFieldsCount;
    private List<MissingCandleInfo> missingCandles = new ArrayList<>();
    private List<NullFieldInfo> nullFields = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MissingCandleInfo {
        private ZonedDateTime expectedOpenTime;
        private ZonedDateTime previousOpenTime;
        private ZonedDateTime nextOpenTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NullFieldInfo {
        private Long candleId;
        private ZonedDateTime openTime;
        private List<String> nullFieldNames = new ArrayList<>();
    }
}