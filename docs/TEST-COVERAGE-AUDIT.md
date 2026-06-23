# Test Coverage Audit Report

## Latest Audit: 2026-06-23 (Quality Rules Applied)

## Summary

| Metric | Count |
|--------|-------|
| Total tests | 190 |
| Form fill interactions | 23 |
| Network intercepts | 5 |
| Cross-page E2E flows | 5 |
| Fresh state tests | 2 |
| Error path references | 61 (32%) |

## Per-Rule Compliance

| Rule | Score | Detail |
|------|-------|--------|
| R1: Outcome vs status | **LOW** | ~50 status-only assertions with no behavior verification |
| R2: Form fill+submit | **MEDIUM** | 23 fills but mostly in 2 files. 12 pages with forms, most have no fill test |
| R3: Mutation verify-after | **LOW** | ~30 POST/PUT with no GET to verify data persisted |
| R4: Cross-page E2E flows | **GOOD** | Change password→re-login, old password rejected, restart survival |
| R5: Encryption verification | **GOOD** | 5 encrypted payload checks, 4 raw passwords (API-only, acceptable) |
| R6: Fresh state | **GOOD** | clearCookies + localStorage.clear in restart test |
| R7: Error path ratio | **GOOD** | 61/190 = 32% (target ≥30%) |

## Remaining Gaps (Priority Order)

### Rule 1 Violations — Status-only tests to strengthen

These tests check HTTP status but never verify the outcome:

| Test | Current | Should Add |
|------|---------|-----------|
| Signals analyze → 200 | Status only | GET /signals/active after, verify count increased |
| Admin FAQ create → 201 | Status only | GET /help/faq after, verify new FAQ appears |
| Quotes refresh → 200 | Status only | GET /quotes after, verify data updated |
| Performance snapshot → 200 | Status only | GET /performance/today after, verify snapshot |
| Admin user status → 200 | Status only | GET /admin/users/{id} after, verify status changed |
| Profile update → 200 | Checks response | Also re-GET /profile and verify |
| Groww sync → 200 | Status only | GET /holdings after, verify count |
| Ticket submit → 201 | Status only | GET /help/tickets after, verify ticket appears with AI response |

### Rule 3 Violations — Mutations without verify-after

| Mutation | Verify Missing |
|----------|---------------|
| POST /api/help/tickets | No GET to check ticket + AI classification |
| POST /api/admin/faq | No GET to check FAQ created |
| POST /api/performance/snapshot | No GET to check snapshot exists |
| PUT /api/admin/users/{id}/status | No GET to verify status changed |
| PUT /api/profile | Response checked but no re-GET |
| PUT /api/profile/groww | Partial — has save+delete test but no re-GET verify |
| POST /api/auth/register (validation) | Correct for error tests |
| POST /api/auth/setup-admin | Has login-after verify — GOOD |

### Rule 2 Gaps — Forms without interactive tests

| Page | Form Elements | Has Fill+Submit Test |
|------|--------------|---------------------|
| HoldingsPage | 6 | **No** — add modal fill+submit |
| TransactionsPage | 8 | **No** — add modal fill+submit |
| StocksPage | 7 | **No** — add search+select |
| MutualFundsPage | 9 | **No** — add fund search+add |
| SignalsPage | 9 | **No** — add create signal form |
| RegisterPage | 9 | **No** — add full form fill with security Qs |
| ForgotPasswordPage | 5 | **No** — add 3-step UI flow |
| AdminTicketsPage | 3 | **No** — add respond textarea+submit |
| ProfilePage | 8 | **Partial** — change password done, profile update + Groww not filled via UI |
| LoginPage | 2 | **Yes** — full fill+submit+redirect |
| HelpPage | 2 | **Yes** — ticket submit via UI |

## Test Quality Score

| Category | Before (Jun 22) | After (Jun 23) | Target |
|----------|-----------------|----------------|--------|
| Total tests | 127 | 190 | — |
| Form fills | 2 | 23 | 1 per form |
| Network intercepts | 0 | 5 | All auth flows |
| Cross-page E2E | 0 | 5 | All critical paths |
| Fresh state tests | 0 | 2 | All auth flows |
| Error path % | ~25% | 32% | ≥30% |
| Status-only tests | ~80 | ~50 | 0 |
| Verify-after mutations | 0 | ~10 | 100% |

## Audit History

| Date | Tests | R1 | R2 | R3 | R4 | R5 | R6 | R7 |
|------|-------|----|----|----|----|----|----|-----|
| 2026-06-23 (v1) | 127 | LOW | LOW | LOW | NONE | NONE | NONE | LOW |
| 2026-06-23 (v2) | 190 | LOW | MED | LOW | GOOD | GOOD | GOOD | GOOD |
