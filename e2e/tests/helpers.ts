import { APIRequestContext } from '@playwright/test';

export const TEST_EMAIL = process.env.TEST_EMAIL || 'e2e-tester@solosprint.dev';
export const TEST_PASSWORD = process.env.TEST_PASSWORD || 'E2eTestUser@12345';

let cachedToken: string | null = null;

export async function getTestToken(request: APIRequestContext): Promise<string> {
  if (cachedToken) return cachedToken;

  // Try login
  let login = await request.post('/api/auth/login', {
    data: { email: TEST_EMAIL, password: TEST_PASSWORD },
  });

  if (login.status() !== 200) {
    // Register test user
    await request.post('/api/auth/register', {
      data: {
        email: TEST_EMAIL, password: TEST_PASSWORD,
        firstName: 'E2E', lastName: 'Tester',
        securityQuestion1: 'What city were you born in?', securityAnswer1: 'TestCity',
        securityQuestion2: 'What is your favorite movie?', securityAnswer2: 'TestMovie',
      },
    });

    // Try setup-admin as fallback (creates verified admin if none exists)
    await request.post('/api/auth/setup-admin', {
      data: { email: TEST_EMAIL, password: TEST_PASSWORD, firstName: 'E2E' },
    });

    login = await request.post('/api/auth/login', {
      data: { email: TEST_EMAIL, password: TEST_PASSWORD },
    });

    // If test user still can't login (OTP not verified), fall back to admin
    if (login.status() !== 200) {
      const adminEmail = process.env.ADMIN_EMAIL || 'sampath12082@gmail.com';
      const adminPass = process.env.ADMIN_PASSWORD || 'Admin@1234567890*';
      login = await request.post('/api/auth/login', {
        data: { email: adminEmail, password: adminPass },
      });
    }
  }

  const body = await login.json();
  cachedToken = body.accessToken;
  return cachedToken!;
}

export function authHeaders(token: string) {
  return { Authorization: `Bearer ${token}` };
}

export const ADMIN_EMAIL = TEST_EMAIL;
export const ADMIN_PASSWORD = TEST_PASSWORD;
export const getAdminToken = getTestToken;
