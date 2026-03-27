# 智途高校端服务 API 文档

## 服务概述

**服务名称**: zhitu-college  
**服务描述**: 高校端服务，提供高校信息管理、专业管理、实习监管、培训管理、预警管理等功能  
**服务版本**: 1.0.0  
**基础路径**: `/college`

## 功能范围

智途高校端服务是面向高校管理人员的业务模块，负责：

- **高校信息管理**: 高校基本信息、院系设置、师资管理
- **专业管理**: 专业信息维护、专业设置、培养方案
- **实习监管**: 学生实习过程监督、实习报告审核、实习评价
- **培训管理**: 校内培训项目、培训计划、培训记录
- **预警管理**: 学生异常情况预警、风险监控、预警处理
- **用户管理**: 高校端用户（教师、辅导员等）的管理

## 主要 API 端点

### CollegeController - 高校信息管理

| 端点 | 方法 | 描述 |
|------|------|------|
| `/college/info/list` | GET | 查询高校列表 |
| `/college/info/{id}` | GET | 获取高校详情 |
| `/college/info` | POST | 创建高校信息 |
| `/college/info` | PUT | 更新高校信息 |
| `/college/info/{id}` | DELETE | 删除高校信息 |
| `/college/info/statistics` | GET | 获取高校统计数据 |

### MajorController - 专业管理

| 端点 | 方法 | 描述 |
|------|------|------|
| `/college/major/list` | GET | 查询专业列表 |
| `/college/major/{id}` | GET | 获取专业详情 |
| `/college/major` | POST | 创建专业 |
| `/college/major` | PUT | 更新专业信息 |
| `/college/major/{id}` | DELETE | 删除专业 |
| `/college/major/tree` | GET | 获取专业树形结构 |

### CollegeInternshipOversightController - 实习监管

| 端点 | 方法 | 描述 |
|------|------|------|
| `/college/internship/oversight/list` | GET | 查询实习监管列表 |
| `/college/internship/oversight/{id}` | GET | 获取实习监管详情 |
| `/college/internship/oversight/approve` | POST | 审批实习申请 |
| `/college/internship/oversight/report` | GET | 查看实习报告 |
| `/college/internship/oversight/evaluate` | POST | 评价学生实习 |
| `/college/internship/oversight/statistics` | GET | 实习统计数据 |

### CollegeTrainingController - 培训管理

| 端点 | 方法 | 描述 |
|------|------|------|
| `/college/training/list` | GET | 查询培训列表 |
| `/college/training/{id}` | GET | 获取培训详情 |
| `/college/training` | POST | 创建培训项目 |
| `/college/training` | PUT | 更新培训信息 |
| `/college/training/{id}` | DELETE | 删除培训项目 |
| `/college/training/enroll` | POST | 学生报名培训 |
| `/college/training/records` | GET | 查询培训记录 |

### CollegeWarningController - 预警管理

| 端点 | 方法 | 描述 |
|------|------|------|
| `/college/warning/list` | GET | 查询预警列表 |
| `/college/warning/{id}` | GET | 获取预警详情 |
| `/college/warning/handle` | POST | 处理预警 |
| `/college/warning/statistics` | GET | 预警统计数据 |
| `/college/warning/rules` | GET | 查询预警规则 |
| `/college/warning/rules` | POST | 配置预警规则 |

### CollegeUserController - 高校用户管理

| 端点 | 方法 | 描述 |
|------|------|------|
| `/college/user/list` | GET | 查询高校用户列表 |
| `/college/user/{id}` | GET | 获取用户详情 |
| `/college/user` | POST | 创建高校用户 |
| `/college/user` | PUT | 更新用户信息 |
| `/college/user/{id}` | DELETE | 删除用户 |
| `/college/user/import` | POST | 批量导入用户 |

## 认证要求

### JWT Bearer Token

所有接口都需要在请求头中携带有效的 JWT Bearer Token：

```http
Authorization: Bearer <your-jwt-token>
```

### 权限要求

