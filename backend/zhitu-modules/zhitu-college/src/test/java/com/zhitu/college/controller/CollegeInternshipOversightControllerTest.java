package com.zhitu.college.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhitu.college.dto.ContractDTO;
import com.zhitu.college.entity.InternshipRecord;
import com.zhitu.college.service.CollegeInternshipService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CollegeInternshipOversightController.class)
@AutoConfigureMockMvc(addFilters = false)
class CollegeInternshipOversightControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CollegeInternshipService collegeInternshipService;

    @Test
    void testGetInternshipStudents() throws Exception {
        InternshipRecord record1 = new InternshipRecord();
        record1.setId(1L);
        record1.setStudentId(100L);
        record1.setStatus(1);

        InternshipRecord record2 = new InternshipRecord();
        record2.setId(2L);
        record2.setStudentId(101L);
        record2.setStatus(1);

        IPage<InternshipRecord> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(record1, record2));
        page.setTotal(2);

        when(collegeInternshipService.getInternshipStudents(null, 1, 10)).thenReturn(page);

        mockMvc.perform(get("/api/internship/v1/college/students")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records[0].id").value(1))
                .andExpect(jsonPath("$.data.total").value(2));

        verify(collegeInternshipService).getInternshipStudents(null, 1, 10);
    }

    @Test
    void testGetInternshipStudents_WithStatus() throws Exception {
        InternshipRecord record1 = new InternshipRecord();
        record1.setId(1L);
        record1.setStudentId(100L);
        record1.setStatus(1);

        IPage<InternshipRecord> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(record1));
        page.setTotal(1);

        when(collegeInternshipService.getInternshipStudents("active", 1, 10)).thenReturn(page);

        mockMvc.perform(get("/api/internship/v1/college/students")
                .param("status", "active")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].status").value(1));

        verify(collegeInternshipService).getInternshipStudents("active", 1, 10);
    }

    @Test
    void testGetInternshipStudents_DefaultPagination() throws Exception {
        IPage<InternshipRecord> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList());
        page.setTotal(0);

        when(collegeInternshipService.getInternshipStudents(null, 1, 10)).thenReturn(page);

        mockMvc.perform(get("/api/internship/v1/college/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(collegeInternshipService).getInternshipStudents(null, 1, 10);
    }

    @Test
    void testGetPendingContracts() throws Exception {
        ContractDTO contract1 = new ContractDTO();
        contract1.setId(1L);
        contract1.setStudentName("张三");
        contract1.setCompanyName("阿里巴巴");
        contract1.setPosition("Java开发实习生");
        contract1.setSubmitTime("2024-03-15T10:30:00Z");
        contract1.setStatus("pending");

        ContractDTO contract2 = new ContractDTO();
        contract2.setId(2L);
        contract2.setStudentName("李四");
        contract2.setCompanyName("腾讯");
        contract2.setPosition("前端开发实习生");
        contract2.setSubmitTime("2024-03-16T11:00:00Z");
        contract2.setStatus("pending");

        IPage<ContractDTO> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(contract1, contract2));
        page.setTotal(2);

        when(collegeInternshipService.getPendingContracts(1, 10)).thenReturn(page);

        mockMvc.perform(get("/api/internship/v1/college/contracts/pending")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records[0].collegeAudit").value(0))
                .andExpect(jsonPath("$.data.total").value(2));

        verify(collegeInternshipService).getPendingContracts(1, 10);
    }

    @Test
    void testGetPendingContracts_DefaultPagination() throws Exception {
        IPage<ContractDTO> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList());
        page.setTotal(0);

        when(collegeInternshipService.getPendingContracts(1, 10)).thenReturn(page);

        mockMvc.perform(get("/api/internship/v1/college/contracts/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(collegeInternshipService).getPendingContracts(1, 10);
    }

    @Test
    void testAuditContract_Pass() throws Exception {
        String requestBody = """
                {
                    "action": "pass",
                    "comment": "合同条款符合要求"
                }
                """;

        mockMvc.perform(post("/api/internship/v1/college/contracts/1/audit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(collegeInternshipService).auditContract(1L, "pass", "合同条款符合要求");
    }

    @Test
    void testAuditContract_Reject() throws Exception {
        String requestBody = """
                {
                    "action": "reject",
                    "comment": "薪资低于最低标准"
                }
                """;

        mockMvc.perform(post("/api/internship/v1/college/contracts/2/audit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(collegeInternshipService).auditContract(2L, "reject", "薪资低于最低标准");
    }

    @Test
    void testCreateInspection() throws Exception {
        when(collegeInternshipService.createInspection(any())).thenReturn(1L);

        String requestBody = """
                {
                    "internshipId": 1,
                    "inspectionDate": "2024-02-15",
                    "location": "企业现场",
                    "findings": "实习环境良好",
                    "issues": "无",
                    "recommendations": "继续保持"
                }
                """;

        mockMvc.perform(post("/api/internship/v1/college/inspections")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(1));

        verify(collegeInternshipService).createInspection(any());
    }

    @Test
    void testCreateInspection_MinimalFields() throws Exception {
        when(collegeInternshipService.createInspection(any())).thenReturn(2L);

        String requestBody = """
                {
                    "internshipId": 1,
                    "inspectionDate": "2024-02-15"
                }
                """;

        mockMvc.perform(post("/api/internship/v1/college/inspections")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(2));

        verify(collegeInternshipService).createInspection(any());
    }
}
