import { test, expect } from '@playwright/test';

test.describe('企业端功能测试', () => {
  test.beforeEach(async ({ page }) => {
    // 登录为企业
    await page.goto('/login');
    await page.click('[id="role"]');
    await page.waitForSelector('[role="option"]');
    await page.click('[role="option"]:has-text("企业端")');
    await page.fill('#username', 'enterprise01');
    await page.fill('#password', 'password');
    await page.click('button[type="submit"]');

    await page.waitForURL(/\/enterprise\/dashboard/, { timeout: 10000 });
  });

  test('企业仪表盘加载', async ({ page }) => {
    await expect(page).toHaveURL(/\/enterprise\/dashboard/);
    await page.waitForLoadState('networkidle');
    await page.screenshot({ path: 'e2e/screenshots/enterprise-dashboard.png', fullPage: true });
  });

  test('导航到招聘页面', async ({ page }) => {
    await page.click('text=招聘');
    await expect(page).toHaveURL(/\/enterprise\/recruitment/);
  });

  test('导航到人才库页面', async ({ page }) => {
    await page.click('text=人才库');
    await expect(page).toHaveURL(/\/enterprise\/talent-pool/);
  });

  test('导航到培训页面', async ({ page }) => {
    await page.click('text=培训');
    await expect(page).toHaveURL(/\/enterprise\/training/);
  });

  test('导航到实习管理页面', async ({ page }) => {
    await page.click('text=实习管理');
    await expect(page).toHaveURL(/\/enterprise\/internship/);
  });

  test('导航到导师管理页面', async ({ page }) => {
    await page.click('text=导师管理');
    await expect(page).toHaveURL(/\/enterprise\/mentor/);
  });

  test('导航到数据分析页面', async ({ page }) => {
    await page.click('text=数据分析');
    await expect(page).toHaveURL(/\/enterprise\/analytics/);
  });
});
