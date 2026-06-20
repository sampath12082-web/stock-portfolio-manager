# Handoff — Stock Portfolio Manager

## Project Summary

Indian stock portfolio + mutual funds tracking app. Spring Boot 3.5 / Java 21 backend + React 19 / Vite / Tailwind CSS frontend. Integrates with Groww API, Yahoo Finance, and AMFI NAV feed.

## Current State (as of 2026-06-20)

### What's Running (port 8081)

V1-V14 migrations applied. 129 stocks, 567 stock transactions (469 MIS + 77 CNC + 21 fund), 15 MF funds, 12 MF holdings (Rs 7,34,440 invested), 279 MF transactions. 6 frontend pages. Groww API synced.

### Dashboard Summary

| Metric | Value |
|--------|-------|
| Deposited | Rs 5,30,373 |
| Invested (stocks) | Rs 4,69,416 |
| Current Value | Rs 4,50,213 |
| Cash Balance | Rs 44,298 |
| Unrealized P&L | -Rs 19,203 (-4.09%) |
| Delivery P&L | -Rs 18,082 |
| Intraday P&L | -Rs 4,90,941 |
| Active Holdings | 9 |
| MF Invested | Rs 7,34,440 |
| MF Current | Rs 6,68,462 |

### Groww API Key — Requires Daily Renewal

Requires daily re-approval at https://groww.in/trade-api/api-keys. Without it, Groww sync returns 503.

---

## Pending Work

### Optional: Import pre-period buy data (data quality improvement)

45 stocks have sell transactions but no matching buy (bought before April 2026). Their delivery sells are currently **excluded** from realized P&L (skipped when cost basis = 0). To include them accurately:
- Parse `Stocks_Capital_Gains_*.xlsx` to extract buy prices
- Create synthetic opening-balance BUY transactions with those cost bases
- This would make delivery P&L more complete

### Validation pending

Full 52-check validation suite needs re-run after the intraday/delivery separation fixes to confirm all numbers are consistent.

---

## Resolved Work

### Resolved Bugs (19) — BUGS.md

