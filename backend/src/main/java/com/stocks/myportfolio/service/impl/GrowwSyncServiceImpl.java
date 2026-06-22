package com.stocks.myportfolio.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;

import com.stocks.myportfolio.common.enums.Exchange;
import com.stocks.myportfolio.common.enums.TransactionType;
import com.stocks.myportfolio.dto.response.GrowwAccountResponse;
import com.stocks.myportfolio.dto.response.GrowwAccountResponse.GrowwOrderSummary;
import com.stocks.myportfolio.dto.response.GrowwAccountResponse.GrowwPositionData;
import com.stocks.myportfolio.dto.response.GrowwSyncResponse;
import com.stocks.myportfolio.dto.response.StockLookupResponse;
import com.stocks.myportfolio.entity.Holding;
import com.stocks.myportfolio.entity.Stock;
import com.stocks.myportfolio.entity.Transaction;
import com.stocks.myportfolio.integration.groww.GrowwClient;
import com.stocks.myportfolio.integration.groww.GrowwHoldingData;
import com.stocks.myportfolio.integration.groww.GrowwOrderData;
import com.stocks.myportfolio.integration.groww.GrowwPortfolioResponse;
import com.stocks.myportfolio.repository.HoldingRepository;
import com.stocks.myportfolio.repository.StockRepository;
import com.stocks.myportfolio.repository.TransactionRepository;
import com.stocks.myportfolio.security.CurrentUserProvider;
import com.stocks.myportfolio.service.GrowwSyncService;
import com.stocks.myportfolio.service.StockLookupService;

@Service
@ConditionalOnProperty(name = "groww.api.enabled", havingValue = "true")
public class GrowwSyncServiceImpl implements GrowwSyncService {

    private static final Logger log = LoggerFactory.getLogger(GrowwSyncServiceImpl.class);

    private final GrowwClient growwClient;
    private final StockRepository stockRepository;
    private final HoldingRepository holdingRepository;
    private final TransactionRepository transactionRepository;
    private final StockLookupService stockLookupService;
    private final CurrentUserProvider currentUser;

    public GrowwSyncServiceImpl(
            GrowwClient growwClient,
            StockRepository stockRepository,
            HoldingRepository holdingRepository,
            TransactionRepository transactionRepository,
            StockLookupService stockLookupService,
            CurrentUserProvider currentUser) {

        this.growwClient = growwClient;
        this.stockRepository = stockRepository;
        this.holdingRepository = holdingRepository;
        this.transactionRepository = transactionRepository;
        this.stockLookupService = stockLookupService;
        this.currentUser = currentUser;
    }

    @Override
    @Transactional
    public GrowwSyncResponse syncPortfolio() {
        GrowwPortfolioResponse portfolio = growwClient.getHoldings();
        List<GrowwHoldingData> growwHoldings = portfolio.getHoldings();

        int stocksCreated = 0;
        int holdingsCreated = 0;
        int holdingsUpdated = 0;
        List<String> errors = new ArrayList<>();

        java.util.Set<Long> growwStockIds = new java.util.HashSet<>();

        for (GrowwHoldingData gh : growwHoldings) {
            try {
                Stock stock = findOrCreateStock(gh);
                if (stock.getId() == null) {
                    stock = stockRepository.save(stock);
                    stocksCreated++;
                    log.info("Created stock: {}", stock.getSymbol());
                }

                growwStockIds.add(stock.getId());

                int qty = gh.getQuantity().intValue();
                BigDecimal avgPrice = gh.getAveragePrice();
                BigDecimal invested = avgPrice.multiply(BigDecimal.valueOf(qty));

                Holding holding = holdingRepository.findByStock(stock)
                        .orElse(null);

                if (holding == null) {
                    holding = new Holding();
                    holding.setStock(stock);
                    holding.setQuantity(qty);
                    holding.setAverageBuyPrice(avgPrice);
                    holding.setInvestedAmount(invested);
                    holdingRepository.save(holding);
                    holdingsCreated++;
                } else {
                    holding.setQuantity(qty);
                    holding.setAverageBuyPrice(avgPrice);
                    holding.setInvestedAmount(invested);
                    holdingRepository.save(holding);
                    holdingsUpdated++;
                }
            } catch (Exception e) {
                String msg = gh.getTradingSymbol() + ": " + e.getMessage();
                log.warn("Failed to sync holding: {}", msg);
                errors.add(msg);
            }
        }

        // Zero out holdings not present in Groww (sold stocks)
        List<Holding> allHoldings = holdingRepository.findByUserId(currentUser.getUserId());
        for (Holding h : allHoldings) {
            if (h.getQuantity() > 0 && !growwStockIds.contains(h.getStock().getId())) {
                log.info("Zeroing out stale holding: {} (qty was {})",
                        h.getStock().getSymbol(), h.getQuantity());
                h.setQuantity(0);
                h.setInvestedAmount(BigDecimal.ZERO);
                holdingRepository.save(h);
                holdingsUpdated++;
            }
        }

        log.info("Groww sync complete: {} stocks created, {} holdings created, {} updated, {} errors",
                stocksCreated, holdingsCreated, holdingsUpdated, errors.size());

        return new GrowwSyncResponse(stocksCreated, holdingsCreated, holdingsUpdated, errors);
    }

