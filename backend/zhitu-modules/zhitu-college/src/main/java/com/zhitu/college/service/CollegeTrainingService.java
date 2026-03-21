package com.zhitu.college.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhitu.college.entity.TrainingPlan;
import com.zhitu.college.mapper.TrainingPlanMapper;
import com.zhitu.common.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * College Training Service
 * Handles training plan management
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CollegeTrainingService {

    private final TrainingPlanMapper trainingPlanMapper;

    /**
     * Get training plans with filtering and pagination
     * Requirements: 23.1-23.6
     */
    public IPage<TrainingPlan> getPlans(String semester, int page, int size) {
        Page<TrainingPlan> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<TrainingPlan> wrapper = new LambdaQueryWrapper<>();
        
        if (semester != null && !semester.isEmpty()) {
            wrapper.like(TrainingPlan::getPlanName, semester);
        }
        
        wrapper.orderByDesc(TrainingPlan::getCreatedAt);
        
        return trainingPlanMapper.selectPage(pageParam, wrapper);
    }

    /**
     * Create a new training plan
     * Requirements: 23.1-23.6
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createPlan(TrainingPlan plan) {
        // Validate required fields
        if (plan.getPlanName() == null || plan.getPlanName().isEmpty()) {
            throw new BusinessException("计划名称不能为空");
        }
        if (plan.getStartDate() == null) {
            throw new BusinessException("开始日期不能为空");
        }
        if (plan.getEndDate() == null) {
            throw new BusinessException("结束日期不能为空");
        }
        if (plan.getProjectId() == null) {
            throw new BusinessException("项目ID不能为空");
        }
        
        // Validate date range
        if (plan.getEndDate().isBefore(plan.getStartDate())) {
            throw new BusinessException("结束日期不能早于开始日期");
        }
        
        // Set default status if not provided
        if (plan.getStatus() == null) {
            plan.setStatus(0); // 0 = pending
        }
        
        trainingPlanMapper.insert(plan);
        return plan.getId();
    }

    /**
     * Assign mentor to training plan
     * Requirements: 29.1-29.6
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignMentor(Long planId, Long teacherId) {
        if (planId == null) {
            throw new BusinessException("计划ID不能为空");
        }
        if (teacherId == null) {
            throw new BusinessException("教师ID不能为空");
        }
        
        TrainingPlan plan = trainingPlanMapper.selectById(planId);
        if (plan == null) {
            throw new BusinessException("培训计划不存在");
        }
        
        plan.setTeacherId(teacherId);
        trainingPlanMapper.updateById(plan);
    }
}
