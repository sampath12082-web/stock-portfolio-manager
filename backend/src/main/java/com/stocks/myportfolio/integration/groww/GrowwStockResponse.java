package com.stocks.myportfolio.integration.groww;

import java.math.BigDecimal;

public class GrowwStockResponse {

    private BigDecimal ltp;
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal close;
    private BigDecimal previousClose;
    private Long volume;
    private Long tsInMillis;

    public GrowwStockResponse() {
    }

    public BigDecimal getLtp() {
        return ltp;
    }

    public void setLtp(BigDecimal ltp) {
        this.ltp = ltp;
    }

    public BigDecimal getOpen() {
        return open;
    }

    public void setOpen(BigDecimal open) {
        this.open = open;
    }

    public BigDecimal getHigh() {
        return high;
    }

    public void setHigh(BigDecimal high) {
        this.high = high;
    }

    public BigDecimal getLow() {
        return low;
    }

    public void setLow(BigDecimal low) {
        this.low = low;
    }

    public BigDecimal getClose() {
        return close;
    }

    public void setClose(BigDecimal close) {
        this.close = close;
    }

    public BigDecimal getPreviousClose() {
        return previousClose;
    }

    public void setPreviousClose(BigDecimal previousClose) {
        this.previousClose = previousClose;
    }

    public Long getVolume() {
        return volume;
    }

    public void setVolume(Long volume) {
        this.volume = volume;
    }

    public Long getTsInMillis() {
        return tsInMillis;
    }

    public void setTsInMillis(Long tsInMillis) {
        this.tsInMillis = tsInMillis;
    }
}
