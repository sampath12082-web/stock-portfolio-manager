package com.stocks.myportfolio.integration.groww;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GrowwHoldingData {

    @JsonProperty("trading_symbol")
    private String tradingSymbol;

    private BigDecimal quantity;

    @JsonProperty("average_price")
    private BigDecimal averagePrice;

    @JsonProperty("tradable_exchanges")
    private List<String> tradableExchanges;

    public String getTradingSymbol() {
        return tradingSymbol;
    }

    public void setTradingSymbol(String tradingSymbol) {
        this.tradingSymbol = tradingSymbol;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getAveragePrice() {
        return averagePrice;
    }

    public void setAveragePrice(BigDecimal averagePrice) {
        this.averagePrice = averagePrice;
    }

    public List<String> getTradableExchanges() {
        return tradableExchanges;
    }

    public void setTradableExchanges(List<String> tradableExchanges) {
        this.tradableExchanges = tradableExchanges;
    }
}
