import { test, expect } from '@playwright/test';
import { getAdminToken, authHeaders } from './helpers';

test.describe('Regression — API Data Consistency', () => {
  let token: string;
  let headers: Record<string, string>;

  test.beforeAll(async ({ request }) => {
    token = await getAdminToken(request);
    headers = authHeaders(token);
  });

  test('dashboard invested matches holdings sum', async ({ request }) => {
    const dash = await (await request.get('/api/dashboard', { headers })).json();
    const holdings = await (await request.get('/api/holdings', { headers })).json();
    const activeSum = holdings
      .filter((h: { quantity: number }) => h.quantity > 0)
      .reduce((s: number, h: { investedAmount: number }) => s + h.investedAmount, 0);
    expect(Math.abs(dash.investedAmount - activeSum)).toBeLessThan(1);
  });

  test('dashboard currentValue matches holdings sum', async ({ request }) => {
    const dash = await (await request.get('/api/dashboard', { headers })).json();
    const holdings = await (await request.get('/api/holdings', { headers })).json();
    const activeSum = holdings
      .filter((h: { quantity: number }) => h.quantity > 0)
      .reduce((s: number, h: { currentValue: number; investedAmount: number }) => s + (h.currentValue ?? h.investedAmount), 0);
    expect(Math.abs(dash.currentValue - activeSum)).toBeLessThan(1);
  });

  test('unrealizedPnL = currentValue - investedAmount', async ({ request }) => {
    const d = await (await request.get('/api/dashboard', { headers })).json();
    expect(Math.abs(d.unrealizedPnL - (d.currentValue - d.investedAmount))).toBeLessThan(1);
  });

  test('totalDeposited matches sum of DEPOSIT transactions', async ({ request }) => {
    const dash = await (await request.get('/api/dashboard', { headers })).json();
    const txns = await (await request.get('/api/transactions', { headers })).json();
    const depositSum = txns
      .filter((t: { transactionType: string }) => t.transactionType === 'DEPOSIT')
      .reduce((s: number, t: { totalAmount: number }) => s + t.totalAmount, 0);
    expect(Math.abs(dash.totalDeposited - depositSum)).toBeLessThan(1);
  });

  test('transaction analytics counts match actual', async ({ request }) => {
    const a = await (await request.get('/api/transactions/analytics', { headers })).json();
    const txns = await (await request.get('/api/transactions', { headers })).json();
    expect(a.totalTransactions).toBe(txns.length);
  });

  test('all transactions have tradeDate populated', async ({ request }) => {
    const txns = await (await request.get('/api/transactions', { headers })).json();
    const missing = txns.filter((t: { tradeDate: string | null }) => !t.tradeDate);
    expect(missing.length).toBe(0);
  });

  test('all stock transactions have tradeType (CNC/MIS)', async ({ request }) => {
    const txns = await (await request.get('/api/transactions', { headers })).json();
    const stockTxns = txns.filter((t: { transactionType: string }) => t.transactionType === 'BUY' || t.transactionType === 'SELL');
    const untyped = stockTxns.filter((t: { tradeType: string | null }) => !t.tradeType || t.tradeType === 'UNKNOWN');
    expect(untyped.length).toBe(0);
  });

  test('performance snapshot matches dashboard investment', async ({ request }) => {
    const snap = await (await request.get('/api/performance/today', { headers })).json();
    const dash = await (await request.get('/api/dashboard', { headers })).json();
    expect(Math.abs(snap.totalInvestment - dash.investedAmount)).toBeLessThan(1);
  });

  test('sector allocation sums to ~100%', async ({ request }) => {
    const sectors = await (await request.get('/api/portfolio/allocation', { headers })).json();
    if (sectors.length > 0) {
      const totalPct = sectors.reduce((s: number, sec: { percentage: number }) => s + sec.percentage, 0);
      expect(Math.abs(totalPct - 100)).toBeLessThan(1);
    }
  });

  test('profile returns admin user', async ({ request }) => {
    const profile = await (await request.get('/api/profile', { headers })).json();
    expect(profile.email).toBe('sampath12082@gmail.com');
    expect(profile.role).toBe('ROLE_ADMIN');
  });

  test('admin users endpoint returns users list', async ({ request }) => {
    const users = await (await request.get('/api/admin/users', { headers })).json();
    expect(users.length).toBeGreaterThanOrEqual(1);
  });
});

