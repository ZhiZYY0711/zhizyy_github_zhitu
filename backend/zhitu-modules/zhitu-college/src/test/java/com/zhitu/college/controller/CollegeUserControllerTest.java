package com.zhitu.college.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhitu.college.entity.StudentInfo;
import com.zhitu.college.service.CollegeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CollegeUserController.class)
@AutoConfigureMockMvc(addFilters = false)
class CollegeUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CollegeService collegeService;

    @Test
    void testGetStudents() throws Exception {
        StudentInfo student1 = new StudentInfo();
        student1.setId(1L);
        student1.setRealName("张三");
        student1.setStudentNo("2024001");

        StudentInfo student2 = new StudentInfo();
        student2.setId(2L);
        student2.setRealName("李四");
        student2.setStudentNo("2024002");

        IPage<StudentInfo> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(student1, student2));
        page.setTotal(2);

        when(collegeService.getStudentList(null, null, 1, 10)).thenReturn(page);

        mockMvc.perform(get("/api/college/v1/students")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records[0].realName").value("张三"))
                .andExpect(jsonPath("$.data.total").value(2));

        verify(collegeService).getStudentList(null, null, 1, 10);
    }

    @Test
    void testGetStudents_WithKeyword() throws Exception {
        StudentInfo student1 = new StudentInfo();
        student1.setId(1L);
        student1.setRealName("张三");
        student1.setStudentNo("2024001");

        IPage<StudentInfo> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(student1));
        page.setTotal(1);

        when(collegeService.getStudentList("张三", null, 1, 10)).thenReturn(page);

        mockMvc.perform(get("/api/college/v1/students")
                .param("keyword", "张三")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].realName").value("张三"));

        verify(collegeService).getStudentList("张三", null, 1, 10);
    }

    @Test
    void testGetStudents_WithClassId() throws Exception {
        StudentInfo student1 = new StudentInfo();
        student1.setId(1L);
        student1.setRealName("张三");
        student1.setClassId(100L);

        IPage<StudentInfo> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(student1));
        page.setTotal(1);

        when(collegeService.getStudentList(null, 100L, 1, 10)).thenReturn(page);

        mockMvc.perform(get("/api/college/v1/students")
                .param("classId", "100")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].classId").value(100));

        verify(collegeService).getStudentList(null, 100L, 1, 10);
    }

    @Test
    void testGetStudents_WithKeywordAndClassId() throws Exception {
        IPage<StudentInfo> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList());
        page.setTotal(0);

        when(collegeService.getStudentList("张三", 100L, 1, 10)).thenReturn(page);

        mockMvc.perform(get("/api/college/v1/students")
                .param("keyword", "张三")
                .param("classId", "100")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(collegeService).getStudentList("张三", 100L, 1, 10);
    }

    @Test
    void testGetStudents_DefaultPagination() throws Exception {
        IPage<StudentInfo> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList());
        page.setTotal(0);

        when(collegeService.getStudentList(null, null, 1, 10)).thenReturn(page);

        mockMvc.perform(get("/api/college/v1/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(collegeService).getStudentList(null, null, 1, 10);
    }
}
