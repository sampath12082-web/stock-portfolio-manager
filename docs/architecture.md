# Architecture Overview

## System Architecture

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│   Frontend   │────▶│   Backend    │────▶│  PostgreSQL   │
│  React 19    │     │ Spring Boot  │     │   Database    │
│  Port 3000   │     │  Port 8081   │     │   Port 5432   │
└──────────────┘     └──────┬───────┘     └──────────────┘
                            │
                    ┌───────┼───────┐
                    ▼       ▼       ▼
              ┌─────────┐ ┌────┐ ┌──────┐
              │  Groww   │ │Yahoo│ │ AMFI │
              │Trade API │ │Fin. │ │  NAV │
              └─────────┘ └────┘ └──────┘
```

## Project Structure

```
stock-portfolio-manager/
├── backend/              # Spring Boot 3.5 / Java 21
│   ├── pom.xml
│   ├── mvnw, mvnw.cmd
│   ├── src/main/java/com/stocks/myportfolio/
│   │   ├── controller/   # REST API endpoints
│   │   ├── service/      # Business logic (interfaces + impl/)
│   │   ├── repository/   # JPA repositories
│   │   ├── entity/       # JPA entities
│   │   ├── dto/          # Request/response records
│   │   ├── integration/  # External APIs (Yahoo, Groww)
│   │   ├── common/       # Enums, exceptions, utils
│   │   ├── config/       # Spring config, CORS, SPA
│   │   ├── mapper/       # Entity ↔ DTO mappers
│   │   ├── scheduler/    # Cron jobs
│   │   └── validation/   # Input validators
│   └── src/main/resources/
│       ├── application.yml
│       └── db/migration/ # Flyway V1–V18
├── frontend/             # React 19 / Vite / Tailwind CSS
│   ├── src/
│   │   ├── pages/        # 11 page components
│   │   ├── components/   # UI components (layout, ui)
│   │   ├── hooks/        # TanStack Query hooks
│   │   ├── api/          # Axios client, types, endpoints
│   │   └── utils/        # Formatters
│   └── vite.config.ts
├── e2e/                  # Playwright tests
│   └── tests/            # smoke, functional, regression
├── scripts/              # Data import scripts (Python)
├── docs/                 # Documentation + tracking (HANDOFF, BUGS, ENHANCEMENTS)
└── CLAUDE.md
```

## Data Model

### Entity Relationship Diagram

```
┌──────────────────┐
│      users       │
│──────────────────│
│ id (PK)          │
│ email (UNIQUE)   │
│ password_hash    │
│ first_name       │
│ last_name        │
│ phone            │
│ role             │
│ status           │
│ email_verified   │
└──────┬───────────┘
       │ user_id (FK on all domain tables)
       │
       ├──────────────────────────────────────────────────────┐
       │                                                      │
       ▼                                                      ▼
┌──────────────────┐    ┌──────────────────┐    ┌──────────────────┐
│      stock       │    │  mutual_fund     │    │ portfolio_snapshot│
│──────────────────│    │──────────────────│    │──────────────────│
│ id (PK)          │    │ id (PK)          │    │ id (PK)          │
│ symbol (UNIQUE)  │    │ scheme_code      │    │ snapshot_date    │
│ company_name     │    │ scheme_name      │    │ total_investment │
│ exchange         │    │ fund_house       │    │ current_value    │
│ sector           │    │ category         │    │ total_pnl        │
│ industry         │    │ current_nav      │    │ holding_count    │
│ user_id (FK)     │    │ nav_date         │    │ top_gainer       │
└──────┬───────────┘    │ user_id (FK)     │    │ top_loser        │
       │                └──────┬───────────┘    │ user_id (FK)     │
       │                       │                └──────────────────┘
       │                       │
       ├───────────┐           ├───────────┐
       │           │           │           │
       ▼           ▼           ▼           ▼
┌────────────┐ ┌────────────┐ ┌────────────┐ ┌────────────────┐
│  holding   │ │transaction │ │ mf_holding │ │ mf_transaction │
│────────────│ │_history    │ │────────────│ │────────────────│
│ id (PK)    │ │────────────│ │ id (PK)    │ │ id (PK)        │
│ stock_id   │→│ id (PK)    │ │ fund_id    │→│ fund_id (FK)   │
│ quantity   │ │ stock_id   │ │ units      │ │ units          │
│ avg_price  │ │ quantity   │ │ avg_nav    │ │ nav            │
│ invested   │ │ price      │ │ invested   │ │ amount         │
│ user_id    │ │ total_amt  │ │ user_id    │ │ txn_type       │
└────────────┘ │ txn_type   │ └────────────┘ │ trade_date     │
               │ trade_type │                │ folio_number   │
               │ trade_date │                │ user_id (FK)   │
               │ description│                └────────────────┘
               │ user_id    │
               └────────────┘

