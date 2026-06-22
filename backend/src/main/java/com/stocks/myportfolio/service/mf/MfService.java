package com.stocks.myportfolio.service.mf;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stocks.myportfolio.common.enums.MfTransactionType;
import com.stocks.myportfolio.common.exception.DuplicateResourceException;
import com.stocks.myportfolio.common.exception.ResourceNotFoundException;
import com.stocks.myportfolio.common.exception.ValidationException;
import com.stocks.myportfolio.common.util.CalculationUtils;
import com.stocks.myportfolio.dto.request.mf.CreateMfHoldingRequest;
import com.stocks.myportfolio.dto.request.mf.CreateMfRequest;
import com.stocks.myportfolio.dto.request.mf.CreateMfTransactionRequest;
import com.stocks.myportfolio.dto.response.mf.MfFundResponse;
import com.stocks.myportfolio.dto.response.mf.MfHoldingResponse;
import com.stocks.myportfolio.dto.response.mf.MfTransactionResponse;
import com.stocks.myportfolio.entity.mf.MfHolding;
import com.stocks.myportfolio.entity.mf.MfTransaction;
import com.stocks.myportfolio.entity.mf.MutualFund;
import com.stocks.myportfolio.repository.mf.MfHoldingRepository;
import com.stocks.myportfolio.repository.mf.MfTransactionRepository;
import com.stocks.myportfolio.repository.mf.MutualFundRepository;
import com.stocks.myportfolio.security.CurrentUserProvider;

@Service
@Transactional
public class MfService {

    private final MutualFundRepository fundRepository;
    private final MfHoldingRepository holdingRepository;
    private final MfTransactionRepository transactionRepository;
    private final CurrentUserProvider currentUser;

    public MfService(MutualFundRepository fundRepository,
                     MfHoldingRepository holdingRepository,
                     MfTransactionRepository transactionRepository,
                     CurrentUserProvider currentUser) {
        this.fundRepository = fundRepository;
        this.holdingRepository = holdingRepository;
        this.transactionRepository = transactionRepository;
        this.currentUser = currentUser;
    }

    public MfFundResponse createFund(CreateMfRequest request) {
        fundRepository.findBySchemeCode(request.schemeCode())
                .ifPresent(f -> { throw new DuplicateResourceException("Fund already exists: " + request.schemeCode()); });

        MutualFund fund = new MutualFund();
        fund.setSchemeCode(request.schemeCode());
        fund.setSchemeName(request.schemeName());
        fund.setFundHouse(request.fundHouse());
        fund.setIsin(request.isin());
        fund.setCategory(request.category());
        fund.setFundType(request.fundType());

        return toFundResponse(fundRepository.save(fund));
    }

    @Transactional(readOnly = true)
    public List<MfFundResponse> getAllFunds() {
        return fundRepository.findByUserId(currentUser.getUserId())
                .stream().map(this::toFundResponse).toList();
    }

    public void deleteFund(Long id) {
        MutualFund fund = fundRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fund not found"));
        if (holdingRepository.findByMutualFund(fund).isPresent()) {
            throw new ValidationException("Cannot delete fund with existing holdings");
        }
        fundRepository.delete(fund);
    }

    public MfHoldingResponse createHolding(CreateMfHoldingRequest request) {
        MutualFund fund = fundRepository.findBySchemeCode(request.schemeCode())
                .orElseThrow(() -> new ResourceNotFoundException("Fund not found: " + request.schemeCode()));

        BigDecimal invested = request.units().multiply(request.averageNav()).setScale(2, RoundingMode.HALF_UP);

        MfHolding holding = holdingRepository.findByMutualFund(fund).orElseGet(() -> {
            MfHolding h = new MfHolding();
            h.setMutualFund(fund);
            return h;
        });
        holding.setUnits(request.units());
        holding.setAverageNav(request.averageNav());
        holding.setInvestedAmount(invested);

        return toHoldingResponse(holdingRepository.save(holding));
    }

    @Transactional(readOnly = true)
    public List<MfHoldingResponse> getAllHoldings() {
        return holdingRepository.findByUserId(currentUser.getUserId())
                .stream().filter(h -> h.getUnits().compareTo(BigDecimal.ZERO) > 0)
                .map(this::toHoldingResponse).toList();
    }

