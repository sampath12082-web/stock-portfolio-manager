package com.stocks.myportfolio.integration.groww;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;

import com.stocks.myportfolio.common.exception.MarketDataException;

@Component
@ConditionalOnProperty(name = "groww.api.enabled", havingValue = "true")
public class GrowwClient {

    private static final Logger log = LoggerFactory.getLogger(GrowwClient.class);

    private final GrowwProperties properties;
    private volatile String sessionToken;
    private volatile long sessionExpiresAt;

    public GrowwClient(GrowwProperties properties) {
        this.properties = properties;
    }

    public GrowwStockResponse getQuote(String tradingSymbol, String exchange) {
        try {
            return buildClient().get()
                    .uri("/v1/live-data/quote?exchange={exchange}&segment=CASH&trading_symbol={symbol}",
                            exchange, tradingSymbol)
                    .retrieve()
                    .body(GrowwStockResponse.class);
        } catch (Exception e) {
            throw new MarketDataException(
                    "Failed to fetch quote for " + tradingSymbol, e);
        }
    }

    public Map<String, GrowwStockResponse> getOhlc(List<String> exchangeSymbols) {
        try {
            Map<String, GrowwStockResponse> allResults = new HashMap<>();

            for (int i = 0; i < exchangeSymbols.size(); i += 50) {
                List<String> batch = exchangeSymbols.subList(
                        i, Math.min(i + 50, exchangeSymbols.size()));
                String symbols = batch.stream()
                        .collect(Collectors.joining(","));

                Map<String, GrowwStockResponse> batchResult = buildClient().get()
                        .uri("/v1/live-data/ohlc?segment=CASH&exchange_symbols={symbols}",
                                symbols)
                        .retrieve()
                        .body(new ParameterizedTypeReference<>() {});

                if (batchResult != null) {
                    allResults.putAll(batchResult);
                }
            }

            return allResults;
        } catch (Exception e) {
            throw new MarketDataException(
                    "Failed to fetch OHLC data", e);
        }
    }

    public GrowwPortfolioResponse getHoldings() {
        try {
            JsonNode root = buildClient().get()
                    .uri("/v1/holdings/user")
                    .retrieve()
                    .body(JsonNode.class);

            JsonNode payload = root.path("payload");
            if (payload.isMissingNode()) {
                throw new MarketDataException("Groww holdings response missing payload");
            }

            GrowwPortfolioResponse response = new GrowwPortfolioResponse();
            java.util.List<GrowwHoldingData> holdings = new java.util.ArrayList<>();

            for (JsonNode node : payload.path("holdings")) {
                GrowwHoldingData h = new GrowwHoldingData();
                h.setTradingSymbol(node.path("trading_symbol").asText());
                h.setQuantity(java.math.BigDecimal.valueOf(node.path("quantity").asDouble()));
                h.setAveragePrice(java.math.BigDecimal.valueOf(node.path("average_price").asDouble()));

                java.util.List<String> exchanges = new java.util.ArrayList<>();
                for (JsonNode ex : node.path("tradable_exchanges")) {
                    exchanges.add(ex.asText());
                }
                h.setTradableExchanges(exchanges);
                holdings.add(h);
            }

            response.setHoldings(holdings);
            return response;
        } catch (MarketDataException e) {
            throw e;
        } catch (Exception e) {
            throw new MarketDataException(
                    "Failed to fetch holdings from Groww", e);
        }
    }

    public java.util.List<GrowwOrderData> getOrders() {
        try {
            java.util.List<GrowwOrderData> allOrders = new java.util.ArrayList<>();
            int page = 0;

            while (true) {
                JsonNode root = buildClient().get()
                        .uri("/v1/order/list?page={page}&page_size=50", page)
                        .retrieve()
                        .body(JsonNode.class);

                JsonNode payload = root.path("payload");
                JsonNode orderList = payload.has("order_list")
                        ? payload.path("order_list")
                        : root.path("order_list");

                if (orderList.isMissingNode() || orderList.isEmpty()) {
                    break;
                }

                for (JsonNode node : orderList) {
                    if (!"EXECUTED".equals(node.path("order_status").asText())) {
                        continue;
                    }
                    if (!"CASH".equals(node.path("segment").asText())) {
                        continue;
                    }

                    GrowwOrderData order = new GrowwOrderData();
                    order.setGrowwOrderId(node.path("groww_order_id").asText());
                    order.setTradingSymbol(node.path("trading_symbol").asText());
                    order.setOrderStatus(node.path("order_status").asText());
                    order.setQuantity(node.path("quantity").asInt());
                    order.setFilledQuantity(node.path("filled_quantity").asInt());
                    order.setAverageFillPrice(java.math.BigDecimal.valueOf(
                            node.path("average_fill_price").asDouble()));
                    order.setExchange(node.path("exchange").asText());
                    order.setTransactionType(node.path("transaction_type").asText());
                    order.setSegment(node.path("segment").asText());
                    order.setProduct(node.path("product").asText());
                    order.setTradeDate(node.path("trade_date").asText());
                    order.setCreatedAt(node.path("created_at").asText());
                    allOrders.add(order);
                }

                if (orderList.size() < 50) {
                    break;
                }
                page++;
            }

            return allOrders;
        } catch (Exception e) {
            throw new MarketDataException("Failed to fetch orders from Groww", e);
        }
    }