┌──────────────────┐    ┌──────────────────┐
│  stock_quote     │    │  trading_signal  │
│──────────────────│    │──────────────────│
│ id (PK)          │    │ id (PK)          │
│ stock_id (FK)    │→   │ stock_id (FK)    │→ stock
│ ltp              │    │ symbol           │
│ open/high/low    │    │ signal_type      │
│ close            │    │ target_price     │
│ previous_close   │    │ current_price    │
│ volume           │    │ status           │
│ fetched_at       │    │ signal_date      │
└──────────────────┘    │ user_id (FK)     │
                        └──────────────────┘

┌──────────────────┐
│   otp_tokens     │
│──────────────────│
│ id (PK)          │
│ email            │
│ otp_code         │
│ purpose          │
│ expires_at       │
│ used             │
└──────────────────┘
```

### Entity Summary

| Entity | Table | Relationships | Key Fields |
|--------|-------|---------------|------------|
| User | `users` | owns all domain data via `user_id` FK | email (unique), password_hash, role, status |
| Stock | `stock` | has many Holdings, Transactions, Quotes, Signals | symbol (unique), exchange, sector |
| Holding | `holding` | belongs to Stock, User | quantity, avg_buy_price, invested_amount |
| Transaction | `transaction_history` | belongs to Stock (nullable), User | type, trade_type (CNC/MIS), trade_date |
| StockQuote | `stock_quote` | belongs to Stock | ltp, OHLC, volume, 5-min cache |
| TradingSignal | `trading_signal` | belongs to Stock, User | signal_type, target_price, status |
| PortfolioSnapshot | `portfolio_snapshot` | belongs to User | investment, current_value, daily capture |
| MutualFund | `mutual_fund` | has many MfHoldings, MfTransactions | scheme_code, current_nav |
| MfHolding | `mf_holding` | belongs to MutualFund, User | units, avg_nav, invested |
| MfTransaction | `mf_transaction` | belongs to MutualFund, User | type, nav, amount, folio |
| OtpToken | `otp_tokens` | standalone (linked by email) | otp_code, purpose, expires_at |

### Flyway Migrations (V1–V18)

| Version | Table(s) | Purpose |
|---------|----------|---------|
| V1 | `stock` | Stock master data |
| V2 | `holding` | Stock holdings |
| V3 | `transaction_history` | No-op (V6 has actual DDL) |
| V4 | `stock_quote` | Cached quotes |
| V5 | `portfolio_snapshot` | Daily snapshots |
| V6 | `transaction_history` | Actual transaction DDL |
| V7 | `trading_signal` | Technical analysis signals |
| V8 | `trading_signal` | Add `source` column |
| V9 | `transaction_history` | Make `stock_id` nullable, add `description` |
| V10 | `transaction_history` | Add `trade_date` column |
| V11 | `mutual_fund` | Mutual fund master |
| V12 | `mf_holding` | MF holdings |
| V13 | `mf_transaction` | MF transactions |
| V14 | `transaction_history` | Add `trade_type` (CNC/MIS/UNKNOWN) |
| V15 | `users` | User authentication |
| V16 | `otp_tokens` | Email OTP verification |
| V17 | all domain tables | Add `user_id` FK for multi-tenancy |
| V18 | all domain tables | Assign existing data to admin user |

## Backend Layered Architecture

```
Controller → Service (interface) → ServiceImpl → Repository → PostgreSQL
```

- **DTOs** are Java records for immutable request/response objects
- **Entities** extend `BaseEntity` (JPA auditing: `createdAt`, `updatedAt`)
- **All entities use manual getters/setters** — Lombok is on classpath but not used
- **Constructor injection** everywhere — no `@Autowired`
- **Flyway** manages schema (V1–V18). Hibernate `ddl-auto=validate`

## Security Architecture

```
Client → JWT Filter → SecurityContext → Controller
         ↓ (no token)
         401 Unauthorized

/api/auth/*    → permitAll (public)
/api/admin/*   → ROLE_ADMIN only
/api/*         → authenticated (any role)
/*             → permitAll (static files, SPA)
```

- **JWT tokens** via jjwt 0.12.6 (access: 15min, refresh: 7 days)
- **BCrypt** password hashing
- **Email OTP** for registration verification and password reset
- **Admin user** seeded on startup via `AdminUserSeeder`
- **Multi-tenant**: `user_id` FK on all domain tables

## External Integrations

| Provider | Purpose | Auth |
|----------|---------|------|
| Yahoo Finance | Live quotes, 3-month OHLC, stock search | None (public API) |
| Groww Trade API | Holdings sync, order sync, account details | SHA-256 checksum → session token |
| AMFI | Mutual fund NAV feed | None (public CSV) |

## Frontend Architecture

- **State management**: TanStack Query (server state caching, mutations, invalidation)
- **Routing**: React Router v6 with SPA forwarding on backend
- **Charts**: Recharts (pie, area, bar, composed)
- **Build output**: `backend/src/main/resources/static/` — WAR serves both API and UI
- **Key invalidation chain**: transaction create → invalidates transactions + holdings + portfolio + dashboard
