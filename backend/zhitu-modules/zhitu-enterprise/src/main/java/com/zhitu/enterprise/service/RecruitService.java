package com.zhitu.enterprise.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhitu.common.core.context.UserContext;
import com.zhitu.enterprise.dto.PublishJobRequest;
import com.zhitu.enterprise.dto.SendOfferRequest;
import com.zhitu.enterprise.dto.TalentCollectRequest;
import com.zhitu.enterprise.entity.*;
import com.zhitu.enterprise.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RecruitService {

    private final InternshipJobMapper jobMapper;
    private final JobApplicationMapper applicationMapper;
    private final InternshipOfferMapper offerMapper;
    private final TalentPoolMapper talentPoolMapper;

    @Transactional
    public void publishJob(PublishJobRequest req) {
        Long tenantId = UserContext.getTenantId();
        InternshipJob job = new InternshipJob();
        job.setEnterpriseId(tenantId);
        job.setJobTitle(req.getJobTitle());
        job.setJobType("internship");
        job.setDescription(req.getDescription());
        job.setRequirements(req.getRequirements());
        job.setTechStack(req.getTechStack());
        job.setIndustry(req.getIndustry());
        job.setCity(req.getCity());
        job.setSalaryMin(req.getSalaryMin());
        job.setSalaryMax(req.getSalaryMax());
        job.setHeadcount(req.getHeadcount());
        job.setStartDate(req.getStartDate());
        job.setEndDate(req.getEndDate());
        job.setStatus(1);
        jobMapper.insert(job);
    }

    public IPage<InternshipJob> getJobList(int page, int size) {
        Long tenantId = UserContext.getTenantId();
        return jobMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<InternshipJob>()
                        .eq(InternshipJob::getEnterpriseId, tenantId)
                        .orderByDesc(InternshipJob::getId));
    }

    public IPage<JobApplication> getApplicationList(Long jobId, int page, int size) {
        Long tenantId = UserContext.getTenantId();
        LambdaQueryWrapper<JobApplication> wrapper = new LambdaQueryWrapper<JobApplication>()
                .orderByDesc(JobApplication::getAppliedAt);
        if (jobId != null) {
            wrapper.eq(JobApplication::getJobId, jobId);
        }
        return applicationMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Transactional
    public void sendOffer(SendOfferRequest req) {
        Long tenantId = UserContext.getTenantId();
        JobApplication app = applicationMapper.selectById(req.getApplicationId());
        InternshipOffer offer = new InternshipOffer();
        offer.setApplicationId(req.getApplicationId());
        offer.setStudentId(app.getStudentId());
        offer.setEnterpriseId(tenantId);
        offer.setJobId(app.getJobId());
        offer.setSalary(req.getSalary());
        offer.setStartDate(req.getStartDate());
        offer.setEndDate(req.getEndDate());
        offer.setStatus(0);
        offer.setCollegeAudit(0);
        offerMapper.insert(offer);
        // 更新申请状态为 Offer
        app.setStatus(2);
        applicationMapper.updateById(app);
    }

    @Transactional
    public void collectTalent(TalentCollectRequest req) {
        Long tenantId = UserContext.getTenantId();
        Long collectedBy = UserContext.getUserId();
        TalentPool tp = new TalentPool();
        tp.setTenantId(tenantId);
        tp.setStudentId(req.getStudentId());
        tp.setCollectedBy(collectedBy);
        tp.setRemark(req.getRemark());
        talentPoolMapper.insert(tp);
    }

    @Transactional
    public void closeJob(Long jobId) {
        InternshipJob job = jobMapper.selectById(jobId);
        if (job != null) {
            job.setStatus(0); // 0=关闭
            jobMapper.updateById(job);
        }
    }

    @Transactional
    public void rejectApplication(Long applicationId) {
        JobApplication app = applicationMapper.selectById(applicationId);
        if (app != null) {
            app.setStatus(3); // 3=拒绝
            applicationMapper.updateById(app);
        }
    }

    public void scheduleInterview(Map<String, Object> req) {
        // 面试安排：更新申请状态为面试中，实际项目可扩展为独立 interview 表
        Object appIdObj = req.get("application_id");
        if (appIdObj != null) {
            Long appId = Long.valueOf(appIdObj.toString());
            JobApplication app = applicationMapper.selectById(appId);
            if (app != null) {
                app.setStatus(1); // 1=面试中
                applicationMapper.updateById(app);
            }
        }
    }

    public List<Map<String, Object>> getTalentPool() {
        Long tenantId = UserContext.getTenantId();
        List<TalentPool> list = talentPoolMapper.selectList(
                new LambdaQueryWrapper<TalentPool>()
                        .eq(TalentPool::getTenantId, tenantId));
        List<Map<String, Object>> result = new ArrayList<>();
        for (TalentPool tp : list) {
            result.add(Map.of(
                    "id", tp.getId(),
                    "student_id", tp.getStudentId(),
                    "remark", tp.getRemark() != null ? tp.getRemark() : ""
            ));
        }
        return result;
    }

    public void removeFromTalentPool(Long id) {
        talentPoolMapper.deleteById(id);
    }
}
