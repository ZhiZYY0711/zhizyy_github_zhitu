package com.zhitu.common.redis.constants;

import java.util.concurrent.TimeUnit;

/**
 * 缓存常量
 * 定义所有缓存键模式和 TTL 配置
 */
public class CacheConstants {

    // ==================== TTL 常量 ====================

    /**
     * 仪表板统计数据 TTL：5 分钟
     * 频繁更新，平衡新鲜度与负载
     */
    public static final long TTL_DASHBOARD_STATS = 5;
    public static final TimeUnit TTL_DASHBOARD_STATS_UNIT = TimeUnit.MINUTES;

    /**
     * 列表数据 TTL：5 分钟
     * 分页列表数据，平衡新鲜度与性能
     */
    public static final long TTL_LIST_DATA = 5;
    public static final TimeUnit TTL_LIST_DATA_UNIT = TimeUnit.MINUTES;

    /**
     * 分析数据 TTL：30 分钟
     * 查询成本高，更新频率低
     */
    public static final long TTL_ANALYTICS = 30;
    public static final TimeUnit TTL_ANALYTICS_UNIT = TimeUnit.MINUTES;

    /**
     * 配置数据 TTL：1 小时
     * 很少变化的配置数据
     */
    public static final long TTL_CONFIG = 1;
    public static final TimeUnit TTL_CONFIG_UNIT = TimeUnit.HOURS;

    /**
     * 健康指标 TTL：1 分钟
     * 需要接近实时的数据
     */
    public static final long TTL_HEALTH = 1;
    public static final TimeUnit TTL_HEALTH_UNIT = TimeUnit.MINUTES;

    /**
     * 推荐数据 TTL：15 分钟
     * 平衡个性化与性能
     */
    public static final long TTL_RECOMMENDATIONS = 15;
    public static final TimeUnit TTL_RECOMMENDATIONS_UNIT = TimeUnit.MINUTES;

    /**
     * 能力数据 TTL：10 分钟
     * 学生能力雷达图数据
     */
    public static final long TTL_CAPABILITY = 10;
    public static final TimeUnit TTL_CAPABILITY_UNIT = TimeUnit.MINUTES;

    /**
     * 活动数据 TTL：3 分钟
     * 企业活动流数据
     */
    public static final long TTL_ACTIVITIES = 3;
    public static final TimeUnit TTL_ACTIVITIES_UNIT = TimeUnit.MINUTES;

    /**
     * 趋势数据 TTL：1 小时
     * 就业趋势等统计数据
     */
    public static final long TTL_TRENDS = 1;
    public static final TimeUnit TTL_TRENDS_UNIT = TimeUnit.HOURS;

    /**
     * 预警统计 TTL：10 分钟
     * 学院预警统计数据
     */
    public static final long TTL_WARNING_STATS = 10;
    public static final TimeUnit TTL_WARNING_STATS_UNIT = TimeUnit.MINUTES;

    /**
     * 横幅数据 TTL：30 分钟
     * 推荐横幅配置
     */
    public static final long TTL_BANNERS = 30;
    public static final TimeUnit TTL_BANNERS_UNIT = TimeUnit.MINUTES;

    /**
     * 榜单数据 TTL：1 小时
     * 推荐榜单数据
     */
    public static final long TTL_TOP_LIST = 1;
    public static final TimeUnit TTL_TOP_LIST_UNIT = TimeUnit.HOURS;

    // ==================== 学生门户缓存键 ====================

    /**
     * 学生仪表板统计
     * 格式：student:dashboard:{userId}
     */
    public static final String KEY_STUDENT_DASHBOARD = "student:dashboard:%d";

    /**
     * 学生能力雷达图
     * 格式：student:capability:{userId}
     */
    public static final String KEY_STUDENT_CAPABILITY = "student:capability:%d";

    /**
     * 学生任务列表
     * 格式：student:tasks:{userId}:{status}
     */
    public static final String KEY_STUDENT_TASKS = "student:tasks:%d:%s";

    /**
     * 学生推荐数据
     * 格式：student:recommendations:{userId}:{type}
     */
    public static final String KEY_STUDENT_RECOMMENDATIONS = "student:recommendations:%d:%s";

    /**
     * 实训项目列表
     * 格式：student:projects:list:{page}:{size}
     */
    public static final String KEY_STUDENT_PROJECTS_LIST = "student:projects:list:%d:%d";

