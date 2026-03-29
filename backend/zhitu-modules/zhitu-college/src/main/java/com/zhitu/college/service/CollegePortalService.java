package com.zhitu.college.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhitu.college.entity.*;
import com.zhitu.college.mapper.*;
import com.zhitu.common.core.context.UserContext;
import com.zhitu.common.redis.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.zhitu.college.dto.DashboardStatsDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * College Portal Service
 * Handles dashboard statistics, employment trends, and CRM operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CollegePortalService {

    private final StudentInfoMapper studentInfoMapper;
    private final EnterpriseRelationshipMapper enterpriseRelationshipMapper;
    private final EnterpriseVisitMapper enterpriseVisitMapper;
    private final EnterpriseAuditMapper enterpriseAuditMapper;
    private final CacheService cacheService;

    /**
     * Get dashboard employment statistics
     * Requirements: 20.1-20.7
     */
    public DashboardStatsDTO getDashboardStats(String year) {
        Long tenantId = UserContext.getTenantId();
        
        // Total student count
        long totalStudents = studentInfoMapper.selectCount(
            new LambdaQueryWrapper<StudentInfo>()
                .eq(StudentInfo::getTenantId, tenantId)
        );

        // TODO: Calculate actual rates from internship/employment records
        // TODO: Calculate actual average salary from employment records
        // TODO: Get top hiring industries from internship/employment records
        return DashboardStatsDTO.builder()
            .totalGraduates(totalStudents)
            .internshipRate(0.93)
            .employmentRate(0.87)
            .flexibleEmploymentRate(0.12)
            .avgSalary(9200)
            .topIndustries(Arrays.asList(
                DashboardStatsDTO.IndustryItem.builder().name("互联网").ratio(0.42).build(),
                DashboardStatsDTO.IndustryItem.builder().name("金融").ratio(0.18).build(),
                DashboardStatsDTO.IndustryItem.builder().name("制造业").ratio(0.15).build(),
                DashboardStatsDTO.IndustryItem.builder().name("教育").ratio(0.10).build(),
                DashboardStatsDTO.IndustryItem.builder().name("其他").ratio(0.15).build()
            ))
            .build();
    }

    /**
     * Get employment trends by dimension
     * Requirements: 21.1-21.6
     */
    public Map<String, Object> getEmploymentTrends(String dimension) {
        Long tenantId = UserContext.getTenantId();
        
        Map<String, Object> trends = new HashMap<>();
        
        // Frontend expects: { labels: string[], series: [{ name: string, data: number[] }] }
        List<String> labels = new ArrayList<>();
        List<Double> internshipData = new ArrayList<>();
        List<Double> employmentData = new ArrayList<>();
        
        // TODO: Calculate actual trends from historical data
        // For now, return mock trend data matching frontend expectations
        switch (dimension) {
            case "month":
                labels = Arrays.asList("1月", "2月", "3月", "4月", "5月", "6月");
                internshipData = Arrays.asList(0.12, 0.28, 0.55, 0.72, 0.88, 0.93);
                employmentData = Arrays.asList(0.05, 0.10, 0.22, 0.38, 0.55, 0.68);
                break;
            case "quarter":
                labels = Arrays.asList("Q1", "Q2", "Q3", "Q4");
                internshipData = Arrays.asList(0.25, 0.55, 0.75, 0.90);
                employmentData = Arrays.asList(0.15, 0.35, 0.60, 0.80);
                break;
            case "year":
                int currentYear = LocalDate.now().getYear();
                labels = new ArrayList<>();
                internshipData = new ArrayList<>();
                employmentData = new ArrayList<>();
                for (int i = 0; i < 5; i++) {
                    labels.add(String.valueOf(currentYear - 4 + i));
                    internshipData.add(0.65 + (i * 0.05));
                    employmentData.add(0.70 + (i * 0.04));
                }
                break;
        }
        
        trends.put("labels", labels);
        trends.put("series", Arrays.asList(
            Map.of("name", "实习率", "data", internshipData),
            Map.of("name", "三方签约率", "data", employmentData)
        ));
        
        return trends;
    }

    /**
     * Get CRM enterprises with filtering
     * Requirements: 25.1, 25.5
     */
    public List<Map<String, Object>> getCrmEnterprises(Integer level, String industry) {
        Long tenantId = UserContext.getTenantId();
        
        LambdaQueryWrapper<EnterpriseRelationship> wrapper = new LambdaQueryWrapper<EnterpriseRelationship>()
            .eq(EnterpriseRelationship::getCollegeTenantId, tenantId)
            .eq(EnterpriseRelationship::getStatus, 1);
        
        if (level != null) {
            wrapper.eq(EnterpriseRelationship::getCooperationLevel, level);
        }
        
        // TODO: Add industry filtering when enterprise info is available
        
        List<EnterpriseRelationship> relationships = enterpriseRelationshipMapper.selectList(wrapper);
        
        return relationships.stream().map(rel -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", rel.getId());
            item.put("enterpriseTenantId", rel.getEnterpriseTenantId());
            item.put("cooperationLevel", rel.getCooperationLevel());
            item.put("status", rel.getStatus());
            item.put("createdAt", rel.getCreatedAt());
            // TODO: Join with enterprise info to get name, industry, etc.
            item.put("enterpriseName", "企业" + rel.getEnterpriseTenantId());
            item.put("industry", "互联网");
            return item;
        }).collect(Collectors.toList());
    }

    /**
     * Get CRM audits with filtering
     * Requirements: 25.2, 25.6
     */
    public List<Map<String, Object>> getCrmAudits(Integer status) {
        Long tenantId = UserContext.getTenantId();
        
        LambdaQueryWrapper<EnterpriseAudit> wrapper = new LambdaQueryWrapper<EnterpriseAudit>()
            .orderByDesc(EnterpriseAudit::getCreatedAt);
        
        if (status != null) {
            wrapper.eq(EnterpriseAudit::getStatus, status);
        }
        
        List<EnterpriseAudit> audits = enterpriseAuditMapper.selectList(wrapper);
        
        return audits.stream().map(audit -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", audit.getId());
            item.put("enterpriseTenantId", audit.getEnterpriseTenantId());
            item.put("auditType", audit.getAuditType());
            item.put("status", audit.getStatus());
            item.put("auditorId", audit.getAuditorId());
            item.put("auditComment", audit.getAuditComment());
            item.put("auditedAt", audit.getAuditedAt());
            item.put("createdAt", audit.getCreatedAt());
            // TODO: Join with enterprise info
            item.put("enterpriseName", "企业" + audit.getEnterpriseTenantId());
            return item;
        }).collect(Collectors.toList());
    }

    /**
     * Audit enterprise
     * Requirements: 25.3, 25.7
     */
    public void auditEnterprise(Long id, String action, String comment) {
        Long userId = UserContext.getUserId();
        
        EnterpriseAudit audit = enterpriseAuditMapper.selectById(id);
        if (audit == null) {
            throw new RuntimeException("审核记录不存在");
        }
        
        if (audit.getStatus() != 0) {
            throw new RuntimeException("该审核已处理");
        }
        
        // Update audit status
        audit.setStatus("pass".equals(action) ? 1 : 2);
        audit.setAuditorId(userId);
        audit.setAuditComment(comment);
        audit.setAuditedAt(OffsetDateTime.now());
        
        enterpriseAuditMapper.updateById(audit);
        
        // TODO: Validate business license and qualification documents
        // TODO: Send notification to enterprise
        
        log.info("Enterprise audit completed: id={}, action={}, auditor={}", id, action, userId);
    }

    /**
     * Update enterprise cooperation level
     * Requirements: 25.4, 25.8
     */
    public void updateEnterpriseLevel(Long id, Integer level, String reason) {
        Long userId = UserContext.getUserId();
        
        EnterpriseRelationship relationship = enterpriseRelationshipMapper.selectById(id);
        if (relationship == null) {
            throw new RuntimeException("企业关系不存在");
        }
        
        Integer oldLevel = relationship.getCooperationLevel();
        relationship.setCooperationLevel(level);
        relationship.setUpdatedAt(OffsetDateTime.now());
        
        enterpriseRelationshipMapper.updateById(relationship);
        
        // TODO: Record level change history with reason and operator
        
        log.info("Enterprise level updated: id={}, oldLevel={}, newLevel={}, reason={}, operator={}", 
            id, oldLevel, level, reason, userId);
    }

    /**
     * Get visit records with filtering
     * Requirements: 26.1, 26.3, 26.5, 26.6
     */
    public List<Map<String, Object>> getVisitRecords(Long enterpriseId) {
        Long tenantId = UserContext.getTenantId();
        
        LambdaQueryWrapper<EnterpriseVisit> wrapper = new LambdaQueryWrapper<EnterpriseVisit>()
            .eq(EnterpriseVisit::getCollegeTenantId, tenantId)
            .orderByDesc(EnterpriseVisit::getVisitDate);
        
        if (enterpriseId != null) {
            wrapper.eq(EnterpriseVisit::getEnterpriseTenantId, enterpriseId);
        }
        
        List<EnterpriseVisit> visits = enterpriseVisitMapper.selectList(wrapper);
        
        return visits.stream().map(visit -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", visit.getId());
            item.put("enterpriseTenantId", visit.getEnterpriseTenantId());
            item.put("visitDate", visit.getVisitDate());
            item.put("visitorId", visit.getVisitorId());
            item.put("visitorName", visit.getVisitorName());
            item.put("purpose", visit.getPurpose());
            item.put("outcome", visit.getOutcome());
            item.put("nextAction", visit.getNextAction());
            item.put("createdAt", visit.getCreatedAt());
            // TODO: Join with enterprise info
            item.put("enterpriseName", "企业" + visit.getEnterpriseTenantId());
            item.put("contactPerson", "联系人");
            return item;
        }).collect(Collectors.toList());
    }

    /**
     * Create visit record
     * Requirements: 26.2, 26.4
     */
    public void createVisitRecord(Long enterpriseTenantId, LocalDate visitDate, String visitor, 
                                  String purpose, String outcome, String nextAction) {
        Long tenantId = UserContext.getTenantId();
        Long userId = UserContext.getUserId();
        
        // Validate required fields
        if (enterpriseTenantId == null || visitDate == null || visitor == null || 
            purpose == null || outcome == null) {
            throw new RuntimeException("必填字段不能为空");
        }
        
        EnterpriseVisit visit = new EnterpriseVisit();
        visit.setCollegeTenantId(tenantId);
        visit.setEnterpriseTenantId(enterpriseTenantId);
        visit.setVisitDate(visitDate);
        visit.setVisitorId(userId);
        visit.setVisitorName(visitor);
        visit.setPurpose(purpose);
        visit.setOutcome(outcome);
        visit.setNextAction(nextAction);
        visit.setCreatedAt(OffsetDateTime.now());
        
        enterpriseVisitMapper.insert(visit);
        
        log.info("Visit record created: enterpriseId={}, visitDate={}, visitor={}", 
            enterpriseTenantId, visitDate, visitor);
    }
}
