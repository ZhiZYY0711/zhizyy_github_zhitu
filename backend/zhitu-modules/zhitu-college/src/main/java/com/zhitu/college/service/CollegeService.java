package com.zhitu.college.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhitu.college.dto.CreateTrainingPlanRequest;
import com.zhitu.college.dto.InterveneRequest;
import com.zhitu.college.entity.*;
import com.zhitu.college.mapper.*;
import com.zhitu.common.core.context.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CollegeService {

    private final CollegeInfoMapper collegeInfoMapper;
    private final OrganizationMapper organizationMapper;
    private final StudentInfoMapper studentInfoMapper;
    private final TrainingPlanMapper trainingPlanMapper;
    private final WarningRecordMapper warningRecordMapper;

    public CollegeInfo getProfile() {
        Long tenantId = UserContext.getTenantId();
        return collegeInfoMapper.selectOne(
                new LambdaQueryWrapper<CollegeInfo>()
                        .eq(CollegeInfo::getTenantId, tenantId));
    }

    public List<Organization> getOrganizationTree() {
        Long tenantId = UserContext.getTenantId();
        return organizationMapper.selectList(
                new LambdaQueryWrapper<Organization>()
                        .eq(Organization::getTenantId, tenantId)
                        .orderByAsc(Organization::getSortOrder));
    }

    public IPage<StudentInfo> getStudentList(String keyword, Long classId, int page, int size) {
        Long tenantId = UserContext.getTenantId();
        LambdaQueryWrapper<StudentInfo> wrapper = new LambdaQueryWrapper<StudentInfo>()
                .eq(StudentInfo::getTenantId, tenantId);
        if (classId != null) wrapper.eq(StudentInfo::getClassId, classId);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(StudentInfo::getRealName, keyword)
                    .or().like(StudentInfo::getStudentNo, keyword));
        }
        return studentInfoMapper.selectPage(new Page<>(page, size), wrapper);
    }

    public StudentInfo getStudentFullView(Long studentId) {
        return studentInfoMapper.selectById(studentId);
    }

    public IPage<TrainingPlan> getTrainingPlans(int page, int size) {
        Long tenantId = UserContext.getTenantId();
        return trainingPlanMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<TrainingPlan>()
                        .eq(TrainingPlan::getTenantId, tenantId)
                        .orderByDesc(TrainingPlan::getId));
    }

    @Transactional
    public void createTrainingPlan(CreateTrainingPlanRequest req) {
        Long tenantId = UserContext.getTenantId();
        TrainingPlan plan = new TrainingPlan();
        plan.setTenantId(tenantId);
        plan.setProjectId(req.getProjectId());
        plan.setPlanName(req.getPlanName());
        plan.setStartDate(req.getStartDate());
        plan.setEndDate(req.getEndDate());
        plan.setTeacherId(req.getTeacherId());
        plan.setStatus(1);
        trainingPlanMapper.insert(plan);
    }

    public IPage<WarningRecord> getWarnings(Integer status, int page, int size) {
        Long tenantId = UserContext.getTenantId();
        LambdaQueryWrapper<WarningRecord> wrapper = new LambdaQueryWrapper<WarningRecord>()
                .eq(WarningRecord::getTenantId, tenantId)
                .orderByDesc(WarningRecord::getId);
        if (status != null) wrapper.eq(WarningRecord::getStatus, status);
        return warningRecordMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Transactional
    public void interveneWarning(Long warningId, InterveneRequest req) {
        Long userId = UserContext.getUserId();
        WarningRecord record = warningRecordMapper.selectById(warningId);
        record.setStatus(1);
        record.setInterveneNote(req.getInterveneNote());
        record.setIntervenedBy(userId);
        record.setIntervenedAt(LocalDateTime.now());
        warningRecordMapper.updateById(record);
    }

    // ── Dashboard & Stats ─────────────────────────────────────────────────────

    public Map<String, Object> getEmploymentStats(String year) {
        Long tenantId = UserContext.getTenantId();
        long studentCount = studentInfoMapper.selectCount(
                new LambdaQueryWrapper<StudentInfo>().eq(StudentInfo::getTenantId, tenantId));
        Map<String, Object> stats = new HashMap<>();
        stats.put("student_count", studentCount);
        stats.put("internship_rate", 0);
        stats.put("employment_rate", 0);
        stats.put("year", year);
        return stats;
    }

    public Map<String, Object> getTrends(String dimension) {
        Map<String, Object> trends = new HashMap<>();
        trends.put("dimension", dimension);
        trends.put("data", List.of());
        return trends;
    }

    public Map<String, Object> getWarningStats() {
        Long tenantId = UserContext.getTenantId();
        long total = warningRecordMapper.selectCount(
                new LambdaQueryWrapper<WarningRecord>().eq(WarningRecord::getTenantId, tenantId));
        long pending = warningRecordMapper.selectCount(
                new LambdaQueryWrapper<WarningRecord>()
                        .eq(WarningRecord::getTenantId, tenantId)
                        .eq(WarningRecord::getStatus, 0));
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("pending", pending);
        stats.put("resolved", total - pending);
        return stats;
    }

    // ── CRM ───────────────────────────────────────────────────────────────────

    public List<Map<String, Object>> getCrmEnterprises(String level, String industry) {
        return new ArrayList<>();
    }

    public List<Map<String, Object>> getCrmAudits(String status) {
        return new ArrayList<>();
    }

    public void auditEnterprise(Long id, Map<String, Object> req) {
        // 企业审核逻辑（跨模块调用，后续通过 Feign 实现）
    }

    public void updateEnterpriseLevel(Long id, Map<String, Object> req) {
        // 更新企业合作等级
    }

    public List<Map<String, Object>> getVisitRecords(Long enterpriseId) {
        return new ArrayList<>();
    }

    public void createVisitRecord(Map<String, Object> req) {
        // 创建走访记录
    }

    // ── Internship ────────────────────────────────────────────────────────────

    public List<Map<String, Object>> getInternshipStudents(String status) {
        Long tenantId = UserContext.getTenantId();
        LambdaQueryWrapper<StudentInfo> wrapper = new LambdaQueryWrapper<StudentInfo>()
                .eq(StudentInfo::getTenantId, tenantId);
        List<StudentInfo> students = studentInfoMapper.selectList(wrapper);
        List<Map<String, Object>> result = new ArrayList<>();
        for (StudentInfo s : students) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", s.getId());
            item.put("name", s.getRealName());
            item.put("student_no", s.getStudentNo());
            result.add(item);
        }
        return result;
    }

    public List<Map<String, Object>> getPendingContracts() {
        return new ArrayList<>();
    }

    public void auditContract(Long id, Map<String, Object> req) {
        // 合同审核逻辑
    }

    public void createInspection(Map<String, Object> req) {
        // 创建巡查记录
    }

    public void assignMentor(Map<String, Object> req) {
        // 分配导师
    }
}
