---
name: app-critique
description: Critique and score the web app against market standards. Use when asked to evaluate the app, compare with competitors, rate features, find gaps, provide improvement suggestions, or assess market readiness.
---

# App Critique — Market Readiness Review

Evaluates the stock portfolio manager against market-leading Indian brokerage/portfolio apps (Groww, Zerodha Kite, Smallcase, INDMoney, Tickertape, MProfit) and industry standards. Provides a scored assessment with actionable suggestions.

## When to Use

- Before launch or demo
- When asked "how does this compare to Groww/Zerodha?"
- When asked "what's missing?" or "rate this app"
- For investment pitch or portfolio showcase
- Periodic competitive gap analysis

## Evaluation Framework

### Scoring (0-10 per category, 100 total)

| Category | Weight | What to Evaluate |
|----------|--------|-----------------|
| Core Functionality | 15 | Portfolio tracking, P&L, holdings, transactions |
| Data Accuracy | 15 | P&L calculations, live prices, historical data |
| User Experience | 10 | Design, navigation, responsiveness, loading states |
| Security & Auth | 10 | Login, encryption, session management, RBAC |
| Market Data | 10 | Live quotes, charts, technical indicators |
| Mutual Funds | 5 | MF tracking, NAV, transactions |
| Reporting & Analytics | 10 | Dashboards, charts, sector allocation, insights |
| Performance | 5 | Page load time, API response time, caching |
| Mobile & Responsive | 5 | Works on phone/tablet, touch-friendly |
| Production Readiness | 15 | Error handling, logging, monitoring, deployment, testing |

### How to Score Each Category

#### 1. Core Functionality (15 pts)

**Check:**
```bash
TOKEN=$(curl -s -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"sampath12082@gmail.com","password":"Admin@123"}' \
  | python -c "import json,sys; print(json.load(sys.stdin)['accessToken'])")

# Portfolio features
curl -s http://localhost:8081/api/dashboard -H "Authorization: Bearer $TOKEN" | python -m json.tool
curl -s http://localhost:8081/api/holdings -H "Authorization: Bearer $TOKEN" | python -c "import json,sys; print(f'Holdings: {len(json.load(sys.stdin))}')"
curl -s http://localhost:8081/api/transactions -H "Authorization: Bearer $TOKEN" | python -c "import json,sys; print(f'Transactions: {len(json.load(sys.stdin))}')"
```

**Compare with market:**
| Feature | Groww | Zerodha | This App | Gap |
|---------|-------|---------|----------|-----|
| Stock holdings with live P&L | Yes | Yes | ? | |
| Transaction history | Yes | Yes | ? | |
| Buy/sell from app | Yes | Yes | No (view only) | Critical |
| Watchlist | Yes | Yes | ? | |
| Multi-account | No | No | ? | |
| PDF/CSV export | Yes | Yes | ? | |
| Tax reports (STCG/LTCG) | Yes | Yes | ? | |
| Dividend tracking | Yes | Yes | ? | |

#### 2. Data Accuracy (15 pts)

```bash
# Run the number validation
cd e2e && npx playwright test tests/regression.spec.ts 2>&1 | tail -3

# Check P&L formula correctness
curl -s http://localhost:8081/api/dashboard -H "Authorization: Bearer $TOKEN" | python -c "
import json,sys; d=json.load(sys.stdin)
print(f'Unrealized = Current - Invested: {d[\"currentValue\"] - d[\"investedAmount\"]:,.2f} vs {d[\"unrealizedPnL\"]:,.2f}')
print(f'Match: {abs((d[\"currentValue\"] - d[\"investedAmount\"]) - d[\"unrealizedPnL\"]) < 1}')
"
```

**Compare with market:**
| Feature | Market Standard | This App | Gap |
|---------|----------------|----------|-----|
| Real-time quotes | < 1 sec delay | 5-min cache | Stale data |
| FIFO/weighted avg cost | Both options | Groww avg only | Limited |
| Corporate actions (split/bonus) | Auto-adjusted | Manual | Missing |
| Historical P&L | Multi-year | Current period only | Limited |
| Intraday vs delivery P&L | Separate | Separate (CNC/MIS) | OK |

