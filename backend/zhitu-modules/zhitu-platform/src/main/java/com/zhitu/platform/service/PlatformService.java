package com.zhitu.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhitu.common.redis.constants.CacheConstants;
import com.zhitu.common.redis.service.CacheService;
import com.zhitu.platform.dto.AuditRequest;
import com.zhitu.platform.dto.PlatformDashboardStatsDTO;
import com.zhitu.platform.entity.EnterpriseInfo;
import com.zhitu.platform.entity.SysTenant;
import com.zhitu.platform.entity.SysUser;
import com.zhitu.platform.entity.TrainingProject;
import com.zhitu.platform.mapper.EnterpriseInfoMapper;
import com.zhitu.platform.mapper.SysTenantMapper;
import com.zhitu.platform.mapper.SysUserMapper;
import com.zhitu.platform.mapper.TrainingProjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlatformService {

    private final TrainingProjectMapper trainingProjectMapper;
    private final EnterpriseInfoMapper enterpriseInfoMapper;
    private final SysTenantMapper sysTenantMapper;
    private final SysUserMapper sysUserMapper;
    private final CacheService cacheService;

    public IPage<TrainingProject> getPendingProjects(int page, int size) {
        return trainingProjectMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<TrainingProject>()
                        .eq(TrainingProject::getAuditStatus, 0)
                        .orderByAsc(TrainingProject::getId));
    }

    @Transactional
    public void auditProject(Long projectId, AuditRequest req) {
        TrainingProject project = trainingProjectMapper.selectById(projectId);
        project.setAuditStatus(req.getAuditStatus());
        trainingProjectMapper.updateById(project);
    }

    public IPage<EnterpriseInfo> getPendingEnterprises(int page, int size) {
        return enterpriseInfoMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<EnterpriseInfo>()
                        .eq(EnterpriseInfo::getAuditStatus, 0)
                        .orderByAsc(EnterpriseInfo::getId));
    }

    @Transactional
    public void auditEnterprise(Long enterpriseId, AuditRequest req) {
        EnterpriseInfo info = enterpriseInfoMapper.selectById(enterpriseId);
        info.setAuditStatus(req.getAuditStatus());
        info.setAuditRemark(req.getAuditRemark());
        enterpriseInfoMapper.updateById(info);
    }

    // ── Dashboard ─────────────────────────────────────────────────────────────

    /**
     * 获取平台仪表板统计数据
     * Get platform dashboard statistics
     * 
     * Requirements: 28.1-28.7
     * - 28.2: Include total tenant count
     * - 28.3: Include total user count
     * - 28.4: Include active user count (last 30 days)
     * - 28.5: Include total enterprise count
     * - 28.6: Include pending audit count
     * - Cache: Redis key platform:dashboard:stats, TTL 10 minutes
     */
    public PlatformDashboardStatsDTO getDashboardStats() {
        return cacheService.getOrSet(
            CacheConstants.KEY_PLATFORM_DASHBOARD_STATS,
            10,
            TimeUnit.MINUTES,
            () -> {
                log.debug("Computing platform dashboard statistics");

                // 1. 查询租户总数（排除平台租户 type=0）
                Long totalTenantCount = sysTenantMapper.selectCount(
                    new LambdaQueryWrapper<SysTenant>()
                        .ne(SysTenant::getType, 0)
                );

                // 2. 查询用户总数
                Long totalUserCount = sysUserMapper.selectCount(null);

                // 3. 查询活跃用户数（最近30天登录过的用户）
                OffsetDateTime thirtyDaysAgo = OffsetDateTime.now().minusDays(30);
                Long activeUserCount = sysUserMapper.selectCount(
                    new LambdaQueryWrapper<SysUser>()
                        .ge(SysUser::getLastLoginAt, thirtyDaysAgo)
                );

                // 4. 查询企业总数（租户类型为企业 type=2）
                Long totalEnterpriseCount = sysTenantMapper.selectCount(
                    new LambdaQueryWrapper<SysTenant>()
                        .eq(SysTenant::getType, 2)
                );

                // 5. 查询待审核数量（企业审核 + 项目审核）
                Long pendingEnterpriseAudit = enterpriseInfoMapper.selectCount(
                    new LambdaQueryWrapper<EnterpriseInfo>()
                        .eq(EnterpriseInfo::getAuditStatus, 0)
                );
                
                Long pendingProjectAudit = trainingProjectMapper.selectCount(
                    new LambdaQueryWrapper<TrainingProject>()
                        .eq(TrainingProject::getAuditStatus, 0)
                );
                
                Long pendingAuditCount = pendingEnterpriseAudit + pendingProjectAudit;

                log.debug("Platform stats - Tenants: {}, Users: {}, Active: {}, Enterprises: {}, Pending: {}",
                    totalTenantCount, totalUserCount, activeUserCount, totalEnterpriseCount, pendingAuditCount);

                return new PlatformDashboardStatsDTO(
                    totalTenantCount,
                    totalUserCount,
                    activeUserCount,
                    totalEnterpriseCount,
                    pendingAuditCount
                );
            }
        );
    }

    // ── Tenants ───────────────────────────────────────────────────────────────

    /**
     * 获取租户列表
     * Get tenant list with filtering and pagination
     * 
     * Requirements: 30.1-30.6
     * - 30.1: Expose GET /api/system/v1/tenants/colleges endpoint
     * - 30.2: Support filtering by type and status
     * - 30.3: Support pagination
     * - 30.4: Include tenant name, type, status, creation date, and user count
     * - 30.5: Include subscription plan and expiration date
     * - 30.6: Respond within 500ms
     */
    public IPage<com.zhitu.platform.dto.TenantDTO> getTenantList(String type, String status, Integer page, Integer size) {
        log.debug("Getting tenant list - type: {}, status: {}, page: {}, size: {}", type, status, page, size);

        // Build query with filters
        LambdaQueryWrapper<SysTenant> queryWrapper = new LambdaQueryWrapper<SysTenant>()
                .ne(SysTenant::getType, 0); // Exclude platform tenants (type=0)

        // Filter by type if provided
        if (type != null && !type.isEmpty()) {
            try {
                Integer typeValue = Integer.parseInt(type);
                queryWrapper.eq(SysTenant::getType, typeValue);
            } catch (NumberFormatException e) {
                log.warn("Invalid type parameter: {}", type);
            }
        }

        // Filter by status if provided
        if (status != null && !status.isEmpty()) {
            try {
                Integer statusValue = Integer.parseInt(status);
                queryWrapper.eq(SysTenant::getStatus, statusValue);
            } catch (NumberFormatException e) {
                log.warn("Invalid status parameter: {}", status);
            }
        }

        // Order by creation date descending
        queryWrapper.orderByDesc(SysTenant::getCreatedAt);

        // Execute paginated query
        Page<SysTenant> tenantPage = sysTenantMapper.selectPage(
                new Page<>(page != null ? page : 1, size != null ? size : 10),
                queryWrapper
        );

        // Convert to DTO with user counts
        Page<com.zhitu.platform.dto.TenantDTO> dtoPage = new Page<>(tenantPage.getCurrent(), tenantPage.getSize(), tenantPage.getTotal());
        List<com.zhitu.platform.dto.TenantDTO> dtoList = tenantPage.getRecords().stream()
                .map(this::convertToTenantDTO)
                .toList();
        dtoPage.setRecords(dtoList);

        log.debug("Retrieved {} tenants (page {}/{})", dtoList.size(), page, tenantPage.getPages());
        return dtoPage;
    }

    /**
     * Convert SysTenant entity to TenantDTO with user count
     */
    private com.zhitu.platform.dto.TenantDTO convertToTenantDTO(SysTenant tenant) {
        // Query user count for this tenant
        Long userCount = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getTenantId, tenant.getId())
        );

        // Extract subscription info from config JSON (placeholder for now)
        // In a real implementation, parse tenant.getConfig() JSON
        String subscriptionPlan = null;
        OffsetDateTime expirationDate = null;

        return new com.zhitu.platform.dto.TenantDTO(
                tenant.getId(),
                tenant.getName(),
                tenant.getType(),
                tenant.getStatus(),
                tenant.getCreatedAt(),
                userCount,
                subscriptionPlan,
                expirationDate
        );
    }

    // ── Tags ──────────────────────────────────────────────────────────────────

    public List<Map<String, Object>> getTags(String category) {
        return new ArrayList<>();
    }

    public void createTag(Map<String, Object> req) {}

    public void deleteTag(Long id) {}

    // ── Skill Tree ────────────────────────────────────────────────────────────

    public List<Map<String, Object>> getSkillTree() {
        return new ArrayList<>();
    }

    // ── Templates ─────────────────────────────────────────────────────────────

    public List<Map<String, Object>> getCertificateTemplates() {
        return new ArrayList<>();
    }

    public List<Map<String, Object>> getContractTemplates() {
        return new ArrayList<>();
    }

    // ── Logs ──────────────────────────────────────────────────────────────────

    public List<Map<String, Object>> getOperationLogs(String userId, String module,
            String result, String startTime, String endTime) {
        return new ArrayList<>();
    }

    public List<Map<String, Object>> getSecurityLogs(String level) {
        return new ArrayList<>();
    }

    // ── Recommendations ───────────────────────────────────────────────────────

    public List<Map<String, Object>> getRecommendationBanners() {
        return new ArrayList<>();
    }

    public void saveRecommendationBanner(Map<String, Object> req) {}

    public List<Map<String, Object>> getTopListItems(String listType) {
        return new ArrayList<>();
    }

    public void saveTopListItems(Map<String, Object> req) {}
}
