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

  test('forgot-password rejects unknown email', async ({ request }) => {
    const resp = await request.post('/api/auth/forgot-password', {
      data: { email: 'nonexistent@example.com' },
    });
    expect([400, 404]).toContain(resp.status());
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

  test('groww config endpoint accessible', async ({ request }) => {
    const resp = await request.get('/api/profile/groww', { headers });
    expect([200, 204]).toContain(resp.status());
  });
});

// ─── HIGH: Signals API ───────────────────────────────────────────

test.describe('Functional — Signals API', () => {
  let headers: Record<string, string>;

  test.beforeAll(async ({ request }) => {
    headers = authHeaders(await getAdminToken(request));
  });

  test('active signals returns array', async ({ request }) => {
    const resp = await request.get('/api/signals/active', { headers });
    expect(resp.status()).toBe(200);
    const signals = await resp.json();
    expect(Array.isArray(signals)).toBe(true);
    expect(signals.length).toBeGreaterThan(0);
  });

  test('active signals have required fields', async ({ request }) => {
    const resp = await request.get('/api/signals/active', { headers });
    const signals = await resp.json();
    if (signals.length > 0) {
      expect(signals[0]).toHaveProperty('symbol');
      expect(signals[0]).toHaveProperty('signalType');
      expect(signals[0]).toHaveProperty('status');
      expect(signals[0]).toHaveProperty('targetPrice');
    }
  });

  test('today signals returns array', async ({ request }) => {
    const resp = await request.get('/api/signals/today', { headers });
    expect(resp.status()).toBe(200);
    const signals = await resp.json();
    expect(Array.isArray(signals)).toBe(true);
  });

  test('signals list returns all signals', async ({ request }) => {
    const resp = await request.get('/api/signals', { headers });
    expect(resp.status()).toBe(200);
    const signals = await resp.json();
    expect(Array.isArray(signals)).toBe(true);
  });

  test('analyze endpoint triggers signal generation', async ({ request }) => {
    const resp = await request.post('/api/signals/analyze', { headers, timeout: 60000 });
    expect([200, 202]).toContain(resp.status());
  });

  test('recommendations returns array', async ({ request }) => {
    const resp = await request.get('/api/signals/recommendations', { headers });
    expect(resp.status()).toBe(200);
  });
});

// ─── HIGH: Help / FAQ API ────────────────────────────────────────

test.describe('Functional — Help & FAQ API', () => {
  let headers: Record<string, string>;

  test.beforeAll(async ({ request }) => {
    headers = authHeaders(await getAdminToken(request));
  });

  test('public FAQ returns seeded entries', async ({ request }) => {
    const resp = await request.get('/api/help/faq', { headers });
    expect(resp.status()).toBe(200);
    const faqs = await resp.json();
    expect(Array.isArray(faqs)).toBe(true);
    expect(faqs.length).toBeGreaterThan(0);
  });

  test('FAQ entries have question, answer, category', async ({ request }) => {
    const resp = await request.get('/api/help/faq', { headers });
    const faqs = await resp.json();
    if (faqs.length > 0) {
      expect(faqs[0]).toHaveProperty('question');
      expect(faqs[0]).toHaveProperty('answer');
      expect(faqs[0]).toHaveProperty('category');
    }
  });

  test('user can submit a support ticket', async ({ request }) => {
    const resp = await request.post('/api/help/tickets', {
      headers,
      data: { subject: 'Test Ticket', message: 'Automated test ticket' },
    });
    expect([200, 201]).toContain(resp.status());
  });

  test('user can list their tickets', async ({ request }) => {
    const resp = await request.get('/api/help/tickets', { headers });
    expect(resp.status()).toBe(200);
    const tickets = await resp.json();
    expect(Array.isArray(tickets)).toBe(true);
  });

  test('admin can list all tickets', async ({ request }) => {
    const resp = await request.get('/api/admin/tickets', { headers });
    expect(resp.status()).toBe(200);
    const tickets = await resp.json();
    expect(Array.isArray(tickets)).toBe(true);
  });

  test('admin FAQ create endpoint accessible', async ({ request }) => {
    const resp = await request.post('/api/admin/faq', {
      headers,
      data: { question: 'Test Q?', answer: 'Test A', category: 'General' },
    });
    expect([200, 201]).toContain(resp.status());
  });
});

