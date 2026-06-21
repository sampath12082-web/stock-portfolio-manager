# API Reference

Base URL: `http://localhost:8081`

## Authentication

All `/api/*` endpoints require JWT authentication (except `/api/auth/*`).
Pass token as: `Authorization: Bearer <accessToken>`

## Auth APIs (Public)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register with email + password + name → sends OTP |
| POST | `/api/auth/verify-otp` | Verify email OTP (6-digit, 10-min expiry) |
| POST | `/api/auth/login` | Login → JWT access token (15min) + refresh token (7d) |
| POST | `/api/auth/forgot-password` | Send password reset OTP to email |
| POST | `/api/auth/reset-password` | Verify OTP + set new password |
| POST | `/api/auth/change-password` | Change password (requires current password) |
| POST | `/api/auth/refresh` | Refresh access token using refresh token |

## Profile APIs (Authenticated)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/profile` | Get own profile |
| PUT | `/api/profile` | Update name, phone (email immutable) |

## Admin APIs (ROLE_ADMIN only)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/admin/users` | List all users |
| GET | `/api/admin/users/{id}` | View user profile |
| PUT | `/api/admin/users/{id}/status` | Activate/inactivate/suspend user |
| POST | `/api/admin/users/{id}/reset-password` | Reset user password (returns temp password) |
| DELETE | `/api/admin/users/{id}` | Delete user (cannot delete admin) |

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
| GET | `/api/portfolio/stock-pnl` | Per-stock P&L breakdown |

## Market Data APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/quotes` | All cached quotes |
| POST | `/api/quotes/refresh` | Force refresh from Yahoo Finance |

## Performance APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/performance/recent?days=7` | Recent snapshots |
| GET | `/api/performance/today` | Today's snapshot |
| POST | `/api/performance/snapshot` | Capture snapshot |

## Trading Signal APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/signals/active` | Active signals |
| POST | `/api/signals/analyze` | Run technical analysis |

## Groww APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/groww/sync` | Sync holdings from Groww |
| POST | `/api/groww/sync-orders` | Sync today's orders |
| GET | `/api/groww/account` | Account details (balance, positions, orders) |
| GET | `/api/groww/status` | API enabled/disabled status |

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
