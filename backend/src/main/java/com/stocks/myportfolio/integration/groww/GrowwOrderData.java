package com.stocks.myportfolio.integration.groww;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GrowwOrderData {

    @JsonProperty("groww_order_id")
    private String growwOrderId;

    @JsonProperty("trading_symbol")
    private String tradingSymbol;

    @JsonProperty("order_status")
    private String orderStatus;

    private int quantity;

    @JsonProperty("filled_quantity")
    private int filledQuantity;

    @JsonProperty("average_fill_price")
    private BigDecimal averageFillPrice;

    private String exchange;

    @JsonProperty("transaction_type")
    private String transactionType;

    private String segment;

    private String product;

    @JsonProperty("trade_date")
    private String tradeDate;

    @JsonProperty("created_at")
    private String createdAt;

    public String getGrowwOrderId() { return growwOrderId; }
    public void setGrowwOrderId(String growwOrderId) { this.growwOrderId = growwOrderId; }
    public String getTradingSymbol() { return tradingSymbol; }
    public void setTradingSymbol(String tradingSymbol) { this.tradingSymbol = tradingSymbol; }
    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public int getFilledQuantity() { return filledQuantity; }
    public void setFilledQuantity(int filledQuantity) { this.filledQuantity = filledQuantity; }
    public BigDecimal getAverageFillPrice() { return averageFillPrice; }
    public void setAverageFillPrice(BigDecimal averageFillPrice) { this.averageFillPrice = averageFillPrice; }
    public String getExchange() { return exchange; }
    public void setExchange(String exchange) { this.exchange = exchange; }
    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
    public String getSegment() { return segment; }
    public void setSegment(String segment) { this.segment = segment; }
    public String getProduct() { return product; }
    public void setProduct(String product) { this.product = product; }
    public String getTradeDate() { return tradeDate; }
    public void setTradeDate(String tradeDate) { this.tradeDate = tradeDate; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
