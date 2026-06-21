import { APIRequestContext } from '@playwright/test';

const ADMIN_EMAIL = 'sampath12082@gmail.com';
const ADMIN_PASSWORD = 'Admin@123';

export async function getAdminToken(request: APIRequestContext): Promise<string> {
  const resp = await request.post('/api/auth/login', {
    data: { email: ADMIN_EMAIL, password: ADMIN_PASSWORD },
  });
  const body = await resp.json();
  return body.accessToken;
}

export function authHeaders(token: string) {
  return { Authorization: `Bearer ${token}` };
}
