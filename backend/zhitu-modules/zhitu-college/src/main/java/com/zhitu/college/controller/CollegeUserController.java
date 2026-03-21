package com.zhitu.college.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhitu.college.entity.StudentInfo;
import com.zhitu.college.service.CollegeService;
import com.zhitu.common.core.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * College User Management Controller
 * Handles student management endpoints
 */
@RestController
@RequestMapping("/api/user/v1/college")
@RequiredArgsConstructor
public class CollegeUserController {

    private final CollegeService collegeService;

    /**
     * Get student list with filtering and pagination
     * Requirements: 22.1-22.6
     */
    @GetMapping("/students")
    public Result<IPage<StudentInfo>> getStudents(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "classId", required = false) Long classId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        
        IPage<StudentInfo> students = collegeService.getStudentList(keyword, classId, page, size);
        return Result.ok(students);
    }
}
