# 智途平台运营端服务 API 文档

## 服务概述

**服务名称**: zhitu-platform  
**服务描述**: 平台运营端服务，提供审核管理、监控管理、门户管理、系统配置等平台级功能  
**服务版本**: 1.0.0  
**基础路径**: `/platform`

## 功能范围

智途平台运营端服务是面向平台管理员的核心管理模块，负责：

- **审核管理**: 企业认证审核、高校认证审核、内容审核
- **监控管理**: 系统运行监控、业务数据监控、异常告警
- **门户管理**: 平台门户内容管理、公告发布、帮助文档
- **系统配置**: 平台参数配置、业务规则设置、系统维护
- **数据统计**: 平台整体数据统计、报表生成、数据分析
- **运营管理**: 活动管理、推广管理、用户运营

## 主要 API 端点

### PlatformAuditController - 审核管理

| 端点 | 方法 | 描述 |
|------|------|------|
| `/platform/audit/enterprise/list` | GET | 查询企业审核列表 |
| `/platform/audit/enterprise/{id}` | GET | 获取企业审核详情 |
| `/platform/audit/enterprise/approve` | POST | 审批企业认证 |
| `/platform/audit/college/list` | GET | 查询高校审核列表 |
| `/platform/audit/college/approve` | POST | 审批高校认证 |
| `/platform/audit/content/list` | GET | 查询内容审核列表 |
| `/platform/audit/content/approve` | POST | 审批内容 |
| `/platform/audit/statistics` | GET | 审核统计数据 |

### PlatformMonitorController - 监控管理

| 端点 | 方法 | 描述 |
|------|------|------|
| `/platform/monitor/system/info` | GET | 获取系统信息 |
| `/platform/monitor/system/metrics` | GET | 获取系统指标 |
| `/platform/monitor/service/health` | GET | 服务健康检查 |
| `/platform/monitor/service/list` | GET | 查询服务列表 |
| `/platform/monitor/alert/list` | GET | 查询告警列表 |
| `/platform/monitor/alert/handle` | POST | 处理告警 |
| `/platform/monitor/log/list` | GET | 查询系统日志 |
| `/platform/monitor/statistics` | GET | 监控统计数据 |

### PlatformPortalController - 门户管理

| 端点 | 方法 | 描述 |
|------|------|------|
| `/platform/portal/banner/list` | GET | 查询轮播图列表 |
| `/platform/portal/banner` | POST | 创建轮播图 |
| `/platform/portal/banner` | PUT | 更新轮播图 |
| `/platform/portal/banner/{id}` | DELETE | 删除轮播图 |
| `/platform/portal/notice/list` | GET | 查询公告列表 |
| `/platform/portal/notice` | POST | 发布公告 |
| `/platform/portal/article/list` | GET | 查询文章列表 |
| `/platform/portal/article` | POST | 发布文章 |
| `/platform/portal/help/list` | GET | 查询帮助文档 |
| `/platform/portal/help` | POST | 创建帮助文档 |

### PlatformSystemController - 系统配置

| 端点 | 方法 | 描述 |
|------|------|------|
| `/platform/system/config/list` | GET | 查询配置列表 |
| `/platform/system/config/{key}` | GET | 获取配置值 |
| `/platform/system/config` | PUT | 更新配置 |
| `/platform/system/param/list` | GET | 查询参数列表 |
| `/platform/system/param` | PUT | 更新参数 |
| `/platform/system/cache/clear` | POST | 清除缓存 |
| `/platform/system/maintenance` | POST | 系统维护模式 |

## 认证要求

### JWT Bearer Token

所有接口都需要在请求头中携带有效的 JWT Bearer Token：

```http
Authorization: Bearer <your-jwt-token>
```

### 权限要求

平台运营端接口需要超级管理员或平台管理员权限：

- **审核管理**: 需要 `platform:audit:*` 权限
- **监控管理**: 需要 `platform:monitor:*` 权限
- **门户管理**: 需要 `platform:portal:*` 权限
- **系统配置**: 需要 `platform:system:*` 权限（超级管理员）

## 使用示例

### 审批企业认证

```bash
curl -X POST http://localhost:8084/platform/audit/enterprise/approve \
  -H "Authorization: Bearer your-jwt-token" \
  -H "Content-Type: application/json" \
  -d '{
    "auditId": 123,
    "status": "approved",
    "comment": "企业资质齐全，审核通过"
  }'
```

### 查询系统指标

```bash
curl -X GET http://localhost:8084/platform/monitor/system/metrics \
  -H "Authorization: Bearer your-jwt-token"
```

