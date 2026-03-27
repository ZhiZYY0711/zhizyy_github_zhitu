# 实施计划：API 文档与 Swagger 集成

## 概述

本实施计划将智途云平台的 API 文档系统从设计转化为可执行的开发任务。系统基于 SpringDoc OpenAPI 2.5.0 和 Spring Boot 3.2.5，为 7 个微服务模块提供标准化的 API 文档生成、Gateway 层文档聚合、自动化变更检测和访问控制功能。

实施策略采用增量开发方式：首先建立公共基础设施（依赖配置和公共模块），然后逐步为各微服务添加 Swagger 注解，接着实现 Gateway 文档聚合，最后完成文档生成、变更检测和访问控制等高级功能。

## 任务列表

- [x] 1. 配置项目依赖和公共基础设施
  - [x] 1.1 在父 pom.xml 中添加 SpringDoc OpenAPI 依赖管理
    - 在 `backend/pom.xml` 的 `<dependencyManagement>` 中添加 `springdoc-openapi-starter-webmvc-ui` 版本 2.5.0
    - 添加 `springdoc-openapi-maven-plugin` 版本 1.4
    - _需求: 3.1, 8.1_

  - [x] 1.2 在各微服务 pom.xml 中添加 SpringDoc 依赖
    - 为 zhitu-auth、zhitu-college、zhitu-enterprise、zhitu-platform、zhitu-student、zhitu-system 添加依赖
    - 在每个服务的 pom.xml 中添加 `springdoc-openapi-starter-webmvc-ui` 依赖（无需指定版本）
    - _需求: 3.1_

  - [x] 1.3 创建公共 OpenAPI 配置类
    - 在 `zhitu-common/zhitu-common-core/src/main/java/com/zhitu/common/config/OpenApiConfig.java` 创建配置类
    - 实现 `customOpenAPI()` 方法，配置 API 信息（标题、版本、描述）
    - 实现 `jwtSecurityScheme()` 方法，配置 JWT Bearer 认证方案
    - 实现 `publicApi()` 方法，配置 API 分组
    - _需求: 3.2, 3.3, 1.5_


  - [ ]* 1.4 为公共 OpenAPI 配置类编写单元测试
    - 测试 JWT 安全方案配置正确性
    - 测试 API 信息设置正确性
    - 测试不同环境配置的加载
    - _需求: 3.3_

- [x] 2. 为 zhitu-auth 服务添加 Swagger 注解
  - [x] 2.1 为 AuthController 添加 @Tag 和 @Operation 注解
    - 在类级别添加 `@Tag(name = "认证管理", description = "用户认证和令牌管理接口")`
    - 为 login、logout、refresh 等方法添加 `@Operation` 注解，描述接口功能
    - 为所有请求参数添加 `@Parameter` 注解
    - _需求: 4.1, 4.2, 4.3_

  - [x] 2.2 为 AuthController 的 DTO 类添加 @Schema 注解
    - 为 LoginRequest、LoginResponse、TokenRefreshRequest 等 DTO 类添加 `@Schema` 注解
    - 为 DTO 类的所有字段添加 `@Schema` 注解，包含描述、示例值、是否必填
    - _需求: 4.4, 4.5, 4.7_

  - [x] 2.3 为 AuthController 添加错误响应注解
    - 使用 `@ApiResponse` 注解描述 401、403、500 等错误场景
    - 为每个错误响应提供示例值
    - _需求: 4.6, 4.7_

  - [ ]* 2.4 为 AuthController 编写单元测试
    - 测试注解完整性（验证所有方法都有 @Operation 注解）
    - 测试 DTO 字段注解完整性
    - _需求: 4.1, 4.2, 4.3, 4.4, 4.5_

