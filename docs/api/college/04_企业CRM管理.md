# API 接口设计 - 高校端 (College Client) - 企业CRM管理

> **文档说明**: 高校端-企业合作与资质管理 API
> **服务**: `portal-college-service`, `user-service`
> **模块**: 1.3 高校管理驾驶舱 - 企业CRM系统

## 1. 企业库管理

### 1.1 合作企业列表
- **URL**: `GET /api/portal-college/v1/crm/enterprises`
- **Query**: 
  - `level=strategic` (strategic战略/core核心/normal普通)
  - `status=active`
  - `industry=IT`
- **Response**:
  ```json
  {
    "records": [
      {
        "id": "ent_001",
        "name": "腾讯科技",
        "industry": "互联网",
        "level": "strategic",
        "contact_person": "王HR",
        "phone": "139...",
        "active_interns": 15, // 在岗实习生数
        "total_hired": 50 // 累计录用数
      }
    ]
  }
  ```

### 1.2 企业详情
- **URL**: `GET /api/portal-college/v1/crm/enterprises/{id}`
- **Response**: 企业基础信息、资质文件(营业执照)、历史合作记录、提供的岗位列表。

## 2. 资质审核与评级

### 2.1 企业入驻审核
- **URL**: `GET /api/portal-college/v1/crm/audits`
- **Query**: `status=pending`
- **Response**: 待审核的企业注册申请列表。

- **URL**: `POST /api/portal-college/v1/crm/audits/{id}`
- **Request Body**:
  ```json
  {
    "action": "approve", // approve, reject
    "comment": "资质合规"
  }
  ```

### 2.2 调整合作等级
- **URL**: `PUT /api/portal-college/v1/crm/enterprises/{id}/level`
- **Request Body**:
  ```json
  {
    "level": "strategic",
    "reason": "该企业连续3年接收实习生超过50人，且转正率高"
  }
  ```

## 3. 访企拓岗记录

### 3.1 添加走访记录
- **URL**: `POST /api/portal-college/v1/crm/visits`
- **Request Body**:
  ```json
  {
    "enterprise_id": "ent_001",
    "visit_date": "2024-05-20",
    "visitors": ["张院长", "李辅导员"],
    "content": "洽谈2025届校招合作...",
    "photos": ["url1", "url2"]
  }
  ```

### 3.2 走访记录列表
- **URL**: `GET /api/portal-college/v1/crm/visits`
- **Query**: `enterprise_id=...`
