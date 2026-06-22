---
name: run-stock-portfolio-manager
description: Build, run, and drive the stock portfolio manager app. Use when asked to run, start, test, or verify the app.
---

# Run: Stock Portfolio Manager

Spring Boot 3.5 + React 19 web app on port 8081. JWT auth required for all `/api/*` endpoints.  
Drive via `curl` (API) and Playwright (UI). E2E tests in `e2e/`.

All paths relative to repo root.

## Prerequisites

- Java 21, PostgreSQL, Node.js 18+
- Python 3 with `openpyxl`, `requests` (for data import scripts)
- Playwright chromium: `cd e2e && npx playwright install chromium`

## Build

```bash
cd frontend && npm install && npm run build && cd ..
cd backend && ./mvnw compile -q && cd ..
```

## Database Setup

```bash
psql -U sampat -h localhost -c "DROP DATABASE IF EXISTS myportfolio;" -c "CREATE DATABASE myportfolio;" postgres
```

Flyway V1–V18 apply automatically on startup.

## Run (Agent Path)

### CRITICAL: Backend must be restarted after frontend build

The backend serves static files from its classpath at startup. A frontend `npm run build` 
outputs new files to `backend/src/main/resources/static/`, but the running backend still 
serves the OLD files. **Always restart the backend after any frontend build.**

```bash
# Full build + restart sequence:
cd frontend && npm run build && cd ..
# Stop existing backend:
pkill -f "spring-boot:run" 2>/dev/null || true
# Recompile and start:
cd backend && ./mvnw compile -q && ./mvnw spring-boot:run -q > /dev/null 2>&1 &
# Wait for readiness:
for i in $(seq 1 20); do
  code=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8081/api/auth/login 2>/dev/null)
  [ "$code" != "000" ] && echo "Ready" && break
  sleep 3
done
# Verify build files match:
echo "Served: $(curl -s http://localhost:8081/ | grep -o 'assets/index-[^\"]*\.js')"
echo "Built:  $(ls backend/src/main/resources/static/assets/index-*.js | xargs basename)"
```

### Authenticate (required for all API calls)

```bash
TOKEN=$(curl -s -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"sampath12082@gmail.com","password":"Admin@1234567890*"}' \
  | python -c "import json,sys; print(json.load(sys.stdin)['accessToken'])")
```

### Drive via curl

```bash
# Dashboard
curl -s http://localhost:8081/api/dashboard -H "Authorization: Bearer $TOKEN" | python -m json.tool

# Holdings
curl -s http://localhost:8081/api/holdings -H "Authorization: Bearer $TOKEN" | python -m json.tool

# Profile
curl -s http://localhost:8081/api/profile -H "Authorization: Bearer $TOKEN" | python -m json.tool

# Admin users (ROLE_ADMIN only)
curl -s http://localhost:8081/api/admin/users -H "Authorization: Bearer $TOKEN" | python -m json.tool

# Groww sync
curl -s -X POST http://localhost:8081/api/groww/sync -H "Authorization: Bearer $TOKEN" | python -m json.tool

# Capture performance snapshot
curl -s -X POST http://localhost:8081/api/performance/snapshot -H "Authorization: Bearer $TOKEN" | python -m json.tool
```

### Drive via Playwright (61 tests)

```bash
cd e2e && npx playwright test                    # All tests
cd e2e && npx playwright test tests/smoke.spec.ts       # 9 smoke tests
cd e2e && npx playwright test tests/auth.spec.ts        # 16 auth tests
cd e2e && npx playwright test tests/functional.spec.ts  # 23 functional tests
cd e2e && npx playwright test tests/regression.spec.ts  # 13 regression tests
```

### Import data (after DB rebuild)

```bash
python scripts/import_stock_data.py   # 567 stock transactions
python scripts/import_mf_data.py      # 12 MF holdings, 279 MF transactions
```

Scripts auto-login as admin before importing.

## Run (Human Path)

```bash
cd backend && ./mvnw spring-boot:run
# Open http://localhost:8081
# Login: sampath12082@gmail.com / Admin@1234567890*
```

For frontend dev server with hot reload:
```bash
cd frontend && npm run dev
# Open http://localhost:3000 (proxies /api to :8081)
```

## Stop

```bash
# PowerShell:
Get-Process -Name java | Stop-Process -Force
# Or bash:
pkill -f "spring-boot:run"
```

## Environment Variables

```bash
export DB_PASSWORD="<postgres-password>"
export GROWW_API_ENABLED=true
export GROWW_ACCESS_TOKEN="<groww-key>"       # daily renewal
export GROWW_API_SECRET="<groww-secret>"
export JWT_SECRET="<base64-key>"
export ADMIN_DEFAULT_PASSWORD="Admin@1234567890*"
export SPRING_MAIL_USERNAME="<email>"
export SPRING_MAIL_PASSWORD="<app-password>"
```

## Gotchas

- **JWT expires in 15min.** Re-run the TOKEN= line if you get 401s.
- **Groww API key expires daily.** Renew at groww.in/trade-api. Without it, Groww sync returns 503 and dashboard shows "Groww offline" for cash/realized P&L.
- **Performance snapshot returns stale data** if captured before Groww sync. Click "Capture Snapshot" again after syncing.
- **Frontend build outputs to `backend/src/main/resources/static/`.** Must rebuild frontend after changes, then restart backend to serve new files.
- **Import scripts require auth.** They auto-login as admin — if admin password changes, update the scripts.
- **`npm run build` from `frontend/` must have backend dir** — vite.config.ts outputs to `../backend/src/main/resources/static/`.

## Troubleshooting

| Symptom | Fix |
|---------|-----|
| Backend won't start — "No sources to compile" | Run `git checkout HEAD -- backend/src/` to restore Java files |
| 401 on all endpoints | Token expired. Re-login to get new token |
| Dashboard blank / all dashes | Groww API offline OR data not imported. Check `/api/groww/status` |
| Holdings page blank (React error #310) | `useState` hooks after early return. Check HoldingsPage.tsx hook order |
| Performance page all zeroes | Stale snapshot. POST `/api/performance/snapshot` to recapture |
| Import script fails with 401 | Scripts need auth. Ensure `login()` is called in `__main__` |
| `npm run build` outputs to wrong dir | Must run from `frontend/` dir. Check vite.config.ts `outDir` |
