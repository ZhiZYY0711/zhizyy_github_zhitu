import { test, expect } from '@playwright/test';

test.describe('高校端功能测试', () => {
  test.beforeEach(async ({ page }) => {
    // 登录为高校
    await page.goto('/login');
    await page.click('[id="role"]');
    await page.waitForSelector('[role="option"]');
    await page.click('[role="option"]:has-text("高校端")');
    await page.fill('#username', 'college01');
    await page.fill('#password', 'password');
    await page.click('button[type="submit"]');

    await page.waitForURL(/\/college\/dashboard/, { timeout: 10000 });
  });

  test('高校仪表盘加载', async ({ page }) => {
    await expect(page).toHaveURL(/\/college\/dashboard/);
    await page.waitForLoadState('networkidle');
    await page.screenshot({ path: 'e2e/screenshots/college-dashboard.png', fullPage: true });
  });

  test('导航到教学管理页面', async ({ page }) => {
    await page.click('text=教学管理');
    await expect(page).toHaveURL(/\/college\/teaching/);
  });

  test('导航到就业管理页面', async ({ page }) => {
    await page.click('text=就业管理');
    await expect(page).toHaveURL(/\/college\/employment/);
  });

  test('导航到企业关系管理页面', async ({ page }) => {
    await page.click('text=企业关系');
    await expect(page).toHaveURL(/\/college\/crm/);
  });

  test('导航到预警系统页面', async ({ page }) => {
    await page.click('text=预警系统');
    await expect(page).toHaveURL(/\/college\/warning/);
  });
});
