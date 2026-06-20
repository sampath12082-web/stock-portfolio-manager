package com.stocks.myportfolio.entity.mf;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.stocks.myportfolio.entity.BaseEntity;

import jakarta.persistence.*;

@Entity
@Table(name = "mutual_fund")
public class MutualFund extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "scheme_code", nullable = false, unique = true, length = 20)
    private String schemeCode;

    @Column(name = "scheme_name", nullable = false, length = 500)
    private String schemeName;

    @Column(name = "fund_house")
    private String fundHouse;

    @Column(length = 20)
    private String isin;

    @Column(length = 100)
    private String category;

    @Column(name = "fund_type", length = 100)
    private String fundType;

    @Column(name = "current_nav")
    private BigDecimal currentNav;

    @Column(name = "nav_date")
    private LocalDate navDate;

    public MutualFund() {}

    public Long getId() { return id; }
    public String getSchemeCode() { return schemeCode; }
    public void setSchemeCode(String schemeCode) { this.schemeCode = schemeCode; }
    public String getSchemeName() { return schemeName; }
    public void setSchemeName(String schemeName) { this.schemeName = schemeName; }
    public String getFundHouse() { return fundHouse; }
    public void setFundHouse(String fundHouse) { this.fundHouse = fundHouse; }
    public String getIsin() { return isin; }
    public void setIsin(String isin) { this.isin = isin; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getFundType() { return fundType; }
    public void setFundType(String fundType) { this.fundType = fundType; }
    public BigDecimal getCurrentNav() { return currentNav; }
    public void setCurrentNav(BigDecimal currentNav) { this.currentNav = currentNav; }
    public LocalDate getNavDate() { return navDate; }
    public void setNavDate(LocalDate navDate) { this.navDate = navDate; }
}
