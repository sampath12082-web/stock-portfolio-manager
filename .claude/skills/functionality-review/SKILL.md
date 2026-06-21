---
name: functionality-review
description: Review app functionality against documentation. Use when asked to verify features, compare UI with docs, check if implementation matches spec, or audit what's working vs documented.
---

# Functionality Review

Compares the running application's actual behavior against what is documented in `docs/`. Identifies gaps where documentation promises something the app doesn't deliver, or the app does something undocumented.

## When to Use

- After implementing a batch of features
- Before a release/handoff
- When user says "this page is broken" or "feature X is missing"
- Periodic health check

## Review Process

### Step 1: Start the App and Authenticate

```bash
# Ensure backend is running
curl -s -o /dev/null -w "%{http_code}" http://localhost:8081/api/auth/login

# Get auth token
TOKEN=$(curl -s -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"sampath12082@gmail.com","password":"Admin@123"}' \
  | python -c "import json,sys; print(json.load(sys.stdin)['accessToken'])")
AUTH="-H 'Authorization: Bearer $TOKEN'"
```

### Step 2: Compare Each Page Against docs/features.md

Read `docs/features.md` and verify each documented feature by hitting the API:

#### Dashboard (`/`)
```bash
# Documented: Portfolio summary, Groww Account, Mutual Funds, Sector Allocation,
# Top Holdings, Portfolio Value chart, Trading Signals, Today's Positions, Today's Orders

curl -s http://localhost:8081/api/dashboard -H "Authorization: Bearer $TOKEN" | python -c "
import json,sys; d=json.load(sys.stdin)
fields = ['investedAmount','currentValue','unrealizedPnL','unrealizedPnLPercentage','totalDeposited']
missing = [f for f in fields if f not in d]
print(f'Dashboard fields: {len(d)} present, {len(missing)} missing: {missing}')
"

curl -s http://localhost:8081/api/groww/account -H "Authorization: Bearer $TOKEN" | python -c "
import json,sys
try:
    g=json.load(sys.stdin)
    print(f'Groww: clearCash={g.get(\"clearCash\")}, orders={len(g.get(\"todayOrders\",[]))}')
except: print('Groww: OFFLINE')
"

curl -s http://localhost:8081/api/portfolio/allocation -H "Authorization: Bearer $TOKEN" | python -c "
import json,sys; s=json.load(sys.stdin)
unknown = [x for x in s if x['sector']=='Unknown']
print(f'Sectors: {len(s)} total, {len(unknown)} unknown')
"

curl -s http://localhost:8081/api/mf/holdings -H "Authorization: Bearer $TOKEN" | python -c "
import json,sys; h=json.load(sys.stdin); print(f'MF Holdings: {len(h)}')
"
```

#### Holdings (`/holdings`)
```bash
# Documented: Search, signal filters, sortable columns, color coding,
# Columns: Stock, Qty, Avg Price, LTP, Target, Invested, Current Value,
# Realized P&L, Unrealized P&L, Signal

curl -s http://localhost:8081/api/holdings -H "Authorization: Bearer $TOKEN" | python -c "
import json,sys; h=json.load(sys.stdin)
active=[x for x in h if x['quantity']>0]
print(f'Holdings: {len(h)} total, {len(active)} active')
fields = ['symbol','quantity','averageBuyPrice','investedAmount','currentPrice','currentValue','pnl']
if h: missing=[f for f in fields if f not in h[0]]; print(f'Missing fields: {missing}')
"
```

#### Transactions (`/transactions`)
```bash
# Documented: Analytics (Fund Flow, Delivery CNC, Intraday MIS, Activity),
# Monthly chart, filters, CNC/MIS badges, sortable columns

curl -s http://localhost:8081/api/transactions/analytics -H "Authorization: Bearer $TOKEN" | python -c "
import json,sys; a=json.load(sys.stdin)
required = ['totalBuyAmount','totalSellAmount','intradayPnL','deliveryBuyAmount','intradayCount','deliveryCount']
missing = [f for f in required if f not in a]
print(f'Analytics fields: {len(a)} present, missing: {missing}')
"
```

#### Stocks (`/stocks`)
```bash
# Documented: Signal/Target filter chips, sortable, My Qty, Avg Price,
# Current Price, Target, Signal columns

curl -s http://localhost:8081/api/stocks -H "Authorization: Bearer $TOKEN" | python -c "
import json,sys; s=json.load(sys.stdin); print(f'Stocks: {len(s)}')
"

curl -s http://localhost:8081/api/signals/active -H "Authorization: Bearer $TOKEN" | python -c "
import json,sys; s=json.load(sys.stdin); print(f'Active signals: {len(s)}')
"
```

