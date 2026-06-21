# Handoff — Stock Portfolio Manager

## Current State (as of 2026-06-21)

### What's Running (port 8081)

V1-V18 migrations. JWT auth. 12 entities, 15 controllers, 18 migrations. 129 stocks, 567 transactions, 9 active holdings, 15 MF funds, 12 MF holdings, 279 MF transactions, 8 users. 11 frontend pages. 61 E2E tests passing.

### Authentication
- Admin: `sampath12082@gmail.com` / `Admin@123` (ROLE_ADMIN)
- JWT: access 15min, refresh 7 days

### Test Results: 61 passed (3.1s)

| Suite | Tests |
|-------|-------|
| Smoke | 9 |
| Auth | 16 |
| Functional | 23 |
| Regression | 13 |

---

## Open Bugs (1)

| # | Severity | Bug |
|---|----------|-----|
| 21 | High | Admin panel — no sidebar link, no /admin route for ROLE_ADMIN |

## Open Enhancements (10)

| # | Module | Description | Status |
|---|--------|-------------|--------|
| 25 | Auth | Forgot password — security questions + OTP 3-step | Pending |
| 26 | Auth | Password policy (16-20 chars, complexity) | Pending |
| 30 | Admin | Admin panel frontend | Backend done, frontend pending |
| 31 | Groww | Per-user Groww credentials (AES encrypted) | Open |
| 32 | Help | FAQ page | Open |
| 33 | Help | Support tickets | Open |
| 34 | Auth | Password policy enforcement | Open |
| 35 | Auth | Security questions (2 per user) | Open |
| 36 | Security | RSA encryption for passwords in transit | Open |

## Doc Gaps Found
- CLAUDE.md: V1-V14 reference needs V1-V18
- api-reference.md: 6 endpoints in code but not documented
- features.md: missing Login, Register, ForgotPassword, Profile pages

---

## Project Structure
```
backend/    — Spring Boot 3.5 / Java 21 (12 entities, 15 controllers, V1-V18)
frontend/   — React 19 / Vite / Tailwind (11 pages, 9 hooks)
e2e/        — Playwright (4 test files, 61 tests)
scripts/    — Python import scripts (stock + MF)
docs/       — 8 documentation files
.claude/skills/ — 7 custom skills
```

## Key Files
- [CLAUDE.md](../CLAUDE.md) — Architecture reference
- [docs/ENHANCEMENTS.md](ENHANCEMENTS.md) — 10 open + 21 resolved
- [docs/BUGS.md](BUGS.md) — 1 open + 20 resolved
- [docs/user-module.md](user-module.md) — Auth module documentation
