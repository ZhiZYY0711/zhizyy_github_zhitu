package com.zhitu.student.controller;

import com.zhitu.common.core.result.Result;
import com.zhitu.student.dto.EvaluationRequest;
import com.zhitu.student.entity.EvaluationRecord;
import com.zhitu.student.entity.GrowthBadge;
import com.zhitu.student.service.GrowthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 成长评价接口
 * GET  /api/growth/v1/evaluations          - 学生查看自己的成绩单
 * GET  /api/growth/v1/evaluations/student/{studentId} - 高校/企业查看指定学生
 * POST /api/growth/v1/evaluations/enterprise - 企业端写入评价
 * POST /api/growth/v1/evaluations/school     - 高校端写入评价
 * POST /api/training/v1/reviews/peer         - 同学互评
 * GET  /api/growth/v1/badges               - 学生查看证书与徽章
 * POST /api/growth/v1/badges               - 颁发证书/徽章
 */
@RestController
@RequiredArgsConstructor
public class GrowthController {

    private final GrowthService growthService;

    @GetMapping("/api/growth/v1/evaluations")
    public Result<List<EvaluationRecord>> getMyEvaluations() {
        return Result.ok(growthService.getMyEvaluations());
    }

    @GetMapping("/api/growth/v1/evaluations/student/{studentId}")
    public Result<List<EvaluationRecord>> getStudentEvaluations(@PathVariable Long studentId) {
        return Result.ok(growthService.getStudentEvaluations(studentId));
    }

    @PostMapping("/api/growth/v1/evaluations/enterprise")
    public Result<Void> submitEnterpriseEvaluation(@Valid @RequestBody EvaluationRequest req) {
        growthService.submitEvaluation(req, "enterprise");
        return Result.ok();
    }

    @PostMapping("/api/growth/v1/evaluations/school")
    public Result<Void> submitSchoolEvaluation(@Valid @RequestBody EvaluationRequest req) {
        growthService.submitEvaluation(req, "school");
        return Result.ok();
    }

    @PostMapping("/api/training/v1/reviews/peer")
    public Result<Void> submitPeerReview(@Valid @RequestBody EvaluationRequest req) {
        growthService.submitEvaluation(req, "peer");
        return Result.ok();
    }

    @GetMapping("/api/growth/v1/badges")
    public Result<List<GrowthBadge>> getMyBadges() {
        return Result.ok(growthService.getMyBadges());
    }

    @PostMapping("/api/growth/v1/badges")
    public Result<Void> issueBadge(@RequestBody GrowthBadge badge) {
        growthService.issueBadge(badge);
        return Result.ok();
    }
}
