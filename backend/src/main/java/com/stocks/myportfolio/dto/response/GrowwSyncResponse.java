package com.stocks.myportfolio.dto.response;

import java.util.List;

public record GrowwSyncResponse(
        int stocksCreated,
        int holdingsCreated,
        int holdingsUpdated,
        List<String> errors) {
}
