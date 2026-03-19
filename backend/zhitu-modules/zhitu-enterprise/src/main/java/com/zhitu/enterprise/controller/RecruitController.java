package com.zhitu.enterprise.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhitu.common.core.result.Result;
import com.zhitu.enterprise.dto.PublishJobRequest;
import com.zhitu.enterprise.dto.SendOfferRequest;
import com.zhitu.enterprise.dto.TalentCollectRequest;
import com.zhitu.enterprise.entity.InternshipJob;
import com.zhitu.enterprise.entity.JobApplication;
import com.zhitu.enterprise.service.RecruitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 企业招聘接口
 * POST /api/internship/v1/enterprise/jobs          - 发布职位
 * GET  /api/internship/v1/enterprise/jobs          - 职位列表
 * GET  /api/internship/v1/enterprise/applications  - 候选人简历列表
 * POST /api/internship/v1/enterprise/offers        - 发送 Offer
 * POST /api/portal-enterprise/v1/talent-pool/collect - 加入人才库
 */
@RestController
@RequiredArgsConstructor
public class RecruitController {

    private final RecruitService recruitService;

    @PostMapping("/api/internship/v1/enterprise/jobs")
    public Result<Void> publishJob(@Valid @RequestBody PublishJobRequest req) {
        recruitService.publishJob(req);
        return Result.ok();
    }

    @GetMapping("/api/internship/v1/enterprise/jobs")
    public Result<IPage<InternshipJob>> getJobs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(recruitService.getJobList(page, size));
    }

    @GetMapping("/api/internship/v1/enterprise/applications")
    public Result<IPage<JobApplication>> getApplications(
            @RequestParam(required = false) Long jobId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(recruitService.getApplicationList(jobId, page, size));
    }

    @PostMapping("/api/internship/v1/enterprise/offers")
    public Result<Void> sendOffer(@Valid @RequestBody SendOfferRequest req) {
        recruitService.sendOffer(req);
        return Result.ok();
    }

    @PostMapping("/api/portal-enterprise/v1/talent-pool/collect")
    public Result<Void> collectTalent(@Valid @RequestBody TalentCollectRequest req) {
        recruitService.collectTalent(req);
        return Result.ok();
    }

    @PostMapping("/api/internship/v1/enterprise/jobs/{id}/close")
    public Result<Void> closeJob(@PathVariable Long id) {
        recruitService.closeJob(id);
        return Result.ok();
    }

    @PostMapping("/api/internship/v1/enterprise/applications/{id}/reject")
    public Result<Void> rejectApplication(@PathVariable Long id) {
        recruitService.rejectApplication(id);
        return Result.ok();
    }

    @PostMapping("/api/internship/v1/enterprise/interviews")
    public Result<Void> scheduleInterview(@RequestBody Map<String, Object> req) {
        recruitService.scheduleInterview(req);
        return Result.ok();
    }
}
