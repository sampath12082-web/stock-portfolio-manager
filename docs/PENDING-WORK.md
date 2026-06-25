# Pending Work

## Date: 2026-06-23 (updated)

## Completed This Session

| # | Item | Status |
|---|------|--------|
| 1 | Rule 1: Status-only tests | PARTIAL — 4 key mutations strengthened with verify-after |
| 2 | Rule 3: Mutations without verify | PARTIAL — ticket, FAQ, profile, admin status now verify |
| 5 | SpringDoc exposed in prod | DONE — disabled in application-prod.yml |
| 6 | Hibernate dialect warning | DONE — removed explicit dialect |
| 7 | HANDOFF.md update | DONE — full rewrite |
| 8 | CLAUDE.md test count | DONE — updated to 190 |

## Still Pending

| # | Priority | Item | Detail |
|---|----------|------|--------|
| 1 | Medium | Rule 1: More status-only tests | ~46 remaining in functional.spec.ts |
| 2 | Medium | Rule 3: More mutations | ~26 remaining POST/PUT without verify |
| 3 | Medium | Rule 2: Forms without fill+submit | 9 pages: Holdings, Transactions, Stocks, MF, Signals, Register, ForgotPassword, AdminTickets, Profile update |
| 4 | Info | Groww token expires daily | IP-bound — works locally, needs IP whitelisting for Render |
| 9 | Low | AI Search page redesign | Skipped in all audits |

## Current Stats

- **Tests:** 190 total, 181+ passing
- **Open Bugs:** 0
- **Open Enhancements:** 0
- **Git:** Clean, all pushed
- **Migrations:** V1-V24
- **Render:** Live at https://stock-portfolio-manager-4rht.onrender.com
