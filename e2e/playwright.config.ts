import { defineConfig } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  timeout: 60000,
  retries: 1,
  use: {
    baseURL: 'http://localhost:8081',
    screenshot: 'only-on-failure',
    trace: 'on-first-retry',
    navigationTimeout: 15000,
    actionTimeout: 15000,
  },
  expect: {
    timeout: 10000,
  },
  projects: [
    { name: 'chromium', use: { browserName: 'chromium' } },
  ],
});
