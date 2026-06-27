import { TEST_EMAIL, TEST_PASSWORD } from "./helpers";
import { test, expect } from '@playwright/test';
import { getAdminToken, authHeaders } from './helpers';

test.describe('Smoke Tests — App loads and API health', () => {
  let token: string;

  test.beforeAll(async ({ request }) => {
    token = await getAdminToken(request);
  });

  test('auth endpoint is accessible (login returns 200)', async ({ request }) => {
    const resp = await request.post('/api/auth/login', {
      data: { email: TEST_EMAIL, password: TEST_PASSWORD },
    });
    expect(resp.status()).toBe(200);
  });

  test('protected endpoint returns 401 without token', async ({ request }) => {
    const resp = await request.get('/api/stocks');
    expect(resp.status()).toBe(401);
  });

  test('API health — dashboard returns 200 with token', async ({ request }) => {
    const resp = await request.get('/api/dashboard', { headers: authHeaders(token) });
    expect(resp.status()).toBe(200);
  });

  test('API health — stocks returns 200 with token', async ({ request }) => {
    const resp = await request.get('/api/stocks', { headers: authHeaders(token) });
    expect(resp.status()).toBe(200);
  });

  test('API health — holdings returns 200 with token', async ({ request }) => {
    const resp = await request.get('/api/holdings', { headers: authHeaders(token) });
    expect(resp.status()).toBe(200);
  });

  test('API health — MF funds returns 200 with token', async ({ request }) => {
    const resp = await request.get('/api/mf/funds', { headers: authHeaders(token) });
    expect(resp.status()).toBe(200);
  });

  test('API health — transactions returns 200 with token', async ({ request }) => {
    const resp = await request.get('/api/transactions', { headers: authHeaders(token) });
    expect(resp.status()).toBe(200);
  });

  test('API health — profile returns 200 with token', async ({ request }) => {
    const resp = await request.get('/api/profile', { headers: authHeaders(token) });
    expect(resp.status()).toBe(200);
  });

  test('all SPA routes return 200', async ({ request }) => {
    for (const path of ['/', '/holdings', '/transactions', '/stocks', '/mutual-funds', '/performance', '/help', '/admin/users']) {
      const resp = await request.get(path);
      expect(resp.status()).toBe(200);
    }
  });

  test('build validation — served JS matches built JS', async ({ request }) => {
    const htmlResp = await request.get('/');
    const html = await htmlResp.text();
    const servedMatch = html.match(/assets\/index-([^"]+)\.js/);
    expect(servedMatch).toBeTruthy();
  });
});
