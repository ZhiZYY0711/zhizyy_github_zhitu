package com.zhitu.common.doc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AnnotationValidator 单元测试
 */
class AnnotationValidatorTest {
    
    private AnnotationValidator validator;
    
    @BeforeEach
    void setUp() {
        validator = new AnnotationValidator();
    }
    
    @Test
    void testValidateWithNullPackageName() {
        assertThrows(IllegalArgumentException.class, () -> {
            validator.validate((String) null);
        });
    }
    
    @Test
    void testValidateWithEmptyPackageName() {
        assertThrows(IllegalArgumentException.class, () -> {
            validator.validate("");
        });
    }
    
    @Test
    void testValidateWithNullControllersList() {
        assertThrows(IllegalArgumentException.class, () -> {
            validator.validate((List<Class<?>>) null);
        });
    }
    
    @Test
    void testValidateControllerWithoutTag() {
        List<Class<?>> controllers = Arrays.asList(ControllerWithoutTag.class);
        ValidationReport report = validator.validate(controllers);
        
        assertNotNull(report);
        assertEquals(1, report.getControllersWithoutTag().size());
        assertEquals(ControllerWithoutTag.class.getName(), 
                    report.getControllersWithoutTag().get(0).getClassName());
    }
    
    @Test
    void testValidateControllerWithTag() {
        List<Class<?>> controllers = Arrays.asList(ControllerWithTag.class);
        ValidationReport report = validator.validate(controllers);
        
        assertNotNull(report);
        assertEquals(0, report.getControllersWithoutTag().size());
    }
    
    @Test
    void testValidateMethodWithoutOperation() {
        List<Class<?>> controllers = Arrays.asList(ControllerWithMethodWithoutOperation.class);
        ValidationReport report = validator.validate(controllers);
        
        assertNotNull(report);
        assertEquals(1, report.getMethodsWithoutOperation().size());
        ValidationReport.MethodViolation violation = report.getMethodsWithoutOperation().get(0);
        assertEquals(ControllerWithMethodWithoutOperation.class.getName(), violation.getClassName());
        assertEquals("testMethod", violation.getMethodName());
    }
    
    @Test
    void testValidateMethodWithOperation() {
        List<Class<?>> controllers = Arrays.asList(ControllerWithMethodWithOperation.class);
        ValidationReport report = validator.validate(controllers);
        
        assertNotNull(report);
        assertEquals(0, report.getMethodsWithoutOperation().size());
    }
    
    @Test
    void testValidateParameterWithoutAnnotation() {
        List<Class<?>> controllers = Arrays.asList(ControllerWithParameterWithoutAnnotation.class);
        ValidationReport report = validator.validate(controllers);
        
        assertNotNull(report);
        assertEquals(1, report.getParametersWithoutAnnotation().size());
        ValidationReport.ParameterViolation violation = report.getParametersWithoutAnnotation().get(0);
        assertEquals(ControllerWithParameterWithoutAnnotation.class.getName(), violation.getClassName());
        assertEquals("testMethod", violation.getMethodName());
    }
    
    @Test
    void testValidateParameterWithAnnotation() {
        List<Class<?>> controllers = Arrays.asList(ControllerWithParameterWithAnnotation.class);
        ValidationReport report = validator.validate(controllers);
        
        assertNotNull(report);
        assertEquals(0, report.getParametersWithoutAnnotation().size());
    }
    
    @Test
    void testValidateDtoWithoutSchema() {
        List<Class<?>> controllers = Arrays.asList(ControllerWithDtoWithoutSchema.class);
        ValidationReport report = validator.validate(controllers);
        
        assertNotNull(report);
        assertTrue(report.getSchemasWithoutAnnotation().size() > 0);
        
        // 检查是否有类级别的 Schema 缺失
        boolean hasClassViolation = report.getSchemasWithoutAnnotation().stream()
            .anyMatch(v -> v.getType() == ValidationReport.SchemaViolation.ViolationType.CLASS_MISSING_SCHEMA);
        assertTrue(hasClassViolation);
    }
    
    @Test
    void testValidateDtoFieldWithoutSchema() {
        List<Class<?>> controllers = Arrays.asList(ControllerWithDtoFieldWithoutSchema.class);
        ValidationReport report = validator.validate(controllers);
        
        assertNotNull(report);
        assertTrue(report.getSchemasWithoutAnnotation().size() > 0);
        
        // 检查是否有字段级别的 Schema 缺失
        boolean hasFieldViolation = report.getSchemasWithoutAnnotation().stream()
            .anyMatch(v -> v.getType() == ValidationReport.SchemaViolation.ViolationType.FIELD_MISSING_SCHEMA);
        assertTrue(hasFieldViolation);
    }
    
