# Handoff — Stock Portfolio Manager

## Current State (as of 2026-06-22)

### What's Running (port 8081)

V1-V22 migrations. JWT auth + RSA encryption. 15 entities, 16 controllers, 22 migrations. 129 stocks, 567 transactions, 9 active holdings, 15 MF funds, 12 MF holdings, 279 MF transactions, 17 users, 12 FAQs, 128 signals. 14 frontend pages. 8 custom skills.

### Authentication
- Admin: `sampath12082@gmail.com` / `Admin@1234567890*` (ROLE_ADMIN)
- JWT: access 15min, refresh 7 days
- Password policy: 16-20 chars, upper+lower+digit+special
- RSA 2048-bit encryption for passwords in transit
- Security questions: 2 per user, BCrypt hashed, used for password reset

### App URL
- **http://localhost:8081** (backend serves frontend)
- Frontend dev: http://localhost:3000 (proxies to 8081)

---

## Pending Work

**0 open bugs. 0 open enhancements.** All 36 enhancements and 22 bugs resolved.

---

## Project Structure
```
backend/    — Spring Boot 3.5 / Java 21 (15 entities, 16 controllers, V1-V22)
frontend/   — React 19 / Vite / Tailwind (14 pages, 9 hooks)
e2e/        — Playwright (5 test files)
scripts/    — Python import scripts (stock + MF)
docs/       — 7 documentation files
.claude/skills/ — 8 custom skills
```

## Key Files
- [CLAUDE.md](../CLAUDE.md) — Architecture reference
- [docs/ENHANCEMENTS.md](ENHANCEMENTS.md) — 0 open + 36 resolved
- [docs/BUGS.md](BUGS.md) — 0 open + 22 resolved
- [docs/user-module.md](user-module.md) — Auth, security questions, RSA, password policy
- [docs/api-reference.md](api-reference.md) — Complete API reference
- [docs/features.md](features.md) — All 14 pages documented
- [docs/architecture.md](architecture.md) — System diagram + ER diagram

## Flyway Migrations (V1-V22)
| Range | Purpose |
|-------|---------|
| V1-V14 | Stock, holding, transaction, quote, snapshot, signal, MF, trade_type |
| V15-V18 | Users, OTP, user_id FK, admin seed |
| V19-V20 | FAQ, support tickets |
| V21 | Security questions on users |
| V22 | Per-user Groww config |
