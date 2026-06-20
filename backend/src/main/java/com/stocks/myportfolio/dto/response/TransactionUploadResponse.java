package com.stocks.myportfolio.dto.response;

import java.util.List;

public record TransactionUploadResponse(
        int transactionsCreated,
        int stocksCreated,
        List<String> errors) {
}
