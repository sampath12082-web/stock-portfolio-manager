package com.stocks.myportfolio.integration.groww;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
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
import com.stocks.myportfolio.entity.UserGrowwConfig;
import com.stocks.myportfolio.repository.UserGrowwConfigRepository;
import com.stocks.myportfolio.security.CurrentUserProvider;

@Component
@ConditionalOnProperty(name = "groww.api.enabled", havingValue = "true")
public class GrowwClient {

    private static final Logger log = LoggerFactory.getLogger(GrowwClient.class);

    private final GrowwProperties properties;
    private final UserGrowwConfigRepository growwConfigRepo;
    private final CurrentUserProvider currentUser;

    private record SessionEntry(String token, long expiresAt) {}
    private final ConcurrentHashMap<Long, SessionEntry> userSessions = new ConcurrentHashMap<>();

    public GrowwClient(GrowwProperties properties, UserGrowwConfigRepository growwConfigRepo,
            CurrentUserProvider currentUser) {
        this.properties = properties;
        this.growwConfigRepo = growwConfigRepo;
        this.currentUser = currentUser;
    }

    private UserGrowwConfig getUserConfig() {
        Long userId = currentUser.getUserId();
        if (userId == null) throw new MarketDataException("Not authenticated");
        return growwConfigRepo.findByUserId(userId)
                .orElseThrow(() -> new MarketDataException("Groww not configured. Set credentials in Profile → Groww Config."));
    }

