package com.zhitu.enterprise.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhitu.common.core.context.UserContext;
import com.zhitu.enterprise.dto.AttendanceAuditRequest;
import com.zhitu.enterprise.dto.ReportReviewRequest;
import com.zhitu.enterprise.entity.*;
import com.zhitu.enterprise.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InternshipManageService {

    private final InternshipRecordMapper internshipRecordMapper;
    private final AttendanceMapper attendanceMapper;
    private final WeeklyReportMapper weeklyReportMapper;
    private final InternshipCertificateMapper certificateMapper;

    public IPage<InternshipRecord> getInternList(int page, int size) {
        Long tenantId = UserContext.getTenantId();
        return internshipRecordMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<InternshipRecord>()
                        .eq(InternshipRecord::getEnterpriseId, tenantId)
                        .eq(InternshipRecord::getStatus, 1)
                        .orderByDesc(InternshipRecord::getStartDate));
    }

    @Transactional
    public void auditAttendance(AttendanceAuditRequest req) {
        Long auditorId = UserContext.getUserId();
        Attendance attendance = attendanceMapper.selectById(req.getAttendanceId());
        attendance.setStatus(req.getStatus());
        attendance.setAuditRemark(req.getAuditRemark());
        attendance.setAuditedBy(auditorId);
        attendanceMapper.updateById(attendance);
    }

    @Transactional
    public void reviewReport(Long reportId, ReportReviewRequest req) {
        Long reviewerId = UserContext.getUserId();
        WeeklyReport report = weeklyReportMapper.selectById(reportId);
        report.setStatus(2);
        report.setReviewComment(req.getReviewComment());
        report.setReviewedBy(reviewerId);
        report.setReviewedAt(OffsetDateTime.now());
        weeklyReportMapper.updateById(report);
    }

    @Transactional
    public void issueCertificate(Long internshipId) {
        Long issuedBy = UserContext.getUserId();
        InternshipRecord record = internshipRecordMapper.selectById(internshipId);
        InternshipCertificate cert = new InternshipCertificate();
        cert.setInternshipId(internshipId);
        cert.setStudentId(record.getStudentId());
        cert.setEnterpriseId(record.getEnterpriseId());
        cert.setCertNo("CERT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        cert.setIssuedBy(issuedBy);
        certificateMapper.insert(cert);
    }
}
