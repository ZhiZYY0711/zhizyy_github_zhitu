package com.zhitu.enterprise.controller;

import com.zhitu.common.core.result.Result;
import com.zhitu.enterprise.dto.CodeReviewDTO;
import com.zhitu.enterprise.service.EnterpriseMentorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 导师实训接口
 * 处理 /api/training/v1/mentor/** 路径下的请求
 */
@RestController
@RequestMapping("/api/training/v1/mentor")
@RequiredArgsConstructor
@Tag(name = "导师实训接口", description = "导师代码评审相关接口")
public class MentorTrainingController {

    private final EnterpriseMentorService enterpriseMentorService;

    /**
     * 获取代码评审列表
     * 
     * @param status 状态过滤（可选）：pending/resolved/closed
     * @return 代码评审列表
     */
    @GetMapping("/code-reviews")
    @Operation(summary = "获取代码评审列表", description = "获取导师待处理的代码评审列表")
    public Result<List<CodeReviewDTO>> getCodeReviews(
            @Parameter(description = "状态过滤：pending/resolved/closed")
            @RequestParam(required = false) String status) {
        return Result.ok(enterpriseMentorService.getCodeReviews(status));
    }

    /**
     * 提交代码评审意见
     * 
     * @param id 评审ID
     * @param request 评审请求体
     * @return 操作结果
     */
    @PostMapping("/code-reviews/{id}/comment")
    @Operation(summary = "提交代码评审意见", description = "导师对代码评审提交意见")
    public Result<Void> submitCodeReview(
            @Parameter(description = "评审ID") @PathVariable Long id,
            @RequestBody CodeReviewCommentRequest request) {
        enterpriseMentorService.submitCodeReview(id, request.getComment());
        return Result.ok();
    }

    /**
     * 代码评审意见请求体
     */
    @lombok.Data
    public static class CodeReviewCommentRequest {
        private String comment;
    }
}
