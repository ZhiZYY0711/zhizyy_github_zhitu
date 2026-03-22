import { test, expect } from '@playwright/test';
import { login } from './helpers';

test.describe('登录功能测试', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
  });

  test('页面加载正确', async ({ page }) => {
    // 检查页面标题
    await expect(page.locator('h1')).toContainText('智途平台');
    await expect(page.locator('text=产教融合一体化管理系统')).toBeVisible();

    // 检查登录表单元素
    await expect(page.locator('label:has-text("登录角色")')).toBeVisible();
    await expect(page.locator('label:has-text("用户名")')).toBeVisible();
    await expect(page.locator('label:has-text("密码")')).toBeVisible();
    await expect(page.locator('button[type="submit"]')).toBeVisible();
  });

  test('学生角色登录成功', async ({ page }) => {
    await login(page, '学生端', 'student01', 'password');
    await expect(page).toHaveURL(/\/student\/dashboard/, { timeout: 10000 });
  });

  test('企业角色登录成功', async ({ page }) => {
    await login(page, '企业端', 'enterprise01', 'password');
    await expect(page).toHaveURL(/\/enterprise\/dashboard/, { timeout: 10000 });
  });

  test('高校角色登录成功', async ({ page }) => {
    await login(page, '高校端', 'college01', 'password');
    await expect(page).toHaveURL(/\/college\/dashboard/, { timeout: 10000 });
  });

  test('平台管理员角色登录成功', async ({ page }) => {
    await login(page, '平台端', 'admin01', 'password');
    await expect(page).toHaveURL(/\/platform\/dashboard/, { timeout: 10000 });
  });

  test('空表单提交验证', async ({ page }) => {
    await page.click('button[type="submit"]');

    // 应该仍在登录页面
    await expect(page).toHaveURL(/\/login/);
  });
});
