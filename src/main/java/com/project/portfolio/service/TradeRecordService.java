package com.project.portfolio.service;

import com.project.portfolio.entity.TradeRecord;
import com.project.portfolio.entity.type.TradeType;
import com.project.portfolio.repository.TradeRecordRepository;
import com.project.reference.CheckUserReference;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TradeRecordService {
    private final TradeRecordRepository tradeRecordRepository;
    private final CheckUserReference checkUserReference;

    public void saveTradeRecord(String email, TradeType tradeType, String coinName,
                                double volume, double price) {

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime tradeTime = LocalDateTime.parse(LocalDateTime.now().format(timeFormatter));

        TradeRecord tradeRecord = makeDocument(email, tradeType, coinName, volume, price, tradeTime);

        tradeRecordRepository.save(tradeRecord);
    }

    public TradeRecord makeDocument(String email, TradeType tradeType, String coinName,
                                    double volume, double price, LocalDateTime tradeTimeStamp) {
        return TradeRecord.builder()
                .email(email)
                .tradeType(tradeType)
                .coinName(coinName)
                .volume(volume)
                .price(price)
                .tradeTimeStamp(tradeTimeStamp)
                .build();
    }

    public List<TradeRecord> findTradeRecordList(HttpServletRequest request, String coinName) { // 특정 유저의 특정 코인 거래 기록 리스트
        String email = checkUserReference.checkUserReference(request);

        return tradeRecordRepository.findByEmailAndCoinName(email, coinName);
    }
}