#### 3. User Experience (10 pts)

**Check manually or via screenshots:**
- Login flow: clean? error messages helpful?
- Dashboard: information density, readability
- Navigation: sidebar consistent, breadcrumbs?
- Loading states: spinners or blank pages?
- Empty states: helpful messages or just blank?
- Color coding: consistent green=profit, red=loss?
- Typography: readable font sizes, hierarchy?
- Dark mode: available?

**Compare with market:**
| Feature | Market Standard | This App | Gap |
|---------|----------------|----------|-----|
| Dark mode | All major apps | No | Missing |
| Onboarding tutorial | Most apps | No | Missing |
| Toast notifications | Standard | No | Missing |
| Search everywhere (Cmd+K) | Modern apps | Per-page only | Limited |
| Pagination for large lists | Standard | Full list | Performance risk |
| Breadcrumbs | Common | No | Minor |

#### 4. Security & Auth (10 pts)

```bash
# Test auth
curl -s -o /dev/null -w "%{http_code}" http://localhost:8081/api/stocks  # Should be 401
curl -s -o /dev/null -w "%{http_code}" -H "Authorization: Bearer invalid" http://localhost:8081/api/stocks  # 401

# Check for secrets in code
grep -rn --include="*.java" --include="*.ts" --include="*.yml" -iE "password\s*=\s*[\"'][^$]" backend/src/ frontend/src/ | grep -v test | grep -v ".class" | wc -l
```

**Compare with market:**
| Feature | Market Standard | This App | Gap |
|---------|----------------|----------|-----|
| 2FA (TOTP/SMS) | All brokers | No | Critical for finance |
| Session timeout UI | Standard | JWT expiry only | No warning |
| Login attempt limiting | Standard | No | Missing |
| Password strength meter | Common | No (backend validation only) | Missing |
| Audit log | Required for finance | No | Missing |
| HTTPS | Required | HTTP only (dev) | Needs TLS in prod |
| CORS restrictive | Required | All origins allowed | Must fix for prod |

#### 5. Market Data (10 pts)

```bash
curl -s http://localhost:8081/api/quotes -H "Authorization: Bearer $TOKEN" | python -c "
import json,sys; q=json.load(sys.stdin); print(f'Quotes cached: {len(q)} stocks')
"
curl -s http://localhost:8081/api/signals/active -H "Authorization: Bearer $TOKEN" | python -c "
import json,sys; s=json.load(sys.stdin); print(f'Signals: {len(s)} active')
"
```

**Compare with market:**
| Feature | Market Standard | This App | Gap |
|---------|----------------|----------|-----|
| Live streaming quotes | WebSocket/SSE | 5-min polling | Stale |
| Candlestick charts | Interactive | No | Missing |
| Order book / depth | Real-time | No | Missing |
| News feed | Integrated | No | Missing |
| Screener/filter | Advanced | Basic search | Limited |
| Alerts/price targets | Push notifications | Signal badges only | Limited |
| IPO tracking | Most apps | No | Missing |

#### 6. Mutual Funds (5 pts)

```bash
curl -s http://localhost:8081/api/mf/holdings -H "Authorization: Bearer $TOKEN" | python -c "
import json,sys; h=json.load(sys.stdin); print(f'MF Holdings: {len(h)}')
"
```

**Compare with market:**
| Feature | Market Standard | This App | Gap |
|---------|----------------|----------|-----|
| SIP tracking | All MF apps | No | Missing |
| Fund comparison | Standard | No | Missing |
| Category-wise breakdown | Standard | Basic | Limited |
| XIRR calculation | Standard | No | Missing |
| Fund factsheet | Standard | No | Missing |
| SIP calculator | Common | No | Missing |

#### 7. Reporting & Analytics (10 pts)