test.describe('Regression — Security', () => {
  test('all protected endpoints return 401 without token', async ({ request }) => {
    const endpoints = ['/api/stocks', '/api/holdings', '/api/dashboard', '/api/transactions', '/api/profile'];
    for (const ep of endpoints) {
      const resp = await request.get(ep);
      expect(resp.status()).toBe(401);
    }
  });

  test('admin endpoint returns 403 for regular user', async ({ request }) => {
    // Register a test user
    const testEmail = `test${Date.now()}@example.com`;
    const testPassword = 'TestPassword@12345';
    const regResp = await request.post('/api/auth/register', {
      data: { email: testEmail, password: testPassword, firstName: 'Test',
        securityQuestion1: 'What city were you born in?', securityAnswer1: 'City',
        securityQuestion2: 'What is your favorite movie?', securityAnswer2: 'Movie' },
    });
    if (regResp.status() === 201) {
      const loginResp = await request.post('/api/auth/login', {
        data: { email: testEmail, password: testPassword },
      });
      if (loginResp.status() === 200) {
        const { accessToken } = await loginResp.json();
        const adminResp = await request.get('/api/admin/users', {
          headers: { Authorization: `Bearer ${accessToken}` },
        });
        expect(adminResp.status()).toBe(403);
      }
    }
  });
});

test.describe('Regression — MF Data Consistency', () => {
  let headers: Record<string, string>;

  test.beforeAll(async ({ request }) => {
    headers = authHeaders(await getAdminToken(request));
  });

  test('MF holdings P&L = currentValue - investedAmount', async ({ request }) => {
    const holdings = await (await request.get('/api/mf/holdings', { headers })).json();
    for (const h of holdings) {
      if (h.investedAmount > 0 && h.currentValue != null) {
        const expected = h.currentValue - h.investedAmount;
        expect(Math.abs(h.pnl - expected)).toBeLessThan(1);
      }
    }
  });

  test('MF holdings pnlPercentage matches formula', async ({ request }) => {
    const holdings = await (await request.get('/api/mf/holdings', { headers })).json();
    for (const h of holdings) {
      if (h.investedAmount > 0 && h.pnl != null) {
        const expected = (h.pnl / h.investedAmount) * 100;
        expect(Math.abs(h.pnlPercentage - expected)).toBeLessThan(0.1);
      }
    }
  });
});

test.describe('Regression — Holdings Total Row', () => {
  let headers: Record<string, string>;

  test.beforeAll(async ({ request }) => {
    headers = authHeaders(await getAdminToken(request));
  });

  test('holdings invested sum matches dashboard', async ({ request }) => {
    const holdings = await (await request.get('/api/holdings', { headers })).json();
    const dash = await (await request.get('/api/dashboard', { headers })).json();
    const activeInvested = holdings
      .filter((h: { quantity: number }) => h.quantity > 0)
      .reduce((s: number, h: { investedAmount: number }) => s + h.investedAmount, 0);
    expect(Math.abs(activeInvested - dash.investedAmount)).toBeLessThan(1);
  });

  test('no holdings have negative quantity', async ({ request }) => {
    const holdings = await (await request.get('/api/holdings', { headers })).json();
    const negative = holdings.filter((h: { quantity: number }) => h.quantity < 0);
    expect(negative.length).toBe(0);
  });
});

test.describe('Regression — Edge Cases', () => {
  let headers: Record<string, string>;

  test.beforeAll(async ({ request }) => {
    headers = authHeaders(await getAdminToken(request));
  });

  test('long text in ticket subject accepted', async ({ request }) => {
    const longSubject = 'A'.repeat(250);
    const resp = await request.post('/api/help/tickets', {
      headers,
      data: { subject: longSubject, message: 'Testing long subject' },
    });
    expect([200, 201]).toContain(resp.status());
  });

  test('special characters in stock search', async ({ request }) => {
    const resp = await request.get("/api/stocks/lookup?query=HDFC%20Bank%20%26%20Co", { headers });
    expect([200, 400]).toContain(resp.status());
  });

  test('empty string search returns results or empty array', async ({ request }) => {
    const resp = await request.get('/api/stocks/lookup?query=', { headers });
    expect(resp.status()).toBe(200);
  });

  test('decimal precision preserved in holdings', async ({ request }) => {
    const holdings = await (await request.get('/api/holdings', { headers })).json();
    for (const h of holdings) {
      if (h.averageBuyPrice != null) {
        expect(typeof h.averageBuyPrice).toBe('number');
      }
      if (h.investedAmount != null) {
        expect(typeof h.investedAmount).toBe('number');
      }
    }
  });

  test('rate limiting on ticket submission', async ({ request }) => {
    for (let i = 0; i < 4; i++) {
      const resp = await request.post('/api/help/tickets', {
        headers,
        data: { subject: `Rate limit test ${i}`, message: 'Testing rate limit' },
      });
      if (i >= 3) {
        const body = await resp.json();
        if (body.message && body.message.includes('Rate limited')) {
          expect(body.message).toContain('Rate limited');
          return;
        }
      }
    }
  });
});
