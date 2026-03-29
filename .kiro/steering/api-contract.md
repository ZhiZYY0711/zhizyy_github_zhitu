---
title: API Interface Contract
inclusion: always
---

# API Interface Contract — React/TS + Spring Cloud

这份文件是前后端接口的"宪法"，Kiro 生成任何 API 相关代码时必须遵守。

---

## 技术栈

- **前端**: React + TypeScript（axios 请求，React Query 管理状态）
- **后端**: Spring Cloud（Jackson 序列化，统一响应封装）

---

## 统一响应结构

后端所有接口返回格式：

```typescript
interface ApiResult<T> {
  code: number      // 200 = 成功，其他为错误
  message: string
  data: T
}
```

前端取值路径：`response.data.data`（axios 自带一层 `.data`）

分页接口返回：

```typescript
interface ApiResult<Page<T>> {
  data: {
    content: T[]
    totalElements: number
    totalPages: number
    number: number   // ⚠️ 0-indexed，不是 1
    size: number
    first: boolean
    last: boolean
  }
}
```

---

## 类型映射规则（必须遵守）

| Java 类型 | TypeScript 类型 | 说明 |
|-----------|----------------|------|
| `Long` / `long` | `string` | ⚠️ 不能用 number，超出 JS 安全整数范围 |
| `Integer` / `int` | `number` | ✅ 安全 |
| `String` | `string` | ✅ |
| `Boolean` | `boolean` | ✅ |
| `BigDecimal` | `string` | 金额/精度字段，后端序列化为字符串 |
| `LocalDateTime` | `string` | ISO 8601 格式（Jackson 已配置） |
| `LocalDate` | `string` | 如 `"2024-01-15"` |
| `List<T>` | `T[]` | ✅ |
| `Map<String, V>` | `Record<string, V>` | ✅ |
| `Optional<T>` | `T \| null` | ✅ |
| 无 `@NotNull` 的字段 | `T \| null` 或 `T?` | Java 字段默认可为 null |

---

## 命名规范

- 后端 Jackson 使用默认 **camelCase**（无全局 snake_case 策略）
- 前端 TypeScript 字段名必须与 Java DTO 字段名完全一致
- 如有特殊字段用 `@JsonProperty`，前端对应字段名以注解值为准

---

## ID 字段规则

**所有实体 ID（userId, orderId, etc.）在 TypeScript 中一律用 `string`**

```typescript
// ✅ 正确
interface User {
  id: string
  deptId: string
}

// ❌ 错误
interface User {
  id: number
  deptId: number
}
```

---

## 枚举规则

Java enum 默认序列化为大写字符串：

```typescript
// Java: enum OrderStatus { PENDING, PAID, CANCELLED }
type OrderStatus = 'PENDING' | 'PAID' | 'CANCELLED'
```

---

## 日期规则

Jackson 配置：`write-dates-as-timestamps: false`

所有日期字段类型为 `string`（ISO 8601），不用 `number` 或 `Date`。

---

## Gateway 路由

```
前端请求 → http://localhost:8080/api/{service-name}/{path}
```

---

## 自查清单

生成 API 相关代码后，必须检查：

- [ ] ID 字段是否用 `string` 而非 `number`
- [ ] 日期字段是否为 `string`
- [ ] 响应解包路径是否正确（`res.data.data`）
- [ ] 分页 `number` 字段是否为 0-indexed
- [ ] 可空 Java 字段是否有 `| null`
- [ ] 枚举值是否大写字符串
- [ ] 请求体字段名与 Java DTO 是否完全一致
