package com.zhitu.enterprise.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 项目团队DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectTeamDTO {

    private String teamId;
    private String projectId;
    private String teamName;
    private List<TeamMemberDTO> members;
    private String mentorId;
    private String mentorName;
    private Integer progress;
    private String status;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeamMemberDTO {
        private String studentId;
        private String studentName;
        private String school;
        private String role;
    }
}
