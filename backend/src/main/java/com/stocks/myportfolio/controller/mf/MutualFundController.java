package com.stocks.myportfolio.controller.mf;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.stocks.myportfolio.dto.request.mf.CreateMfHoldingRequest;
import com.stocks.myportfolio.dto.request.mf.CreateMfRequest;
import com.stocks.myportfolio.dto.request.mf.CreateMfTransactionRequest;
import com.stocks.myportfolio.dto.response.mf.MfFundResponse;
import com.stocks.myportfolio.dto.response.mf.MfHoldingResponse;
import com.stocks.myportfolio.dto.response.mf.MfLookupResponse;
import com.stocks.myportfolio.dto.response.mf.MfTransactionResponse;
import com.stocks.myportfolio.service.mf.MfNavService;
import com.stocks.myportfolio.service.mf.MfService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/mf")
public class MutualFundController {

    private final MfService mfService;
    private final MfNavService mfNavService;

    public MutualFundController(MfService mfService, MfNavService mfNavService) {
        this.mfService = mfService;
        this.mfNavService = mfNavService;
    }

    @PostMapping("/funds")
    @ResponseStatus(HttpStatus.CREATED)
    public MfFundResponse createFund(@Valid @RequestBody CreateMfRequest request) {
        return mfService.createFund(request);
    }

    @GetMapping("/funds")
    public List<MfFundResponse> getAllFunds() {
        return mfService.getAllFunds();
    }

    @GetMapping("/funds/search")
    public List<MfLookupResponse> searchFunds(@RequestParam String query) {
        return mfNavService.searchAmfi(query);
    }

    @DeleteMapping("/funds/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFund(@PathVariable Long id) {
        mfService.deleteFund(id);
    }

    @PostMapping("/funds/refresh-nav")
    public void refreshNav() {
        mfNavService.refreshNavs();
    }

    @GetMapping("/holdings")
    public List<MfHoldingResponse> getAllHoldings() {
        return mfService.getAllHoldings();
    }

    @PostMapping("/holdings")
    @ResponseStatus(HttpStatus.CREATED)
    public MfHoldingResponse createHolding(@Valid @RequestBody CreateMfHoldingRequest request) {
        return mfService.createHolding(request);
    }

    @PostMapping("/transactions")
    @ResponseStatus(HttpStatus.CREATED)
    public MfTransactionResponse createTransaction(@Valid @RequestBody CreateMfTransactionRequest request) {
        return mfService.createTransaction(request);
    }

    @GetMapping("/transactions")
    public List<MfTransactionResponse> getAllTransactions() {
        return mfService.getAllTransactions();
    }
}
