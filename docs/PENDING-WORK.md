# Pending Work

## Date: 2026-06-27

## Consolidated from all 5 skill audits (Run 3)

### High Priority — Test Quality

| # | Item | Source | Detail |
|---|------|--------|--------|
| 1 | R3: Verify-after for mutations | Test Audit | ~32 POST/PUT in functional.spec.ts never GET back to confirm data persisted |
| 2 | R1: Status-only assertions | Test Audit | ~26 tests only check HTTP status, no outcome verification |
| 3 | R5: Encryption checks | Test Audit | Only 4 tests verify RSA payload; auth.spec.ts has zero |
| 4 | R6: Fresh state tests | Test Audit | Only 2 tests clear cookies/localStorage; no session isolation in main suites |

### Medium Priority — Features & Security

| # | Item | Source | Detail |
|---|------|--------|--------|
| 5 | IP-based brute-force lockout | App Critique | Rate limiter uses email key for login; add IP-based lockout after 10 failed attempts across all emails |
| 6 | AI Search page redesign | All audits | Skipped in every audit. Needs UX rethink — current chat UI functional but basic |
| 7 | Watchlist / alerts feature | App Critique | No watchlist or price alert system. Would complete the trading lifecycle |

### Medium Priority — Documentation

| # | Item | Source | Detail |
|---|------|--------|--------|
| 8 | Update all stale docs | Doc Audit | HANDOFF.md, TEST-COVERAGE-AUDIT.md, SKILL-AUDIT-RESULTS.md need test count 237, date refresh |
| 9 | Document setup-admin endpoint | Doc Audit | POST /api/auth/setup-admin missing from api-reference.md |
| 10 | Document CORS override for prod | Project Review | How to set cors.allowed-origins env var on Render |

### Low Priority — Polish

| # | Item | Source | Detail |
|---|------|--------|--------|
| 11 | R2: SignalsPage form test | Test Audit | Create/edit signal form has no fill+submit UI test |
| 12 | Backend unit tests for remaining services | App Critique | StockService, HoldingService, PortfolioService, PerformanceService, MfService still untested |
| 13 | Docker containerization | App Critique | No Dockerfile — would help local dev consistency and CI |
| 14 | Groww IP whitelisting | Info | Works locally; Render needs static IP (paid plan) or Groww IP whitelist |

## Already Completed (from audit action items)

| # | Item | Date |
|---|------|------|
| ✅ | Connection pool leak fix | Jun 27 |
| ✅ | Backend unit tests (40 total) | Jun 27 |
| ✅ | Global rate limiting (login/register) | Jun 27 |
| ✅ | Test credentials externalized | Jun 27 |
| ✅ | Mobile bottom navigation | Jun 27 |
| ✅ | Dark mode (system + toggle) | Jun 27 |
| ✅ | CI/CD pipeline (GitHub Actions) | Jun 27 |
| ✅ | Code splitting (React.lazy) | Jun 27 |
| ✅ | CORS headers restricted | Jun 27 |
| ✅ | @Autowired → Optional constructor | Jun 27 |
| ✅ | SpringDoc disabled in prod | Jun 23 |
| ✅ | Hibernate dialect removed | Jun 23 |
| ✅ | CLAUDE.md GROWW default fixed | Jun 27 |

## Current Stats

- **Tests:** 197 E2E + 40 backend = 237 total
- **Backend unit test classes:** 8 (MyportfolioApp, JWT, Password, RSA, AI, Dashboard, Transaction, Playwright)
- **Open Bugs:** 0
- **Open Enhancements:** 0
- **App Score:** 88/100
- **Git:** Clean, all pushed
- **Migrations:** V1–V24
- **Render:** Live at https://stock-portfolio-manager-4rht.onrender.com
