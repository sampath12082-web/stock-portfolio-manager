# Skill Audit Results

## Date: 2026-06-28 (Run 6)

All 5 skills executed in parallel via subagents.

---

## 1. Project Review

**Verdict: 0 Critical, 3 Warnings, 2 Suggestions**

### Passed (9/14)
- Backend compile + frontend build: Clean
- Backend tests: 55 pass, 0 fail
- No console.log, no @Autowired, no sensitive logs, no hardcoded secrets
- Auth config: Correct (auth public, admin ROLE_ADMIN, api authenticated)
- setup-admin: resetPassword removed, countByRole used, locked after first admin
- Rate limiting: ALL 10 auth endpoints covered (register, login, OTP, forgot, security, reset, change-password, refresh, setup-admin + public-key read-only)

### Warnings
1. **RSA key regenerates on restart** — RsaKeyService.java:24. Not persisted. Stale ciphertext undecryptable after restart.
2. **CORS localhost defaults** — WebConfig.java:11. Override needed for production.
3. **RSA decrypt silently returns ciphertext** — RsaKeyService.java:51. Could hash ciphertext as password.

### Suggestions
1. AdminController.findAll() — use paginated query for user list.
2. AuthController depends on AuthServiceImpl concrete class — extract methods to interface.

---

## 2. Functionality Review

**Verdict: 13/13 endpoints OK, 14/14 pages match, API docs complete, data integrity verified**

| Endpoint | Status | Data |
|----------|--------|------|
| /api/dashboard | 200 | invested 4.69L, current 4.31L, P&L -38K |
| /api/holdings | 200 | 80+ holdings, 9 active |
| /api/stocks | 200 | Full stock list |
| /api/transactions | 200 | 567 transactions |
| /api/transactions/analytics | 200 | CNC/MIS split, 331 buy/215 sell |
| /api/mf/holdings | 200 | 12 MF holdings |
| /api/performance/today | 200 | Snapshot, gainer=HDFCBANK |
| /api/signals/active | 200 | Active signals with rationale |
| /api/help/faq | 200 | 14 FAQs (3 test entries) |
| /api/profile | 200 | Admin, emailVerified |
| /api/admin/users | 200 | 5 users |
| /api/profile/groww | 200 | Not connected |
| /api/admin/bugs | 200 | 1 bug (NOT_REPRODUCIBLE) |

**Data integrity:** Dashboard invested (469,416) = Sum active holdings invested (469,416) ✓

Note: 3 test FAQ entries from E2E runs visible — need cleanup.

---

## 3. Test Coverage Audit

**Verdict: 216 E2E + 46 backend = 262 tests**

| Rule | Score | Detail |
|------|-------|--------|
| R1: Outcome vs status | LOW | 52/191 functional assertions status-only (27%) |
| R2: Form fill+submit | LOW | page.fill only in auth + UI specs, not functional |
| R3: Write operations | LOW | Functional suite only reads, never creates/mutates |
| R5: Payload validation | LOW | 9 total, zero in functional/regression |
| R6: Fresh state | LOW | 5 cleanups, only in auth specs |
| R7: Error paths | MEDIUM | 19/78 functional tests (24%) |

Key gap: functional.spec.ts is navigation-only — never fills forms or creates data.

---

## 4. App Critique

**Score: 86/100**

| Category | Max | Score | Notes |
|----------|-----|-------|-------|
| Feature Completeness | 15 | 14 | 15 pages, 18 controllers |
| Security | 15 | **15** | Rate limiting ALL endpoints, RSA+BCrypt+JWT+RBAC, countByRole |
| Code Quality | 12 | 11 | Zero @Autowired, Flyway, React.lazy, no console.log |
| Testing | 15 | 11 | 262 tests, dedicated test user. No integration tests. |
| Documentation | 10 | **10** | 12 docs, thorough CLAUDE.md |
| UI/UX | 13 | 11 | Dark mode + BottomNav + branding. No PWA. |
| DevOps | 12 | 10 | CI + Docker + scripts. No staging. |
| Data Integrity | 8 | 5 | P&L client-side from Groww. No server reconciliation. |

### Top 3 Strengths
1. Security — rate limiting every auth endpoint, RSA+BCrypt+JWT, admin lockdown
2. Documentation — 12 docs, full architecture coverage
3. Feature breadth — 18 controllers, AI, Groww sync, signals, MF

### Top 3 Gaps
1. Backend unit/integration tests thin (46 for 18 controllers)
2. P&L computed client-side — no server-side reconciliation
3. No staging environment or automated DB backups

---

## 5. Documentation Audit

| File | Status | Gap |
|------|--------|-----|
| CLAUDE.md | STALE | Backend count 55→46; broken GROWW table markdown |
| features.md | CURRENT | — |
| api-reference.md | CURRENT | setup-admin documented, 67 endpoints match |
| HANDOFF.md | STALE | auth 25→33, UI 64→69, backend 47→46 |
| BUGS.md | CURRENT | 0 open |
| ENHANCEMENTS.md | CURRENT | 0 open |
| PENDING-WORK.md | STALE | 237→262, Docker+setup-admin should be done |
| TEST-COVERAGE-AUDIT.md | STALE | 250→262 |
| SKILL-AUDIT-RESULTS.md | CURRENT | This file |

**Ground truth: 216 E2E + 46 backend = 262 tests. 24 migrations. 6 suites. 12 backend test classes.**

---

## Action Items

| # | Priority | Source | Item |
|---|----------|--------|------|
| 1 | Medium | Doc Audit | Fix test counts in CLAUDE.md, HANDOFF.md, PENDING-WORK.md, TEST-COVERAGE-AUDIT.md |
| 2 | Medium | Functionality | Clean 3 test FAQ entries from DB |
| 3 | Low | Project Review | RSA key persistence (prod concern, not blocking) |
| 4 | Low | Project Review | Extract AuthServiceImpl methods to interface |
| 5 | Low | App Critique | Backend integration tests |
| 6 | Low | App Critique | Server-side P&L reconciliation |

**No critical findings. All previous criticals resolved.**

## Score History

| Date | Project Review | Functionality | Test Coverage | App Score | Docs |
|------|---------------|---------------|---------------|-----------|------|
| Jun 22 | 0C/2W | 14/14 OK | 127 tests | — | — |
| Jun 23 (v1) | 0C/2W | 12/12 OK | 190 tests, R3 LOW | 83/100 | 5 stale |
| Jun 23 (v2) | 0C/0W | 12/12 OK | 197 tests, R3 LOW | 88/100 | 5 stale |
| Jun 27 (v3) | 0C/1W | BUG FOUND | 221 tests, R1-R6 LOW | 88/100 | 6 stale |
| Jun 27 (v4) | 0C/2W | 12/12 OK | 241 tests, R3 MEDIUM | 87/100 | 4 stale |
| Jun 27 (v5) | 3C/2W | 13/13 OK | 258 tests, R1/R3/R7 GOOD | 92/100 | 5 stale |
| **Jun 28 (v6)** | **0C/3W** | **13/13 OK** | **262 tests** | **86/100** | 4 stale |