#### Mutual Funds (`/mutual-funds`)
```bash
# Documented: Holdings table (Avg NAV, Current NAV, Invested, Current Value),
# Transactions, color coding (red/green by NAV comparison)

curl -s http://localhost:8081/api/mf/holdings -H "Authorization: Bearer $TOKEN" | python -c "
import json,sys; h=json.load(sys.stdin)
if h: 
    fields=['schemeName','averageNav','currentNav','investedAmount','currentValue','pnl']
    missing=[f for f in fields if f not in h[0]]
    print(f'MF Holdings: {len(h)}, missing fields: {missing}')
"
```

#### Performance (`/performance`)
```bash
# Documented: 7D default, snapshot summary, chart, history table

curl -s http://localhost:8081/api/performance/today -H "Authorization: Bearer $TOKEN" | python -c "
import json,sys; s=json.load(sys.stdin)
print(f'Snapshot: invested={s.get(\"totalInvestment\",0):,.0f} holdings={s.get(\"holdingCount\",0)}')
zero = s.get('totalInvestment',0) == 0
if zero: print('WARNING: Snapshot shows zero — needs recapture')
"
```

### Step 3: Compare API Endpoints Against docs/api-reference.md

```bash
# List all actual controller endpoints
grep -rn "@RequestMapping\|@GetMapping\|@PostMapping\|@PutMapping\|@DeleteMapping" \
  backend/src/main/java/com/stocks/myportfolio/controller/ | \
  grep -v ".class" | sed 's/.*"\(.*\)".*/\1/' | sort

# Compare with documented endpoints
grep "| .* | \`/api/" docs/api-reference.md | sed 's/.*`\(\/api\/[^`]*\)`.*/\1/' | sort

# Diff
echo "=== In code but not in docs ==="
comm -23 <(grep -rn "@.*Mapping" backend/src/main/java/com/stocks/myportfolio/controller/ | grep -oP '"/api/[^"]*"' | tr -d '"' | sort -u) \
         <(grep -oP '`/api/[^`]*`' docs/api-reference.md | tr -d '`' | sort -u) 2>/dev/null

echo "=== In docs but not in code ==="
comm -13 <(grep -rn "@.*Mapping" backend/src/main/java/com/stocks/myportfolio/controller/ | grep -oP '"/api/[^"]*"' | tr -d '"' | sort -u) \
         <(grep -oP '`/api/[^`]*`' docs/api-reference.md | tr -d '`' | sort -u) 2>/dev/null
```

### Step 4: Compare Auth Against docs/user-module.md

```bash
# Documented features vs actual
echo "=== Auth Endpoints ===" 
for ep in register verify-otp login forgot-password reset-password change-password refresh; do
  code=$(curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8081/api/auth/$ep -H "Content-Type: application/json" -d '{}')
  echo "  /api/auth/$ep → $code"
done

echo "=== Admin Endpoints ==="
curl -s -o /dev/null -w "  /api/admin/users → %{http_code}\n" http://localhost:8081/api/admin/users -H "Authorization: Bearer $TOKEN"

echo "=== Profile ==="
curl -s -o /dev/null -w "  /api/profile → %{http_code}\n" http://localhost:8081/api/profile -H "Authorization: Bearer $TOKEN"
```

### Step 5: Check Enhancement Status Against docs/ENHANCEMENTS.md

```bash
# Find enhancements marked "Done" but not actually working
grep "Done\|done" docs/ENHANCEMENTS.md | while read line; do
  echo "$line"
done

# Find enhancements marked "Open" that might already be implemented
grep "Open\|open\|Pending\|pending" docs/ENHANCEMENTS.md
```

### Step 6: Run E2E Tests as Functional Verification

```bash
cd e2e && npx playwright test 2>&1 | tail -5
```

## Report Format

```
## Functionality Review Report

### Date: YYYY-MM-DD

### Pages Verified
| Page | Documented | Actual | Status |
|------|-----------|--------|--------|
| Dashboard | 9 sections | 8 working, 1 Groww-dependent | PARTIAL |
| Holdings | All features | All working | PASS |

### API Gaps
| Documented | Status |
|-----------|--------|
| POST /api/auth/public-key | NOT IMPLEMENTED (RSA encryption pending) |

### Documentation Gaps
| Feature in App | Missing From |
|---------------|-------------|
| CNC/MIS badges on transactions | Not in features.md |

### Enhancement Status Mismatches
| # | Docs Say | Actual |
|---|---------|--------|
| 25 | Open | Backend partially done |

### Test Results
- Smoke: X/Y passed
- Auth: X/Y passed
- Functional: X/Y passed
- Regression: X/Y passed

### Recommendations
1. Fix: ...
2. Document: ...
3. Implement: ...
```
