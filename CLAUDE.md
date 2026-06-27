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
cd backend && ./mvnw test -Dtest=MyportfolioApplicationTests  # single test class

# Frontend — dev server (port 3000, proxies /api to backend on 8081)
cd frontend && npm run dev

# Frontend — production build (outputs to backend/src/main/resources/static/)
cd frontend && npm run build

# Frontend — lint
cd frontend && npm run lint

# E2E tests — 216 E2E + 55 backend = 271 tests across 6 suites (requires backend running on port 8081)
cd e2e && npm test                    # All tests
cd e2e && npm run test:smoke          # Smoke tests (10)
cd e2e && npm run test:auth           # Auth tests (33)
cd e2e && npm run test:functional     # Functional tests (78)
cd e2e && npm run test:regression     # Regression tests (22)
cd e2e && npm run test:ui             # UI rendering tests (69)
cd e2e && npm run test:headed         # Run with browser visible
cd e2e && npx playwright test tests/smoke.spec.ts --grep "test name"  # single test

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
| `GROWW_API_ENABLED | No | true` | Enable Groww portfolio sync |
| `GROWW_ACCESS_TOKEN` | If Groww enabled | — | Groww API key (JWT) |
| `GROWW_API_SECRET` | If Groww enabled | — | Groww API secret for checksum auth |
| `ANTHROPIC_API_KEY` | No | — | Claude API key for AI Stock Assistant (optional) |
| `SPRING_MAIL_USERNAME` | For OTP email | — | Gmail SMTP username |
| `SPRING_MAIL_PASSWORD` | For OTP email | — | Gmail SMTP app password |

## Architecture

Spring Boot 3.5 / Java 21 WAR backend + React 19 / Vite / Tailwind CSS frontend for Indian stock portfolio tracking (NSE/BSE). Base package: `com.stocks.myportfolio`.

### Backend Layered Architecture

**Controller → Service (interface) → ServiceImpl → Repository → PostgreSQL**

- **DTOs** use Java records for requests and responses
- **Entities** extend `BaseEntity` (provides `createdAt`/`updatedAt` via JPA auditing)
- **All entities use manual getters/setters** — Lombok is on classpath but not used
- **Database migrations** managed by Flyway (`backend/src/main/resources/db/migration/`, V1–V24). Hibernate `ddl-auto=validate` — schema changes must go through Flyway
- **Constructor injection** throughout (no `@Autowired`)
- **Mappers** are `@Component` classes (`StockMapper`, `HoldingMapper`)

### Security & Auth

- **JWT stateless auth**: `JwtAuthenticationFilter` → `SecurityConfig` filter chain. Access token 15 min, refresh token 7 days.
- **Route protection**: `/api/auth/**` public, `/api/admin/**` requires `ROLE_ADMIN`, all other `/api/**` requires authentication. Non-API paths serve the SPA (`SpaForwardingConfig`).
- **Password handling**: RSA 2048-bit encryption in transit (frontend encrypts via Web Crypto, backend decrypts). BCrypt for storage.
- **Multi-user**: All domain entities have `user_id` FK (V17). `CurrentUserProvider`/`SecurityUtils` resolves the authenticated user. Admin seeded on startup via `AdminUserSeeder`.
- **Registration flow**: Register → mandatory email OTP verification → login. Password reset: security questions → OTP → reset.

### Key Domains

| Domain | Entity | Route | Key Features |
|--------|--------|-------|--------------|
| Stocks | `Stock` | `/api/stocks` | CRUD, search, smart lookup via Yahoo Finance |
| Holdings | `Holding` | `/api/holdings` | CRUD, enriched with live P&L, Groww portfolio sync |
| Transactions | `Transaction` | `/api/transactions` | Buy/sell/deposit/withdrawal/dividend/charges, CNC/MIS trade types, analytics |
| Market Data | `StockQuote` | `/api/quotes` | Yahoo Finance quotes with 5-min cache |
| Portfolio | — | `/api/portfolio` | Summary, sector allocation, per-stock P&L |
| Dashboard | — | `/api/dashboard` | Portfolio metrics (invested, current, P&L, deposits) |
| Performance | `PortfolioSnapshot` | `/api/performance` | Daily snapshots, historical tracking |
| Signals | `TradingSignal` | `/api/signals` | SMA/RSI/52-week auto-generated signals |
| Groww Sync | — | `/api/groww` | Portfolio sync, order sync, account details |
| Mutual Funds | `MutualFund` | `/api/mf/*` | AMFI NAV, holdings, transactions |
| AI Search | — | `/api/ai` | Dynamic chat, stock analysis, portfolio insights |
| Help/FAQ | `Faq`, `SupportTicket` | `/api/help/*` | FAQ CRUD, support tickets with AI classification |