响应示例：
```json
{
  "code": 200,
  "data": {
    "cpu": {
      "usage": 45.2,
      "cores": 8
    },
    "memory": {
      "total": 16384,
      "used": 8192,
      "free": 8192,
      "usage": 50.0
    },
    "disk": {
      "total": 512000,
      "used": 256000,
      "free": 256000,
      "usage": 50.0
    },
    "jvm": {
      "heapUsed": 512,
      "heapMax": 2048,
      "threadCount": 150
    }
  }
}
```

### 发布公告

```bash
curl -X POST http://localhost:8084/platform/portal/notice \
  -H "Authorization: Bearer your-jwt-token" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "系统维护通知",
    "content": "系统将于今晚22:00-24:00进行维护升级",
    "type": "system",
    "priority": "high",
    "publishTime": "2024-01-10 10:00:00"
  }'
```

### 更新系统配置

```bash
curl -X PUT http://localhost:8084/platform/system/config \
  -H "Authorization: Bearer your-jwt-token" \
  -H "Content-Type: application/json" \
  -d '{
    "key": "system.max.upload.size",
    "value": "10485760",
    "description": "最大上传文件大小（字节）"
  }'
```

### 处理告警

```bash
curl -X POST http://localhost:8084/platform/monitor/alert/handle \
  -H "Authorization: Bearer your-jwt-token" \
  -H "Content-Type: application/json" \
  -d '{
    "alertId": 789,
    "handleResult": "已重启服务，恢复正常",
    "handleStatus": "resolved"
  }'
```

## OpenAPI 规范

完整的 OpenAPI 3.0 规范文档请参考：
- [OpenAPI JSON](../openapi/zhitu-platform-openapi.json)
- [OpenAPI YAML](../openapi/zhitu-platform-openapi.yaml)

## Swagger UI

在开发和测试环境中，可以通过以下地址访问交互式 API 文档：

- **直接访问**: http://localhost:8084/swagger-ui.html
- **通过网关**: http://localhost:9999/swagger-ui.html (选择 zhitu-platform 服务)

## 错误码说明

| 错误码 | 描述 | 解决方案 |
|--------|------|----------|
| 401 | 未授权 - 令牌无效或已过期 | 重新登录 |
| 403 | 禁止访问 - 权限不足 | 需要平台管理员权限 |
| 5001 | 审核记录不存在 | 检查审核 ID |
| 5002 | 审核已处理 | 无法重复审核 |
| 5003 | 配置项不存在 | 检查配置键名 |
| 5004 | 配置值格式错误 | 检查配置值格式 |
| 5005 | 系统维护中 | 等待维护完成 |
| 5006 | 服务不可用 | 检查服务状态 |

## 监控指标说明

### 系统指标

- **CPU 使用率**: 当前 CPU 使用百分比
- **内存使用率**: 当前内存使用百分比
- **磁盘使用率**: 当前磁盘使用百分比
- **JVM 堆内存**: Java 虚拟机堆内存使用情况
- **线程数**: 当前活跃线程数量

### 业务指标

- **在线用户数**: 当前在线用户总数
- **请求 QPS**: 每秒请求数
- **平均响应时间**: 接口平均响应时间
- **错误率**: 请求错误率
- **数据库连接数**: 当前数据库连接数

### 告警级别

- **critical**: 严重告警，需要立即处理
- **high**: 高级告警，需要尽快处理
- **medium**: 中级告警，需要关注
- **low**: 低级告警，可延后处理

## 审核流程

### 企业认证审核

1. 企业提交认证申请（通过企业端）
2. 平台查看审核列表（`/platform/audit/enterprise/list`）
3. 查看企业详细信息（`/platform/audit/enterprise/{id}`）
4. 审批认证申请（`/platform/audit/enterprise/approve`）
5. 系统发送审核结果通知

### 内容审核

1. 用户发布内容（文章、动态等）
2. 系统自动触发审核或人工举报
3. 平台查看待审核内容（`/platform/audit/content/list`）
4. 审核内容合规性（`/platform/audit/content/approve`）
5. 处理违规内容（删除、警告等）

## 系统配置项

常用的系统配置项：

- `system.max.upload.size`: 最大上传文件大小（字节）
- `system.session.timeout`: 会话超时时间（秒）
- `system.password.strength`: 密码强度要求（weak/medium/strong）
- `system.captcha.enabled`: 是否启用验证码
- `system.maintenance.mode`: 系统维护模式
- `business.internship.max.duration`: 实习最长期限（天）
- `business.application.auto.expire`: 申请自动过期时间（天）

## 相关文档

- [平台管理指南](../../guides/platform-management-guide.md)
- [审核规则说明](../../guides/audit-rules-guide.md)
- [监控告警配置](../../guides/monitoring-guide.md)
- [系统配置说明](../../guides/system-config-guide.md)
- [API 变更日志](../CHANGELOG.md)

## 技术支持

如有问题或建议，请联系：
- 技术支持邮箱: support@zhitu.com
- 开发团队: dev@zhitu.com
