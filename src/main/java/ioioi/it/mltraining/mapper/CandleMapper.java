package ioioi.it.mltraining.mapper;

import com.bybit.api.client.domain.websocket_message.public_channel.KlineData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ioioi.it.mltraining.domain.CandleDTO;
import ioioi.it.mltraining.domain.CandlestickInterval;
import ioioi.it.mltraining.utils.TimeUtils;
import lombok.RequiredArgsConstructor;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class CandleMapper {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static CandleDTO map(KlineData klineData, String symbol, String intervalCode) {
        Double open = Double.valueOf(klineData.getOpen());
        Double close = Double.valueOf(klineData.getClose());
        Double low = Double.valueOf(klineData.getLow());
        Double high = Double.valueOf(klineData.getHigh());
        Boolean bearish = open > close;
        Boolean bullish = open <= close;
        Double volume = Double.valueOf(klineData.getVolume());
        Double turnover = Double.valueOf(klineData.getTurnover());
        ZonedDateTime openTime = TimeUtils.longToZonedDateTime(klineData.getStart());
        ZonedDateTime closeTime = TimeUtils.longToZonedDateTime(klineData.getEnd());
        Boolean isFinal = klineData.getConfirm();
        CandlestickInterval interval = IntervalMapper.map(intervalCode);
        return new CandleDTO(
                symbol,
                interval,
                openTime,
                closeTime,
                open,
                close,
                low,
                high,
                volume,
                turnover,
                isFinal
        );
    }

    public static List<CandleDTO> map(String json, String interval) throws JsonProcessingException {
        JsonNode rootNode = objectMapper.readTree(json);
        JsonNode result = rootNode.path("result");
        JsonNode listNode = result.path("list");
        String symbol = result.path("symbol").asText();

        CandlestickInterval candlestickInterval = IntervalMapper.map(interval);
        long intervalMs = Long.parseLong(interval) * 60 * 1000L;

        List<CandleDTO> candleDTOs = new ArrayList<>();
        for (JsonNode node : listNode) {
            ZonedDateTime openTime = TimeUtils.longToZonedDateTime(node.get(0).asLong());
            ZonedDateTime closeTime = openTime.plus(java.time.Duration.ofMillis(intervalMs));

            CandleDTO candleDTO = new CandleDTO(
                    symbol,
                    candlestickInterval,
                    openTime,
                    closeTime,
                    node.get(1).asDouble(),  // open
                    node.get(4).asDouble(),  // close
                    node.get(3).asDouble(),  // low
                    node.get(2).asDouble(),  // high
                    node.get(5).asDouble(),  // volume
                    node.get(6).asDouble(),   // turnover
                    true  // isClosed
            );
            candleDTOs.add(candleDTO);
        }

        return candleDTOs;
    }
}
