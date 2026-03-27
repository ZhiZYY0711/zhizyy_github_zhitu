# 智途企业端服务 API 文档

## 服务概述

**服务名称**: zhitu-enterprise  
**服务描述**: 企业端服务，提供企业信息管理、职位管理、申请管理、实习管理、导师管理、企业门户等功能  
**服务版本**: 1.0.0  
**基础路径**: `/enterprise`

## 功能范围

智途企业端服务是面向企业用户的业务模块，负责：

- **企业信息管理**: 企业基本信息、资质认证、企业文化展示
- **职位管理**: 实习职位发布、职位要求设置、职位状态管理
- **申请管理**: 学生实习申请处理、面试安排、录用管理
- **实习管理**: 在岗学生管理、实习考勤、实习评价、实习报告
- **导师管理**: 企业导师信息、导师分配、指导记录
- **企业门户**: 企业展示页面、企业动态、招聘信息

## 主要 API 端点

### EnterpriseProfileController - 企业信息管理

| 端点 | 方法 | 描述 |
|------|------|------|
| `/enterprise/profile/list` | GET | 查询企业列表 |
| `/enterprise/profile/{id}` | GET | 获取企业详情 |
| `/enterprise/profile` | POST | 创建企业信息 |
| `/enterprise/profile` | PUT | 更新企业信息 |
| `/enterprise/profile/{id}` | DELETE | 删除企业信息 |
| `/enterprise/profile/verify` | POST | 提交企业认证 |
| `/enterprise/profile/statistics` | GET | 获取企业统计数据 |

### EnterpriseJobController - 职位管理

| 端点 | 方法 | 描述 |
|------|------|------|
| `/enterprise/job/list` | GET | 查询职位列表 |
| `/enterprise/job/{id}` | GET | 获取职位详情 |
| `/enterprise/job` | POST | 发布职位 |
| `/enterprise/job` | PUT | 更新职位信息 |
| `/enterprise/job/{id}` | DELETE | 删除职位 |
| `/enterprise/job/publish` | PUT | 发布/下架职位 |
| `/enterprise/job/statistics` | GET | 职位统计数据 |

### EnterpriseApplicationController - 申请管理

| 端点 | 方法 | 描述 |
|------|------|------|
| `/enterprise/application/list` | GET | 查询申请列表 |
| `/enterprise/application/{id}` | GET | 获取申请详情 |
| `/enterprise/application/review` | POST | 审核申请 |
| `/enterprise/application/interview` | POST | 安排面试 |
| `/enterprise/application/offer` | POST | 发送录用通知 |
| `/enterprise/application/reject` | POST | 拒绝申请 |
| `/enterprise/application/statistics` | GET | 申请统计数据 |

### InternshipManageController - 实习管理

| 端点 | 方法 | 描述 |
|------|------|------|
| `/enterprise/internship/list` | GET | 查询实习生列表 |
| `/enterprise/internship/{id}` | GET | 获取实习生详情 |
| `/enterprise/internship/attendance` | POST | 记录考勤 |
| `/enterprise/internship/evaluate` | POST | 评价实习生 |
| `/enterprise/internship/report` | GET | 查看实习报告 |
| `/enterprise/internship/terminate` | POST | 终止实习 |
| `/enterprise/internship/statistics` | GET | 实习统计数据 |

### EnterpriseMentorController - 导师管理

| 端点 | 方法 | 描述 |
|------|------|------|
| `/enterprise/mentor/list` | GET | 查询导师列表 |
| `/enterprise/mentor/{id}` | GET | 获取导师详情 |
| `/enterprise/mentor` | POST | 添加导师 |
| `/enterprise/mentor` | PUT | 更新导师信息 |
| `/enterprise/mentor/{id}` | DELETE | 删除导师 |
| `/enterprise/mentor/assign` | POST | 分配导师 |
| `/enterprise/mentor/records` | GET | 查询指导记录 |

### EnterprisePortalController - 企业门户

| 端点 | 方法 | 描述 |
|------|------|------|
| `/enterprise/portal/info` | GET | 获取企业门户信息 |
| `/enterprise/portal/news` | GET | 查询企业动态 |
| `/enterprise/portal/news` | POST | 发布企业动态 |
| `/enterprise/portal/jobs` | GET | 查询招聘信息 |
| `/enterprise/portal/culture` | GET | 获取企业文化 |
| `/enterprise/portal/culture` | PUT | 更新企业文化 |

## 认证要求

### JWT Bearer Token

所有接口都需要在请求头中携带有效的 JWT Bearer Token：

```http
Authorization: Bearer <your-jwt-token>
```

### 权限要求

不同的接口需要不同的权限，请确保当前用户具有相应的角色权限：

- **企业信息管理**: 需要 `enterprise:profile:*` 权限
- **职位管理**: 需要 `enterprise:job:*` 权限
- **申请管理**: 需要 `enterprise:application:*` 权限
- **实习管理**: 需要 `enterprise:internship:*` 权限
- **导师管理**: 需要 `enterprise:mentor:*` 权限
- **企业门户**: 需要 `enterprise:portal:*` 权限

