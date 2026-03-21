package com.zhitu.college.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhitu.college.entity.*;
import com.zhitu.college.mapper.*;
import com.zhitu.common.core.context.UserContext;
import com.zhitu.common.redis.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
    public Map<String, Object> getDashboardStats(String year) {
        Long tenantId = UserContext.getTenantId();
        String cacheKey = "college:dashboard:stats:" + tenantId + ":" + (year != null ? year : "all");
        
        return cacheService.getOrSet(cacheKey, 3600, () -> {
            Map<String, Object> stats = new HashMap<>();
            
            // Total student count
            long totalStudents = studentInfoMapper.selectCount(
                new LambdaQueryWrapper<StudentInfo>()
                    .eq(StudentInfo::getTenantId, tenantId)
            );
            stats.put("totalStudentCount", totalStudents);
            
            // TODO: Calculate actual internship participation rate from internship records
            // For now, return mock data
            stats.put("internshipParticipationRate", 75.5);
            
            // TODO: Calculate actual employment rate from employment records
            stats.put("employmentRate", 82.3);
            
            // TODO: Calculate actual average salary from employment records
            stats.put("averageSalary", 8500);
            
            // TODO: Get top hiring enterprises from internship/employment records
            stats.put("topHiringEnterprises", Arrays.asList(
                Map.of("name", "企业A", "hireCount", 15),
                Map.of("name", "企业B", "hireCount", 12),
                Map.of("name", "企业C", "hireCount", 10)
            ));
            
            stats.put("year", year != null ? year : LocalDate.now().getYear());
            
            return stats;
        });
    }

    /**
     * Get employment trends by dimension
     * Requirements: 21.1-21.6
     */
    public Map<String, Object> getEmploymentTrends(String dimension) {
        Long tenantId = UserContext.getTenantId();
        String cacheKey = "college:employment:trends:" + tenantId + ":" + dimension;
        
        return cacheService.getOrSet(cacheKey, 3600, () -> {
            Map<String, Object> trends = new HashMap<>();
            trends.put("dimension", dimension);
            
            // TODO: Calculate actual trends from historical data
            // For now, return mock trend data
            List<Map<String, Object>> trendData = new ArrayList<>();
            
            switch (dimension) {
                case "month":
                    for (int i = 1; i <= 12; i++) {
                        trendData.add(Map.of(
                            "period", i + "月",
                            "internshipRate", 70 + (i * 2),
                            "employmentRate", 75 + i,
                            "averageSalary", 8000 + (i * 100)
                        ));
                    }
                    break;
                case "quarter":
                    for (int i = 1; i <= 4; i++) {
                        trendData.add(Map.of(
                            "period", "Q" + i,
                            "internshipRate", 72 + (i * 3),
                            "employmentRate", 78 + (i * 2),
                            "averageSalary", 8200 + (i * 300)
                        ));
                    }
                    break;
                case "year":
                    int currentYear = LocalDate.now().getYear();
                    for (int i = 0; i < 5; i++) {
                        int year = currentYear - 4 + i;
                        trendData.add(Map.of(
                            "period", String.valueOf(year),
                            "internshipRate", 65 + (i * 5),
                            "employmentRate", 70 + (i * 4),
                            "averageSalary", 7000 + (i * 500)
                        ));
                    }
                    break;
            }
            
            trends.put("internshipTrends", trendData);
            trends.put("employmentTrends", trendData);
            trends.put("salaryTrends", trendData);
            
            // TODO: Calculate industry distribution from actual data
            trends.put("industryDistribution", Arrays.asList(
                Map.of("industry", "互联网", "percentage", 35.5),
                Map.of("industry", "制造业", "percentage", 25.3),
                Map.of("industry", "金融", "percentage", 18.2),
                Map.of("industry", "教育", "percentage", 12.0),
                Map.of("industry", "其他", "percentage", 9.0)
            ));
            
            return trends;
        });
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
