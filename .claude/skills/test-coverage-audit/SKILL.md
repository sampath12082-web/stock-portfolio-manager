---
name: test-coverage-audit
description: Deep test coverage audit — crawls every page, section, form, button, and API endpoint to find untested functionality. Enforces quality rules that prevent shallow tests. Use when asked to check test coverage, find untested features, verify all functionality has tests, or identify testing gaps.
---

# Test Coverage Audit

Deep-crawls the entire application — every page, section, form element, API endpoint, CRUD operation, error path, and user flow — to find untested functionality and generate missing tests that actually catch real bugs.

## Quality Rules (MANDATORY)

These rules exist because shallow tests passed while real bugs shipped. Every test written MUST follow these rules.

### Rule 1: Test the user outcome, not the HTTP status

```
BAD:  expect(resp.status()).toBe(200)  // Status 200 doesn't mean password changed
GOOD: change password → logout → login with new password → reaches dashboard
```

A test that only checks status codes is a **contract test**, not a **feature test**. Every feature test must verify the actual user-visible outcome: data changed, page rendered, error displayed.

### Rule 2: Every form gets a fill+submit+verify test

If a page has `<input>`, `<select>`, or `<textarea>`, there MUST be a test that:
1. Fills the form with valid data
2. Clicks submit
3. Verifies the success message OR the data persisted

Check: `grep -c '<input\|<select\|<textarea' frontend/src/pages/*.tsx` — every page with >0 needs a form test.

### Rule 3: Every mutation gets a verify-after test

- **Create** → read back, confirm it exists
- **Update** → read back, confirm the value changed
- **Delete** → read back, confirm it's gone
- **Change password** → logout → login with new password

```
BAD:  POST /api/holdings → expect 201  // Never verified it saved
GOOD: POST /api/holdings → GET /api/holdings → find the new one → verify fields
```

### Rule 4: Test across page boundaries (E2E flows)

Features that span multiple pages MUST be tested as a single flow:
- Change password → logout → login with new password → dashboard loads
- Register → verify OTP → login → see empty dashboard
- Submit ticket → ticket appears in My Requests with AI response
- Admin responds → user sees response

A change-password test that doesn't re-login is **incomplete**.

### Rule 5: Test with the real encryption path

Never send raw passwords in tests that claim to test the UI flow. Intercept network and verify:

```typescript
const body = request.postDataJSON();
expect(body.password.length).toBeGreaterThan(100);  // RSA-encrypted = 344 chars
expect(body.password).not.toBe(actualPassword);      // Never plaintext
```

If encryption breaks silently, BCrypt hashes the ciphertext → login permanently broken. Tests MUST catch this.

### Rule 6: Simulate real user conditions

```typescript
// Clear all state — simulates fresh browser / server restart
await page.context().clearCookies();
await page.evaluate(() => localStorage.clear());
await page.goto('/login');  // Fresh start
```

Tests that only work with a warm session miss bugs that appear after:
- Server restart (new RSA key pair)
- Token expiry (JWT 15 min)
- Browser refresh (cleared cache)

### Rule 7: Test what goes wrong, not just what works

For every happy path test, ask: "what if the user does it wrong?"

| Happy Path | Error Path Required |
|------------|-------------------|
| Login with correct password | Login with wrong password → error visible |
| Submit ticket | Submit empty ticket → validation error |
| Change password | Wrong current password → error message shown |
| Add holding | Missing required fields → 400 + error displayed |
| Search stocks | SQL injection chars → no crash |

**If there's no error path test, the error handling is untested and likely broken.**

## Audit Process

### Phase 1: Deep Inventory

#### 1a. Crawl Every Frontend Page — Sections, Forms, Buttons

For EACH page in `frontend/src/pages/*.tsx`, extract:

```bash
echo "=== DEEP PAGE CRAWL ==="
for f in frontend/src/pages/*.tsx; do
  page=$(basename $f .tsx)
  inputs=$(grep -c '<input' "$f" 2>/dev/null)
  selects=$(grep -c '<select' "$f" 2>/dev/null)
  textareas=$(grep -c '<textarea' "$f" 2>/dev/null)
  buttons=$(grep -c 'type="submit"\|onClick' "$f" 2>/dev/null)
  apis=$(grep -c 'client\.\(get\|post\|put\|delete\)' "$f" 2>/dev/null)
  mutations=$(grep -c '\.mutate\|useMutation' "$f" 2>/dev/null)
  echo "$page | forms=$((inputs+selects+textareas)) | buttons=$buttons | apis=$apis | mutations=$mutations"
done
```

