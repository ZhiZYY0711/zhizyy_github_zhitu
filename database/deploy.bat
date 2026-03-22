@echo off
REM 智途平台数据库部署脚本 (Windows)
REM 用法: deploy.bat [init|upgrade|verify]

setlocal enabledelayedexpansion

REM 配置
set DB_USER=zhitu_user
set DB_NAME=zhitu_cloud
set DB_HOST=localhost
set PSQL=psql

REM 检查参数
if "%1"=="" (
    call :show_help
    exit /b 1
)

REM 执行命令
if /i "%1"=="init" (
    call :check_connection && call :init_database && call :upgrade_database
) else if /i "%1"=="upgrade" (
    call :check_connection && call :upgrade_database
) else if /i "%1"=="verify" (
    call :check_connection && call :verify_database
) else if /i "%1"=="help" (
    call :show_help
) else (
    echo [ERROR] 未知命令: %1
    call :show_help
    exit /b 1
)

exit /b 0

REM ==================== 函数定义 ====================

:check_connection
echo [INFO] 检查数据库连接...
%PSQL% -U %DB_USER% -d %DB_NAME% -h %DB_HOST% -c "SELECT 1" >nul 2>&1
if errorlevel 1 (
    echo [ERROR] 数据库连接失败，请检查配置
    exit /b 1
)
echo [INFO] 数据库连接成功
exit /b 0

:init_database
echo [INFO] 开始初始化数据库...
cd v1.0.0-init

REM 执行schema文件
for %%f in (0*.sql) do (
    echo [INFO] 执行: %%f
    %PSQL% -U %DB_USER% -d %DB_NAME% -h %DB_HOST% -f %%f
    if errorlevel 1 (
        echo [ERROR] 执行失败: %%f
        cd ..
        exit /b 1
    )
)

REM 询问是否导入测试数据
set /p IMPORT_DATA="是否导入测试数据? (y/n): "
if /i "%IMPORT_DATA%"=="y" (
    echo [INFO] 导入测试数据...
    %PSQL% -U %DB_USER% -d %DB_NAME% -h %DB_HOST% -f test_data.sql
)

cd ..
echo [INFO] 数据库初始化完成
exit /b 0

:upgrade_database
echo [INFO] 开始升级数据库...

echo [INFO] 应用 v1.1.0 迁移...
%PSQL% -U %DB_USER% -d %DB_NAME% -h %DB_HOST% -f v1.1.0-missing-api/upgrade.sql
if errorlevel 1 exit /b 1

echo [INFO] 应用 v1.2.0 迁移...
%PSQL% -U %DB_USER% -d %DB_NAME% -h %DB_HOST% -f v1.2.0-talent-pool/upgrade.sql
if errorlevel 1 exit /b 1

echo [INFO] 应用 v1.3.0 迁移...
%PSQL% -U %DB_USER% -d %DB_NAME% -h %DB_HOST% -f v1.3.0-growth-fix/upgrade.sql
if errorlevel 1 exit /b 1

echo [INFO] 数据库升级完成
exit /b 0

:verify_database
echo [INFO] 开始验证数据库...

echo [INFO] 检查schema...
%PSQL% -U %DB_USER% -d %DB_NAME% -h %DB_HOST% -c "SELECT schema_name FROM information_schema.schemata WHERE schema_name LIKE '%%svc' OR schema_name IN ('auth_center', 'platform_service');"

echo [INFO] 检查表数量...
%PSQL% -U %DB_USER% -d %DB_NAME% -h %DB_HOST% -c "SELECT table_schema, COUNT(*) as table_count FROM information_schema.tables WHERE table_schema IN ('auth_center', 'platform_service', 'student_svc', 'college_svc', 'enterprise_svc', 'internship_svc', 'training_svc', 'growth_svc') GROUP BY table_schema;"

echo [INFO] 执行v1.3.0验证...
%PSQL% -U %DB_USER% -d %DB_NAME% -h %DB_HOST% -f v1.3.0-growth-fix/verify.sql

echo [INFO] 数据库验证完成
exit /b 0

:show_help
echo 智途平台数据库部署脚本 (Windows)
echo.
echo 用法: %~nx0 [命令]
echo.
echo 命令:
echo   init      - 初始化数据库（全新安装）
echo   upgrade   - 升级数据库（应用所有迁移）
echo   verify    - 验证数据库状态
echo   help      - 显示此帮助信息
echo.
echo 示例:
echo   %~nx0 init      # 全新安装
echo   %~nx0 upgrade   # 升级到最新版本
echo   %~nx0 verify    # 验证数据库
exit /b 0