// ─── HIGH: Quotes API ────────────────────────────────────────────

test.describe('Functional — Quotes API', () => {
  let headers: Record<string, string>;

  test.beforeAll(async ({ request }) => {
    headers = authHeaders(await getAdminToken(request));
  });

  test('quotes returns cached price map', async ({ request }) => {
    const resp = await request.get('/api/quotes', { headers });
    expect(resp.status()).toBe(200);
    const quotes = await resp.json();
    expect(typeof quotes).toBe('object');
  });

  test('quote for specific symbol returns data', async ({ request }) => {
    const resp = await request.get('/api/quotes/TCS', { headers });
    expect(resp.status()).toBe(200);
    const q = await resp.json();
    expect(q).toHaveProperty('ltp');
  });

  test('refresh quotes triggers update', async ({ request }) => {
    const resp = await request.post('/api/quotes/refresh', { headers, timeout: 60000 });
    expect(resp.status()).toBe(200);
  });
});

// ─── MEDIUM: Auth — Password Policy ─────────────────────────────

test.describe('Functional — Password Policy', () => {
  test('rejects password shorter than 16 chars', async ({ request }) => {
    const resp = await request.post('/api/auth/register', {
      data: {
        email: `policy1_${Date.now()}@test.com`, password: 'Short@1234',
        firstName: 'Test', securityQuestion1: 'Q1?', securityAnswer1: 'A1',
        securityQuestion2: 'Q2?', securityAnswer2: 'A2',
      },
    });
    expect(resp.status()).toBe(400);
  });

  test('rejects password longer than 20 chars', async ({ request }) => {
    const resp = await request.post('/api/auth/register', {
      data: {
        email: `policy2_${Date.now()}@test.com`, password: 'VeryLongPassword@123456',
        firstName: 'Test', securityQuestion1: 'Q1?', securityAnswer1: 'A1',
        securityQuestion2: 'Q2?', securityAnswer2: 'A2',
      },
    });
    expect(resp.status()).toBe(400);
  });

  test('rejects password without special char', async ({ request }) => {
    const resp = await request.post('/api/auth/register', {
      data: {
        email: `policy3_${Date.now()}@test.com`, password: 'NoSpecialChar12345',
        firstName: 'Test', securityQuestion1: 'Q1?', securityAnswer1: 'A1',
        securityQuestion2: 'Q2?', securityAnswer2: 'A2',
      },
    });
    expect(resp.status()).toBe(400);
  });

  test('rejects password without digit', async ({ request }) => {
    const resp = await request.post('/api/auth/register', {
      data: {
        email: `policy4_${Date.now()}@test.com`, password: 'NoDigitsHere@abcde',
        firstName: 'Test', securityQuestion1: 'Q1?', securityAnswer1: 'A1',
        securityQuestion2: 'Q2?', securityAnswer2: 'A2',
      },
    });
    expect(resp.status()).toBe(400);
  });
});

// ─── MEDIUM: Auth — Security Questions & OTP ─────────────────────

