# Playwright 自动化测试套件

## ✅ 已完成

我已经为你的智途平台创建了一套完整的 Playwright 自动化测试：

### 📦 安装的内容

1. **Playwright** - 端到端测试框架
2. **Chromium 浏览器** - 用于运行测试
3. **8 个测试文件** - 覆盖所有主要功能

### 📁 测试文件

```
frontend/e2e/
├── login.spec.ts       - 登录功能测试（4种角色）
├── student.spec.ts     - 学生端测试
├── enterprise.spec.ts  - 企业端测试
├── college.spec.ts     - 高校端测试
├── platform.spec.ts    - 平台管理端测试
├── auth.spec.ts        - 认证和权限测试
├── visual.spec.ts      - 视觉回归测试
├── helpers.ts          - 测试辅助函数
└── README.md           - 详细文档
```

### 🎯 测试覆盖

- ✅ 登录页面加载
- ✅ 4种角色登录（学生、企业、高校、平台）
- ✅ 页面导航测试
- ✅ 权限控制测试
- ✅ 视觉回归测试（截图）
- ✅ 响应式设计测试

### 🚀 如何运行

#### 1. UI 模式（推荐 - 可视化界面）

```bash
cd frontend
npm run test:e2e:ui
```

这会打开 Playwright 的可视化界面，你可以：
- 选择要运行的测试
- 实时查看测试执行
- 查看每一步的截图
- 调试失败的测试

#### 2. 命令行模式

```bash
npm run test:e2e
```

#### 3. 有头模式（查看浏览器）

```bash
npm run test:e2e:headed
```

#### 4. 调试模式

```bash
npm run test:e2e:debug
```

### 📊 查看测试报告

测试完成后：

```bash
npx playwright show-report
```

### 📸 自动截图

所有关键页面会自动截图，保存在：
```
frontend/e2e/screenshots/
```

### ⚙️ 配置

测试配置在 `playwright.config.ts`：
- 自动启动开发服务器
- 失败时自动截图
- 失败时记录追踪信息

### 📝 测试凭据

已更新为正确的测试账号：
- 用户名格式：`student01`, `enterprise01`, `college01`, `admin01`
- 密码：`password`

### 🔧 下一步

1. **运行测试**：`npm run test:e2e:ui`
2. **查看结果**：测试会自动截图并生成报告
3. **扩展测试**：在 `e2e/` 目录添加更多测试文件

### 📚 文档

详细文档见：
- `frontend/e2e/README.md` - 完整测试文档
- `frontend/TESTING.md` - 快速开始指南

### ⚠️ 注意事项

部分测试可能需要调整，因为：
1. 登录后的跳转行为需要验证
2. 某些页面可能还在开发中
3. 导航菜单的文本可能需要调整

建议先用 UI 模式运行测试，可以直观地看到每个步骤的执行情况。