- [x] 3. 为 zhitu-system 服务添加 Swagger 注解
  - [x] 3.1 为 UserController、RoleController、MenuController 添加注解
    - 为每个 Controller 类添加 `@Tag` 注解
    - 为所有 Controller 方法添加 `@Operation` 注解
    - 为所有请求参数添加 `@Parameter` 注解
    - 为所有响应添加 `@ApiResponse` 注解
    - _需求: 4.1, 4.2, 4.3, 4.6_

  - [x] 3.2 为 zhitu-system 的 DTO 类添加 @Schema 注解
    - 为 UserDTO、RoleDTO、MenuDTO 等类及其字段添加 `@Schema` 注解
    - 包含描述、示例值、验证规则
    - _需求: 4.4, 4.5, 4.7_

  - [ ]* 3.3 编写属性测试验证注解完整性
    - **属性 8: 注解完整性**
    - **验证需求: 4.1, 4.2, 4.3, 4.4, 4.5**
    - 使用 jqwik 生成随机 Controller 类，验证所有类、方法、参数、DTO 字段都有相应注解

- [x] 4. 为 zhitu-college 服务添加 Swagger 注解
  - [x] 4.1 为 CollegeController、MajorController 添加完整注解
    - 添加 `@Tag`、`@Operation`、`@Parameter`、`@ApiResponse` 注解
    - _需求: 4.1, 4.2, 4.3, 4.6_

  - [x] 4.2 为 zhitu-college 的 DTO 类添加 @Schema 注解
    - 为所有 DTO 类及字段添加注解
    - _需求: 4.4, 4.5, 4.7_

- [x] 5. 为 zhitu-enterprise 服务添加 Swagger 注解
  - [x] 5.1 为 EnterpriseController、PositionController 添加完整注解
    - 添加 `@Tag`、`@Operation`、`@Parameter`、`@ApiResponse` 注解
    - _需求: 4.1, 4.2, 4.3, 4.6_

  - [x] 5.2 为 zhitu-enterprise 的 DTO 类添加 @Schema 注解
    - 为所有 DTO 类及字段添加注解
    - _需求: 4.4, 4.5, 4.7_

- [x] 6. 为 zhitu-platform 服务添加 Swagger 注解
  - [x] 6.1 为 PlatformController 添加完整注解
    - 添加 `@Tag`、`@Operation`、`@Parameter`、`@ApiResponse` 注解
    - _需求: 4.1, 4.2, 4.3, 4.6_

  - [x] 6.2 为 zhitu-platform 的 DTO 类添加 @Schema 注解
    - 为所有 DTO 类及字段添加注解
    - _需求: 4.4, 4.5, 4.7_

- [x] 7. 为 zhitu-student 服务添加 Swagger 注解
  - [x] 7.1 为 StudentController、ResumeController 添加完整注解
    - 添加 `@Tag`、`@Operation`、`@Parameter`、`@ApiResponse` 注解
    - _需求: 4.1, 4.2, 4.3, 4.6_

  - [x] 7.2 为 zhitu-student 的 DTO 类添加 @Schema 注解
    - 为所有 DTO 类及字段添加注解
    - _需求: 4.4, 4.5, 4.7_

- [x] 8. 检查点 - 验证各微服务 Swagger UI 可访问
  - 启动各微服务，访问 `/swagger-ui.html` 端点
  - 确认所有 API 端点正确显示
  - 确认可以在 Swagger UI 中测试 API
  - 如有问题请咨询用户


