package com.zhitu.college.controller;

import com.zhitu.college.service.CollegePortalService;
import com.zhitu.college.service.CollegeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CollegePortalController.class)
@AutoConfigureMockMvc(addFilters = false)
class CollegePortalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CollegeService collegeService;

    @MockBean
    private CollegePortalService collegePortalService;

    @Test
    void testGetDashboardStats() throws Exception {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalStudentCount", 100L);
        stats.put("internshipParticipationRate", 75.5);
        stats.put("employmentRate", 82.3);
        stats.put("averageSalary", 8500);
        stats.put("year", "2024");

        when(collegePortalService.getDashboardStats("2024")).thenReturn(stats);

        mockMvc.perform(get("/api/portal-college/v1/dashboard/stats")
                .param("year", "2024"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalStudentCount").value(100))
                .andExpect(jsonPath("$.data.year").value("2024"));

        verify(collegePortalService).getDashboardStats("2024");
    }

    @Test
    void testGetDashboardStats_NoYear() throws Exception {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalStudentCount", 100L);
        stats.put("year", LocalDate.now().getYear());

        when(collegePortalService.getDashboardStats(null)).thenReturn(stats);

        mockMvc.perform(get("/api/portal-college/v1/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(collegePortalService).getDashboardStats(null);
    }

    @Test
    void testGetTrends() throws Exception {
        Map<String, Object> trends = new HashMap<>();
        trends.put("dimension", "month");
        trends.put("internshipTrends", Arrays.asList());

        when(collegePortalService.getEmploymentTrends("month")).thenReturn(trends);

        mockMvc.perform(get("/api/portal-college/v1/dashboard/trends")
                .param("dimension", "month"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.dimension").value("month"));

        verify(collegePortalService).getEmploymentTrends("month");
    }

    @Test
    void testGetCrmEnterprises() throws Exception {
        when(collegePortalService.getCrmEnterprises(2, "互联网"))
                .thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/portal-college/v1/crm/enterprises")
                .param("level", "2")
                .param("industry", "互联网"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(collegePortalService).getCrmEnterprises(2, "互联网");
    }

    @Test
    void testGetCrmAudits() throws Exception {
        when(collegePortalService.getCrmAudits(0))
                .thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/portal-college/v1/crm/audits")
                .param("status", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(collegePortalService).getCrmAudits(0);
    }

    @Test
    void testAuditEnterprise() throws Exception {
        String requestBody = """
                {
                    "action": "pass",
                    "comment": "审核通过"
                }
                """;

        mockMvc.perform(post("/api/portal-college/v1/crm/audits/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(collegePortalService).auditEnterprise(1L, "pass", "审核通过");
    }

    @Test
    void testUpdateEnterpriseLevel() throws Exception {
        String requestBody = """
                {
                    "level": 3,
                    "reason": "升级为战略合作伙伴"
                }
                """;

        mockMvc.perform(put("/api/portal-college/v1/crm/enterprises/1/level")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(collegePortalService).updateEnterpriseLevel(1L, 3, "升级为战略合作伙伴");
    }

    @Test
    void testGetVisitRecords() throws Exception {
        when(collegePortalService.getVisitRecords(10L))
                .thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/portal-college/v1/crm/visits")
                .param("enterpriseId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(collegePortalService).getVisitRecords(10L);
    }

    @Test
    void testCreateVisitRecord() throws Exception {
        String requestBody = """
                {
                    "enterpriseTenantId": 10,
                    "visitDate": "2024-01-15",
                    "visitor": "张老师",
                    "purpose": "洽谈合作",
                    "outcome": "达成初步意向",
                    "nextAction": "下周签订协议"
                }
                """;

        mockMvc.perform(post("/api/portal-college/v1/crm/visits")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(collegePortalService).createVisitRecord(
                eq(10L),
                eq(LocalDate.parse("2024-01-15")),
                eq("张老师"),
                eq("洽谈合作"),
                eq("达成初步意向"),
                eq("下周签订协议")
        );
    }
}
