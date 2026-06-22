# API Reference

Base URL: `http://localhost:8081`

## Authentication

All `/api/*` endpoints require JWT authentication (except `/api/auth/*`).
Pass token as: `Authorization: Bearer <accessToken>`

## Auth APIs (Public)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/auth/public-key` | Get RSA public key (PEM) for password encryption |
| POST | `/api/auth/register` | Register with email + password + name + 2 security questions |
| POST | `/api/auth/verify-otp` | Verify email OTP (6-digit, 10-min expiry) |
| POST | `/api/auth/login` | Login → JWT access token (15min) + refresh token (7d) |
| POST | `/api/auth/forgot-password` | Returns security questions for the email |
| POST | `/api/auth/verify-security` | Verify security answers → sends OTP to email |
| POST | `/api/auth/reset-password` | Verify OTP + set new password |
| POST | `/api/auth/change-password` | Change password (requires current password) |
| POST | `/api/auth/refresh` | Refresh access token using refresh token |

## Profile APIs (Authenticated)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/profile` | Get own profile |
| PUT | `/api/profile` | Update name, phone (email immutable) |
| GET | `/api/profile/groww` | View own Groww config (masked — hasAccessToken/hasApiSecret) |
| PUT | `/api/profile/groww` | Set Groww access token + secret + enabled |
| DELETE | `/api/profile/groww` | Remove Groww credentials |

## Admin APIs (ROLE_ADMIN only)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/admin/users` | List all users |
| GET | `/api/admin/users/{id}` | View user profile |
| PUT | `/api/admin/users/{id}/status` | Activate/inactivate/suspend user |
| POST | `/api/admin/users/{id}/reset-password` | Reset user password (returns temp password) |
| DELETE | `/api/admin/users/{id}` | Delete user (cannot delete admin) |

## Bug Report APIs (ROLE_ADMIN only)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/admin/bugs` | List all bug reports (optional `?status=` filter) |
| PUT | `/api/admin/bugs/{id}/approve` | Approve bug for development (body: `{adminNotes, priority}`) |
| PUT | `/api/admin/bugs/{id}/reject` | Reject bug (body: `{adminNotes}`) |
| PUT | `/api/admin/bugs/{id}/start-development` | Mark bug as in development |
| PUT | `/api/admin/bugs/{id}/mark-fixed` | Mark bug as fixed, closes ticket (body: `{resolution}`) |
| POST | `/api/admin/bugs/{id}/rerun-tests` | Re-run Playwright tests for this bug |

Bug statuses: `PENDING_VERIFICATION` → `VERIFIED` / `NOT_REPRODUCIBLE` → `APPROVED` → `IN_DEVELOPMENT` → `FIXED` / `WONT_FIX`

## Ticket Activity API

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/help/tickets/{id}/activity` | Get activity timeline for a ticket |

Response: array of `{ actor, action, detail, createdAt }`. Actors: `AI_AGENT`, `ADMIN`, `USER`, `SYSTEM`.

## Stock APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/stocks` | List all stocks |
| GET | `/api/stocks/{symbol}` | Get stock by symbol |
| POST | `/api/stocks` | Create stock |
| DELETE | `/api/stocks/{id}` | Delete stock |
| GET | `/api/stocks/lookup?query=` | Smart search (DB + Yahoo Finance) |
| GET | `/api/stocks/search?query=&exchange=&sector=` | Filtered search |
| POST | `/api/stocks/refresh-sectors` | Refresh sector data from mapping |

## Holding APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/holdings` | List all holdings (with live P&L) |
| POST | `/api/holdings` | Create holding |
| PUT | `/api/holdings/{id}` | Update holding |

## Transaction APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/transactions` | List all transactions (filterable) |
| POST | `/api/transactions` | Create transaction |
| GET | `/api/transactions/analytics` | Aggregated analytics |
| POST | `/api/transactions/upload` | Upload PDF trade report |

Query params for GET: `?symbol=`, `?type=BUY`, `?from=2026-04-01&to=2026-06-30`

Transaction types: `BUY`, `SELL`, `BONUS`, `SPLIT`, `DIVIDEND`, `DEPOSIT`, `WITHDRAWAL`, `CHARGES`

Trade types: `CNC` (delivery), `MIS` (intraday), `UNKNOWN`

## Dashboard API

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/dashboard` | Portfolio summary (invested, current, P&L, deposits) |

Response: `investedAmount`, `currentValue`, `unrealizedPnL`, `unrealizedPnLPercentage`, `totalDeposited`

## Portfolio APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/portfolio/summary` | Portfolio summary with day P&L |
| GET | `/api/portfolio/allocation` | Sector allocation (active holdings) |
| GET | `/api/portfolio/pnl` | Per-stock P&L breakdown |

## Health API

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/health` | Application health check |

## Market Data APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/quotes` | All cached quotes |
| GET | `/api/quotes/{symbol}` | Quote for specific stock |
| POST | `/api/quotes/refresh` | Force refresh from Yahoo Finance |

## Performance APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/performance/recent?days=7` | Recent snapshots |
| GET | `/api/performance/history?from=&to=` | Snapshots in date range |
| GET | `/api/performance/today` | Today's snapshot |
| POST | `/api/performance/snapshot` | Capture snapshot (overwrites if exists for today) |

## Trading Signal APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/signals` | List all signals |
| GET | `/api/signals/active` | Active signals only |
| GET | `/api/signals/today` | Today's signals |
| GET | `/api/signals/recommendations` | Buy recommendations |
| POST | `/api/signals` | Create manual signal |
| POST | `/api/signals/analyze` | Run technical analysis (auto-generates signals) |
| PUT | `/api/signals/{id}` | Update signal |
| DELETE | `/api/signals/{id}` | Delete signal |

## Groww APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/groww/sync` | Sync holdings from Groww |
| POST | `/api/groww/sync-orders` | Sync today's orders |
| GET | `/api/groww/account` | Account details (balance, positions, orders) |
| GET | `/api/groww/status` | API enabled/disabled status |

## Help & FAQ APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/help/faq` | List all FAQs (grouped by category) |
| GET | `/api/help/tickets` | List own support tickets |
| POST | `/api/help/tickets` | Submit support ticket (subject + message) |
| POST | `/api/admin/faq` | Create FAQ entry (admin only) |
| PUT | `/api/admin/faq/{id}` | Update FAQ entry (admin only) |
| DELETE | `/api/admin/faq/{id}` | Delete FAQ entry (admin only) |
| GET | `/api/admin/tickets` | List all support tickets (admin only) |
| PUT | `/api/admin/tickets/{id}` | Respond to ticket (admin only) |

## AI Search APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/ai/chat` | Dynamic prompt — stock analysis, portfolio review, signals, market questions |
| GET | `/api/ai/search?query=` | Quick stock search (legacy, redirects to chat) |

Request body for chat: `{ "prompt": "analyze TCS" }`
Response: `{ "response": "...", "stockData": { symbol, ltp, signalType, ... }, "source": "local|claude" }`

## Mutual Fund APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/mf/funds` | List all MF funds |
| POST | `/api/mf/funds` | Create fund |
| DELETE | `/api/mf/funds/{id}` | Delete fund |
| GET | `/api/mf/funds/search?query=` | Search AMFI |
| POST | `/api/mf/funds/refresh-nav` | Refresh NAVs from AMFI |
| GET | `/api/mf/holdings` | List MF holdings (with P&L) |
| POST | `/api/mf/holdings` | Create MF holding |
| GET | `/api/mf/transactions` | List MF transactions |
| POST | `/api/mf/transactions` | Create MF transaction |