- [x] 9. 实现 Gateway 文档聚合功能
  - [x] 9.1 在 zhitu-gateway 中添加 SpringDoc Gateway 依赖
    - 在 `zhitu-gateway/pom.xml` 中添加 `springdoc-openapi-starter-webflux-ui` 依赖
    - 注意：Gateway 使用 WebFlux，需要使用 webflux-ui 而非 webmvc-ui
    - _需求: 5.1_

  - [x] 9.2 创建 Swagger 聚合配置属性类
    - 在 `zhitu-gateway/src/main/java/com/zhitu/gateway/config/SwaggerProperties.java` 创建配置属性类
    - 使用 `@ConfigurationProperties(prefix = "swagger")` 注解
    - 定义 enabled、aggregationEnabled、services、accessControl 等属性
    - _需求: 5.1, 5.2, 7.5_

  - [x] 9.3 实现 Gateway 文档聚合器
    - 在 `zhitu-gateway/src/main/java/com/zhitu/gateway/config/SwaggerAggregationConfig.java` 创建配置类
    - 实现 `apis()` 方法，从 Nacos DiscoveryClient 获取服务列表
    - 为每个服务创建 `GroupedOpenApi` 分组
    - 实现 `createServiceGroup()` 方法，配置服务路径前缀
    - 实现服务不可用时的错误处理逻辑
    - _需求: 5.1, 5.2, 5.4, 5.5, 5.7_

  - [x] 9.4 配置 Gateway 的 application.yml
    - 添加 `springdoc.swagger-ui.urls` 配置，指向各微服务的 OpenAPI 端点
    - 配置 `springdoc.api-docs.enabled: true`
    - 配置服务发现相关参数
    - _需求: 5.3, 5.4_

  - [ ]* 9.5 为 Gateway 文档聚合编写单元测试
    - 测试单个服务的文档分组创建
    - 测试服务不可用时的错误处理
    - 测试路径前缀保留逻辑
    - _需求: 5.2, 5.5, 5.7_

  - [ ]* 9.6 编写属性测试验证 Gateway 聚合完整性
    - **属性 7: Gateway 文档聚合完整性**
    - **验证需求: 3.7, 5.2, 5.4, 5.5**
    - 使用 jqwik 生成随机服务列表，验证 Gateway 为每个服务创建文档分组且保留路径前缀

- [x] 10. 实现文档生成和变更检测功能
  - [x] 10.1 创建文档元数据模型
    - 在 `zhitu-common/zhitu-common-core/src/main/java/com/zhitu/common/doc/DocumentMetadata.java` 创建模型类
    - 定义 serviceName、version、generatedAt、generatedBy、environment 等字段
    - _需求: 1.1, 1.6, 6.1_

  - [x] 10.2 创建变更报告模型
    - 在 `zhitu-common/zhitu-common-core/src/main/java/com/zhitu/common/doc/ChangeReport.java` 创建模型类
    - 定义 EndpointChange、SchemaChange、ChangeStatistics 等内部类
    - _需求: 6.2, 6.3, 6.4, 6.5, 6.6_

  - [x] 10.3 实现变更检测器
    - 在 `zhitu-common/zhitu-common-core/src/main/java/com/zhitu/common/doc/ChangeDetector.java` 创建组件
    - 实现 `detectChanges()` 方法，比较两个 OpenAPI 规范
    - 实现 `compareEndpoints()` 方法，识别端点变更
    - 实现 `compareSchemas()` 方法，识别数据模型变更
    - _需求: 6.2, 6.3, 6.4, 6.5, 6.6_

  - [x] 10.4 配置 Maven 文档生成插件
    - 在各微服务的 pom.xml 中添加 `springdoc-openapi-maven-plugin` 配置
    - 配置 `apiDocsUrl` 指向本地服务端点
    - 配置 `outputDir` 为 `docs/api/{module-name}`
    - 配置 `outputFileName` 为 `openapi.json`
    - _需求: 8.1, 8.4, 8.5, 8.6_

  - [x] 10.5 创建注解验证器
    - 在 `zhitu-common/zhitu-common-core/src/main/java/com/zhitu/common/doc/AnnotationValidator.java` 创建验证器
    - 实现扫描 Controller 类的逻辑
    - 实现检查 @Tag、@Operation、@Parameter、@Schema 注解的逻辑
    - 生成注解缺失报告
    - _需求: 8.2, 8.3_

  - [ ]* 10.6 为变更检测器编写单元测试
    - 测试新增端点的识别
    - 测试删除端点的识别
    - 测试参数变更的识别
    - 测试响应格式变更的识别
    - _需求: 6.2, 6.3, 6.4, 6.5, 6.6_

  - [ ]* 10.7 编写属性测试验证变更检测准确性
    - **属性 10: 变更检测准确性**
    - **验证需求: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6**
    - 使用 jqwik 生成随机 OpenAPI 规范对，验证所有变更被正确识别且报告包含时间戳

