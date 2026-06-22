# Session Handoff

## Date: 2026-06-22

## Current State

App running at **http://localhost:8081**. Admin: `sampath12082@gmail.com` / `Admin@1234567890*`.

### Data
| Entity | Count |
|--------|-------|
| Stocks | 129 |
| Holdings | 129 (9 active) |
| Transactions | 567 |
| MF Holdings | 12 |
| Users | 29 |
| Active Signals | 1,790 |

### Migrations: V1–V24
- V1-V14: Core domain (stocks, holdings, transactions, signals, MF, trade_type)
- V15-V18: Users, OTP, user_id FK, admin seed
- V19-V20: FAQ (14), support tickets
- V21: Security questions
- V22: Per-user Groww config
- V23-V24: AI support agent (bug_report, ticket_activity, ticket classification fields)

### Branding
SoloSprint Trade — Sprint Orange (#D85A30), Plus Jakarta Sans + JetBrains Mono, cream background (#FAFAF8).

## Recent Changes (This Session)
1. **AI Stock Search** — Chat-style page, dynamic prompts, Claude API + local fallback
2. **SoloSprint Trade branding** — All 15 pages, sidebar, header, login/register/forgot
3. **Holdings total row** — Footer sums qty, invested, current, realized/unrealized P&L
4. **MF total row** — Footer sums units, invested, current, P&L, P&L%
5. **MF sortable columns** — Holdings + transactions tables both sortable
6. **Dashboard MF layout** — Col 1: Invested + Current | Col 2: Holdings + P&L
7. **AI Support Ticket Agent** — Auto-classifies tickets, generates responses, runs Playwright tests for bugs, full bug lifecycle with admin approval
8. **33 new E2E tests** — Signals, Help/FAQ, Quotes, Password Policy, Security Questions, Groww, Admin
9. **8 new UI tests** — Performance, Admin Tickets, Register, Forgot Password, Login branding
10. **Bug fixes** — Today's Orders filter, test password updates, strict selectors

## Test Results
- **124 tests** across 5 suites (smoke:10, auth:17, functional:56, regression:13, ui-rendering:28)
- 121 passed in last run, 3 flaky (token expiry in long runs)

## Open Bugs: 0
## Open Enhancements: 0

## Known Issues
- **AI Search page** needs redesign (not tested, skipped intentionally)
- **Playwright test runner** — `e2e.test-dir` may need absolute path if `../e2e` doesn't resolve
- **3 DELETE endpoints** untested — intentionally skipped to avoid data loss

## Environment
```
DB_PASSWORD=<set in env>
DB_USER=sampat
GROWW_API_ENABLED=true
ANTHROPIC_API_KEY=<optional, enables Claude AI for ticket agent + stock search>
```

## Key Files Modified This Session
- `backend/.../service/AiTicketAgentService.java` — AI ticket classification + response
- `backend/.../service/BugLifecycleService.java` — Bug approve/reject/fix lifecycle
- `backend/.../service/PlaywrightTestRunnerService.java` — ProcessBuilder test execution
- `frontend/src/pages/HelpPage.tsx` — AI responses, bug progress, auto-refresh
- `frontend/src/pages/AdminTicketsPage.tsx` — Filter tabs, bug management, lifecycle buttons
- `frontend/src/components/brand/Logo.tsx` — SoloSprint Trade logo component
