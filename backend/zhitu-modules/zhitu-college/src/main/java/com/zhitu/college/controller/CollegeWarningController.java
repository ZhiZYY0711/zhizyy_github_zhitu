package com.zhitu.college.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhitu.college.dto.InterveneRequest;
import com.zhitu.college.entity.WarningRecord;
import com.zhitu.college.service.CollegeService;
import com.zhitu.common.core.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 高校预警管理接口
 * GET  /api/portal-college/v1/warnings           - 预警学生列表
 * POST /api/portal-college/v1/warnings/{id}/intervene - 预警干预
 */
@RestController
@RequestMapping("/api/portal-college/v1/warnings")
@RequiredArgsConstructor
public class CollegeWarningController {

    private final CollegeService collegeService;

    @GetMapping
    public Result<IPage<WarningRecord>> getWarnings(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(collegeService.getWarnings(status, page, size));
    }

    @PostMapping("/{id}/intervene")
    public Result<Void> intervene(@PathVariable Long id,
                                  @Valid @RequestBody InterveneRequest req) {
        collegeService.interveneWarning(id, req);
        return Result.ok();
    }
}
