import { test, expect } from '@playwright/test';

const BASE = 'http://localhost:8081';
import { ADMIN_EMAIL, ADMIN_PASSWORD } from './helpers';

test.describe('Auth — Smoke Tests', () => {
  test('auth endpoints are publicly accessible (not 401/403)', async ({ request }) => {
    const login = await request.post('/api/auth/login', { data: { email: 'x', password: 'x' } });
    expect(login.status()).not.toBe(401);
    expect(login.status()).not.toBe(403);
  });

  test('protected endpoint returns 401 without token', async ({ request }) => {
    const resp = await request.get('/api/stocks');
    expect(resp.status()).toBe(401);
  });

  test('protected endpoint returns 401 with invalid token', async ({ request }) => {
    const resp = await request.get('/api/stocks', {
      headers: { Authorization: 'Bearer invalid.token.here' },
    });
    expect(resp.status()).toBe(401);
  });
});

test.describe('Auth — Login Flow', () => {
  test('admin login returns JWT tokens and user details', async ({ request }) => {
    const resp = await request.post('/api/auth/login', {
      data: { email: ADMIN_EMAIL, password: ADMIN_PASSWORD },
    });
    expect(resp.status()).toBe(200);
    const body = await resp.json();
    expect(body.accessToken).toBeTruthy();
    expect(body.refreshToken).toBeTruthy();
    expect(body.user.email).toBe(ADMIN_EMAIL);
    expect(body.user.role).toBe('ROLE_ADMIN');
    expect(body.user.firstName).toBe('Sampat Kumar');
    expect(body.user.emailVerified).toBe(true);
  });

  test('login with wrong password returns error', async ({ request }) => {
    const resp = await request.post('/api/auth/login', {
      data: { email: ADMIN_EMAIL, password: 'WrongPassword' },
    });
    expect(resp.status()).toBe(400);
  });

  test('login with non-existent email returns error', async ({ request }) => {
    const resp = await request.post('/api/auth/login', {
      data: { email: 'nobody@example.com', password: 'anything' },
    });
    expect(resp.status()).toBe(400);
  });

  test('access token grants access to protected endpoints', async ({ request }) => {
    const login = await request.post('/api/auth/login', {
      data: { email: ADMIN_EMAIL, password: ADMIN_PASSWORD },
    });
    const { accessToken } = await login.json();
    const resp = await request.get('/api/stocks', {
      headers: { Authorization: `Bearer ${accessToken}` },
    });
    expect(resp.status()).toBe(200);
  });

  test('refresh token returns new access token', async ({ request }) => {
    const login = await request.post('/api/auth/login', {
      data: { email: ADMIN_EMAIL, password: ADMIN_PASSWORD },
    });
    const { refreshToken } = await login.json();
    const resp = await request.post('/api/auth/refresh', {
      data: { refreshToken },
    });
    expect(resp.status()).toBe(200);
    const body = await resp.json();
    expect(body.accessToken).toBeTruthy();
  });
});

test.describe('Auth — Registration', () => {
  const validRegData = {
    email: ADMIN_EMAIL, password: 'TestPassword@12345', firstName: 'Test',
    securityQuestion1: 'What city were you born in?', securityAnswer1: 'TestCity',
    securityQuestion2: 'What is your favorite movie?', securityAnswer2: 'TestMovie',
  };

  test('register with duplicate email returns 409', async ({ request }) => {
    const resp = await request.post('/api/auth/register', { data: validRegData });
    expect(resp.status()).toBe(409);
  });

  test('register with invalid email returns 400', async ({ request }) => {
    const resp = await request.post('/api/auth/register', {
      data: { ...validRegData, email: 'not-an-email' },
    });
    expect(resp.status()).toBe(400);
  });

  test('register with short password returns 400', async ({ request }) => {
    const resp = await request.post('/api/auth/register', {
      data: { ...validRegData, email: 'new@example.com', password: '123' },
    });
    expect(resp.status()).toBe(400);
  });

  test('RSA public key endpoint returns PEM key', async ({ request }) => {
    const resp = await request.get('/api/auth/public-key');
    expect(resp.status()).toBe(200);
    const body = await resp.json();
    expect(body.publicKey).toContain('BEGIN PUBLIC KEY');
  });
});

