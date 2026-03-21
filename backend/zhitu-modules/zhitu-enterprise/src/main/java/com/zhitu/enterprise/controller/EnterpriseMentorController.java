package com.zhitu.enterprise.controller;

import com.zhitu.common.core.result.Result;
import com.zhitu.enterprise.dto.MentorDashboardDTO;
import com.zhitu.enterprise.service.EnterpriseMentorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 企业导师接口
 * GET /api/portal-enterprise/v1/mentor/dashboard - 获取导师仪表板数据
 */
@RestController
@RequestMapping("/api/portal-enterprise/v1/mentor")
@RequiredArgsConstructor
public class EnterpriseMentorController {

    private final EnterpriseMentorService enterpriseMentorService;

    /**
     * 获取导师仪表板数据
     * 包括：分配的实习生数量、待批阅周报数量、待审核代码评审数量、最近的实习生活动
     * 
     * 多租户隔离：通过UserContext获取当前导师用户ID，所有查询都按mentor_id过滤
     * 缓存策略：使用Redis缓存，TTL为5分钟
     * 
     * @return 导师仪表板数据
     */
    @GetMapping("/dashboard")
    public Result<MentorDashboardDTO> getDashboard() {
        return Result.ok(enterpriseMentorService.getDashboard());
    }
}
