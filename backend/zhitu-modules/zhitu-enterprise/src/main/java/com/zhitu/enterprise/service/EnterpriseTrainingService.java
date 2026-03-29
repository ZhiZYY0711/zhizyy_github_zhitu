package com.zhitu.enterprise.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhitu.common.core.context.UserContext;
import com.zhitu.common.core.result.PageResult;
import com.zhitu.enterprise.dto.CreateTrainingProjectRequest;
import com.zhitu.enterprise.dto.ProjectTeamDTO;
import com.zhitu.enterprise.dto.TrainingProjectDTO;
import com.zhitu.enterprise.entity.TrainingProject;
import com.zhitu.enterprise.mapper.TrainingProjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 企业实训项目服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EnterpriseTrainingService {

    private final TrainingProjectMapper trainingProjectMapper;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 获取企业的实训项目列表
     */
    public List<TrainingProjectDTO> getProjects() {
        Long userId = UserContext.getUserId();
        Long tenantId = getTenantIdByUserId(userId);
        
        if (tenantId == null) {
            log.warn("Tenant not found for user: {}", userId);
            return Collections.emptyList();
        }

        log.debug("Getting training projects for tenant: {}", tenantId);

        LambdaQueryWrapper<TrainingProject> queryWrapper = new LambdaQueryWrapper<TrainingProject>()
                .eq(TrainingProject::getEnterpriseId, tenantId)
                .eq(TrainingProject::getDeleted, false)
                .orderByDesc(TrainingProject::getCreatedAt);

        List<TrainingProject> projects = trainingProjectMapper.selectList(queryWrapper);

        return projects.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 创建实训项目
     */
    public void createProject(CreateTrainingProjectRequest request) {
        Long userId = UserContext.getUserId();
        Long tenantId = getTenantIdByUserId(userId);
        
        if (tenantId == null) {
            throw new RuntimeException("无法获取企业信息");
        }

        TrainingProject project = new TrainingProject();
        project.setEnterpriseId(tenantId);
        project.setProjectName(request.getName());
        project.setDescription(request.getDescription());
        project.setMaxTeams(request.getMaxTeams());
        project.setAuditStatus(0);
        project.setStatus(1);

        try {
            project.setTechStack(objectMapper.writeValueAsString(request.getTechStack()));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize tech stack", e);
            project.setTechStack("[]");
        }

        trainingProjectMapper.insert(project);
        log.info("Created training project: {} for tenant: {}", project.getId(), tenantId);
    }

    /**
     * 获取项目团队列表
     */
    public List<ProjectTeamDTO> getProjectTeams(Long projectId) {
        Long userId = UserContext.getUserId();
        Long tenantId = getTenantIdByUserId(userId);
        
        if (tenantId == null) {
            return Collections.emptyList();
        }

        String sql = "SELECT pe.team_id, pe.project_id, " +
                "COUNT(DISTINCT pe.student_id) as member_count, " +
                "MAX(CASE WHEN pe.role = 'leader' THEN si.real_name END) as leader_name " +
                "FROM training_svc.project_enrollment pe " +
                "INNER JOIN student_svc.student_info si ON pe.student_id = si.id " +
                "WHERE pe.project_id = ? AND pe.status = 1 " +
                "GROUP BY pe.team_id, pe.project_id";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            ProjectTeamDTO dto = new ProjectTeamDTO();
            dto.setTeamId(String.valueOf(rs.getLong("team_id")));
            dto.setProjectId(String.valueOf(rs.getLong("project_id")));
            dto.setTeamName("第" + (rowNum + 1) + "组");
            dto.setProgress(50);
            dto.setStatus("active");
            return dto;
        }, projectId);
    }

    /**
     * 转换为DTO
     */
    private TrainingProjectDTO convertToDTO(TrainingProject project) {
        List<String> techStack = Collections.emptyList();
        try {
            if (project.getTechStack() != null) {
                techStack = objectMapper.readValue(project.getTechStack(), new TypeReference<List<String>>() {});
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to parse tech stack", e);
        }

        int currentTeams = countProjectTeams(project.getId());

        return new TrainingProjectDTO(
                project.getId(),
                project.getProjectName(),
                project.getDescription(),
                3,
                techStack,
                project.getMaxTeams(),
                currentTeams,
                convertStatus(project.getStatus()),
                project.getStartDate(),
                project.getEndDate(),
                project.getCreatedAt()
        );
    }

    /**
     * 统计项目团队数
     */
    private int countProjectTeams(Long projectId) {
        String sql = "SELECT COUNT(DISTINCT team_id) FROM training_svc.project_enrollment " +
                "WHERE project_id = ? AND status = 1";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, projectId);
        return count != null ? count : 0;
    }

    /**
     * 转换状态
     */
    private String convertStatus(Integer status) {
        if (status == null) return "draft";
        return switch (status) {
            case 1 -> "recruiting";
            case 2 -> "in_progress";
            case 3 -> "completed";
            default -> "draft";
        };
    }

    /**
     * 根据用户ID获取租户ID
     */
    private Long getTenantIdByUserId(Long userId) {
        String sql = "SELECT tenant_id FROM auth_center.sys_user WHERE id = ?";
        List<Long> results = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("tenant_id"), userId);
        return results.isEmpty() ? null : results.get(0);
    }
}
