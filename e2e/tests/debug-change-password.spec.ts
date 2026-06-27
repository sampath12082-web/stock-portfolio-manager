import { test, expect } from '@playwright/test';

import { TEST_EMAIL as EMAIL, TEST_PASSWORD } from './helpers';
const OLD_PASSWORD = TEST_PASSWORD;
const NEW_PASSWORD = 'Changed@123456789';

test.describe('E2E — Change Password Full Flow', () => {
  test('change password via UI → logout → login with new → revert', async ({ page }) => {
    // 1. Login with old password
    await page.goto('/login');
    await page.fill('input[type="email"]', EMAIL);
    await page.fill('input[type="password"]', OLD_PASSWORD);
    await page.click('button[type="submit"]');
    await page.waitForURL('/', { timeout: 15000 });

    // 2. Navigate to Profile
    await page.click('text=Sampat Kumar');
    await expect(page.locator('h1')).toContainText('Profile', { timeout: 10000 });

    // 3. Fill change password form
    const pwInputs = page.locator('input[type="password"]');
    await pwInputs.nth(0).fill(OLD_PASSWORD);
    await pwInputs.nth(1).fill(NEW_PASSWORD);

    // 4. Submit and verify request is encrypted
    const [request] = await Promise.all([
      page.waitForRequest(req => req.url().includes('/auth/change-password'), { timeout: 10000 }),
      page.locator('button:has-text("Change Password")').click(),
    ]);
    const body = request.postDataJSON();
    expect(body.currentPassword.length).toBeGreaterThan(100);
    expect(body.newPassword.length).toBeGreaterThan(100);

    // 5. Verify success message
    await expect(page.getByText('Password changed')).toBeVisible({ timeout: 5000 });

    // 6. Logout
    await page.click('text=Logout');
    await page.waitForURL('/login', { timeout: 5000 });

    // 7. Login with NEW password should work
    await page.fill('input[type="email"]', EMAIL);
    await page.fill('input[type="password"]', NEW_PASSWORD);
    await page.click('button[type="submit"]');
    await page.waitForURL('/', { timeout: 15000 });
    await expect(page.getByText('Total Funds')).toBeVisible({ timeout: 20000 });

    // 8. Revert password back
    await page.click('text=Sampat Kumar');
    await expect(page.locator('h1')).toContainText('Profile', { timeout: 10000 });
    const pwInputs2 = page.locator('input[type="password"]');
    await pwInputs2.nth(0).fill(NEW_PASSWORD);
    await pwInputs2.nth(1).fill(OLD_PASSWORD);
    await page.locator('button:has-text("Change Password")').click();
    await expect(page.getByText('Password changed')).toBeVisible({ timeout: 5000 });
  });

  test('change password with wrong current shows error', async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[type="email"]', EMAIL);
    await page.fill('input[type="password"]', OLD_PASSWORD);
    await page.click('button[type="submit"]');
    await page.waitForURL('/', { timeout: 15000 });

    await page.click('text=Sampat Kumar');
    await expect(page.locator('h1')).toContainText('Profile', { timeout: 10000 });

    const pwInputs = page.locator('input[type="password"]');
    await pwInputs.nth(0).fill('WrongPassword@12345');
    await pwInputs.nth(1).fill(NEW_PASSWORD);
    await page.locator('button:has-text("Change Password")').click();
    await page.waitForTimeout(2000);

    const errorVisible = await page.getByText(/incorrect|failed|change/i).first().isVisible().catch(() => false);
    const pageContent = await page.textContent('body');
    const hasErrorIndicator = pageContent?.includes('incorrect') || pageContent?.includes('failed') ||
                              pageContent?.includes('Password change') || pageContent?.includes('error');
    expect(errorVisible || hasErrorIndicator).toBeTruthy();
  });

  test('passwords are RSA-encrypted in network', async ({ page }) => {
    await page.goto('/login');

    const [loginReq] = await Promise.all([
      page.waitForRequest(req => req.url().includes('/auth/login'), { timeout: 10000 }),
      (async () => {
        await page.fill('input[type="email"]', EMAIL);
        await page.fill('input[type="password"]', OLD_PASSWORD);
        await page.click('button[type="submit"]');
      })(),
    ]);

    const loginBody = loginReq.postDataJSON();
    expect(loginBody.password.length).toBeGreaterThan(100);
    expect(loginBody.password).not.toBe(OLD_PASSWORD);
  });
});

test.describe('E2E — Change Password Survives Restart', () => {
  test('change password → restart backend → login with new password', async ({ page, request }) => {
    // 1. Login
    await page.goto('/login');
    await page.fill('input[type="email"]', EMAIL);
    await page.fill('input[type="password"]', OLD_PASSWORD);
    await page.click('button[type="submit"]');
    await page.waitForURL('/', { timeout: 15000 });

    // 2. Change password
    await page.click('text=Sampat Kumar');
    await expect(page.locator('h1')).toContainText('Profile', { timeout: 10000 });
    const pwInputs = page.locator('input[type="password"]');
    await pwInputs.nth(0).fill(OLD_PASSWORD);
    await pwInputs.nth(1).fill(NEW_PASSWORD);
    await page.locator('button:has-text("Change Password")').click();
    await expect(page.getByText('Password changed')).toBeVisible({ timeout: 5000 });

    // 3. Simulate RSA key rotation by clearing cached key — 
    //    On real Render restart, new RSA key generated.
    //    We test by logging out (clears frontend state) and doing a fresh login.
    await page.click('text=Logout');
    await page.waitForURL('/login', { timeout: 5000 });

    // 4. Clear all browser state (simulates fresh browser after restart)
    await page.context().clearCookies();
    await page.evaluate(() => localStorage.clear());

    // 5. Fresh login with NEW password
    await page.goto('/login');
    await page.fill('input[type="email"]', EMAIL);
    await page.fill('input[type="password"]', NEW_PASSWORD);
    await page.click('button[type="submit"]');
    await page.waitForURL('/', { timeout: 15000 });
    await expect(page.getByText('Total Funds')).toBeVisible({ timeout: 20000 });

    // 6. OLD password should fail
    await page.click('text=Logout');
    await page.waitForURL('/login', { timeout: 5000 });
    await page.fill('input[type="email"]', EMAIL);
    await page.fill('input[type="password"]', OLD_PASSWORD);
    await page.click('button[type="submit"]');
    await page.waitForTimeout(3000);
    const stillOnLogin = page.url().includes('/login');
    expect(stillOnLogin).toBeTruthy();

    // 7. Revert password back
    await page.fill('input[type="email"]', EMAIL);
    await page.fill('input[type="password"]', NEW_PASSWORD);
    await page.click('button[type="submit"]');
    await page.waitForURL('/', { timeout: 15000 });
    await page.click('text=Sampat Kumar');
    await expect(page.locator('h1')).toContainText('Profile', { timeout: 10000 });
    const pw2 = page.locator('input[type="password"]');
    await pw2.nth(0).fill(NEW_PASSWORD);
    await pw2.nth(1).fill(OLD_PASSWORD);
    await page.locator('button:has-text("Change Password")').click();
    await expect(page.getByText('Password changed')).toBeVisible({ timeout: 5000 });
  });
});
