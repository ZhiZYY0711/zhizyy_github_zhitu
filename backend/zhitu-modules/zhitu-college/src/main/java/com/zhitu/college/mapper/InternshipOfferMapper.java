package com.zhitu.college.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhitu.college.dto.ContractDTO;
import com.zhitu.college.entity.InternshipOffer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface InternshipOfferMapper extends BaseMapper<InternshipOffer> {

    /**
     * Get pending contracts with student and enterprise information
     */
    @Select("SELECT " +
            "io.id AS id, " +
            "COALESCE(si.real_name, 'Unknown') AS studentName, " +
            "COALESCE(ei.enterprise_name, 'Unknown') AS companyName, " +
            "COALESCE(ij.job_title, 'Unknown') AS position, " +
            "TO_CHAR(io.created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') AS submitTime, " +
            "CASE " +
            "  WHEN io.college_audit = 0 THEN 'pending' " +
            "  WHEN io.college_audit = 1 THEN 'approved' " +
            "  WHEN io.college_audit = 2 THEN 'rejected' " +
            "  ELSE 'pending' " +
            "END AS status " +
            "FROM internship_svc.internship_offer io " +
            "LEFT JOIN student_svc.student_info si ON io.student_id = si.id " +
            "LEFT JOIN enterprise_svc.enterprise_info ei ON io.enterprise_id = ei.tenant_id " +
            "LEFT JOIN internship_svc.internship_job ij ON io.job_id = ij.id " +
            "WHERE io.college_audit = 0 AND io.status = 1 " +
            "ORDER BY io.created_at ASC")
    IPage<ContractDTO> selectPendingContracts(Page<?> page);
}