#### 1b. Crawl Every API Endpoint

```bash
for ctrl in backend/src/main/java/com/stocks/myportfolio/controller/*.java; do
  name=$(basename $ctrl .java)
  endpoints=$(grep -c '@\(Get\|Post\|Put\|Delete\)Mapping' "$ctrl" 2>/dev/null)
  echo "$name: $endpoints endpoints"
done
```

#### 1c. Inventory Existing Tests — Check Quality Not Just Count

For each test, check which rules it follows:

```bash
for f in e2e/tests/*.spec.ts; do
  echo "--- $(basename $f) ---"
  echo "  Tests: $(grep -c 'test(' "$f")"
  echo "  Form fills: $(grep -c 'page\.fill' "$f")"
  echo "  Network intercepts: $(grep -c 'waitForRequest\|postDataJSON' "$f")"
  echo "  Cross-page flows: $(grep -c 'waitForURL.*login\|waitForURL.*\/' "$f")"
  echo "  Error path tests: $(grep -c 'incorrect\|failed\|error\|400\|401\|403' "$f")"
  echo "  State clear: $(grep -c 'clearCookies\|localStorage.clear' "$f")"
  echo "  Verify-after: $(grep -c 'verify\|still works\|should fail\|should work' "$f")"
done
```

### Phase 2: Gap Analysis — Apply Quality Rules

For every feature, check ALL 7 rules:

#### Rule 1 Check: Status-only tests

```bash
echo "=== Tests that ONLY check status (no behavior verification) ==="
grep -n 'expect.*status.*toBe\|toContain.*status' e2e/tests/*.spec.ts | \
  while read line; do
    file=$(echo "$line" | cut -d: -f1)
    linenum=$(echo "$line" | cut -d: -f2)
    # Check if there's a verify-after within 5 lines
    has_verify=$(sed -n "$((linenum+1)),$((linenum+5))p" "$file" | grep -c 'toHaveProperty\|toBe\|toContain\|toEqual')
    [ "$has_verify" -eq 0 ] && echo "  SHALLOW: $line"
  done
```

#### Rule 2 Check: Forms without fill+submit tests

```bash
echo "=== Pages with forms but no fill+submit test ==="
for f in frontend/src/pages/*.tsx; do
  page=$(basename $f .tsx)
  forms=$(grep -c '<input\|<select\|<textarea' "$f" 2>/dev/null)
  [ "$forms" -eq 0 ] && continue
  fills=$(grep -ric "fill.*$page\|$page.*fill" e2e/tests/*.spec.ts 2>/dev/null | \
    python -c "import sys; print(sum(int(l.split(':')[-1]) for l in sys.stdin))")
  [ "$fills" -eq 0 ] && echo "  GAP: $page has $forms form elements, 0 fill tests"
done
```

#### Rule 3 Check: Mutations without verify-after

```bash
echo "=== POST/PUT without verify-after ==="
grep -n 'request\.\(post\|put\)' e2e/tests/functional.spec.ts | while read line; do
  linenum=$(echo "$line" | cut -d: -f2)
  next_lines=$(sed -n "$((linenum+1)),$((linenum+10))p" e2e/tests/functional.spec.ts)
  has_get=$(echo "$next_lines" | grep -c 'request\.get\|toHaveProperty')
  [ "$has_get" -eq 0 ] && echo "  NO VERIFY: $line"
done
```

#### Rule 4 Check: Cross-page flow coverage

```bash
echo "=== E2E flows that cross pages ==="
echo "  Login→Dashboard→Logout: $(grep -c 'waitForURL.*/' e2e/tests/*.spec.ts)"
echo "  Change password→Re-login: $(grep -c 'Changed.*login\|password.*Logout' e2e/tests/*.spec.ts)"
echo "  Register→Verify→Login: $(grep -c 'register.*login\|verify.*login' e2e/tests/*.spec.ts)"
echo "  Submit ticket→See response: $(grep -c 'ticket.*response\|Submit.*AI' e2e/tests/*.spec.ts)"
```

#### Rule 5 Check: Encryption verification

```bash
echo "=== Encryption tests ==="
echo "  Tests checking encrypted payload: $(grep -c 'length.*Greater.*100\|postDataJSON' e2e/tests/*.spec.ts)"
echo "  Tests sending raw passwords: $(grep -c "password.*Admin@\|password.*Test@" e2e/tests/*.spec.ts)"
```

