package com.zhitu.platform.controller;

import com.zhitu.common.core.result.Result;
import com.zhitu.platform.dto.BannerDTO;
import com.zhitu.platform.dto.SaveBannerRequest;
import com.zhitu.platform.dto.SaveTopListRequest;
import com.zhitu.platform.dto.TopListDTO;
import com.zhitu.platform.service.PlatformRecommendationService;
import com.zhitu.platform.service.PlatformService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 平台门户接口（/api/portal-platform/v1）
 */
@Tag(name = "平台门户", description = "平台门户相关接口")
@RestController
@RequestMapping("/api/portal-platform/v1")
@RequiredArgsConstructor
public class PlatformPortalController {

    private final PlatformService platformService;
    private final PlatformRecommendationService recommendationService;

    // ── Recommendation Banners ────────────────────────────────────────────────

    /**
     * 获取推荐横幅列表
     * GET /api/portal-platform/v1/recommendations/banner
     * 
     * Requirements: 37.1, 37.4, 37.5, 37.6
     */
    @Operation(
        summary = "获取推荐横幅列表",
        description = "获取门户推荐横幅列表，支持按门户类型筛选\n\n" +
            "**测试前置条件：**\n" +
            "- 需要已登录用户\n" +
            "- 必须携带有效的 JWT token\n\n" +
            "**数据依赖：**\n" +
            "- 查询 portal_banner 表\n" +
            "- 横幅按门户类型（student/enterprise/college）分类"
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
    @GetMapping("/recommendations/banner")
    public Result<List<BannerDTO>> getRecommendationBanners(
            @Parameter(description = "门户类型：student-学生门户，enterprise-企业门户，college-高校门户", example = "student")
            @RequestParam(value = "portal", required = false) String portal) {
        return Result.ok(recommendationService.getBanners(portal));
    }

    /**
     * 保存推荐横幅
     * POST /api/portal-platform/v1/recommendations/banner
     * 
     * Requirements: 37.2, 37.3
     */
    @Operation(
        summary = "保存推荐横幅",
        description = "创建或更新推荐横幅配置\n\n" +
            "**测试前置条件：**\n" +
            "- 用户必须以平台管理员角色登录\n" +
            "- 横幅图片URL必须有效\n" +
            "- 门户类型必须为 student/enterprise/college 之一\n\n" +
            "**数据依赖：**\n" +
            "- 如果是更新操作，横幅ID必须存在于 portal_banner 表中",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "横幅保存请求",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SaveBannerRequest.class),
                examples = {
                    @ExampleObject(
                        name = "创建学生门户横幅",
                        summary = "为学生门户创建新横幅",
                        value = "{\"title\": \"春季校园招聘会\", \"imageUrl\": \"https://example.com/banner-spring.jpg\", \"linkUrl\": \"https://example.com/event/spring-2024\", \"targetPortal\": \"student\", \"startDate\": \"2024-03-01\", \"endDate\": \"2024-03-31\", \"sortOrder\": 1, \"status\": 1}"
                    ),
                    @ExampleObject(
                        name = "创建企业门户横幅",
                        summary = "为企业门户创建新横幅",
                        value = "{\"title\": \"优秀实习生推荐\", \"imageUrl\": \"https://example.com/banner-intern.jpg\", \"linkUrl\": \"https://example.com/students/recommended\", \"targetPortal\": \"enterprise\", \"startDate\": \"2024-04-01\", \"endDate\": \"2024-06-30\", \"sortOrder\": 1, \"status\": 1}"
                    ),
                    @ExampleObject(
                        name = "更新横幅",
                        summary = "更新现有横幅信息",
                        value = "{\"id\": 1, \"title\": \"春季校园招聘会（延期）\", \"imageUrl\": \"https://example.com/banner-spring-updated.jpg\", \"linkUrl\": \"https://example.com/event/spring-2024\", \"targetPortal\": \"student\", \"startDate\": \"2024-03-01\", \"endDate\": \"2024-04-15\", \"sortOrder\": 1, \"status\": 1}"
                    )
                }
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "保存成功",
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
    @PostMapping("/recommendations/banner")
    public Result<Void> saveRecommendationBanner(
            @Parameter(description = "横幅保存请求", required = true)
            @RequestBody SaveBannerRequest request) {
        recommendationService.saveBanner(request);
        return Result.ok();
    }

    // ── Top List ──────────────────────────────────────────────────────────────

    /**
     * 获取推荐榜单
     * GET /api/portal-platform/v1/recommendations/top-list
     * 
     * Requirements: 38.1, 38.3
     */
    @Operation(
        summary = "获取推荐榜单",
        description = "根据榜单类型获取推荐榜单数据\n\n" +
            "**测试前置条件：**\n" +
            "- 需要已登录用户\n" +
            "- 榜单类型必须为有效值（hot_projects/top_students/active_enterprises等）\n\n" +
            "**数据依赖：**\n" +
            "- 查询 ranking_list 表\n" +
            "- 榜单数据由系统定期计算生成"
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
    @GetMapping("/recommendations/top-list")
    public Result<TopListDTO> getTopList(
            @Parameter(description = "榜单类型：hot_projects-热门项目，top_enterprises-优质企业", required = true, example = "hot_projects")
            @RequestParam(value = "listType", required = true) String listType) {
        return Result.ok(recommendationService.getTopList(listType));
    }

    /**
     * 保存推荐榜单
     * POST /api/portal-platform/v1/recommendations/top-list
     * 
     * Requirements: 38.2, 38.4, 38.5, 38.6
     */
    @Operation(
        summary = "保存推荐榜单",
        description = "创建或更新推荐榜单配置\n\n" +
            "**测试前置条件：**\n" +
            "- 用户必须以平台管理员角色登录\n" +
            "- 榜单类型必须为有效值\n" +
            "- 榜单项目ID必须存在于对应的表中\n\n" +
            "**数据依赖：**\n" +
            "- 根据榜单类型，依赖不同的表（training_project, student_profile, enterprise_info等）",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "榜单保存请求",
            required = true,
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "热门项目榜单",
                        summary = "保存热门项目榜单",
                        value = "{\"listType\": \"hot_projects\", \"title\": \"本月热门实训项目\", \"items\": [{\"itemId\": 1001, \"rank\": 1}, {\"itemId\": 1002, \"rank\": 2}, {\"itemId\": 1003, \"rank\": 3}]}"
                    ),
                    @ExampleObject(
                        name = "优质企业榜单",
                        summary = "保存优质企业榜单",
                        value = "{\"listType\": \"top_enterprises\", \"title\": \"优质合作企业\", \"items\": [{\"itemId\": 2001, \"rank\": 1}, {\"itemId\": 2002, \"rank\": 2}, {\"itemId\": 2003, \"rank\": 3}]}"
                    ),
                    @ExampleObject(
                        name = "明星学生榜单",
                        summary = "保存明星学生榜单",
                        value = "{\"listType\": \"star_students\", \"title\": \"本月明星学生\", \"items\": [{\"itemId\": 3001, \"rank\": 1}, {\"itemId\": 3002, \"rank\": 2}, {\"itemId\": 3003, \"rank\": 3}]}"
                    )
                }
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "保存成功",
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
    @PostMapping("/recommendations/top-list")
    public Result<Void> saveTopList(
            @Parameter(description = "榜单保存请求", required = true)
            @RequestBody SaveTopListRequest request) {
        recommendationService.saveTopList(request);
        return Result.ok();
    }
}
