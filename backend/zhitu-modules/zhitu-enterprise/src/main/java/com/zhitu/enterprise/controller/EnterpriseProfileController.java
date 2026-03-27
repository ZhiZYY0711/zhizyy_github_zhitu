package com.zhitu.enterprise.controller;

import com.zhitu.common.core.result.Result;
import com.zhitu.enterprise.dto.AddStaffRequest;
import com.zhitu.enterprise.dto.EnterpriseProfileUpdateRequest;
import com.zhitu.enterprise.entity.EnterpriseInfo;
import com.zhitu.enterprise.entity.EnterpriseStaff;
import com.zhitu.enterprise.service.EnterpriseService;
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
 * 企业基础信息接口
 * GET  /api/user/v1/enterprise/profile  - 获取企业详情
 * PUT  /api/user/v1/enterprise/profile  - 更新企业信息
 * GET  /api/user/v1/enterprise/staff    - 员工列表
 * POST /api/user/v1/enterprise/staff    - 添加员工
 */
@Tag(name = "企业档案", description = "企业档案管理相关接口")
@RestController
@RequestMapping("/api/user/v1/enterprise")
@RequiredArgsConstructor
public class EnterpriseProfileController {

    private final EnterpriseService enterpriseService;

    @Operation(
        summary = "获取企业详情",
        description = "获取当前登录企业的详细信息，包括企业名称、简介、联系方式等\n\n" +
            "**测试前置条件：**\n" +
            "- 用户必须以企业角色登录\n" +
            "- 必须携带有效的 JWT token\n" +
            "- 企业信息必须已在 enterprise_info 表中存在\n\n" +
            "**数据依赖：**\n" +
            "- 依赖 enterprise_info 表中的企业记录\n" +
            "- 通过当前用户的 tenantId 查询企业信息"
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
            description = "未授权",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 401, \"message\": \"未授权\", \"data\": null}"
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
    @GetMapping("/profile")
    public Result<EnterpriseInfo> getProfile() {
        EnterpriseInfo profile = enterpriseService.getProfile();
        return Result.ok(profile);
    }

    @Operation(
        summary = "更新企业信息",
        description = "更新当前登录企业的基本信息\n\n" +
            "**测试前置条件：**\n" +
            "- 用户必须以企业角色登录\n" +
            "- 必须携带有效的 JWT token\n" +
            "- 企业信息必须已存在\n\n" +
            "**数据依赖：**\n" +
            "- 依赖 enterprise_info 表中的现有企业记录",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "企业信息更新请求",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = EnterpriseProfileUpdateRequest.class),
                examples = {
                    @ExampleObject(
                        name = "更新基本信息",
                        summary = "更新企业基本信息",
                        value = "{\"enterpriseName\": \"智途科技有限公司\", \"industry\": \"互联网\", \"scale\": \"100-500人\", \"province\": \"广东省\", \"city\": \"深圳市\", \"address\": \"南山区科技园\", \"logoUrl\": \"https://example.com/logo.png\", \"website\": \"https://www.zhitu.com\", \"description\": \"专注于教育科技的创新企业\"}"
                    ),
                    @ExampleObject(
                        name = "更新联系方式",
                        summary = "更新企业联系信息",
                        value = "{\"enterpriseName\": \"智途科技有限公司\", \"contactName\": \"李经理\", \"contactPhone\": \"13800138000\", \"contactEmail\": \"contact@zhitu.com\"}"
                    ),
                    @ExampleObject(
                        name = "完整更新",
                        summary = "更新企业完整信息",
                        value = "{\"enterpriseName\": \"智途科技有限公司\", \"industry\": \"互联网\", \"scale\": \"100-500人\", \"province\": \"广东省\", \"city\": \"深圳市\", \"address\": \"南山区科技园南区R2-B栋\", \"logoUrl\": \"https://example.com/logo.png\", \"website\": \"https://www.zhitu.com\", \"description\": \"专注于产教融合的教育科技创新企业，致力于为高校和学生提供优质的实习实训机会\", \"contactName\": \"李经理\", \"contactPhone\": \"13800138000\", \"contactEmail\": \"contact@zhitu.com\"}"
                    ),
                    @ExampleObject(
                        name = "边界值-最小规模",
                        summary = "企业规模为最小值",
                        value = "{\"enterpriseName\": \"小型创业公司\", \"industry\": \"科技\", \"scale\": \"1-10人\", \"province\": \"广东省\", \"city\": \"深圳市\", \"address\": \"南山区创业园\", \"contactName\": \"创始人\", \"contactPhone\": \"13900000000\", \"contactEmail\": \"founder@startup.com\"}"
                    ),
                    @ExampleObject(
                        name = "边界值-最大规模",
                        summary = "企业规模为最大值",
                        value = "{\"enterpriseName\": \"大型集团公司\", \"industry\": \"制造业\", \"scale\": \"10000+人\", \"province\": \"广东省\", \"city\": \"深圳市\", \"address\": \"福田区总部大厦\", \"contactName\": \"人力资源总监\", \"contactPhone\": \"0755-12345678\", \"contactEmail\": \"hr@large-corp.com\"}"
                    ),
                    @ExampleObject(
                        name = "特殊字符-长企业名",
                        summary = "包含很长名称的企业",
                        value = "{\"enterpriseName\": \"深圳市智途云计算科技有限责任公司（华南区总部）\", \"industry\": \"信息技术\", \"scale\": \"500-1000人\", \"province\": \"广东省\", \"city\": \"深圳市\", \"address\": \"南山区高新技术产业园区\", \"contactName\": \"张总监\", \"contactPhone\": \"0755-88888888\", \"contactEmail\": \"zhang@long-name-company.com\"}"
                    ),
                    @ExampleObject(
                        name = "特殊字符-国际企业",
                        summary = "包含英文的企业名称",
                        value = "{\"enterpriseName\": \"Zhitu Technology Co., Ltd.\", \"industry\": \"Software\", \"scale\": \"100-500人\", \"province\": \"广东省\", \"city\": \"深圳市\", \"address\": \"Nanshan District, Shenzhen\", \"website\": \"https://www.zhitu-tech.com\", \"contactName\": \"John Wang\", \"contactPhone\": \"+86-755-12345678\", \"contactEmail\": \"john.wang@zhitu-tech.com\"}"
                    ),
                    @ExampleObject(
                        name = "边界值-空网站",
                        summary = "网站URL为空（可选字段）",
                        value = "{\"enterpriseName\": \"无网站企业\", \"industry\": \"服务业\", \"scale\": \"50-100人\", \"province\": \"广东省\", \"city\": \"深圳市\", \"address\": \"罗湖区\", \"contactName\": \"李经理\", \"contactPhone\": \"13800138000\", \"contactEmail\": \"contact@no-website.com\"}"
                    ),
                    @ExampleObject(
                        name = "特殊字符-特殊行业",
                        summary = "包含特殊符号的行业名称",
                        value = "{\"enterpriseName\": \"创新企业\", \"industry\": \"互联网+教育\", \"scale\": \"100-500人\", \"province\": \"广东省\", \"city\": \"深圳市\", \"address\": \"南山区\", \"contactName\": \"王总\", \"contactPhone\": \"13900139000\", \"contactEmail\": \"wang@innovative.com\"}"
                    ),
                    @ExampleObject(
                        name = "边界值-长描述",
                        summary = "包含很长描述的企业信息",
                        value = "{\"enterpriseName\": \"详细描述企业\", \"industry\": \"科技\", \"scale\": \"100-500人\", \"province\": \"广东省\", \"city\": \"深圳市\", \"address\": \"南山区\", \"description\": \"这是一家专注于人工智能、大数据、云计算等前沿技术研发的高新技术企业。公司成立于2010年，拥有强大的研发团队和丰富的行业经验。我们致力于为客户提供最优质的技术解决方案和服务，推动产业数字化转型升级。\", \"contactName\": \"赵经理\", \"contactPhone\": \"13800138000\", \"contactEmail\": \"zhao@detailed.com\"}"
                    )
                }
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "更新成功",
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
    @PutMapping("/profile")
    public Result<Void> updateProfile(
            @Parameter(description = "企业信息更新请求", required = true)
            @Valid @RequestBody EnterpriseProfileUpdateRequest req) {
        enterpriseService.updateProfile(req);
        return Result.ok();
    }

    @Operation(
        summary = "获取员工列表",
        description = "获取当前企业的员工列表\n\n" +
            "**测试前置条件：**\n" +
            "- 用户必须以企业角色登录\n" +
            "- 必须携带有效的 JWT token\n\n" +
            "**数据依赖：**\n" +
            "- 查询 enterprise_staff 表\n" +
            "- 关联 sys_user 表获取员工详细信息"
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
    @GetMapping("/staff")
    public Result<List<EnterpriseStaff>> getStaff() {
        List<EnterpriseStaff> staff = enterpriseService.getStaffList();
        return Result.ok(staff);
    }

    @Operation(
        summary = "添加员工",
        description = "为当前企业添加新员工\n\n" +
            "**测试前置条件：**\n" +
            "- 用户必须以企业角色登录且有管理员权限\n" +
            "- userId 必须存在于 sys_user 表中\n" +
            "- 该用户不能已经是本企业的员工\n\n" +
            "**数据依赖：**\n" +
            "- 依赖 sys_user 表中的用户记录\n" +
            "- 依赖 enterprise_info 表中的企业记录",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "添加员工请求",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AddStaffRequest.class),
                examples = {
                    @ExampleObject(
                        name = "添加普通员工",
                        summary = "添加普通员工",
                        value = "{\"userId\": 1001, \"department\": \"技术部\", \"position\": \"软件工程师\", \"isMentor\": false}"
                    ),
                    @ExampleObject(
                        name = "添加导师",
                        summary = "添加企业导师",
                        value = "{\"userId\": 1002, \"department\": \"技术部\", \"position\": \"高级工程师\", \"isMentor\": true}"
                    ),
                    @ExampleObject(
                        name = "添加HR",
                        summary = "添加人力资源员工",
                        value = "{\"userId\": 1003, \"department\": \"人力资源部\", \"position\": \"招聘专员\", \"isMentor\": false}"
                    ),
                    @ExampleObject(
                        name = "边界值-最小用户ID",
                        summary = "用户ID为1（最小值）",
                        value = "{\"userId\": 1, \"department\": \"管理部\", \"position\": \"总经理\", \"isMentor\": false}"
                    ),
                    @ExampleObject(
                        name = "边界值-最大用户ID",
                        summary = "用户ID为Long.MAX_VALUE",
                        value = "{\"userId\": 9223372036854775807, \"department\": \"测试部\", \"position\": \"测试工程师\", \"isMentor\": false}"
                    ),
                    @ExampleObject(
                        name = "特殊字符-长部门名",
                        summary = "包含很长的部门名称",
                        value = "{\"userId\": 1004, \"department\": \"人工智能与大数据研发中心（华南区）\", \"position\": \"算法工程师\", \"isMentor\": true}"
                    ),
                    @ExampleObject(
                        name = "特殊字符-中英文混合",
                        summary = "中英文混合的职位名称",
                        value = "{\"userId\": 1005, \"department\": \"技术部\", \"position\": \"Senior Software Engineer高级软件工程师\", \"isMentor\": true}"
                    ),
                    @ExampleObject(
                        name = "特殊字符-特殊符号",
                        summary = "包含特殊符号的部门和职位",
                        value = "{\"userId\": 1006, \"department\": \"研发部-AI组\", \"position\": \"机器学习工程师（NLP方向）\", \"isMentor\": true}"
                    ),
                    @ExampleObject(
                        name = "边界值-导师标识",
                        summary = "导师标识为true",
                        value = "{\"userId\": 1007, \"department\": \"技术部\", \"position\": \"架构师\", \"isMentor\": true}"
                    ),
                    @ExampleObject(
                        name = "特殊字符-短名称",
                        summary = "最短的部门和职位名称",
                        value = "{\"userId\": 1008, \"department\": \"IT\", \"position\": \"PM\", \"isMentor\": false}"
                    )
                }
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "添加成功",
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
    @PostMapping("/staff")
    public Result<Void> addStaff(
            @Parameter(description = "添加员工请求", required = true)
            @Valid @RequestBody AddStaffRequest req) {
        enterpriseService.addStaff(req);
        return Result.ok();
    }
}