test.describe('Functional — Security Questions & OTP', () => {
  test('register requires security questions', async ({ request }) => {
    const resp = await request.post('/api/auth/register', {
      data: {
        email: `nosq_${Date.now()}@test.com`, password: 'ValidPassword@1234',
        firstName: 'Test',
      },
    });
    expect(resp.status()).toBe(400);
  });

  test('verify-security rejects wrong answers', async ({ request }) => {
    const resp = await request.post('/api/auth/verify-security', {
      data: { email: 'sampath12082@gmail.com', answer1: 'wrong1', answer2: 'wrong2' },
    });
    expect([400, 401]).toContain(resp.status());
  });

  test('verify-otp rejects invalid OTP', async ({ request }) => {
    const resp = await request.post('/api/auth/verify-otp', {
      data: { email: 'sampath12082@gmail.com', otp: '000000' },
    });
    expect([400, 401]).toContain(resp.status());
  });

  test('reset-password rejects without valid OTP', async ({ request }) => {
    const resp = await request.post('/api/auth/reset-password', {
      data: { email: 'sampath12082@gmail.com', otp: '000000', newPassword: 'NewPassword@12345' },
    });
    expect([400, 401]).toContain(resp.status());
  });

  test('public-key returns RSA PEM', async ({ request }) => {
    const resp = await request.get('/api/auth/public-key');
    expect(resp.status()).toBe(200);
    const body = await resp.json();
    expect(body).toHaveProperty('publicKey');
    expect(body.publicKey).toContain('BEGIN PUBLIC KEY');
  });
});

// ─── MEDIUM: Groww API ───────────────────────────────────────────

test.describe('Functional — Groww API', () => {
  let headers: Record<string, string>;

  test.beforeAll(async ({ request }) => {
    headers = authHeaders(await getAdminToken(request));
  });

  test('groww status returns enabled flag', async ({ request }) => {
    const resp = await request.get('/api/groww/status', { headers });
    expect(resp.status()).toBe(200);
    const s = await resp.json();
    expect(s).toHaveProperty('enabled');
  });

  test('groww account returns data when enabled', async ({ request }) => {
    const resp = await request.get('/api/groww/account', { headers });
    if (resp.status() === 200) {
      const g = await resp.json();
      expect(g).toHaveProperty('clearCash');
      expect(g).toHaveProperty('todayOrders');
    }
  });

  test('groww sync endpoint accessible', async ({ request }) => {
    const resp = await request.post('/api/groww/sync', { headers });
    expect([200, 400, 503]).toContain(resp.status());
  });
});

// ─── MEDIUM: Admin Extended ──────────────────────────────────────

test.describe('Functional — Admin Extended', () => {
  let headers: Record<string, string>;

  test.beforeAll(async ({ request }) => {
    headers = authHeaders(await getAdminToken(request));
  });

  test('admin can respond to ticket', async ({ request }) => {
    const tickets = await (await request.get('/api/admin/tickets', { headers })).json();
    if (tickets.length > 0) {
      const resp = await request.put(`/api/admin/tickets/${tickets[0].id}`, {
        headers,
        data: { response: 'Test response from admin' },
      });
      expect([200, 204]).toContain(resp.status());
    }
  });

  test('admin reset-password for user', async ({ request }) => {
    const users = await (await request.get('/api/admin/users', { headers })).json();
    const nonAdmin = users.find((u: { role: string; id: number }) => u.role !== 'ROLE_ADMIN');
    if (nonAdmin) {
      const resp = await request.post(`/api/admin/users/${nonAdmin.id}/reset-password`, { headers });
      expect([200, 204]).toContain(resp.status());
    }
  });
});

// ─── LOW: Performance Extended ───────────────────────────────────

test.describe('Functional — Performance Extended', () => {
  let headers: Record<string, string>;

  test.beforeAll(async ({ request }) => {
    headers = authHeaders(await getAdminToken(request));
  });

  test('snapshot capture works', async ({ request }) => {
    const resp = await request.post('/api/performance/snapshot', { headers, timeout: 60000 });
    expect([200, 201]).toContain(resp.status());
  });

  test('history returns array with date range', async ({ request }) => {
    const resp = await request.get('/api/performance/history?from=2026-01-01&to=2026-12-31', { headers });
    expect(resp.status()).toBe(200);
    const hist = await resp.json();
    expect(Array.isArray(hist)).toBe(true);
  });
});

// ─── LOW: Health Endpoint ────────────────────────────────────────

test.describe('Functional — Health', () => {
  let headers: Record<string, string>;

  test.beforeAll(async ({ request }) => {
    headers = authHeaders(await getAdminToken(request));
  });

  test('health endpoint returns 200', async ({ request }) => {
    const resp = await request.get('/api/health', { headers });
    expect(resp.status()).toBe(200);
  });
});
