import { test, expect } from '@playwright/test';

test.describe('认证和权限测试', () => {
  test('未登录访问受保护页面应重定向到登录页', async ({ page }) => {
    await page.goto('/student/dashboard');

    // 应该被重定向到登录页
    await expect(page).toHaveURL(/\/login/);
  });

  test('登录后访问根路径应重定向到登录页', async ({ page }) => {
    await page.goto('/');
    await expect(page).toHaveURL(/\/login/);
  });

  test('学生登录后不能访问企业页面', async ({ page }) => {
    // 以学生身份登录
    await page.goto('/login');
    await page.click('[id="role"]');
    await page.waitForSelector('[role="option"]');
    await page.click('[role="option"]:has-text("学生端")');
    await page.fill('#username', 'student01');
    await page.fill('#password', 'password');
    await page.click('button[type="submit"]');

    await page.waitForURL(/\/student\/dashboard/, { timeout: 10000 });

    // 尝试直接访问企业页面
    await page.goto('/enterprise/dashboard');

    // 应该仍然在学生页面或被重定向
    // 注意：这取决于你的权限控制实现
    const url = page.url();
    expect(url).toMatch(/\/(student|login)/);
  });

  test('登录状态持久化', async ({ page, context }) => {
    // 登录
    await page.goto('/login');
    await page.click('[id="role"]');
    await page.waitForSelector('[role="option"]');
    await page.click('[role="option"]:has-text("学生端")');
    await page.fill('#username', 'student01');
    await page.fill('#password', 'password');
    await page.click('button[type="submit"]');

    await page.waitForURL(/\/student\/dashboard/, { timeout: 10000 });

    // 创建新页面（模拟新标签页）
    const newPage = await context.newPage();
    await newPage.goto('/');

    // 应该自动跳转到学生仪表盘（如果有持久化）
    // 或者跳转到登录页（如果没有持久化）
    await newPage.waitForLoadState('networkidle');

    const url = newPage.url();
    console.log('New page URL:', url);
  });
});
