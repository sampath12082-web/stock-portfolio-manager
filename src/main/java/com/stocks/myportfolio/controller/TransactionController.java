package com.stocks.myportfolio.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.stocks.myportfolio.common.enums.TransactionType;
import com.stocks.myportfolio.dto.request.CreateTransactionRequest;
import com.stocks.myportfolio.dto.response.TransactionAnalyticsResponse;
import com.stocks.myportfolio.dto.response.TransactionResponse;
import com.stocks.myportfolio.dto.response.TransactionUploadResponse;
import com.stocks.myportfolio.service.TransactionService;
import com.stocks.myportfolio.service.TransactionUploadService;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionUploadService transactionUploadService;

    public TransactionController(
            TransactionService transactionService,
            TransactionUploadService transactionUploadService) {

        this.transactionService = transactionService;
        this.transactionUploadService = transactionUploadService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse createTransaction(
            @RequestBody CreateTransactionRequest request) {

        return transactionService.createTransaction(request);
    }

    @GetMapping
    public List<TransactionResponse> getTransactions(
            @RequestParam(required = false) String symbol,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        if (symbol != null) {
            return transactionService.getTransactionsBySymbol(symbol);
        }
        if (type != null) {
            return transactionService.getTransactionsByType(type);
        }
        if (from != null && to != null) {
            return transactionService.getTransactionsByDateRange(from, to);
        }

        return transactionService.getAllTransactions();
    }

    @GetMapping("/analytics")
    public TransactionAnalyticsResponse getAnalytics() {
        return transactionService.getAnalytics();
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public TransactionUploadResponse uploadPdf(
            @RequestParam("file") MultipartFile file) {

        return transactionUploadService.uploadPdf(file);
    }
}
