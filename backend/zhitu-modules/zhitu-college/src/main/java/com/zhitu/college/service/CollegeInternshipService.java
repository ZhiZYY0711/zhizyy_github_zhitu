package com.zhitu.college.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhitu.college.dto.AuditContractRequest;
import com.zhitu.college.dto.ContractDTO;
import com.zhitu.college.dto.CreateInspectionRequest;
import com.zhitu.college.dto.InternshipStudentDTO;
import com.zhitu.college.entity.*;
import com.zhitu.college.mapper.*;
import com.zhitu.common.core.context.UserContext;
import com.zhitu.common.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * College Internship Service
 * Handles internship oversight endpoints
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CollegeInternshipService {

    private final InternshipRecordMapper internshipRecordMapper;
    private final InternshipOfferMapper internshipOfferMapper;
    private final InternshipInspectionMapper internshipInspectionMapper;
    private final StudentInfoMapper studentInfoMapper;

    /**
     * Get internship students with filtering and pagination
     * Requirements: 24.1-24.7
     */
    public IPage<InternshipStudentDTO> getInternshipStudents(String status, int page, int size) {
        Page<InternshipStudentDTO> pageParam = new Page<>(page, size);
        return internshipRecordMapper.selectEnrichedInternshipStudents(pageParam, status);
    }

    /**
     * Get pending contracts for audit
     * Requirements: 24.1-24.7
     */
    public IPage<ContractDTO> getPendingContracts(int page, int size) {
        Page<ContractDTO> pageParam = new Page<>(page, size);
        return internshipOfferMapper.selectPendingContracts(pageParam);
    }

    /**
     * Audit internship contract
     * Requirements: 24.1-24.7
     */
    @Transactional(rollbackFor = Exception.class)
    public void auditContract(Long id, String action, String comment) {
        // Validate parameters
        if (id == null) {
            throw new BusinessException("合同ID不能为空");
        }
        if (action == null || action.isEmpty()) {
            throw new BusinessException("审核动作不能为空");
        }
        if (!"pass".equals(action) && !"reject".equals(action)) {
            throw new BusinessException("审核动作必须是 pass 或 reject");
        }
        
        // Get the offer
        InternshipOffer offer = internshipOfferMapper.selectById(id);
        if (offer == null) {
            throw new BusinessException("合同不存在");
        }
        
        // Check if already audited
        if (offer.getCollegeAudit() != 0) {
            throw new BusinessException("合同已审核，不能重复审核");
        }
        
        // Validate contract terms
        validateContractTerms(offer);
        
        // Validate enterprise credentials (basic check)
        validateEnterpriseCredentials(offer.getEnterpriseId());
        
        // Update audit status
        if ("pass".equals(action)) {
            offer.setCollegeAudit(1); // 1=通过
            log.info("Contract {} approved by college. Comment: {}", id, comment);
        } else {
            offer.setCollegeAudit(2); // 2=拒绝
            if (comment == null || comment.isEmpty()) {
                throw new BusinessException("拒绝审核时必须提供原因");
            }
            log.info("Contract {} rejected by college. Reason: {}", id, comment);
        }
        
        internshipOfferMapper.updateById(offer);
    }

    /**
     * Create inspection record
     * Requirements: 24.1-24.7
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createInspection(CreateInspectionRequest request) {
        // Validate required fields
        if (request.getInternshipId() == null) {
            throw new BusinessException("实习ID不能为空");
        }
        if (request.getInspectionDate() == null) {
            throw new BusinessException("巡查日期不能为空");
        }
        
        // Verify internship exists
        InternshipRecord internship = internshipRecordMapper.selectById(request.getInternshipId());
        if (internship == null) {
            throw new BusinessException("实习记录不存在");
        }
        
        // Validate inspection date is not in the future
        if (request.getInspectionDate().isAfter(LocalDate.now())) {
            throw new BusinessException("巡查日期不能是未来日期");
        }
        
        Long tenantId = UserContext.getTenantId();
        Long userId = UserContext.getUserId();
        
        // Create inspection record
        InternshipInspection inspection = new InternshipInspection();
        inspection.setCollegeTenantId(tenantId);
        inspection.setInternshipId(request.getInternshipId());
        inspection.setInspectorId(userId);
        inspection.setInspectionDate(request.getInspectionDate());
        inspection.setLocation(request.getLocation());
        inspection.setFindings(request.getFindings());
        inspection.setIssues(request.getIssues());
        inspection.setRecommendations(request.getRecommendations());
        
        internshipInspectionMapper.insert(inspection);
        
        log.info("Inspection created for internship {} by user {}", request.getInternshipId(), userId);
        
        return inspection.getId();
    }

    /**
     * Validate contract terms
     */
    private void validateContractTerms(InternshipOffer offer) {
        // Validate salary is reasonable
        if (offer.getSalary() != null && offer.getSalary() < 0) {
            throw new BusinessException("薪资不能为负数");
        }
        
        // Validate date range
        if (offer.getStartDate() == null) {
            throw new BusinessException("合同开始日期不能为空");
        }
        if (offer.getEndDate() == null) {
            throw new BusinessException("合同结束日期不能为空");
        }
        if (offer.getEndDate().isBefore(offer.getStartDate())) {
            throw new BusinessException("合同结束日期不能早于开始日期");
        }
        
        // Validate student exists
        StudentInfo student = studentInfoMapper.selectById(offer.getStudentId());
        if (student == null) {
            throw new BusinessException("学生不存在");
        }
    }

    /**
     * Validate enterprise credentials (basic check)
     */
    private void validateEnterpriseCredentials(Long enterpriseId) {
        if (enterpriseId == null) {
            throw new BusinessException("企业ID不能为空");
        }
        // In a real implementation, this would check enterprise business license,
        // qualifications, etc. For now, we just verify the ID is not null.
        // This could be extended to call an enterprise service to verify credentials.
    }
}