    /**
     * 项目看板
     * 格式：student:project:board:{projectId}
     */
    public static final String KEY_STUDENT_PROJECT_BOARD = "student:project:board:%d";

    /**
     * 实习岗位列表
     * 格式：student:jobs:list:{page}:{size}
     */
    public static final String KEY_STUDENT_JOBS_LIST = "student:jobs:list:%d:%d";

    /**
     * 学生周报列表
     * 格式：student:reports:{userId}:{page}
     */
    public static final String KEY_STUDENT_REPORTS = "student:reports:%d:%d";

    /**
     * 学生评价汇总
     * 格式：student:evaluation:{userId}
     */
    public static final String KEY_STUDENT_EVALUATION = "student:evaluation:%d";

    /**
     * 学生证书列表
     * 格式：student:certificates:{userId}
     */
    public static final String KEY_STUDENT_CERTIFICATES = "student:certificates:%d";

    /**
     * 学生徽章列表
     * 格式：student:badges:{userId}
     */
    public static final String KEY_STUDENT_BADGES = "student:badges:%d";

    // ==================== 企业门户缓存键 ====================

    /**
     * 企业仪表板统计
     * 格式：enterprise:dashboard:{tenantId}
     */
    public static final String KEY_ENTERPRISE_DASHBOARD = "enterprise:dashboard:%d";

    /**
     * 企业待办事项
     * 格式：enterprise:todos:{userId}:{page}
     */
    public static final String KEY_ENTERPRISE_TODOS = "enterprise:todos:%d:%d";

    /**
     * 企业活动流
     * 格式：enterprise:activities:{tenantId}:{page}
     */
    public static final String KEY_ENTERPRISE_ACTIVITIES = "enterprise:activities:%d:%d";

    /**
     * 企业岗位列表
     * 格式：enterprise:jobs:list:{tenantId}:{status}:{page}
     */
    public static final String KEY_ENTERPRISE_JOBS_LIST = "enterprise:jobs:list:%d:%s:%d";

    /**
     * 企业申请列表
     * 格式：enterprise:applications:{tenantId}:{jobId}:{status}
     */
    public static final String KEY_ENTERPRISE_APPLICATIONS = "enterprise:applications:%d:%d:%s";

    /**
     * 企业人才库
     * 格式：enterprise:talent:pool:{tenantId}
     */
    public static final String KEY_ENTERPRISE_TALENT_POOL = "enterprise:talent:pool:%d";

    /**
     * 导师仪表板
     * 格式：mentor:dashboard:{userId}
     */
    public static final String KEY_MENTOR_DASHBOARD = "mentor:dashboard:%d";

    /**
     * 企业分析数据
     * 格式：enterprise:analytics:{tenantId}:{range}
     */
    public static final String KEY_ENTERPRISE_ANALYTICS = "enterprise:analytics:%d:%s";

    // ==================== 学院门户缓存键 ====================

    /**
     * 学院仪表板统计
     * 格式：college:dashboard:{tenantId}:{year}
     */
    public static final String KEY_COLLEGE_DASHBOARD = "college:dashboard:%d:%d";

    /**
     * 就业趋势数据
     * 格式：college:trends:{tenantId}:{dimension}
     */
    public static final String KEY_COLLEGE_TRENDS = "college:trends:%d:%s";

    /**
     * 学生列表
     * 格式：college:students:list:{tenantId}:{filters}
     */
    public static final String KEY_COLLEGE_STUDENTS_LIST = "college:students:list:%d:%s";

    /**
     * 实训计划列表
     * 格式：college:training:plans:{tenantId}:{semester}
     */
    public static final String KEY_COLLEGE_TRAINING_PLANS = "college:training:plans:%d:%s";

    /**
     * 实习监控列表
     * 格式：college:internship:students:{tenantId}:{status}
     */
    public static final String KEY_COLLEGE_INTERNSHIP_STUDENTS = "college:internship:students:%d:%s";

    /**
     * 待审核合同列表
     * 格式：college:contracts:pending:{tenantId}
     */
    public static final String KEY_COLLEGE_CONTRACTS_PENDING = "college:contracts:pending:%d";

    /**
     * 企业关系列表
     * 格式：college:crm:enterprises:{tenantId}:{filters}
     */
    public static final String KEY_COLLEGE_CRM_ENTERPRISES = "college:crm:enterprises:%d:%s";

    /**
     * 企业审核列表
     * 格式：college:crm:audits:{tenantId}:{status}
     */
    public static final String KEY_COLLEGE_CRM_AUDITS = "college:crm:audits:%d:%s";

