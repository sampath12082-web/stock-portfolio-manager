package com.stocks.myportfolio.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.stocks.myportfolio.common.constants.AppConstants;
import com.stocks.myportfolio.common.enums.Exchange;
import com.stocks.myportfolio.common.exception.DuplicateResourceException;
import com.stocks.myportfolio.common.exception.ResourceNotFoundException;
import com.stocks.myportfolio.common.exception.ValidationException;
import com.stocks.myportfolio.dto.request.CreateStockRequest;
import com.stocks.myportfolio.dto.response.StockResponse;
import com.stocks.myportfolio.entity.Stock;
import com.stocks.myportfolio.mapper.StockMapper;
import com.stocks.myportfolio.repository.HoldingRepository;
import com.stocks.myportfolio.repository.StockRepository;
import com.stocks.myportfolio.service.StockLookupService;
import com.stocks.myportfolio.service.StockService;

@Service
public class StockServiceImpl implements StockService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StockServiceImpl.class);

    private final StockRepository stockRepository;
    private final HoldingRepository holdingRepository;
    private final StockMapper stockMapper;
    private final StockLookupService stockLookupService;

    public StockServiceImpl(
            StockRepository stockRepository,
            HoldingRepository holdingRepository,
            StockMapper stockMapper,
            StockLookupService stockLookupService) {

        this.stockRepository = stockRepository;
        this.holdingRepository = holdingRepository;
        this.stockMapper = stockMapper;
        this.stockLookupService = stockLookupService;
    }

    @Override
    public StockResponse createStock(CreateStockRequest request) {
        stockRepository.findBySymbol(request.symbol().toUpperCase())
                .ifPresent(s -> {
                    throw new DuplicateResourceException(
                            AppConstants.STOCK_ALREADY_EXISTS + ": " + request.symbol());
                });

        Stock stock = stockMapper.toEntity(request);
        Stock saved = stockRepository.save(stock);
        return stockMapper.toResponse(saved);
    }

    @Override
    public StockResponse getStockBySymbol(String symbol) {
        Stock stock = stockRepository
                .findBySymbolIgnoreCase(symbol)
                .orElseThrow(() -> new ResourceNotFoundException(
                        AppConstants.STOCK_NOT_FOUND + ": " + symbol));

        return stockMapper.toResponse(stock);
    }

    @Override
    public List<StockResponse> getAllStocks() {
        return stockMapper.toResponseList(stockRepository.findAll());
    }

    @Override
    public List<StockResponse> searchStocks(
            String query, Exchange exchange, String sector) {

        if (query != null && !query.isBlank()) {
            return stockMapper.toResponseList(
                    stockRepository.findBySymbolContainingIgnoreCase(query));
        }
        if (exchange != null) {
            return stockMapper.toResponseList(
                    stockRepository.findByExchange(exchange));
        }
        if (sector != null && !sector.isBlank()) {
            return stockMapper.toResponseList(
                    stockRepository.findBySectorIgnoreCase(sector));
        }
        return stockMapper.toResponseList(stockRepository.findAll());
    }

    @Override
    public void deleteStock(Long id) {
        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        AppConstants.STOCK_NOT_FOUND));

        if (holdingRepository.findByStock(stock).isPresent()) {
            throw new ValidationException(
                    "Cannot delete stock with existing holdings: " + stock.getSymbol());
        }

        stockRepository.delete(stock);
    }

    private static final java.util.Map<String, String[]> SECTOR_MAP = java.util.Map.ofEntries(
            java.util.Map.entry("HDFCBANK", new String[]{"Financial Services", "Banking"}),
            java.util.Map.entry("JSWSTEEL", new String[]{"Materials", "Steel"}),
            java.util.Map.entry("HINDALCO", new String[]{"Materials", "Aluminium"}),
            java.util.Map.entry("TCS", new String[]{"Technology", "IT Services"}),
            java.util.Map.entry("WIPRO", new String[]{"Technology", "IT Services"}),
            java.util.Map.entry("BALRAMCHIN", new String[]{"Consumer Staples", "Sugar"}),
            java.util.Map.entry("RPOWER", new String[]{"Utilities", "Power"}),
            java.util.Map.entry("PCJEWELLER", new String[]{"Consumer Discretionary", "Jewellery"}),
            java.util.Map.entry("OLAELEC", new String[]{"Automobile", "Electric Vehicles"}),
            java.util.Map.entry("SUNPHARMA", new String[]{"Healthcare", "Pharmaceuticals"}),
            java.util.Map.entry("DRREDDY", new String[]{"Healthcare", "Pharmaceuticals"}),
            java.util.Map.entry("BPCL", new String[]{"Energy", "Oil & Gas"}),
            java.util.Map.entry("ONGC", new String[]{"Energy", "Oil & Gas"}),
            java.util.Map.entry("NTPC", new String[]{"Utilities", "Power"}),
            java.util.Map.entry("NHPC", new String[]{"Utilities", "Power"}),
            java.util.Map.entry("COALINDIA", new String[]{"Energy", "Mining"}),
            java.util.Map.entry("VEDL", new String[]{"Materials", "Mining"}),
            java.util.Map.entry("BHEL", new String[]{"Industrials", "Capital Goods"}),
            java.util.Map.entry("BEL", new String[]{"Industrials", "Defence"}),
            java.util.Map.entry("ADANIPOWER", new String[]{"Utilities", "Power"}),
            java.util.Map.entry("SUZLON", new String[]{"Utilities", "Renewable Energy"}),
            java.util.Map.entry("TRENT", new String[]{"Consumer Discretionary", "Retail"}),
            java.util.Map.entry("BAJAJ-AUTO", new String[]{"Automobile", "Two Wheelers"}),
            java.util.Map.entry("BIOCON", new String[]{"Healthcare", "Biotechnology"}),
            java.util.Map.entry("APOLLOHOSP", new String[]{"Healthcare", "Hospitals"}),
            java.util.Map.entry("ASIANPAINT", new String[]{"Consumer Discretionary", "Paints"}),
            java.util.Map.entry("PIDILITIND", new String[]{"Consumer Discretionary", "Chemicals"}),
            java.util.Map.entry("BANKBARODA", new String[]{"Financial Services", "Banking"}),
            java.util.Map.entry("HDFCLIFE", new String[]{"Financial Services", "Insurance"}),
            java.util.Map.entry("BOSCHLTD", new String[]{"Automobile", "Auto Components"}),
            java.util.Map.entry("COFORGE", new String[]{"Technology", "IT Services"}),
            java.util.Map.entry("IDEA", new String[]{"Telecom", "Telecom Services"}),
            java.util.Map.entry("PFC", new String[]{"Financial Services", "NBFC"}),
            java.util.Map.entry("MARICO", new String[]{"Consumer Staples", "FMCG"}),
            java.util.Map.entry("CGPOWER", new String[]{"Industrials", "Electrical Equipment"}),
            java.util.Map.entry("PAYTM", new String[]{"Technology", "Fintech"}),
            java.util.Map.entry("HFCL", new String[]{"Telecom", "Telecom Equipment"})
    );

    @Override
    public int refreshSectorData() {
        List<Stock> needsSector = stockRepository.findAll().stream()
                .filter(s -> s.getSector() == null || s.getSector().isBlank())
                .toList();

        int updated = 0;
        for (Stock stock : needsSector) {
            String[] sectorInfo = SECTOR_MAP.get(stock.getSymbol());
            if (sectorInfo != null) {
                stock.setSector(sectorInfo[0]);
                stock.setIndustry(sectorInfo[1]);
                stockRepository.save(stock);
                updated++;
                log.info("Updated sector for {}: {}/{}", stock.getSymbol(), sectorInfo[0], sectorInfo[1]);
            }
        }
        return updated;
    }
}
