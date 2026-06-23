# Test Coverage Audit Report

## Date: 2026-06-23

## Summary

| Metric | Count |
|--------|-------|
| Frontend pages | 15 |
| Form elements across app | 135 |
| API endpoints | 75 |
| Existing tests | 127 |
| Form fill interactions in tests | 2 |
| DELETE tests | 0 |
| XSS/injection tests | 0 |
| Setup-admin tests | 0 |
| Gaps found | 42 |

## Page-by-Page Coverage

| Page | Forms | API Tests | UI Tests | Form Tests | Gaps |
|------|-------|-----------|----------|------------|------|
| Dashboard | 0 | 14 | 9 | — | Refresh Quotes click, Orders toggle |
| Holdings | 6 | 21 | 5 | 0 | Add/Edit form, search, sort, filter chips, total row |
| Transactions | 8 | 20 | 4 | 0 | Add form, upload, filters, sort |
| Stocks | 7 | 15 | 4 | 0 | Add form, search, signal/target chips |
| Mutual Funds | 9 | 0 | 0 | 0 | Entire page untested by name |
| Performance | 0 | 11 | 4 | — | Capture snapshot button |
| Profile | 8 | 19 | 2 | 0 | Update profile form, change password UI, Groww config form |
| Help | 2 | 10 | 4 | 0 | Submit ticket form, FAQ expand/collapse |
| Admin Users | 0 | 0 | 0 | — | Page tests reference "admin" generically |
| Admin Tickets | 3 | 0 | 0 | 0 | Respond form, bug approve/reject, filter tabs |
| Login | 2 | 7 | 15 | Yes | Only page with form fill tests |
| Register | 9 | 14 | 5 | 0 | No form fill, no security question select |
| Forgot Password | 5 | 0 | 0 | 0 | 3-step flow completely untested |
| AI Search | 1 | 0 | 0 | 0 | Skipped — redesign pending |
| Signals | 9 | 25 | 2 | 0 | Create/edit signal forms |

## CRUD Coverage

| Entity | Create | Read | Update | Delete | Validation |
|--------|--------|------|--------|--------|-----------|
| Stock | No | Yes | No | No | Yes |
| Holding | No | Yes | No | No | No |
| Transaction | No | Yes | No | No | Yes |
| MF Fund | No | Yes | No | No | No |
| MF Holding | No | Yes | No | No | No |
| MF Transaction | No | Yes | No | No | No |
| Support Ticket | Yes | Yes | No | No | No |
| FAQ | Yes | No | No | No | No |
| Bug Report | No | No | No | No | No |
| Signal | No | Yes | No | No | No |

## User Flow Coverage

| Flow | Tested | Gap |
|------|--------|-----|
| Login → Dashboard | Yes | — |
| Logout → redirect | Yes | — |
| Register → OTP → Login | API only | No UI flow test |
| Forgot Password 3-step | Not tested | Critical gap |
| Change password → Re-login | API only | No UI flow |
| Profile update → Header reflects | Not tested | |
| Groww config → Sync | Not tested | |
| Submit ticket → AI response | Not tested | |
| Admin approve bug → estimation | Not tested | |
| Setup-admin API | Not tested | New endpoint |

## Form Validation Coverage

| Form | Happy Path | Required Missing | Invalid Format | Error Display |
|------|-----------|-----------------|----------------|--------------|
| Login | Yes (API) | No | No | No |
| Register | Partial (API) | Yes (API) | Partial | No |
| Forgot Password | No | No | No | No |
| Add Holding | No | No | No | No |
| Edit Holding | No | No | No | No |
| Add Transaction | No | No | No | No |
| Add Stock | No | No | No | No |
| Add MF Fund | No | No | No | No |
| Add MF Transaction | No | No | No | No |
| Change Password | Yes (API) | No | Yes (API) | No |
| Groww Config | No | No | No | No |
| Submit Ticket | Yes (API) | No | No | No |
| Create Signal | No | No | No | No |
| Admin Respond | No | No | No | No |
| Profile Update | Yes (API) | No | No | No |

## Security Coverage

| Check | Tested | Gap |
|-------|--------|-----|
| 401 without token (5 endpoints) | Yes | Expand to all |
| 403 regular user on admin | Yes | — |
| XSS in user input | No | Critical gap |
| SQL injection in search | No | |
| Multi-tenant data isolation | No | |
| Setup-admin idempotency | No | |
| Rate limiting on tickets | No | |

## Priority Test Additions

### Critical (12 tests)

1. Setup-admin: creates user, idempotent, reset password
2. Forgot password API flow (3-step)
3. XSS safe: HTML in ticket subject
4. Multi-tenant: user can't see other user's data
5. Bug report lifecycle: approve → estimate → fix

### High (15 tests)

6. Profile: update name form (UI fill + submit)
7. Profile: change password form (UI fill + submit + re-login)
8. Profile: Groww config form (UI fill + submit)
9. Holdings: add holding form (UI fill + submit)
10. Transactions: add transaction form (UI fill + submit)
11. Stocks: add stock search + select (UI)
12. MF: search fund + add holding (UI)
13. Help: submit ticket form (UI fill + submit)
14. Register: full form fill with security questions (UI)
15. Admin tickets: respond form (UI)

### Medium (10 tests)

16. Holdings: search filter works
17. Holdings: signal chip filter works
18. Holdings: sort by column
19. Stocks: signal/target filter chips
20. Dashboard: Refresh Quotes button click
21. Performance: Capture Snapshot button
22. Help: FAQ accordion expand/collapse
23. Admin tickets: filter tabs (All/Bugs/Inquiries)
24. MF: sort columns
25. Console error check on all pages

### Low (5 tests)

26. Long text in ticket subject (255+ chars)
27. Special characters in stock search
28. Decimal precision in holding quantity
29. Empty state on Holdings page
30. Rate limit on ticket submission

## Audit History

| Date | Tests Before | Tests After | Gaps Found | Gaps Fixed |
|------|-------------|-------------|------------|------------|
| 2026-06-23 | 127 | — | 42 | Pending |
