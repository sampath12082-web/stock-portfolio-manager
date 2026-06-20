import { test, expect } from '@playwright/test';

test.describe('Smoke Tests — App loads and all pages render', () => {
  test('homepage loads with Dashboard title', async ({ page }) => {
    await page.goto('/');
    await expect(page.locator('h1')).toContainText('Dashboard');
  });

  test('all 6 sidebar nav links present', async ({ page }) => {
    await page.goto('/');
    const nav = page.locator('nav');
    await expect(nav.getByText('Dashboard')).toBeVisible();
    await expect(nav.getByText('Holdings')).toBeVisible();
    await expect(nav.getByText('Transactions')).toBeVisible();
    await expect(nav.getByText('Stocks')).toBeVisible();
    await expect(nav.getByText('Mutual Funds')).toBeVisible();
    await expect(nav.getByText('Performance')).toBeVisible();
  });

  test('Holdings page loads', async ({ page }) => {
    await page.goto('/holdings');
    await expect(page.locator('h1')).toContainText('Holdings');
  });

  test('Transactions page loads', async ({ page }) => {
    await page.goto('/transactions');
    await expect(page.locator('h1')).toContainText('Transactions');
  });

  test('Stocks page loads', async ({ page }) => {
    await page.goto('/stocks');
    await expect(page.locator('h1')).toContainText('Stocks');
  });

  test('Mutual Funds page loads', async ({ page }) => {
    await page.goto('/mutual-funds');
    await expect(page.locator('h1')).toContainText('Mutual Funds');
  });

  test('Performance page loads', async ({ page }) => {
    await page.goto('/performance');
    await expect(page.locator('h1')).toContainText('Performance');
  });

  test('API health — dashboard endpoint returns 200', async ({ request }) => {
    const resp = await request.get('/api/dashboard');
    expect(resp.status()).toBe(200);
  });

  test('API health — stocks endpoint returns 200', async ({ request }) => {
    const resp = await request.get('/api/stocks');
    expect(resp.status()).toBe(200);
  });

  test('API health — holdings endpoint returns 200', async ({ request }) => {
    const resp = await request.get('/api/holdings');
    expect(resp.status()).toBe(200);
  });

  test('API health — MF funds endpoint returns 200', async ({ request }) => {
    const resp = await request.get('/api/mf/funds');
    expect(resp.status()).toBe(200);
  });
});
