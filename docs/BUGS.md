# Bug Tracking

## Open Bugs

| # | Severity | Bug | Root Cause | Status |
|---|----------|-----|-----------|--------|
None — all bugs resolved.

## Resolved Bugs

| # | Severity | Bug | Resolution | Date |
|---|----------|-----|-----------|------|
| 1 | Critical | Dashboard P&L shows zeroes | Fixed YahooFinanceProvider field mapping | 2026-06-20 |
| 2 | Critical | Holdings table shows hyphens for LTP, Current, P&L | Fixed by bug #1 | 2026-06-20 |
| 3 | Critical | Top Holdings table shows no numbers | Fixed by bug #1 | 2026-06-20 |
| 4 | Critical | Day Change shows zeroes | Fixed by bug #1 | 2026-06-20 |
| 5 | High | Sync from Groww button returns 404 | Added non-conditional GrowwStatusController | 2026-06-20 |
| 6 | Medium | Portfolio Value chart empty | Auto-create snapshot on first load | 2026-06-20 |
| 7 | Medium | Trading Signals section empty | Added "Run Analysis" button | 2026-06-20 |
| 8 | Medium | Performance page empty | Fixed by bug #6 | 2026-06-20 |
| 9 | Low | Stocks page search local-only | Added debounced API lookup with web results | 2026-06-20 |
| 10 | Critical | Dashboard cash balance shows -₹43K | Fixed — added WITHDRAWAL handling, fixed cash balance formula in DashboardServiceImpl | 2026-06-20 |
| 11 | Critical | Dashboard realized P&L incorrect | Fixed — cascaded from #10 fix | 2026-06-20 |
| 12 | High | All transactions show 06/20 as trade date | Fixed — added tradeDate to CreateTransactionRequest, set in TransactionServiceImpl + GrowwSyncServiceImpl | 2026-06-20 |
| 13 | Critical | Realized P&L Rs 35.4L phantom profit | Fixed — separated intraday (MIS) from delivery (CNC), skipped zero-cost pre-period sells in FIFO | 2026-06-20 |
| 14 | Critical | Total P&L 664% return impossible | Fixed — cascaded from #13, now shows -Rs 5.3L (-99.6%) | 2026-06-20 |
| 15 | High | Transaction analytics inflated ~25x | Fixed — added separate intraday/delivery volume and P&L fields | 2026-06-20 |
| 16 | High | FIFO sorts by createdAt not tradeDate | Fixed — changed to tradeDate in DashboardServiceImpl + TransactionServiceImpl | 2026-06-20 |
| 17 | High | Performance snapshot totalInvestment mismatch | Fixed — filtered to active holdings (qty > 0) in PerformanceServiceImpl | 2026-06-20 |
| 18 | Medium | Cash balance mixes intraday/delivery flows | Fixed — formula uses actual delivery buy/sell flows + intraday net P&L | 2026-06-20 |
| 19 | Medium | No tradeType in data model | Fixed — added TradeType enum (CNC/MIS/UNKNOWN), V14 migration, import auto-detection | 2026-06-20 |
| 20 | Medium | Sector allocation empty — all stocks have sector=null | Fixed — added POST /api/stocks/refresh-sectors endpoint, filtered allocation to active holdings | 2026-06-20 |
| 21 | High | Admin panel not accessible for admin login | Fixed — added AdminUsersPage, sidebar link (conditional ROLE_ADMIN), /admin/users route | 2026-06-21 |
| 22 | Low | Dashboard shows redundant Today's Positions section | Fixed — removed Today's Positions, kept Today's Orders only | 2026-06-22 |
| 23 | Medium | Today's Orders doesn't show on dashboard | Fixed — default filter changed from EXECUTED-only to ALL, so APPROVED/PENDING orders are visible | 2026-06-22 |
