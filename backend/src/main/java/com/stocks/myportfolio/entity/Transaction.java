package com.stocks.myportfolio.entity;

import java.math.BigDecimal;

import com.stocks.myportfolio.common.enums.Exchange;
import com.stocks.myportfolio.common.enums.TradeType;
import com.stocks.myportfolio.common.enums.TransactionType;

import jakarta.persistence.*;

@Entity
@Table(name = "transaction_history")
public class Transaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "stock_id")
    private Stock stock;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private Exchange exchange;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    private String description;

    @Column(name = "trade_date")
    private java.time.LocalDateTime tradeDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "trade_type")
    private TradeType tradeType;

    public Transaction() {
    }

    public Long getId() {
        return id;
    }

    public Stock getStock() {
        return stock;
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Exchange getExchange() {
        return exchange;
    }

    public void setExchange(Exchange exchange) {
        this.exchange = exchange;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public java.time.LocalDateTime getTradeDate() {
        return tradeDate;
    }

    public void setTradeDate(java.time.LocalDateTime tradeDate) {
        this.tradeDate = tradeDate;
    }

    public TradeType getTradeType() {
        return tradeType;
    }

    public void setTradeType(TradeType tradeType) {
        this.tradeType = tradeType;
    }
}
