package com.zhitu.enterprise.controller;

import com.zhitu.common.core.result.PageResult;
import com.zhitu.common.core.result.Result;
import com.zhitu.enterprise.dto.AttendanceAuditRequest;
import com.zhitu.enterprise.dto.InternDTO;
import com.zhitu.enterprise.dto.ReportReviewRequest;
import com.zhitu.enterprise.service.InternshipManageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class InternshipManageController {

    private final InternshipManageService internshipManageService;

    @GetMapping("/api/internship/v1/enterprise/interns")
    public Result<PageResult<InternDTO>> getInterns(
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
