import { test, expect } from '@playwright/test';
import { getAdminToken, authHeaders } from './helpers';

test.describe('Functional — Auth API', () => {
  test('register validates required fields', async ({ request }) => {
    const resp = await request.post('/api/auth/register', { data: {} });
    expect(resp.status()).toBe(400);
  });

  test('register validates email format', async ({ request }) => {
    const resp = await request.post('/api/auth/register', {
      data: { email: 'bad-email', password: 'Test@123', firstName: 'Test' },
    });
    expect(resp.status()).toBe(400);
  });

  test('register validates password length', async ({ request }) => {
    const resp = await request.post('/api/auth/register', {
      data: { email: 'valid@example.com', password: '123', firstName: 'Test' },
    });
    expect(resp.status()).toBe(400);
  });

  test('login validates required fields', async ({ request }) => {
    const resp = await request.post('/api/auth/login', { data: {} });
    expect(resp.status()).toBe(400);
  });

  test('forgot-password always returns success (no email leak)', async ({ request }) => {
    const resp = await request.post('/api/auth/forgot-password', {
      data: { email: 'nonexistent@example.com' },
    });
    expect(resp.status()).toBe(200);
  });
});

test.describe('Functional — Dashboard API', () => {
  let token: string;
  let headers: Record<string, string>;

  test.beforeAll(async ({ request }) => {
    token = await getAdminToken(request);
    headers = authHeaders(token);
  });

  test('dashboard returns expected fields', async ({ request }) => {
    const resp = await request.get('/api/dashboard', { headers });
    expect(resp.status()).toBe(200);
    const d = await resp.json();
    expect(d).toHaveProperty('investedAmount');
    expect(d).toHaveProperty('currentValue');
    expect(d).toHaveProperty('unrealizedPnL');
    expect(d).toHaveProperty('totalDeposited');
  });

  test('portfolio summary returns day P&L', async ({ request }) => {
    const resp = await request.get('/api/portfolio/summary', { headers });
    expect(resp.status()).toBe(200);
    const s = await resp.json();
    expect(s).toHaveProperty('totalInvestment');
    expect(s).toHaveProperty('currentValue');
  });

  test('sector allocation returns sectors', async ({ request }) => {
    const resp = await request.get('/api/portfolio/allocation', { headers });
    expect(resp.status()).toBe(200);
    const sectors = await resp.json();
    expect(Array.isArray(sectors)).toBe(true);
  });
});

test.describe('Functional — Holdings API', () => {
  let headers: Record<string, string>;

  test.beforeAll(async ({ request }) => {
    headers = authHeaders(await getAdminToken(request));
  });

  test('holdings returns array with expected fields', async ({ request }) => {
    const resp = await request.get('/api/holdings', { headers });
    expect(resp.status()).toBe(200);
    const holdings = await resp.json();
    expect(Array.isArray(holdings)).toBe(true);
    if (holdings.length > 0) {
      expect(holdings[0]).toHaveProperty('symbol');
      expect(holdings[0]).toHaveProperty('quantity');
      expect(holdings[0]).toHaveProperty('investedAmount');
    }
  });
});

test.describe('Functional — Transactions API', () => {
  let headers: Record<string, string>;

  test.beforeAll(async ({ request }) => {
    headers = authHeaders(await getAdminToken(request));
  });

  test('transactions returns array', async ({ request }) => {
    const resp = await request.get('/api/transactions', { headers });
    expect(resp.status()).toBe(200);
    const txns = await resp.json();
    expect(Array.isArray(txns)).toBe(true);
  });

  test('transaction analytics returns all fields', async ({ request }) => {
    const resp = await request.get('/api/transactions/analytics', { headers });
    expect(resp.status()).toBe(200);
    const a = await resp.json();
    expect(a).toHaveProperty('totalBuyAmount');
    expect(a).toHaveProperty('totalSellAmount');
    expect(a).toHaveProperty('intradayPnL');
    expect(a).toHaveProperty('deliveryBuyAmount');
    expect(a).toHaveProperty('totalTransactions');
  });
});

