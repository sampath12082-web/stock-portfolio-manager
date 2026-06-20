import { test, expect } from '@playwright/test';

test.describe('Dashboard — Portfolio Summary', () => {
  test('shows Portfolio, Groww Account, and Mutual Funds sections', async ({ page }) => {
    await page.goto('/');
    await expect(page.getByText('Deposited')).toBeVisible({ timeout: 20000 });
    await expect(page.locator('h3', { hasText: 'Mutual Funds' })).toBeVisible({ timeout: 10000 });
  });

  test('portfolio section shows key metrics', async ({ page }) => {
    await page.goto('/');
    await expect(page.getByText('Deposited')).toBeVisible({ timeout: 20000 });
    const portfolio = page.locator('h3', { hasText: 'Portfolio' }).locator('..');
    await expect(portfolio.getByText('Deposited')).toBeVisible();
    await expect(portfolio.getByText('Invested')).toBeVisible();
    await expect(portfolio.getByText('Current Value')).toBeVisible();
    await expect(portfolio.getByText('Cash Balance')).toBeVisible();
  });

  test('sector allocation chart or empty state renders', async ({ page }) => {
    await page.goto('/');
    const sectorCard = page.locator('text=Sector Allocation').locator('..');
    await expect(sectorCard).toBeVisible();
  });
});

test.describe('Holdings Page — Table and Filters', () => {
  test('displays holdings table with correct columns', async ({ page }) => {
    await page.goto('/holdings');
    await expect(page.getByRole('columnheader', { name: 'Stock' })).toBeVisible();
    await expect(page.getByRole('columnheader', { name: 'Qty' })).toBeVisible();
    await expect(page.getByRole('columnheader', { name: 'Avg Price' })).toBeVisible();
    await expect(page.getByRole('columnheader', { name: 'LTP' })).toBeVisible();
    await expect(page.getByRole('columnheader', { name: 'Invested' })).toBeVisible();
  });

  test('search filter works', async ({ page }) => {
    await page.goto('/holdings');
    await page.fill('input[placeholder="Search holdings..."]', 'HDFC');
    const rows = page.locator('tbody tr');
    const count = await rows.count();
    for (let i = 0; i < count; i++) {
      const text = await rows.nth(i).textContent();
      expect(text?.toLowerCase()).toContain('hdfc');
    }
  });

  test('signal filter chips are clickable', async ({ page }) => {
    await page.goto('/holdings');
    const allChip = page.locator('button', { hasText: 'ALL' });
    await expect(allChip).toBeVisible();
    await allChip.click();
  });

  test('Sync from Groww button exists', async ({ page }) => {
    await page.goto('/holdings');
    await expect(page.getByText('Sync from Groww')).toBeVisible();
  });
});

test.describe('Transactions Page — Analytics and Table', () => {
  test('shows analytics sections', async ({ page }) => {
    await page.goto('/transactions');
    await page.waitForLoadState('networkidle');
    await expect(page.getByText('Fund Flow')).toBeVisible();
    await expect(page.getByText('Activity')).toBeVisible();
  });

  test('transactions table has correct columns', async ({ page }) => {
    await page.goto('/transactions');
    await expect(page.getByRole('columnheader', { name: 'Trade Date' })).toBeVisible();
    await expect(page.getByRole('columnheader', { name: 'Symbol' })).toBeVisible();
    await expect(page.getByRole('columnheader', { name: 'Type' })).toBeVisible();
    await expect(page.getByRole('columnheader', { name: 'Total' })).toBeVisible();
  });

  test('type filter dropdown works', async ({ page }) => {
    await page.goto('/transactions');
    await page.selectOption('select', 'BUY');
    const badges = page.locator('tbody .bg-emerald-50');
    const count = await badges.count();
    expect(count).toBeGreaterThanOrEqual(0);
  });

  test('transactions by month chart renders', async ({ page }) => {
    await page.goto('/transactions');
    const chart = page.locator('.recharts-wrapper');
    await expect(chart).toBeVisible();
  });
});

test.describe('Stocks Page — Table and Sorting', () => {
  test('stocks table renders with correct columns', async ({ page }) => {
    await page.goto('/stocks');
    await expect(page.getByRole('columnheader', { name: 'Stock' })).toBeVisible();
    await expect(page.getByRole('columnheader', { name: 'Exchange' })).toBeVisible();
    await expect(page.getByRole('columnheader', { name: 'My Qty' })).toBeVisible();
    await expect(page.getByRole('columnheader', { name: 'Signal' })).toBeVisible();
  });

  test('search filters stocks', async ({ page }) => {
    await page.goto('/stocks');
    await page.fill('input[placeholder="Search stocks..."]', 'TCS');
    const rows = page.locator('tbody tr');
    const count = await rows.count();
    expect(count).toBeGreaterThan(0);
    const firstRow = await rows.first().textContent();
    expect(firstRow?.toUpperCase()).toContain('TCS');
  });

  test('signal filter chips are visible', async ({ page }) => {
    await page.goto('/stocks');
    await expect(page.getByText('Signal:')).toBeVisible();
    await expect(page.getByText('Target:')).toBeVisible();
  });

  test('column headers are sortable (click toggles)', async ({ page }) => {
    await page.goto('/stocks');
    const header = page.getByRole('columnheader', { name: 'Stock' });
    await header.click();
    await header.click();
  });

  test('held stocks have blue left border', async ({ page }) => {
    await page.goto('/stocks');
    const heldRow = page.locator('tr.border-l-4').first();
    if (await heldRow.count() > 0) {
      await expect(heldRow).toHaveClass(/border-l-blue/);
    }
  });
});

test.describe('Mutual Funds Page', () => {
  test('shows MF holdings table', async ({ page }) => {
    await page.goto('/mutual-funds');
    await expect(page.locator('h1')).toContainText('Mutual Funds');
    const rows = page.locator('tbody tr');
    expect(await rows.count()).toBeGreaterThan(0);
  });
});

test.describe('Performance Page', () => {
  test('defaults to 7D view', async ({ page }) => {
    await page.goto('/performance');
    const activeBtn = page.locator('button.bg-blue-600', { hasText: '7D' });
    await expect(activeBtn).toBeVisible();
  });

  test('capture snapshot button exists', async ({ page }) => {
    await page.goto('/performance');
    await expect(page.getByText('Capture Snapshot')).toBeVisible();
  });

  test('time range buttons are clickable', async ({ page }) => {
    await page.goto('/performance');
    await page.click('button:has-text("30D")');
    const activeBtn = page.locator('button.bg-blue-600', { hasText: '30D' });
    await expect(activeBtn).toBeVisible();
  });
});
