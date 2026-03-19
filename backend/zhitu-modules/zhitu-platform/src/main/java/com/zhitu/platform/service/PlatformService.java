package com.zhitu.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhitu.platform.dto.AuditRequest;
import com.zhitu.platform.entity.EnterpriseInfo;
import com.zhitu.platform.entity.TrainingProject;
import com.zhitu.platform.mapper.EnterpriseInfoMapper;
import com.zhitu.platform.mapper.TrainingProjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PlatformService {

    private final TrainingProjectMapper trainingProjectMapper;
    private final EnterpriseInfoMapper enterpriseInfoMapper;

    public IPage<TrainingProject> getPendingProjects(int page, int size) {
        return trainingProjectMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<TrainingProject>()
                        .eq(TrainingProject::getAuditStatus, 0)
                        .orderByAsc(TrainingProject::getId));
    }

    @Transactional
    public void auditProject(Long projectId, AuditRequest req) {
        TrainingProject project = trainingProjectMapper.selectById(projectId);
        project.setAuditStatus(req.getAuditStatus());
        trainingProjectMapper.updateById(project);
    }

    public IPage<EnterpriseInfo> getPendingEnterprises(int page, int size) {
        return enterpriseInfoMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<EnterpriseInfo>()
                        .eq(EnterpriseInfo::getAuditStatus, 0)
                        .orderByAsc(EnterpriseInfo::getId));
    }

    @Transactional
    public void auditEnterprise(Long enterpriseId, AuditRequest req) {
        EnterpriseInfo info = enterpriseInfoMapper.selectById(enterpriseId);
        info.setAuditStatus(req.getAuditStatus());
        info.setAuditRemark(req.getAuditRemark());
        enterpriseInfoMapper.updateById(info);
    }

    // ── Dashboard ─────────────────────────────────────────────────────────────

    public Map<String, Object> getDashboardStats() {
        long enterpriseCount = enterpriseInfoMapper.selectCount(null);
        long projectCount = trainingProjectMapper.selectCount(null);
        Map<String, Object> stats = new HashMap<>();
        stats.put("enterprise_count", enterpriseCount);
        stats.put("project_count", projectCount);
        stats.put("college_count", 0);
        stats.put("student_count", 0);
        return stats;
    }

    // ── Tenants ───────────────────────────────────────────────────────────────

    public List<Map<String, Object>> getTenantList(String type, String status) {
        return new ArrayList<>();
    }

    // ── Tags ──────────────────────────────────────────────────────────────────

    public List<Map<String, Object>> getTags(String category) {
        return new ArrayList<>();
    }

    public void createTag(Map<String, Object> req) {}

    public void deleteTag(Long id) {}

    // ── Skill Tree ────────────────────────────────────────────────────────────

    public List<Map<String, Object>> getSkillTree() {
        return new ArrayList<>();
    }

    // ── Templates ─────────────────────────────────────────────────────────────

    public List<Map<String, Object>> getCertificateTemplates() {
        return new ArrayList<>();
    }

    public List<Map<String, Object>> getContractTemplates() {
        return new ArrayList<>();
    }

    // ── Logs ──────────────────────────────────────────────────────────────────

    public List<Map<String, Object>> getOperationLogs(String userId, String module,
            String result, String startTime, String endTime) {
        return new ArrayList<>();
    }

    public List<Map<String, Object>> getSecurityLogs(String level) {
        return new ArrayList<>();
    }

    // ── Recommendations ───────────────────────────────────────────────────────

    public List<Map<String, Object>> getRecommendationBanners() {
        return new ArrayList<>();
    }

    public void saveRecommendationBanner(Map<String, Object> req) {}

    public List<Map<String, Object>> getTopListItems(String listType) {
        return new ArrayList<>();
    }

    public void saveTopListItems(Map<String, Object> req) {}
}
