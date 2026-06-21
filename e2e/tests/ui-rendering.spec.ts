import { test, expect, type Page } from '@playwright/test';

const ADMIN_EMAIL = 'sampath12082@gmail.com';
const ADMIN_PASSWORD = 'Admin@123';

async function loginViaUI(page: Page) {
  await page.goto('/login');
  await page.fill('input[type="email"]', ADMIN_EMAIL);
  await page.fill('input[type="password"]', ADMIN_PASSWORD);
  await page.click('button[type="submit"]');
  await page.waitForURL('/', { timeout: 15000 });
}

test.describe('UI Rendering — Dashboard', () => {
  test.beforeEach(async ({ page }) => {
    await loginViaUI(page);
  });

  test('dashboard shows Total Funds section', async ({ page }) => {
    await expect(page.getByText('Total Funds')).toBeVisible({ timeout: 20000 });
  });

  test('dashboard shows all 4 summary sections in correct order', async ({ page }) => {
    await expect(page.getByText('Total Funds')).toBeVisible({ timeout: 20000 });
    const sections = page.locator('h3');
    const texts: string[] = [];
    for (let i = 0; i < await sections.count(); i++) {
      texts.push((await sections.nth(i).textContent() || '').trim());
    }
    const order = ['Total Funds', 'Portfolio', 'Mutual Funds', 'Groww Account'];
    const indices = order.map(s => texts.findIndex(t => t.toUpperCase().includes(s.toUpperCase())));
    for (let i = 1; i < indices.length; i++) {
      expect(indices[i]).toBeGreaterThan(indices[i - 1]);
    }
  });

  test('Total Funds shows key metrics', async ({ page }) => {
    await expect(page.getByText('Total Invested')).toBeVisible({ timeout: 20000 });
    await expect(page.getByText('Net Worth')).toBeVisible();
    await expect(page.getByText('Current Value').first()).toBeVisible();
  });

  test('Portfolio section shows P&L fields', async ({ page }) => {
    await expect(page.getByText('Deposited')).toBeVisible({ timeout: 20000 });
    await expect(page.getByText('Unrealized P&L').first()).toBeVisible();
    await expect(page.getByText('Realized P&L')).toBeVisible();
    await expect(page.getByText('Day Change')).toBeVisible();
  });

  test('dashboard shows Today\'s Positions section', async ({ page }) => {
    await expect(page.getByText('Today\'s Positions')).toBeVisible({ timeout: 20000 });
  });

  test('dashboard shows Sector Allocation', async ({ page }) => {
    await expect(page.getByText('Sector Allocation')).toBeVisible({ timeout: 20000 });
  });

  test('dashboard shows Trading Signals', async ({ page }) => {
    await expect(page.getByText('Trading Signals')).toBeVisible({ timeout: 20000 });
  });

  test('dashboard does not show NaN or undefined', async ({ page }) => {
    await page.waitForTimeout(3000);
    const content = await page.textContent('body');
    expect(content).not.toContain('NaN');
    expect(content).not.toContain('undefined');
  });

  test('no console errors on dashboard', async ({ page }) => {
    const errors: string[] = [];
    page.on('pageerror', (err) => errors.push(err.message));
    await page.waitForTimeout(5000);
    expect(errors.length).toBe(0);
  });
});

test.describe('UI Rendering — Sidebar', () => {
  test.beforeEach(async ({ page }) => {
    await loginViaUI(page);
  });

  test('sidebar shows all navigation links', async ({ page }) => {
    const nav = page.locator('nav');
    await expect(nav.getByText('Dashboard')).toBeVisible();
    await expect(nav.getByText('Holdings')).toBeVisible();
    await expect(nav.getByText('Transactions')).toBeVisible();
    await expect(nav.getByText('Stocks')).toBeVisible();
    await expect(nav.getByText('Mutual Funds')).toBeVisible();
    await expect(nav.getByText('Performance')).toBeVisible();
    await expect(nav.getByText('Help & Support')).toBeVisible();
  });

  test('admin user sees admin links in sidebar', async ({ page }) => {
    const nav = page.locator('nav');
    await expect(nav.getByText('User Management')).toBeVisible();
    await expect(nav.getByText('Support Tickets')).toBeVisible();
  });

  test('header shows user name and logout', async ({ page }) => {
    await expect(page.getByText('Sampat Kumar')).toBeVisible();
    await expect(page.getByText('Logout')).toBeVisible();
  });
});

test.describe('UI Rendering — Page Navigation', () => {
  test.beforeEach(async ({ page }) => {
    await loginViaUI(page);
  });

  test('holdings page renders table', async ({ page }) => {
    await page.click('nav >> text=Holdings');
    await expect(page.locator('h1')).toContainText('Holdings');
    await expect(page.locator('table')).toBeVisible({ timeout: 10000 });
  });

  test('transactions page renders analytics', async ({ page }) => {
    await page.click('nav >> text=Transactions');
    await expect(page.locator('h1')).toContainText('Transactions');
    await expect(page.getByText('Fund Flow')).toBeVisible({ timeout: 10000 });
  });

  test('stocks page renders table', async ({ page }) => {
    await page.click('nav >> text=Stocks');
    await expect(page.locator('h1')).toContainText('Stocks');
    await expect(page.locator('table')).toBeVisible({ timeout: 10000 });
  });

  test('mutual funds page renders holdings', async ({ page }) => {
    await page.click('nav >> text=Mutual Funds');
    await expect(page.locator('h1')).toContainText('Mutual Funds');
  });

  test('help page renders FAQ', async ({ page }) => {
    await page.click('nav >> text=Help');
    await expect(page.locator('h1')).toContainText('Help');
    await expect(page.getByText('Frequently Asked Questions')).toBeVisible({ timeout: 10000 });
  });

  test('admin page renders user table', async ({ page }) => {
    await page.click('nav >> text=User Management');
    await expect(page.locator('h1')).toContainText('Admin');
    await expect(page.locator('table')).toBeVisible({ timeout: 10000 });
  });

  test('profile page renders form', async ({ page }) => {
    await page.click('text=Sampat Kumar');
    await expect(page.locator('h1')).toContainText('Profile');
    await expect(page.getByText('Personal Details')).toBeVisible({ timeout: 10000 });
  });

  test('logout redirects to login', async ({ page }) => {
    await page.click('text=Logout');
    await expect(page).toHaveURL('/login', { timeout: 5000 });
  });
});
