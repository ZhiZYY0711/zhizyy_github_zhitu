# 智途系统管理服务 API 文档

## 服务概述

**服务名称**: zhitu-system  
**服务描述**: 系统管理服务，提供用户管理、角色管理、菜单管理、字典管理、租户管理等系统基础功能  
**服务版本**: 1.0.0  
**基础路径**: `/system`

## 功能范围

智途系统管理服务是平台的核心管理模块，负责：

- **用户管理**: 用户的增删改查、密码重置、状态管理
- **角色管理**: 角色的创建、权限分配、角色绑定
- **菜单管理**: 系统菜单的配置、权限控制、动态路由
- **字典管理**: 系统字典数据的维护和查询
- **租户管理**: 多租户的创建、配置和管理
- **组织架构**: 部门、岗位等组织结构管理

## 主要 API 端点

### UserController - 用户管理

| 端点 | 方法 | 描述 |
|------|------|------|
| `/system/user/list` | GET | 查询用户列表 |
| `/system/user/{id}` | GET | 获取用户详情 |
| `/system/user` | POST | 创建用户 |
| `/system/user` | PUT | 更新用户信息 |
| `/system/user/{id}` | DELETE | 删除用户 |
| `/system/user/reset-password` | PUT | 重置用户密码 |
| `/system/user/status` | PUT | 修改用户状态 |

### RoleController - 角色管理

| 端点 | 方法 | 描述 |
|------|------|------|
| `/system/role/list` | GET | 查询角色列表 |
| `/system/role/{id}` | GET | 获取角色详情 |
| `/system/role` | POST | 创建角色 |
| `/system/role` | PUT | 更新角色信息 |
| `/system/role/{id}` | DELETE | 删除角色 |
| `/system/role/permissions` | PUT | 分配角色权限 |
| `/system/role/users` | GET | 查询角色下的用户 |

### MenuController - 菜单管理

| 端点 | 方法 | 描述 |
|------|------|------|
| `/system/menu/tree` | GET | 获取菜单树 |
| `/system/menu/{id}` | GET | 获取菜单详情 |
| `/system/menu` | POST | 创建菜单 |
| `/system/menu` | PUT | 更新菜单信息 |
| `/system/menu/{id}` | DELETE | 删除菜单 |
| `/system/menu/routes` | GET | 获取用户路由 |

### DictController - 字典管理

| 端点 | 方法 | 描述 |
|------|------|------|
| `/system/dict/type/list` | GET | 查询字典类型列表 |
| `/system/dict/data/list` | GET | 查询字典数据列表 |
| `/system/dict/type` | POST | 创建字典类型 |
| `/system/dict/data` | POST | 创建字典数据 |
| `/system/dict/type/{id}` | DELETE | 删除字典类型 |
| `/system/dict/data/{id}` | DELETE | 删除字典数据 |

### TenantController - 租户管理

| 端点 | 方法 | 描述 |
|------|------|------|
| `/system/tenant/list` | GET | 查询租户列表 |
| `/system/tenant/{id}` | GET | 获取租户详情 |
| `/system/tenant` | POST | 创建租户 |
| `/system/tenant` | PUT | 更新租户信息 |
| `/system/tenant/{id}` | DELETE | 删除租户 |
| `/system/tenant/status` | PUT | 修改租户状态 |

## 认证要求

### JWT Bearer Token

所有接口都需要在请求头中携带有效的 JWT Bearer Token：

```http
Authorization: Bearer <your-jwt-token>
```

### 权限要求

不同的接口需要不同的权限，请确保当前用户具有相应的角色权限：

- **用户管理**: 需要 `system:user:*` 权限
- **角色管理**: 需要 `system:role:*` 权限
- **菜单管理**: 需要 `system:menu:*` 权限
- **字典管理**: 需要 `system:dict:*` 权限
- **租户管理**: 需要 `system:tenant:*` 权限（超级管理员）

## 使用示例

### 查询用户列表

```bash
curl -X GET "http://localhost:8081/system/user/list?pageNum=1&pageSize=10" \
  -H "Authorization: Bearer your-jwt-token"
```

### 创建用户

```bash
curl -X POST http://localhost:8081/system/user \
  -H "Authorization: Bearer your-jwt-token" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "zhangsan",
    "nickname": "张三",
    "email": "zhangsan@example.com",
    "phone": "13800138000",
    "roleIds": [2, 3],
    "deptId": 100
  }'
```

### 分配角色权限

```bash
curl -X PUT http://localhost:8081/system/role/permissions \
  -H "Authorization: Bearer your-jwt-token" \
  -H "Content-Type: application/json" \
  -d '{
    "roleId": 2,
    "menuIds": [1, 2, 3, 100, 101]
  }'
```

### 获取菜单树

```bash
curl -X GET http://localhost:8081/system/menu/tree \
  -H "Authorization: Bearer your-jwt-token"
```

## OpenAPI 规范

完整的 OpenAPI 3.0 规范文档请参考：
- [OpenAPI JSON](../openapi/zhitu-system-openapi.json)
- [OpenAPI YAML](../openapi/zhitu-system-openapi.yaml)

## Swagger UI

在开发和测试环境中，可以通过以下地址访问交互式 API 文档：

- **直接访问**: http://localhost:8081/swagger-ui.html
- **通过网关**: http://localhost:9999/swagger-ui.html (选择 zhitu-system 服务)

## 错误码说明

| 错误码 | 描述 | 解决方案 |
|--------|------|----------|
| 401 | 未授权 - 令牌无效或已过期 | 重新登录 |
| 403 | 禁止访问 - 权限不足 | 联系管理员分配权限 |
| 2001 | 用户名已存在 | 使用其他用户名 |
| 2002 | 角色名已存在 | 使用其他角色名 |
| 2003 | 菜单权限标识已存在 | 使用其他权限标识 |
| 2004 | 不能删除超级管理员 | 无法删除系统内置账号 |
| 2005 | 不能删除当前登录用户 | 请使用其他账号操作 |
| 2006 | 租户已存在 | 使用其他租户标识 |

## 数据字典

系统中常用的字典类型：

- `sys_user_sex`: 用户性别（0-男，1-女，2-未知）
- `sys_user_status`: 用户状态（0-正常，1-停用）
- `sys_role_status`: 角色状态（0-正常，1-停用）
- `sys_menu_type`: 菜单类型（M-目录，C-菜单，F-按钮）
- `sys_show_hide`: 显示状态（0-显示，1-隐藏）

## 相关文档

- [用户管理指南](../../guides/user-management-guide.md)
- [权限配置指南](../../guides/permission-guide.md)
- [多租户配置](../../guides/multi-tenant-guide.md)
- [API 变更日志](../CHANGELOG.md)

## 技术支持

如有问题或建议，请联系：
- 技术支持邮箱: support@zhitu.com
- 开发团队: dev@zhitu.com
