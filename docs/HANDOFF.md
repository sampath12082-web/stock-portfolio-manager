# Session Handoff

## Date: 2026-06-27

## Current State

App: **http://localhost:8081** (local) | **https://stock-portfolio-manager-4rht.onrender.com** (Render)
Admin: `sampath12082@gmail.com` / `Admin@1234567890*`

### Data (local)
| Entity | Count |
|--------|-------|
| Stocks | 129 |
| Holdings | 129 (9 active) |
| Transactions | 567 |
| MF Holdings | 12 |
| Users | 3 |
| Active Signals | 4,067 |
| FAQs | 14 |

### Migrations: V1–V24

## All Session Changes (Jun 22–27)

### Features
1. AI Stock Search — chat-style, Claude API + local fallback
2. SoloSprint Trade branding — Sprint Orange, Plus Jakarta Sans, 15 pages
3. Holdings/MF total rows, MF sortable columns
4. AI Support Ticket Agent — auto-classify, Playwright bug verification, bug lifecycle
5. Groww per-user credentials — Profile config with live validation on save + page load
6. Setup-admin API — POST /api/auth/setup-admin (replaces AdminUserSeeder)
7. Dark mode — system detect + manual Sun/Moon toggle
8. Mobile bottom navigation — Home, Holdings, Trades, Stocks, More
9. Code splitting — React.lazy for all 15 pages

### Security
10. Multi-tenant findAll() eliminated — all services use findByUserId
11. SecureRandom temp passwords
12. RSA public key 5-min TTL cache (prevents stale key after restart)
13. decryptIfEncrypted throws for corrupted ciphertext
14. Rate limiting: login (5/email + 10/IP), register (3/IP), OTP (5/email), forgot/reset (3/IP), setup-admin (3/IP)
15. Setup-admin restricted — blocks new admin if one exists
16. CORS headers restricted to Authorization, Content-Type, X-Requested-With
17. Sensitive logs moved to debug level
18. Shared ClaudeApiClient (removed duplicate code)

### Deployment
19. JWT key padding for short keys
20. PostgreSQL prepareThreshold=0 for PgBouncer
21. CORS configurable via property
22. SpringDoc disabled in prod
23. Hibernate dialect auto-detected
24. Connection pool leak fixed (removed @Transactional from analysis)
25. HikariCP pool=10, leak-detection=60s
26. GitHub Actions CI/CD — 3 jobs (backend, frontend, E2E)
27. Docker — Dockerfile + docker-compose.yml

## Test Results
- **E2E:** 203+ tests across 6 suites (auth:25, functional:78, regression:22, smoke:10, ui-rendering:64+, debug-change-password:4)
- **Backend:** 47 unit tests across 10 classes
- **Total:** 250+ tests

### Test Quality (7 rules)
- R1 Outcome vs status: RESOLVED (remaining are correct error-path tests)
- R2 Form fill+submit: GOOD (12/12 form pages now have fill tests)
- R3 Mutation verify-after: MEDIUM (improved from LOW)
- R5 Encryption: GOOD (login + register encryption verified)
- R6 Session isolation: GOOD (clear state + re-login tests added)
- R7 Error paths: GOOD (47%)

## Open Bugs: 0
## Open Enhancements: 0
## App Score: 87/100

## Deferred Items
- AI Search page redesign (needs UX planning)
- Watchlist/alerts feature (new feature, needs design)
- Groww IP whitelisting for Render (requires Render paid plan)

## Key Commands
```bash
.\scripts\start.ps1          # Start locally
.\scripts\stop.ps1           # Stop
docker-compose up             # Docker
cd e2e && npm test            # E2E tests
cd backend && ./mvnw test     # Backend tests
```