- [x] 11. 实现访问控制和审计功能
  - [x] 11.1 创建 Swagger 访问控制过滤器
    - 在 `zhitu-gateway/src/main/java/com/zhitu/gateway/filter/SwaggerAccessFilter.java` 创建过滤器
    - 实现 `GlobalFilter` 和 `Ordered` 接口
    - 实现 `filter()` 方法，检查请求路径是否为 Swagger 路径
    - 实现 `isAuthorized()` 方法，验证 JWT token 和用户角色
    - _需求: 7.4, 7.5, 7.6_

  - [x] 11.2 实现审计日志记录
    - 在过滤器中记录所有 Swagger UI 访问
    - 记录用户身份、访问时间、访问路径
    - 记录未授权访问尝试
    - _需求: 7.7_

  - [x] 11.3 配置环境级别的访问控制
    - 在 `application-dev.yml` 中配置 `springdoc.swagger-ui.enabled: true`
    - 在 `application-test.yml` 中配置 `springdoc.swagger-ui.enabled: true`
    - 在 `application-prod.yml` 中配置 `springdoc.swagger-ui.enabled: false`
    - 配置 `swagger.access-control.enabled` 和 `allowed-roles`
    - _需求: 7.1, 7.2, 7.3, 7.5_

  - [ ]* 11.4 为访问控制过滤器编写单元测试
    - 测试有效 token 的访问授权
    - 测试无效 token 的访问拒绝
    - 测试角色权限验证
    - 测试审计日志记录
    - _需求: 7.4, 7.6, 7.7_

  - [ ]* 11.5 编写属性测试验证环境访问控制
    - **属性 11: 环境访问控制**
    - **验证需求: 7.1, 7.2, 7.3, 7.4, 7.6, 7.7**
    - 使用 jqwik 生成随机环境配置和访问请求，验证访问控制规则正确应用


- [x] 12. 实现国际化支持
  - [x] 12.1 创建国际化消息管理器
    - 在 `zhitu-common/zhitu-common-core/src/main/java/com/zhitu/common/i18n/ApiMessageSource.java` 创建组件
    - 实现 `getMessage()` 方法，支持 Locale 参数
    - 实现回退机制，缺少翻译时回退到中文
    - _需求: 10.1, 10.5, 10.7_

  - [x] 12.2 创建中文消息资源文件
    - 在 `zhitu-common/zhitu-common-core/src/main/resources/i18n/api-messages_zh_CN.properties` 创建文件
    - 定义所有 API 描述、参数说明、错误消息的中文翻译
    - _需求: 10.1, 10.3, 10.4_

  - [x] 12.3 创建英文消息资源文件
    - 在 `zhitu-common/zhitu-common-core/src/main/resources/i18n/api-messages_en_US.properties` 创建文件
    - 定义所有 API 描述、参数说明、错误消息的英文翻译
    - _需求: 10.1, 10.3, 10.4_

  - [x] 12.4 更新 Swagger 注解使用 i18n 消息键
    - 修改各 Controller 的 @Tag、@Operation、@Parameter 注解
    - 将硬编码文本替换为消息键（如 `{api.auth.login.description}`）
    - _需求: 10.6_

  - [x] 12.5 配置 Swagger UI 语言切换
    - 在 Gateway 的 OpenAPI 配置中添加语言切换支持
    - 配置 `springdoc.swagger-ui.locale` 参数
    - _需求: 10.3_

  - [ ]* 12.6 为国际化功能编写单元测试
    - 测试中文消息加载
    - 测试英文消息加载
    - 测试回退机制
    - 测试消息键解析
    - _需求: 10.1, 10.5, 10.7_

  - [ ]* 12.7 编写属性测试验证国际化支持
    - **属性 14: 国际化支持**
    - **验证需求: 10.1, 10.2, 10.3, 10.4, 10.5, 10.6, 10.7**
    - 使用 jqwik 生成随机消息键和 Locale，验证双语支持和回退机制

