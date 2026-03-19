package com.zhitu.enterprise.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhitu.common.core.result.Result;
import com.zhitu.enterprise.dto.AttendanceAuditRequest;
import com.zhitu.enterprise.dto.ReportReviewRequest;
import com.zhitu.enterprise.entity.InternshipRecord;
import com.zhitu.enterprise.service.InternshipManageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 企业实习管理接口
 * GET  /api/internship/v1/enterprise/interns              - 实习生列表
 * POST /api/internship/v1/enterprise/attendance/audit     - 考勤审批
 * POST /api/internship/v1/enterprise/certificates/issue  - 发放实习证明
 * POST /api/internship/v1/mentor/reports/{id}/review     - 批阅周报
 */
@RestController
@RequiredArgsConstructor
public class InternshipManageController {

    private final InternshipManageService internshipManageService;

    @GetMapping("/api/internship/v1/enterprise/interns")
    public Result<IPage<InternshipRecord>> getInterns(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(internshipManageService.getInternList(page, size));
    }

    @PostMapping("/api/internship/v1/enterprise/attendance/audit")
    public Result<Void> auditAttendance(@Valid @RequestBody AttendanceAuditRequest req) {
        internshipManageService.auditAttendance(req);
        return Result.ok();
    }

    @PostMapping("/api/internship/v1/enterprise/certificates/issue")
    public Result<Void> issueCertificate(@RequestParam Long internshipId) {
        internshipManageService.issueCertificate(internshipId);
        return Result.ok();
    }

    @PostMapping("/api/internship/v1/mentor/reports/{id}/review")
    public Result<Void> reviewReport(@PathVariable Long id,
                                     @Valid @RequestBody ReportReviewRequest req) {
        internshipManageService.reviewReport(id, req);
        return Result.ok();
    }
}
