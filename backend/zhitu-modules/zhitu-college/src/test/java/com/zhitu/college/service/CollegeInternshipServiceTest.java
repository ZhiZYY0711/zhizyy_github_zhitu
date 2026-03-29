package com.zhitu.college.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhitu.college.dto.ContractDTO;
import com.zhitu.college.dto.CreateInspectionRequest;
import com.zhitu.college.entity.*;
import com.zhitu.college.mapper.*;
import com.zhitu.common.core.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollegeInternshipServiceTest {

    @Mock
    private InternshipRecordMapper internshipRecordMapper;

    @Mock
    private InternshipOfferMapper internshipOfferMapper;

    @Mock
    private InternshipInspectionMapper internshipInspectionMapper;

    @Mock
    private StudentInfoMapper studentInfoMapper;

    @InjectMocks
    private CollegeInternshipService collegeInternshipService;

    @Test
    void testGetInternshipStudents_NoFilter() {
        InternshipRecord record1 = new InternshipRecord();
        record1.setId(1L);
        record1.setStudentId(100L);
        record1.setStatus(1);

        InternshipRecord record2 = new InternshipRecord();
        record2.setId(2L);
        record2.setStudentId(101L);
        record2.setStatus(2);

        Page<InternshipRecord> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(record1, record2));
        page.setTotal(2);

        when(internshipRecordMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        IPage<InternshipRecord> result = collegeInternshipService.getInternshipStudents(null, 1, 10);

        assertNotNull(result);
        assertEquals(2, result.getRecords().size());
        assertEquals(2, result.getTotal());
        verify(internshipRecordMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void testGetInternshipStudents_WithActiveStatus() {
        InternshipRecord record1 = new InternshipRecord();
        record1.setId(1L);
        record1.setStudentId(100L);
        record1.setStatus(1);

        Page<InternshipRecord> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(record1));
        page.setTotal(1);

        when(internshipRecordMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        IPage<InternshipRecord> result = collegeInternshipService.getInternshipStudents("active", 1, 10);

        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
        verify(internshipRecordMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void testGetInternshipStudents_WithCompletedStatus() {
        InternshipRecord record1 = new InternshipRecord();
        record1.setId(2L);
        record1.setStudentId(101L);
        record1.setStatus(2);

        Page<InternshipRecord> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(record1));
        page.setTotal(1);

        when(internshipRecordMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        IPage<InternshipRecord> result = collegeInternshipService.getInternshipStudents("completed", 1, 10);

        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
        verify(internshipRecordMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void testGetPendingContracts() {
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

        Page<ContractDTO> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(contract1, contract2));
        page.setTotal(2);

        when(internshipOfferMapper.selectPendingContracts(any(Page.class)))
                .thenReturn(page);

        IPage<ContractDTO> result = collegeInternshipService.getPendingContracts(1, 10);

        assertNotNull(result);
        assertEquals(2, result.getRecords().size());
        assertEquals(2, result.getTotal());
        verify(internshipOfferMapper).selectPendingContracts(any(Page.class));
    }

    @Test
    void testAuditContract_Pass_Success() {
        InternshipOffer offer = new InternshipOffer();
        offer.setId(1L);
        offer.setStudentId(100L);
        offer.setEnterpriseId(200L);
        offer.setCollegeAudit(0);
        offer.setStatus(1);
        offer.setSalary(5000);
        offer.setStartDate(LocalDate.of(2024, 3, 1));
        offer.setEndDate(LocalDate.of(2024, 8, 31));

        StudentInfo student = new StudentInfo();
        student.setId(100L);
        student.setRealName("张三");

        when(internshipOfferMapper.selectById(1L)).thenReturn(offer);
        when(studentInfoMapper.selectById(100L)).thenReturn(student);
        when(internshipOfferMapper.updateById(eq(offer))).thenReturn(1);

        collegeInternshipService.auditContract(1L, "pass", "合同条款符合要求");

        assertEquals(1, offer.getCollegeAudit());
        verify(internshipOfferMapper).selectById(1L);
        verify(studentInfoMapper).selectById(100L);
        verify(internshipOfferMapper).updateById(eq(offer));
    }

    @Test
    void testAuditContract_Reject_Success() {
        InternshipOffer offer = new InternshipOffer();
        offer.setId(1L);
        offer.setStudentId(100L);
        offer.setEnterpriseId(200L);
        offer.setCollegeAudit(0);
        offer.setStatus(1);
        offer.setSalary(5000);
        offer.setStartDate(LocalDate.of(2024, 3, 1));
        offer.setEndDate(LocalDate.of(2024, 8, 31));

        StudentInfo student = new StudentInfo();
        student.setId(100L);
        student.setRealName("张三");

        when(internshipOfferMapper.selectById(1L)).thenReturn(offer);
        when(studentInfoMapper.selectById(100L)).thenReturn(student);
        when(internshipOfferMapper.updateById(eq(offer))).thenReturn(1);

        collegeInternshipService.auditContract(1L, "reject", "薪资低于最低标准");

        assertEquals(2, offer.getCollegeAudit());
        verify(internshipOfferMapper).selectById(1L);
        verify(studentInfoMapper).selectById(100L);
        verify(internshipOfferMapper).updateById(eq(offer));
    }

    @Test
    void testAuditContract_MissingId() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            collegeInternshipService.auditContract(null, "pass", "comment");
        });

        assertEquals("合同ID不能为空", exception.getMessage());
        verify(internshipOfferMapper, never()).selectById(any());
    }

    @Test
    void testAuditContract_MissingAction() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            collegeInternshipService.auditContract(1L, null, "comment");
        });

        assertEquals("审核动作不能为空", exception.getMessage());
        verify(internshipOfferMapper, never()).selectById(any());
    }

    @Test
    void testAuditContract_InvalidAction() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            collegeInternshipService.auditContract(1L, "invalid", "comment");
        });

        assertEquals("审核动作必须是 pass 或 reject", exception.getMessage());
        verify(internshipOfferMapper, never()).selectById(any());
    }

    @Test
    void testAuditContract_OfferNotFound() {
        when(internshipOfferMapper.selectById(1L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            collegeInternshipService.auditContract(1L, "pass", "comment");
        });

        assertEquals("合同不存在", exception.getMessage());
        verify(internshipOfferMapper).selectById(1L);
    }

    @Test
    void testAuditContract_AlreadyAudited() {
        InternshipOffer offer = new InternshipOffer();
        offer.setId(1L);
        offer.setCollegeAudit(1); // Already approved

        when(internshipOfferMapper.selectById(1L)).thenReturn(offer);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            collegeInternshipService.auditContract(1L, "pass", "comment");
        });

        assertEquals("合同已审核，不能重复审核", exception.getMessage());
        verify(internshipOfferMapper).selectById(1L);
    }

    @Test
    void testAuditContract_RejectWithoutComment() {
        InternshipOffer offer = new InternshipOffer();
        offer.setId(1L);
        offer.setStudentId(100L);
        offer.setEnterpriseId(200L);
        offer.setCollegeAudit(0);
        offer.setStatus(1);
        offer.setSalary(5000);
        offer.setStartDate(LocalDate.of(2024, 3, 1));
        offer.setEndDate(LocalDate.of(2024, 8, 31));

        StudentInfo student = new StudentInfo();
        student.setId(100L);

        when(internshipOfferMapper.selectById(1L)).thenReturn(offer);
        when(studentInfoMapper.selectById(100L)).thenReturn(student);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            collegeInternshipService.auditContract(1L, "reject", null);
        });

        assertEquals("拒绝审核时必须提供原因", exception.getMessage());
    }

    @Test
    void testAuditContract_InvalidDateRange() {
        InternshipOffer offer = new InternshipOffer();
        offer.setId(1L);
        offer.setStudentId(100L);
        offer.setEnterpriseId(200L);
        offer.setCollegeAudit(0);
        offer.setStatus(1);
        offer.setSalary(5000);
        offer.setStartDate(LocalDate.of(2024, 8, 31));
        offer.setEndDate(LocalDate.of(2024, 3, 1)); // End before start

        when(internshipOfferMapper.selectById(1L)).thenReturn(offer);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            collegeInternshipService.auditContract(1L, "pass", "comment");
        });

        assertEquals("合同结束日期不能早于开始日期", exception.getMessage());
    }

    @Test
    void testAuditContract_StudentNotFound() {
        InternshipOffer offer = new InternshipOffer();
        offer.setId(1L);
        offer.setStudentId(100L);
        offer.setEnterpriseId(200L);
        offer.setCollegeAudit(0);
        offer.setStatus(1);
        offer.setSalary(5000);
        offer.setStartDate(LocalDate.of(2024, 3, 1));
        offer.setEndDate(LocalDate.of(2024, 8, 31));

        when(internshipOfferMapper.selectById(1L)).thenReturn(offer);
        when(studentInfoMapper.selectById(100L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            collegeInternshipService.auditContract(1L, "pass", "comment");
        });

        assertEquals("学生不存在", exception.getMessage());
    }

    @Test
    void testCreateInspection_Success() {
        CreateInspectionRequest request = new CreateInspectionRequest();
        request.setInternshipId(1L);
        request.setInspectionDate(LocalDate.of(2024, 2, 15));
        request.setLocation("企业现场");
        request.setFindings("实习环境良好");
        request.setIssues("无");
        request.setRecommendations("继续保持");

        InternshipRecord internship = new InternshipRecord();
        internship.setId(1L);
        internship.setStudentId(100L);

        when(internshipRecordMapper.selectById(1L)).thenReturn(internship);
        when(internshipInspectionMapper.insert(any(InternshipInspection.class))).thenAnswer(invocation -> {
            InternshipInspection inspection = invocation.getArgument(0);
            inspection.setId(1L);
            return 1;
        });

        Long inspectionId = collegeInternshipService.createInspection(request);

        assertNotNull(inspectionId);
        assertEquals(1L, inspectionId);
        verify(internshipRecordMapper).selectById(1L);
        verify(internshipInspectionMapper).insert(any(InternshipInspection.class));
    }

    @Test
    void testCreateInspection_MissingInternshipId() {
        CreateInspectionRequest request = new CreateInspectionRequest();
        request.setInspectionDate(LocalDate.of(2024, 2, 15));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            collegeInternshipService.createInspection(request);
        });

        assertEquals("实习ID不能为空", exception.getMessage());
        verify(internshipRecordMapper, never()).selectById(any());
    }

    @Test
    void testCreateInspection_MissingInspectionDate() {
        CreateInspectionRequest request = new CreateInspectionRequest();
        request.setInternshipId(1L);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            collegeInternshipService.createInspection(request);
        });

        assertEquals("巡查日期不能为空", exception.getMessage());
        verify(internshipRecordMapper, never()).selectById(any());
    }

    @Test
    void testCreateInspection_InternshipNotFound() {
        CreateInspectionRequest request = new CreateInspectionRequest();
        request.setInternshipId(1L);
        request.setInspectionDate(LocalDate.of(2024, 2, 15));

        when(internshipRecordMapper.selectById(1L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            collegeInternshipService.createInspection(request);
        });

        assertEquals("实习记录不存在", exception.getMessage());
        verify(internshipRecordMapper).selectById(1L);
    }

    @Test
    void testCreateInspection_FutureDate() {
        CreateInspectionRequest request = new CreateInspectionRequest();
        request.setInternshipId(1L);
        request.setInspectionDate(LocalDate.now().plusDays(1)); // Future date

        InternshipRecord internship = new InternshipRecord();
        internship.setId(1L);

        when(internshipRecordMapper.selectById(1L)).thenReturn(internship);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            collegeInternshipService.createInspection(request);
        });

        assertEquals("巡查日期不能是未来日期", exception.getMessage());
    }
}