### Flyway Migration Groups

- **V1–V14**: Core domain (stocks, holdings, transactions, quotes, snapshots, signals, mutual funds, trade_type)
- **V15–V18**: Users, OTP, user_id FK on all domain tables, admin seed
- **V19–V20**: FAQ (seeded with 14 entries), support tickets
- **V21**: Security questions for password reset
- **V22**: Per-user Groww API config
- **V23–V24**: AI support agent (bug_report, ticket_activity, ticket classification fields)

### P&L Formulas

- **Realized P&L** = (Total Invested + Clear Cash) - Total Deposited. Uses Groww `clearCash` as source of truth.
- **Unrealized P&L** = Current Value - Invested Amount
- Dashboard backend returns basic facts; frontend computes realized P&L from Groww account data.

### External Integrations

- **Yahoo Finance**: Quotes, historical OHLC, stock search. Symbol mapping: NSE → `SYMBOL.NS`, BSE → `SYMBOL.BO`.
- **Groww Trade API**: Conditional (`@ConditionalOnProperty`). Two-step auth: SHA-256 checksum → session token. Daily key renewal required.
- **AMFI NAV feed**: Public CSV at `amfiindia.com`. Parsed by `MfNavService`, refreshed nightly at 9 PM.
- **Claude API**: `AiStockService` for AI stock search, `AiTicketAgentService` for auto-classifying support tickets and generating responses. Falls back gracefully when `ANTHROPIC_API_KEY` is unset.

### Scheduled Jobs (`PortfolioScheduler` + `MfNavService`)

| Cron | Job |
|------|-----|
| 9:00 AM Mon–Fri | Quote refresh for all holdings |
| 3:30 PM Mon–Fri | Daily portfolio snapshot |
| 4:00 PM Mon–Fri | Technical analysis (SMA/RSI signals) |
| 6:00 PM Mon–Fri | Expire old trading signals |
| 9:00 PM Mon–Fri | AMFI mutual fund NAV refresh |

### Frontend Architecture

React 19 + Vite + Tailwind CSS in `frontend/`. **SoloSprint Trade** branding: Plus Jakarta Sans + JetBrains Mono fonts, Sprint Orange (#D85A30) palette, cream background (#FAFAF8).

- **Build output**: `backend/src/main/resources/static/` (WAR serves both API and UI)
- **State**: TanStack Query for server state (caching, mutations, invalidation)
- **API client**: Axios with JWT interceptors (`frontend/src/api/client.ts`). Auto-redirects to `/login` on 401.
- **Path alias**: `@/` maps to `frontend/src/` (configured in `vite.config.ts`)
- **Charts**: Recharts. **Routing**: React Router v7 with SPA forwarding.
- **UI patterns**: Sortable columns (`SortHeader` component), sticky table headers, signal filter chips, color-coded P&L rows, total row footers
- **Auth flow**: `AuthContext` + `AuthGuard` wrapper. Tokens stored in `localStorage`. RSA encryption for password fields via `frontend/src/auth/crypto.ts`.

## Documentation

- **`docs/architecture.md`** — System architecture and project structure
- **`docs/api-reference.md`** — Complete API endpoint reference
- **`docs/features.md`** — Feature documentation for all 15 pages

## Session Continuity

- **`docs/HANDOFF.md`** — Read first when resuming. Current state, pending work, data sources.
- **`docs/ENHANCEMENTS.md`** — Feature requests with status tracking.
- **`docs/BUGS.md`** — Bug tracking with root cause analysis.
- **`GROWW_Reports_06192026/`** — Groww export files (gitignored, personal data).
