package com.stocks.myportfolio.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.stocks.myportfolio.common.enums.Exchange;
import com.stocks.myportfolio.common.enums.TransactionType;
import com.stocks.myportfolio.dto.response.StockLookupResponse;
import com.stocks.myportfolio.dto.response.TransactionUploadResponse;
import com.stocks.myportfolio.entity.Holding;
import com.stocks.myportfolio.entity.Stock;
import com.stocks.myportfolio.entity.Transaction;
import com.stocks.myportfolio.repository.HoldingRepository;
import com.stocks.myportfolio.repository.StockRepository;
import com.stocks.myportfolio.repository.TransactionRepository;
import com.stocks.myportfolio.service.StockLookupService;
import com.stocks.myportfolio.service.TransactionUploadService;

@Service
public class TransactionUploadServiceImpl implements TransactionUploadService {

    private static final Logger log = LoggerFactory.getLogger(TransactionUploadServiceImpl.class);

    // Trade report: DATE SYMBOL EXCHANGE BUY/SELL QTY PRICE
    private static final Pattern TRADE_LINE = Pattern.compile(
            "(\\d{2}/\\d{2}/\\d{4})\\s+" +
            "([A-Z][A-Z0-9&]+)\\s+" +
            "(NSE|BSE)\\s+" +
            "(BUY|SELL|B|S)\\s+" +
            "(\\d+)\\s+" +
            "([\\d,.]+)");

    // Groww Ledger settlement: DATE DATE CM STOCKS_SETTLEMENT ... CREDIT DEBIT BALANCE
    private static final Pattern LEDGER_SETTLEMENT = Pattern.compile(
            "(\\d{2}/\\d{2}/\\d{4})\\s+" +
            "\\d{2}/\\d{2}/\\d{4}\\s+" +
            "CM\\s+" +
            "STOCKS_SETTLEMENT\\s+" +
            "\\S+\\s+" +
            "\\S+\\s+" +
            "\\S+\\s+" +
            "\\S+\\s+" +
            "\\S+\\s+" +
            "([\\d,.]+)\\s+" +
            "([\\d,.]+)\\s+" +
            "([\\d,.]+)");

    private final TransactionRepository transactionRepository;
    private final StockRepository stockRepository;
    private final HoldingRepository holdingRepository;
    private final StockLookupService stockLookupService;

    public TransactionUploadServiceImpl(
            TransactionRepository transactionRepository,
            StockRepository stockRepository,
            HoldingRepository holdingRepository,
            StockLookupService stockLookupService) {

        this.transactionRepository = transactionRepository;
        this.stockRepository = stockRepository;
        this.holdingRepository = holdingRepository;
        this.stockLookupService = stockLookupService;
    }

    @Override
    @Transactional
    public TransactionUploadResponse uploadPdf(MultipartFile file) {
        String text;
        try (PDDocument doc = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            text = stripper.getText(doc);
        } catch (Exception e) {
            log.error("Failed to parse PDF: {}", e.getMessage());
            return new TransactionUploadResponse(0, 0,
                    List.of("Failed to parse PDF: " + e.getMessage()));
        }

        boolean isLedger = text.contains("Statement of accounts")
                || text.contains("STOCKS_SETTLEMENT");

        if (isLedger) {
            return parseLedger(text);
        }
        return parseTradeReport(text);
    }

    private TransactionUploadResponse parseLedger(String text) {
        List<String> info = new ArrayList<>();
        int settlements = 0;
        BigDecimal totalCredit = BigDecimal.ZERO;
        BigDecimal totalDebit = BigDecimal.ZERO;

        for (String line : text.split("\\r?\\n")) {
            Matcher m = LEDGER_SETTLEMENT.matcher(line.trim());
            if (m.find()) {
                settlements++;
                BigDecimal credit = parseMoney(m.group(2));
                BigDecimal debit = parseMoney(m.group(3));
                totalCredit = totalCredit.add(credit);
                totalDebit = totalDebit.add(debit);
            }
        }

        if (settlements == 0) {
            // Fallback: count settlement lines by keyword
            for (String line : text.split("\\r?\\n")) {
                if (line.contains("STOCKS_SETTLEMENT")) {
                    settlements++;
                }
            }
        }

        info.add("Parsed Groww Ledger: " + settlements + " settlement entries found");
        if (totalCredit.compareTo(BigDecimal.ZERO) > 0) {
            info.add("Total credit (sell proceeds): Rs " + totalCredit.toPlainString());
        }
        if (totalDebit.compareTo(BigDecimal.ZERO) > 0) {
            info.add("Total debit (buy payments): Rs " + totalDebit.toPlainString());
        }
        info.add("Note: This is a funds ledger. Individual stock trades (symbol, qty, price) are not available in this file. "
                + "To import stock-level transactions, please upload a Groww Contract Note or Trade History report.");

        log.info("Ledger parsed: {} settlements, credit={}, debit={}",
                settlements, totalCredit, totalDebit);

        return new TransactionUploadResponse(0, 0, info);
    }

