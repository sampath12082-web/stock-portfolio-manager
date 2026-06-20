package com.stocks.myportfolio.integration.groww;

import java.util.List;

public class GrowwPortfolioResponse {

    private List<GrowwHoldingData> holdings;

    public List<GrowwHoldingData> getHoldings() {
        return holdings;
    }

    public void setHoldings(List<GrowwHoldingData> holdings) {
        this.holdings = holdings;
    }
}
