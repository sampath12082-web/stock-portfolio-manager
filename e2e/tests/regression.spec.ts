import { test, expect } from '@playwright/test';

test.describe('Regression — API Data Consistency', () => {
  test('dashboard invested matches holdings sum', async ({ request }) => {
    const dashResp = await request.get('/api/dashboard');
    const dash = await dashResp.json();
    const holdResp = await request.get('/api/holdings');
    const holdings = await holdResp.json();
    const activeSum = holdings
      .filter((h: { quantity: number }) => h.quantity > 0)
      .reduce((s: number, h: { investedAmount: number }) => s + h.investedAmount, 0);
    expect(Math.abs(dash.investedAmount - activeSum)).toBeLessThan(1);
  });

  test('dashboard currentValue matches holdings sum', async ({ request }) => {
    const dashResp = await request.get('/api/dashboard');
    const dash = await dashResp.json();
    const holdResp = await request.get('/api/holdings');
    const holdings = await holdResp.json();
    const activeSum = holdings
      .filter((h: { quantity: number }) => h.quantity > 0)
      .reduce((s: number, h: { currentValue: number; investedAmount: number }) => s + (h.currentValue ?? h.investedAmount), 0);
    expect(Math.abs(dash.currentValue - activeSum)).toBeLessThan(1);
  });

  test('unrealizedPnL = currentValue - investedAmount', async ({ request }) => {
    const resp = await request.get('/api/dashboard');
    const d = await resp.json();
    expect(Math.abs(d.unrealizedPnL - (d.currentValue - d.investedAmount))).toBeLessThan(1);
  });

  test('totalDeposited matches sum of DEPOSIT transactions', async ({ request }) => {
    const dashResp = await request.get('/api/dashboard');
    const dash = await dashResp.json();
    const txnResp = await request.get('/api/transactions');
    const txns = await txnResp.json();
    const depositSum = txns
      .filter((t: { transactionType: string }) => t.transactionType === 'DEPOSIT')
      .reduce((s: number, t: { totalAmount: number }) => s + t.totalAmount, 0);
    expect(Math.abs(dash.totalDeposited - depositSum)).toBeLessThan(1);
  });

  test('transaction analytics counts match actual', async ({ request }) => {
    const analyticsResp = await request.get('/api/transactions/analytics');
    const a = await analyticsResp.json();
    const txnResp = await request.get('/api/transactions');
    const txns = await txnResp.json();
    expect(a.totalTransactions).toBe(txns.length);
    const buys = txns.filter((t: { transactionType: string }) => t.transactionType === 'BUY');
    expect(a.buyCount).toBe(buys.length);
  });

  test('MF holdings count matches API', async ({ request }) => {
    const resp = await request.get('/api/mf/holdings');
    const holdings = await resp.json();
    expect(holdings.length).toBe(12);
  });

  test('MF transactions count matches API', async ({ request }) => {
    const resp = await request.get('/api/mf/transactions');
    const txns = await resp.json();
    expect(txns.length).toBe(279);
  });

  test('all transactions have tradeDate populated', async ({ request }) => {
    const resp = await request.get('/api/transactions');
    const txns = await resp.json();
    const missing = txns.filter((t: { tradeDate: string | null }) => !t.tradeDate);
    expect(missing.length).toBe(0);
  });

  test('all stock transactions have tradeType (CNC/MIS)', async ({ request }) => {
    const resp = await request.get('/api/transactions');
    const txns = await resp.json();
    const stockTxns = txns.filter((t: { transactionType: string }) => t.transactionType === 'BUY' || t.transactionType === 'SELL');
    const untyped = stockTxns.filter((t: { tradeType: string | null }) => !t.tradeType || t.tradeType === 'UNKNOWN');
    expect(untyped.length).toBe(0);
  });

  test('performance snapshot matches dashboard investment', async ({ request }) => {
    const snapResp = await request.get('/api/performance/today');
    const snap = await snapResp.json();
    const dashResp = await request.get('/api/dashboard');
    const dash = await dashResp.json();
    expect(Math.abs(snap.totalInvestment - dash.investedAmount)).toBeLessThan(1);
  });

  test('sector allocation sums to ~100%', async ({ request }) => {
    const resp = await request.get('/api/portfolio/allocation');
    const sectors = await resp.json();
    if (sectors.length > 0) {
      const totalPct = sectors.reduce((s: number, sec: { percentage: number }) => s + sec.percentage, 0);
      expect(Math.abs(totalPct - 100)).toBeLessThan(1);
    }
  });

  test('all SPA routes return 200', async ({ request }) => {
    for (const path of ['/', '/holdings', '/transactions', '/stocks', '/mutual-funds', '/performance']) {
      const resp = await request.get(path);
      expect(resp.status()).toBe(200);
    }
  });
});

test.describe('Regression — UI Rendering', () => {
  test('dashboard does not show NaN or undefined', async ({ page }) => {
    await page.goto('/');
    await page.waitForTimeout(2000);
    const content = await page.textContent('body');
    expect(content).not.toContain('NaN');
    expect(content).not.toContain('undefined');
  });

  test('holdings page does not crash (no blank page)', async ({ page }) => {
    await page.goto('/holdings');
    await page.waitForTimeout(2000);
    const h1 = page.locator('h1');
    await expect(h1).toContainText('Holdings');
  });

  test('stocks page does not show NaN or undefined', async ({ page }) => {
    await page.goto('/stocks');
    await page.waitForTimeout(2000);
    const content = await page.textContent('body');
    expect(content).not.toContain('NaN');
    expect(content).not.toContain('undefined');
  });

  test('no console errors on dashboard', async ({ page }) => {
    const errors: string[] = [];
    page.on('pageerror', (err) => errors.push(err.message));
    await page.goto('/');
    await page.waitForTimeout(3000);
    expect(errors.length).toBe(0);
  });

  test('no console errors on holdings', async ({ page }) => {
    const errors: string[] = [];
    page.on('pageerror', (err) => errors.push(err.message));
    await page.goto('/holdings');
    await page.waitForTimeout(3000);
    expect(errors.length).toBe(0);
  });
});
