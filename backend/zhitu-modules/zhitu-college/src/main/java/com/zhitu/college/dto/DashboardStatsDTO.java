package com.zhitu.college.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DashboardStatsDTO {

    @JsonProperty("total_graduates")
    private long totalGraduates;

    @JsonProperty("employment_rate")
    private double employmentRate;

    @JsonProperty("internship_rate")
    private double internshipRate;

    @JsonProperty("flexible_employment_rate")
    private double flexibleEmploymentRate;

    @JsonProperty("avg_salary")
    private int avgSalary;

    @JsonProperty("top_industries")
    private List<IndustryItem> topIndustries;

    @Data
    @Builder
    public static class IndustryItem {
        private String name;
        private double ratio;
    }
}