    @Test
    void testValidateCompleteController() {
        List<Class<?>> controllers = Arrays.asList(CompleteController.class);
        ValidationReport report = validator.validate(controllers);
        
        assertNotNull(report);
        assertEquals(0, report.getControllersWithoutTag().size());
        assertEquals(0, report.getMethodsWithoutOperation().size());
        assertEquals(0, report.getParametersWithoutAnnotation().size());
    }
    
    @Test
    void testValidationReportStatistics() {
        List<Class<?>> controllers = Arrays.asList(
            ControllerWithoutTag.class,
            ControllerWithMethodWithoutOperation.class
        );
        ValidationReport report = validator.validate(controllers);
        
        assertNotNull(report);
        assertNotNull(report.getStatistics());
        assertTrue(report.getStatistics().getTotalViolations() > 0);
        assertTrue(report.hasViolations());
    }
    
    @Test
    void testValidationReportTimestamp() {
        List<Class<?>> controllers = Arrays.asList(CompleteController.class);
        ValidationReport report = validator.validate(controllers);
        
        assertNotNull(report);
        assertNotNull(report.getTimestamp());
    }
    
    // 测试用的 Controller 类
    
    @RestController
    static class ControllerWithoutTag {
        @GetMapping("/test")
        @Operation(summary = "Test")
        public String test() {
            return "test";
        }
    }
    
    @Tag(name = "Test Controller")
    @RestController
    static class ControllerWithTag {
        @GetMapping("/test")
        @Operation(summary = "Test")
        public String test() {
            return "test";
        }
    }
    
    @Tag(name = "Test Controller")
    @RestController
    static class ControllerWithMethodWithoutOperation {
        @GetMapping("/test")
        public String testMethod() {
            return "test";
        }
    }
    
    @Tag(name = "Test Controller")
    @RestController
    static class ControllerWithMethodWithOperation {
        @GetMapping("/test")
        @Operation(summary = "Test method")
        public String testMethod() {
            return "test";
        }
    }
    
    @Tag(name = "Test Controller")
    @RestController
    static class ControllerWithParameterWithoutAnnotation {
        @GetMapping("/test")
        @Operation(summary = "Test method")
        public String testMethod(@RequestParam String param) {
            return "test";
        }
    }
    
    @Tag(name = "Test Controller")
    @RestController
    static class ControllerWithParameterWithAnnotation {
        @GetMapping("/test")
        @Operation(summary = "Test method")
        public String testMethod(
            @Parameter(description = "Test parameter")
            @RequestParam String param
        ) {
            return "test";
        }
    }
    
    @Tag(name = "Test Controller")
    @RestController
    static class ControllerWithDtoWithoutSchema {
        @PostMapping("/test")
        @Operation(summary = "Test method")
        public String testMethod(@RequestBody DtoWithoutSchema dto) {
            return "test";
        }
    }
    
    static class DtoWithoutSchema {
        private String name;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
    }
    
    @Tag(name = "Test Controller")
    @RestController
    static class ControllerWithDtoFieldWithoutSchema {
        @PostMapping("/test")
        @Operation(summary = "Test method")
        public String testMethod(@RequestBody DtoWithFieldWithoutSchema dto) {
            return "test";
        }
    }
    
    @Schema(description = "Test DTO")
    static class DtoWithFieldWithoutSchema {
        private String name;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
    }
    
    @Tag(name = "Complete Controller")
    @RestController
    static class CompleteController {
        @GetMapping("/test")
        @Operation(summary = "Test method")
        public String testMethod(
            @Parameter(description = "Test parameter")
            @RequestParam String param
        ) {
            return "test";
        }
        
        @PostMapping("/create")
        @Operation(summary = "Create method")
        public String createMethod(
            @Parameter(description = "Test DTO")
            @RequestBody CompleteDto dto
        ) {
            return "created";
        }
    }
    
    @Schema(description = "Complete DTO")
    static class CompleteDto {
        @Schema(description = "Name field")
        private String name;
        
        @Schema(description = "Age field")
        private Integer age;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public Integer getAge() {
            return age;
        }
        
        public void setAge(Integer age) {
            this.age = age;
        }
    }
}
