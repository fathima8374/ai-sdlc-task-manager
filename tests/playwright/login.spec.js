const path = require('node:path');
const { test, expect } = require('@playwright/test');

const fileUrl = 'file://' + path.join(__dirname, '..', '..', 'client', 'index.html');

test('page loads the login view and required controls', async ({ page }) => {
  await page.goto(fileUrl);
  await expect(page.locator('h1')).toHaveText('Sign in');
  await expect(page.locator('#username')).toBeVisible();
  await expect(page.locator('#password')).toBeVisible();
  await expect(page.getByRole('button', { name: 'Login' })).toBeVisible();
  await expect(page.getByRole('button', { name: 'Forgot Password' })).toBeVisible();
});

test('invalid login credentials display messages without leaking password data', async ({ page }) => {
  await page.goto(fileUrl);
  await page.locator('#username').fill('bad');
  await page.locator('#password').fill('short');
  await page.getByRole('button', { name: 'Login' }).click();

  const status = page.locator('#login-status');
  await expect(status).toContainText('Invalid user.');
  await expect(status).not.toContainText('short');
});

test('forgot password toggles to recovery flow and validates username', async ({ page }) => {
  await page.goto(fileUrl);
  await page.getByRole('button', { name: 'Forgot Password' }).click();

  await expect(page.locator('#recovery-view')).toBeVisible();
  await expect(page.locator('#recovery-username')).toBeVisible();

  await page.locator('#recovery-username').fill('bad');
  await page.getByRole('button', { name: 'Submit' }).click();

  await expect(page.locator('#recovery-status')).toContainText('Invalid user.');

  await page.locator('#recovery-username').fill('ValidUser');
  await page.getByRole('button', { name: 'Submit' }).click();
  await expect(page.locator('#recovery-status')).toHaveText('');
});

test('direct file loading includes the expected browser script structure', async ({ page }) => {
  await page.goto(fileUrl);
  await expect(page.locator('script[src="validation.js"]')).toHaveCount(1);
  await expect(page.locator('script[src="login.js"]')).toHaveCount(1);
});
