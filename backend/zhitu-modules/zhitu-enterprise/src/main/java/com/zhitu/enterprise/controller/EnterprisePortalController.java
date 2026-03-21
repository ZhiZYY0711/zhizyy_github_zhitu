package com.zhitu.enterprise.controller;

import com.zhitu.common.core.result.PageResult;
import com.zhitu.common.core.result.Result;
import com.zhitu.enterprise.dto.ActivityDTO;
import com.zhitu.enterprise.dto.AnalyticsDTO;
import com.zhitu.enterprise.dto.DashboardStatsDTO;
import com.zhitu.enterprise.dto.TalentPoolDTO;
import com.zhitu.enterprise.dto.TodoDTO;
import com.zhitu.enterprise.service.EnterpriseAnalyticsService;
import com.zhitu.enterprise.service.EnterprisePortalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 企业门户接口
 * GET /api/portal-enterprise/v1/dashboard/stats - 获取企业仪表板统计数据
 * GET /api/portal-enterprise/v1/todos - 获取企业待办事项列表
 * GET /api/portal-enterprise/v1/activities - 获取企业活动动态列表
 * GET /api/portal-enterprise/v1/talent-pool - 获取企业人才库列表
 * DELETE /api/portal-enterprise/v1/talent-pool/{id} - 从人才库中移除学生
 */
@RestController
@RequestMapping("/api/portal-enterprise/v1")
@RequiredArgsConstructor
public class EnterprisePortalController {

    private final EnterprisePortalService enterprisePortalService;
    private final EnterpriseAnalyticsService enterpriseAnalyticsService;

    /**
     * 获取企业仪表板统计数据
     * 包括：活跃岗位数、待处理申请数、活跃实习生数、实训项目数
     * 
     * 多租户隔离：通过UserContext获取当前用户的tenant_id，所有查询都按tenant_id过滤
     * 缓存策略：使用Redis缓存，TTL为5分钟
     * 
     * @return 仪表板统计数据
     */
    @GetMapping("/dashboard/stats")
    public Result<DashboardStatsDTO> getDashboardStats() {
        return Result.ok(enterprisePortalService.getDashboardStats());
    }

    /**
     * 获取企业待办事项列表
     * 查询当前用户的待处理事项，按优先级和截止日期排序
     * 
     * 实现逻辑：
     * 1. 从UserContext获取当前用户ID
     * 2. 查询enterprise_todo表，过滤条件：user_id=当前用户 AND status=0（待处理）
     * 3. 按priority DESC, due_date ASC排序
     * 4. 支持分页
     * 
     * @param page 页码（默认1）
     * @param size 每页大小（默认10）
     * @return 分页的待办事项列表
     */
    @GetMapping("/todos")
    public Result<PageResult<TodoDTO>> getTodos(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.ok(enterprisePortalService.getTodos(page, size));
    }

    /**
     * 获取企业活动动态列表
     * 查询最近30天的企业活动，按时间降序排序
     * 
     * 实现逻辑：
     * 1. 从UserContext获取当前用户ID
     * 2. 查询sys_user表获取用户的tenant_id（企业ID）
     * 3. 查询enterprise_activity表，过滤条件：tenant_id=当前企业 AND created_at >= NOW() - INTERVAL '30 days'
     * 4. 按created_at DESC排序
     * 5. 使用Redis缓存，TTL为3分钟
     * 6. 支持分页
     * 
     * @param page 页码（默认1）
     * @param size 每页大小（默认10）
     * @return 分页的活动动态列表
     */
    @GetMapping("/activities")
    public Result<PageResult<ActivityDTO>> getActivities(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.ok(enterprisePortalService.getActivities(page, size));
    }

    /**
     * 获取企业人才库列表
     * 查询talent_pool表，关联student_info获取学生详细信息
     * 
     * 实现逻辑：
     * 1. 从UserContext获取当前用户ID
     * 2. 查询sys_user表获取用户的tenant_id（企业ID）
     * 3. 查询talent_pool表，过滤条件：tenant_id=当前企业 AND is_deleted=false
     * 4. 关联student_info表获取学生姓名、学号、专业、年级、技能标签
     * 5. 按created_at DESC排序
     * 6. 支持分页
     * 
     * @param page 页码（默认1）
     * @param size 每页大小（默认10）
     * @return 分页的人才库列表
     */
    @GetMapping("/talent-pool")
    public Result<PageResult<TalentPoolDTO>> getTalentPool(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.ok(enterprisePortalService.getTalentPool(page, size));
    }

    /**
     * 从人才库中移除学生（软删除）
     * 
     * 实现逻辑：
     * 1. 从UserContext获取当前用户ID
     * 2. 查询sys_user表获取用户的tenant_id（企业ID）
     * 3. 验证该人才库记录属于当前企业（多租户隔离）
     * 4. 执行软删除（设置is_deleted=true）
     * 
     * @param id 人才库记录ID
     * @return 操作结果
     */
    @DeleteMapping("/talent-pool/{id}")
    public Result<Void> removeFromTalentPool(@PathVariable Long id) {
        enterprisePortalService.removeFromTalentPool(id);
        return Result.ok();
    }

    /**
     * 获取企业分析数据
     * 包括：申请趋势、实习生绩效、项目完成率、导师满意度
     * 
     * 实现逻辑：
     * 1. 从UserContext获取当前用户ID
     * 2. 查询sys_user表获取用户的tenant_id（企业ID）
     * 3. 根据时间范围聚合数据
     * 4. 使用Redis缓存，TTL为30分钟
     * 
     * @param range 时间范围: "week", "month", "quarter", "year"
     * @return 分析数据
     */
    @GetMapping("/analytics")
    public Result<AnalyticsDTO> getAnalytics(@RequestParam(defaultValue = "month") String range) {
        return Result.ok(enterpriseAnalyticsService.getAnalytics(range));
    }
}
