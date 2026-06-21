---
name: groww-api
description: Work with Groww Trade API integration. Use when asked about Groww sync, Groww API issues, broker integration, portfolio sync, order sync, or Groww account details.
---

# Groww Trade API Integration

Drives the Groww brokerage API for portfolio sync, order sync, and account details. Conditional module — only loads when `GROWW_API_ENABLED=true`.

## Architecture

```
GrowwClient (@ConditionalOnProperty)
  ├── Two-step auth: SHA-256 checksum → session token
  ├── getHoldings()      → /v1/holdings/user
  ├── getOrders()        → /v1/order/list
  ├── getMarginDetails() → /v1/user/margin
  ├── getUserProfile()   → /v1/user/profile
  ├── getPositions()     → /v1/user/positions
  └── getAllOrders()     → /v1/order/list (all statuses)

GrowwSyncServiceImpl
  ├── syncPortfolio()  → Upserts holdings, zeros out sold stocks
  ├── syncOrders()     → Creates transactions from today's orders
  └── getAccountDetails() → Combines profile + margin + positions + orders
```

## Authentication Flow

```
1. API key (access-token) from Groww dashboard → env var GROWW_ACCESS_TOKEN
2. API secret → env var GROWW_API_SECRET
3. On first API call:
   a. Generate timestamp
   b. SHA-256 hash of: apiSecret + timestamp
   c. POST /v1/token/api/access
      Body: { "apiKey": accessToken, "checksum": sha256hex, "timestamp": ts }
   d. Response: { "sessionToken": "..." }
4. Session token cached in-memory (24h validity)
5. All subsequent calls: Header "x-session-token: <sessionToken>"
```

**CRITICAL: API key must be renewed daily** at https://groww.in/trade-api/api-keys

## API Endpoints (App)

```bash
TOKEN=$(curl -s -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"sampath12082@gmail.com","password":"Admin@123"}' \
  | python -c "import json,sys; print(json.load(sys.stdin)['accessToken'])")

# Sync holdings from Groww (corrects quantities, zeroes sold stocks)
curl -s -X POST http://localhost:8081/api/groww/sync \
  -H "Authorization: Bearer $TOKEN" | python -m json.tool

# Sync today's orders as transactions
curl -s -X POST http://localhost:8081/api/groww/sync-orders \
  -H "Authorization: Bearer $TOKEN" | python -m json.tool

# Get full account details (balance, positions, orders)
curl -s http://localhost:8081/api/groww/account \
  -H "Authorization: Bearer $TOKEN" | python -c "
import json,sys
g = json.load(sys.stdin)
print(f'Clear Cash:  {g[\"clearCash\"]:,.2f}')
print(f'Available:   {g[\"availableCash\"]:,.2f}')
print(f'Margin Used: {g[\"marginUsed\"]:,.2f}')
print(f'UCC:         {g[\"ucc\"]}')
print(f'Positions:   {len(g[\"todayPositions\"])}')
print(f'Orders:      {len(g[\"todayOrders\"])}')
"

# Check if Groww API is enabled (always works, even without key)
curl -s http://localhost:8081/api/groww/status \
  -H "Authorization: Bearer $TOKEN" | python -m json.tool
```

## Key Files

| File | Purpose |
|------|---------|
| `integration/groww/GrowwClient.java` | HTTP client — auth, all API calls |
| `integration/groww/GrowwProperties.java` | `@ConfigurationProperties(prefix = "groww.api")` |
| `integration/groww/GrowwHoldingData.java` | Holdings response DTO |
| `integration/groww/GrowwOrderData.java` | Orders response DTO |
| `integration/groww/GrowwPortfolioResponse.java` | Portfolio wrapper |
| `service/impl/GrowwSyncServiceImpl.java` | Sync logic — holdings, orders, account |
| `controller/GrowwSyncController.java` | `/api/groww/*` (conditional) |
| `controller/GrowwStatusController.java` | `/api/groww/status` (always loaded) |

## Groww API Gotchas

### 1. Holdings response wrapped in `payload`
```json
// Groww returns:
{ "payload": { "holdings": [...] } }
// NOT:
{ "holdings": [...] }
```
`GrowwClient.getHoldings()` manually parses `payload.holdings`.

### 2. Only today's orders available
```
GET /v1/order/list → returns only today's executed/cancelled orders
No historical order API exists. Use Excel reports for past orders.
```

### 3. Conditional loading
All Groww beans use `@ConditionalOnProperty(name = "groww.api.enabled", havingValue = "true")`.
`GrowwStatusController` is NOT conditional — always returns enabled/disabled status.

### 4. Session token caching
Token cached in `GrowwClient` field. Regenerated on 401/403. Not persisted — restarts require re-auth.

### 5. API key daily renewal
Key expires at midnight IST. Must manually re-approve at Groww dashboard.
Without valid key: all Groww endpoints return 503 `MarketDataException`.

### 6. Response field mapping
```
Groww field          → App field
tradingSymbol        → symbol (uppercase)
averagePrice         → averageBuyPrice
quantity             → quantity (BigDecimal → int)
tradableExchanges    → exchange (NSE preferred, BSE fallback)
```

### 7. Phantom holdings
After sync, stocks not in Groww response get zeroed out:
```java
// GrowwSyncServiceImpl.syncPortfolio()
for (Holding h : allHoldings) {
    if (h.getQuantity() > 0 && !growwStockIds.contains(h.getStock().getId())) {
        h.setQuantity(0);  // Sold stock — zero out
    }
}
```

### 8. Order sync creates transactions
`syncOrders()` creates BUY/SELL transactions from today's executed orders.
Does NOT set `tradeType` (CNC/MIS) — all synced orders get `tradeType=null`.
Import script handles MIS/CNC detection separately.

## Dashboard Dependency on Groww

The dashboard computes P&L from Groww `clearCash`:
```
Realized P&L = (Invested + clearCash) - Deposited
Cash Balance = clearCash
```

When Groww is offline:
- Cash Balance shows "Groww offline"
- Realized P&L shows "Groww offline"
- Total P&L shows "Groww offline"
- Unrealized P&L still works (from Yahoo Finance quotes)

## Environment Variables

| Variable | Required | Purpose |
|----------|----------|---------|
| `GROWW_API_ENABLED` | Yes | `true` to load Groww beans |
| `GROWW_ACCESS_TOKEN` | If enabled | API key from Groww dashboard |
| `GROWW_API_SECRET` | If enabled | API secret for checksum auth |

## Troubleshooting

| Issue | Fix |
|-------|-----|
| 503 on all Groww endpoints | API key expired. Renew at groww.in/trade-api |
| "Failed to obtain session token" | Wrong API key or secret. Check env vars. |
| Holdings sync shows 0 created, 0 updated | All holdings already synced. Check response for errors. |
| Positions/orders empty | No trades today. Groww only returns same-day data. |
| clearCash is null on dashboard | Groww API offline. Dashboard shows "Groww offline". |
| syncOrders duplicates transactions | Groww returns same orders on repeated calls. Need dedup logic. |
| Stock not found during sync | Groww symbol doesn't match DB. Check symbol mapping in `findOrCreateStock()`. |
