---
name: test-coverage-audit
description: Deep test coverage audit — crawls every page, section, form, button, and API endpoint to find untested functionality. Use when asked to check test coverage, find untested features, verify all functionality has tests, or identify testing gaps.
---

# Test Coverage Audit

Deep-crawls the entire application — every page, section, form element, API endpoint, CRUD operation, error path, and user flow — to find untested functionality and generate missing tests.

## When to Use

- After implementing new features
- Before a release
- When asked "are all features tested?" or "what's untested?"
- Periodic quality check
- When quality issues are found that tests should have caught

## Audit Process

### Phase 1: Deep Inventory

#### 1a. Crawl Every Frontend Page — Sections, Forms, Buttons

For EACH page in `frontend/src/pages/*.tsx`, extract:

```bash
echo "=== DEEP PAGE CRAWL ==="
for f in frontend/src/pages/*.tsx; do
  page=$(basename $f .tsx)
  echo ""
  echo "--- $page ---"
  
  # Sections (h1, h2, h3, Card titles)
  echo "  Sections:"
  grep -oP "title=['\"]([^'\"]*)['\"]|<h[1-3][^>]*>([^<]*)<|font-bold[^>]*>([^<]*)<" "$f" | head -10
  
  # Form inputs (text, password, email, number, select, textarea)
  echo "  Form elements:"
  grep -c '<input' "$f" && echo "    inputs"
  grep -c '<select' "$f" && echo "    selects"
  grep -c '<textarea' "$f" && echo "    textareas"
  
  # Buttons with actions
  echo "  Buttons/Actions:"
  grep -oP 'onClick=\{[^}]*\}|type="submit"|onSubmit' "$f" | head -10
  
  # API calls (client.get/post/put/delete)
  echo "  API calls:"
  grep -oP "client\.(get|post|put|delete)\(['\"][^'\"]*['\"]" "$f" | head -10
  
  # State mutations (useState, useMutation)
  echo "  State: $(grep -c 'useState' "$f") states, $(grep -c 'useMutation\|\.mutate' "$f") mutations"
  
  # Conditional rendering (loading, error, empty states)
  echo "  States: loading=$(grep -c 'isLoading\|Loading' "$f"), error=$(grep -c 'error\|Error' "$f"), empty=$(grep -c 'EmptyState\|No.*yet\|length === 0' "$f")"
done
```

#### 1b. Crawl Every API Endpoint — Request/Response Fields

```bash
echo "=== API ENDPOINT INVENTORY ==="
for ctrl in backend/src/main/java/com/stocks/myportfolio/controller/*.java; do
  name=$(basename $ctrl .java)
  base=$(grep '@RequestMapping' "$ctrl" | grep -oP '"[^"]*"' | tr -d '"')
  echo ""
  echo "--- $name (base: $base) ---"
  
  # Each endpoint: method, path, request body, response
  grep -B1 -A3 '@\(Get\|Post\|Put\|Delete\)Mapping' "$ctrl" | head -30
done
```

#### 1c. Inventory Existing Tests — What's Actually Tested

```bash
echo "=== TEST INVENTORY ==="
for f in e2e/tests/*.spec.ts; do
  echo ""
  echo "--- $(basename $f) ---"
  
  # Test names
  grep "test(" "$f" | sed "s/.*test(['\"]//;s/['\"].*//"
  
  # API endpoints hit
  echo "  Endpoints:"
  grep -oP "'/api/[^']*'" "$f" | tr -d "'" | sort -u
  
  # UI pages navigated
  echo "  Pages visited:"
  grep -oP "goto\('[^']*'\)|click\([^)]*\)" "$f" | head -10
  
  # Form interactions (fill, click submit)
  echo "  Form interactions:"
  grep -c "page.fill\|page.click.*submit\|page.selectOption" "$f"
  
  # Assertions count
  echo "  Assertions: $(grep -c 'expect(' "$f")"
done
```

### Phase 2: Gap Analysis — Feature × Test Matrix

For every feature found in Phase 1, check if a test exists. Build a matrix:

#### 2a. Page-Level Coverage

