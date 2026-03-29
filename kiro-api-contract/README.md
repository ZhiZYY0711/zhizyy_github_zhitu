# Kiro API Contract Checker

适用于 **React + TypeScript 前端 / Spring Cloud 后端** 项目的 Kiro 配置。

## 目录结构

```
.kiro/
├── steering/
│   ├── api-contract.md          # 接口规范（每次交互自动加载）
│   └── check-api-contract.md   # 手动触发检查指令（slash command）
└── hooks/
    ├── api-contract-check.kiro.hook    # 保存前端 service 文件时自动检查
    └── dto-to-ts-sync.kiro.hook        # 保存 Java DTO 时自动生成 TS 类型
```

## 安装方式

将 `.kiro` 文件夹复制到你的项目根目录：

```bash
cp -r .kiro /your-project-root/
```

## 使用方式

### 1. 自动检查（Hooks）

开箱即用，保存以下文件时自动触发：

- 保存 `src/services/**/*.ts` 或 `src/api/**/*.ts` → 自动检查接口一致性
- 保存 `**/*DTO.java` / `**/*Request.java` 等 → 自动生成 TypeScript 类型建议

> 如果你的目录结构不同，修改 `hooks/*.kiro.hook` 中的 `patterns` 字段。

### 2. 手动检查（Slash Command）

在 Kiro 聊天框输入 `/` 即可看到 `check-api-contract`，选择后粘贴代码即可检查。

或者直接说："检查这个接口是否匹配" 并粘贴前后端代码。

### 3. 规范始终在线（Steering）

`api-contract.md` 设置为 `inclusion: always`，Kiro 每次生成代码时都会自动遵守其中的类型规则，无需重复提醒。

## 自定义配置

修改 `.kiro/steering/api-contract.md` 中以下部分以适配你的项目：

```markdown
## 统一响应结构   ← 改成你们实际的 wrapper 格式
## Gateway 路由   ← 改成你们实际的 gateway 地址和路径前缀
## 类型映射规则   ← 根据 Jackson 实际配置调整
```

## Hook 文件路径适配

如果你的项目结构不是 `src/services/`，修改 hook 文件中的 patterns：

```json
"patterns": [
  "src/api/**/*.ts",      // 改成你的实际路径
  "src/request/**/*.ts"
]
```
