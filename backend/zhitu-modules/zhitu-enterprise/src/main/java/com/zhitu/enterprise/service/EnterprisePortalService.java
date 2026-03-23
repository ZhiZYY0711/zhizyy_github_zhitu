package com.zhitu.enterprise.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhitu.common.core.context.UserContext;
import com.zhitu.common.core.result.PageResult;
import com.zhitu.common.redis.service.CacheService;
import com.zhitu.enterprise.dto.ActivityDTO;
import com.zhitu.enterprise.dto.DashboardStatsDTO;
import com.zhitu.enterprise.dto.TalentPoolDTO;
import com.zhitu.enterprise.dto.TodoDTO;
import com.zhitu.enterprise.entity.EnterpriseTodo;
import com.zhitu.enterprise.entity.TalentPool;
import com.zhitu.enterprise.mapper.EnterpriseTodoMapper;
import com.zhitu.enterprise.mapper.TalentPoolMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 企业门户服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EnterprisePortalService {

    private final CacheService cacheService;
    private final JdbcTemplate jdbcTemplate;
    private final EnterpriseTodoMapper enterpriseTodoMapper;
    private final TalentPoolMapper talentPoolMapper;

    /**
     * 获取企业仪表板统计数据
     * 包括：活跃岗位数、待处理申请数、活跃实习生数、实训项目数
     * 
     * 实现逻辑：
     * 1. 从UserContext获取当前用户ID
     * 2. 查询sys_user表获取用户的tenant_id（企业ID）
     * 3. 统计该企业的各项数据（按tenant_id过滤）
     * 4. 使用Redis缓存，TTL为5分钟
     * 
     * @return 仪表板统计数据
     */
    public DashboardStatsDTO getDashboardStats() {
        Long userId = UserContext.getUserId();
        
        // 获取企业租户ID
        Long tenantId = getTenantIdByUserId(userId);
        if (tenantId == null) {
            log.warn("Tenant not found for user: {}", userId);
            return new DashboardStatsDTO(0, 0, 0, 0);
        }

        String cacheKey = "enterprise:dashboard:" + tenantId;

        return cacheService.getOrSet(cacheKey, 5, TimeUnit.MINUTES, () -> {
            log.debug("Computing dashboard stats for tenant: {}", tenantId);

            // 1. 查询活跃岗位数量（status=1表示开放状态）
            Integer activeJobCount = countActiveJobs(tenantId);

            // 2. 查询待处理申请数量（status=0表示待审核）
            Integer pendingApplicationCount = countPendingApplications(tenantId);

            // 3. 查询活跃实习生数量（status=1表示实习中）
            Integer activeInternCount = countActiveInterns(tenantId);

            // 4. 查询实训项目数量（企业创建的实训项目）
            Integer trainingProjectCount = countTrainingProjects(tenantId);

            return new DashboardStatsDTO(
                    activeJobCount,
                    pendingApplicationCount,
                    activeInternCount,
                    trainingProjectCount
            );
        });
    }

    /**
     * 根据用户ID获取租户ID（企业ID）
     * 从auth_center.sys_user表查询tenant_id字段
     */
    private Long getTenantIdByUserId(Long userId) {
        String sql = "SELECT tenant_id FROM auth_center.sys_user WHERE id = ?";
        List<Long> results = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("tenant_id"), userId);
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * 统计活跃岗位数量
     * 查询internship_svc.internship_job表，条件：
     * - enterprise_id = tenant_id
     * - status = 1（开放状态）
     * - is_deleted = false
     */
    private Integer countActiveJobs(Long tenantId) {
        String sql = "SELECT COUNT(*) FROM internship_svc.internship_job " +
                "WHERE enterprise_id = ? AND status = 1 AND is_deleted IS FALSE";
        return jdbcTemplate.queryForObject(sql, Integer.class, tenantId);
    }

    /**
     * 统计待处理申请数量
     * 查询internship_svc.job_application表，条件：
     * - 岗位的enterprise_id = tenant_id
     * - status = 0（待审核）
     */
    private Integer countPendingApplications(Long tenantId) {
        String sql = "SELECT COUNT(*) FROM internship_svc.job_application ja " +
                "INNER JOIN internship_svc.internship_job ij ON ja.job_id = ij.id " +
                "WHERE ij.enterprise_id = ? AND ja.status = 0";
        return jdbcTemplate.queryForObject(sql, Integer.class, tenantId);
    }

    /**
     * 统计活跃实习生数量
     * 查询internship_svc.internship_record表，条件：
     * - enterprise_id = tenant_id
     * - status = 1（实习中）
     */
    private Integer countActiveInterns(Long tenantId) {
        String sql = "SELECT COUNT(*) FROM internship_svc.internship_record " +
                "WHERE enterprise_id = ? AND status = 1";
        return jdbcTemplate.queryForObject(sql, Integer.class, tenantId);
    }

    /**
     * 统计实训项目数量
     * 查询training_svc.training_project表，条件：
     * - enterprise_id = tenant_id
     * - is_deleted = false
     */
    private Integer countTrainingProjects(Long tenantId) {
        String sql = "SELECT COUNT(*) FROM training_svc.training_project " +
                "WHERE enterprise_id = ? AND is_deleted IS FALSE";
        return jdbcTemplate.queryForObject(sql, Integer.class, tenantId);
    }

    /**
     * 获取企业待办事项列表
     * 查询enterprise_todo表，条件：
     * - user_id = 当前用户ID
     * - status = 0（待处理）
     * - 按优先级降序、截止日期升序排序
     * 
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 分页的待办事项列表
     */
    public PageResult<TodoDTO> getTodos(Integer page, Integer size) {
        Long userId = UserContext.getUserId();
        log.debug("Getting todos for user: {}, page: {}, size: {}", userId, page, size);

        // 构建查询条件
        LambdaQueryWrapper<EnterpriseTodo> queryWrapper = new LambdaQueryWrapper<EnterpriseTodo>()
                .eq(EnterpriseTodo::getUserId, userId)
                .eq(EnterpriseTodo::getStatus, 0) // 只查询待处理的
                .orderByDesc(EnterpriseTodo::getPriority) // 优先级降序
                .orderByAsc(EnterpriseTodo::getDueDate); // 截止日期升序

        // 分页查询
        Page<EnterpriseTodo> pageRequest = new Page<>(page, size);
        Page<EnterpriseTodo> pageResult = enterpriseTodoMapper.selectPage(pageRequest, queryWrapper);

        // 转换为DTO
        List<TodoDTO> todoDTOs = pageResult.getRecords().stream()
                .map(this::convertToTodoDTO)
                .collect(Collectors.toList());

        return PageResult.of(
                pageResult.getTotal(),
                todoDTOs,
                page,
                size
        );
    }

    /**
     * 将EnterpriseTodo实体转换为TodoDTO
     */
    private TodoDTO convertToTodoDTO(EnterpriseTodo todo) {
        return new TodoDTO(
                todo.getId(),
                todo.getTodoType(),
                todo.getRefType(),
                todo.getRefId(),
                todo.getTitle(),
                todo.getPriority(),
                todo.getDueDate(),
                todo.getStatus(),
                todo.getCreatedAt()
        );
    }

    /**
     * 获取企业活动动态列表
     * 查询enterprise_activity表，条件：
     * - tenant_id = 当前企业租户ID
     * - created_at >= NOW() - INTERVAL '30 days'（最近30天）
     * - 按created_at降序排序
     * 
     * 实现逻辑：
     * 1. 从UserContext获取当前用户ID
     * 2. 查询sys_user表获取用户的tenant_id（企业ID）
     * 3. 查询enterprise_activity表，过滤最近30天的活动
     * 4. 使用Redis缓存，TTL为3分钟
     * 
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 分页的活动动态列表
     */
    public PageResult<ActivityDTO> getActivities(Integer page, Integer size) {
        Long userId = UserContext.getUserId();
        
        // 获取企业租户ID
        Long tenantId = getTenantIdByUserId(userId);
        if (tenantId == null) {
            log.warn("Tenant not found for user: {}", userId);
            return PageResult.of(0L, List.of(), page, size);
        }

        String cacheKey = "enterprise:activities:" + tenantId + ":" + page;

        return cacheService.getOrSet(cacheKey, 3, TimeUnit.MINUTES, () -> {
            log.debug("Querying activities for tenant: {}, page: {}, size: {}", tenantId, page, size);

            // 计算偏移量
            int offset = (page - 1) * size;

            // 查询最近30天的活动，按时间降序
            String sql = "SELECT id, activity_type, description, ref_type, ref_id, created_at " +
                    "FROM enterprise_svc.enterprise_activity " +
                    "WHERE tenant_id = ? AND created_at >= NOW() - INTERVAL '30 days' " +
                    "ORDER BY created_at DESC " +
                    "LIMIT ? OFFSET ?";

            List<ActivityDTO> activities = jdbcTemplate.query(sql, (rs, rowNum) -> new ActivityDTO(
                    rs.getLong("id"),
                    rs.getString("activity_type"),
                    rs.getString("description"),
                    rs.getString("ref_type"),
                    rs.getObject("ref_id", Long.class),
                    rs.getObject("created_at", OffsetDateTime.class)
            ), tenantId, size, offset);

            // 查询总数
            String countSql = "SELECT COUNT(*) FROM enterprise_svc.enterprise_activity " +
                    "WHERE tenant_id = ? AND created_at >= NOW() - INTERVAL '30 days'";
            Long total = jdbcTemplate.queryForObject(countSql, Long.class, tenantId);

            return PageResult.of(total != null ? total : 0L, activities, page, size);
        });
    }

    /**
     * 获取企业人才库列表
     * 查询talent_pool表，条件：
     * - tenant_id = 当前企业租户ID
     * - is_deleted = false
     * - 按created_at降序排序
     * 
     * 实现逻辑：
     * 1. 从UserContext获取当前用户ID
     * 2. 查询sys_user表获取用户的tenant_id（企业ID）
     * 3. 查询talent_pool表，关联student_info获取学生详细信息
     * 4. 支持分页
     * 
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 分页的人才库列表
     */
    public PageResult<TalentPoolDTO> getTalentPool(Integer page, Integer size) {
        Long userId = UserContext.getUserId();
        
        // 获取企业租户ID
        Long tenantId = getTenantIdByUserId(userId);
        if (tenantId == null) {
            log.warn("Tenant not found for user: {}", userId);
            return PageResult.of(0L, List.of(), page, size);
        }

        log.debug("Getting talent pool for tenant: {}, page: {}, size: {}", tenantId, page, size);

        // 计算偏移量
        int offset = (page - 1) * size;

        // 查询人才库，关联学生信息
        String sql = "SELECT tp.id, tp.student_id, tp.remark, tp.created_at, " +
                "si.real_name, si.student_no, si.major_id, si.grade, si.skills " +
                "FROM enterprise_svc.talent_pool tp " +
                "INNER JOIN student_svc.student_info si ON tp.student_id = si.id " +
                "WHERE tp.tenant_id = ? AND tp.is_deleted IS FALSE " +
                "ORDER BY tp.created_at DESC " +
                "LIMIT ? OFFSET ?";

        List<TalentPoolDTO> talents = jdbcTemplate.query(sql, (rs, rowNum) -> {
            TalentPoolDTO dto = new TalentPoolDTO();
            dto.setId(rs.getLong("id"));
            dto.setStudentId(rs.getLong("student_id"));
            dto.setStudentName(rs.getString("real_name"));
            dto.setStudentNo(rs.getString("student_no"));
            
            // Get major name from major_id (simplified - just use ID for now)
            Long majorId = rs.getObject("major_id", Long.class);
            dto.setMajor(majorId != null ? "专业" + majorId : null);
            
            dto.setGrade(rs.getString("grade"));
            dto.setSkills(rs.getString("skills"));
            dto.setRemark(rs.getString("remark"));
            dto.setCollectedAt(rs.getObject("created_at", OffsetDateTime.class));
            return dto;
        }, tenantId, size, offset);

        // 查询总数
        String countSql = "SELECT COUNT(*) FROM enterprise_svc.talent_pool " +
                "WHERE tenant_id = ? AND is_deleted IS FALSE";
        Long total = jdbcTemplate.queryForObject(countSql, Long.class, tenantId);

        return PageResult.of(total != null ? total : 0L, talents, page, size);
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
     */
    public void removeFromTalentPool(Long id) {
        Long userId = UserContext.getUserId();
        
        // 获取企业租户ID
        Long tenantId = getTenantIdByUserId(userId);
        if (tenantId == null) {
            log.warn("Tenant not found for user: {}", userId);
            throw new RuntimeException("无法获取企业信息");
        }

        log.debug("Removing talent pool entry: {} for tenant: {}", id, tenantId);

        // 查询记录并验证租户ID（多租户隔离）
        LambdaQueryWrapper<TalentPool> queryWrapper = new LambdaQueryWrapper<TalentPool>()
                .eq(TalentPool::getId, id)
                .eq(TalentPool::getTenantId, tenantId);

        TalentPool talentPool = talentPoolMapper.selectOne(queryWrapper);
        
        if (talentPool == null) {
            log.warn("Talent pool entry not found or access denied: {} for tenant: {}", id, tenantId);
            throw new RuntimeException("人才库记录不存在或无权访问");
        }

        // 执行软删除（MyBatis Plus的@TableLogic会自动处理）
        int result = talentPoolMapper.deleteById(id);
        
        if (result > 0) {
            log.info("Successfully removed talent pool entry: {} for tenant: {}", id, tenantId);
        } else {
            log.error("Failed to remove talent pool entry: {} for tenant: {}", id, tenantId);
            throw new RuntimeException("删除失败");
        }
    }
}
