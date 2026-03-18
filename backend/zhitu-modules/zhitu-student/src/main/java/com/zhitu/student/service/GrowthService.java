package com.zhitu.student.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhitu.common.core.context.UserContext;
import com.zhitu.common.core.exception.BusinessException;
import com.zhitu.common.core.result.ResultCode;
import com.zhitu.student.dto.EvaluationRequest;
import com.zhitu.student.entity.EvaluationRecord;
import com.zhitu.student.entity.GrowthBadge;
import com.zhitu.student.mapper.EvaluationRecordMapper;
import com.zhitu.student.mapper.GrowthBadgeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GrowthService {

    private final EvaluationRecordMapper evaluationRecordMapper;
    private final GrowthBadgeMapper growthBadgeMapper;

    /**
     * 学生查看自己的成绩单（评价列表）
     */
    public List<EvaluationRecord> getMyEvaluations() {
        Long studentId = UserContext.getUserId();
        return evaluationRecordMapper.selectList(
                new LambdaQueryWrapper<EvaluationRecord>()
                        .eq(EvaluationRecord::getStudentId, studentId)
                        .orderByDesc(EvaluationRecord::getCreatedAt));
    }

    /**
     * 高校/企业查看指定学生成绩单
     */
    public List<EvaluationRecord> getStudentEvaluations(Long studentId) {
        return evaluationRecordMapper.selectList(
                new LambdaQueryWrapper<EvaluationRecord>()
                        .eq(EvaluationRecord::getStudentId, studentId)
                        .orderByDesc(EvaluationRecord::getCreatedAt));
    }

    /**
     * 提交评价（企业端/高校端/同学互评）
     */
    @Transactional
    public void submitEvaluation(EvaluationRequest req, String sourceType) {
        Long evaluatorId = UserContext.getUserId();
        EvaluationRecord record = new EvaluationRecord();
        record.setStudentId(req.getStudentId());
        record.setEvaluatorId(evaluatorId);
        record.setSourceType(sourceType);
        record.setRefType(req.getRefType());
        record.setRefId(req.getRefId());
        record.setScores(req.getScores());
        record.setComment(req.getComment());
        record.setHireRecommendation(req.getHireRecommendation());
        evaluationRecordMapper.insert(record);
    }

    /**
     * 学生查看自己的证书与徽章
     */
    public List<GrowthBadge> getMyBadges() {
        Long studentId = UserContext.getUserId();
        return growthBadgeMapper.selectList(
                new LambdaQueryWrapper<GrowthBadge>()
                        .eq(GrowthBadge::getStudentId, studentId)
                        .orderByDesc(GrowthBadge::getCreatedAt));
    }

    /**
     * 颁发证书/徽章（系统或管理员触发）
     */
    @Transactional
    public void issueBadge(GrowthBadge badge) {
        growthBadgeMapper.insert(badge);
    }
}