**Compare with market:**
| Feature | Market Standard | This App | Gap |
|---------|----------------|----------|-----|
| PDF/Excel export | Standard | No | Missing |
| Tax computation | P&L statement | No | Missing |
| Custom date range P&L | Standard | Current only | Limited |
| Asset allocation (equity/debt/gold) | Standard | Sector only | Limited |
| Goal tracking | INDMoney, Smallcase | No | Missing |
| Benchmark comparison (vs Nifty) | Tickertape, MProfit | No | Missing |

#### 8. Performance (5 pts)

```bash
# API response times
for ep in dashboard holdings stocks transactions; do
  time=$(curl -s -o /dev/null -w "%{time_total}" http://localhost:8081/api/$ep -H "Authorization: Bearer $TOKEN")
  echo "  /api/$ep: ${time}s"
done
```

**Benchmarks:**
| Metric | Good | Acceptable | Poor |
|--------|------|-----------|------|
| API response | < 200ms | < 1s | > 2s |
| Page load (FCP) | < 1s | < 3s | > 5s |
| Quote refresh | Real-time | < 1min | > 5min |

#### 9. Mobile & Responsive (5 pts)

**Check:** Resize browser to 375px width. Are tables scrollable? Is sidebar collapsible? Are buttons tappable?

**Compare with market:**
| Feature | Market Standard | This App | Gap |
|---------|----------------|----------|-----|
| Native mobile app | All major | No | Critical for adoption |
| PWA / installable | Some apps | No | Missing |
| Touch-friendly tables | Standard | Horizontal scroll | Adequate |
| Mobile-first design | Standard | Desktop-first | Partial |

#### 10. Production Readiness (15 pts)

```bash
# Tests
cd e2e && npx playwright test 2>&1 | tail -3

# Error handling
grep -rn "GlobalExceptionHandler\|@RestControllerAdvice" backend/src/ | wc -l

# Logging
grep -rn "LoggerFactory\|log\." backend/src/main/java/ | wc -l

# CI/CD
ls .github/workflows/ 2>/dev/null || echo "No CI/CD"
```

**Compare with market:**
| Feature | Market Standard | This App | Gap |
|---------|----------------|----------|-----|
| E2E tests | Required | 61 tests ✓ | Good |
| CI/CD pipeline | Required | No | Missing |
| Docker deployment | Standard | No | Missing |
| Health check endpoint | Standard | Exists ✓ | OK |
| Rate limiting | Required | No | Missing |
| Monitoring (APM) | Required | No | Missing |
| Database backups | Required | No | Missing |
| Error tracking (Sentry) | Standard | No | Missing |
| SSL/TLS | Required | HTTP only | Must fix |
| Environment-based config | Required | Env vars ✓ | OK |

## Report Template

```
# App Critique Report — Stock Portfolio Manager
## Date: YYYY-MM-DD

## Overall Score: XX/100

| Category | Score | Max | Notes |
|----------|-------|-----|-------|
| Core Functionality | X | 15 | |
| Data Accuracy | X | 15 | |
| User Experience | X | 10 | |
| Security & Auth | X | 10 | |
| Market Data | X | 10 | |
| Mutual Funds | X | 5 | |
| Reporting & Analytics | X | 10 | |
| Performance | X | 5 | |
| Mobile & Responsive | X | 5 | |
| Production Readiness | X | 15 | |

## Strengths
1. ...
2. ...

## Critical Gaps (vs market)
1. ...
2. ...

## Top 10 Improvements for Market Readiness
1. ...

## Competitive Position
- vs Groww: ...
- vs Zerodha Kite: ...
- vs MProfit: ...

## Target User
Who would use this over existing apps and why?

## Verdict
Is this demo-ready? MVP-ready? Production-ready?
```

## Score Interpretation

| Score | Rating | Meaning |
|-------|--------|---------|
| 80-100 | Excellent | Production-ready, competitive |
| 60-79 | Good | MVP-ready, needs polish |
| 40-59 | Fair | Demo-ready, significant gaps |
| 20-39 | Early | Prototype, core features only |
| 0-19 | Concept | Framework with minimal features |
