# OpenAPI 3.0 Documentation Guide

## Overview

This document provides guidelines for generating and maintaining OpenAPI 3.0 specifications for all Zhitu Cloud Platform API endpoints. The platform uses SpringDoc OpenAPI to automatically generate API documentation.

## Setup

### 1. Add Dependencies

Add SpringDoc OpenAPI dependency to each microservice `pom.xml`:

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.2.0</version>
</dependency>
```

### 2. Configuration

Create OpenAPI configuration class in each microservice:

```java
package com.zhitu.student.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI studentPortalOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Zhitu Student Portal API")
                        .description("API documentation for Zhitu Cloud Platform Student Portal")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Zhitu Platform Team")
                                .email("support@zhitu.com")
                                .url("https://zhitu.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development Server"),
                        new Server()
                                .url("https://api.zhitu.com")
                                .description("Production Server")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT authentication token")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
```

### 3. Application Properties

Configure SpringDoc in `application.yml`:

```yaml
springdoc:
  api-docs:
    path: /v3/api-docs
    enabled: true
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    operations-sorter: method
    tags-sorter: alpha
  show-actuator: false
  default-consumes-media-type: application/json
  default-produces-media-type: application/json
```

## Controller Annotations

### Basic Controller Documentation

```java
package com.zhitu.student.controller;

