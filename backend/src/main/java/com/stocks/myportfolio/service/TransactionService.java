package com.stocks.myportfolio.service;

import java.time.LocalDate;
import java.util.List;

import com.stocks.myportfolio.common.enums.TransactionType;
import com.stocks.myportfolio.dto.request.CreateTransactionRequest;
import com.stocks.myportfolio.dto.response.TransactionAnalyticsResponse;
import com.stocks.myportfolio.dto.response.TransactionResponse;

public interface TransactionService {

    TransactionResponse createTransaction(CreateTransactionRequest request);

    List<TransactionResponse> getAllTransactions();

    List<TransactionResponse> getTransactionsBySymbol(String symbol);

    List<TransactionResponse> getTransactionsByType(TransactionType type);

    List<TransactionResponse> getTransactionsByDateRange(LocalDate from, LocalDate to);

    TransactionAnalyticsResponse getAnalytics();
}
