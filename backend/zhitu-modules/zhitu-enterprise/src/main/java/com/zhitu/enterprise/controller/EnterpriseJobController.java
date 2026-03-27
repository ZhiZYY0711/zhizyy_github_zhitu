package com.zhitu.enterprise.controller;

import com.zhitu.common.core.result.PageResult;
import com.zhitu.common.core.result.Result;
import com.zhitu.enterprise.dto.CreateJobRequest;
import com.zhitu.enterprise.dto.JobDTO;
import com.zhitu.enterprise.service.EnterpriseJobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 企业岗位管理控制器
 */
@Tag(name = "岗位管理", description = "企业岗位管理相关接口")
@Slf4j
@RestController
@RequestMapping("/api/internship/v1/enterprise/jobs")
@RequiredArgsConstructor
public class EnterpriseJobController {

    private final EnterpriseJobService enterpriseJobService;

    /**
     * 获取企业岗位列表
     * 
     * @param status 岗位状态 (可选: 1=招募中, 0=已关闭)
     * @param page 页码（从1开始，默认1）
     * @param size 每页大小（默认10）
     * @return 分页的岗位列表
     */
    @Operation(
        summary = "获取企业岗位列表",
        description = "分页查询企业发布的实习岗位列表，支持按状态筛选\n\n" +
            "**测试前置条件：**\n" +
            "- 用户必须以企业角色登录\n" +
            "- 必须携带有效的 JWT token\n\n" +
            "**数据依赖：**\n" +
            "- 查询 internship_job 表\n" +
            "- 通过当前用户的 enterpriseId 筛选岗位"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "查询成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Result.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "参数校验失败",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 422, \"message\": \"参数校验失败\", \"data\": null}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "服务器内部错误",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 500, \"message\": \"服务器内部错误\", \"data\": null}"
                )
            )
        )
    })
    @GetMapping
    public Result<PageResult<JobDTO>> getJobs(
            @Parameter(description = "岗位状态：1-招募中，0-已关闭", example = "1")
            @RequestParam(required = false) Integer status,
            @Parameter(description = "页码，从1开始", example = "1")
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页记录数", example = "10")
            @RequestParam(defaultValue = "10") Integer size) {
        
        log.debug("GET /api/internship/v1/enterprise/jobs - status: {}, page: {}, size: {}", status, page, size);
        
        PageResult<JobDTO> result = enterpriseJobService.getJobs(status, page, size);
        return Result.ok(result);
    }

    /**
     * 创建岗位
     * 
     * @param request 创建岗位请求
     * @return 创建的岗位ID
     */
    @Operation(
        summary = "创建实习岗位",
        description = "企业创建新的实习岗位信息\n\n" +
            "**测试前置条件：**\n" +
            "- 用户必须以企业角色登录\n" +
            "- 企业信息必须已完善（enterprise_info 表）\n" +
            "- 必须有发布岗位的权限\n\n" +
            "**数据依赖：**\n" +
            "- 依赖 enterprise_info 表中的企业记录\n" +
            "- 技能标签（techStack）建议从系统标签库中选择",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "岗位创建请求",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CreateJobRequest.class),
                examples = {
                    @ExampleObject(
                        name = "Java开发岗位",
                        summary = "创建Java开发实习岗位",
                        value = "{\"jobTitle\": \"Java开发实习生\", \"jobType\": \"技术类\", \"description\": \"负责后端服务开发，参与项目需求分析和系统设计\", \"requirements\": \"1. 熟悉Java语言，了解Spring Boot框架\\n2. 了解MySQL数据库\\n3. 有良好的学习能力和团队协作精神\", \"techStack\": [\"Java\", \"Spring Boot\", \"MySQL\", \"Redis\"], \"city\": \"深圳\", \"salaryMin\": 3000, \"salaryMax\": 5000, \"headcount\": 5, \"startDate\": \"2024-07-01\", \"endDate\": \"2024-09-30\"}"
                    ),
                    @ExampleObject(
                        name = "前端开发岗位",
                        summary = "创建前端开发实习岗位",
                        value = "{\"jobTitle\": \"前端开发实习生\", \"jobType\": \"技术类\", \"description\": \"负责Web前端开发，实现产品界面和交互功能\", \"requirements\": \"1. 熟悉HTML、CSS、JavaScript\\n2. 了解Vue或React框架\\n3. 有良好的审美和用户体验意识\", \"techStack\": [\"Vue.js\", \"TypeScript\", \"Element Plus\"], \"city\": \"北京\", \"salaryMin\": 3500, \"salaryMax\": 6000, \"headcount\": 3, \"startDate\": \"2024-07-01\", \"endDate\": \"2024-12-31\"}"
                    ),
                    @ExampleObject(
                        name = "产品运营岗位",
                        summary = "创建产品运营实习岗位",
                        value = "{\"jobTitle\": \"产品运营实习生\", \"jobType\": \"运营类\", \"description\": \"协助产品运营工作，包括用户调研、数据分析、活动策划等\", \"requirements\": \"1. 对互联网产品有浓厚兴趣\\n2. 具备良好的沟通能力和文案能力\\n3. 熟练使用Office办公软件\", \"techStack\": [\"数据分析\", \"用户运营\", \"活动策划\"], \"city\": \"上海\", \"salaryMin\": 2500, \"salaryMax\": 4000, \"headcount\": 2, \"startDate\": \"2024-06-01\", \"endDate\": \"2024-08-31\"}"
                    )
                }
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "创建成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Result.class),
                examples = @ExampleObject(
                    value = "{\"code\": 200, \"message\": \"操作成功\", \"data\": {\"id\": 1001}}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "参数校验失败",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 422, \"message\": \"参数校验失败\", \"data\": null}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "服务器内部错误",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 500, \"message\": \"服务器内部错误\", \"data\": null}"
                )
            )
        )
    })
    @PostMapping
    public Result<Map<String, Long>> createJob(
            @Parameter(description = "岗位创建请求", required = true)
            @RequestBody CreateJobRequest request) {
        log.debug("POST /api/internship/v1/enterprise/jobs - title: {}", request.getJobTitle());
        
        Long jobId = enterpriseJobService.createJob(request);
        return Result.ok(Map.of("id", jobId));
    }

    /**
     * 关闭岗位
     * 
     * @param id 岗位ID
     * @return 成功响应
     */
    @Operation(
        summary = "关闭岗位",
        description = "关闭指定的实习岗位，停止招募"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "关闭成功",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 200, \"message\": \"操作成功\", \"data\": null}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "岗位不存在",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 404, \"message\": \"岗位不存在\", \"data\": null}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "服务器内部错误",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 500, \"message\": \"服务器内部错误\", \"data\": null}"
                )
            )
        )
    })
    @PostMapping("/{id}/close")
    public Result<Void> closeJob(
            @Parameter(description = "岗位ID", required = true, example = "1001")
            @PathVariable Long id) {
        log.debug("POST /api/internship/v1/enterprise/jobs/{}/close", id);
        
        enterpriseJobService.closeJob(id);
        return Result.ok();
    }
}
