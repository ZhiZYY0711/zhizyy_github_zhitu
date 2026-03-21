package com.zhitu.enterprise.controller;

import com.zhitu.common.core.result.PageResult;
import com.zhitu.common.core.result.Result;
import com.zhitu.enterprise.dto.ApplicationDTO;
import com.zhitu.enterprise.dto.InterviewDTO;
import com.zhitu.enterprise.dto.ScheduleInterviewRequest;
import com.zhitu.enterprise.service.EnterpriseApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 企业申请管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/internship/v1/enterprise")
@RequiredArgsConstructor
public class EnterpriseApplicationController {

    private final EnterpriseApplicationService applicationService;

    /**
     * 获取岗位申请列表
     * 
     * @param jobId 岗位ID (可选)
     * @param status 申请状态 (可选)
     * @param page 页码
     * @param size 每页大小
     * @return 分页的申请列表
     */
    @GetMapping("/applications")
    public Result<PageResult<ApplicationDTO>> getApplications(
            @RequestParam(required = false) Long jobId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        log.info("Getting applications - jobId: {}, status: {}, page: {}, size: {}", 
            jobId, status, page, size);
        
        PageResult<ApplicationDTO> result = applicationService.getApplications(jobId, status, page, size);
        return Result.ok(result);
    }

    /**
     * 安排面试
     * 
     * @param request 面试安排请求
     * @return 面试ID
     */
    @PostMapping("/interviews")
    public Result<InterviewDTO> scheduleInterview(@RequestBody ScheduleInterviewRequest request) {
        log.info("Scheduling interview for application: {}", request.getApplicationId());
        
        InterviewDTO result = applicationService.scheduleInterview(request);
        return Result.ok(result);
    }
}
