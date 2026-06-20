# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Backend — build WAR
./mvnw package
./mvnw package -DskipTests

# Backend — run locally (requires PostgreSQL on localhost:5432)
./mvnw spring-boot:run

# Backend — run tests
./mvnw test

# Frontend — dev server (port 3000, proxies /api to backend on 8081)
cd frontend && npm run dev

# Frontend — production build (outputs to src/main/resources/static/)
cd frontend && npm run build

# Windows build + deploy script
build-deploy.cmd
powershell scripts/build-deploy.ps1 -SkipTests -StopExisting

# Database setup script (drops and recreates myportfolio DB)
powershell scripts/setup-database.ps1
```

The backend runs on **port 8081**. The frontend dev server runs on **port 3000** with a proxy to the backend.

### Environment Variables

| Variable | Required | Default | Purpose |
|----------|----------|---------|---------|
| `DB_PASSWORD` | Yes | — | PostgreSQL password |
| `DB_USER` | No | `sampat` | PostgreSQL username |
| `DB_HOST` | No | `localhost` | PostgreSQL host |
| `GROWW_API_ENABLED` | No | `false` | Enable Groww portfolio sync |
| `GROWW_ACCESS_TOKEN` | If Groww enabled | — | Groww API key (JWT) |
| `GROWW_API_SECRET` | If Groww enabled | — | Groww API secret for checksum auth |

## Architecture

Spring Boot 3.5 / Java 21 WAR backend + React 19 / Vite / Tailwind CSS frontend for Indian stock portfolio tracking (NSE/BSE). Base package: `com.stocks.myportfolio`.

### Backend Layered Architecture

**Controller → Service (interface) → ServiceImpl → Repository → PostgreSQL**

- **DTOs** use Java records for requests and responses
- **Entities** extend `BaseEntity` (provides `createdAt`/`updatedAt` via JPA auditing)
- **All entities use manual getters/setters** — Lombok is on classpath but not used
- **Database migrations** managed by Flyway (`src/main/resources/db/migration/`, V1–V13). Hibernate `ddl-auto=validate` — schema changes must go through Flyway.
- **Constructor injection** throughout (no `@Autowired`)
- **Mappers** are `@Component` classes (`StockMapper`, `HoldingMapper`)

### Key Domains

| Domain | Entity | Route | Key Features |
|--------|--------|-------|--------------|
| Stocks | `Stock` | `/api/stocks` | CRUD, delete, search, smart lookup via Yahoo Finance |
| Holdings | `Holding` | `/api/holdings` | CRUD, enriched with live P&L, Groww portfolio sync |
| Transactions | `Transaction` | `/api/transactions` | Buy/sell/deposit/withdrawal/dividend/charges, auto-holding updates, filtering, analytics, PDF upload, Groww order sync |
| Market Data | `StockQuote` | `/api/quotes` | Yahoo Finance quotes with 5-min cache |
| Portfolio | — | `/api/portfolio` | Summary, sector allocation, per-stock P&L |
| Dashboard | — | `/api/dashboard` | Aggregated P&L with live market data |
| Performance | `PortfolioSnapshot` | `/api/performance` | Daily snapshots, historical tracking, manual capture |
| Signals | `TradingSignal` | `/api/signals` | Auto-generated BUY/SELL/HOLD signals via technical analysis |
| Groww Sync | — | `/api/groww` | Portfolio sync, order sync, account details (balance, positions, orders) |
| Mutual Funds | `MutualFund` | `/api/mf/funds` | Fund catalog, AMFI NAV search, NAV refresh |
| MF Holdings | `MfHolding` | `/api/mf/holdings` | MF positions with P&L via current NAV |
| MF Transactions | `MfTransaction` | `/api/mf/transactions` | Purchase/redemption/SIP/SWP history |

### Market Data Provider Pattern

```
MarketDataProvider (interface)
├── YahooFinanceProvider  (@Primary, always loaded — used for quotes)
└── NoOpMarketDataProvider (fallback, not a Spring bean)

