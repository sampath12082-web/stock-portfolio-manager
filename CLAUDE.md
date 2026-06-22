# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Backend — build WAR
cd backend && ./mvnw package
cd backend && ./mvnw package -DskipTests

# Backend — run locally (requires PostgreSQL on localhost:5432)
cd backend && ./mvnw spring-boot:run

# Backend — run tests
cd backend && ./mvnw test

# Frontend — dev server (port 3000, proxies /api to backend on 8081)
cd frontend && npm run dev

# Frontend — production build (outputs to backend/src/main/resources/static/)
cd frontend && npm run build

# E2E tests (requires backend running on port 8081)
cd e2e && npm test                    # All tests
cd e2e && npm run test:smoke          # Smoke tests only
cd e2e && npm run test:functional     # Functional tests
cd e2e && npm run test:regression     # Regression tests
cd e2e && npm run test:headed         # Run with browser visible

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

## Project Structure

```
stock-portfolio-manager/
├── backend/              # Spring Boot 3.5 / Java 21
│   ├── pom.xml
│   ├── mvnw, mvnw.cmd
│   └── src/main/java/com/stocks/myportfolio/
├── frontend/             # React 19 / Vite / Tailwind CSS
│   └── src/
├── e2e/                  # Playwright tests (smoke, functional, regression)
│   └── tests/
├── scripts/              # Python data import scripts
├── docs/                 # Architecture, API reference, features
└── CLAUDE.md
```

## Architecture

Spring Boot 3.5 / Java 21 WAR backend + React 19 / Vite / Tailwind CSS frontend for Indian stock portfolio tracking (NSE/BSE). Base package: `com.stocks.myportfolio`.

### Backend Layered Architecture

**Controller → Service (interface) → ServiceImpl → Repository → PostgreSQL**

- **DTOs** use Java records for requests and responses
- **Entities** extend `BaseEntity` (provides `createdAt`/`updatedAt` via JPA auditing)
- **All entities use manual getters/setters** — Lombok is on classpath but not used
- **Database migrations** managed by Flyway (`backend/src/main/resources/db/migration/`, V1–V22). Hibernate `ddl-auto=validate` — schema changes must go through Flyway. V15-V18 add users, OTP, user_id FK, admin seed. V19-V20 add FAQ and support tickets. V21 adds security questions. V22 adds per-user Groww config.
- **Constructor injection** throughout (no `@Autowired`)
- **Mappers** are `@Component` classes (`StockMapper`, `HoldingMapper`)

### Key Domains

| Domain | Entity | Route | Key Features |
|--------|--------|-------|--------------|
| Stocks | `Stock` | `/api/stocks` | CRUD, delete, search, smart lookup via Yahoo Finance |
| Holdings | `Holding` | `/api/holdings` | CRUD, enriched with live P&L, Groww portfolio sync |
| Transactions | `Transaction` | `/api/transactions` | Buy/sell/deposit/withdrawal/dividend/charges, CNC/MIS trade types, analytics |
| Market Data | `StockQuote` | `/api/quotes` | Yahoo Finance quotes with 5-min cache |
| Portfolio | — | `/api/portfolio` | Summary, sector allocation, per-stock P&L |
| Dashboard | — | `/api/dashboard` | Portfolio metrics (invested, current, P&L, deposits) |
| Performance | `PortfolioSnapshot` | `/api/performance` | Daily snapshots, historical tracking |
| Signals | `TradingSignal` | `/api/signals` | SMA/RSI/52-week auto-generated signals |
| Groww Sync | — | `/api/groww` | Portfolio sync, order sync, account details |
| Mutual Funds | `MutualFund` | `/api/mf/*` | AMFI NAV, holdings, transactions |

### P&L Formulas

- **Realized P&L** = (Total Invested + Clear Cash) - Total Deposited. Uses Groww `clearCash` as source of truth.
- **Unrealized P&L** = Current Value - Invested Amount
- **Cash Balance** = Groww Clear Cash
- Dashboard backend returns basic facts; frontend computes realized P&L from Groww account data.

### External Integrations

- **Yahoo Finance**: Quotes, historical OHLC, stock search. Symbol mapping: NSE → `SYMBOL.NS`, BSE → `SYMBOL.BO`.
- **Groww Trade API**: Conditional (`@ConditionalOnProperty`). Two-step auth: SHA-256 checksum → session token. Daily key renewal required.
- **AMFI NAV feed**: Public CSV at `amfiindia.com`. Parsed by `MfNavService`, refreshed nightly at 9 PM.

### Frontend Architecture

React 19 + Vite + Tailwind CSS (light theme) in `frontend/`.

- **Build output**: `backend/src/main/resources/static/` (WAR serves both API and UI)
- **State**: TanStack Query for server state (caching, mutations, invalidation)
- **Charts**: Recharts. **Routing**: React Router v6 with SPA forwarding.
- **UI features**: Sortable columns (SortHeader component), sticky table headers, signal filter chips, color-coded P&L rows

## Documentation

- **`docs/architecture.md`** — System architecture and project structure
- **`docs/api-reference.md`** — Complete API endpoint reference
- **`docs/features.md`** — Feature documentation for all 6 pages

## Session Continuity

- **`docs/HANDOFF.md`** — Read first when resuming. Current state, pending work, data sources.
- **`docs/ENHANCEMENTS.md`** — Feature requests with status tracking.
- **`docs/BUGS.md`** — Bug tracking with root cause analysis.
- **`GROWW_Reports_06192026/`** — Groww export files (gitignored, personal data).