    @Override
    @Transactional
    public GrowwSyncResponse syncOrders() {
        List<GrowwOrderData> orders = growwClient.getOrders();

        int stocksCreated = 0;
        int transactionsCreated = 0;
        List<String> errors = new ArrayList<>();

        for (GrowwOrderData order : orders) {
            try {
                String symbol = order.getTradingSymbol();
                Exchange exchange = "BSE".equals(order.getExchange())
                        ? Exchange.BSE : Exchange.NSE;

                Stock stock = stockRepository.findBySymbolIgnoreCase(symbol)
                        .orElseGet(() -> {
                            Stock s = new Stock();
                            s.setSymbol(symbol.toUpperCase());
                            s.setExchange(exchange);

                            List<StockLookupResponse> lookup = stockLookupService.lookup(symbol);
                            for (StockLookupResponse lr : lookup) {
                                if (lr.symbol().equalsIgnoreCase(symbol) && !lr.existsInDb()) {
                                    s.setCompanyName(lr.companyName());
                                    s.setSector(lr.sector());
                                    s.setIndustry(lr.industry());
                                    break;
                                }
                            }
                            if (s.getCompanyName() == null) {
                                s.setCompanyName(symbol);
                            }
                            return stockRepository.save(s);
                        });

                if (stock.getId() != null
                        && stockRepository.findBySymbolIgnoreCase(symbol).isPresent()
                        && !stockRepository.findBySymbolIgnoreCase(symbol).get().equals(stock)) {
                    stocksCreated++;
                }

                TransactionType txnType = "SELL".equalsIgnoreCase(order.getTransactionType())
                        ? TransactionType.SELL : TransactionType.BUY;

                int qty = order.getFilledQuantity();
                BigDecimal price = order.getAverageFillPrice();
                BigDecimal totalAmount = price.multiply(BigDecimal.valueOf(qty));

                Transaction txn = new Transaction();
                txn.setStock(stock);
                txn.setQuantity(qty);
                txn.setPrice(price);
                txn.setTotalAmount(totalAmount);
                txn.setTransactionType(txnType);
                txn.setExchange(exchange);

                if (order.getTradeDate() != null && !order.getTradeDate().isBlank()) {
                    try {
                        txn.setTradeDate(java.time.LocalDateTime.parse(
                                order.getTradeDate().replace(" ", "T")));
                    } catch (Exception e) {
                        try {
                            txn.setTradeDate(java.time.LocalDate.parse(order.getTradeDate(),
                                    java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                                    .atStartOfDay());
                        } catch (Exception ex) {
                            txn.setTradeDate(java.time.LocalDateTime.now());
                        }
                    }
                } else {
                    txn.setTradeDate(java.time.LocalDateTime.now());
                }

                transactionRepository.save(txn);
                transactionsCreated++;

                log.info("Synced order: {} {} {} x {} @ {}",
                        txnType, symbol, qty, price, order.getTradeDate());

            } catch (Exception e) {
                String msg = order.getTradingSymbol() + ": " + e.getMessage();
                log.warn("Failed to sync order: {}", msg);
                errors.add(msg);
            }
        }

        log.info("Order sync complete: {} transactions, {} stocks created, {} errors",
                transactionsCreated, stocksCreated, errors.size());

        return new GrowwSyncResponse(stocksCreated, transactionsCreated, 0, errors);
    }

    @Override
    public GrowwAccountResponse getAccountDetails() {
        JsonNode profile = growwClient.getUserProfile();
        JsonNode margin = growwClient.getMarginDetails();
        JsonNode positionsNode = growwClient.getPositions();
        List<GrowwOrderData> orders = growwClient.getOrders();

        List<String> segments = new ArrayList<>();
        JsonNode segNode = profile.has("activeSegments")
                ? profile.path("activeSegments")
                : profile.path("active_segments");
        for (JsonNode seg : segNode) {
            segments.add(seg.asText());
        }

        BigDecimal availableCash = BigDecimal.valueOf(
                margin.path("equity_margin_details").path("cnc_balance_available").asDouble(0));
        BigDecimal clearCash = BigDecimal.valueOf(margin.path("clear_cash").asDouble(0));
        BigDecimal marginUsed = BigDecimal.valueOf(margin.path("net_margin_used").asDouble(0));

        List<GrowwPositionData> positions = new ArrayList<>();
        for (JsonNode p : positionsNode.path("positions")) {
            positions.add(new GrowwPositionData(
                    p.path("trading_symbol").asText(),
                    p.path("exchange").asText(),
                    p.path("quantity").asInt(),
                    BigDecimal.valueOf(p.path("net_price").asDouble()),
                    BigDecimal.valueOf(p.path("realised_pnl").asDouble()),
                    p.path("product").asText()));
        }

        List<GrowwOrderSummary> orderSummaries = new ArrayList<>();
        for (GrowwOrderData o : orders) {
            orderSummaries.add(new GrowwOrderSummary(
                    o.getTradingSymbol(),
                    o.getTransactionType(),
                    "EXECUTED",
                    o.getQuantity(),
                    o.getFilledQuantity(),
                    o.getAverageFillPrice(),
                    o.getAverageFillPrice(),
                    o.getExchange(),
                    o.getTradeDate()));
        }

        // Also add non-executed orders
        try {
            JsonNode allOrdersNode = growwClient.getAllOrders();
            for (JsonNode node : allOrdersNode) {
                String status = node.path("order_status").asText();
                if (!"EXECUTED".equals(status)) {
                    orderSummaries.add(new GrowwOrderSummary(
                            node.path("trading_symbol").asText(),
                            node.path("transaction_type").asText(),
                            status,
                            node.path("quantity").asInt(),
                            node.path("filled_quantity").asInt(),
                            BigDecimal.valueOf(node.path("price").asDouble()),
                            BigDecimal.valueOf(node.path("average_fill_price").asDouble()),
                            node.path("exchange").asText(),
                            node.path("trade_date").asText()));
                }
            }
        } catch (Exception e) {
            log.warn("Failed to fetch full order list: {}", e.getMessage());
        }

        return new GrowwAccountResponse(
                profile.has("vendorUserId")
                        ? profile.path("vendorUserId").asText()
                        : profile.path("vendor_user_id").asText(),
                profile.path("ucc").asText(),
                profile.has("nseEnabled")
                        ? profile.path("nseEnabled").asBoolean()
                        : profile.path("nse_enabled").asBoolean(),
                profile.has("bseEnabled")
                        ? profile.path("bseEnabled").asBoolean()
                        : profile.path("bse_enabled").asBoolean(),
                segments,
                availableCash,
                clearCash,
                marginUsed,
                positions,
                orderSummaries);
    }

    private Stock findOrCreateStock(GrowwHoldingData gh) {
        String symbol = gh.getTradingSymbol();

        return stockRepository.findBySymbolIgnoreCase(symbol)
                .orElseGet(() -> {
                    Stock stock = new Stock();
                    stock.setSymbol(symbol.toUpperCase());

                    Exchange exchange = Exchange.NSE;
                    if (gh.getTradableExchanges() != null
                            && !gh.getTradableExchanges().contains("NSE")
                            && gh.getTradableExchanges().contains("BSE")) {
                        exchange = Exchange.BSE;
                    }
                    stock.setExchange(exchange);

                    List<StockLookupResponse> lookupResults =
                            stockLookupService.lookup(symbol);
                    for (StockLookupResponse lr : lookupResults) {
                        if (lr.symbol().equalsIgnoreCase(symbol) && !lr.existsInDb()) {
                            stock.setCompanyName(lr.companyName());
                            stock.setSector(lr.sector());
                            stock.setIndustry(lr.industry());
                            break;
                        }
                    }

                    if (stock.getCompanyName() == null) {
                        stock.setCompanyName(symbol);
                    }

                    return stock;
                });
    }
}
