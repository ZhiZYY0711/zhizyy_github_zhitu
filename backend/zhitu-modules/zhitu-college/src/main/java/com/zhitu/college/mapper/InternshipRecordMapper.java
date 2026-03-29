package com.zhitu.college.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhitu.college.dto.InternshipStudentDTO;
import com.zhitu.college.entity.InternshipRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InternshipRecordMapper extends BaseMapper<InternshipRecord> {

    /**
     * 分页查询实习学生列表（包含关联名称和最近周报时间）
     */
    IPage<InternshipStudentDTO> selectEnrichedInternshipStudents(Page<?> page, String status);
}
