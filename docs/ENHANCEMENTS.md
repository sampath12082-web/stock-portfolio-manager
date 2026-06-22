# Enhancement Tracking

## Open Enhancements

None — all enhancements resolved.

## Resolved Enhancements

| # | Type | Description | Resolution | Date |
|---|------|-------------|-----------|------|
| 1 | Enhancement | Show Groww `clear_cash` / uninvested balance on Dashboard | Added `cashBalance` field to `DashboardResponse` | 2026-06-20 |
| 2 | Enhancement | Realized vs unrealized P&L on Dashboard | Added FIFO cost basis calculation in `DashboardServiceImpl` | 2026-06-20 |
| 3 | Bug | Trading signals should prioritize portfolio stocks | `scanMarket()` analyzes holdings first, then watchlist | 2026-06-20 |
| 4 | Enhancement | Show trade execution date, not import date | Added V10 migration + `tradeDate` field | 2026-06-20 |
| 5 | Enhancement | Analytics at top of Transactions page | Moved above filters/list | 2026-06-20 |
| 6 | Enhancement | Stocks table current price column | Already existed (LTP column) | 2026-06-20 |
| 7 | Enhancement | Remove Symbol column from Stocks page | Merged into "Stock" column | 2026-06-20 |
| 8 | Enhancement | Remove Sector column from Stocks page | Removed | 2026-06-20 |
| 9 | Enhancement | Dashboard compact summary layout | Replaced 8 StatCards with single compact card grid | 2026-06-20 |
| 10 | Enhancement | Lift Groww account above charts on Dashboard | Moved Groww section above charts/signals | 2026-06-20 |
| 11 | Enhancement | Hide cancelled orders by default | Added Executed/All toggle via GrowwOrdersSection component | 2026-06-20 |
| 12 | Enhancement | Holdings stock name + signal sort | Show companyName, sort active first then SELL>HOLD>BUY>none | 2026-06-20 |
| 13 | Enhancement | Holdings signal filter chips | Added All/BUY/SELL/HOLD/No Signal chips with count badges | 2026-06-20 |
| 14 | Enhancement | Verify transaction analytics | Confirmed analytics use same FIFO as dashboard | 2026-06-20 |
| 15 | Enhancement | Transaction compact summary | Grouped into Fund Flow / Trading / Activity sections | 2026-06-20 |
| 16 | Enhancement | Stocks sort portfolio first | Stocks with holdings (qty>0) sorted to top | 2026-06-20 |
| 17 | Enhancement | Performance verify accuracy | Snapshots use same API as dashboard; values consistent | 2026-06-20 |
| 18 | Enhancement | Performance default 7D | Changed default from 30D to 7D | 2026-06-20 |
| 19 | Feature | Mutual Funds module | Deployed — V11-V13, 15 funds, 12 holdings, 279 MF txns | 2026-06-20 |
| 20 | Enhancement | Stocks page color-coded P&L | Red text for loss, green bg for gain, blue border for held | 2026-06-20 |
| 21 | Enhancement | Simplified P&L formula | Uses Groww clearCash as source of truth | 2026-06-20 |
| 22 | Feature | User registration with email + password + security questions | Backend + frontend done | 2026-06-21 |
| 23 | Feature | JWT login (access 15min + refresh 7d) | Done | 2026-06-21 |
| 24 | Feature | Email OTP verification on registration | Mandatory — user cannot login until verified | 2026-06-21 |
| 25 | Feature | Forgot password — security questions → OTP → reset | 3-step flow in AuthController + ForgotPasswordPage | 2026-06-22 |
| 26 | Feature | Password policy (16-20 chars, upper+lower+digit+special) | PASSWORD_PATTERN regex in AuthServiceImpl | 2026-06-22 |
| 27 | Feature | Spring Security JWT filter | Protects /api/*, public /api/auth/*, admin /api/admin/* | 2026-06-21 |
| 28 | Feature | Profile page (email immutable) | View/edit name, phone, change password | 2026-06-21 |
| 29 | Feature | Admin user seeded on startup | sampath12082@gmail.com, ROLE_ADMIN, email pre-verified | 2026-06-21 |
| 30 | Feature | Admin panel (user management + tickets) | AdminUsersPage + AdminTicketsPage + sidebar links | 2026-06-22 |
| 31 | Feature | Per-user Groww API credentials | V22, UserGrowwConfig entity, profile endpoints | 2026-06-22 |
| 32 | Feature | FAQ page with 12 seeded FAQs | V19, Faq entity, HelpPage with accordion | 2026-06-22 |
| 33 | Feature | Support tickets | V20, SupportTicket entity, user submit + admin respond | 2026-06-22 |
| 34 | Feature | Password policy enforcement | Same as #26 — regex validated on register/reset/change | 2026-06-22 |
| 35 | Feature | Security questions (2 per user) | V21, BCrypt hashed answers, used for password reset | 2026-06-22 |
| 36 | Feature | RSA encryption for passwords in transit | RSA 2048-bit, /api/auth/public-key, Web Crypto frontend, OAEPParameterSpec fix | 2026-06-22 |