For each page, check:
- [ ] Page loads without errors (navigation test)
- [ ] All sections render (headings visible)
- [ ] Table data loads (if has table)
- [ ] Form submission works (if has form)
- [ ] Form validation works (required fields, invalid input)
- [ ] Empty state renders (when no data)
- [ ] Loading state renders (spinner)
- [ ] Error state handled (API failure)
- [ ] No console errors

#### 2b. Form-Level Coverage

For each form (login, register, profile, add holding, add transaction, etc.):
- [ ] Happy path: valid data → success
- [ ] Required field missing → validation error
- [ ] Invalid format (email, password policy) → error message
- [ ] Duplicate data → handled gracefully
- [ ] Submit button disabled during loading
- [ ] Success message/redirect after submit
- [ ] Form clears after successful submit

#### 2c. CRUD Operation Coverage

For each entity (stocks, holdings, transactions, MF, signals, tickets):
- [ ] CREATE: API returns 201, data persists
- [ ] READ: API returns list with expected fields
- [ ] UPDATE: API returns 200, data changes
- [ ] DELETE: API returns 200, data removed
- [ ] Validation: missing required fields → 400
- [ ] Not found: invalid ID → 404
- [ ] Auth: unauthenticated → 401
- [ ] RBAC: regular user on admin endpoint → 403

#### 2d. User Flow Coverage

End-to-end flows that cross multiple pages:
- [ ] Register → verify OTP → login → dashboard
- [ ] Login → navigate all pages → logout → redirected to login
- [ ] Submit ticket → AI classifies → admin reviews → responds
- [ ] Add stock → create holding → record transaction → verify dashboard P&L
- [ ] Forgot password → security questions → OTP → reset → login with new password
- [ ] Admin: list users → reset password → user logs in with temp password
- [ ] Profile: update name → verify header updates
- [ ] Profile: change password → logout → login with new password
- [ ] Profile: save Groww config → sync holdings

#### 2e. Data Consistency Coverage

Mathematical/logical validations:
- [ ] Dashboard invested = sum of active holdings invested
- [ ] Dashboard currentValue = sum of active holdings current value
- [ ] unrealizedPnL = currentValue - investedAmount
- [ ] totalDeposited = sum of DEPOSIT transactions
- [ ] Transaction analytics counts match actual transaction count
- [ ] Performance snapshot matches dashboard investment
- [ ] Sector allocation sums to ~100%
- [ ] Holdings total row matches sum of individual rows
- [ ] MF total row matches sum of individual rows

#### 2f. Security Coverage

