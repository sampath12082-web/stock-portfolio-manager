# Session Handoff

## Date: 2026-06-23

## Current State

App running at **http://localhost:8081** (local) and **https://stock-portfolio-manager-4rht.onrender.com** (Render).

### Data (local)
| Entity | Count |
|--------|-------|
| Stocks | 129 |
| Holdings | 129 (9 active) |
| Transactions | 567 |
| MF Holdings | 12 |
| Users | 29+ |
| Active Signals | 1,790+ |

### Migrations: V1-V24
- V1-V14: Core domain
- V15-V18: Users, OTP, user_id FK, admin seed
- V19-V20: FAQ, support tickets
- V21: Security questions
- V22: Per-user Groww config
- V23-V24: AI support agent (bug_report, ticket_activity, ticket classification)

### Branding
SoloSprint Trade — Sprint Orange (#D85A30), Plus Jakarta Sans + JetBrains Mono.

## Session Changes (Jun 22-23)

### Features
1. AI Stock Search — chat-style, Claude API + local fallback
2. SoloSprint Trade branding — all 15 pages
3. Holdings/MF total rows, MF sortable columns
4. AI Support Ticket Agent — auto-classify, Playwright bug verification, bug lifecycle
5. Groww per-user credentials — Profile page config with live validation
6. Groww connection status — green/red dot on page load
7. Setup-admin API — POST /api/auth/setup-admin (replaces AdminUserSeeder)

### Security Fixes
8. Multi-tenant findAll() eliminated — all services use findByUserId
9. SecureRandom temp passwords (was predictable)
10. RSA public key 5-min TTL cache (was cached forever, broke after restart)
11. decryptIfEncrypted throws for corrupted ciphertext (was silent fallback)
12. Sensitive logs moved to debug level
13. Rate limiting on ticket submission (3 per 5 min)
14. Shared ClaudeApiClient (removed duplicate code)

### Deployment Fixes
15. JWT key padding for short keys
16. PostgreSQL prepareThreshold=0 for PgBouncer
17. CORS configurable via property
18. Prod config YAML structure fixed
19. SpringDoc disabled in prod
20. Hibernate dialect warning removed

## Test Results
- **190 tests** across 6 suites
- auth: 20, functional: 78, regression: 22, smoke: 10, ui-rendering: 56, e2e-change-password: 4
- 189 passing, 1 Groww flaky

### Test Quality (7 rules)
- R1 Outcome vs status: LOW (50 status-only)
- R2 Form fill+submit: MEDIUM
- R3 Mutation verify-after: LOW (30 without verify)
- R4 Cross-page E2E: GOOD
- R5 Encryption: GOOD
- R6 Fresh state: GOOD
- R7 Error paths: GOOD (32%)

## Open Bugs: 0
## Open Enhancements: 0

## Pending Work
See `docs/PENDING-WORK.md` — 9 items (test quality, docs, prod config).

## Environment
```
DB_PASSWORD=<set in env>
DB_USER=sampat
GROWW_API_ENABLED=true (default)
ANTHROPIC_API_KEY=<optional>
```

## Key Commands
```bash
.\scripts\start.ps1   # Start locally
.\scripts\stop.ps1    # Stop
cd e2e && npm test     # Run all 190 tests
```