- [x] 13. 生成文档和创建目录结构
  - [x] 13.1 创建文档目录结构
    - 创建 `docs/api` 主目录
    - 为每个微服务创建子目录：`docs/api/zhitu-auth`、`docs/api/zhitu-system` 等
    - 创建 `docs/api/openapi` 目录存放 OpenAPI 规范文件
    - _需求: 1.2, 1.6_

  - [x] 13.2 为每个微服务创建 README 文件
    - 在每个服务的文档目录中创建 README.md
    - 说明该模块的功能范围、主要接口、认证要求
    - _需求: 1.7_

  - [x] 13.3 执行 Maven 构建生成文档
    - 运行 `mvn clean install` 触发文档生成插件
    - 验证 OpenAPI JSON 文件生成到正确位置
    - 验证注解验证器输出警告信息
    - _需求: 8.1, 8.2, 8.3, 8.4, 8.5_

  - [x] 13.4 生成 Markdown 格式文档
    - 使用工具将 OpenAPI JSON 转换为 Markdown
    - 确保文档包含接口概述、请求/响应示例、参数标注、错误码列表
    - _需求: 2.1, 2.2, 2.3, 2.4_

  - [x] 13.5 创建变更历史文档
    - 创建 `docs/api/CHANGELOG.md` 文件
    - 记录初始版本的接口列表
    - _需求: 2.6, 6.7_

  - [ ]* 13.6 编写属性测试验证文档生成完整性
    - **属性 1: 文档生成完整性**
    - **验证需求: 1.1, 1.3, 1.4, 1.5**
    - 使用 jqwik 生成随机服务列表，验证每个服务生成独立文档且包含所有必需元素

  - [ ]* 13.7 编写属性测试验证目录结构一致性
    - **属性 2: 文档目录结构一致性**
    - **验证需求: 1.2, 1.6, 1.7**
    - 使用 jqwik 生成随机服务列表，验证文档按模块组织且每个目录包含 README

- [x] 14. 添加接口测试数据和示例
  - [x] 14.1 在 Swagger 注解中添加请求示例
    - 为每个 @Operation 注解添加 `requestBody` 示例
    - 使用 `@io.swagger.v3.oas.annotations.parameters.RequestBody` 的 `content.examples` 属性
    - _需求: 9.1_

  - [x] 14.2 在 Swagger 注解中添加响应示例
    - 为每个 @ApiResponse 注解添加成功响应示例
    - 添加常见错误场景示例（400、401、403、404、500）
    - _需求: 9.2, 9.3_

  - [x] 14.3 添加边界值和特殊字符测试用例
    - 在注解示例中包含边界值（最小值、最大值、空值）
    - 包含特殊字符测试用例
    - _需求: 9.4_

  - [x] 14.4 添加测试数据前置条件说明
    - 在 @Operation 注解的 description 中说明前置条件
    - 说明数据依赖关系
    - _需求: 9.5_

  - [x] 14.5 配置 Swagger UI 预填充示例数据
    - 在 OpenAPI 配置中启用 `tryItOutEnabled`
    - 配置默认示例数据
    - _需求: 9.6_

  - [ ]* 14.6 编写属性测试验证测试数据完整性
    - **属性 13: 测试数据完整性**
    - **验证需求: 9.1, 9.2, 9.3, 9.4, 9.5, 9.6**
    - 使用 jqwik 生成随机 API 端点，验证每个端点包含请求示例、响应示例、错误示例、边界值测试用例