| # | Bug | Date |
|---|-----|------|
| 1-4 | P&L zeroes/hyphens (Yahoo Finance field mapping) | 2026-06-20 |
| 5 | Groww sync 404 (conditional controller) | 2026-06-20 |
| 6-8 | Empty performance/signals/snapshots | 2026-06-20 |
| 9 | Stocks local-only search | 2026-06-20 |
| 10 | Dashboard cash balance -₹43K (added WITHDRAWAL handling) | 2026-06-20 |
| 11 | Realized P&L incorrect (cascaded from #10) | 2026-06-20 |
| 12 | tradeDate never set (added to CreateTransactionRequest + all code paths) | 2026-06-20 |
| 13 | Realized P&L Rs 35.4L phantom profit — separated intraday/delivery, skipped zero-cost pre-period sells | 2026-06-20 |
| 14 | Total P&L 664% — fixed by #13, now shows -Rs 5.3L (-99.6%) | 2026-06-20 |
| 15 | Transaction analytics inflated ~25x — added CNC/MIS breakdown | 2026-06-20 |
| 16 | FIFO sorted by createdAt — changed to tradeDate | 2026-06-20 |
| 17 | Performance snapshot totalInvestment mismatch — filtered to active holdings only | 2026-06-20 |
| 18 | Cash balance mixed intraday/delivery — fixed formula to use actual cash flows | 2026-06-20 |
| 19 | No tradeType field — added TradeType enum (CNC/MIS/UNKNOWN), V14 migration, import detection | 2026-06-20 |

### Resolved Enhancements (19) — ENHANCEMENTS.md

| # | Enhancement | Date |
|---|-------------|------|
| 1 | Cash balance on Dashboard | 2026-06-20 |
| 2 | Realized vs unrealized P&L | 2026-06-20 |
| 3 | Trading signals portfolio priority | 2026-06-20 |
| 4 | Trade date column (V10 + bug #12 fix) | 2026-06-20 |
| 5 | Analytics at top of Transactions | 2026-06-20 |
| 6 | Stock LTP column | 2026-06-20 |
| 7 | Remove Symbol column from Stocks | 2026-06-20 |
| 8 | Remove Sector column from Stocks | 2026-06-20 |
| 9-18 | UI enhancements: compact layouts, signal filters, stock sorting, Groww order toggle, performance 7D default | 2026-06-20 |
| 19 | Mutual Funds module — deploy + data load | 2026-06-20 |

### Completed Features

| Feature | Status |
|---------|--------|
| Stock CRUD + Yahoo Finance smart lookup | Done |
| Holdings with live P&L + Groww portfolio sync | Done |
| Transactions (BUY/SELL/DIVIDEND/DEPOSIT/WITHDRAWAL/CHARGES) with CNC/MIS tagging | Done |
| Groww order sync + account details | Done |
| Market data via Yahoo Finance | Done |
| Technical analysis (SMA/RSI/52-week) | Done |
| Portfolio performance snapshots (active holdings only) | Done |
| PDF upload (trade reports + Groww ledger) | Done |
| Dashboard with separated intraday/delivery P&L + Groww account | Done |
| Light theme UI | Done |
| Mutual Funds (AMFI NAV, holdings, transactions) | Done |

### Completed Data Imports

| Import | Records | Status |
|--------|---------|--------|
| Stock orders from `Stocks_Order_History_*_19-06-2026.xlsx` | 546 (469 MIS, 77 CNC) | Done |
| Dividends from `Dividend_Report_*.pdf` | 2 (TECHNOCRAFT Rs 200, TRENT Rs 180) | Done |
| Deposits from `Groww_Balance_Statement_*_19-06-2026.xlsx` | 12 (Rs 5,30,373 total) | Done |
| Charges from Balance Statement | 7 (Rs 531 total) | Done |
| MF holdings from `Mutual_Funds_*.xlsx` | 12 holdings across 12 funds | Done |
| MF trades from `MF_Capital_Gains_2025-2026.xlsx` | 279 (140 purchases + 139 redemptions) | Done |
| AMFI NAV refresh | 15 funds updated | Done |
| Groww API holdings sync | 28 holdings corrected (9 active) | Done |

---

## Import Scripts

| Script | Purpose |
|--------|---------|
| `scripts/import_stock_data.py` | Import stock orders with MIS/CNC detection, dividends, deposits, charges |
| `scripts/import_mf_data.py` | Import MF holdings + capital gains trades from Groww Excel, refresh NAVs |

---

## Groww Reports Inventory (18 files)

| File | Loaded? | Target |
|------|---------|--------|
| Stocks_Order_History_*_19-06-2026.xlsx | **Yes** | transaction_history |
| Dividend_Report_*.pdf | **Yes** | transaction_history |
| Groww_Balance_Statement_*_19-06-2026.xlsx | **Yes** | transaction_history |
| Mutual_Funds_Holdings_*.xlsx | **Yes** | mutual_fund + mf_holding |
| MF_Capital_Gains_2025-2026.xlsx | **Yes** | mf_transaction |
| Stocks_Capital_Gains_*.xlsx | **Not loaded** — could provide pre-period buy prices for 45 stocks | — |
| Stocks_Holdings_Statement_*.xlsx | Skip (validation) | — |
| MF_Order_History 2025/2026 | Skip (empty) | — |
| MF_Capital_Gains_2026-2027 | Skip (summary only) | — |
| MF_ELSS_Statements (x2) | Skip (tax ref) | — |
| F&O/Commodities/FnO_Tax | Skip (out of scope) | — |
| Demat_Report | Skip (account ref) | — |

---

## Environment

```powershell
$env:DB_PASSWORD = "<postgres-password>"
$env:GROWW_API_ENABLED = "true"
$env:GROWW_ACCESS_TOKEN = "<groww-api-key>"  # Needs daily renewal
$env:GROWW_API_SECRET = "<groww-secret>"
```

## Flyway Migrations (V1–V14)

| V | Purpose | Applied? |
|---|---------|----------|
| V1-V10 | Stock tables, signals, fund txns, trade_date | Yes |
| V11 | mutual_fund table | Yes |
| V12 | mf_holding table | Yes |
| V13 | mf_transaction table | Yes |
| V14 | trade_type column (CNC/MIS/UNKNOWN) | Yes |

## Key Files

- [CLAUDE.md](CLAUDE.md) — Architecture reference
- [ENHANCEMENTS.md](ENHANCEMENTS.md) — 0 open + 19 resolved
- [BUGS.md](BUGS.md) — 0 open + 19 resolved
- [GROWW_Reports_06192026/](GROWW_Reports_06192026/) — 18 report files (5 loaded, 13 skipped)
- [scripts/](scripts/) — Import scripts + build/deploy scripts
