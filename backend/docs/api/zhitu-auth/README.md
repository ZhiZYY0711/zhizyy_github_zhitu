# 智途认证授权服务 API 文档

## 服务概述

**服务名称**: zhitu-auth  
**服务描述**: 认证授权服务，提供用户登录、令牌管理、权限验证等核心安全功能  
**服务版本**: 1.0.0  
**基础路径**: `/auth`

## 功能范围

智途认证授权服务是整个平台的安全基础设施，负责：

- **用户认证**: 支持多种登录方式（用户名密码、手机验证码、第三方登录）
- **令牌管理**: JWT 令牌的生成、刷新、验证和撤销
- **权限验证**: 基于角色的访问控制（RBAC）
- **会话管理**: 用户会话的创建、维护和销毁
- **安全审计**: 登录日志、操作日志的记录和查询

## 主要 API 端点

### AuthController - 认证控制器

| 端点 | 方法 | 描述 |
|------|------|------|
| `/auth/login` | POST | 用户登录 |
| `/auth/logout` | POST | 用户登出 |
| `/auth/refresh` | POST | 刷新访问令牌 |
| `/auth/verify` | GET | 验证令牌有效性 |
| `/auth/captcha` | GET | 获取图形验证码 |
| `/auth/sms/code` | POST | 发送短信验证码 |

## 认证要求

### JWT Bearer Token

所有需要认证的接口都需要在请求头中携带 JWT Bearer Token：

```http
Authorization: Bearer <your-jwt-token>
```

### 令牌获取

通过 `/auth/login` 接口登录成功后，响应中会包含访问令牌（access_token）和刷新令牌（refresh_token）：

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expires_in": 7200,
    "token_type": "Bearer"
  }
}
```

### 令牌刷新

访问令牌过期后，可使用刷新令牌获取新的访问令牌：

```bash
curl -X POST http://localhost:8080/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refresh_token": "your-refresh-token"}'
```

## 使用示例

### 用户登录

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "123456",
    "captcha": "abcd",
    "captchaKey": "uuid-key"
  }'
```

### 验证令牌

```bash
curl -X GET http://localhost:8080/auth/verify \
  -H "Authorization: Bearer your-jwt-token"
```

### 用户登出

```bash
curl -X POST http://localhost:8080/auth/logout \
  -H "Authorization: Bearer your-jwt-token"
```

## OpenAPI 规范

完整的 OpenAPI 3.0 规范文档请参考：
- [OpenAPI JSON](../openapi/zhitu-auth-openapi.json)
- [OpenAPI YAML](../openapi/zhitu-auth-openapi.yaml)

## Swagger UI

在开发和测试环境中，可以通过以下地址访问交互式 API 文档：

- **直接访问**: http://localhost:8080/swagger-ui.html
- **通过网关**: http://localhost:9999/swagger-ui.html (选择 zhitu-auth 服务)

## 错误码说明

| 错误码 | 描述 | 解决方案 |
|--------|------|----------|
| 401 | 未授权 - 令牌无效或已过期 | 重新登录或刷新令牌 |
| 403 | 禁止访问 - 权限不足 | 联系管理员分配相应权限 |
| 429 | 请求过于频繁 | 稍后重试 |
| 1001 | 用户名或密码错误 | 检查登录凭证 |
| 1002 | 验证码错误或已过期 | 重新获取验证码 |
| 1003 | 账号已被锁定 | 联系管理员解锁 |
| 1004 | 令牌已被撤销 | 重新登录 |

## 相关文档

- [用户指南](../../guides/authentication-guide.md)
- [安全最佳实践](../../guides/security-best-practices.md)
- [API 变更日志](../CHANGELOG.md)

## 技术支持

如有问题或建议，请联系：
- 技术支持邮箱: support@zhitu.com
- 开发团队: dev@zhitu.com
