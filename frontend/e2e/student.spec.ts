import { test, expect } from '@playwright/test';

test.describe('学生端功能测试', () => {
  test.beforeEach(async ({ page }) => {
    // 登录为学生
    await page.goto('/login');
    await page.click('[id="role"]');
    await page.waitForSelector('[role="option"]');
    await page.click('[role="option"]:has-text("学生端")');
    await page.fill('#username', 'student01');
    await page.fill('#password', 'password');
    await page.click('button[type="submit"]');

    // 等待跳转完成
    await page.waitForURL(/\/student\/dashboard/, { timeout: 10000 });
  });

  test('学生仪表盘加载', async ({ page }) => {
    // 验证在仪表盘页面
    await expect(page).toHaveURL(/\/student\/dashboard/);

    // 等待页面内容加载
    await page.waitForLoadState('networkidle');

    // 截图保存
    await page.screenshot({ path: 'e2e/screenshots/student-dashboard.png', fullPage: true });
  });

  test('导航到培训页面', async ({ page }) => {
    // 点击培训菜单
    await page.click('text=培训');

    // 验证 URL 变化
    await expect(page).toHaveURL(/\/student\/training/);

    // 等待页面加载
    await page.waitForLoadState('networkidle');
  });

  test('导航到实习页面', async ({ page }) => {
    await page.click('text=实习');
    await expect(page).toHaveURL(/\/student\/internship/);
  });

  test('导航到成长页面', async ({ page }) => {
    await page.click('text=成长');
    await expect(page).toHaveURL(/\/student\/growth/);
  });

  test('页面间导航流畅性', async ({ page }) => {
    // 测试多个页面间的导航
    const pages = [
      { name: '培训', url: /\/student\/training/ },
      { name: '实习', url: /\/student\/internship/ },
      { name: '成长', url: /\/student\/growth/ },
      { name: '仪表盘', url: /\/student\/dashboard/ },
    ];

    for (const { name, url } of pages) {
      await page.click(`text=${name}`);
      await expect(page).toHaveURL(url);
      await page.waitForLoadState('networkidle');
    }
  });
});