    public JsonNode getAllOrders() {
        try {
            JsonNode root = buildClient().get()
                    .uri("/v1/order/list?page=0&page_size=100")
                    .retrieve()
                    .body(JsonNode.class);
            JsonNode payload = root.path("payload");
            return payload.has("order_list") ? payload.path("order_list") : root.path("order_list");
        } catch (Exception e) {
            throw new MarketDataException("Failed to fetch all orders from Groww", e);
        }
    }

    public JsonNode getMarginDetails() {
        try {
            JsonNode root = buildClient().get()
                    .uri("/v1/margins/detail/user")
                    .retrieve()
                    .body(JsonNode.class);
            return root.has("payload") ? root.path("payload") : root;
        } catch (Exception e) {
            throw new MarketDataException("Failed to fetch margin details from Groww", e);
        }
    }

    public JsonNode getPositions() {
        try {
            JsonNode root = buildClient().get()
                    .uri("/v1/positions/user?segment=CASH")
                    .retrieve()
                    .body(JsonNode.class);
            return root.has("payload") ? root.path("payload") : root;
        } catch (Exception e) {
            throw new MarketDataException("Failed to fetch positions from Groww", e);
        }
    }

    public JsonNode getUserProfile() {
        try {
            JsonNode root = buildClient().get()
                    .uri("/v1/user/profile")
                    .retrieve()
                    .body(JsonNode.class);
            if (root.has("success")) {
                return root.path("success").path("data");
            }
            return root.has("payload") ? root.path("payload") : root;
        } catch (Exception e) {
            throw new MarketDataException("Failed to fetch user profile from Groww", e);
        }
    }

    private RestClient buildClient() {
        String token = getOrRefreshSessionToken();
        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + token)
                .defaultHeader("Accept", "application/json")
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("x-api-version", properties.getApiVersion())
                .defaultHeader("x-request-id", UUID.randomUUID().toString())
                .defaultHeader("x-client-id", "growwapi")
                .defaultHeader("x-client-platform", "myportfolio-java")
                .defaultHeader("x-client-platform-version", "1.0.0")
                .build();
    }

    private synchronized String getOrRefreshSessionToken() {
        long now = System.currentTimeMillis() / 1000;
        if (sessionToken != null && now < sessionExpiresAt - 300) {
            return sessionToken;
        }

        log.debug("Obtaining Groww API session");
        try {
            long timestamp = System.currentTimeMillis() / 1000;
            String checksum = generateChecksum(
                    properties.getApiSecret(), String.valueOf(timestamp));

            Map<String, Object> body = Map.of(
                    "key_type", "approval",
                    "checksum", checksum,
                    "timestamp", timestamp);

            RestClient loginClient = RestClient.builder()
                    .baseUrl(properties.getBaseUrl())
                    .defaultHeader("Authorization", "Bearer " + properties.getAccessToken())
                    .defaultHeader("Content-Type", "application/json")
                    .defaultHeader("Accept", "application/json")
                    .defaultHeader("X-API-VERSION", properties.getApiVersion())
                    .defaultHeader("x-request-id", UUID.randomUUID().toString())
                    .build();

            JsonNode response = loginClient.post()
                    .uri("/v1/token/api/access")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);

            sessionToken = response.path("token").asText();
            sessionExpiresAt = timestamp + 86400;
            log.debug("Groww session established");
            return sessionToken;

        } catch (Exception e) {
            throw new MarketDataException(
                    "Failed to obtain Groww session token", e);
        }
    }

    private static String generateChecksum(String secret, String timestamp) {
        try {
            String input = secret + timestamp;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new MarketDataException("Failed to generate checksum", e);
        }
    }
}
