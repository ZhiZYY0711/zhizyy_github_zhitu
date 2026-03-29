package com.zhitu.enterprise.controller;

import com.zhitu.common.core.result.Result;
import com.zhitu.enterprise.dto.CreateTrainingProjectRequest;
import com.zhitu.enterprise.dto.ProjectTeamDTO;
import com.zhitu.enterprise.dto.TrainingProjectDTO;
import com.zhitu.enterprise.service.EnterpriseTrainingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 企业实训项目接口
 * GET /api/training/v1/enterprise/projects - 获取企业实训项目列表
 * POST /api/training/v1/enterprise/projects - 创建实训项目
 * GET /api/training/v1/enterprise/projects/{id}/teams - 获取项目团队列表
 */
@RestController
@RequestMapping("/api/training/v1/enterprise")
@RequiredArgsConstructor
public class EnterpriseTrainingController {

    private final EnterpriseTrainingService enterpriseTrainingService;

    /**
     * 获取企业实训项目列表
     */
    @GetMapping("/projects")
    public Result<List<TrainingProjectDTO>> getProjects() {
        return Result.ok(enterpriseTrainingService.getProjects());
    }

    /**
     * 创建实训项目
     */
    @PostMapping("/projects")
    public Result<Void> createProject(@RequestBody CreateTrainingProjectRequest request) {
        enterpriseTrainingService.createProject(request);
        return Result.ok();
    }

    /**
     * 获取项目团队列表
     */
    @GetMapping("/projects/{id}/teams")
    public Result<List<ProjectTeamDTO>> getProjectTeams(@PathVariable Long id) {
        return Result.ok(enterpriseTrainingService.getProjectTeams(id));
    }
}
