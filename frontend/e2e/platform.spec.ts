import { test, expect } from '@playwright/test';

test.describe('平台管理端功能测试', () => {
  test.beforeEach(async ({ page }) => {
    // 登录为平台管理员
    await page.goto('/login');
    await page.click('[id="role"]');
    await page.waitForSelector('[role="option"]');
    await page.click('[role="option"]:has-text("平台端")');
    await page.fill('#username', 'admin01');
    await page.fill('#password', 'password');
    await page.click('button[type="submit"]');

    await page.waitForURL(/\/platform\/dashboard/, { timeout: 10000 });
  });

  test('平台仪表盘加载', async ({ page }) => {
    await expect(page).toHaveURL(/\/platform\/dashboard/);
    await page.waitForLoadState('networkidle');
    await page.screenshot({ path: 'e2e/screenshots/platform-dashboard.png', fullPage: true });
  });

  test('导航到主数据管理页面', async ({ page }) => {
    await page.click('text=主数据管理');
    await expect(page).toHaveURL(/\/platform\/master-data/);
  });

  test('导航到审核管理页面', async ({ page }) => {
    await page.click('text=审核管理');
    await expect(page).toHaveURL(/\/platform\/audit/);
  });

  test('导航到资源管理页面', async ({ page }) => {
    await page.click('text=资源管理');
    await expect(page).toHaveURL(/\/platform\/resources/);
  });

  test('导航到系统监控页面', async ({ page }) => {
    await page.click('text=系统监控');
    await expect(page).toHaveURL(/\/platform\/monitor/);
  });

  test('导航到日志管理页面', async ({ page }) => {
    await page.click('text=日志管理');
    await expect(page).toHaveURL(/\/platform\/logs/);
  });
});
