# Pending Work

## Date: 2026-06-23

## Test Quality (from coverage audit)

| # | Priority | Item | Detail |
|---|----------|------|--------|
| 1 | High | Rule 1: Status-only tests | ~50 tests in functional.spec.ts only check HTTP status, never verify outcome. Need verify-after assertions. |
| 2 | High | Rule 3: Mutations without verify | ~30 POST/PUT calls never GET back to confirm data persisted. |
| 3 | Medium | Rule 2: Forms without fill+submit | 9 pages have forms but no UI fill test: Holdings, Transactions, Stocks, MF, Signals, Register, ForgotPassword, AdminTickets, Profile update. |

## Deployment (Render)

| # | Priority | Item | Detail |
|---|----------|------|--------|
| 4 | Info | Groww token expires daily | User must paste fresh token from Groww browser session each day. Profile page now shows Connected/Disconnected status. |
| 5 | Low | SpringDoc exposed in prod | `/swagger-ui.html` and `/v3/api-docs` enabled. Set `springdoc.api-docs.enabled=false` and `springdoc.swagger-ui.enabled=false` in application-prod.yml. |
| 6 | Low | Hibernate dialect warning | Remove explicit `hibernate.dialect` from application-prod.yml. Hibernate auto-detects PostgreSQL. |

## Documentation

| # | Priority | Item | Detail |
|---|----------|------|--------|
| 7 | Medium | HANDOFF.md update | Needs latest: Groww validation, RSA key TTL fix, test coverage audit, 190 tests. |
| 8 | Low | CLAUDE.md test count | Update from 124 to 190 tests. |

## Features

| # | Priority | Item | Detail |
|---|----------|------|--------|
| 9 | Low | AI Search page redesign | Skipped in all audits. Needs UX rethink. |

## Current Stats

- **Tests:** 190 total, 189 passing (1 Groww flaky)
- **Open Bugs:** 0
- **Open Enhancements:** 0
- **Git:** Clean, all pushed
- **Migrations:** V1–V24
- **Render:** Live at https://stock-portfolio-manager-4rht.onrender.com