- [ ] All /api/* endpoints return 401 without token
- [ ] All /api/admin/* endpoints return 403 for regular user
- [ ] JWT expired token → 401
- [ ] Passwords encrypted in network (RSA)
- [ ] Groww credentials encrypted in network (RSA)
- [ ] Rate limiting on ticket submission
- [ ] Multi-tenant: user A cannot see user B's data
- [ ] SQL injection safe (special chars in search)
- [ ] XSS safe (HTML in ticket subject/message)

#### 2g. Edge Case Coverage

- [ ] Empty database (no stocks, no holdings) → pages don't crash
- [ ] Very long text in fields (255+ chars)
- [ ] Special characters in search queries
- [ ] Decimal precision in currency fields
- [ ] Date boundary (end of month, year)
- [ ] Concurrent form submissions
- [ ] Network timeout handling
- [ ] Groww API offline → graceful degradation

### Phase 3: Generate Missing Test Specifications

For each gap found, generate a specific test specification:

```
GAP: [page/feature] → [what's untested]
TEST: [test name]
TYPE: API | UI | E2E Flow
SUITE: smoke | auth | functional | regression | ui-rendering
PRIORITY: Critical | High | Medium | Low
STEPS:
  1. [setup]
  2. [action]
  3. [assertion]
```

Group by priority:
- **Critical**: Auth flows, data integrity, security
- **High**: CRUD operations, form validations
- **Medium**: UI rendering, empty/error states
- **Low**: Edge cases, accessibility

### Phase 4: Implement Missing Tests

After generating specs, implement them in the appropriate test file:

- `smoke.spec.ts` — App loads, health check, basic navigation
- `auth.spec.ts` — Login, register, password, JWT, RBAC
- `functional.spec.ts` — API CRUD, field validation, business logic
- `regression.spec.ts` — Data consistency, calculation accuracy
- `ui-rendering.spec.ts` — Page rendering, sections, forms, navigation, console errors

### Phase 5: Run and Verify

```bash
cd e2e && npx playwright test --reporter=line 2>&1 | tail -10
```

## Report Template

```
# Test Coverage Audit Report

## Summary
| Metric | Count |
|--------|-------|
| Frontend pages | X |
| Form elements across app | X |
| API endpoints | X |
| CRUD operations | X |
| Existing tests | X |
| Gaps found | X |
| Tests to add | X |
| Coverage before | X% |
| Coverage after | X% |

## Page-by-Page Coverage

### Dashboard (/)
| Section/Feature | API Test | UI Test | Form Test | Gap |
|----------------|---------|---------|-----------|-----|
| Total Funds section | Yes | Yes | — | — |
| Portfolio P&L | Yes | Yes | — | — |
| Sector allocation chart | Yes | No | — | Add UI check |
| Refresh Quotes button | No | No | No | Add click test |
| Today's Orders toggle | No | No | No | Add toggle test |

### Holdings (/holdings)
| Section/Feature | API Test | UI Test | Form Test | Gap |
|----------------|---------|---------|-----------|-----|
| Holdings table | Yes | Yes | — | — |
| Add Holding form | No | No | No | Add full CRUD |
| Edit Holding form | No | No | No | Add form test |
| Search filter | No | No | No | Add filter test |
| Signal filter chips | No | No | No | Add chip test |
| Sort columns | No | No | No | Add sort test |
| Total row | No | No | — | Add validation |
| Groww sync button | No | No | — | Add sync test |

[... repeat for ALL pages ...]

## CRUD Coverage

| Entity | Create | Read | Update | Delete | Validation |
|--------|--------|------|--------|--------|-----------|
| Stock | No | Yes | No | No | Yes |
| Holding | No | Yes | No | No | No |
| Transaction | No | Yes | No | No | Yes |
| MF Fund | No | Yes | No | No | No |
| MF Holding | No | Yes | No | No | No |
| Support Ticket | Yes | Yes | No | No | No |
| FAQ | Yes | No | No | No | No |

## User Flow Coverage

| Flow | Tested | Gap |
|------|--------|-----|
| Register → OTP → Login | Partial (API only) | Add UI flow |
| Login → Dashboard → Logout | Yes | — |
| Submit ticket → AI response | No | Add E2E flow |
| Change password → Re-login | Partial | Add UI flow |
| Groww config → Sync | No | Add E2E flow |

## Form Validation Coverage

| Form | Happy Path | Missing Required | Invalid Format | Error Display |
|------|-----------|-----------------|----------------|--------------|
| Login | Yes | No | No | No |
| Register | Partial | Yes | Partial | No |
| Add Holding | No | No | No | No |
| Add Transaction | No | No | No | No |
| Change Password | Yes | No | Yes | No |
| Groww Config | No | No | No | No |
| Submit Ticket | Yes | No | No | No |

## Security Coverage

| Check | Tested | Gap |
|-------|--------|-----|
| 401 without token | Yes (5 endpoints) | Add all endpoints |
| 403 regular user on admin | Yes | — |
| RSA password encryption | No | Add network check |
| Multi-tenant isolation | No | Add cross-user test |
| XSS in user input | No | Add injection test |

## Priority Test Additions

### Critical (implement first)
1. [test spec]

### High
1. [test spec]

### Medium
1. [test spec]

### Low
1. [test spec]
```

## Coverage Standards

| Category | Target | How to Check |
|----------|--------|--------------|
| API endpoints tested | 100% | Every endpoint hit in at least one test |
| UI pages rendered | 100% | Every page navigated, sections verified |
| Forms happy path | 100% | Every form submitted with valid data |
| Forms validation | 80%+ | Required fields, format rules tested |
| CRUD operations | 100% | Create/Read/Update for each entity |
| Auth flows | 100% | Login, register, forgot password, change password |
| Data consistency | Key formulas | P&L, totals, sums match |
| Security | Key paths | 401/403, encryption, multi-tenant |
| Console errors | All pages | No errors on any page load |
| Error states | 80%+ | API failures show user-friendly messages |
| Empty states | 80%+ | Pages with no data don't crash |
