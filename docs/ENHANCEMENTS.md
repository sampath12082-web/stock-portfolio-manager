# Enhancement Tracking

## Open Enhancements

| # | Type | Module | Description | Priority | Status |
|---|------|--------|-------------|----------|--------|
| 22 | Feature | Auth | User registration — email (unique, mandatory), password (BCrypt), first name, last name | P1 | Backend+Frontend done |
| 23 | Feature | Auth | JWT login — email + password → access token (15min) + refresh token (7d) | P1 | Done |
| 24 | Feature | Auth | Email OTP verification on registration — mandatory, user cannot login until verified | P1 | Backend+Frontend done |
| 25 | Feature | Auth | Forgot password — security questions → OTP → reset (3-step flow) | P1 | Pending implementation |
| 26 | Feature | Auth | Change password (16-20 chars, upper+lower+digit+special) | P1 | Pending password policy |
| 27 | Feature | Auth | Spring Security JWT filter — protect `/api/*`, public `/api/auth/*`, admin `/api/admin/*` | P1 | Done |
| 28 | Feature | Profile | Profile page — view/edit name, phone; email immutable and unique | P2 | Done |
| 29 | Feature | Admin | Seed admin user on first run (email pre-verified, ROLE_ADMIN) | P1 | Done |
| 30 | Feature | Admin | Admin panel — list users, view profiles, reset passwords, activate/inactivate/delete users | P2 | Backend done, frontend pending |
| 31 | Feature | Groww | Per-user Groww API credentials — AES-256 encrypted in DB, only owner/admin can configure | P2 | Open |
| 32 | Feature | Help | FAQ page — categorized questions/answers, admin-managed | P3 | Open |
| 33 | Feature | Help | Support tickets — user submits request, admin views/responds, status tracking | P3 | Open |
| 34 | Feature | Auth | Password policy: 16-20 chars, must contain uppercase, lowercase, digit, special char | P1 | Open |
| 35 | Feature | Auth | Security questions: 2 per user during registration, BCrypt hashed, used for password reset | P1 | Open |
| 36 | Feature | Security | RSA encryption for passwords/security answers in transit — never visible in dev tools | P1 | Open |

## Resolved Enhancements

| # | Type | Description | Resolution | Date |
|---|------|-------------|-----------|------|
| 1 | Enhancement | Show Groww `clear_cash` / uninvested balance on Dashboard | Added `cashBalance` field to `DashboardResponse` | 2026-06-20 |
| 2 | Enhancement | Realized vs unrealized P&L on Dashboard | Added FIFO cost basis calculation in `DashboardServiceImpl` | 2026-06-20 |
| 3 | Bug | Trading signals should prioritize portfolio stocks | `scanMarket()` analyzes holdings first, then watchlist | 2026-06-20 |
| 4 | Enhancement | Show trade execution date, not import date | Added V10 migration + `tradeDate` field (needs bug #12 fix to actually populate) | 2026-06-20 |
| 5 | Enhancement | Analytics at top of Transactions page | Moved above filters/list | 2026-06-20 |
| 6 | Enhancement | Stocks table current price column | Already existed (LTP column) | 2026-06-20 |
| 7 | Enhancement | Remove Symbol column from Stocks page | Merged into "Stock" column | 2026-06-20 |
| 8 | Enhancement | Remove Sector column from Stocks page | Removed | 2026-06-20 |
| 9 | Enhancement | Dashboard compact summary layout | Replaced 8 StatCards with single compact card grid | 2026-06-20 |
| 10 | Enhancement | Lift Groww account above charts on Dashboard | Moved Groww section above charts/signals | 2026-06-20 |
| 11 | Enhancement | Hide cancelled orders by default | Added Executed/All toggle via GrowwOrdersSection component | 2026-06-20 |
| 12 | Enhancement | Holdings stock name + signal sort | Show companyName, sort active first then SELL>HOLD>BUY>none | 2026-06-20 |
| 13 | Enhancement | Holdings signal filter chips | Added All/BUY/SELL/HOLD/No Signal chips with count badges | 2026-06-20 |
| 14 | Enhancement | Verify transaction analytics | Confirmed analytics use same FIFO as dashboard; grouped layout aids verification | 2026-06-20 |
| 15 | Enhancement | Transaction compact summary | Grouped into Fund Flow / Trading / Activity sections | 2026-06-20 |
| 16 | Enhancement | Stocks sort portfolio first | Stocks with holdings (qty>0) sorted to top | 2026-06-20 |
| 17 | Enhancement | Performance verify accuracy | Snapshots use same API as dashboard; values consistent | 2026-06-20 |
| 18 | Enhancement | Performance default 7D | Changed default from 30D to 7D | 2026-06-20 |
| 19 | Feature | Mutual Funds module — tables, AMFI NAV, MF page, data load | Deployed — V1-V13 applied, 15 funds, 12 holdings, 279 MF txns, NAVs refreshed | 2026-06-20 |
| 20 | Enhancement | Stocks page color-coded P&L: red text for loss, green bg for gain, blue border for held stocks | Implemented in StocksPage.tsx | 2026-06-20 |
| 21 | Enhancement | Simplified P&L: realized = deposits - (invested + clearCash), cash = clearCash - marginUsed | Removed FIFO, uses Groww data | 2026-06-20 |
