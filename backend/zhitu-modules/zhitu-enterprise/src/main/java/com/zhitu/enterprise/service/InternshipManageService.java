package com.zhitu.enterprise.service;

import com.zhitu.common.core.context.UserContext;
import com.zhitu.common.core.result.PageResult;
import com.zhitu.enterprise.dto.AttendanceAuditRequest;
import com.zhitu.enterprise.dto.InternDTO;
import com.zhitu.enterprise.dto.ReportReviewRequest;
import com.zhitu.enterprise.entity.*;
import com.zhitu.enterprise.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InternshipManageService {

    private final InternshipRecordMapper internshipRecordMapper;
    private final AttendanceMapper attendanceMapper;
    private final WeeklyReportMapper weeklyReportMapper;
    private final InternshipCertificateMapper certificateMapper;
    private final JdbcTemplate jdbcTemplate;

    public PageResult<InternDTO> getInternList(int page, int size) {
        Long userId = UserContext.getUserId();
        Long tenantId = getTenantIdByUserId(userId);
        
        if (tenantId == null) {
            log.warn("Tenant not found for user: {}", userId);
            return PageResult.of(0L, List.of(), page, size);
        }

        log.debug("Getting intern list for tenant: {}, page: {}, size: {}", tenantId, page, size);

        int offset = (page - 1) * size;

        String sql = "SELECT ir.id, ir.student_id, ir.enterprise_id, ir.job_id, ir.mentor_id, " +
                "ir.teacher_id, ir.start_date, ir.end_date, ir.status, ir.created_at, ir.updated_at, " +
                "si.real_name as student_name, si.student_no, si.phone as student_phone, si.grade, " +
                "m.org_name as major_name, j.job_title, e.enterprise_name, " +
                "c.college_name as school_name, " +
                "mentor_user.real_name as mentor_name, teacher_user.real_name as teacher_name " +
                "FROM internship_svc.internship_record ir " +
                "LEFT JOIN student_svc.student_info si ON ir.student_id = si.id " +
                "LEFT JOIN college_svc.organization m ON si.major_id = m.id AND m.org_type = 2 " +
                "LEFT JOIN college_svc.college_info c ON si.tenant_id = c.tenant_id " +
                "LEFT JOIN internship_svc.internship_job j ON ir.job_id = j.id " +
                "LEFT JOIN enterprise_svc.enterprise_info e ON ir.enterprise_id = e.id " +
                "LEFT JOIN auth_center.sys_user mentor_user ON ir.mentor_id = mentor_user.id " +
                "LEFT JOIN auth_center.sys_user teacher_user ON ir.teacher_id = teacher_user.id " +
                "WHERE ir.enterprise_id = ? AND ir.status = 1 " +
                "ORDER BY ir.start_date DESC " +
                "LIMIT ? OFFSET ?";

        List<InternDTO> interns = jdbcTemplate.query(sql, (rs, rowNum) -> {
            InternDTO dto = new InternDTO();
            dto.setId(rs.getLong("id"));
            dto.setStudentId(rs.getLong("student_id"));
            dto.setStudentName(rs.getString("student_name"));
            dto.setStudentNo(rs.getString("student_no"));
            dto.setStudentPhone(rs.getString("student_phone"));
            dto.setGrade(rs.getString("grade"));
            dto.setSchoolName(rs.getString("school_name"));
            dto.setMajor(rs.getString("major_name"));
            dto.setEnterpriseId(rs.getLong("enterprise_id"));
            dto.setEnterpriseName(rs.getString("enterprise_name"));
            dto.setJobId(rs.getLong("job_id"));
            dto.setJobTitle(rs.getString("job_title"));
            dto.setMentorId(rs.getObject("mentor_id", Long.class));
            dto.setMentorName(rs.getString("mentor_name"));
            dto.setTeacherId(rs.getObject("teacher_id", Long.class));
            dto.setTeacherName(rs.getString("teacher_name"));
            dto.setStartDate(rs.getDate("start_date") != null ? rs.getDate("start_date").toLocalDate() : null);
            dto.setEndDate(rs.getDate("end_date") != null ? rs.getDate("end_date").toLocalDate() : null);
            dto.setStatus(rs.getInt("status"));
            dto.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
            dto.setUpdatedAt(rs.getObject("updated_at", OffsetDateTime.class));
            return dto;
        }, tenantId, size, offset);

        String countSql = "SELECT COUNT(*) FROM internship_svc.internship_record " +
                "WHERE enterprise_id = ? AND status = 1";
        Long total = jdbcTemplate.queryForObject(countSql, Long.class, tenantId);

        return PageResult.of(total != null ? total : 0L, interns, page, size);
    }

    private Long getTenantIdByUserId(Long userId) {
        String sql = "SELECT tenant_id FROM auth_center.sys_user WHERE id = ?";
        List<Long> results = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("tenant_id"), userId);
        return results.isEmpty() ? null : results.get(0);
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