#### Rule 6 Check: Fresh state tests

```bash
echo "=== Tests that clear browser state ==="
echo "  clearCookies: $(grep -c 'clearCookies' e2e/tests/*.spec.ts)"
echo "  localStorage.clear: $(grep -c 'localStorage.clear' e2e/tests/*.spec.ts)"
```

#### Rule 7 Check: Error path coverage

```bash
echo "=== Error path ratio ==="
total=$(grep -c 'test(' e2e/tests/*.spec.ts)
errors=$(grep -c 'incorrect\|failed\|error\|wrong\|invalid\|reject\|400\|401\|403' e2e/tests/*.spec.ts)
echo "  Happy path tests: $((total - errors))"
echo "  Error path tests: $errors"
echo "  Ratio: error paths should be >= 30% of total"
```

### Phase 3: Generate Test Specifications

For each gap, generate a test spec that follows ALL 7 rules:

```
GAP: [what's untested]
RULE VIOLATED: [which rule(s)]
TEST: [test name]
TYPE: API | UI | E2E Flow
PRIORITY: Critical | High | Medium | Low
STEPS:
  1. [setup — including any state clearing]
  2. [action — using real UI flow, not raw API]
  3. [verify outcome — not just status code]
  4. [verify error path — what if wrong input?]
  5. [verify persistence — does it survive refresh/restart?]
```

### Phase 4: Implement Tests

Write tests in the appropriate file:

- `smoke.spec.ts` — App loads, health check, basic navigation
- `auth.spec.ts` — Login, register, password, JWT, RBAC
- `functional.spec.ts` — API CRUD with verify-after, field validation
- `regression.spec.ts` — Data consistency, calculation accuracy, edge cases
- `ui-rendering.spec.ts` — Page rendering, sections, console errors
- `debug-change-password.spec.ts` — Full E2E flows that cross pages

**Before writing any test, ask:**
1. Does this test verify the USER OUTCOME or just an HTTP status?
2. If it's a form, does it FILL and SUBMIT?
3. If it mutates data, does it READ BACK to verify?
4. If it crosses pages, does it test the FULL FLOW?
5. Does it test what happens when the user does it WRONG?

### Phase 5: Run and Verify

```bash
cd e2e && npx playwright test --reporter=line 2>&1 | tail -10
```

### Phase 6: Save Results

Update `docs/TEST-COVERAGE-AUDIT.md` with:
- Date, test count before/after, gaps found/fixed
- Per-rule compliance scores
- Remaining gaps with priority

## Quality Standards

| Category | Target | How to Check |
|----------|--------|--------------|
| User outcome tests | 100% of features | Every feature has a test that verifies the visible result |
| Form fill+submit | 100% of forms | Every form has a test that fills, submits, verifies |
| Mutation verify-after | 100% of create/update | Every POST/PUT followed by GET to confirm |
| Cross-page E2E flows | All critical paths | Change password→re-login, register→login, ticket→response |
| Encryption verification | All password/secret fields | Network intercept confirms >100 char ciphertext |
| Error path coverage | ≥30% of total tests | Wrong password, missing fields, invalid format |
| Fresh state tests | All auth flows | clearCookies + localStorage.clear before login |
| Console error checks | All pages | No JS errors on any page load |

## Anti-Patterns to Flag

| Anti-Pattern | Why It's Bad | Fix |
|-------------|-------------|-----|
| `expect(status).toBe(200)` alone | Doesn't verify anything changed | Add verify-after assertion |
| UI test with zero `page.fill()` | Only checks render, not functionality | Add form interaction |
| Sending raw password in API test | Skips encryption, misses RSA bugs | Use UI flow or verify encryption |
| Test only works in warm session | Misses restart/key-rotation bugs | Clear state before test |
| Happy path only, no error test | Error handling untested, likely broken | Add wrong-input companion test |
| Testing create without read-back | Data might not persist | GET after POST to verify |
| Isolated page tests only | Cross-page bugs invisible | Add E2E flow tests |

## Lessons Learned

These rules come from real bugs that shipped:

1. **Change password returned 200 but password didn't change** — test only checked status, never re-logged in
2. **RSA key rotation broke all logins** — tests used raw passwords, never tested encryption path
3. **Groww credentials lost on restart** — tests saved encrypted value, never verified it could be read back
4. **Error messages not shown to user** — tests checked API 400, never checked if UI displayed the error
5. **Frontend cached stale RSA key** — tests ran in single session, never cleared browser state
