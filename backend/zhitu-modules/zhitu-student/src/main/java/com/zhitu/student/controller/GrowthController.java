package com.zhitu.student.controller;

import com.zhitu.common.core.result.Result;
import com.zhitu.student.dto.EvaluationRequest;
import com.zhitu.student.entity.EvaluationRecord;
import com.zhitu.student.entity.GrowthBadge;
import com.zhitu.student.service.GrowthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 成长评价接口
 * GET  /api/growth/v1/evaluations          - 学生查看自己的成绩单
 * GET  /api/growth/v1/evaluations/student/{studentId} - 高校/企业查看指定学生
 * POST /api/growth/v1/evaluations/enterprise - 企业端写入评价
 * POST /api/growth/v1/evaluations/school     - 高校端写入评价
 * POST /api/training/v1/reviews/peer         - 同学互评
 * GET  /api/growth/v1/badges               - 学生查看证书与徽章
 * POST /api/growth/v1/badges               - 颁发证书/徽章
 */
@Tag(name = "成长评价", description = "学生成长评价相关接口")
@RestController
@RequiredArgsConstructor
public class GrowthController {

    private final GrowthService growthService;

    @Operation(
        summary = "获取我的评价记录",
        description = "学生查看自己的所有成长评价记录\n\n" +
            "**测试前置条件：**\n" +
            "- 用户必须以学生角色登录\n" +
            "- 必须携带有效的 JWT token\n\n" +
            "**数据依赖：**\n" +
            "- 查询 growth_evaluation 表\n" +
            "- 无需评价记录预先存在，空列表也是有效结果"
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
            responseCode = "401",
            description = "未授权访问",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 401, \"message\": \"未授权访问\", \"data\": null}"
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
    @GetMapping("/api/growth/v1/evaluations")
    public Result<List<EvaluationRecord>> getMyEvaluations() {
        return Result.ok(growthService.getMyEvaluations());
    }

    @Operation(
        summary = "获取指定学生的评价记录",
        description = "高校或企业查看指定学生的成长评价记录\n\n" +
            "**测试前置条件：**\n" +
            "- 用户必须以高校或企业角色登录\n" +
            "- 学生ID必须存在于 student_profile 表中\n" +
            "- 必须有权限查看该学生的评价（同一高校或有合作关系）\n\n" +
            "**数据依赖：**\n" +
            "- 依赖 student_profile 表中的学生记录\n" +
            "- 查询 growth_evaluation 表中的评价记录"
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
            responseCode = "403",
            description = "无权限访问",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 403, \"message\": \"无权限访问\", \"data\": null}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "学生不存在",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 404, \"message\": \"学生不存在\", \"data\": null}"
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
    @GetMapping("/api/growth/v1/evaluations/student/{studentId}")
    public Result<List<EvaluationRecord>> getStudentEvaluations(
            @Parameter(description = "学生ID", required = true, example = "1001")
            @PathVariable Long studentId) {
        return Result.ok(growthService.getStudentEvaluations(studentId));
    }

    @Operation(
        summary = "提交企业评价",
        description = "企业端对学生进行成长评价\n\n" +
            "**测试前置条件：**\n" +
            "- 用户必须以企业角色登录\n" +
            "- 学生ID必须存在于 student_profile 表中\n" +
            "- 学生必须在该企业实习或参与项目\n" +
            "- 评价维度分数必须在1-5范围内\n\n" +
            "**数据依赖：**\n" +
            "- 依赖 student_profile 表中的学生记录\n" +
            "- 依赖 internship_record 或 project_enrollment 表确认学生与企业的关系",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "评价请求信息",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = EvaluationRequest.class),
                examples = {
                    @ExampleObject(
                        name = "实习项目评价",
                        summary = "企业对实习学生的评价",
                        value = "{\"studentId\": 1001, \"sourceType\": \"enterprise\", \"refType\": \"internship\", \"refId\": 2001, \"scores\": \"{\\\"technical\\\":85,\\\"attitude\\\":90,\\\"teamwork\\\":88}\", \"comment\": \"该学生在实习期间表现优秀，技术能力强，工作态度积极，团队协作能力好\", \"hireRecommendation\": \"强烈推荐\"}"
                    ),
                    @ExampleObject(
                        name = "项目评价",
                        summary = "企业对项目参与学生的评价",
                        value = "{\"studentId\": 1002, \"sourceType\": \"enterprise\", \"refType\": \"project\", \"refId\": 3001, \"scores\": \"{\\\"technical\\\":78,\\\"attitude\\\":85,\\\"communication\\\":80}\", \"comment\": \"学生基础扎实，学习能力强，需要提升沟通表达能力\", \"hireRecommendation\": \"推荐\"}"
                    ),
                    @ExampleObject(
                        name = "边界值-最低分数",
                        summary = "所有评分为最低值0分",
                        value = "{\"studentId\": 1003, \"sourceType\": \"enterprise\", \"refType\": \"internship\", \"refId\": 2002, \"scores\": \"{\\\"technical\\\":0,\\\"attitude\\\":0,\\\"teamwork\\\":0}\", \"comment\": \"学生表现不符合要求\", \"hireRecommendation\": \"不推荐\"}"
                    ),
                    @ExampleObject(
                        name = "边界值-最高分数",
                        summary = "所有评分为最高值100分",
                        value = "{\"studentId\": 1004, \"sourceType\": \"enterprise\", \"refType\": \"internship\", \"refId\": 2003, \"scores\": \"{\\\"technical\\\":100,\\\"attitude\\\":100,\\\"teamwork\\\":100}\", \"comment\": \"学生表现完美，各方面都非常出色\", \"hireRecommendation\": \"强烈推荐\"}"
                    ),
                    @ExampleObject(
                        name = "边界值-最小学生ID",
                        summary = "学生ID为1（最小值）",
                        value = "{\"studentId\": 1, \"sourceType\": \"enterprise\", \"refType\": \"internship\", \"refId\": 2001, \"scores\": \"{\\\"technical\\\":85,\\\"attitude\\\":90}\", \"comment\": \"表现良好\", \"hireRecommendation\": \"推荐\"}"
                    ),
                    @ExampleObject(
                        name = "边界值-最大学生ID",
                        summary = "学生ID为Long.MAX_VALUE",
                        value = "{\"studentId\": 9223372036854775807, \"sourceType\": \"enterprise\", \"refType\": \"project\", \"refId\": 3001, \"scores\": \"{\\\"technical\\\":80}\", \"comment\": \"测试最大ID\", \"hireRecommendation\": \"推荐\"}"
                    ),
                    @ExampleObject(
                        name = "特殊字符-长评论",
                        summary = "包含很长评论的评价",
                        value = "{\"studentId\": 1005, \"sourceType\": \"enterprise\", \"refType\": \"internship\", \"refId\": 2004, \"scores\": \"{\\\"technical\\\":88,\\\"attitude\\\":92,\\\"teamwork\\\":90}\", \"comment\": \"该学生在为期三个月的实习期间表现非常出色。技术方面，能够快速掌握新技术，独立完成开发任务，代码质量高。工作态度方面，积极主动，遇到问题能够及时沟通，从不推诿责任。团队协作方面，与同事相处融洽，乐于分享知识，帮助他人解决问题。综合来看，是一位非常优秀的实习生，强烈建议公司考虑录用。\", \"hireRecommendation\": \"强烈推荐\"}"
                    ),
                    @ExampleObject(
                        name = "特殊字符-中英文混合",
                        summary = "评论包含中英文混合内容",
                        value = "{\"studentId\": 1006, \"sourceType\": \"enterprise\", \"refType\": \"project\", \"refId\": 3002, \"scores\": \"{\\\"technical\\\":85,\\\"attitude\\\":88}\", \"comment\": \"Student shows good understanding of Java and Spring Boot framework. 学生对技术栈掌握扎实，能够独立完成开发任务。Communication skills需要进一步提升。\", \"hireRecommendation\": \"推荐\"}"
                    ),
                    @ExampleObject(
                        name = "边界值-空推荐意见",
                        summary = "招聘推荐意见为空（可选字段）",
                        value = "{\"studentId\": 1007, \"sourceType\": \"enterprise\", \"refType\": \"internship\", \"refId\": 2005, \"scores\": \"{\\\"technical\\\":75,\\\"attitude\\\":80}\", \"comment\": \"学生表现中规中矩\"}"
                    ),
                    @ExampleObject(
                        name = "特殊字符-特殊符号",
                        summary = "评论包含特殊符号和标点",
                        value = "{\"studentId\": 1008, \"sourceType\": \"enterprise\", \"refType\": \"project\", \"refId\": 3003, \"scores\": \"{\\\"technical\\\":90,\\\"attitude\\\":95,\\\"innovation\\\":92}\", \"comment\": \"优秀！该学生具备以下特点：1）技术能力强；2）学习态度好；3）创新思维活跃。建议重点培养。\", \"hireRecommendation\": \"强烈推荐（优先考虑）\"}"
                    )
                }
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "提交成功",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 200, \"message\": \"操作成功\", \"data\": null}"
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
            responseCode = "403",
            description = "无权限操作",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 403, \"message\": \"无权限操作\", \"data\": null}"
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
    @PostMapping("/api/growth/v1/evaluations/enterprise")
    public Result<Void> submitEnterpriseEvaluation(
            @Parameter(description = "评价请求信息", required = true)
            @Valid @RequestBody EvaluationRequest req) {
        growthService.submitEvaluation(req, "enterprise");
        return Result.ok();
    }

    @Operation(
        summary = "提交高校评价",
        description = "高校端对学生进行成长评价\n\n" +
            "**测试前置条件：**\n" +
            "- 用户必须以高校角色登录（教师或辅导员）\n" +
            "- 学生ID必须存在于 student_profile 表中\n" +
            "- 学生必须属于该高校\n" +
            "- 评价维度分数必须在1-5范围内\n\n" +
            "**数据依赖：**\n" +
            "- 依赖 student_profile 表中的学生记录\n" +
            "- 学生的 collegeId 必须与评价者的 collegeId 匹配",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "评价请求信息",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = EvaluationRequest.class),
                examples = {
                    @ExampleObject(
                        name = "实训项目评价",
                        summary = "高校对实训项目学生的评价",
                        value = "{\"studentId\": 1001, \"sourceType\": \"school\", \"refType\": \"project\", \"refId\": 3001, \"scores\": \"{\\\"learning\\\":90,\\\"participation\\\":88,\\\"innovation\\\":85}\", \"comment\": \"学生在实训项目中表现出色，学习态度认真，积极参与讨论，具有创新思维\"}"
                    ),
                    @ExampleObject(
                        name = "实习评价",
                        summary = "高校对实习学生的评价",
                        value = "{\"studentId\": 1002, \"sourceType\": \"school\", \"refType\": \"internship\", \"refId\": 2001, \"scores\": \"{\\\"professionalism\\\":85,\\\"responsibility\\\":90}\", \"comment\": \"学生实习期间遵守纪律，责任心强，能够将理论知识应用到实践中\"}"
                    )
                }
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "提交成功",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 200, \"message\": \"操作成功\", \"data\": null}"
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
            responseCode = "403",
            description = "无权限操作",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 403, \"message\": \"无权限操作\", \"data\": null}"
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
    @PostMapping("/api/growth/v1/evaluations/school")
    public Result<Void> submitSchoolEvaluation(
            @Parameter(description = "评价请求信息", required = true)
            @Valid @RequestBody EvaluationRequest req) {
        growthService.submitEvaluation(req, "school");
        return Result.ok();
    }

    @Operation(
        summary = "提交同学互评",
        description = "学生之间进行互相评价\n\n" +
            "**测试前置条件：**\n" +
            "- 用户必须以学生角色登录\n" +
            "- 被评价学生ID必须存在于 student_profile 表中\n" +
            "- 两个学生必须在同一项目或班级中\n" +
            "- 不能评价自己\n\n" +
            "**数据依赖：**\n" +
            "- 依赖 student_profile 表中的学生记录\n" +
            "- 依赖 project_enrollment 或 college_organization 表确认学生关系"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "提交成功",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 200, \"message\": \"操作成功\", \"data\": null}"
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
            responseCode = "403",
            description = "无权限操作",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 403, \"message\": \"无权限操作\", \"data\": null}"
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
    @PostMapping("/api/training/v1/reviews/peer")
    public Result<Void> submitPeerReview(
            @Parameter(description = "评价请求信息", required = true)
            @Valid @RequestBody EvaluationRequest req) {
        growthService.submitEvaluation(req, "peer");
        return Result.ok();
    }

    @Operation(
        summary = "获取我的徽章",
        description = "学生查看自己获得的所有证书与徽章\n\n" +
            "**测试前置条件：**\n" +
            "- 用户必须以学生角色登录\n" +
            "- 必须携带有效的 JWT token\n\n" +
            "**数据依赖：**\n" +
            "- 查询 student_badge 表\n" +
            "- 无需徽章记录预先存在，空列表也是有效结果"
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
            responseCode = "401",
            description = "未授权访问",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 401, \"message\": \"未授权访问\", \"data\": null}"
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
    @GetMapping("/api/growth/v1/badges")
    public Result<List<GrowthBadge>> getMyBadges() {
        return Result.ok(growthService.getMyBadges());
    }

    @Operation(
        summary = "颁发证书或徽章",
        description = "管理员为学生颁发证书或徽章\n\n" +
            "**测试前置条件：**\n" +
            "- 用户必须有管理员权限（平台、高校或企业管理员）\n" +
            "- 学生ID必须存在于 student_profile 表中\n" +
            "- 证书/徽章模板ID必须存在\n\n" +
            "**数据依赖：**\n" +
            "- 依赖 student_profile 表中的学生记录\n" +
            "- 依赖 certificate_template 或 badge_template 表中的模板记录"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "颁发成功",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 200, \"message\": \"操作成功\", \"data\": null}"
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
            responseCode = "403",
            description = "无权限操作",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 403, \"message\": \"无权限操作\", \"data\": null}"
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
    @PostMapping("/api/growth/v1/badges")
    public Result<Void> issueBadge(
            @Parameter(description = "徽章信息", required = true)
            @RequestBody GrowthBadge badge) {
        growthService.issueBadge(badge);
        return Result.ok();
    }
}
