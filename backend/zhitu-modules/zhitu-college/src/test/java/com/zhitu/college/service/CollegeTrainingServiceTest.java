package com.zhitu.college.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhitu.college.entity.TrainingPlan;
import com.zhitu.college.mapper.TrainingPlanMapper;
import com.zhitu.common.core.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollegeTrainingServiceTest {

    @Mock
    private TrainingPlanMapper trainingPlanMapper;

    @InjectMocks
    private CollegeTrainingService collegeTrainingService;

    @Test
    void testGetPlans() {
        TrainingPlan plan1 = new TrainingPlan();
        plan1.setId(1L);
        plan1.setPlanName("2024春季实训");

        TrainingPlan plan2 = new TrainingPlan();
        plan2.setId(2L);
        plan2.setPlanName("2024秋季实训");

        Page<TrainingPlan> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(plan1, plan2));
        page.setTotal(2);

        when(trainingPlanMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        IPage<TrainingPlan> result = collegeTrainingService.getPlans(null, 1, 10);

        assertNotNull(result);
        assertEquals(2, result.getRecords().size());
        assertEquals(2, result.getTotal());
        verify(trainingPlanMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void testGetPlans_WithSemester() {
        TrainingPlan plan1 = new TrainingPlan();
        plan1.setId(1L);
        plan1.setPlanName("2024春季实训");

        Page<TrainingPlan> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(plan1));
        page.setTotal(1);

        when(trainingPlanMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        IPage<TrainingPlan> result = collegeTrainingService.getPlans("春季", 1, 10);

        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
        verify(trainingPlanMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void testCreatePlan_Success() {
        TrainingPlan plan = new TrainingPlan();
        plan.setPlanName("2024春季实训");
        plan.setStartDate(LocalDate.of(2024, 3, 1));
        plan.setEndDate(LocalDate.of(2024, 6, 30));
        plan.setProjectId(100L);

        when(trainingPlanMapper.insert(any(TrainingPlan.class))).thenAnswer(invocation -> {
            TrainingPlan p = invocation.getArgument(0);
            p.setId(1L);
            return 1;
        });

        Long planId = collegeTrainingService.createPlan(plan);

        assertNotNull(planId);
        assertEquals(1L, planId);
        assertEquals(0, plan.getStatus()); // Default status
        verify(trainingPlanMapper).insert(any(TrainingPlan.class));
    }

    @Test
    void testCreatePlan_MissingPlanName() {
        TrainingPlan plan = new TrainingPlan();
        plan.setStartDate(LocalDate.of(2024, 3, 1));
        plan.setEndDate(LocalDate.of(2024, 6, 30));
        plan.setProjectId(100L);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            collegeTrainingService.createPlan(plan);
        });

        assertEquals("计划名称不能为空", exception.getMessage());
        verify(trainingPlanMapper, never()).insert(any(TrainingPlan.class));
    }

    @Test
    void testCreatePlan_MissingStartDate() {
        TrainingPlan plan = new TrainingPlan();
        plan.setPlanName("2024春季实训");
        plan.setEndDate(LocalDate.of(2024, 6, 30));
        plan.setProjectId(100L);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            collegeTrainingService.createPlan(plan);
        });

        assertEquals("开始日期不能为空", exception.getMessage());
        verify(trainingPlanMapper, never()).insert(any(TrainingPlan.class));
    }

    @Test
    void testCreatePlan_InvalidDateRange() {
        TrainingPlan plan = new TrainingPlan();
        plan.setPlanName("2024春季实训");
        plan.setStartDate(LocalDate.of(2024, 6, 30));
        plan.setEndDate(LocalDate.of(2024, 3, 1));
        plan.setProjectId(100L);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            collegeTrainingService.createPlan(plan);
        });

        assertEquals("结束日期不能早于开始日期", exception.getMessage());
        verify(trainingPlanMapper, never()).insert(any(TrainingPlan.class));
    }

    @Test
    void testAssignMentor_Success() {
        TrainingPlan plan = new TrainingPlan();
        plan.setId(1L);
        plan.setPlanName("2024春季实训");

        when(trainingPlanMapper.selectById(1L)).thenReturn(plan);
        when(trainingPlanMapper.updateById(any(TrainingPlan.class))).thenReturn(1);

        collegeTrainingService.assignMentor(1L, 100L);

        assertEquals(100L, plan.getTeacherId());
        verify(trainingPlanMapper).selectById(1L);
        verify(trainingPlanMapper).updateById(any(TrainingPlan.class));
    }

    @Test
    void testAssignMentor_PlanNotFound() {
        when(trainingPlanMapper.selectById(1L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            collegeTrainingService.assignMentor(1L, 100L);
        });

        assertEquals("培训计划不存在", exception.getMessage());
        verify(trainingPlanMapper).selectById(1L);
        verify(trainingPlanMapper, never()).updateById(any(TrainingPlan.class));
    }

    @Test
    void testAssignMentor_MissingPlanId() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            collegeTrainingService.assignMentor(null, 100L);
        });

        assertEquals("计划ID不能为空", exception.getMessage());
        verify(trainingPlanMapper, never()).selectById(any());
    }

    @Test
    void testAssignMentor_MissingTeacherId() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            collegeTrainingService.assignMentor(1L, null);
        });

        assertEquals("教师ID不能为空", exception.getMessage());
        verify(trainingPlanMapper, never()).selectById(any());
    }
}