YahooFinanceProvider also provides fetchHistoricalData() for 3-month OHLC data.
MarketDataService → caches quotes in stock_quote table, checks 5-min staleness.
```

Yahoo Finance is the default provider. Symbol mapping: NSE → `SYMBOL.NS`, BSE → `SYMBOL.BO`.

### Groww API Integration (Optional)

Enabled via `GROWW_API_ENABLED=true`. Uses two-step auth:
1. SHA-256 checksum of `apiSecret + timestamp`
2. `POST /v1/token/api/access` with checksum → session token (24h validity)
3. Session token used for `GET /v1/holdings/user` (response wrapped in `payload`)

`GrowwClient`, `GrowwSyncService`, and `GrowwSyncController` all use `@ConditionalOnProperty(groww.api.enabled=true)`. `GrowwStatusController` is always loaded (returns enabled/disabled status).

Groww endpoints: `POST /api/groww/sync` (holdings), `POST /api/groww/sync-orders` (today's executed orders → transactions), `GET /api/groww/account` (balance, positions, all orders). Note: Groww API only returns today's orders — historical orders are not available via API.

**Transaction types**: `BUY`, `SELL`, `BONUS`, `SPLIT`, `DIVIDEND`, `DEPOSIT`, `WITHDRAWAL`, `CHARGES`. Fund transactions (DEPOSIT/WITHDRAWAL/CHARGES) have `stock_id=null` and use the `description` field. V9 migration made `stock_id` nullable.

### Technical Analysis Engine

`TechnicalAnalysisServiceImpl` computes signals from Yahoo Finance historical data:
- **SMA crossover** (20-day vs 50-day): golden cross = BUY, death cross = SELL
- **RSI (14-day)**: <30 = oversold/BUY, >70 = overbought/SELL
- **52-week position**: near low = BUY opportunity, near high = caution
- **Volume trend**: 5-day vs 20-day average confirms signals

Signals stored in `TradingSignal` table with `source=AUTO`. Watchlist of 30 NIFTY stocks scanned for BUY candidates.

### Stock Lookup Service

`StockLookupServiceImpl` searches database first, then Yahoo Finance (`/v1/finance/search`) for NSE/BSE Indian equities. Returns `existsInDb` flag. Used in Add Stock modal smart search and stocks page search.

### PDF Upload

`TransactionUploadServiceImpl` uses Apache PDFBox 3.0.5. Supports:
- **Trade reports**: Parses `DATE SYMBOL EXCHANGE BUY/SELL QTY PRICE` lines
- **Groww Ledger**: Identifies as funds statement, reports settlement count (can't extract individual trades)

Auto-creates stocks via `StockLookupService` when importing.

### Scheduled Jobs (`PortfolioScheduler`)

| Time (MON-FRI) | Job | Purpose |
|----------------|-----|---------|
| 9:00 AM | `morningQuoteRefresh()` | Pre-warm quote cache |
| 3:30 PM | `dailySnapshot()` | Capture portfolio snapshot after NSE close |
| 4:00 PM | `dailyTechnicalAnalysis()` | Run SMA/RSI analysis on holdings + market scan |
| 6:00 PM | `expireOldSignals()` | Expire ACTIVE signals >7 days old |
| 9:00 PM | `refreshNavs()` (MfNavService) | Refresh mutual fund NAVs from AMFI |

### Frontend Architecture

React 19 + Vite + Tailwind CSS (light theme) in `frontend/`.

- **Build output**: `../src/main/resources/static/` (WAR serves both API and UI)
- **Routing**: React Router v6 with SPA forwarding via `SpaForwardingConfig`
- **State**: TanStack Query for server state (caching, mutations, invalidation)
- **Charts**: Recharts (pie charts, area charts, bar charts)
- **API layer**: `src/api/` — types.ts (all DTOs), client.ts (axios), endpoints.ts (24 typed functions)
- **Hooks**: `src/hooks/` — one file per domain with useQuery/useMutation wrappers
- **Pages**: Dashboard, Holdings (with Groww sync + signal badges), Transactions (with PDF upload), Stocks (compact table + smart search), Mutual Funds (AMFI search + holdings + transactions), Performance

Key invalidation chains: transaction create → invalidates transactions + holdings + portfolio + dashboard.

### Exception Handling

`GlobalExceptionHandler` (`@RestControllerAdvice`) maps:
- `ResourceNotFoundException` → 404
- `DuplicateResourceException` → 409
- `ValidationException` → 400
- `MarketDataException` → 503

## Configuration

- Database: `application.yml` under `spring.datasource` (env vars for credentials)
- Groww API: `application.yml` under `groww.api.*` (env vars for secrets)
- Multipart upload: max 10MB (`spring.servlet.multipart`)
- CORS: All origins allowed (`WebConfig`)
- Security: CSRF disabled, all requests permitted (no auth yet)
- OpenAPI: Swagger UI at `/swagger-ui.html`
- WAR packaging: supports both `spring-boot:run` and external Tomcat via `ServletInitializer`

## Database

Before first run:
```sql
DROP DATABASE IF EXISTS myportfolio;
CREATE DATABASE myportfolio;
```

Note: V3 migration is a no-op (`SELECT 1;`) — V6 contains the actual `transaction_history` DDL. V9 makes `stock_id` nullable and adds `description` column for fund transactions. V10 adds `trade_date` column for actual trade execution timestamps. V11-V13 add mutual fund tables (`mutual_fund`, `mf_holding`, `mf_transaction`).

## Session Continuity

- **`HANDOFF.md`** — Read this first when resuming work. Contains current project state, database contents, in-progress work (if any), data sources loaded, and exact next steps. Updated before every session ends.
- **`ENHANCEMENTS.md`** — Numbered enhancement/feature requests with status tracking. New enhancements must be listed here first before implementation. Similar to BUGS.md but for feature work.
- **`BUGS.md`** — Tracked bugs with status (open/resolved) and root cause analysis.
- **`GROWW_Reports_06192026/`** — Groww export files (order history, dividends, balance statement, capital gains, F&O, commodities). Order history and balance statement have been imported; others are reference-only.
- **`scripts/import_stock_data.py`** — Imports stock orders, dividends, deposits, charges from Groww Excel reports via REST API. Requires `openpyxl`, `requests`.
- **`scripts/import_mf_data.py`** — Imports MF holdings + capital gains trades from Groww Excel, refreshes NAVs via AMFI. Requires `openpyxl`, `requests`.
- **`.claude/plans/`** — Implementation plans from prior sessions.

## Known Data Quality Issues

The transaction data has fundamental issues that affect P&L calculations:

1. **No intraday (MIS) vs delivery (CNC) distinction** — 96% of imported stock orders are intraday (same-day round-trips). The system treats them identically to delivery, inflating buy/sell volumes ~25x and corrupting realized P&L.
2. **Pre-period sells with zero cost basis** — 45 stocks have sells imported from April-June 2026 but their buys happened before April (not in import data). FIFO assigns zero cost basis → phantom profit of Rs 59L.
3. **FIFO sorts by `createdAt` not `tradeDate`** — Both `DashboardServiceImpl` and `TransactionServiceImpl.computeSellCostBasis()` sort by `createdAt`. Since all 546 orders were batch-imported, they share the same `createdAt`, destroying chronological order.
4. **`processSell` clamps negative to zero** — `TransactionServiceImpl.processSell()` silently allows selling more shares than held, setting holdings to 0 instead of rejecting. This enables phantom profit from pre-period sells.

These issues are tracked in BUGS.md (#13-#19). Fix requires: adding `tradeType` field (V14 migration), separating intraday/delivery P&L, fixing FIFO sort order, and importing pre-period buy prices from capital gains reports.