test.describe('Functional — Stocks API', () => {
  let headers: Record<string, string>;

  test.beforeAll(async ({ request }) => {
    headers = authHeaders(await getAdminToken(request));
  });

  test('stocks returns array with expected fields', async ({ request }) => {
    const resp = await request.get('/api/stocks', { headers });
    expect(resp.status()).toBe(200);
    const stocks = await resp.json();
    expect(Array.isArray(stocks)).toBe(true);
    if (stocks.length > 0) {
      expect(stocks[0]).toHaveProperty('symbol');
      expect(stocks[0]).toHaveProperty('companyName');
      expect(stocks[0]).toHaveProperty('exchange');
    }
  });

  test('stock lookup searches Yahoo Finance', async ({ request }) => {
    const resp = await request.get('/api/stocks/lookup?query=RELIANCE', { headers });
    expect(resp.status()).toBe(200);
    const results = await resp.json();
    expect(Array.isArray(results)).toBe(true);
  });
});

test.describe('Functional — Mutual Funds API', () => {
  let headers: Record<string, string>;

  test.beforeAll(async ({ request }) => {
    headers = authHeaders(await getAdminToken(request));
  });

  test('MF funds returns array', async ({ request }) => {
    const resp = await request.get('/api/mf/funds', { headers });
    expect(resp.status()).toBe(200);
  });

  test('MF holdings returns array', async ({ request }) => {
    const resp = await request.get('/api/mf/holdings', { headers });
    expect(resp.status()).toBe(200);
  });

  test('MF transactions returns array', async ({ request }) => {
    const resp = await request.get('/api/mf/transactions', { headers });
    expect(resp.status()).toBe(200);
  });
});

test.describe('Functional — Performance API', () => {
  let headers: Record<string, string>;

  test.beforeAll(async ({ request }) => {
    headers = authHeaders(await getAdminToken(request));
  });

  test('recent performance returns array', async ({ request }) => {
    const resp = await request.get('/api/performance/recent?days=7', { headers });
    expect(resp.status()).toBe(200);
  });

  test('today snapshot returns snapshot', async ({ request }) => {
    const resp = await request.get('/api/performance/today', { headers });
    expect(resp.status()).toBe(200);
    const snap = await resp.json();
    expect(snap).toHaveProperty('totalInvestment');
    expect(snap).toHaveProperty('currentValue');
  });
});

test.describe('Functional — Admin API', () => {
  let adminHeaders: Record<string, string>;

  test.beforeAll(async ({ request }) => {
    adminHeaders = authHeaders(await getAdminToken(request));
  });

  test('admin can list users', async ({ request }) => {
    const resp = await request.get('/api/admin/users', { headers: adminHeaders });
    expect(resp.status()).toBe(200);
    const users = await resp.json();
    expect(users.length).toBeGreaterThanOrEqual(1);
  });

  test('admin can view specific user', async ({ request }) => {
    const resp = await request.get('/api/admin/users/1', { headers: adminHeaders });
    expect(resp.status()).toBe(200);
    const user = await resp.json();
    expect(user.email).toBe('sampath12082@gmail.com');
  });

  test('admin can update user status', async ({ request }) => {
    const resp = await request.put('/api/admin/users/1/status', {
      headers: adminHeaders,
      data: { status: 'ACTIVE' },
    });
    expect(resp.status()).toBe(200);
  });
});

test.describe('Functional — Profile API', () => {
  let headers: Record<string, string>;

  test.beforeAll(async ({ request }) => {
    headers = authHeaders(await getAdminToken(request));
  });

  test('profile returns user with email', async ({ request }) => {
    const resp = await request.get('/api/profile', { headers });
    expect(resp.status()).toBe(200);
    const profile = await resp.json();
    expect(profile.email).toBe('sampath12082@gmail.com');
    expect(profile.emailVerified).toBe(true);
  });

  test('profile update preserves email', async ({ request }) => {
    const resp = await request.put('/api/profile', {
      headers,
      data: { firstName: 'Sampat Kumar' },
    });
    expect(resp.status()).toBe(200);
    const profile = await resp.json();
    expect(profile.email).toBe('sampath12082@gmail.com');
    expect(profile.firstName).toBe('Sampat Kumar');
  });
});