- [x] 15. 检查点 - 完整功能验证
  - 启动所有微服务和 Gateway
  - 访问 Gateway 的 Swagger UI (`http://gateway:port/swagger-ui.html`)
  - 验证可以切换不同服务的文档分组
  - 验证可以在 Swagger UI 中测试各服务 API
  - 验证访问控制在不同环境正确应用
  - 验证文档支持中英文切换
  - 验证文档目录结构和文件完整性
  - 如有问题请咨询用户

- [ ] 16. 集成测试和文档验证
  - [ ]* 16.1 编写 Gateway 聚合集成测试
    - 启动多个微服务实例
    - 验证 Gateway 能发现并聚合所有服务文档
    - 验证聚合后的 Swagger UI 可访问
    - 验证可以通过 Gateway 测试各服务 API
    - _需求: 5.1, 5.2, 5.3, 5.6_

  - [ ]* 16.2 编写文档生成流程集成测试
    - 执行完整的 Maven 构建
    - 验证文档文件生成到正确位置
    - 验证 OpenAPI 规范文件格式正确
    - 验证变更报告生成
    - _需求: 8.1, 8.4, 8.5, 8.6, 6.7_

  - [ ]* 16.3 编写访问控制集成测试
    - 配置不同环境（dev、test、prod）
    - 验证访问控制规则在各环境正确应用
    - 验证审计日志正确记录
    - _需求: 7.1, 7.2, 7.3, 7.4, 7.6, 7.7_

  - [ ]* 16.4 编写属性测试验证 Swagger UI 可访问性
    - **属性 6: Swagger UI 可访问性**
    - **验证需求: 3.4, 3.5**
    - 使用 jqwik 生成随机微服务配置，验证 Swagger UI 端点返回成功响应且显示所有 API 端点

  - [ ]* 16.5 编写属性测试验证 Controller 文档覆盖率
    - **属性 4: Controller 文档覆盖率**
    - **验证需求: 2.7**
    - 使用 jqwik 扫描所有 Controller 类，验证每个公共方法在文档中有对应条目

  - [ ]* 16.6 编写属性测试验证文档格式标准化
    - **属性 3: 文档格式标准化**
    - **验证需求: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6**
    - 使用 jqwik 生成随机 API 规范文档，验证文档使用 Markdown 格式且包含所有必需部分

  - [ ]* 16.7 编写属性测试验证依赖配置一致性
    - **属性 5: 依赖配置一致性**
    - **验证需求: 3.1**
    - 使用 jqwik 扫描所有微服务 pom.xml，验证包含 springdoc-openapi-starter-webmvc-ui 依赖

  - [ ]* 16.8 编写属性测试验证错误响应文档化
    - **属性 9: 错误响应文档化**
    - **验证需求: 4.6, 4.7**
    - 使用 jqwik 扫描所有 Controller 方法，验证可能返回错误的方法有 @ApiResponse 注解且包含示例值

  - [ ]* 16.9 编写属性测试验证构建时文档生成
    - **属性 12: 构建时文档生成**
    - **验证需求: 8.2, 8.3, 8.4, 8.5**
    - 使用 jqwik 模拟构建过程，验证 Maven 插件生成 OpenAPI 文件、验证注解完整性并输出警告

## 注意事项

- 标记 `*` 的任务为可选任务，可以跳过以加快 MVP 交付
- 每个任务都引用了具体的需求编号，确保可追溯性
- 检查点任务确保增量验证，及早发现问题
- 属性测试验证通用正确性属性，每个测试运行至少 100 次迭代
- 单元测试验证特定示例和边界情况
- 集成测试验证组件之间的交互

## 技术栈

- Spring Boot 3.2.5
- SpringDoc OpenAPI 2.5.0
- Spring Cloud Gateway
- Maven 3.x
- jqwik 1.8.2（属性测试）
- JUnit 5（单元测试）
