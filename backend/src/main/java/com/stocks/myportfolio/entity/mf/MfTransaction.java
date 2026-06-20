package com.stocks.myportfolio.entity.mf;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.stocks.myportfolio.common.enums.MfTransactionType;
import com.stocks.myportfolio.entity.BaseEntity;

import jakarta.persistence.*;

@Entity
@Table(name = "mf_transaction")
public class MfTransaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mutual_fund_id")
    private MutualFund mutualFund;

    @Column(nullable = false)
    private BigDecimal units;

    @Column(nullable = false)
    private BigDecimal nav;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private MfTransactionType transactionType;

    private String description;

    @Column(name = "trade_date")
    private LocalDateTime tradeDate;

    @Column(name = "folio_number", length = 50)
    private String folioNumber;

    public MfTransaction() {}

    public Long getId() { return id; }
    public MutualFund getMutualFund() { return mutualFund; }
    public void setMutualFund(MutualFund mutualFund) { this.mutualFund = mutualFund; }
    public BigDecimal getUnits() { return units; }
    public void setUnits(BigDecimal units) { this.units = units; }
    public BigDecimal getNav() { return nav; }
    public void setNav(BigDecimal nav) { this.nav = nav; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public MfTransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(MfTransactionType transactionType) { this.transactionType = transactionType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getTradeDate() { return tradeDate; }
    public void setTradeDate(LocalDateTime tradeDate) { this.tradeDate = tradeDate; }
    public String getFolioNumber() { return folioNumber; }
    public void setFolioNumber(String folioNumber) { this.folioNumber = folioNumber; }
}