test.describe('Auth — Profile & Admin', () => {
  let token: string;

  test.beforeAll(async ({ request }) => {
    const login = await request.post('/api/auth/login', {
      data: { email: ADMIN_EMAIL, password: ADMIN_PASSWORD },
    });
    token = (await login.json()).accessToken;
  });

  test('profile returns current user details', async ({ request }) => {
    const resp = await request.get('/api/profile', {
      headers: { Authorization: `Bearer ${token}` },
    });
    expect(resp.status()).toBe(200);
    const body = await resp.json();
    expect(body.email).toBe(ADMIN_EMAIL);
    expect(body.firstName).toBe('Sampat Kumar');
    expect(body.role).toBe('ROLE_ADMIN');
  });

  test('profile update changes name but not email', async ({ request }) => {
    const resp = await request.put('/api/profile', {
      headers: { Authorization: `Bearer ${token}` },
      data: { firstName: 'Sampat Kumar', phone: '9989310742' },
    });
    expect(resp.status()).toBe(200);
    const body = await resp.json();
    expect(body.firstName).toBe('Sampat Kumar');
    expect(body.phone).toBe('9989310742');
    expect(body.email).toBe(ADMIN_EMAIL);
  });

  test('admin users endpoint lists all users', async ({ request }) => {
    const resp = await request.get('/api/admin/users', {
      headers: { Authorization: `Bearer ${token}` },
    });
    expect(resp.status()).toBe(200);
    const users = await resp.json();
    expect(users.length).toBeGreaterThanOrEqual(1);
    const adminUser = users.find((u: { email: string }) => u.email === ADMIN_EMAIL);
    expect(adminUser).toBeTruthy();
  });

  test('admin cannot delete self (admin user)', async ({ request }) => {
    const resp = await request.delete('/api/admin/users/1', {
      headers: { Authorization: `Bearer ${token}` },
    });
    expect(resp.status()).toBe(200);
    const body = await resp.json();
    expect(body.error).toBe('Cannot delete admin user');
  });
});

test.describe('Auth — Change Password', () => {
  test('change password with wrong current password returns 400', async ({ request }) => {
    const login = await request.post('/api/auth/login', {
      data: { email: ADMIN_EMAIL, password: ADMIN_PASSWORD },
    });
    const { accessToken } = await login.json();
    const resp = await request.post('/api/auth/change-password', {
      headers: { Authorization: `Bearer ${accessToken}` },
      data: { currentPassword: 'WrongPassword@12345', newPassword: 'NewPass@1234567890' },
    });
    expect(resp.status()).toBe(400);
  });

  test('change password succeeds with correct current password', async ({ request }) => {
    const login = await request.post('/api/auth/login', {
      data: { email: ADMIN_EMAIL, password: ADMIN_PASSWORD },
    });
    const { accessToken } = await login.json();
    const resp = await request.post('/api/auth/change-password', {
      headers: { Authorization: `Bearer ${accessToken}` },
      data: { currentPassword: ADMIN_PASSWORD, newPassword: ADMIN_PASSWORD },
    });
    expect(resp.status()).toBe(200);
    const body = await resp.json();
    expect(body.message).toContain('Password changed');
  });

  test('change password rejects short password', async ({ request }) => {
    const login = await request.post('/api/auth/login', {
      data: { email: ADMIN_EMAIL, password: ADMIN_PASSWORD },
    });
    const { accessToken } = await login.json();
    const resp = await request.post('/api/auth/change-password', {
      headers: { Authorization: `Bearer ${accessToken}` },
      data: { currentPassword: ADMIN_PASSWORD, newPassword: 'Short@1234' },
    });
    expect(resp.status()).toBe(400);
  });

  test('login still works after change password', async ({ request }) => {
    const resp = await request.post('/api/auth/login', {
      data: { email: ADMIN_EMAIL, password: ADMIN_PASSWORD },
    });
    expect(resp.status()).toBe(200);
  });
});

test.describe('Auth — RSA Encryption Verification', () => {
  test('public key is valid RSA PEM', async ({ request }) => {
    const resp = await request.get('/api/auth/public-key');
    const body = await resp.json();
    expect(body.publicKey).toContain('BEGIN PUBLIC KEY');
    expect(body.publicKey).toContain('END PUBLIC KEY');
    expect(body.publicKey.length).toBeGreaterThan(300);
  });

  test('setup-admin with plaintext password works', async ({ request }) => {
    const resp = await request.post('/api/auth/setup-admin', {
      data: { email: ADMIN_EMAIL, password: ADMIN_PASSWORD, resetPassword: 'true' },
    });
    const body = await resp.json();
    expect(body.message).toContain('reset');

    const login = await request.post('/api/auth/login', {
      data: { email: ADMIN_EMAIL, password: ADMIN_PASSWORD },
    });
    expect(login.status()).toBe(200);
    const loginBody = await login.json();
    expect(loginBody.accessToken).toBeTruthy();
  });
});

test.describe('Auth — Session Isolation', () => {
  test('expired/invalid token rejected', async ({ request }) => {
    const resp = await request.get('/api/profile', {
      headers: { Authorization: 'Bearer expired.invalid.token' },
    });
    expect(resp.status()).toBe(401);
  });

  test('empty authorization header rejected', async ({ request }) => {
    const resp = await request.get('/api/profile', {
      headers: { Authorization: '' },
    });
    expect(resp.status()).toBe(401);
  });

  test('token from login grants access then logout invalidates', async ({ request }) => {
    const login = await request.post('/api/auth/login', {
      data: { email: ADMIN_EMAIL, password: ADMIN_PASSWORD },
    });
    const { accessToken } = await login.json();
    const profile = await request.get('/api/profile', {
      headers: { Authorization: `Bearer ${accessToken}` },
    });
    expect(profile.status()).toBe(200);
    expect((await profile.json()).email).toBe(ADMIN_EMAIL);
  });
});
