# Handoff — Stock Portfolio Manager

## Project Summary

Indian stock portfolio + mutual funds tracking app with JWT authentication. Spring Boot 3.5 / Java 21 backend + React 19 / Vite / Tailwind CSS frontend. Integrates with Groww API, Yahoo Finance, and AMFI NAV feed.

## Current State (as of 2026-06-21)

### What's Running (port 8081)

V1-V18 migrations applied. JWT auth enabled. Admin user seeded. 129 stocks, 567 stock transactions (469 MIS, 77 CNC), 15 MF funds, 12 MF holdings, 279 MF transactions. 6 frontend pages. All data belongs to admin user (user_id=1).

### Authentication

| Item | Detail |
|------|--------|
| Mechanism | JWT (stateless) — access token 15min, refresh token 7 days |
| Admin user | `sampath12082@gmail.com` / `Admin@123` (ROLE_ADMIN) |
| Password | BCrypt hashed |
| Protected | All `/api/*` except `/api/auth/*` |
| Admin-only | `/api/admin/*` |

### Test Results (61 tests, all passing)

| Suite | Tests | Status |
|-------|-------|--------|
| Smoke | 9 | All pass |
| Auth | 16 | All pass |
| Functional | 23 | All pass |
| Regression | 13 | All pass |
| **Total** | **61** | **All pass (1.4s)** |

### Application URL

- **Backend + Frontend**: http://localhost:8081
- **API**: http://localhost:8081/api/*
- **Login**: `POST /api/auth/login` with `{"email":"sampath12082@gmail.com","password":"Admin@123"}`

---

## Pending Work

### Frontend Auth (Not Yet Implemented)

The backend auth is complete but the React frontend doesn't have login/register/profile pages yet. Currently, the frontend makes unauthenticated API calls which return 401. To use the app:
- Access via API with JWT token, OR
- Implement frontend auth pages (Login, Register, Profile, Admin panel)

### Enhancements (#22-#33) — Auth & Help Module

| # | Module | Description | Status |
|---|--------|-------------|--------|
| 22 | Auth | User registration with email + password + name | Backend done |
| 23 | Auth | JWT login (access + refresh tokens) | Backend done |
| 24 | Auth | Email OTP verification on registration | Backend done |
| 25 | Auth | Forgot password (email OTP → reset) | Backend done |
| 26 | Auth | Change password (requires current) | Backend done |
| 27 | Auth | Spring Security JWT filter | Backend done |
| 28 | Profile | Profile page (view/edit name, phone; email immutable) | Backend done, frontend pending |
| 29 | Admin | Admin user seeded on startup | Done |
| 30 | Admin | Admin panel (list/manage users) | Backend done, frontend pending |
| 31 | Groww | Per-user Groww credentials (encrypted) | Not started |
| 32 | Help | FAQ page (admin-managed) | Not started |
| 33 | Help | Support tickets (user → admin) | Not started |

---

## Resolved Work

### All Bugs Resolved (20) — docs/BUGS.md
### All Prior Enhancements Resolved (#1-#21) — docs/ENHANCEMENTS.md
### Auth Backend Complete (#22-#30 backend)

---

## Project Structure

```
stock-portfolio-manager/
├── backend/          # Spring Boot 3.5 / Java 21
├── frontend/         # React 19 / Vite / Tailwind CSS
├── e2e/              # Playwright tests (61 tests)
├── scripts/          # Python data import scripts
├── docs/             # Documentation + tracking files
└── CLAUDE.md
```

## Environment Variables

```powershell
$env:DB_PASSWORD = "<postgres-password>"
$env:GROWW_API_ENABLED = "true"
$env:GROWW_ACCESS_TOKEN = "<groww-api-key>"
$env:GROWW_API_SECRET = "<groww-secret>"
$env:JWT_SECRET = "<base64-encoded-secret>"
$env:ADMIN_DEFAULT_PASSWORD = "Admin@123"
$env:SPRING_MAIL_HOST = "smtp.gmail.com"
$env:SPRING_MAIL_USERNAME = "<email>"
$env:SPRING_MAIL_PASSWORD = "<app-password>"
```

## Flyway Migrations (V1–V18)

| V | Purpose | Applied? |
|---|---------|----------|
| V1-V14 | Stock tables, signals, MF, trade_type | Yes |
| V15 | users table | Yes |
| V16 | otp_tokens table | Yes |
| V17 | Add user_id FK to all domain tables | Yes |
| V18 | Assign existing data to admin user | Yes |

## Key Files

- [CLAUDE.md](../CLAUDE.md) — Architecture reference
- [docs/ENHANCEMENTS.md](ENHANCEMENTS.md) — 12 open (#22-#33) + 21 resolved
- [docs/BUGS.md](BUGS.md) — 0 open + 20 resolved
- [docs/api-reference.md](api-reference.md) — Complete API reference with auth endpoints
- [docs/features.md](features.md) — Feature documentation with auth section
- [docs/architecture.md](architecture.md) — System architecture with security flow
