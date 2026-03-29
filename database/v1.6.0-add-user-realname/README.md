# v1.6.0 - 添加用户真实姓名字段

## 变更说明

为 `auth_center.sys_user` 表添加 `real_name` 字段，用于存储所有用户（企业员工、教师、平台管理员等）的真实姓名。

## 背景

当前系统中：
- `student_svc.student_info` 表有 `real_name` 字段存储学生姓名
- 但企业员工、教师等其他用户类型没有存储真实姓名的字段
- 导致跨服务查询时无法获取这些用户的姓名信息

## 影响范围

- `auth_center.sys_user` 表新增 `real_name` 字段
- 现有数据将 `username` 复制到 `real_name` 作为默认值
- 不影响现有功能，向后兼容

## 部署步骤

1. 执行 `upgrade.sql` 添加字段和迁移数据
2. 重启相关服务
3. 如需回滚，执行 `rollback.sql`

## 验证

执行 `verify.sql` 检查升级是否成功。
