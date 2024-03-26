package com.project.market.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.project.market.entity.Market;
import com.project.market.entity.Ticker;
import com.project.market.entity.type.Change;
import com.project.reference.CheckMarketReference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class TickerService {
    private final CheckMarketReference checkMarketReference;
    private static final String tickerUrl = "https://api.upbit.com/v1/ticker?markets=";
    /** MarketName ex) "KRW-BTC" 으로 검색하는 방법 우선 사용
     * 이후 BTC , 혹은 비트코인 등으로 검색 기능 고도화 필요
     * String List 로 여러개 검색 가능 KRW-BTC%2CKRW-ETH%2CETC
     * %2C 로 구분지어 검색
     */
    public String parseMarketNameList(String marketNameRaw) {
        // 입력 값을 KRW , Market 이용
        // 이름 구분은 쉼표로 ex) KRW-BTC,KRW-ETH / 비트코인,이더리움

        String[] marketArray;
        if (!marketNameRaw.contains(",")) {
            marketArray = new String[1];
            marketArray[0] = marketNameRaw;
        } else {
            marketArray = marketNameRaw.split(",");
        }

        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < marketArray.length; i++) {
            String marketCode = "";

            if (marketArray[i].startsWith("KRW")) {
                Market market = checkMarketReference.findByMarketCode(marketArray[i]);

                marketCode = market.getMarket();
            } else {
                Market market = checkMarketReference.findByCoinName(marketArray[i]);

                marketCode = market.getMarket();
            }

            sb.append(marketCode);

            if (i != marketArray.length - 1) {
                sb.append("%2C");
            }
        }

        return sb.toString();
    }

    public List<Ticker> searchCoinTicker(String marketNameRaw) {
        String marketList = parseMarketNameList(marketNameRaw); // URL 에 쓰일 marketList 형식으로 변경

        try {
            URL url = new URL(tickerUrl + marketList);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-type", "application/json");
            conn.setRequestProperty("accept", "application/json");

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = "";
            String result = "";
            List<Ticker> tickerList = new ArrayList<>();

            while ((line = br.readLine()) != null) {
                result += line;
            }

            JsonArray parse = (JsonArray) JsonParser.parseString(result);
            for (JsonElement element : parse) {
                JsonObject object = element.getAsJsonObject();
                String marketName = object.getAsJsonObject().get("market").getAsString();
                String tradeDate = object.getAsJsonObject().get("trade_date_kst").getAsString();
                String tradeTime = object.getAsJsonObject().get("trade_time_kst").getAsString();
                double openPrice = mathRound(object.getAsJsonObject().get("opening_price").getAsDouble());
                double highPrice = mathRound(object.getAsJsonObject().get("high_price").getAsDouble());
                double lowPrice = mathRound(object.getAsJsonObject().get("low_price").getAsDouble());
                double tradePrice = mathRound(object.getAsJsonObject().get("trade_price").getAsDouble());
                String change = object.getAsJsonObject().get("change").getAsString();
                double changePrice = mathRound(object.getAsJsonObject().get("signed_change_price").getAsDouble());
                double changeRate = mathRound(object.getAsJsonObject().get("signed_change_rate").getAsDouble());
                double allDayTradePrice = mathRound(object.getAsJsonObject().get("acc_trade_price_24h").getAsDouble());
                double allDayTradeVolume = mathRound(object.getAsJsonObject().get("acc_trade_volume_24h").getAsDouble());
                double highestWeekPrice = mathRound(object.getAsJsonObject().get("highest_52_week_price").getAsDouble());
                LocalDate highestWeekDate = formatStringToLocalDate(object.getAsJsonObject().get("highest_52_week_date").getAsString());
                double lowestWeekPrice = mathRound(object.getAsJsonObject().get("lowest_52_week_price").getAsDouble());
                LocalDate lowestWeekDate = formatStringToLocalDate(object.getAsJsonObject().get("lowest_52_week_date").getAsString());

                String changedDescription = Change.valueOf(change).getDescription();

                Ticker ticker = Ticker.builder()
                        .marketName(marketName)
                        .lastTradeTimeStamp(formatStringToLocalDateTime(tradeDate + tradeTime))
                        .openPrice(openPrice)
                        .highPrice(highPrice)
                        .lowPrice(lowPrice)
                        .tradePrice(tradePrice)
                        .changed(changedDescription)
                        .changedPrice(changePrice)
                        .changedRate(changeRate)
                        .allDayTradePrice(allDayTradePrice)
                        .allDayTradeVolume(allDayTradeVolume)
                        .highestWeekPrice(highestWeekPrice)
                        .highestDay(highestWeekDate)
                        .lowestWeekPrice(lowestWeekPrice)
                        .lowestDay(lowestWeekDate)
                        .build();

                tickerList.add(ticker);
            }

            return tickerList;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public double mathRound(double number) { // 소수점 2번째 자리 반올림
        return Math.round(number * 100) / 100.0;
    }

    public LocalDateTime formatStringToLocalDateTime (String stringOfLocalDateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime localDateTime = LocalDateTime.parse(stringOfLocalDateTime, formatter);

        return LocalDateTime.parse(localDateTime.format(DateTimeFormatter.ISO_DATE_TIME));
    }

    public LocalDate formatStringToLocalDate (String stringOfLocalDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.parse(stringOfLocalDate, formatter);

    }
}