import com.zhitu.common.core.result.Result;
import com.zhitu.common.core.result.PageResult;
import com.zhitu.student.dto.*;
import com.zhitu.student.service.StudentPortalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Student Portal", description = "Student portal dashboard and management APIs")
@RestController
@RequestMapping("/api/student-portal/v1")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class StudentPortalController {

    private final StudentPortalService studentPortalService;

    @Operation(
        summary = "Get student dashboard statistics",
        description = "Retrieves dashboard statistics including training project count, internship job count, pending task count, and growth score for the authenticated student"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved dashboard statistics",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DashboardStatsResponse.class),
                examples = @ExampleObject(
                    name = "Success Response",
                    value = """
                        {
                          "code": 200,
                          "message": "success",
                          "data": {
                            "trainingProjectCount": 3,
                            "internshipJobCount": 15,
                            "pendingTaskCount": 5,
                            "growthScore": 85
                          }
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "code": 401,
                          "message": "Unauthorized: Invalid token"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "code": 500,
                          "message": "Internal server error",
                          "errorId": "ERR-2024-001"
                        }
                        """
                )
            )
        )
    })
    @GetMapping("/dashboard")
    public Result<DashboardStatsDTO> getDashboardStats() {
        return Result.ok(studentPortalService.getDashboardStats());
    }

    @Operation(
        summary = "Get student tasks",
        description = "Retrieves paginated list of student tasks filtered by status"
    )
    @Parameters({
        @Parameter(
            name = "status",
            description = "Task status filter",
            required = false,
            schema = @Schema(type = "string", allowableValues = {"pending", "completed"}),
            example = "pending"
        ),
        @Parameter(
            name = "page",
            description = "Page number (1-indexed)",
            required = false,
            schema = @Schema(type = "integer", defaultValue = "1", minimum = "1"),
            example = "1"
        ),
        @Parameter(
            name = "size",
            description = "Page size",
            required = false,
            schema = @Schema(type = "integer", defaultValue = "10", minimum = "1", maximum = "100"),
            example = "10"
        )
    })
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved task list",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TaskPageResponse.class)
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid parameters"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/tasks")
    public Result<PageResult<TaskDTO>> getTasks(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.ok(studentPortalService.getTasks(status, page, size));
    }

    @Operation(
        summary = "Get project scrum board",
        description = "Retrieves scrum board with tasks organized by status (todo, in_progress, done) for a specific project. Student must be enrolled in the project."
    )
    @Parameter(
        name = "id",
        description = "Project ID",
        required = true,
        schema = @Schema(type = "integer", format = "int64"),
        example = "1001"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved scrum board",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ScrumBoardResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Student not enrolled in project",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "code": 403,
                          "message": "Access denied: Student not enrolled in project"
                        }
                        """
                )
            )
        ),
        @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @GetMapping("/training/projects/{id}/board")
    public Result<ScrumBoardDTO> getProjectBoard(@PathVariable Long id) {
        return Result.ok(studentPortalService.getProjectBoard(id));
    }
}
```

### DTO Schema Documentation

```java
package com.zhitu.student.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Dashboard statistics response")
public class DashboardStatsDTO {
    
    @Schema(
        description = "Number of training projects the student is enrolled in",
        example = "3",
        minimum = "0"
    )
    private Integer trainingProjectCount;
    
    @Schema(
        description = "Number of available internship job postings",
        example = "15",
        minimum = "0"
    )
    private Integer internshipJobCount;
    
    @Schema(
        description = "Number of pending tasks requiring student action",
        example = "5",
        minimum = "0"
    )
    private Integer pendingTaskCount;
    
    @Schema(
        description = "Student's overall growth score (0-100)",
        example = "85",
        minimum = "0",
        maximum = "100"
    )
    private Integer growthScore;
}

@Data
@Schema(description = "Task information")
public class TaskDTO {
    
    @Schema(description = "Task ID", example = "1001")
    private Long id;
    
    @Schema(
        description = "Task type",
        allowableValues = {"training", "internship", "evaluation"},
        example = "training"
    )
    private String taskType;
    
    @Schema(description = "Task title", example = "Complete project milestone 1")
    private String title;
    
    @Schema(description = "Task description", example = "Implement user authentication module")
    private String description;
    
    @Schema(
        description = "Task priority",
        allowableValues = {"1", "2", "3"},
        example = "2"
    )
    private Integer priority;
    
    @Schema(
        description = "Task status",
        allowableValues = {"0", "1"},
        example = "0"
    )
    private Integer status;
    
    @Schema(description = "Task due date", example = "2024-12-31T23:59:59")
    private String dueDate;
}
```

### Request Body Documentation

```java
package com.zhitu.enterprise.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import javax.validation.constraints.*;

@Data
@Schema(description = "Request to create a new job posting")
public class CreateJobRequest {
    
    @Schema(
        description = "Job title",
        example = "Java Backend Developer Intern",
        required = true,
        minLength = 1,
        maxLength = 200
    )
    @NotBlank(message = "Job title is required")
    @Size(max = 200, message = "Job title must not exceed 200 characters")
    private String title;
    
    @Schema(
        description = "Job description",
        example = "We are looking for a motivated Java developer intern...",
        required = true
    )
    @NotBlank(message = "Job description is required")
    private String description;
    
    @Schema(
        description = "Job requirements",
        example = "- Proficiency in Java\n- Understanding of Spring Boot\n- Good communication skills",
        required = true
    )
    @NotBlank(message = "Job requirements are required")
    private String requirements;
    
    @Schema(
        description = "Minimum salary (monthly, in CNY)",
        example = "5000",
        required = true,
        minimum = "0"
    )
    @NotNull(message = "Minimum salary is required")
    @Min(value = 0, message = "Minimum salary must be non-negative")
    private Integer salaryMin;
    
    @Schema(
        description = "Maximum salary (monthly, in CNY)",
        example = "8000",
        required = true,
        minimum = "0"
    )
    @NotNull(message = "Maximum salary is required")
    @Min(value = 0, message = "Maximum salary must be non-negative")
    private Integer salaryMax;
    
    @Schema(
        description = "Job location",
        example = "Beijing, China",
        required = false
    )
    private String location;
    
    @Schema(
        description = "Required skills (comma-separated)",
        example = "Java,Spring Boot,MySQL,Redis",
        required = false
    )
    private String requiredSkills;
}
```

## Response Wrapper Documentation

```java
package com.zhitu.common.core.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Standard API response wrapper")
public class Result<T> {
    
    @Schema(
        description = "Response code (200 = success, 4xx = client error, 5xx = server error)",
        example = "200"
    )
    private Integer code;
    
    @Schema(
        description = "Response message",
        example = "success"
    )
    private String message;
    
    @Schema(description = "Response data payload")
    private T data;
    
    @Schema(
        description = "Error ID for support reference (only present on errors)",
        example = "ERR-2024-001",
        nullable = true
    )
    private String errorId;
}

@Data
@Schema(description = "Paginated response wrapper")
public class PageResult<T> {
    
    @Schema(description = "Current page number (1-indexed)", example = "1")
    private Integer page;
    
    @Schema(description = "Page size", example = "10")
    private Integer size;
    
    @Schema(description = "Total number of records", example = "150")
    private Long total;
    
    @Schema(description = "List of records for current page")
    private List<T> records;
}
```

## Accessing API Documentation

### Swagger UI

Access interactive API documentation at:
- Development: `http://localhost:8080/swagger-ui.html`
- Production: `https://api.zhitu.com/swagger-ui.html`

### OpenAPI JSON

Access raw OpenAPI specification at:
- Development: `http://localhost:8080/v3/api-docs`
- Production: `https://api.zhitu.com/v3/api-docs`

### OpenAPI YAML

Access YAML format at:
- Development: `http://localhost:8080/v3/api-docs.yaml`
- Production: `https://api.zhitu.com/v3/api-docs.yaml`

## Microservice API Documentation URLs

| Service | Swagger UI | OpenAPI JSON |
|---------|-----------|--------------|
| Student Portal | http://localhost:8081/swagger-ui.html | http://localhost:8081/v3/api-docs |
| Enterprise Portal | http://localhost:8082/swagger-ui.html | http://localhost:8082/v3/api-docs |
| College Portal | http://localhost:8083/swagger-ui.html | http://localhost:8083/v3/api-docs |
| Platform Admin | http://localhost:8084/swagger-ui.html | http://localhost:8084/v3/api-docs |
| Gateway (Aggregated) | http://localhost:8080/swagger-ui.html | http://localhost:8080/v3/api-docs |

## Best Practices

### 1. Use Descriptive Summaries

```java
@Operation(
    summary = "Get student dashboard statistics", // Short, action-oriented
    description = "Retrieves comprehensive dashboard statistics..." // Detailed explanation
)
```

### 2. Document All Parameters

```java
@Parameter(
    name = "status",
    description = "Filter tasks by status",
    required = false,
    schema = @Schema(allowableValues = {"pending", "completed"}),
    example = "pending"
)
```

### 3. Provide Response Examples

```java
@ApiResponse(
    responseCode = "200",
    content = @Content(
        examples = @ExampleObject(
            name = "Success Example",
            value = "{ \"code\": 200, \"data\": {...} }"
        )
    )
)
```

### 4. Document Error Responses

```java
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(responseCode = "400", description = "Bad Request"),
    @ApiResponse(responseCode = "401", description = "Unauthorized"),
    @ApiResponse(responseCode = "403", description = "Forbidden"),
    @ApiResponse(responseCode = "404", description = "Not Found"),
    @ApiResponse(responseCode = "500", description = "Internal Server Error")
})
```

### 5. Use Schema Validation Annotations

```java
@Schema(
    description = "Email address",
    example = "user@example.com",
    pattern = "^[A-Za-z0-9+_.-]+@(.+)$",
    required = true
)
@Email
@NotBlank
private String email;
```

## Generating Static Documentation

### Export OpenAPI Specification

```bash
# Export JSON
curl http://localhost:8080/v3/api-docs > openapi.json

# Export YAML
curl http://localhost:8080/v3/api-docs.yaml > openapi.yaml
```

### Generate HTML Documentation

Use Redoc or ReDoc CLI:

```bash
# Install redoc-cli
npm install -g redoc-cli

# Generate HTML
redoc-cli bundle openapi.yaml -o api-docs.html
```

### Generate PDF Documentation

Use wkhtmltopdf:

```bash
# Install wkhtmltopdf
# Then convert HTML to PDF
wkhtmltopdf api-docs.html api-docs.pdf
```

## API Documentation Checklist

- [ ] Add SpringDoc dependency to all microservices
- [ ] Create OpenAPIConfig class for each service
- [ ] Add @Tag annotation to all controllers
- [ ] Add @Operation annotation to all endpoints
- [ ] Document all request parameters with @Parameter
- [ ] Document all request bodies with @Schema
- [ ] Document all response codes with @ApiResponse
- [ ] Provide examples for all responses
- [ ] Document authentication requirements
- [ ] Document error responses
- [ ] Add validation annotations to DTOs
- [ ] Test Swagger UI accessibility
- [ ] Export OpenAPI specification files
- [ ] Generate static HTML documentation
- [ ] Review documentation for completeness
- [ ] Publish documentation to team

## Conclusion

Comprehensive API documentation improves developer experience and reduces integration time. Follow this guide to maintain high-quality, up-to-date API documentation for all Zhitu Cloud Platform endpoints.