    public MfTransactionResponse createTransaction(CreateMfTransactionRequest request) {
        MutualFund fund = fundRepository.findBySchemeCode(request.schemeCode())
                .orElseThrow(() -> new ResourceNotFoundException("Fund not found: " + request.schemeCode()));

        MfTransaction txn = new MfTransaction();
        txn.setMutualFund(fund);
        txn.setUnits(request.units());
        txn.setNav(request.nav());
        txn.setAmount(request.amount());
        txn.setTransactionType(request.transactionType());
        txn.setDescription(request.description());
        txn.setFolioNumber(request.folioNumber());

        if (request.tradeDate() != null && !request.tradeDate().isBlank()) {
            txn.setTradeDate(LocalDateTime.parse(request.tradeDate().replace(" ", "T")));
        }

        MfTransaction saved = transactionRepository.save(txn);
        updateHolding(fund, request);
        return toTxnResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<MfTransactionResponse> getAllTransactions() {
        return transactionRepository.findAllByOrderByTradeDateDesc()
                .stream().map(this::toTxnResponse).toList();
    }

    private void updateHolding(MutualFund fund, CreateMfTransactionRequest request) {
        MfHolding holding = holdingRepository.findByMutualFund(fund)
                .orElseGet(() -> {
                    MfHolding h = new MfHolding();
                    h.setMutualFund(fund);
                    h.setUnits(BigDecimal.ZERO);
                    h.setAverageNav(BigDecimal.ZERO);
                    h.setInvestedAmount(BigDecimal.ZERO);
                    return h;
                });

        MfTransactionType type = request.transactionType();
        if (type == MfTransactionType.PURCHASE || type == MfTransactionType.SIP
                || type == MfTransactionType.SWITCH_IN || type == MfTransactionType.DIVIDEND_REINVEST) {
            BigDecimal newUnits = holding.getUnits().add(request.units());
            BigDecimal newInvested = holding.getInvestedAmount().add(request.amount());
            BigDecimal newAvg = newUnits.compareTo(BigDecimal.ZERO) > 0
                    ? newInvested.divide(newUnits, 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            holding.setUnits(newUnits);
            holding.setInvestedAmount(newInvested);
            holding.setAverageNav(newAvg);
        } else if (type == MfTransactionType.REDEMPTION || type == MfTransactionType.SWP
                || type == MfTransactionType.SWITCH_OUT) {
            BigDecimal newUnits = holding.getUnits().subtract(request.units());
            if (newUnits.compareTo(BigDecimal.ZERO) < 0) newUnits = BigDecimal.ZERO;
            BigDecimal newInvested = holding.getAverageNav().multiply(newUnits).setScale(2, RoundingMode.HALF_UP);
            holding.setUnits(newUnits);
            holding.setInvestedAmount(newInvested);
        }

        holdingRepository.save(holding);
    }

    private MfFundResponse toFundResponse(MutualFund fund) {
        return new MfFundResponse(fund.getId(), fund.getSchemeCode(), fund.getSchemeName(),
                fund.getFundHouse(), fund.getIsin(), fund.getCategory(), fund.getFundType(),
                fund.getCurrentNav(), fund.getNavDate());
    }

    private MfHoldingResponse toHoldingResponse(MfHolding holding) {
        MutualFund fund = holding.getMutualFund();
        BigDecimal currentNav = fund.getCurrentNav();
        BigDecimal currentValue = null;
        BigDecimal pnl = null;
        BigDecimal pnlPct = null;

        if (currentNav != null && currentNav.compareTo(BigDecimal.ZERO) > 0) {
            currentValue = currentNav.multiply(holding.getUnits()).setScale(2, RoundingMode.HALF_UP);
            pnl = CalculationUtils.calculatePnL(currentValue, holding.getInvestedAmount());
            pnlPct = CalculationUtils.calculatePnLPercentage(pnl, holding.getInvestedAmount());
        }

        return new MfHoldingResponse(holding.getId(), fund.getSchemeCode(), fund.getSchemeName(),
                fund.getFundHouse(), holding.getUnits(), holding.getAverageNav(),
                holding.getInvestedAmount(), currentNav, currentValue, pnl, pnlPct);
    }

    private MfTransactionResponse toTxnResponse(MfTransaction txn) {
        MutualFund fund = txn.getMutualFund();
        return new MfTransactionResponse(txn.getId(),
                fund != null ? fund.getSchemeCode() : null,
                fund != null ? fund.getSchemeName() : null,
                txn.getUnits(), txn.getNav(), txn.getAmount(),
                txn.getTransactionType(), txn.getDescription(), txn.getFolioNumber(),
                txn.getTradeDate(), txn.getCreatedAt());
    }
}
