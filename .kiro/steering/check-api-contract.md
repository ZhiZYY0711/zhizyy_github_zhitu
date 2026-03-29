---
title: Check API Contract
inclusion: manual
---

# 接口规范自查指令

当我说"检查接口"或使用 `/check-api-contract` 时，执行以下检查：

## 检查步骤

### 1. 收集两侧代码

如果上下文中有以下内容，直接使用；否则请我提供：
- Java DTO 类（Request/Response）
- Java Controller 方法签名
- TypeScript interface / type 定义
- TypeScript service / hook 调用代码

### 2. 逐字段对比

对每个字段输出对比表：

| 字段名 | Java 类型 | TS 类型 | 名称匹配 | 类型兼容 | 问题 |
|-------|-----------|---------|---------|---------|------|

图例：✅ 正常 | ⚠️ 警告 | ❌ 不匹配（必然 bug）

### 3. 重点检查项

按以下顺序逐一检查：

**命名**
- Java 字段名 vs TypeScript 字段名是否完全一致
- 是否有 `@JsonProperty` 改变了序列化名称
- 枚举值大小写是否匹配

**类型**
- `Long` → 必须是 TS `string`，不能是 `number`
- `BigDecimal` → 确认是 `string` 还是 `number`
- `LocalDateTime/LocalDate` → 必须是 TS `string`
- `Optional<T>` / 无 `@NotNull` 的字段 → 必须有 `| null`

**结构**
- 响应解包：`response.data` 还是 `response.data.data`
- 分页：`content[]` 字段名，`number`（0起）而非 `page`（1起）
- 是否有多余的包装层

**请求**
- POST/PUT body 字段名匹配
- 文件上传是否用 `FormData`
- 路径参数 `{id}` 与前端 URL 拼接是否一致
- 查询参数 `@RequestParam` 名称匹配

### 4. 输出格式

```
## 接口检查结果：[接口名称]

### 📋 字段对比表
[表格]

### ✅ 正常字段 (N 个)
### ⚠️ 警告 (N 个)  
[说明风险]
### ❌ 必然 Bug (N 个)
[说明原因 + 修复方案]

### 🔧 修正后的代码
[Java DTO]
[TypeScript interface]
[TypeScript service 调用]
```

### 5. 预防建议

检查完后，建议是否需要：
- 更新 `.kiro/steering/api-contract.md` 中的规范
- 添加 TypeScript 类型到统一的 `types/api.ts` 文件
