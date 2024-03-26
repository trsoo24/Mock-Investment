package com.project.portfolio.entity;

import com.project.portfolio.entity.type.TradeType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collation = "trade_records")
@Getter
@Builder
public class TradeRecord {
    @Id
    private String id;
    private String email;
    @Enumerated(EnumType.STRING)
    private TradeType tradeType;
    private String coinName;
    private double volume;
    private double price;
    private LocalDateTime tradeTimeStamp;

}
