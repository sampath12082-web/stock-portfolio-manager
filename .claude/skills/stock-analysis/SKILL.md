---
name: stock-analysis
description: Run and manage stock market technical analysis. Use when asked to analyze stocks, generate trading signals, run SMA/RSI analysis, scan market, add indicators, or debug signals.
---

# Stock Market Technical Analysis

Drives the app's built-in technical analysis engine — SMA crossover, RSI, 52-week position, volume trends. Generates BUY/SELL/HOLD signals for portfolio stocks and NIFTY watchlist.

## How It Works

```
TechnicalAnalysisServiceImpl.scanMarket()
  → Fetches 3-month OHLC from Yahoo Finance
  → Computes indicators per stock
  → Generates TradingSignal records (source=AUTO)

Priority: Portfolio holdings analyzed first, then 30-stock NIFTY watchlist
```

## Run Analysis

### Via API
```bash
TOKEN=$(curl -s -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"sampath12082@gmail.com","password":"Admin@123"}' \
  | python -c "import json,sys; print(json.load(sys.stdin)['accessToken'])")

# Trigger full market scan
curl -s -X POST http://localhost:8081/api/signals/analyze \
  -H "Authorization: Bearer $TOKEN" | python -m json.tool

# View active signals
curl -s http://localhost:8081/api/signals/active \
  -H "Authorization: Bearer $TOKEN" | python -c "
import json,sys
signals = json.load(sys.stdin)
for s in signals:
    print(f'{s[\"symbol\"]:15s} {s[\"signalType\"]:12s} Target: {s.get(\"targetPrice\",\"—\")}')
print(f'\nTotal: {len(signals)} active signals')
"
```

### Via Scheduled Job
Runs automatically weekdays at 4:00 PM IST (after NSE close):
```
PortfolioScheduler.dailyTechnicalAnalysis() → TechnicalAnalysisService.scanMarket()
```

Signals expire after 7 days (6:00 PM daily cleanup).

## Indicators

### SMA Crossover (20/50 day)
```
SMA20 crosses above SMA50 → BUY_SIGNAL  (Golden Cross)
SMA20 crosses below SMA50 → SELL_SIGNAL (Death Cross)
SMA20 ≈ SMA50             → HOLD
```

### RSI (14 day)
```
RSI < 30  → Oversold  → BUY_SIGNAL
RSI > 70  → Overbought → SELL_SIGNAL
30-70     → Neutral
```

### 52-Week Position
```
Price near 52-week low (within 10%)  → BUY opportunity
Price near 52-week high (within 5%)  → Caution/SELL
```

### Volume Trend
```
5-day avg volume > 20-day avg volume → Confirms signal strength
```

### Signal Priority (combined)
Multiple indicators combine. Priority order for display:
```
SELL_SIGNAL > HOLD > BUY_SIGNAL > WATCH
```

## NIFTY Watchlist (30 stocks)

Hardcoded in `TechnicalAnalysisServiceImpl`. Scanned for BUY candidates even if not in portfolio:

```
RELIANCE, TCS, HDFCBANK, INFY, ICICIBANK, HINDUNILVR, SBIN, BHARTIARTL,
KOTAKBANK, ITC, BAJFINANCE, LT, ASIANPAINT, AXISBANK, MARUTI, SUNPHARMA,
TITAN, ULTRACEMCO, NESTLEIND, WIPRO, POWERGRID, NTPC, ONGC, JSWSTEEL,
TATAMOTORS, ADANIENT, TECHM, HCLTECH, DIVISLAB, BAJAJFINSV
```

## Key Files

| File | Purpose |
|------|---------|
| `service/impl/TechnicalAnalysisServiceImpl.java` | Main engine — `scanMarket()`, `analyzeStock()` |
| `integration/yahoo/YahooFinanceProvider.java` | `fetchHistoricalData()` — 3-month OHLC |
| `integration/yahoo/HistoricalQuote.java` | OHLC data record |
| `entity/TradingSignal.java` | Signal entity (symbol, type, target, source, status) |
| `repository/TradingSignalRepository.java` | Query signals |
| `controller/TradingSignalController.java` | `/api/signals/*` endpoints |
| `scheduler/PortfolioScheduler.java` | 4PM auto-analysis, 6PM expiry |

## Signal Data Model

```
TradingSignal
├── symbol          # Stock symbol (e.g., RELIANCE)
├── signalType      # BUY_SIGNAL, SELL_SIGNAL, HOLD, WATCH
├── targetPrice     # Suggested target (from SMA/support levels)
├── currentPrice    # Price when signal generated
├── source          # AUTO (from scanMarket) or MANUAL
├── status          # ACTIVE or EXPIRED
├── notes           # Indicator details
├── createdAt       # When generated
└── expiresAt       # Auto-expires after 7 days
```

## Yahoo Finance Integration

```
Symbol mapping: NSE → SYMBOL.NS, BSE → SYMBOL.BO
Endpoint: /v8/finance/chart/{SYMBOL.NS}?range=3mo&interval=1d
Returns: OHLCV data (open, high, low, close, volume per day)
Rate limit: No auth required, but throttle to avoid blocks
```

## Adding a New Indicator

1. Add computation method in `TechnicalAnalysisServiceImpl`:
```java
private SignalType computeMACD(List<HistoricalQuote> data) {
    // 12-day EMA, 26-day EMA, 9-day signal line
    // MACD line crosses above signal → BUY
    // MACD line crosses below signal → SELL
}
```

2. Call it in `analyzeStock()` and combine with existing signals
3. Update `notes` field with indicator details
4. No migration needed — uses existing `TradingSignal` entity

## Troubleshooting

| Issue | Fix |
|-------|-----|
| No signals generated | Run `POST /api/signals/analyze`. Check Yahoo Finance connectivity. |
| All signals show HOLD | Market may be sideways. Check RSI values (30-70 = neutral). |
| Stale signals | Signals expire after 7 days. Run analysis again. |
| Yahoo Finance timeout | API may be rate-limited. Wait and retry. |
| Missing stock in analysis | Stock must be in holdings OR NIFTY watchlist. |