    public Map<String, Object> validateCredentials(String accessToken, String apiSecret) {
        try {
            long timestamp = System.currentTimeMillis() / 1000;
            String checksum = generateChecksum(apiSecret, String.valueOf(timestamp));

            RestClient loginClient = RestClient.builder()
                    .baseUrl(properties.getBaseUrl())
                    .defaultHeader("Authorization", "Bearer " + accessToken)
                    .defaultHeader("Content-Type", "application/json")
                    .defaultHeader("Accept", "application/json")
                    .defaultHeader("X-API-VERSION", properties.getApiVersion())
                    .defaultHeader("x-request-id", UUID.randomUUID().toString())
                    .build();

            JsonNode response = loginClient.post()
                    .uri("/v1/token/api/access")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("key_type", "approval", "checksum", checksum, "timestamp", timestamp))
                    .retrieve()
                    .body(JsonNode.class);

            String token = response != null ? response.path("token").asText("") : "";
            if (!token.isBlank()) {
                Long userId = currentUser.getUserId();
                if (userId != null) {
                    userSessions.put(userId, new SessionEntry(token, timestamp + 86400));
                }
                return Map.of("valid", true, "message", "Connected to Groww successfully");
            }
            return Map.of("valid", false, "message", "Groww returned empty session token");
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("401")) return Map.of("valid", false, "message", "Invalid access token — may be expired. Get a fresh token from Groww.");
            if (msg != null && msg.contains("403")) return Map.of("valid", false, "message", "Invalid API secret or checksum mismatch.");
            return Map.of("valid", false, "message", "Connection failed: " + (msg != null ? msg : "Unknown error"));
        }
    }

    public GrowwStockResponse getQuote(String tradingSymbol, String exchange) {
        try {
            return buildClient().get()
                    .uri("/v1/live-data/quote?exchange={exchange}&segment=CASH&trading_symbol={symbol}",
                            exchange, tradingSymbol)
                    .retrieve()
                    .body(GrowwStockResponse.class);
        } catch (MarketDataException e) { throw e; } catch (Exception e) {
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
                        .uri("/v1/live-data/ohlc?exchange_symbols=" + symbols)
                        .retrieve()
                        .body(new ParameterizedTypeReference<>() {
                        });

                if (batchResult != null) {
                    allResults.putAll(batchResult);
                }
            }
            return allResults;
        } catch (MarketDataException e) { throw e; } catch (Exception e) {
            throw new MarketDataException("Failed to fetch OHLC data", e);
        }
    }

    public GrowwPortfolioResponse getHoldings() {
        try {
            return buildClient().get()
                    .uri("/v1/user/portfolio/holdings")
                    .retrieve()
                    .body(GrowwPortfolioResponse.class);
        } catch (MarketDataException e) { throw e; } catch (Exception e) {
            throw new MarketDataException("Failed to fetch holdings from Groww", e);
        }
    }

    public JsonNode getTodayPositions() {
        try {
            return buildClient().get()
                    .uri("/v1/user/positions?type=TODAYS")
                    .retrieve()
                    .body(JsonNode.class);
        } catch (MarketDataException e) { throw e; } catch (Exception e) {
            throw new MarketDataException("Failed to fetch positions from Groww", e);
        }
    }

    public JsonNode getTodayOrders() {
        try {
            return buildClient().get()
                    .uri("/v1/user/orders?type=ALL&status=ALL")
                    .retrieve()
                    .body(JsonNode.class);
        } catch (MarketDataException e) { throw e; } catch (Exception e) {
            throw new MarketDataException("Failed to fetch orders from Groww", e);
        }
    }

    public JsonNode getAccountDetails() {
        try {
            return buildClient().get()
                    .uri("/v1/user/details")
                    .retrieve()
                    .body(JsonNode.class);
        } catch (MarketDataException e) { throw e; } catch (Exception e) {
            throw new MarketDataException("Failed to fetch account details from Groww", e);
        }
    }

    public List<GrowwOrderData> getOrders() {
        try {
            JsonNode root = buildClient().get()
                    .uri("/v1/user/orders?type=ALL&status=ALL")
                    .retrieve()
                    .body(JsonNode.class);
            List<GrowwOrderData> orders = new java.util.ArrayList<>();
            if (root != null && root.isArray()) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                for (JsonNode node : root) {
                    orders.add(mapper.treeToValue(node, GrowwOrderData.class));
                }
            }
            return orders;
        } catch (MarketDataException e) { throw e; } catch (Exception e) {
            throw new MarketDataException("Failed to fetch orders from Groww", e);
        }
    }

    public JsonNode getMarginDetails() {
        try {
            return buildClient().get()
                    .uri("/v1/user/margin")
                    .retrieve()
                    .body(JsonNode.class);
        } catch (MarketDataException e) { throw e; } catch (Exception e) {
            throw new MarketDataException("Failed to fetch margin from Groww", e);
        }
    }

    public JsonNode getPositions() {
        try {
            return buildClient().get()
                    .uri("/v1/user/positions?type=TODAYS")
                    .retrieve()
                    .body(JsonNode.class);
        } catch (MarketDataException e) { throw e; } catch (Exception e) {
            throw new MarketDataException("Failed to fetch positions from Groww", e);
        }
    }

    public JsonNode getUserProfile() {
        try {
            JsonNode root = buildClient().get()
                    .uri("/v1/user/profile")
                    .retrieve()
                    .body(JsonNode.class);
            if (root == null) {
                throw new MarketDataException("Empty response from Groww profile");
            }
            return root.has("payload") ? root.path("payload") : root;
        } catch (MarketDataException e) { throw e; } catch (Exception e) {
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
        UserGrowwConfig config = getUserConfig();
        Long userId = currentUser.getUserId();

        long now = System.currentTimeMillis() / 1000;
        SessionEntry cached = userSessions.get(userId);
        if (cached != null && now < cached.expiresAt() - 300) {
            return cached.token();
        }

        String accessToken = config.getAccessTokenEncrypted();
        String apiSecret = config.getApiSecretEncrypted();

        if (accessToken == null || apiSecret == null) {
            throw new MarketDataException("Groww credentials incomplete. Update in Profile → Groww Config.");
        }

        log.debug("Obtaining Groww API session for user {}", userId);
        try {
            long timestamp = System.currentTimeMillis() / 1000;
            String checksum = generateChecksum(apiSecret, String.valueOf(timestamp));

            Map<String, Object> body = Map.of(
                    "key_type", "approval",
                    "checksum", checksum,
                    "timestamp", timestamp);

            RestClient loginClient = RestClient.builder()
                    .baseUrl(properties.getBaseUrl())
                    .defaultHeader("Authorization", "Bearer " + accessToken)
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

            String sessionToken = response.path("token").asText();
            userSessions.put(userId, new SessionEntry(sessionToken, timestamp + 86400));
            log.debug("Groww session established for user {}", userId);
            return sessionToken;

        } catch (MarketDataException e) { throw e; } catch (Exception e) {
            throw new MarketDataException("Failed to obtain Groww session token", e);
        }
    }

    private String generateChecksum(String apiSecret, String timestamp) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String input = apiSecret + "|" + timestamp;
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (MarketDataException e) { throw e; } catch (Exception e) {
            throw new MarketDataException("Checksum generation failed", e);
        }
    }
}
