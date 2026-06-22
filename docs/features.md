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

### Password Policy
- Length: 16–20 characters
- Must contain: uppercase, lowercase, digit, special character
- Validated on register, reset, and change password

### Security Questions
- 2 questions selected during registration (8 options)
- Answers BCrypt-hashed — used in forgot password step 2
- 3-step forgot flow: email → verify security answers → OTP + new password

### RSA Encryption
- Frontend encrypts passwords using RSA-OAEP SHA-256 before sending
- Backend decrypts with private key — passwords never transit in plaintext
- Public key served via `/api/auth/public-key`

### Admin User
- Seeded on every startup: `sampath12082@gmail.com` (ROLE_ADMIN)
- Default password: `Admin@1234567890*` (18 chars, meets policy)
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
- **Mutual Funds summary**: Col 1: Invested, Current Value | Col 2: Holdings count, P&L
- **Sector Allocation**: Pie chart of active holdings by sector
- **Top Holdings**: Table of top 5 by current value
- **Portfolio Value chart**: 30-day area chart from snapshots
- **Trading Signals**: Latest 5 signals with target prices
- **Today's Orders**: Groww orders with Executed/All toggle (bottom of page)

### 2. Holdings (`/holdings`)
- **Summary cards**: Total Invested, Current, P&L
- **Search**: Client-side filter by symbol or company name
- **Signal filter chips**: ALL, BUY, SELL, HOLD, No Signal with counts
- **Sortable table**: Stock, Qty, Avg Price, LTP, Target, Invested, Current Value, Realized P&L, Unrealized P&L, Signal
- **Color coding**: Green row for gain, red for loss, blue left border for active holdings
- **Sticky header**: Stays visible while scrolling
- **Total row**: Footer sums qty, invested, current value, realized P&L, unrealized P&L
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

### 5. AI Stock Assistant (`/ai-search`)
- **Chat interface**: Full-height with message bubbles (user = orange, assistant = white)
- **Dynamic prompts**: Accepts any question — stock analysis, portfolio review, signals, market trends
- **Stock detection**: Auto-detects stock symbols in prompt, fetches live quote + signal
- **Stock data card**: Embedded in responses with LTP, day change, signal badge, exchange
- **Suggestion chips**: 8 quick-start prompts for new users
- **Sources**: Local analysis from signals data, or Claude API when `ANTHROPIC_API_KEY` set
- **Typing indicator**: Animated dots during response generation
- **Recent history**: Session-based search history with quick-select

### 6. Help & Support (`/help`)
- **FAQ accordion**: Grouped by category (General, Account, Trading, Data, Technical)
- **14 seeded FAQs**: Pre-populated via V19 migration + admin additions
- **Support tickets**: Submit with subject + message → AI agent auto-processes
- **AI Agent**: Auto-classifies tickets (Bug/Inquiry/Feature/Feedback), generates smart responses
- **Bug verification**: For bug reports, Playwright tests run automatically to verify
- **Ticket type badges**: Bug (red), Inquiry (blue), Feature (purple), Feedback (green)
- **AI response display**: Purple box with Sparkles icon shows AI-generated response
- **Bug report progress**: Shows test status (passing/failing), estimation, fix status
- **Auto-refresh**: Polls every 8s while tickets are in OPEN status (waiting for AI)
- **My tickets**: Full lifecycle view with AI response, admin response, bug report card
- **Ticket statuses**: OPEN → AI_REVIEWED → BUG_CONFIRMED/BUG_NOT_CONFIRMED → APPROVED → IN_DEVELOPMENT → RESOLVED → CLOSED

### 7. Mutual Funds (`/mutual-funds`)
- **Sortable holdings table**: Fund, Fund House, Units, Avg NAV, Current NAV, Invested, Current Value, P&L, P&L%
- **Sortable transactions table**: Date, Fund, Type, Units, NAV, Amount
- **Total row**: Footer sums units, invested, current value, P&L, P&L%
- **Color coding**: Green row (current NAV > avg NAV), red text (current NAV < avg NAV)
- **AMFI search**: Search and add funds from AMFI catalog
- **NAV refresh**: Bulk refresh from AMFI feed
- **Add fund/holding/transaction modals**

### 8. Performance (`/performance`)
- **Snapshot summary**: Investment, Current Value, Total P&L, Top Gainer/Loser
- **Time range buttons**: 7D (default), 30D, 90D, 1Y
- **Portfolio Value chart**: Area chart with investment line overlay
- **Snapshot history table**: Date, Investment, Value, P&L, P&L%, Gainer, Loser
- **Manual capture**: Button to create snapshot on demand

### 9. Login (`/login`)
- Email + password form
- Error messages for invalid credentials, unverified email, inactive account
- Links to Register and Forgot Password
- Redirects to Dashboard on success

### 10. Register (`/register`)
- Step 1: Email, first name, last name, password (16-20 chars), 2 security questions
- Step 2: OTP verification (6-digit code sent to email)
- Redirects to Login after verification

### 11. Forgot Password (`/forgot-password`)
- Step 1: Enter email → returns security questions
- Step 2: Answer security questions → sends OTP
- Step 3: Enter OTP + new password (16-20 chars) → resets
- Redirects to Login on success

### 12. Profile (`/profile`)
- **Personal Details**: email (read-only), first name, last name, phone, role display
- **Change Password**: current password + new password (16-20 chars) form
- **Groww Config**: Access token, API secret, enable/disable (admin or per-user)
- Save button updates profile via API

### 13. Admin — User Management (`/admin/users`)
- Table: email, name, role, status, actions
- Actions: activate, deactivate, reset password, delete
- Reset returns temporary password
- Cannot delete admin user
- Visible only to ROLE_ADMIN (sidebar conditional)

### 14. Admin — Support Tickets (`/admin/tickets`)
- **Filter tabs**: All, Pending, Bugs, Inquiries, Features with counts
- **Ticket cards**: Subject, type badge, status badge, priority badge, user info, date
- **AI response display**: Purple box shows AI agent's auto-response
- **Bug management inline**: For verified bugs — Approve/Reject buttons, priority selector
- **Bug lifecycle buttons**: Start Development → Mark Fixed progression
- **Re-run Tests**: Button to re-trigger Playwright tests on a bug
- **Test results**: Shows pass/fail count and failed test names
- **Respond**: Inline textarea + status dropdown for admin replies
- **Expanded statuses**: AI_REVIEWED, BUG_CONFIRMED, BUG_NOT_CONFIRMED, APPROVED, IN_DEVELOPMENT

### 15. Header (all pages)
- SoloSprint Trade brand in sidebar
- User's first name with profile link
- Refresh Quotes button
- Orange Logout button (clears JWT, redirects to /login)
- Version badge (v0.1.0-beta) in sidebar footer

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
