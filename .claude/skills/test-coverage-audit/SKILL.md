---
name: test-coverage-audit
description: Audit test coverage against implemented features. Use when asked to check test coverage, find untested features, verify all functionality has tests, or identify testing gaps.
---

# Test Coverage Audit

Compares every implemented feature, API endpoint, and UI page against the test suite to find untested functionality. Answers: "Is every feature tested? What's missing?"

## When to Use

- After implementing new features
- Before a release
- When asked "are all features tested?" or "what's untested?"
- Periodic quality check

## Audit Process

### Step 1: Inventory All Test Cases

```bash
cd e2e

echo "=== Test Files ==="
ls tests/*.spec.ts

echo -e "\n=== Test Count Per Suite ==="
for f in tests/*.spec.ts; do
  count=$(grep -c "test(" "$f")
  echo "  $(basename $f): $count tests"
done

echo -e "\n=== All Test Names ==="
grep "test(" tests/*.spec.ts | sed 's/.*test(\x27\|.*test("//' | sed "s/['\"].*//"
```

### Step 2: Map Features to Tests

#### API Endpoints Coverage

```bash
# All endpoints in code
grep -rn '@.*Mapping' backend/src/main/java/com/stocks/myportfolio/controller/ | \
  grep -oP '"[^"]*"' | tr -d '"' | sort -u > /tmp/all_endpoints.txt
echo "Total endpoints: $(wc -l < /tmp/all_endpoints.txt)"

# Endpoints tested (in test files)
grep -oP "'/api/[^']*'" e2e/tests/*.spec.ts | sed "s/.*'//" | sed "s/'.*//" | sort -u > /tmp/tested_endpoints.txt
echo "Tested endpoints: $(wc -l < /tmp/tested_endpoints.txt)"

# Untested
echo "=== UNTESTED ENDPOINTS ==="
comm -23 /tmp/all_endpoints.txt /tmp/tested_endpoints.txt
```

#### UI Pages Coverage

```bash
# All pages in router
grep 'path=' frontend/src/App.tsx | grep -oP '"[^"]*"' | tr -d '"' | sort

# Pages tested via browser navigation
grep -oP "goto\('[^']*'\)" e2e/tests/ui-rendering.spec.ts | sort -u
grep -oP "click.*text=[^']*'" e2e/tests/ui-rendering.spec.ts | sort -u

echo "=== Compare ==="
echo "Router pages:"
grep 'path=' frontend/src/App.tsx | grep -oP '"[^"]*"' | tr -d '"' | sort
echo ""
echo "Browser-tested pages:"
grep -oP "goto\('[^']*'\)" e2e/tests/ui-rendering.spec.ts | grep -oP "'[^']*'" | tr -d "'" | sort -u
```

#### Feature Coverage Matrix

For each feature in `docs/features.md`, check if a test exists:

```bash
echo "=== Dashboard Features ==="
# Documented features
grep "^\-" docs/features.md | head -15

# Tests that verify dashboard
grep -c "dashboard\|Dashboard" e2e/tests/*.spec.ts

echo -e "\n=== Auth Features ==="
grep "^\-" docs/features.md | grep -i "login\|register\|password\|OTP\|logout"
grep -c "login\|register\|password\|logout\|auth" e2e/tests/*.spec.ts

echo -e "\n=== Holdings Features ==="
grep "^\-" docs/features.md | grep -i "holding\|search\|filter\|sort\|signal"
grep -c "holding\|Holdings" e2e/tests/*.spec.ts

echo -e "\n=== Help Features ==="
grep "^\-" docs/features.md | grep -i "faq\|ticket\|help"
grep -c "faq\|FAQ\|ticket\|help\|Help" e2e/tests/*.spec.ts
```

### Step 3: Check Test Types Coverage

```bash
echo "=== Test Type Distribution ==="
echo "API tests (request.get/post):"
grep -c "request\.\(get\|post\|put\|delete\)" e2e/tests/*.spec.ts | grep -v ":0"

echo ""
echo "UI browser tests (page.goto/click/fill):"
grep -c "page\.\(goto\|click\|fill\)" e2e/tests/*.spec.ts | grep -v ":0"

echo ""
echo "Data validation tests (expect.*toBe/toEqual):"
grep -c "Math.abs\|toBe\|toEqual\|toContain" e2e/tests/*.spec.ts | grep -v ":0"
```

### Step 4: Identify Gaps

Check for common untested scenarios:

```bash
echo "=== Untested Scenarios ==="

# Error handling tests
echo "Error/negative tests:"
grep -c "400\|401\|403\|404\|409\|error\|fail\|invalid\|wrong" e2e/tests/*.spec.ts

# Edge cases
echo "Edge case tests:"
grep -c "empty\|null\|zero\|negative\|boundary" e2e/tests/*.spec.ts

# CRUD operations
echo "Create/Update/Delete tests:"
grep -c "POST\|PUT\|DELETE\|create\|update\|delete" e2e/tests/*.spec.ts

# Security tests
echo "Security tests:"
grep -c "401\|403\|unauthorized\|forbidden\|admin.*regular\|token" e2e/tests/*.spec.ts
```

### Step 5: Run Tests and Verify

```bash
cd e2e && npx playwright test 2>&1 | tail -5
```

## Report Template

```
# Test Coverage Audit Report

## Summary
- Total features documented: X
- Total test cases: X
- Features with tests: X
- Features WITHOUT tests: X
- Coverage: X%

## Endpoint Coverage
| Tested | Total | Coverage |
|--------|-------|----------|
| X | Y | Z% |

### Untested Endpoints
- POST /api/xxx — no test
- DELETE /api/xxx — no test

## UI Page Coverage
| Page | API Test | UI Test | Status |
|------|---------|---------|--------|
| Dashboard | Yes | Yes | Covered |
| Holdings | Yes | Yes | Covered |
| Help | Yes | No | GAP |

## Feature Coverage
| Feature | Test Exists | Test Type | Gap |
|---------|------------|-----------|-----|
| Login | Yes | API + UI | — |
| FAQ accordion | No | — | Need UI test |
| Admin delete user | No | — | Need API test |

## Missing Test Categories
- [ ] No load/performance tests
- [ ] No accessibility tests
- [ ] No mobile responsive tests
- [ ] No offline/error state tests

## Recommendations
1. Add test for: ...
2. Add test for: ...
```

## Coverage Standards

| Category | Target | Current |
|----------|--------|---------|
| API endpoints tested | 100% | Check |
| UI pages with browser test | 100% | Check |
| Auth flows (login/register/forgot) | 100% | Check |
| Admin operations | 100% | Check |
| Error handling (400/401/403) | Key paths | Check |
| Data consistency | Key calculations | Check |
| Console error checks | All pages | Check |
