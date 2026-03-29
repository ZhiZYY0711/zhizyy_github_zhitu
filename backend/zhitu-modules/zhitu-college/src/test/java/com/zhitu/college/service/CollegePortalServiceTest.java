package com.zhitu.college.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhitu.college.dto.DashboardStatsDTO;
import com.zhitu.college.entity.*;
import com.zhitu.college.mapper.*;
import com.zhitu.common.core.context.UserContext;
import com.zhitu.common.redis.service.CacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollegePortalServiceTest {

    @Mock
    private StudentInfoMapper studentInfoMapper;

    @Mock
    private EnterpriseRelationshipMapper enterpriseRelationshipMapper;

    @Mock
    private EnterpriseVisitMapper enterpriseVisitMapper;

    @Mock
    private EnterpriseAuditMapper enterpriseAuditMapper;

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private CollegePortalService collegePortalService;

    @BeforeEach
    void setUp() {
        // Mock UserContext
    }

    @Test
    void testGetDashboardStats() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getTenantId).thenReturn(1L);

            when(studentInfoMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(100L);

            DashboardStatsDTO stats = collegePortalService.getDashboardStats("2024");

            assertNotNull(stats);
            assertEquals(100L, stats.getTotalGraduates());
            assertEquals(0.93, stats.getInternshipRate());
            assertEquals(0.87, stats.getEmploymentRate());
            assertEquals(0.12, stats.getFlexibleEmploymentRate());
            assertEquals(9200, stats.getAvgSalary());
            assertNotNull(stats.getTopIndustries());
            assertEquals(5, stats.getTopIndustries().size());

            verify(studentInfoMapper).selectCount(any(LambdaQueryWrapper.class));
        }
    }

    @Test
    void testGetEmploymentTrends_Month() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getTenantId).thenReturn(1L);

            Map<String, Object> trends = collegePortalService.getEmploymentTrends("month");

            assertNotNull(trends);
            assertTrue(trends.containsKey("labels"));
            assertTrue(trends.containsKey("series"));
            
            @SuppressWarnings("unchecked")
            List<String> labels = (List<String>) trends.get("labels");
            assertEquals(6, labels.size());
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> series = (List<Map<String, Object>>) trends.get("series");
            assertEquals(2, series.size());
        }
    }

    @Test
    void testGetEmploymentTrends_Quarter() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getTenantId).thenReturn(1L);

            Map<String, Object> trends = collegePortalService.getEmploymentTrends("quarter");

            assertNotNull(trends);
            assertTrue(trends.containsKey("labels"));
            assertTrue(trends.containsKey("series"));
            
            @SuppressWarnings("unchecked")
            List<String> labels = (List<String>) trends.get("labels");
            assertEquals(4, labels.size());
        }
    }

    @Test
    void testGetCrmEnterprises() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getTenantId).thenReturn(1L);

            EnterpriseRelationship rel1 = new EnterpriseRelationship();
            rel1.setId(1L);
            rel1.setCollegeTenantId(1L);
            rel1.setEnterpriseTenantId(10L);
            rel1.setCooperationLevel(2);
            rel1.setStatus(1);
            rel1.setCreatedAt(OffsetDateTime.now());

            when(enterpriseRelationshipMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(rel1));

            List<Map<String, Object>> enterprises = collegePortalService.getCrmEnterprises(2, null);

            assertNotNull(enterprises);
            assertEquals(1, enterprises.size());
            assertEquals(1L, enterprises.get(0).get("id"));
            assertEquals(2, enterprises.get(0).get("cooperationLevel"));

            verify(enterpriseRelationshipMapper).selectList(any(LambdaQueryWrapper.class));
        }
    }

    @Test
    void testGetCrmAudits() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getTenantId).thenReturn(1L);

            EnterpriseAudit audit1 = new EnterpriseAudit();
            audit1.setId(1L);
            audit1.setEnterpriseTenantId(10L);
            audit1.setAuditType("registration");
            audit1.setStatus(0);
            audit1.setCreatedAt(OffsetDateTime.now());

            when(enterpriseAuditMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(audit1));

            List<Map<String, Object>> audits = collegePortalService.getCrmAudits(0);

            assertNotNull(audits);
            assertEquals(1, audits.size());
            assertEquals(1L, audits.get(0).get("id"));
            assertEquals(0, audits.get(0).get("status"));

            verify(enterpriseAuditMapper).selectList(any(LambdaQueryWrapper.class));
        }
    }

    @Test
    void testAuditEnterprise_Pass() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn(100L);

            EnterpriseAudit audit = new EnterpriseAudit();
            audit.setId(1L);
            audit.setEnterpriseTenantId(10L);
            audit.setStatus(0);

            when(enterpriseAuditMapper.selectById(1L)).thenReturn(audit);
            when(enterpriseAuditMapper.updateById(audit)).thenReturn(1);

            collegePortalService.auditEnterprise(1L, "pass", "审核通过");

            verify(enterpriseAuditMapper).selectById(1L);
            verify(enterpriseAuditMapper).updateById(audit);
            assertEquals(1, audit.getStatus());
            assertEquals(100L, audit.getAuditorId());
            assertEquals("审核通过", audit.getAuditComment());
            assertNotNull(audit.getAuditedAt());
        }
    }

    @Test
    void testAuditEnterprise_Reject() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn(100L);

            EnterpriseAudit audit = new EnterpriseAudit();
            audit.setId(1L);
            audit.setEnterpriseTenantId(10L);
            audit.setStatus(0);

            when(enterpriseAuditMapper.selectById(1L)).thenReturn(audit);
            when(enterpriseAuditMapper.updateById(audit)).thenReturn(1);

            collegePortalService.auditEnterprise(1L, "reject", "资质不符");

            verify(enterpriseAuditMapper).selectById(1L);
            verify(enterpriseAuditMapper).updateById(audit);
            assertEquals(2, audit.getStatus());
        }
    }

    @Test
    void testAuditEnterprise_AlreadyProcessed() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn(100L);

            EnterpriseAudit audit = new EnterpriseAudit();
            audit.setId(1L);
            audit.setStatus(1); // Already processed

            when(enterpriseAuditMapper.selectById(1L)).thenReturn(audit);

            assertThrows(RuntimeException.class, () -> {
                collegePortalService.auditEnterprise(1L, "pass", "审核通过");
            });

            verify(enterpriseAuditMapper).selectById(1L);
            verify(enterpriseAuditMapper, never()).updateById((EnterpriseAudit) any());
        }
    }

    @Test
    void testUpdateEnterpriseLevel() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn(100L);

            EnterpriseRelationship relationship = new EnterpriseRelationship();
            relationship.setId(1L);
            relationship.setCooperationLevel(1);

            when(enterpriseRelationshipMapper.selectById(1L)).thenReturn(relationship);
            when(enterpriseRelationshipMapper.updateById((EnterpriseRelationship) any())).thenReturn(1);

            collegePortalService.updateEnterpriseLevel(1L, 3, "升级为战略合作伙伴");

            verify(enterpriseRelationshipMapper).selectById(1L);
            verify(enterpriseRelationshipMapper).updateById((EnterpriseRelationship) any());
            assertEquals(3, relationship.getCooperationLevel());
            assertNotNull(relationship.getUpdatedAt());
        }
    }

    @Test
    void testGetVisitRecords() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getTenantId).thenReturn(1L);

            EnterpriseVisit visit1 = new EnterpriseVisit();
            visit1.setId(1L);
            visit1.setCollegeTenantId(1L);
            visit1.setEnterpriseTenantId(10L);
            visit1.setVisitDate(LocalDate.now());
            visit1.setVisitorId(100L);
            visit1.setVisitorName("张老师");
            visit1.setPurpose("洽谈合作");
            visit1.setOutcome("达成初步意向");
            visit1.setCreatedAt(OffsetDateTime.now());

            when(enterpriseVisitMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(visit1));

            List<Map<String, Object>> visits = collegePortalService.getVisitRecords(10L);

            assertNotNull(visits);
            assertEquals(1, visits.size());
            assertEquals(1L, visits.get(0).get("id"));
            assertEquals("张老师", visits.get(0).get("visitorName"));

            verify(enterpriseVisitMapper).selectList(any(LambdaQueryWrapper.class));
        }
    }

    @Test
    void testCreateVisitRecord() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getTenantId).thenReturn(1L);
            mockedUserContext.when(UserContext::getUserId).thenReturn(100L);

            EnterpriseVisit capturedVisit = new EnterpriseVisit();
            when(enterpriseVisitMapper.insert((EnterpriseVisit) any())).thenAnswer(invocation -> {
                EnterpriseVisit visit = invocation.getArgument(0);
                visit.setId(1L);
                return 1;
            });

            collegePortalService.createVisitRecord(
                10L,
                LocalDate.now(),
                "张老师",
                "洽谈合作",
                "达成初步意向",
                "下周签订协议"
            );

            verify(enterpriseVisitMapper).insert((EnterpriseVisit) any());
        }
    }

    @Test
    void testCreateVisitRecord_MissingRequiredFields() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getTenantId).thenReturn(1L);
            mockedUserContext.when(UserContext::getUserId).thenReturn(100L);

            assertThrows(RuntimeException.class, () -> {
                collegePortalService.createVisitRecord(
                    null, // Missing enterprise ID
                    LocalDate.now(),
                    "张老师",
                    "洽谈合作",
                    "达成初步意向",
                    null
                );
            });

            verify(enterpriseVisitMapper, never()).insert((EnterpriseVisit) any());
        }
    }
}