    /**
     * 企业访问记录
     * 格式：college:crm:visits:{tenantId}:{enterpriseId}
     */
    public static final String KEY_COLLEGE_CRM_VISITS = "college:crm:visits:%d:%d";

    /**
     * 预警记录列表
     * 格式：college:warnings:list:{tenantId}:{filters}
     */
    public static final String KEY_COLLEGE_WARNINGS_LIST = "college:warnings:list:%d:%s";

    /**
     * 预警统计数据
     * 格式：college:warnings:stats:{tenantId}
     */
    public static final String KEY_COLLEGE_WARNINGS_STATS = "college:warnings:stats:%d";

    // ==================== 平台管理缓存键 ====================

    /**
     * 平台仪表板统计
     * 格式：platform:dashboard:stats
     */
    public static final String KEY_PLATFORM_DASHBOARD_STATS = "platform:dashboard:stats";

    /**
     * 系统健康状态
     * 格式：platform:health
     */
    public static final String KEY_PLATFORM_HEALTH = "platform:health";

    /**
     * 在线用户趋势
     * 格式：platform:online:trend
     */
    public static final String KEY_PLATFORM_ONLINE_TREND = "platform:online:trend";

    /**
     * 服务健康详情
     * 格式：platform:services:health
     */
    public static final String KEY_PLATFORM_SERVICES_HEALTH = "platform:services:health";

    /**
     * 租户列表
     * 格式：platform:tenants:list:{type}:{status}
     */
    public static final String KEY_PLATFORM_TENANTS_LIST = "platform:tenants:list:%s:%s";

    /**
     * 企业审核列表
     * 格式：platform:audits:enterprises:{status}
     */
    public static final String KEY_PLATFORM_AUDITS_ENTERPRISES = "platform:audits:enterprises:%s";

    /**
     * 项目审核列表
     * 格式：platform:audits:projects:{status}
     */
    public static final String KEY_PLATFORM_AUDITS_PROJECTS = "platform:audits:projects:%s";

    /**
     * 推荐横幅
     * 格式：platform:banners:{portal}
     */
    public static final String KEY_PLATFORM_BANNERS = "platform:banners:%s";

    /**
     * 推荐榜单
     * 格式：platform:toplist:{listType}
     */
    public static final String KEY_PLATFORM_TOP_LIST = "platform:toplist:%s";

    /**
     * 标签列表
     * 格式：platform:tags:{category}
     */
    public static final String KEY_PLATFORM_TAGS = "platform:tags:%s";

    /**
     * 技能树
     * 格式：platform:skill:tree:{category}
     */
    public static final String KEY_PLATFORM_SKILL_TREE = "platform:skill:tree:%s";

    /**
     * 证书模板列表
     * 格式：platform:certificate:templates
     */
    public static final String KEY_PLATFORM_CERTIFICATE_TEMPLATES = "platform:certificate:templates";

    /**
     * 合同模板列表
     * 格式：platform:contract:templates
     */
    public static final String KEY_PLATFORM_CONTRACT_TEMPLATES = "platform:contract:templates";

    // ==================== 缓存失效模式 ====================

    /**
     * 学生相关所有缓存
     */
    public static final String PATTERN_STUDENT_ALL = "student:*";

    /**
     * 企业相关所有缓存
     */
    public static final String PATTERN_ENTERPRISE_ALL = "enterprise:*";

    /**
     * 学院相关所有缓存
     */
    public static final String PATTERN_COLLEGE_ALL = "college:*";

    /**
     * 平台相关所有缓存
     */
    public static final String PATTERN_PLATFORM_ALL = "platform:*";

    /**
     * 企业岗位相关缓存
     */
    public static final String PATTERN_ENTERPRISE_JOBS = "enterprise:jobs:*";

    /**
     * 学生项目相关缓存
     */
    public static final String PATTERN_STUDENT_PROJECTS = "student:projects:*";

    /**
     * 学院预警相关缓存
     */
    public static final String PATTERN_COLLEGE_WARNINGS = "college:warnings:*";

    /**
     * 平台横幅相关缓存
     */
    public static final String PATTERN_PLATFORM_BANNERS = "platform:banners:*";

    /**
     * 平台榜单相关缓存
     */
    public static final String PATTERN_PLATFORM_TOP_LISTS = "platform:toplist:*";

    private CacheConstants() {
        // 工具类，禁止实例化
    }
}
