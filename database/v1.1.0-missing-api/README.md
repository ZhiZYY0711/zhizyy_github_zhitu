# 数据库迁移 v1.1.0 - 缺失API表补充

## 概述

此版本补充了缺失的API端点所需的数据库表，完善了系统的数据模型。

## 变更内容

### 新增表（共22个）

#### student_svc (3个表)
1. **student_task** - 学生任务
   - 支持实训、实习、评价等任务类型
   - 优先级和截止日期管理

2. **student_capability** - 学生能力雷达图
   - 5个维度的能力评分（0-100）
   - 用于能力可视化

3. **student_recommendation** - 个性化推荐
   - 项目、岗位、课程推荐
   - 推荐分数和理由

#### training_svc (2个表)
4. **project_task** - Scrum看板任务
   - 看板式任务管理（todo/in_progress/done）
   - 团队分配和故事点

5. **project_enrollment** - 项目注册
   - 学生参与项目记录
   - 状态跟踪（active/completed/withdrawn）

#### enterprise_svc (3个表)
6. **enterprise_activity** - 企业活动流
   - 记录所有企业活动
   - 支持多态引用

7. **enterprise_todo** - 企业待办
   - 企业员工任务管理
   - 优先级和截止日期

8. **interview_schedule** - 面试安排
   - 面试时间管理
   - 多种面试类型（电话/视频/现场）

#### college_svc (4个表)
9. **enterprise_relationship** - 校企合作关系
   - 合作等级（普通/重点/战略）
   - 合作历史记录

10. **enterprise_visit** - 企业拜访记录
    - CRM功能
    - 拜访目的和结果跟踪

11. **enterprise_audit** - 企业资质审核
    - 审核工作流
    - 审核意见和状态

12. **internship_inspection** - 实习巡查
    - 现场巡查记录
    - 问题和建议记录

#### platform_service (10个表)
13. **sys_tag** - 系统标签
    - 分层标签结构
    - 使用次数统计

14. **skill_tree** - 技能树
    - 三大类技能（技术/软技能/领域知识）
    - 技能等级和层级关系

15. **certificate_template** - 证书模板
    - 可配置布局和变量
    - 背景图和签名支持

16. **contract_template** - 合同模板
    - 实习/实训/就业合同模板
    - 变量替换支持

17. **recommendation_banner** - 推荐横幅
    - 门户特定横幅
    - 日期范围验证

18. **recommendation_top_list** - 推荐榜单
    - 导师/课程/项目榜单
    - 有序项目ID数组

19. **operation_log** - 操作审计日志
    - 完整操作跟踪
    - 请求/响应日志

20. **security_log** - 安全事件日志
    - 三级严重程度
    - 事件类型分类

21. **service_health** - 微服务健康监控
    - 实时服务状态
    - 性能指标（响应时间、错误率、CPU、内存）

22. **online_user_trend** - 在线用户趋势
    - 时序数据
    - 按用户类型分类

## 部署步骤

### 前置条件
- 已完成v1.0.0初始化
- 数据库连接正常

### 升级

```bash
# 执行升级脚本
psql -U zhitu_user -d zhitu_cloud -f upgrade.sql
```

### 回滚（如需要）

```bash
# 执行回滚脚本
psql -U zhitu_user -d zhitu_cloud -f rollback.sql
```

## 验证

```sql
-- 验证新增表
SELECT 
    table_schema, 
    table_name 
FROM information_schema.tables 
WHERE table_schema IN ('student_svc', 'training_svc', 'enterprise_svc', 
                       'college_svc', 'platform_service')
    AND table_name IN (
        'student_task', 'student_capability', 'student_recommendation',
        'project_task', 'project_enrollment',
        'enterprise_activity', 'enterprise_todo', 'interview_schedule',
        'enterprise_relationship', 'enterprise_visit', 'enterprise_audit', 'internship_inspection',
        'sys_tag', 'skill_tree', 'certificate_template', 'contract_template',
        'recommendation_banner', 'recommendation_top_list',
        'operation_log', 'security_log', 'service_health', 'online_user_trend'
    )
ORDER BY table_schema, table_name;

-- 应该返回22行
```

## 影响范围

### 新增API端点
- 学生门户：任务列表、能力雷达、个性化推荐
- 实训模块：Scrum看板、项目注册
- 企业门户：活动流、待办事项、面试管理
- 高校门户：校企关系、企业拜访、资质审核、实习巡查
- 平台管理：标签管理、技能树、模板管理、日志审计、监控

### 数据完整性
- 所有表包含外键约束
- 支持软删除（is_deleted）
- 自动时间戳管理

### 性能优化
- 为常用查询字段添加索引
- 部分索引（WHERE is_deleted = FALSE）
- 复合索引优化

## 注意事项

1. 此迁移不会修改现有数据
2. 所有新表都是空表，需要通过API填充数据
3. 建议在非高峰期执行迁移
4. 迁移前建议备份数据库

## 相关文档

- 设计文档：`.kiro/specs/missing-api-endpoints/design.md`
- 需求文档：`.kiro/specs/missing-api-endpoints/requirements.md`
- API文档：待补充

---

**版本：** v1.1.0  
**发布日期：** 2025-03-22  
**依赖版本：** v1.0.0  
**向后兼容：** 是
