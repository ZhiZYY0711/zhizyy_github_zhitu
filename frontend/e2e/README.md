# Playwright 自动化测试文档

## 📋 测试套件概览

已为智途平台创建了完整的端到端自动化测试套件，覆盖以下模块：

### 测试文件列表

1. **login.spec.ts** - 登录功能测试
   - 页面加载验证
   - 四种角色登录流程（学生、企业、高校、平台管理员）
   - 表单验证

2. **student.spec.ts** - 学生端功能测试
   - 仪表盘加载
   - 培训、实习、成长页面导航
   - 页面间流畅性测试

3. **enterprise.spec.ts** - 企业端功能测试
   - 仪表盘加载
   - 招聘、人才库、培训、实习管理、导师管理、数据分析页面导航

4. **college.spec.ts** - 高校端功能测试
   - 仪表盘加载
   - 教学管理、就业管理、企业关系、预警系统页面导航

5. **platform.spec.ts** - 平台管理端功能测试
   - 仪表盘加载
   - 主数据管理、审核管理、资源管理、系统监控、日志管理页面导航

6. **auth.spec.ts** - 认证和权限测试
   - 未登录访问保护
   - 跨角色访问控制
   - 登录状态持久化

7. **visual.spec.ts** - 视觉回归测试
   - 登录页面截图
   - 学生仪表盘截图
   - 响应式设计测试（移动端、平板）

## 🚀 运行测试

### 前提条件

确保开发服务器正在运行：
```bash
npm run dev
```

### 运行所有测试

```bash
npm run test:e2e
```

### UI 模式（推荐）

可视化界面，方便调试：
```bash
npm run test:e2e:ui
```

### 有头模式

查看浏览器实际操作：
```bash
npm run test:e2e:headed
```

### 调试模式

逐步调试测试：
```bash
npm run test:e2e:debug
```

### 运行特定测试文件

```bash
npx playwright test login.spec.ts
```

### 运行特定测试用例

```bash
npx playwright test -g "学生角色登录成功"
```

## 📊 测试报告

测试完成后，查看 HTML 报告：
```bash
npx playwright show-report
```

## 📸 截图输出

所有截图保存在 `e2e/screenshots/` 目录：
- `login-page.png` - 登录页面
- `student-dashboard.png` - 学生仪表盘
- `enterprise-dashboard.png` - 企业仪表盘
- `college-dashboard.png` - 高校仪表盘
- `platform-dashboard.png` - 平台管理仪表盘
- `login-mobile.png` - 移动端登录页
- `login-tablet.png` - 平板登录页

## 🔧 配置说明

### playwright.config.ts

- **baseURL**: http://localhost:3000
- **浏览器**: Chromium (可扩展到 Firefox、WebKit)
- **失败重试**: CI 环境 2 次，本地 0 次
- **截图**: 仅失败时
- **追踪**: 首次重试时
- **自动启动**: 开发服务器

## 📝 测试覆盖范围

### ✅ 已覆盖

- [x] 登录流程（4 种角色）
- [x] 页面导航（所有主要页面）
- [x] 基础权限控制
- [x] 视觉回归测试
- [x] 响应式设计验证

### 🔄 待扩展

- [ ] 表单提交测试
- [ ] 数据交互测试
- [ ] API Mock 测试
- [ ] 性能测试
- [ ] 可访问性测试

## 🎯 最佳实践

1. **测试隔离**: 每个测试独立运行，使用 `beforeEach` 设置初始状态
2. **等待策略**: 使用 `waitForURL` 和 `waitForLoadState` 确保页面加载完成
3. **截图记录**: 关键页面自动截图，便于视觉验证
4. **选择器策略**: 优先使用文本选择器，其次 ID，避免脆弱的 CSS 选择器

## 🐛 调试技巧

### 查看测试执行过程

```bash
npm run test:e2e:headed
```

### 使用 Playwright Inspector

```bash
npm run test:e2e:debug
```

### 查看失败截图

失败的测试会自动截图，保存在 `test-results/` 目录

### 查看追踪记录

```bash
npx playwright show-trace test-results/[test-name]/trace.zip
```

## 📚 扩展测试

### 添加新测试文件

在 `e2e/` 目录创建 `*.spec.ts` 文件：

```typescript
import { test, expect } from '@playwright/test';

test.describe('新功能测试', () => {
  test('测试用例', async ({ page }) => {
    await page.goto('/your-page');
    // 测试逻辑
  });
});
```

### 添加测试辅助函数

创建 `e2e/helpers.ts`：

```typescript
export async function login(page, role: string) {
  await page.goto('/login');
  await page.click('[id="role"]');
  await page.click(`text=${role}`);
  await page.fill('#username', 'test');
  await page.fill('#password', 'test');
  await page.click('button[type="submit"]');
}
```

## 🔗 相关资源

- [Playwright 官方文档](https://playwright.dev/)
- [测试最佳实践](https://playwright.dev/docs/best-practices)
- [选择器指南](https://playwright.dev/docs/selectors)
