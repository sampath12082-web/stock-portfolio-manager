package com.stocks.myportfolio.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stocks.myportfolio.common.constants.AppConstants;
import com.stocks.myportfolio.common.enums.TradeType;
import com.stocks.myportfolio.common.enums.TransactionType;
import com.stocks.myportfolio.common.exception.ResourceNotFoundException;
import com.stocks.myportfolio.dto.request.CreateTransactionRequest;
import com.stocks.myportfolio.dto.response.TransactionAnalyticsResponse;
import com.stocks.myportfolio.dto.response.TransactionResponse;
import com.stocks.myportfolio.entity.Holding;
import com.stocks.myportfolio.entity.Stock;
import com.stocks.myportfolio.entity.Transaction;
import com.stocks.myportfolio.repository.HoldingRepository;
import com.stocks.myportfolio.security.CurrentUserProvider;
import com.stocks.myportfolio.repository.StockRepository;
import com.stocks.myportfolio.security.CurrentUserProvider;
import com.stocks.myportfolio.repository.TransactionRepository;
import com.stocks.myportfolio.security.CurrentUserProvider;
import com.stocks.myportfolio.service.TransactionService;

@Service
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final StockRepository stockRepository;
    private final HoldingRepository holdingRepository;
    private final CurrentUserProvider currentUser;

    public TransactionServiceImpl(
            TransactionRepository transactionRepository,
            StockRepository stockRepository,
            HoldingRepository holdingRepository,
            CurrentUserProvider currentUser) {

        this.transactionRepository = transactionRepository;
        this.stockRepository = stockRepository;
        this.holdingRepository = holdingRepository;
        this.currentUser = currentUser;
    }

    @Override
    public TransactionResponse createTransaction(CreateTransactionRequest request) {
        TransactionType type = request.transactionType();
        boolean isFundTransaction = type == TransactionType.DEPOSIT
                || type == TransactionType.WITHDRAWAL
                || type == TransactionType.CHARGES;

        Transaction transaction = new Transaction();
        transaction.setTransactionType(type);
        transaction.setDescription(request.description());

        if (request.tradeDate() != null && !request.tradeDate().isBlank()) {
            transaction.setTradeDate(LocalDateTime.parse(request.tradeDate().replace(" ", "T")));
        } else {
            transaction.setTradeDate(LocalDateTime.now());
        }

        if (request.tradeType() != null && !request.tradeType().isBlank()) {
            transaction.setTradeType(TradeType.valueOf(request.tradeType()));
        } else {
            transaction.setTradeType(TradeType.UNKNOWN);
        }

        if (isFundTransaction) {
            transaction.setQuantity(request.quantity() != null ? request.quantity() : 1);
            transaction.setPrice(request.price() != null ? request.price() : BigDecimal.ZERO);
            transaction.setTotalAmount(request.price() != null ? request.price() : BigDecimal.ZERO);
        } else {
            Stock stock = stockRepository
                    .findBySymbolIgnoreCase(request.symbol())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            AppConstants.STOCK_NOT_FOUND + ": " + request.symbol()));

            BigDecimal totalAmount = request.price()
                    .multiply(BigDecimal.valueOf(request.quantity()));

            transaction.setStock(stock);
            transaction.setQuantity(request.quantity());
            transaction.setPrice(request.price());
            transaction.setTotalAmount(totalAmount);
            transaction.setExchange(stock.getExchange());

            Transaction saved = transactionRepository.save(transaction);
            updateHolding(saved);
            return toResponse(saved);
        }

        Transaction saved = transactionRepository.save(transaction);
        return toResponse(saved);
    }

    @Override
    public List<TransactionResponse> getAllTransactions() {
        return transactionRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<TransactionResponse> getTransactionsBySymbol(String symbol) {
        Stock stock = stockRepository.findBySymbolIgnoreCase(symbol)
                .orElseThrow(() -> new ResourceNotFoundException(
                        AppConstants.STOCK_NOT_FOUND + ": " + symbol));

        return transactionRepository.findByStockOrderByCreatedAtDesc(stock)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<TransactionResponse> getTransactionsByType(TransactionType type) {
        return transactionRepository.findByTransactionTypeOrderByCreatedAtDesc(type)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<TransactionResponse> getTransactionsByDateRange(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);

        return transactionRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionAnalyticsResponse getAnalytics() {
        List<Transaction> all = transactionRepository.findByUserId(currentUser.getUserId());

        BigDecimal totalBuyAmount = BigDecimal.ZERO;
        BigDecimal totalSellAmount = BigDecimal.ZERO;
        BigDecimal totalDeposits = BigDecimal.ZERO;
        BigDecimal totalWithdrawals = BigDecimal.ZERO;
        BigDecimal totalCharges = BigDecimal.ZERO;
        BigDecimal intradayBuyAmount = BigDecimal.ZERO;
        BigDecimal intradaySellAmount = BigDecimal.ZERO;
        BigDecimal deliveryBuyAmount = BigDecimal.ZERO;
        BigDecimal deliverySellAmount = BigDecimal.ZERO;
        int buyCount = 0;
        int sellCount = 0;
        int intradayCount = 0;
        int deliveryCount = 0;
        Map<String, Integer> stockTradeCount = new LinkedHashMap<>();
        Map<String, Integer> transactionsByMonth = new LinkedHashMap<>();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");

        for (Transaction t : all) {
            LocalDateTime dateForMonth = t.getTradeDate() != null ? t.getTradeDate() : t.getCreatedAt();
            if (dateForMonth != null) {
                String month = dateForMonth.format(monthFormatter);
                transactionsByMonth.merge(month, 1, Integer::sum);
            }

            if (t.getStock() != null) {
                stockTradeCount.merge(t.getStock().getSymbol(), 1, Integer::sum);
            }

            boolean isMIS = t.getTradeType() == TradeType.MIS;

            if (t.getTransactionType() == TransactionType.BUY) {
                totalBuyAmount = totalBuyAmount.add(t.getTotalAmount());
                buyCount++;
                if (isMIS) {
                    intradayBuyAmount = intradayBuyAmount.add(t.getTotalAmount());
                    intradayCount++;
                } else {
                    deliveryBuyAmount = deliveryBuyAmount.add(t.getTotalAmount());
                    deliveryCount++;
                }
            } else if (t.getTransactionType() == TransactionType.SELL) {
                totalSellAmount = totalSellAmount.add(t.getTotalAmount());
                sellCount++;
                if (isMIS) {
                    intradaySellAmount = intradaySellAmount.add(t.getTotalAmount());
                    intradayCount++;
                } else {
                    deliverySellAmount = deliverySellAmount.add(t.getTotalAmount());
                    deliveryCount++;
                }
            } else if (t.getTransactionType() == TransactionType.DEPOSIT) {
                totalDeposits = totalDeposits.add(t.getTotalAmount());
            } else if (t.getTransactionType() == TransactionType.WITHDRAWAL) {
                totalWithdrawals = totalWithdrawals.add(t.getTotalAmount());
            } else if (t.getTransactionType() == TransactionType.CHARGES) {
                totalCharges = totalCharges.add(t.getTotalAmount());
            }
        }

        BigDecimal totalDividends = all.stream()
                .filter(t -> t.getTransactionType() == TransactionType.DIVIDEND)
                .map(Transaction::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal intradayPnL = intradaySellAmount.subtract(intradayBuyAmount);
        BigDecimal deliveryRealizedGains = deliverySellAmount.subtract(
                computeSellCostBasis(all));
        BigDecimal realizedGains = deliveryRealizedGains.add(intradayPnL)
                .add(totalDividends).subtract(totalCharges);

        String mostTradedStock = stockTradeCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        return new TransactionAnalyticsResponse(
                totalBuyAmount,
                totalSellAmount,
                realizedGains,
                all.size(),
                buyCount,
                sellCount,
                mostTradedStock,
                transactionsByMonth,
                totalDeposits,
                totalWithdrawals,
                totalCharges,
                intradayBuyAmount,
                intradaySellAmount,
                intradayPnL,
                deliveryBuyAmount,
                deliverySellAmount,
                deliveryRealizedGains,
                intradayCount,
                deliveryCount);
    }

    private BigDecimal computeSellCostBasis(List<Transaction> all) {
        Map<String, BigDecimal> avgCost = new LinkedHashMap<>();
        Map<String, Integer> qtyHeld = new LinkedHashMap<>();
        BigDecimal totalCostBasis = BigDecimal.ZERO;

        List<Transaction> sorted = all.stream()
                .sorted((a, b) -> {
                    java.time.LocalDateTime dateA = a.getTradeDate() != null ? a.getTradeDate() : a.getCreatedAt();
                    java.time.LocalDateTime dateB = b.getTradeDate() != null ? b.getTradeDate() : b.getCreatedAt();
                    if (dateA == null || dateB == null) return 0;
                    return dateA.compareTo(dateB);
                })
                .toList();

        for (Transaction t : sorted) {
            if (t.getStock() == null) continue;
            if (t.getTradeType() == TradeType.MIS) continue;
            String symbol = t.getStock().getSymbol();
            if (t.getTransactionType() == TransactionType.BUY) {
                int existingQty = qtyHeld.getOrDefault(symbol, 0);
                BigDecimal existingCost = avgCost.getOrDefault(symbol, BigDecimal.ZERO);
                BigDecimal existingInvestment = existingCost
                        .multiply(BigDecimal.valueOf(existingQty));
                int newTotalQty = existingQty + t.getQuantity();
                if (newTotalQty > 0) {
                    BigDecimal newAvg = existingInvestment.add(t.getTotalAmount())
                            .divide(BigDecimal.valueOf(newTotalQty), 4, RoundingMode.HALF_UP);
                    avgCost.put(symbol, newAvg);
                }
                qtyHeld.put(symbol, newTotalQty);
            } else if (t.getTransactionType() == TransactionType.SELL) {
                BigDecimal costPerUnit = avgCost.getOrDefault(symbol, BigDecimal.ZERO);
                int heldQty = qtyHeld.getOrDefault(symbol, 0);
                if (heldQty > 0 && costPerUnit.compareTo(BigDecimal.ZERO) > 0) {
                    totalCostBasis = totalCostBasis.add(
                            costPerUnit.multiply(BigDecimal.valueOf(t.getQuantity())));
                    int remaining = heldQty - t.getQuantity();
                    qtyHeld.put(symbol, Math.max(remaining, 0));
                }
            }
        }

        return totalCostBasis;
    }

    private TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getStock() != null ? transaction.getStock().getSymbol() : null,
                transaction.getQuantity(),
                transaction.getPrice(),
                transaction.getTotalAmount(),
                transaction.getTransactionType(),
                transaction.getDescription(),
                transaction.getTradeDate(),
                transaction.getCreatedAt(),
                transaction.getTradeType());
    }

    private void updateHolding(Transaction transaction) {
        if (transaction.getStock() == null) return;
        Holding holding = holdingRepository
                .findByStock(transaction.getStock())
                .orElseGet(() -> {
                    Holding newHolding = new Holding();
                    newHolding.setStock(transaction.getStock());
                    newHolding.setQuantity(0);
                    newHolding.setAverageBuyPrice(BigDecimal.ZERO);
                    newHolding.setInvestedAmount(BigDecimal.ZERO);
                    return newHolding;
                });

        switch (transaction.getTransactionType()) {
            case BUY -> processBuy(holding, transaction);
            case SELL -> processSell(holding, transaction);
            default -> { }
        }

        holdingRepository.save(holding);
    }

    private void processBuy(Holding holding, Transaction transaction) {
        int totalQty = holding.getQuantity() + transaction.getQuantity();
        BigDecimal totalInvestment = holding.getInvestedAmount()
                .add(transaction.getTotalAmount());
        BigDecimal averagePrice = totalInvestment.divide(
                BigDecimal.valueOf(totalQty), 2, RoundingMode.HALF_UP);

        holding.setQuantity(totalQty);
        holding.setInvestedAmount(totalInvestment);
        holding.setAverageBuyPrice(averagePrice);
    }

    private void processSell(Holding holding, Transaction transaction) {
        int remainingQty = holding.getQuantity() - transaction.getQuantity();
        if (remainingQty < 0) {
            remainingQty = 0;
        }

        BigDecimal remainingInvestment = holding.getAverageBuyPrice()
                .multiply(BigDecimal.valueOf(remainingQty));

        holding.setQuantity(remainingQty);
        holding.setInvestedAmount(remainingInvestment);
    }
}
