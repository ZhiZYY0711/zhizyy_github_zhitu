import { test, expect } from '@playwright/test';

test.describe('视觉回归测试', () => {
  test('登录页面视觉快照', async ({ page }) => {
    await page.goto('/login');
    await page.waitForLoadState('networkidle');

    // 截取全页面截图
    await page.screenshot({
      path: 'e2e/screenshots/login-page.png',
      fullPage: true
    });

    // 验证关键元素可见
    await expect(page.locator('h1')).toBeVisible();
  });

  test('学生仪表盘视觉快照', async ({ page }) => {
    // 登录
    await page.goto('/login');
    await page.click('[id="role"]');
    await page.waitForSelector('[role="option"]');
    await page.click('[role="option"]:has-text("学生端")');
    await page.fill('#username', 'student01');
    await page.fill('#password', 'password');
    await page.click('button[type="submit"]');

    await page.waitForURL(/\/student\/dashboard/, { timeout: 10000 });
    await page.waitForLoadState('networkidle');

    // 等待动画完成
    await page.waitForTimeout(1000);

    await page.screenshot({
      path: 'e2e/screenshots/student-dashboard-full.png',
      fullPage: true
    });
  });

  test('响应式设计 - 移动端视图', async ({ page }) => {
    // 设置移动端视口
    await page.setViewportSize({ width: 375, height: 667 });

    await page.goto('/login');
    await page.waitForLoadState('networkidle');

    await page.screenshot({
      path: 'e2e/screenshots/login-mobile.png',
      fullPage: true
    });
  });

  test('响应式设计 - 平板视图', async ({ page }) => {
    // 设置平板视口
    await page.setViewportSize({ width: 768, height: 1024 });

    await page.goto('/login');
    await page.waitForLoadState('networkidle');

    await page.screenshot({
      path: 'e2e/screenshots/login-tablet.png',
      fullPage: true
    });
  });
});
