package com.zhitu.college.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhitu.college.dto.AuditContractRequest;
import com.zhitu.college.dto.CreateInspectionRequest;
import com.zhitu.college.entity.InternshipOffer;
import com.zhitu.college.entity.InternshipRecord;
import com.zhitu.college.service.CollegeInternshipService;
import com.zhitu.common.core.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * College Internship Oversight Controller
 * Handles college internship oversight endpoints
 */
@RestController
@RequestMapping("/api/internship/v1/college")
@RequiredArgsConstructor
public class CollegeInternshipOversightController {

    private final CollegeInternshipService collegeInternshipService;

    /**
     * Get internship students with filtering and pagination
     * Requirements: 24.1-24.7
     */
    @GetMapping("/students")
    public Result<IPage<InternshipRecord>> getInternshipStudents(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        
        IPage<InternshipRecord> students = collegeInternshipService.getInternshipStudents(status, page, size);
        return Result.ok(students);
    }

    /**
     * Get pending contracts for audit
     * Requirements: 24.1-24.7
     */
    @GetMapping("/contracts/pending")
    public Result<IPage<InternshipOffer>> getPendingContracts(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        
        IPage<InternshipOffer> contracts = collegeInternshipService.getPendingContracts(page, size);
        return Result.ok(contracts);
    }

    /**
     * Audit internship contract
     * Requirements: 24.1-24.7
     */
    @PostMapping("/contracts/{id}/audit")
    public Result<Void> auditContract(
            @PathVariable(value = "id") Long id,
            @RequestBody AuditContractRequest request) {
        
        collegeInternshipService.auditContract(id, request.getAction(), request.getComment());
        return Result.ok();
    }

    /**
     * Create inspection record
     * Requirements: 24.1-24.7
     */
    @PostMapping("/inspections")
    public Result<Long> createInspection(@RequestBody CreateInspectionRequest request) {
        Long inspectionId = collegeInternshipService.createInspection(request);
        return Result.ok(inspectionId);
    }
}