不同的接口需要不同的权限，请确保当前用户具有相应的角色权限：

- **高校信息管理**: 需要 `college:info:*` 权限
- **专业管理**: 需要 `college:major:*` 权限
- **实习监管**: 需要 `college:internship:*` 权限
- **培训管理**: 需要 `college:training:*` 权限
- **预警管理**: 需要 `college:warning:*` 权限
- **用户管理**: 需要 `college:user:*` 权限

## 使用示例

### 查询专业列表

```bash
curl -X GET "http://localhost:8082/college/major/list?pageNum=1&pageSize=10" \
  -H "Authorization: Bearer your-jwt-token"
```

### 创建专业

```bash
curl -X POST http://localhost:8082/college/major \
  -H "Authorization: Bearer your-jwt-token" \
  -H "Content-Type: application/json" \
  -d '{
    "majorName": "计算机科学与技术",
    "majorCode": "080901",
    "collegeId": 1,
    "degree": "本科",
    "duration": 4,
    "description": "培养计算机科学与技术专业人才"
  }'
```

### 审批实习申请

```bash
curl -X POST http://localhost:8082/college/internship/oversight/approve \
  -H "Authorization: Bearer your-jwt-token" \
  -H "Content-Type: application/json" \
  -d '{
    "internshipId": 123,
    "status": "approved",
    "comment": "符合实习要求，同意实习"
  }'
```

### 处理预警

```bash
curl -X POST http://localhost:8082/college/warning/handle \
  -H "Authorization: Bearer your-jwt-token" \
  -H "Content-Type: application/json" \
  -d '{
    "warningId": 456,
    "handleResult": "已联系学生，情况正常",
    "handleStatus": "resolved"
  }'
```

## OpenAPI 规范

完整的 OpenAPI 3.0 规范文档请参考：
- [OpenAPI JSON](../openapi/zhitu-college-openapi.json)
- [OpenAPI YAML](../openapi/zhitu-college-openapi.yaml)

## Swagger UI

在开发和测试环境中，可以通过以下地址访问交互式 API 文档：

- **直接访问**: http://localhost:8082/swagger-ui.html
- **通过网关**: http://localhost:9999/swagger-ui.html (选择 zhitu-college 服务)

## 错误码说明

| 错误码 | 描述 | 解决方案 |
|--------|------|----------|
| 401 | 未授权 - 令牌无效或已过期 | 重新登录 |
| 403 | 禁止访问 - 权限不足 | 联系管理员分配权限 |
| 3001 | 高校信息不存在 | 检查高校 ID |
| 3002 | 专业代码已存在 | 使用其他专业代码 |
| 3003 | 实习申请不存在 | 检查实习申请 ID |
| 3004 | 实习已审批，无法重复操作 | 查看审批状态 |
| 3005 | 培训项目已满员 | 选择其他培训项目 |
| 3006 | 预警已处理 | 无需重复处理 |

## 业务流程

### 实习监管流程

1. 学生提交实习申请（通过学生端）
2. 高校审核实习申请（`/college/internship/oversight/approve`）
3. 实习过程中监督学生（`/college/internship/oversight/list`）
4. 查看学生实习报告（`/college/internship/oversight/report`）
5. 评价学生实习表现（`/college/internship/oversight/evaluate`）

### 预警处理流程

1. 系统自动触发预警或手动创建预警
2. 高校查看预警列表（`/college/warning/list`）
3. 查看预警详情（`/college/warning/{id}`）
4. 处理预警并记录结果（`/college/warning/handle`）
5. 查看预警统计（`/college/warning/statistics`）

## 相关文档

- [高校管理指南](../../guides/college-management-guide.md)
- [实习监管指南](../../guides/internship-oversight-guide.md)
- [预警系统配置](../../guides/warning-system-guide.md)
- [API 变更日志](../CHANGELOG.md)

## 技术支持

如有问题或建议，请联系：
- 技术支持邮箱: support@zhitu.com
- 开发团队: dev@zhitu.com
