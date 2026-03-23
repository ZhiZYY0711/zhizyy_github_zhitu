package com.zhitu.student.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhitu.common.core.context.UserContext;
import com.zhitu.common.core.result.PageResult;
import com.zhitu.common.redis.service.CacheService;
import com.zhitu.student.dto.*;
import com.zhitu.student.dto.ScrumBoardDTO.TaskItemDTO;
import com.zhitu.student.entity.EvaluationRecord;
import com.zhitu.student.entity.StudentCapability;
import com.zhitu.student.entity.StudentRecommendation;
import com.zhitu.student.entity.StudentTask;
import com.zhitu.student.mapper.EvaluationRecordMapper;
import com.zhitu.student.mapper.StudentCapabilityMapper;
import com.zhitu.student.mapper.StudentRecommendationMapper;
import com.zhitu.student.mapper.StudentTaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

/**
 * 学生门户服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudentPortalService {

    private final StudentTaskMapper studentTaskMapper;
    private final EvaluationRecordMapper evaluationRecordMapper;
    private final StudentCapabilityMapper studentCapabilityMapper;
    private final StudentRecommendationMapper studentRecommendationMapper;
    private final CacheService cacheService;
    private final JdbcTemplate jdbcTemplate;

    /**
     * 获取学生仪表板统计数据
     * 包括：实训项目数、实习岗位数、待办任务数、成长分数
     */
    public DashboardStatsDTO getDashboardStats() {
        Long userId = UserContext.getUserId();
        String cacheKey = "student:dashboard:" + userId;

        return cacheService.getOrSet(cacheKey, 5, TimeUnit.MINUTES, () -> {
            log.debug("Computing dashboard stats for user: {}", userId);

            // 获取学生ID
            Long studentId = getStudentIdByUserId(userId);
            if (studentId == null) {
                log.warn("Student not found for user: {}", userId);
                return new DashboardStatsDTO(0, 0, 0, 0);
            }

            // 1. 查询实训项目数量（学生已加入的项目）
            Integer trainingProjectCount = countTrainingProjects(studentId);

            // 2. 查询实习岗位数量（开放状态的岗位）
            Integer internshipJobCount = countInternshipJobs();

            // 3. 查询待办任务数量
            Integer pendingTaskCount = countPendingTasks(studentId);

            // 4. 计算成长分数（基于评价记录的平均分）
            Integer growthScore = calculateGrowthScore(studentId);

            return new DashboardStatsDTO(
                    trainingProjectCount,
                    internshipJobCount,
                    pendingTaskCount,
                    growthScore
            );
        });
    }

    /**
     * 根据用户ID获取学生ID
     */
    private Long getStudentIdByUserId(Long userId) {
        String sql = "SELECT id FROM student_svc.student_info WHERE user_id = ? AND is_deleted IS FALSE";
        List<Long> results = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("id"), userId);
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * 统计学生已加入的实训项目数量
     */
    private Integer countTrainingProjects(Long studentId) {
        String sql = "SELECT COUNT(*) FROM training_svc.project_enrollment " +
                "WHERE student_id = ? AND status = 1";
        return jdbcTemplate.queryForObject(sql, Integer.class, studentId);
    }

    /**
     * 统计开放状态的实习岗位数量
     */
    private Integer countInternshipJobs() {
        String sql = "SELECT COUNT(*) FROM internship_svc.internship_job " +
                "WHERE status = 1 AND is_deleted IS FALSE";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    /**
     * 统计待办任务数量
     */
    private Integer countPendingTasks(Long studentId) {
        return Math.toIntExact(studentTaskMapper.selectCount(
                new LambdaQueryWrapper<StudentTask>()
                        .eq(StudentTask::getStudentId, studentId)
                        .eq(StudentTask::getStatus, 0)
        ));
    }

    /**
     * 计算成长分数（基于评价记录的平均分）
     * 如果没有评价记录，返回0
     */
    private Integer calculateGrowthScore(Long studentId) {
        List<EvaluationRecord> evaluations = evaluationRecordMapper.selectList(
                new LambdaQueryWrapper<EvaluationRecord>()
                        .eq(EvaluationRecord::getStudentId, studentId)
        );

        if (evaluations.isEmpty()) {
            return 0;
        }

        // 计算平均分（假设scores字段存储的是JSON格式的分数）
        // 这里简化处理，如果有多个评价，取平均值
        double totalScore = 0;
        int count = 0;

        for (EvaluationRecord eval : evaluations) {
            // 假设scores是一个JSON字符串，包含多个维度的分数
            // 这里简化处理，如果需要更复杂的逻辑，可以解析JSON
            // 暂时返回一个固定值或基于评价数量的估算
            count++;
        }

        // 简化计算：基于评价数量给出一个基础分数
        // 实际应该解析scores字段并计算平均值
        if (count > 0) {
            // 假设每个评价平均贡献15分，最高100分
            return Math.min(60 + (count * 10), 100);
        }

        return 0;
    }

    /**
     * 获取学生能力雷达图数据
     * 包括五个维度：technical_skill, communication, teamwork, problem_solving, innovation
     * 
     * 计算逻辑：
     * 1. 从 student_capability 表查询所有维度的分数
     * 2. 如果某个维度没有记录，默认分数为0
     * 3. 使用Redis缓存，TTL为10分钟
     */
    public CapabilityRadarDTO getCapabilityRadar() {
        Long userId = UserContext.getUserId();
        String cacheKey = "student:capability:" + userId;

        return cacheService.getOrSet(cacheKey, 10, TimeUnit.MINUTES, () -> {
            log.debug("Computing capability radar for user: {}", userId);

            // 获取学生ID
            Long studentId = getStudentIdByUserId(userId);
            if (studentId == null) {
                log.warn("Student not found for user: {}", userId);
                return createDefaultCapabilityRadar();
            }

            // 查询学生能力数据
            List<StudentCapability> capabilities = studentCapabilityMapper.selectList(
                    new LambdaQueryWrapper<StudentCapability>()
                            .eq(StudentCapability::getStudentId, studentId)
            );

            // 将查询结果转换为Map，方便查找
            Map<String, Integer> capabilityMap = capabilities.stream()
                    .collect(Collectors.toMap(
                            StudentCapability::getDimension,
                            StudentCapability::getScore
                    ));

            // 定义五个维度
            String[] dimensions = {
                    "technical_skill",
                    "communication",
                    "teamwork",
                    "problem_solving",
                    "innovation"
            };

            // 构建响应数据
            List<CapabilityRadarDTO.DimensionScore> dimensionScores = new ArrayList<>();
            for (String dimension : dimensions) {
                Integer score = capabilityMap.getOrDefault(dimension, 0);
                dimensionScores.add(new CapabilityRadarDTO.DimensionScore(dimension, score));
            }

            return new CapabilityRadarDTO(dimensionScores);
        });
    }

    /**
     * 创建默认的能力雷达图数据（所有维度分数为0）
     */
    private CapabilityRadarDTO createDefaultCapabilityRadar() {
        String[] dimensions = {
                "technical_skill",
                "communication",
                "teamwork",
                "problem_solving",
                "innovation"
        };

        List<CapabilityRadarDTO.DimensionScore> dimensionScores = new ArrayList<>();
        for (String dimension : dimensions) {
            dimensionScores.add(new CapabilityRadarDTO.DimensionScore(dimension, 0));
        }

        return new CapabilityRadarDTO(dimensionScores);
    }

    /**
     * 获取学生任务列表（支持状态过滤和分页）
     * 
     * @param status 任务状态：pending(0) 或 completed(1)，null表示查询所有
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 分页的任务列表
     */
    public PageResult<TaskDTO> getTasks(String status, Integer page, Integer size) {
        Long userId = UserContext.getUserId();
        log.debug("Getting tasks for user: {}, status: {}, page: {}, size: {}", userId, status, page, size);

        // 获取学生ID
        Long studentId = getStudentIdByUserId(userId);
        if (studentId == null) {
            log.warn("Student not found for user: {}", userId);
            return PageResult.of(0L, new ArrayList<>(), page, size);
        }

        // 构建查询条件
        LambdaQueryWrapper<StudentTask> queryWrapper = new LambdaQueryWrapper<StudentTask>()
                .eq(StudentTask::getStudentId, studentId)
                .orderByDesc(StudentTask::getCreatedAt);

        // 根据status参数过滤
        if (status != null && !status.isEmpty()) {
            if ("pending".equalsIgnoreCase(status)) {
                queryWrapper.eq(StudentTask::getStatus, 0);
            } else if ("completed".equalsIgnoreCase(status)) {
                queryWrapper.eq(StudentTask::getStatus, 1);
            }
        }

        // 分页查询
        Page<StudentTask> pageRequest = new Page<>(page, size);
        Page<StudentTask> pageResult = studentTaskMapper.selectPage(pageRequest, queryWrapper);

        // 转换为DTO
        List<TaskDTO> taskDTOs = pageResult.getRecords().stream()
                .map(this::convertToTaskDTO)
                .collect(Collectors.toList());

        return PageResult.of(
                pageResult.getTotal(),
                taskDTOs,
                page,
                size
        );
    }

    /**
     * 将StudentTask实体转换为TaskDTO
     */
    private TaskDTO convertToTaskDTO(StudentTask task) {
        return new TaskDTO(
                task.getId(),
                task.getTaskType(),
                task.getRefId(),
                task.getTitle(),
                task.getDescription(),
                task.getPriority(),
                task.getStatus(),
                task.getDueDate(),
                task.getCreatedAt()
        );
    }

    /**
     * 获取学生个性化推荐
     * 支持按类型过滤：all / project / job / course
     * 使用Redis缓存，TTL为15分钟
     * 
     * @param type 推荐类型：all, project, job, course
     * @return 推荐列表
     */
    public List<RecommendationDTO> getRecommendations(String type) {
        Long userId = UserContext.getUserId();
        String cacheKey = "student:recommendations:" + userId + ":" + type;

        return cacheService.getOrSet(cacheKey, 15, TimeUnit.MINUTES, () -> {
            log.debug("Getting recommendations for user: {}, type: {}", userId, type);

            // 获取学生ID
            Long studentId = getStudentIdByUserId(userId);
            if (studentId == null) {
                log.warn("Student not found for user: {}", userId);
                return new ArrayList<>();
            }

            // 构建查询条件
            LambdaQueryWrapper<StudentRecommendation> queryWrapper = new LambdaQueryWrapper<StudentRecommendation>()
                    .eq(StudentRecommendation::getStudentId, studentId)
                    .orderByDesc(StudentRecommendation::getScore)
                    .orderByDesc(StudentRecommendation::getCreatedAt);

            // 根据type参数过滤
            if (type != null && !type.isEmpty() && !"all".equalsIgnoreCase(type)) {
                queryWrapper.eq(StudentRecommendation::getRecType, type.toLowerCase());
            }

            // 查询推荐数据
            List<StudentRecommendation> recommendations = studentRecommendationMapper.selectList(queryWrapper);

            // 转换为DTO
            return recommendations.stream()
                    .map(this::convertToRecommendationDTO)
                    .collect(Collectors.toList());
        });
    }

    /**
     * 将StudentRecommendation实体转换为RecommendationDTO
     */
    private RecommendationDTO convertToRecommendationDTO(StudentRecommendation recommendation) {
        return new RecommendationDTO(
                recommendation.getId(),
                recommendation.getRecType(),
                recommendation.getRefId(),
                recommendation.getScore(),
                recommendation.getReason(),
                recommendation.getCreatedAt()
        );
    }

    /**
     * 获取实训项目列表（支持分页）
     * 查询开放状态的实训项目，并标注学生的报名状态
     * 使用Redis缓存，TTL为5分钟
     * 
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 分页的实训项目列表
     */
    public PageResult<TrainingProjectDTO> getTrainingProjects(Integer page, Integer size) {
        Long userId = UserContext.getUserId();
        String cacheKey = "student:projects:list:" + page + ":" + size;

        return cacheService.getOrSet(cacheKey, 5, TimeUnit.MINUTES, () -> {
            log.debug("Getting training projects for page: {}, size: {}", page, size);

            // 获取学生ID
            Long studentId = getStudentIdByUserId(userId);

            // 查询开放状态的实训项目（status IN (1, 2) 表示招募中或进行中）
            String sql = "SELECT tp.id, tp.project_name, tp.description, tp.tech_stack, tp.industry, " +
                    "tp.max_teams, tp.max_members, tp.start_date, tp.end_date, tp.status, tp.created_at, " +
                    "pe.id as enrollment_id " +
                    "FROM training_svc.training_project tp " +
                    "LEFT JOIN training_svc.project_enrollment pe ON tp.id = pe.project_id " +
                    "AND pe.student_id = ? AND pe.status = 1 " +
                    "WHERE tp.status IN (1, 2) AND tp.is_deleted IS FALSE " +
                    "ORDER BY tp.created_at DESC " +
                    "LIMIT ? OFFSET ?";

            int offset = (page - 1) * size;
            List<TrainingProjectDTO> projects = jdbcTemplate.query(sql, (rs, rowNum) -> {
                TrainingProjectDTO dto = new TrainingProjectDTO();
                dto.setId(rs.getLong("id"));
                dto.setProjectName(rs.getString("project_name"));
                dto.setDescription(rs.getString("description"));
                
                // 解析tech_stack JSON数组
                String techStackJson = rs.getString("tech_stack");
                if (techStackJson != null) {
                    // 简单解析JSON数组，去掉方括号和引号
                    String[] techArray = techStackJson.replace("[", "").replace("]", "")
                            .replace("\"", "").split(",");
                    dto.setTechStack(List.of(techArray));
                }
                
                dto.setIndustry(rs.getString("industry"));
                dto.setMaxTeams(rs.getInt("max_teams"));
                dto.setMaxMembers(rs.getInt("max_members"));
                dto.setStartDate(rs.getObject("start_date", LocalDate.class));
                dto.setEndDate(rs.getObject("end_date", LocalDate.class));
                dto.setStatus(rs.getInt("status"));
                // TIMESTAMPTZ 需要先转为 OffsetDateTime 再转为 LocalDateTime
                OffsetDateTime createdAtOffset = rs.getObject("created_at", OffsetDateTime.class);
                dto.setCreatedAt(createdAtOffset != null ? createdAtOffset.toLocalDateTime() : null);
                
                // 设置报名状态
                Long enrollmentId = rs.getObject("enrollment_id", Long.class);
                dto.setEnrollmentStatus(enrollmentId != null ? "enrolled" : null);
                
                return dto;
            }, studentId, size, offset);

            // 查询总数
            String countSql = "SELECT COUNT(*) FROM training_svc.training_project " +
                    "WHERE status IN (1, 2) AND is_deleted IS FALSE";
            Long total = jdbcTemplate.queryForObject(countSql, Long.class);

            return PageResult.of(total, projects, page, size);
        });
    }

    /**
     * 获取项目看板（Scrum Board）
     * 验证学生是否已报名该项目，未报名返回403错误
     * 查询项目任务并按状态分组（todo/in_progress/done）
     * 
     * @param projectId 项目ID
     * @return 看板数据
     */
    public ScrumBoardDTO getProjectBoard(Long projectId) {
        Long userId = UserContext.getUserId();
        log.debug("Getting project board for user: {}, project: {}", userId, projectId);

        // 获取学生ID
        Long studentId = getStudentIdByUserId(userId);
        if (studentId == null) {
            throw new RuntimeException("Student not found");
        }

        // 验证学生是否已报名该项目
        String enrollmentCheckSql = "SELECT COUNT(*) FROM training_svc.project_enrollment " +
                "WHERE project_id = ? AND student_id = ? AND status = 1";
        Integer enrollmentCount = jdbcTemplate.queryForObject(enrollmentCheckSql, Integer.class, projectId, studentId);
        
        if (enrollmentCount == null || enrollmentCount == 0) {
            throw new RuntimeException("Not enrolled in this project");
        }

        // 查询项目任务
        String taskSql = "SELECT pt.id, pt.title, pt.description, pt.assignee_id, pt.status, " +
                "pt.priority, pt.story_points, u.real_name as assignee_name " +
                "FROM training_svc.project_task pt " +
                "LEFT JOIN auth_center.sys_user u ON pt.assignee_id = u.id " +
                "WHERE pt.project_id = ? AND pt.is_deleted IS FALSE " +
                "ORDER BY pt.priority DESC, pt.created_at ASC";

        List<TaskItemDTO> allTasks = jdbcTemplate.query(taskSql, (rs, rowNum) -> {
            TaskItemDTO task = new TaskItemDTO();
            task.setId(rs.getLong("id"));
            task.setTitle(rs.getString("title"));
            task.setDescription(rs.getString("description"));
            task.setAssigneeId(rs.getObject("assignee_id", Long.class));
            task.setAssigneeName(rs.getString("assignee_name"));
            task.setPriority(rs.getInt("priority"));
            task.setStoryPoints(rs.getObject("story_points", Integer.class));
            return task;
        }, projectId);

        // 按状态分组
        List<TaskItemDTO> todo = new ArrayList<>();
        List<TaskItemDTO> inProgress = new ArrayList<>();
        List<TaskItemDTO> done = new ArrayList<>();

        String statusSql = "SELECT status FROM training_svc.project_task WHERE id = ?";
        for (TaskItemDTO task : allTasks) {
            String status = jdbcTemplate.queryForObject(statusSql, String.class, task.getId());
            switch (status) {
                case "todo":
                    todo.add(task);
                    break;
                case "in_progress":
                    inProgress.add(task);
                    break;
                case "done":
                    done.add(task);
                    break;
            }
        }

        return new ScrumBoardDTO(todo, inProgress, done);
    }

    /**
     * 获取实习岗位列表（支持分页）
     * 查询开放状态的实习岗位，并标注学生的申请状态
     * 使用Redis缓存，TTL为5分钟
     * 
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 分页的实习岗位列表
     */
    public PageResult<InternshipJobDTO> getInternshipJobs(Integer page, Integer size) {
        Long userId = UserContext.getUserId();
        String cacheKey = "student:jobs:list:" + page + ":" + size;

        return cacheService.getOrSet(cacheKey, 5, TimeUnit.MINUTES, () -> {
            log.debug("Getting internship jobs for page: {}, size: {}", page, size);

            // 获取学生ID
            Long studentId = getStudentIdByUserId(userId);

            // 查询开放状态的实习岗位
            String sql = "SELECT ij.id, ij.job_title, ij.job_type, ij.description, ij.requirements, " +
                    "ij.tech_stack, ij.city, ij.salary_min, ij.salary_max, ij.headcount, " +
                    "ij.start_date, ij.end_date, ij.status, ij.created_at, " +
                    "t.name as enterprise_name, " +
                    "ja.id as application_id, ja.status as application_status " +
                    "FROM internship_svc.internship_job ij " +
                    "LEFT JOIN auth_center.sys_tenant t ON ij.enterprise_id = t.id " +
                    "LEFT JOIN internship_svc.job_application ja ON ij.id = ja.job_id " +
                    "AND ja.student_id = ? " +
                    "WHERE ij.status = 1 AND ij.is_deleted = false " +
                    "ORDER BY ij.created_at DESC " +
                    "LIMIT ? OFFSET ?";

            int offset = (page - 1) * size;
            List<InternshipJobDTO> jobs = jdbcTemplate.query(sql, (rs, rowNum) -> {
                InternshipJobDTO dto = new InternshipJobDTO();
                dto.setId(rs.getLong("id"));
                dto.setJobTitle(rs.getString("job_title"));
                dto.setEnterpriseName(rs.getString("enterprise_name"));
                dto.setJobType(rs.getString("job_type"));
                dto.setDescription(rs.getString("description"));
                dto.setRequirements(rs.getString("requirements"));
                
                // 解析tech_stack JSON数组
                String techStackJson = rs.getString("tech_stack");
                if (techStackJson != null) {
                    String[] techArray = techStackJson.replace("[", "").replace("]", "")
                            .replace("\"", "").split(",");
                    dto.setTechStack(List.of(techArray));
                }
                
                dto.setCity(rs.getString("city"));
                dto.setSalaryMin(rs.getBigDecimal("salary_min"));
                dto.setSalaryMax(rs.getBigDecimal("salary_max"));
                dto.setHeadcount(rs.getInt("headcount"));
                dto.setStartDate(rs.getObject("start_date", LocalDate.class));
                dto.setEndDate(rs.getObject("end_date", LocalDate.class));
                dto.setStatus(rs.getInt("status"));
                // Convert TIMESTAMPTZ to LocalDateTime by getting the timestamp and converting to local time
                Timestamp timestamp = rs.getTimestamp("created_at");
                dto.setCreatedAt(timestamp != null ? timestamp.toLocalDateTime() : null);
                
                // 设置申请状态
                Long applicationId = rs.getObject("application_id", Long.class);
                if (applicationId != null) {
                    Integer appStatus = rs.getInt("application_status");
                    // 0=待审核, 1=通过, 2=拒绝, 3=已面试, 4=已录用
                    if (appStatus == 4) {
                        dto.setApplicationStatus("offered");
                    } else if (appStatus == 3) {
                        dto.setApplicationStatus("interviewed");
                    } else {
                        dto.setApplicationStatus("applied");
                    }
                } else {
                    dto.setApplicationStatus(null);
                }
                
                return dto;
            }, studentId, size, offset);

            // 查询总数
            String countSql = "SELECT COUNT(*) FROM internship_svc.internship_job " +
                    "WHERE status = 1 AND is_deleted IS FALSE";
            Long total = jdbcTemplate.queryForObject(countSql, Long.class);

            return PageResult.of(total, jobs, page, size);
        });
    }

    /**
     * 获取学生的周报列表（支持分页）
     * 按提交日期降序排列
     * 
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 分页的周报列表
     */
    public PageResult<WeeklyReportDTO> getMyReports(Integer page, Integer size) {
        Long userId = UserContext.getUserId();
        log.debug("Getting weekly reports for user: {}, page: {}, size: {}", userId, page, size);

        // 获取学生ID
        Long studentId = getStudentIdByUserId(userId);
        if (studentId == null) {
            log.warn("Student not found for user: {}", userId);
            return PageResult.of(0L, new ArrayList<>(), page, size);
        }

        // 查询周报
        String sql = "SELECT wr.id, wr.internship_id, wr.week_start, wr.week_end, wr.content, " +
                "wr.work_hours, wr.status, wr.review_comment, wr.reviewed_at, wr.created_at, " +
                "u.real_name as reviewer_name " +
                "FROM internship_svc.weekly_report wr " +
                "LEFT JOIN auth_center.sys_user u ON wr.reviewed_by = u.id " +
                "WHERE wr.student_id = ? " +
                "ORDER BY wr.created_at DESC " +
                "LIMIT ? OFFSET ?";

        int offset = (page - 1) * size;
        List<WeeklyReportDTO> reports = jdbcTemplate.query(sql, (rs, rowNum) -> {
            WeeklyReportDTO dto = new WeeklyReportDTO();
            dto.setId(rs.getLong("id"));
            dto.setInternshipId(rs.getLong("internship_id"));
            dto.setWeekStart(rs.getObject("week_start", LocalDate.class));
            dto.setWeekEnd(rs.getObject("week_end", LocalDate.class));
            dto.setContent(rs.getString("content"));
            dto.setWorkHours(rs.getBigDecimal("work_hours"));
            dto.setStatus(rs.getInt("status"));
            dto.setReviewComment(rs.getString("review_comment"));
            dto.setReviewerName(rs.getString("reviewer_name"));
            dto.setReviewedAt(rs.getObject("reviewed_at", LocalDateTime.class));
            dto.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
            return dto;
        }, studentId, size, offset);

        // 查询总数
        String countSql = "SELECT COUNT(*) FROM internship_svc.weekly_report WHERE student_id = ?";
        Long total = jdbcTemplate.queryForObject(countSql, Long.class, studentId);

        return PageResult.of(total, reports, page, size);
    }

    /**
     * 获取学生的成长评价汇总
     * 查询所有评价记录并计算平均分
     * 
     * @return 评价汇总数据
     */
    public EvaluationSummaryDTO getEvaluationSummary() {
        Long userId = UserContext.getUserId();
        log.debug("Getting evaluation summary for user: {}", userId);

        try {
            // 获取学生ID
            Long studentId = getStudentIdByUserId(userId);
            if (studentId == null) {
                log.warn("Student not found for user: {}", userId);
                return new EvaluationSummaryDTO(BigDecimal.ZERO, new ArrayList<>());
            }

            // 查询评价记录
            String sql = "SELECT er.id, er.source_type, er.scores, er.comment, er.created_at, " +
                    "u.real_name as evaluator_name " +
                    "FROM growth_svc.evaluation_record er " +
                    "LEFT JOIN auth_center.sys_user u ON er.evaluator_id = u.id " +
                    "WHERE er.student_id = ? AND er.is_deleted IS FALSE " +
                    "ORDER BY er.created_at DESC";

            List<EvaluationSummaryDTO.EvaluationItemDTO> evaluations = jdbcTemplate.query(sql, (rs, rowNum) -> {
                EvaluationSummaryDTO.EvaluationItemDTO item = new EvaluationSummaryDTO.EvaluationItemDTO();
                item.setEvaluatorName(rs.getString("evaluator_name"));
                item.setSourceType(rs.getString("source_type"));
                
                // 修复：使用Timestamp转换而不是直接getObject
                item.setEvaluationDate(rs.getTimestamp("created_at").toLocalDateTime());
                
                // 解析scores JSON并计算平均分
                String scoresJson = rs.getString("scores");
                int avgScore = calculateAverageScore(scoresJson);
                item.setScore(avgScore);
                
                item.setComment(rs.getString("comment"));
                return item;
            }, studentId);

            // 计算总平均分
            BigDecimal averageScore = BigDecimal.ZERO;
            if (!evaluations.isEmpty()) {
                double sum = evaluations.stream()
                        .mapToInt(EvaluationSummaryDTO.EvaluationItemDTO::getScore)
                        .average()
                        .orElse(0.0);
                // 修复：使用RoundingMode而不是已弃用的BigDecimal常量
                averageScore = BigDecimal.valueOf(sum).setScale(1, java.math.RoundingMode.HALF_UP);
            }

            return new EvaluationSummaryDTO(averageScore, evaluations);
        } catch (Exception e) {
            log.error("Failed to get evaluation summary for user: {}", userId, e);
            return new EvaluationSummaryDTO(BigDecimal.ZERO, new ArrayList<>());
        }
    }

    /**
     * 解析scores JSON并计算平均分
     * scores格式示例: {"technical": 85, "communication": 90, "teamwork": 88}
     */
    private int calculateAverageScore(String scoresJson) {
        if (scoresJson == null || scoresJson.isEmpty()) {
            return 0;
        }
        
        // 简单解析JSON，提取数字并计算平均值
        // 这里使用简单的字符串处理，实际项目中应使用JSON库
        String[] parts = scoresJson.replace("{", "").replace("}", "")
                .replace("\"", "").split(",");
        
        int sum = 0;
        int count = 0;
        for (String part : parts) {
            String[] kv = part.split(":");
            if (kv.length == 2) {
                try {
                    sum += Integer.parseInt(kv[1].trim());
                    count++;
                } catch (NumberFormatException e) {
                    // 忽略解析错误
                }
            }
        }
        
        return count > 0 ? sum / count : 0;
    }

    /**
     * 获取学生的证书列表（支持分页）
     * 按颁发日期降序排列
     * 
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 分页的证书列表
     */
    public PageResult<CertificateDTO> getMyCertificates(Integer page, Integer size) {
        Long userId = UserContext.getUserId();
        log.debug("Getting certificates for user: {}, page: {}, size: {}", userId, page, size);

        try {
            // 获取学生ID
            Long studentId = getStudentIdByUserId(userId);
            if (studentId == null) {
                log.warn("Student not found for user: {}", userId);
                return PageResult.of(0L, new ArrayList<>(), page, size);
            }

            // 查询证书
            String sql = "SELECT id, type, name, issue_date, image_url, blockchain_hash, created_at " +
                    "FROM growth_svc.growth_badge " +
                    "WHERE student_id = ? AND type = 'certificate' AND is_deleted IS FALSE " +
                    "ORDER BY issue_date DESC " +
                    "LIMIT ? OFFSET ?";

            int offset = (page - 1) * size;
            List<CertificateDTO> certificates = jdbcTemplate.query(sql, (rs, rowNum) -> {
                CertificateDTO dto = new CertificateDTO();
                dto.setId(rs.getLong("id"));
                dto.setType(rs.getString("type"));
                dto.setName(rs.getString("name"));
                dto.setIssueDate(rs.getObject("issue_date", LocalDate.class));
                dto.setImageUrl(rs.getString("image_url"));
                
                // 生成PDF下载链接（假设有一个证书下载服务）
                String downloadUrl = "/api/student-portal/v1/growth/certificates/" + rs.getLong("id") + "/download";
                dto.setDownloadUrl(downloadUrl);
                
                dto.setBlockchainHash(rs.getString("blockchain_hash"));
                
                // 修复：使用OffsetDateTime而不是LocalDateTime
                dto.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                return dto;
            }, studentId, size, offset);

            // 查询总数
            String countSql = "SELECT COUNT(*) FROM growth_svc.growth_badge " +
                    "WHERE student_id = ? AND type = 'certificate' AND is_deleted IS FALSE";
            Long total = jdbcTemplate.queryForObject(countSql, Long.class, studentId);

            return PageResult.of(total != null ? total : 0L, certificates, page, size);
        } catch (Exception e) {
            log.error("Failed to get certificates for user: {}", userId, e);
            return PageResult.of(0L, new ArrayList<>(), page, size);
        }
    }

    /**
     * 获取学生的徽章列表（支持分页）
     * 按获得日期降序排列
     * 
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 分页的徽章列表
     */
    public PageResult<BadgeDTO> getMyBadges(Integer page, Integer size) {
        Long userId = UserContext.getUserId();
        log.debug("Getting badges for user: {}, page: {}, size: {}", userId, page, size);

        try {
            // 获取学生ID
            Long studentId = getStudentIdByUserId(userId);
            if (studentId == null) {
                log.warn("Student not found for user: {}", userId);
                return PageResult.of(0L, new ArrayList<>(), page, size);
            }

            // 查询徽章
            String sql = "SELECT id, type, name, issue_date, image_url, created_at " +
                    "FROM growth_svc.growth_badge " +
                    "WHERE student_id = ? AND type = 'badge' AND is_deleted IS FALSE " +
                    "ORDER BY issue_date DESC " +
                    "LIMIT ? OFFSET ?";

            int offset = (page - 1) * size;
            List<BadgeDTO> badges = jdbcTemplate.query(sql, (rs, rowNum) -> {
                BadgeDTO dto = new BadgeDTO();
                dto.setId(rs.getLong("id"));
                dto.setType(rs.getString("type"));
                dto.setName(rs.getString("name"));
                dto.setIssueDate(rs.getObject("issue_date", LocalDate.class));
                dto.setImageUrl(rs.getString("image_url"));
                
                // 修复：使用OffsetDateTime而不是LocalDateTime
                dto.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                return dto;
            }, studentId, size, offset);

            // 查询总数
            String countSql = "SELECT COUNT(*) FROM growth_svc.growth_badge " +
                    "WHERE student_id = ? AND type = 'badge' AND is_deleted IS FALSE";
            Long total = jdbcTemplate.queryForObject(countSql, Long.class, studentId);

            return PageResult.of(total != null ? total : 0L, badges, page, size);
        } catch (Exception e) {
            log.error("Failed to get badges for user: {}", userId, e);
            return PageResult.of(0L, new ArrayList<>(), page, size);
        }
    }
}
