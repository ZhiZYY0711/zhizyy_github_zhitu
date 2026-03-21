package com.zhitu.student.controller;

import com.zhitu.common.core.result.PageResult;
import com.zhitu.common.core.result.Result;
import com.zhitu.student.dto.*;
import com.zhitu.student.service.StudentPortalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学生门户接口
 * GET /api/student-portal/v1/dashboard - 获取学生仪表板统计数据
 * GET /api/student-portal/v1/capability/radar - 获取学生能力雷达图数据
 * GET /api/student-portal/v1/tasks - 获取学生任务列表（支持状态过滤和分页）
 * GET /api/student-portal/v1/recommendations - 获取学生个性化推荐
 */
@RestController
@RequestMapping("/api/student-portal/v1")
@RequiredArgsConstructor
public class StudentPortalController {

    private final StudentPortalService studentPortalService;

    /**
     * 获取学生仪表板统计数据
     * 包括：实训项目数、实习岗位数、待办任务数、成长分数
     *
     * @return 仪表板统计数据
     */
    @GetMapping("/dashboard")
    public Result<DashboardStatsDTO> getDashboard() {
        return Result.ok(studentPortalService.getDashboardStats());
    }

    /**
     * 获取学生能力雷达图数据
     * 包括五个维度：technical_skill, communication, teamwork, problem_solving, innovation
     * 
     * @return 能力雷达图数据
     */
    @GetMapping("/capability/radar")
    public Result<CapabilityRadarDTO> getCapabilityRadar() {
        return Result.ok(studentPortalService.getCapabilityRadar());
    }

    /**
     * 获取学生任务列表
     * 支持按状态过滤（pending/completed）和分页
     * 
     * @param status 任务状态：pending 或 completed（可选）
     * @param page 页码（默认1）
     * @param size 每页大小（默认10）
     * @return 分页的任务列表
     */
    @GetMapping("/tasks")
    public Result<PageResult<TaskDTO>> getTasks(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.ok(studentPortalService.getTasks(status, page, size));
    }

    /**
     * 获取学生个性化推荐
     * 支持按类型过滤：all / project / job / course
     * 使用Redis缓存，TTL为15分钟
     * 
     * @param type 推荐类型：all, project, job, course（默认all）
     * @return 推荐列表
     */
    @GetMapping("/recommendations")
    public Result<List<RecommendationDTO>> getRecommendations(
            @RequestParam(defaultValue = "all") String type) {
        return Result.ok(studentPortalService.getRecommendations(type));
    }

    /**
     * 获取实训项目列表
     * 查询开放状态的实训项目，并标注学生的报名状态
     * 使用Redis缓存，TTL为5分钟
     * 
     * @param page 页码（默认1）
     * @param size 每页大小（默认10）
     * @return 分页的实训项目列表
     */
    @GetMapping("/training/projects")
    public Result<PageResult<TrainingProjectDTO>> getTrainingProjects(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.ok(studentPortalService.getTrainingProjects(page, size));
    }

    /**
     * 获取项目看板（Scrum Board）
     * 验证学生是否已报名该项目，未报名返回403错误
     * 
     * @param id 项目ID
     * @return 看板数据
     */
    @GetMapping("/training/projects/{id}/board")
    public Result<ScrumBoardDTO> getProjectBoard(@PathVariable Long id) {
        return Result.ok(studentPortalService.getProjectBoard(id));
    }

    /**
     * 获取实习岗位列表
     * 查询开放状态的实习岗位，并标注学生的申请状态
     * 使用Redis缓存，TTL为5分钟
     * 
     * @param page 页码（默认1）
     * @param size 每页大小（默认10）
     * @return 分页的实习岗位列表
     */
    @GetMapping("/internship/jobs")
    public Result<PageResult<InternshipJobDTO>> getInternshipJobs(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.ok(studentPortalService.getInternshipJobs(page, size));
    }

    /**
     * 获取学生的周报列表
     * 按提交日期降序排列
     * 
     * @param page 页码（默认1）
     * @param size 每页大小（默认10）
     * @return 分页的周报列表
     */
    @GetMapping("/internship/reports/my")
    public Result<PageResult<WeeklyReportDTO>> getMyReports(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.ok(studentPortalService.getMyReports(page, size));
    }

    /**
     * 获取学生的成长评价汇总
     * 查询所有评价记录并计算平均分
     * 
     * @return 评价汇总数据
     */
    @GetMapping("/growth/evaluation")
    public Result<EvaluationSummaryDTO> getEvaluationSummary() {
        return Result.ok(studentPortalService.getEvaluationSummary());
    }

    /**
     * 获取学生的证书列表
     * 按颁发日期降序排列
     * 
     * @param page 页码（默认1）
     * @param size 每页大小（默认10）
     * @return 分页的证书列表
     */
    @GetMapping("/growth/certificates")
    public Result<PageResult<CertificateDTO>> getMyCertificates(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.ok(studentPortalService.getMyCertificates(page, size));
    }

    /**
     * 获取学生的徽章列表
     * 按获得日期降序排列
     * 
     * @param page 页码（默认1）
     * @param size 每页大小（默认10）
     * @return 分页的徽章列表
     */
    @GetMapping("/growth/badges")
    public Result<PageResult<BadgeDTO>> getMyBadges(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.ok(studentPortalService.getMyBadges(page, size));
    }
}
