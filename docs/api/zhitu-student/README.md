# 智途学生端服务 API 文档

## 服务概述

**服务名称**: zhitu-student  
**服务描述**: 学生端服务，提供学生门户、成长记录、职位浏览、实习申请、能力评估等功能  
**服务版本**: 1.0.0  
**基础路径**: `/student`

## 功能范围

智途学生端服务是面向学生用户的核心业务模块，负责：

- **学生门户**: 个人信息管理、简历管理、消息通知
- **成长记录**: 学习记录、实习经历、技能认证、荣誉奖项
- **职位浏览**: 实习职位搜索、职位详情查看、职位收藏
- **实习申请**: 在线申请实习、申请进度跟踪、面试管理
- **能力评估**: 能力雷达图、技能测评、职业规划建议
- **实习管理**: 实习打卡、实习日志、实习报告提交

## 主要 API 端点

### StudentPortalController - 学生门户

| 端点 | 方法 | 描述 |
|------|------|------|
| `/student/portal/profile` | GET | 获取个人信息 |
| `/student/portal/profile` | PUT | 更新个人信息 |
| `/student/portal/resume` | GET | 获取简历信息 |
| `/student/portal/resume` | PUT | 更新简历 |
| `/student/portal/avatar` | POST | 上传头像 |
| `/student/portal/messages` | GET | 查询消息列表 |
| `/student/portal/messages/read` | PUT | 标记消息已读 |
| `/student/portal/notifications` | GET | 查询通知列表 |

### GrowthController - 成长记录

| 端点 | 方法 | 描述 |
|------|------|------|
| `/student/growth/timeline` | GET | 获取成长时间线 |
| `/student/growth/experience/list` | GET | 查询实习经历 |
| `/student/growth/experience` | POST | 添加实习经历 |
| `/student/growth/experience` | PUT | 更新实习经历 |
| `/student/growth/experience/{id}` | DELETE | 删除实习经历 |
| `/student/growth/skill/list` | GET | 查询技能列表 |
| `/student/growth/skill` | POST | 添加技能 |
| `/student/growth/certificate/list` | GET | 查询证书列表 |
| `/student/growth/certificate` | POST | 上传证书 |
| `/student/growth/honor/list` | GET | 查询荣誉奖项 |
| `/student/growth/honor` | POST | 添加荣誉奖项 |
| `/student/growth/radar` | GET | 获取能力雷达图 |

### 职位相关接口

| 端点 | 方法 | 描述 |
|------|------|------|
| `/student/job/list` | GET | 搜索职位列表 |
| `/student/job/{id}` | GET | 获取职位详情 |
| `/student/job/recommend` | GET | 获取推荐职位 |
| `/student/job/favorite` | POST | 收藏职位 |
| `/student/job/favorite/list` | GET | 查询收藏列表 |
| `/student/job/favorite/{id}` | DELETE | 取消收藏 |

### 申请相关接口

| 端点 | 方法 | 描述 |
|------|------|------|
| `/student/application/apply` | POST | 申请职位 |
| `/student/application/list` | GET | 查询申请列表 |
| `/student/application/{id}` | GET | 获取申请详情 |
| `/student/application/cancel` | POST | 取消申请 |
| `/student/application/interview` | GET | 查询面试安排 |

### 实习相关接口

| 端点 | 方法 | 描述 |
|------|------|------|
| `/student/internship/current` | GET | 获取当前实习 |
| `/student/internship/checkin` | POST | 实习打卡 |
| `/student/internship/diary/list` | GET | 查询实习日志 |
| `/student/internship/diary` | POST | 提交实习日志 |
| `/student/internship/report` | POST | 提交实习报告 |
| `/student/internship/evaluate` | GET | 查看实习评价 |

## 认证要求

### JWT Bearer Token

所有接口都需要在请求头中携带有效的 JWT Bearer Token：

```http
Authorization: Bearer <your-jwt-token>
```

### 权限要求

学生端接口需要学生角色权限：

- **个人信息**: 需要 `student:profile:*` 权限
- **成长记录**: 需要 `student:growth:*` 权限
- **职位浏览**: 需要 `student:job:view` 权限
- **实习申请**: 需要 `student:application:*` 权限
- **实习管理**: 需要 `student:internship:*` 权限

## 使用示例

### 更新个人信息

```bash
curl -X PUT http://localhost:8085/student/portal/profile \
  -H "Authorization: Bearer your-jwt-token" \
  -H "Content-Type: application/json" \
  -d '{
    "nickname": "小明",
    "gender": "male",
    "birthday": "2000-01-01",
    "phone": "13800138000",
    "email": "xiaoming@example.com",
    "college": "北京大学",
    "major": "计算机科学与技术",
    "grade": "2020",
    "expectedGraduation": "2024-06"
  }'
```

### 搜索职位

```bash
curl -X GET "http://localhost:8085/student/job/list?keyword=Java&city=北京&pageNum=1&pageSize=10" \
  -H "Authorization: Bearer your-jwt-token"
```

### 申请职位

```bash
curl -X POST http://localhost:8085/student/application/apply \
  -H "Authorization: Bearer your-jwt-token" \
  -H "Content-Type: application/json" \
  -d '{
    "jobId": 123,
    "resumeId": 1,
    "coverLetter": "我对这个职位很感兴趣，希望能获得面试机会...",
    "expectedStartDate": "2024-02-01"
  }'
```

### 添加实习经历

