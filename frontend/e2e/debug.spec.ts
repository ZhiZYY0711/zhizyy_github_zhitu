import { test, expect } from '@playwright/test';

test('调试登录流程', async ({ page }) => {
  // 打开登录页
  await page.goto('/login');
  console.log('1. 页面加载完成');

  // 选择角色
  await page.click('[id="role"]');
  await page.waitForSelector('[role="option"]');
  console.log('2. 角色选择器打开');

  await page.click('[role="option"]:has-text("学生端")');
  console.log('3. 选择了学生端');

  // 输入用户名和密码
  await page.fill('#username', 'student001');
  await page.fill('#password', 'password123');
  console.log('4. 填写了用户名和密码');

  // 点击登录按钮
  await page.click('button[type="submit"]');
  console.log('5. 点击了登录按钮');

  // 等待一段时间看看会发生什么
  await page.waitForTimeout(3000);
  console.log('6. 当前 URL:', page.url());

  // 截图
  await page.screenshot({ path: 'e2e/screenshots/debug-after-login.png', fullPage: true });
});
