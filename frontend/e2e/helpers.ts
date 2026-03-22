import { Page } from '@playwright/test';

/**
 * 登录辅助函数
 * @param page Playwright Page 对象
 * @param role 角色类型：'学生端' | '企业端' | '高校端' | '平台端'
 * @param username 用户名
 * @param password 密码
 */
export async function login(
  page: Page,
  role: '学生端' | '企业端' | '高校端' | '平台端',
  username: string = 'student01',
  password: string = 'password'
) {
  await page.goto('/login');

  // 选择角色
  await page.click('[id="role"]');
  await page.waitForSelector('[role="option"]', { timeout: 5000 });
  await page.click(`[role="option"]:has-text("${role}")`);

  // 输入用户名和密码
  await page.fill('#username', username);
  await page.fill('#password', password);

  // 点击登录按钮
  await page.click('button[type="submit"]');

  // 等待 URL 变化（离开登录页）
  await page.waitForFunction(
    () => !window.location.pathname.includes('/login'),
    { timeout: 10000 }
  );

  // 等待页面加载完成
  await page.waitForLoadState('networkidle');
}

/**
 * 等待页面完全加载
 */
export async function waitForPageLoad(page: Page) {
  await page.waitForLoadState('networkidle');
  // 额外等待一点时间确保动画完成
  await page.waitForTimeout(500);
}

/**
 * 截图辅助函数
 */
export async function takeScreenshot(page: Page, name: string) {
  await page.screenshot({
    path: `e2e/screenshots/${name}.png`,
    fullPage: true
  });
}
