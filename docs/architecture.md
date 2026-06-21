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
│       └── db/migration/ # Flyway V1–V14
├── frontend/             # React 19 / Vite / Tailwind CSS
│   ├── src/
│   │   ├── pages/        # 6 page components
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