```bash
curl -X POST http://localhost:8085/student/growth/experience \
  -H "Authorization: Bearer your-jwt-token" \
  -H "Content-Type: application/json" \
  -d '{
    "companyName": "某科技公司",
    "position": "Java 开发实习生",
    "startDate": "2023-07-01",
    "endDate": "2023-09-30",
    "description": "负责后端接口开发和维护",
    "skills": ["Java", "Spring Boot", "MySQL"]
  }'
```

### 获取能力雷达图

```bash
curl -X GET http://localhost:8085/student/growth/radar \
  -H "Authorization: Bearer your-jwt-token"
```

响应示例：
```json
{
  "code": 200,
  "data": {
    "dimensions": [
      {
        "name": "专业技能",
        "score": 85,
        "maxScore": 100
      },
      {
        "name": "沟通能力",
        "score": 78,
        "maxScore": 100
      },
      {
        "name": "团队协作",
        "score": 82,
        "maxScore": 100
      },
      {
        "name": "学习能力",
        "score": 90,
        "maxScore": 100
      },
      {
        "name": "创新思维",
        "score": 75,
        "maxScore": 100
      },
      {
        "name": "问题解决",
        "score": 80,
        "maxScore": 100
      }
    ],
    "overallScore": 81.7,
    "level": "良好",
    "suggestions": [
      "建议加强创新思维能力的培养",
      "可以多参与团队项目提升协作能力"
    ]
  }
}
```

### 提交实习日志

```bash
curl -X POST http://localhost:8085/student/internship/diary \
  -H "Authorization: Bearer your-jwt-token" \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2024-01-10",
    "content": "今天完成了用户管理模块的开发，学习了 Spring Security 的使用",
    "workHours": 8,
    "mood": "happy",
    "tags": ["开发", "学习"]
  }'
```

## OpenAPI 规范

完整的 OpenAPI 3.0 规范文档请参考：
- [OpenAPI JSON](../openapi/zhitu-student-openapi.json)
- [OpenAPI YAML](../openapi/zhitu-student-openapi.yaml)

## Swagger UI

在开发和测试环境中，可以通过以下地址访问交互式 API 文档：

- **直接访问**: http://localhost:8085/swagger-ui.html
- **通过网关**: http://localhost:9999/swagger-ui.html (选择 zhitu-student 服务)

## 错误码说明

| 错误码 | 描述 | 解决方案 |
|--------|------|----------|
| 401 | 未授权 - 令牌无效或已过期 | 重新登录 |
| 403 | 禁止访问 - 权限不足 | 需要学生角色权限 |
| 6001 | 简历不存在 | 先创建简历 |
| 6002 | 职位不存在 | 检查职位 ID |
| 6003 | 职位已下架 | 选择其他职位 |
| 6004 | 已申请该职位 | 无法重复申请 |
| 6005 | 申请不存在 | 检查申请 ID |
| 6006 | 无法取消申请 | 申请已被处理 |
| 6007 | 实习不存在 | 检查实习状态 |
| 6008 | 今日已打卡 | 无需重复打卡 |

## 业务流程

### 求职流程

1. 完善个人信息和简历（`/student/portal/profile`, `/student/portal/resume`）
2. 搜索职位（`/student/job/list`）
3. 查看职位详情（`/student/job/{id}`）
4. 收藏感兴趣的职位（`/student/job/favorite`）
5. 申请职位（`/student/application/apply`）
6. 跟踪申请进度（`/student/application/list`）
7. 查看面试安排（`/student/application/interview`）

### 实习流程

1. 收到录用通知，开始实习
2. 每日实习打卡（`/student/internship/checkin`）
3. 定期提交实习日志（`/student/internship/diary`）
4. 实习结束提交实习报告（`/student/internship/report`）
5. 查看企业评价（`/student/internship/evaluate`）
6. 添加到成长记录（`/student/growth/experience`）

### 成长记录流程

1. 添加实习经历（`/student/growth/experience`）
2. 添加技能标签（`/student/growth/skill`）
3. 上传证书（`/student/growth/certificate`）
4. 添加荣誉奖项（`/student/growth/honor`）
5. 查看成长时间线（`/student/growth/timeline`）
6. 查看能力雷达图（`/student/growth/radar`）

## 数据字典

系统中常用的字典类型：

- `student_gender`: 性别（male-男，female-女，other-其他）
- `education_level`: 学历（专科、本科、硕士、博士）
- `job_status`: 求职状态（seeking-求职中，employed-已就业，not_seeking-暂不求职）
- `application_status`: 申请状态（pending-待处理，interview-面试中，offered-已录用，rejected-已拒绝）
- `internship_status`: 实习状态（ongoing-进行中，completed-已完成，terminated-已终止）
- `skill_level`: 技能水平（beginner-初级，intermediate-中级，advanced-高级，expert-专家）

## 能力雷达图说明

能力雷达图从以下六个维度评估学生能力：

1. **专业技能**: 基于所学专业、技能标签、证书等计算
2. **沟通能力**: 基于面试反馈、实习评价等计算
3. **团队协作**: 基于团队项目经历、实习评价等计算
4. **学习能力**: 基于学习记录、技能增长速度等计算
5. **创新思维**: 基于项目经历、荣誉奖项等计算
6. **问题解决**: 基于实习表现、项目经历等计算

每个维度满分 100 分，系统会根据学生的各项数据自动计算得分。

## 相关文档

- [学生用户指南](../../guides/student-user-guide.md)
- [求职技巧指南](../../guides/job-seeking-guide.md)
- [实习管理指南](../../guides/student-internship-guide.md)
- [简历撰写指南](../../guides/resume-writing-guide.md)
- [API 变更日志](../CHANGELOG.md)

## 技术支持

如有问题或建议，请联系：
- 技术支持邮箱: support@zhitu.com
- 开发团队: dev@zhitu.com
