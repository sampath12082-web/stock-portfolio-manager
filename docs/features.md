# Feature Documentation

## Authentication & Security

### Login & Registration
- Email-based registration with mandatory email verification (6-digit OTP, 10-min expiry)
- JWT stateless authentication: access token (15min) + refresh token (7 days)
- BCrypt password hashing
- Forgot password flow: email → OTP → reset
- Change password (requires current password)

### Roles & Access Control
- `ROLE_USER` — access own portfolio data
- `ROLE_ADMIN` — manage all users, reset passwords, activate/deactivate/delete users
- All `/api/*` endpoints protected (except `/api/auth/*`)
- Admin endpoints at `/api/admin/*` restricted to ROLE_ADMIN

### Admin User
- Seeded on first startup: `sampath12082@gmail.com` (ROLE_ADMIN)
- Default password: `Admin@123` (configurable via `ADMIN_DEFAULT_PASSWORD` env var)
- All existing portfolio data assigned to admin user

### Profile Management
- View/edit: first name, last name, phone
- Email is immutable and unique — used as login identifier

### Multi-Tenancy
- `user_id` foreign key on all domain tables (stock, holding, transaction, etc.)
- Each user sees only their own data

## Pages

### 1. Dashboard (`/`)
- **Portfolio summary**: Deposited, Invested, Current Value, Cash Balance, Unrealized/Realized/Total P&L, Day Change
- **Groww Account**: Available Cash, Clear Cash, Margin Used, UCC, exchange/segment status
- **Mutual Funds summary**: Holdings count, Invested, Current Value, P&L
- **Sector Allocation**: Pie chart of active holdings by sector
- **Top Holdings**: Table of top 5 by current value
- **Portfolio Value chart**: 30-day area chart from snapshots
- **Trading Signals**: Latest 5 signals with target prices
- **Today's Positions**: Groww intraday positions (if any)
- **Today's Orders**: Groww orders with Executed/All toggle (bottom)

### 2. Holdings (`/holdings`)
- **Summary cards**: Total Invested, Current, P&L
- **Search**: Client-side filter by symbol or company name
- **Signal filter chips**: ALL, BUY, SELL, HOLD, No Signal with counts
- **Sortable table**: Stock, Qty, Avg Price, LTP, Target, Invested, Current Value, Realized P&L, Unrealized P&L, Signal
- **Color coding**: Green row for gain, red for loss, blue left border for active holdings
- **Sticky header**: Stays visible while scrolling
- **Groww sync**: Button to sync holdings from broker
- **Add/Edit modals**: CRUD operations

### 3. Transactions (`/transactions`)
- **Analytics cards**: Fund Flow (deposited/withdrawn/charges), Delivery CNC (buy/sell/P&L), Intraday MIS (buy/sell/P&L), Activity (counts/most traded)
- **Monthly chart**: Bar chart of transactions by month (uses tradeDate)
- **Filters**: Symbol text filter, transaction type dropdown
- **Sortable table**: Trade Date, Symbol, Type, Mode (CNC/MIS badge), Qty, Price, Total
- **Sticky header**: Scrollable with frozen header
- **Groww order sync**: Import today's orders
- **PDF upload**: Import trade reports
- **Add transaction modal**: Manual entry for all types

### 4. Stocks (`/stocks`)
- **Search**: Client-side filter + Yahoo Finance web results
- **Signal filter chips**: ALL, BUY, SELL, HOLD, WATCH, No Signal
- **Target filter chips**: All, Has Target, No Target
- **Sortable table**: Stock, Exchange, My Qty, Avg Price, Current Price, Target, Signal
- **Blue left border**: For currently held stocks
- **Sticky header**: Scrollable
- **Add Stock modal**: Smart search with AMFI/Yahoo lookup
- **Web results**: Yahoo Finance results shown below for non-DB stocks

### 5. Mutual Funds (`/mutual-funds`)
- **Holdings table**: Scheme name, fund house, units, avg NAV, invested, current NAV, current value, P&L
- **Transactions table**: Scheme, type (PURCHASE/REDEMPTION), units, NAV, amount, date
- **AMFI search**: Search and add funds from AMFI catalog
- **NAV refresh**: Bulk refresh from AMFI feed
- **Add fund/holding/transaction modals**

### 6. Performance (`/performance`)
- **Snapshot summary**: Investment, Current Value, Total P&L, Top Gainer/Loser
- **Time range buttons**: 7D (default), 30D, 90D, 1Y
- **Portfolio Value chart**: Area chart with investment line overlay
- **Snapshot history table**: Date, Investment, Value, P&L, P&L%, Gainer, Loser
- **Manual capture**: Button to create snapshot on demand

## P&L Formulas

- **Realized P&L** = (Total Invested + Clear Cash) - Total Deposited
- **Unrealized P&L** = Current Value - Invested Amount
- **Total P&L** = Realized + Unrealized
- **Cash Balance** = Groww Clear Cash
- **Per-stock Realized P&L** = Total Sold - (Total Bought - Current Holdings Cost)
- **Per-stock Unrealized P&L** = (Qty × LTP) - Current Holdings Cost

## Scheduled Jobs (Weekdays Only)

| Time | Job | Purpose |
|------|-----|---------|
| 9:00 AM | Morning quote refresh | Pre-warm Yahoo Finance cache |
| 3:30 PM | Daily snapshot | Capture portfolio state after NSE close |
| 4:00 PM | Technical analysis | SMA/RSI signals on holdings + market scan |
| 6:00 PM | Expire signals | Deactivate signals >7 days old |
| 9:00 PM | MF NAV refresh | Update mutual fund NAVs from AMFI |

## Data Import

Two Python scripts in `scripts/`:
- `import_stock_data.py` — Imports stock orders (with MIS/CNC auto-detection), dividends, deposits, charges from Groww Excel reports
- `import_mf_data.py` — Imports MF holdings and capital gains trades from Groww Excel, refreshes NAVs

Both require: `pip install openpyxl requests`. Backend must be running on port 8081.
