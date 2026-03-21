package com.zhitu.enterprise.controller;

import com.zhitu.common.core.result.PageResult;
import com.zhitu.common.core.result.Result;
import com.zhitu.enterprise.dto.CreateJobRequest;
import com.zhitu.enterprise.dto.JobDTO;
import com.zhitu.enterprise.service.EnterpriseJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 企业岗位管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/internship/v1/enterprise/jobs")
@RequiredArgsConstructor
public class EnterpriseJobController {

    private final EnterpriseJobService enterpriseJobService;

    /**
     * 获取企业岗位列表
     * 
     * @param status 岗位状态 (可选: 1=招募中, 0=已关闭)
     * @param page 页码（从1开始，默认1）
     * @param size 每页大小（默认10）
     * @return 分页的岗位列表
     */
    @GetMapping
    public Result<PageResult<JobDTO>> getJobs(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        log.debug("GET /api/internship/v1/enterprise/jobs - status: {}, page: {}, size: {}", status, page, size);
        
        PageResult<JobDTO> result = enterpriseJobService.getJobs(status, page, size);
        return Result.ok(result);
    }

    /**
     * 创建岗位
     * 
     * @param request 创建岗位请求
     * @return 创建的岗位ID
     */
    @PostMapping
    public Result<Map<String, Long>> createJob(@RequestBody CreateJobRequest request) {
        log.debug("POST /api/internship/v1/enterprise/jobs - title: {}", request.getJobTitle());
        
        Long jobId = enterpriseJobService.createJob(request);
        return Result.ok(Map.of("id", jobId));
    }

    /**
     * 关闭岗位
     * 
     * @param id 岗位ID
     * @return 成功响应
     */
    @PostMapping("/{id}/close")
    public Result<Void> closeJob(@PathVariable Long id) {
        log.debug("POST /api/internship/v1/enterprise/jobs/{}/close", id);
        
        enterpriseJobService.closeJob(id);
        return Result.ok();
    }
}
