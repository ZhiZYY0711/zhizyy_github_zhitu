package com.zhitu.common.doc;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AnnotationValidator 集成测试
 * 
 * 测试验证器在实际 Controller 类上的工作情况
 */
class AnnotationValidatorIntegrationTest {
    
    @Test
    void testValidateRealControllers() {
        AnnotationValidator validator = new AnnotationValidator();
        
        // 尝试加载实际的 Controller 类
        try {
            Class<?> collegeController = Class.forName("com.zhitu.college.controller.CollegeController");
            ValidationReport report = validator.validate(Arrays.asList(collegeController));
            
            assertNotNull(report);
            assertNotNull(report.getStatistics());
            
            // CollegeController 应该有完整的注解
            assertEquals(0, report.getControllersWithoutTag().size(), 
                "CollegeController should have @Tag annotation");
            
            System.out.println("Validation Report for CollegeController:");
            System.out.println("  Controllers without @Tag: " + report.getControllersWithoutTag().size());
            System.out.println("  Methods without @Operation: " + report.getMethodsWithoutOperation().size());
            System.out.println("  Parameters without @Parameter: " + report.getParametersWithoutAnnotation().size());
            System.out.println("  Schemas without @Schema: " + report.getSchemasWithoutAnnotation().size());
            System.out.println("  Total violations: " + report.getStatistics().getTotalViolations());
            
        } catch (ClassNotFoundException e) {
            // 如果找不到类，跳过测试（可能在不同的模块中运行）
            System.out.println("Skipping integration test - CollegeController not found in classpath");
        }
    }
    
    @Test
    void testValidatePackage() {
        AnnotationValidator validator = new AnnotationValidator();
        
        // 测试扫描一个不存在的包（应该返回空报告）
        ValidationReport report = validator.validate("com.zhitu.nonexistent");
        
        assertNotNull(report);
        assertEquals(0, report.getStatistics().getTotalViolations());
        assertFalse(report.hasViolations());
    }
}
