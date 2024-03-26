package com.project.portfolio.entity.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum TradeType {
    BUY("Buy"), SELL("Sell");
    private String tradeType;
}
