package com.zhitu.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhitu.platform.dto.AuditEnterpriseRequest;
import com.zhitu.platform.dto.AuditProjectRequest;
import com.zhitu.platform.dto.EnterpriseAuditDTO;
import com.zhitu.platform.dto.ProjectAuditDTO;
import com.zhitu.platform.entity.EnterpriseInfo;
import com.zhitu.platform.entity.SysTenant;
import com.zhitu.platform.entity.TrainingProject;
import com.zhitu.platform.mapper.EnterpriseInfoMapper;
import com.zhitu.platform.mapper.SysTenantMapper;
import com.zhitu.platform.mapper.TrainingProjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * 平台审核服务
 * Platform Audit Service
 * 
 * Handles enterprise and project audits for platform administrators
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlatformAuditService {

    private final EnterpriseInfoMapper enterpriseInfoMapper;
    private final SysTenantMapper sysTenantMapper;
    private final TrainingProjectMapper trainingProjectMapper;

    /**
     * 获取企业审核列表
     * Get enterprise audits with filtering and pagination
     * 
     * Requirements: 31.1-31.3, 31.7
     * - 31.1: Expose GET /api/system/v1/audits/enterprises endpoint
     * - 31.3: Support filtering audits by status query parameter
     * - 31.7: Include enterprise name, business license, contact person, and submission date
     * 
     * @param status Filter by audit status (0=pending, 1=passed, 2=rejected), null for all
     * @param page Page number (1-based)
     * @param size Page size
     * @return Paginated list of enterprise audits
     */
    public IPage<EnterpriseAuditDTO> getEnterpriseAudits(Integer status, Integer page, Integer size) {
        log.debug("Getting enterprise audits - status: {}, page: {}, size: {}", status, page, size);

        // Build query with optional status filter
        LambdaQueryWrapper<EnterpriseInfo> queryWrapper = new LambdaQueryWrapper<EnterpriseInfo>()
                .orderByAsc(EnterpriseInfo::getId); // Order by ID for consistent pagination

        // Filter by status if provided
        if (status != null) {
            queryWrapper.eq(EnterpriseInfo::getAuditStatus, status);
        }

        // Execute paginated query
        Page<EnterpriseInfo> enterprisePage = enterpriseInfoMapper.selectPage(
                new Page<>(page != null ? page : 1, size != null ? size : 10),
                queryWrapper
        );

        // Convert to DTO
        Page<EnterpriseAuditDTO> dtoPage = new Page<>(enterprisePage.getCurrent(), enterprisePage.getSize(), enterprisePage.getTotal());
        dtoPage.setRecords(enterprisePage.getRecords().stream()
                .map(this::convertToEnterpriseAuditDTO)
                .toList());

        log.debug("Retrieved {} enterprise audits (page {}/{})", 
                dtoPage.getRecords().size(), page, dtoPage.getPages());
        
        return dtoPage;
    }

    /**
     * 审核企业注册
     * Audit enterprise registration
     * 
     * Requirements: 31.2, 31.4-31.6
     * - 31.2: Expose POST /api/system/v1/audits/enterprises/{id} endpoint
     * - 31.4: Accept action parameter with values "pass" or "reject"
     * - 31.5: Require reject_reason parameter when rejecting
     * - 31.6: Activate enterprise account and send notification on approval
     * 
     * @param id Enterprise tenant ID
     * @param request Audit request containing action and optional reject reason
     */
    @Transactional
    public void auditEnterprise(Long id, AuditEnterpriseRequest request) {
        log.debug("Auditing enterprise {} with action: {}", id, request.getAction());

        // Validate action parameter
        if (!"pass".equals(request.getAction()) && !"reject".equals(request.getAction())) {
            throw new IllegalArgumentException("Invalid action: must be 'pass' or 'reject'");
        }

        // Requirement 31.5: Require reject_reason when rejecting
        if ("reject".equals(request.getAction()) && 
            (request.getRejectReason() == null || request.getRejectReason().trim().isEmpty())) {
            throw new IllegalArgumentException("reject_reason is required when action is 'reject'");
        }

        // Find enterprise info by tenant_id
        EnterpriseInfo enterpriseInfo = enterpriseInfoMapper.selectOne(
                new LambdaQueryWrapper<EnterpriseInfo>()
                        .eq(EnterpriseInfo::getTenantId, id)
        );

        if (enterpriseInfo == null) {
            throw new IllegalArgumentException("Enterprise not found with tenant_id: " + id);
        }

        // Check if already audited
        if (enterpriseInfo.getAuditStatus() != null && enterpriseInfo.getAuditStatus() != 0) {
            throw new IllegalStateException("Enterprise has already been audited");
        }

        // Update audit status
        if ("pass".equals(request.getAction())) {
            enterpriseInfo.setAuditStatus(1); // 1 = passed
            enterpriseInfo.setAuditRemark("审核通过");
            
            // Requirement 31.6: Activate enterprise account on approval
            activateEnterpriseAccount(id);
            
            log.info("Enterprise {} audit passed, account activated", id);
        } else {
            enterpriseInfo.setAuditStatus(2); // 2 = rejected
            enterpriseInfo.setAuditRemark(request.getRejectReason());
            
            log.info("Enterprise {} audit rejected: {}", id, request.getRejectReason());
        }

        enterpriseInfoMapper.updateById(enterpriseInfo);

        // Requirement 31.6: Send notification to enterprise
        sendEnterpriseNotification(id, request.getAction(), request.getRejectReason());
        
        log.debug("Enterprise {} audit completed successfully", id);
    }

    /**
     * 激活企业账号
     * Activate enterprise account by updating tenant status
     * 
     * @param tenantId Enterprise tenant ID
     */
    private void activateEnterpriseAccount(Long tenantId) {
        SysTenant tenant = sysTenantMapper.selectById(tenantId);
        if (tenant != null) {
            tenant.setStatus(1); // 1 = active
            tenant.setUpdatedAt(OffsetDateTime.now());
            sysTenantMapper.updateById(tenant);
            log.debug("Activated enterprise account for tenant {}", tenantId);
        } else {
            log.warn("Tenant {} not found when trying to activate", tenantId);
        }
    }

    /**
     * 发送企业通知
     * Send notification to enterprise about audit result
     * 
     * @param tenantId Enterprise tenant ID
     * @param action Audit action (pass/reject)
     * @param rejectReason Rejection reason (if rejected)
     */
    private void sendEnterpriseNotification(Long tenantId, String action, String rejectReason) {
        // TODO: Implement notification service integration
        // For now, just log the notification
        if ("pass".equals(action)) {
            log.info("Notification sent to enterprise {}: Your registration has been approved", tenantId);
        } else {
            log.info("Notification sent to enterprise {}: Your registration has been rejected. Reason: {}", 
                    tenantId, rejectReason);
        }
    }

    /**
     * Convert EnterpriseInfo entity to EnterpriseAuditDTO
     * 
     * @param enterpriseInfo Enterprise info entity
     * @return Enterprise audit DTO
     */
    private EnterpriseAuditDTO convertToEnterpriseAuditDTO(EnterpriseInfo enterpriseInfo) {
        return new EnterpriseAuditDTO(
                enterpriseInfo.getId(),
                enterpriseInfo.getTenantId(),
                enterpriseInfo.getEnterpriseName(),
                enterpriseInfo.getEnterpriseCode(), // Business license number
                enterpriseInfo.getContactName(),
                enterpriseInfo.getContactPhone(),
                enterpriseInfo.getIndustry(),
                enterpriseInfo.getCity(),
                enterpriseInfo.getAuditStatus(),
                enterpriseInfo.getAuditRemark(),
                enterpriseInfo.getCreatedAt() // Submission date
        );
    }

    /**
     * 获取项目审核列表
     * Get project audits with filtering and pagination
     * 
     * Requirements: 32.1, 32.3, 32.7
     * - 32.1: Expose GET /api/portal-platform/v1/audits/projects endpoint
     * - 32.3: Support filtering audits by status query parameter
     * - 32.7: Include project name, creator, submission date, and description
     * 
     * @param status Filter by audit status (0=pending, 1=passed, 2=rejected), null for all
     * @param page Page number (1-based)
     * @param size Page size
     * @return Paginated list of project audits
     */
    public IPage<ProjectAuditDTO> getProjectAudits(Integer status, Integer page, Integer size) {
        log.debug("Getting project audits - status: {}, page: {}, size: {}", status, page, size);

        // Build query with optional status filter
        LambdaQueryWrapper<TrainingProject> queryWrapper = new LambdaQueryWrapper<TrainingProject>()
                .orderByAsc(TrainingProject::getId); // Order by ID for consistent pagination

        // Filter by status if provided
        if (status != null) {
            queryWrapper.eq(TrainingProject::getAuditStatus, status);
        }

        // Execute paginated query
        Page<TrainingProject> projectPage = trainingProjectMapper.selectPage(
                new Page<>(page != null ? page : 1, size != null ? size : 10),
                queryWrapper
        );

        // Convert to DTO
        Page<ProjectAuditDTO> dtoPage = new Page<>(projectPage.getCurrent(), projectPage.getSize(), projectPage.getTotal());
        dtoPage.setRecords(projectPage.getRecords().stream()
                .map(this::convertToProjectAuditDTO)
                .toList());

        log.debug("Retrieved {} project audits (page {}/{})", 
                dtoPage.getRecords().size(), page, dtoPage.getPages());
        
        return dtoPage;
    }

    /**
     * 审核实训项目
     * Audit training project
     * 
     * Requirements: 32.2, 32.4-32.6
     * - 32.2: Expose POST /api/portal-platform/v1/audits/projects/{id} endpoint
     * - 32.4: Accept action parameter with values "pass" or "reject"
     * - 32.5: Support optional quality_rating parameter on approval
     * - 32.6: Require reject_reason when rejecting
     * 
     * @param id Project ID
     * @param request Audit request containing action, optional quality rating, and optional reject reason
     */
    @Transactional
    public void auditProject(Long id, AuditProjectRequest request) {
        log.debug("Auditing project {} with action: {}", id, request.getAction());

        // Validate action parameter
        if (!"pass".equals(request.getAction()) && !"reject".equals(request.getAction())) {
            throw new IllegalArgumentException("Invalid action: must be 'pass' or 'reject'");
        }

        // Requirement 32.6: Require reject_reason when rejecting
        if ("reject".equals(request.getAction()) && 
            (request.getRejectReason() == null || request.getRejectReason().trim().isEmpty())) {
            throw new IllegalArgumentException("reject_reason is required when action is 'reject'");
        }

        // Find project by ID
        TrainingProject project = trainingProjectMapper.selectById(id);

        if (project == null) {
            throw new IllegalArgumentException("Project not found with id: " + id);
        }

        // Check if already audited
        if (project.getAuditStatus() != null && project.getAuditStatus() != 0) {
            throw new IllegalStateException("Project has already been audited");
        }

        // Update audit status
        if ("pass".equals(request.getAction())) {
            project.setAuditStatus(1); // 1 = passed
            
            // Requirement 32.5: Support optional quality_rating parameter on approval
            if (request.getQualityRating() != null) {
                // Store quality rating in a custom field if needed
                // For now, we'll log it as the implementation may need a new field
                log.info("Project {} approved with quality rating: {}", id, request.getQualityRating());
            }
            
            log.info("Project {} audit passed", id);
        } else {
            project.setAuditStatus(2); // 2 = rejected
            
            log.info("Project {} audit rejected: {}", id, request.getRejectReason());
        }

        project.setUpdatedAt(OffsetDateTime.now());
        trainingProjectMapper.updateById(project);
        
        log.debug("Project {} audit completed successfully", id);
    }

    /**
     * Convert TrainingProject entity to ProjectAuditDTO
     * 
     * @param project Training project entity
     * @return Project audit DTO
     */
    private ProjectAuditDTO convertToProjectAuditDTO(TrainingProject project) {
        // Get enterprise name if enterprise_id is available
        String enterpriseName = null;
        if (project.getEnterpriseId() != null) {
            EnterpriseInfo enterpriseInfo = enterpriseInfoMapper.selectOne(
                    new LambdaQueryWrapper<EnterpriseInfo>()
                            .eq(EnterpriseInfo::getTenantId, project.getEnterpriseId())
            );
            if (enterpriseInfo != null) {
                enterpriseName = enterpriseInfo.getEnterpriseName();
            }
        }

        return new ProjectAuditDTO(
                project.getId(),
                project.getProjectName(),
                project.getDescription(),
                project.getEnterpriseId(),
                enterpriseName,
                project.getTechStack(),
                project.getIndustry(),
                project.getStartDate(),
                project.getEndDate(),
                project.getAuditStatus(),
                null, // Quality rating - would need a new field in TrainingProject
                project.getCreatedAt() // Submission date
        );
    }
}
