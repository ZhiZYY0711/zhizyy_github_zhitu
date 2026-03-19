package com.zhitu.enterprise.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhitu.common.core.context.UserContext;
import com.zhitu.enterprise.dto.AddStaffRequest;
import com.zhitu.enterprise.dto.EnterpriseProfileUpdateRequest;
import com.zhitu.enterprise.entity.EnterpriseInfo;
import com.zhitu.enterprise.entity.EnterpriseStaff;
import com.zhitu.enterprise.mapper.EnterpriseInfoMapper;
import com.zhitu.enterprise.mapper.EnterpriseStaffMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EnterpriseService {

    private final EnterpriseInfoMapper enterpriseInfoMapper;
    private final EnterpriseStaffMapper enterpriseStaffMapper;

    public EnterpriseInfo getProfile() {
        Long tenantId = UserContext.getTenantId();
        return enterpriseInfoMapper.selectOne(
                new LambdaQueryWrapper<EnterpriseInfo>()
                        .eq(EnterpriseInfo::getTenantId, tenantId));
    }

    @Transactional
    public void updateProfile(EnterpriseProfileUpdateRequest req) {
        Long tenantId = UserContext.getTenantId();
        EnterpriseInfo info = enterpriseInfoMapper.selectOne(
                new LambdaQueryWrapper<EnterpriseInfo>()
                        .eq(EnterpriseInfo::getTenantId, tenantId));
        if (info == null) return;
        info.setEnterpriseName(req.getEnterpriseName());
        info.setIndustry(req.getIndustry());
        info.setScale(req.getScale());
        info.setProvince(req.getProvince());
        info.setCity(req.getCity());
        info.setAddress(req.getAddress());
        info.setLogoUrl(req.getLogoUrl());
        info.setWebsite(req.getWebsite());
        info.setDescription(req.getDescription());
        info.setContactName(req.getContactName());
        info.setContactPhone(req.getContactPhone());
        info.setContactEmail(req.getContactEmail());
        enterpriseInfoMapper.updateById(info);
    }

    public List<EnterpriseStaff> getStaffList() {
        Long tenantId = UserContext.getTenantId();
        return enterpriseStaffMapper.selectList(
                new LambdaQueryWrapper<EnterpriseStaff>()
                        .eq(EnterpriseStaff::getTenantId, tenantId));
    }

    @Transactional
    public void addStaff(AddStaffRequest req) {
        Long tenantId = UserContext.getTenantId();
        EnterpriseStaff staff = new EnterpriseStaff();
        staff.setTenantId(tenantId);
        staff.setUserId(req.getUserId());
        staff.setDepartment(req.getDepartment());
        staff.setPosition(req.getPosition());
        staff.setIsMentor(Boolean.TRUE.equals(req.getIsMentor()));
        enterpriseStaffMapper.insert(staff);
    }

    public Map<String, Object> getDashboardStats() {
        Long tenantId = UserContext.getTenantId();
        long staffCount = enterpriseStaffMapper.selectCount(
                new LambdaQueryWrapper<EnterpriseStaff>().eq(EnterpriseStaff::getTenantId, tenantId));
        Map<String, Object> stats = new HashMap<>();
        stats.put("staff_count", staffCount);
        stats.put("active_jobs", 0);
        stats.put("pending_applications", 0);
        stats.put("active_interns", 0);
        return stats;
    }

    public List<Map<String, Object>> getTodos() {
        return new ArrayList<>();
    }

    public List<Map<String, Object>> getActivities() {
        return new ArrayList<>();
    }

    public Map<String, Object> getAnalytics(String range) {
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("range", range);
        analytics.put("hire_trend", List.of());
        analytics.put("department_distribution", List.of());
        return analytics;
    }

    public Map<String, Object> getMentorDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("mentoring_count", 0);
        dashboard.put("pending_reports", 0);
        dashboard.put("pending_reviews", 0);
        return dashboard;
    }
}
