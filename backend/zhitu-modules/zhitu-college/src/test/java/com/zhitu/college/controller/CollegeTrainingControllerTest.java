package com.zhitu.college.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhitu.college.entity.TrainingPlan;
import com.zhitu.college.service.CollegeTrainingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CollegeTrainingController.class)
@AutoConfigureMockMvc(addFilters = false)
class CollegeTrainingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CollegeTrainingService collegeTrainingService;

    @Test
    void testGetPlans() throws Exception {
        TrainingPlan plan1 = new TrainingPlan();
        plan1.setId(1L);
        plan1.setPlanName("2024春季实训");

        TrainingPlan plan2 = new TrainingPlan();
        plan2.setId(2L);
        plan2.setPlanName("2024秋季实训");

        IPage<TrainingPlan> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(plan1, plan2));
        page.setTotal(2);

        when(collegeTrainingService.getPlans(null, 1, 10)).thenReturn(page);

        mockMvc.perform(get("/api/training/v1/college/plans")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records[0].planName").value("2024春季实训"))
                .andExpect(jsonPath("$.data.total").value(2));

        verify(collegeTrainingService).getPlans(null, 1, 10);
    }

    @Test
    void testGetPlans_WithSemester() throws Exception {
        TrainingPlan plan1 = new TrainingPlan();
        plan1.setId(1L);
        plan1.setPlanName("2024春季实训");

        IPage<TrainingPlan> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(plan1));
        page.setTotal(1);

        when(collegeTrainingService.getPlans("春季", 1, 10)).thenReturn(page);

        mockMvc.perform(get("/api/training/v1/college/plans")
                .param("semester", "春季")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].planName").value("2024春季实训"));

        verify(collegeTrainingService).getPlans("春季", 1, 10);
    }

    @Test
    void testCreatePlan() throws Exception {
        when(collegeTrainingService.createPlan(any(TrainingPlan.class))).thenReturn(1L);

        String requestBody = """
                {
                    "planName": "2024春季实训",
                    "startDate": "2024-03-01",
                    "endDate": "2024-06-30",
                    "projectId": 100,
                    "status": 0
                }
                """;

        mockMvc.perform(post("/api/training/v1/college/plans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(1));

        verify(collegeTrainingService).createPlan(any(TrainingPlan.class));
    }

    @Test
    void testAssignMentor() throws Exception {
        mockMvc.perform(post("/api/training/v1/college/mentors/assign")
                .param("planId", "1")
                .param("teacherId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(collegeTrainingService).assignMentor(1L, 100L);
    }

    @Test
    void testGetPlans_DefaultPagination() throws Exception {
        IPage<TrainingPlan> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList());
        page.setTotal(0);

        when(collegeTrainingService.getPlans(null, 1, 10)).thenReturn(page);

        mockMvc.perform(get("/api/training/v1/college/plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(collegeTrainingService).getPlans(null, 1, 10);
    }
}
