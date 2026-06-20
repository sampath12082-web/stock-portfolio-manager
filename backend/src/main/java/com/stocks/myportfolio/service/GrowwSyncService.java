package com.stocks.myportfolio.service;

import com.stocks.myportfolio.dto.response.GrowwAccountResponse;
import com.stocks.myportfolio.dto.response.GrowwSyncResponse;

public interface GrowwSyncService {

    GrowwSyncResponse syncPortfolio();

    GrowwSyncResponse syncOrders();

    GrowwAccountResponse getAccountDetails();
}