## 使用示例

### 发布职位

```bash
curl -X POST http://localhost:8083/enterprise/job \
  -H "Authorization: Bearer your-jwt-token" \
  -H "Content-Type: application/json" \
  -d '{
    "jobTitle": "Java 开发实习生",
    "jobType": "internship",
    "department": "技术部",
    "recruitNumber": 5,
    "requirements": "计算机相关专业，熟悉 Java",
    "salary": "3000-5000",
    "workLocation": "北京市海淀区",
    "description": "负责后端开发工作"
  }'
```

### 审核申请

```bash
curl -X POST http://localhost:8083/enterprise/application/review \
  -H "Authorization: Bearer your-jwt-token" \
  -H "Content-Type: application/json" \
  -d '{
    "applicationId": 123,
    "status": "interview",
    "comment": "简历符合要求，安排面试"
  }'
```

### 安排面试

```bash
curl -X POST http://localhost:8083/enterprise/application/interview \
  -H "Authorization: Bearer your-jwt-token" \
  -H "Content-Type: application/json" \
  -d '{
    "applicationId": 123,
    "interviewTime": "2024-01-15 14:00:00",
    "interviewLocation": "公司会议室A",
    "interviewType": "online",
    "interviewLink": "https://meeting.example.com/123456",
    "interviewer": "张经理"
  }'
```

### 评价实习生

```bash
curl -X POST http://localhost:8083/enterprise/internship/evaluate \
  -H "Authorization: Bearer your-jwt-token" \
  -H "Content-Type: application/json" \
  -d '{
    "internshipId": 456,
    "workAttitude": 90,
    "professionalSkill": 85,
    "teamwork": 88,
    "innovation": 82,
    "overallScore": 86,
    "comment": "工作认真负责，学习能力强"
  }'
```

## OpenAPI 规范

完整的 OpenAPI 3.0 规范文档请参考：
- [OpenAPI JSON](../openapi/zhitu-enterprise-openapi.json)
- [OpenAPI YAML](../openapi/zhitu-enterprise-openapi.yaml)

## Swagger UI

在开发和测试环境中，可以通过以下地址访问交互式 API 文档：

- **直接访问**: http://localhost:8083/swagger-ui.html
- **通过网关**: http://localhost:9999/swagger-ui.html (选择 zhitu-enterprise 服务)

## 错误码说明

| 错误码 | 描述 | 解决方案 |
|--------|------|----------|
| 401 | 未授权 - 令牌无效或已过期 | 重新登录 |
| 403 | 禁止访问 - 权限不足 | 联系管理员分配权限 |
| 4001 | 企业信息不存在 | 检查企业 ID |
| 4002 | 职位不存在 | 检查职位 ID |
| 4003 | 职位已下架 | 重新发布职位 |
| 4004 | 申请不存在 | 检查申请 ID |
| 4005 | 申请已处理 | 无法重复处理 |
| 4006 | 实习生不存在 | 检查实习生 ID |
| 4007 | 导师不存在 | 检查导师 ID |
| 4008 | 企业未认证 | 完成企业认证 |

## 业务流程

### 招聘流程

1. 企业发布职位（`/enterprise/job`）
2. 学生提交申请（通过学生端）
3. 企业查看申请列表（`/enterprise/application/list`）
4. 企业审核申请（`/enterprise/application/review`）
5. 安排面试（`/enterprise/application/interview`）
6. 发送录用通知（`/enterprise/application/offer`）

### 实习管理流程

1. 学生入职，创建实习记录
2. 分配企业导师（`/enterprise/mentor/assign`）
3. 记录日常考勤（`/enterprise/internship/attendance`）
4. 导师指导并记录（`/enterprise/mentor/records`）
5. 查看实习报告（`/enterprise/internship/report`）
6. 评价实习表现（`/enterprise/internship/evaluate`）

## 数据字典

系统中常用的字典类型：

- `job_type`: 职位类型（internship-实习，part_time-兼职，full_time-全职）
- `job_status`: 职位状态（draft-草稿，published-已发布，closed-已关闭）
- `application_status`: 申请状态（pending-待审核，interview-面试中，offered-已录用，rejected-已拒绝）
- `internship_status`: 实习状态（ongoing-进行中，completed-已完成，terminated-已终止）
- `interview_type`: 面试类型（online-线上，offline-线下，phone-电话）

## 相关文档

- [企业管理指南](../../guides/enterprise-management-guide.md)
- [招聘流程指南](../../guides/recruitment-guide.md)
- [实习管理指南](../../guides/internship-management-guide.md)
- [API 变更日志](../CHANGELOG.md)

## 技术支持

如有问题或建议，请联系：
- 技术支持邮箱: support@zhitu.com
- 开发团队: dev@zhitu.com