    private TransactionUploadResponse parseTradeReport(String text) {
        int transactionsCreated = 0;
        int stocksCreated = 0;
        List<String> errors = new ArrayList<>();

        for (String line : text.split("\\r?\\n")) {
            Matcher m = TRADE_LINE.matcher(line.trim());
            if (!m.find()) {
                continue;
            }

            try {
                String symbol = m.group(2);
                String exchangeStr = m.group(3);
                String typeStr = m.group(4);
                int quantity = Integer.parseInt(m.group(5));
                BigDecimal price = parseMoney(m.group(6));

                Exchange exchange = "BSE".equals(exchangeStr) ? Exchange.BSE : Exchange.NSE;
                TransactionType txnType = parseType(typeStr);

                Stock stock = findOrCreateStock(symbol, exchange);
                if (stockRepository.findBySymbolIgnoreCase(symbol).isEmpty()) {
                    stocksCreated++;
                }

                BigDecimal totalAmount = price.multiply(BigDecimal.valueOf(quantity));

                Transaction txn = new Transaction();
                txn.setStock(stock);
                txn.setQuantity(quantity);
                txn.setPrice(price);
                txn.setTotalAmount(totalAmount);
                txn.setTransactionType(txnType);
                txn.setExchange(exchange);
                transactionRepository.save(txn);

                updateHolding(stock, txnType, quantity, totalAmount);
                transactionsCreated++;

            } catch (Exception e) {
                errors.add("Line parse error: " + e.getMessage());
            }
        }

        if (transactionsCreated == 0 && errors.isEmpty()) {
            errors.add("No trade entries found in this PDF. Expected format: DATE SYMBOL EXCHANGE BUY/SELL QTY PRICE");
        }

        return new TransactionUploadResponse(transactionsCreated, stocksCreated, errors);
    }

    private Stock findOrCreateStock(String symbol, Exchange exchange) {
        return stockRepository.findBySymbolIgnoreCase(symbol)
                .orElseGet(() -> {
                    Stock stock = new Stock();
                    stock.setSymbol(symbol.toUpperCase());
                    stock.setExchange(exchange);

                    List<StockLookupResponse> lookup = stockLookupService.lookup(symbol);
                    for (StockLookupResponse lr : lookup) {
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
                    return stockRepository.save(stock);
                });
    }

    private BigDecimal parseMoney(String value) {
        return new BigDecimal(value.replace(",", ""));
    }

    private TransactionType parseType(String type) {
        return switch (type.toUpperCase()) {
            case "BUY", "B" -> TransactionType.BUY;
            case "SELL", "S" -> TransactionType.SELL;
            default -> TransactionType.BUY;
        };
    }

    private void updateHolding(Stock stock, TransactionType type, int qty,
                               BigDecimal totalAmount) {
        Holding holding = holdingRepository.findByStock(stock)
                .orElseGet(() -> {
                    Holding h = new Holding();
                    h.setStock(stock);
                    h.setQuantity(0);
                    h.setAverageBuyPrice(BigDecimal.ZERO);
                    h.setInvestedAmount(BigDecimal.ZERO);
                    return h;
                });

        if (type == TransactionType.BUY) {
            int newQty = holding.getQuantity() + qty;
            BigDecimal newInvested = holding.getInvestedAmount().add(totalAmount);
            BigDecimal newAvg = newInvested.divide(
                    BigDecimal.valueOf(newQty), 2, java.math.RoundingMode.HALF_UP);
            holding.setQuantity(newQty);
            holding.setInvestedAmount(newInvested);
            holding.setAverageBuyPrice(newAvg);
        } else if (type == TransactionType.SELL) {
            int newQty = Math.max(0, holding.getQuantity() - qty);
            BigDecimal newInvested = holding.getAverageBuyPrice()
                    .multiply(BigDecimal.valueOf(newQty));
            holding.setQuantity(newQty);
            holding.setInvestedAmount(newInvested);
        }

        holdingRepository.save(holding);
    }
}
