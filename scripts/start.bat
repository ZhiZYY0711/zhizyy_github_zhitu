@echo off
chcp 65001 >nul
:menu
cls
echo ========================================
echo 智途云平台 - 启动服务
echo ========================================
echo.
echo 【开发环境】(基础设施Docker + 宿主机前后端)
echo   1. 完整开发环境 (基础设施 + 后端 + 前端)
echo   2. 仅基础设施服务
echo   3. 仅后端服务 (含基础设施)
echo   4. 仅前端服务
echo.
echo 【生产环境】(全部Docker镜像)
echo   5. 启动生产环境
echo   6. 构建生产镜像 (重新构建所有镜像)
echo.
echo   0. 退出
echo.
echo ========================================
set /p choice=请输入选项 (0-6): 

if "%choice%"=="1" ( cls & call windows\start-all.bat & goto menu )
if "%choice%"=="2" ( cls & call windows\start-infra.bat & goto menu )
if "%choice%"=="3" ( cls & call windows\start-backend.bat & goto menu )
if "%choice%"=="4" ( cls & call windows\start-frontend.bat & goto menu )
if "%choice%"=="5" ( cls & call windows\prod-start.bat & goto menu )
if "%choice%"=="6" ( cls & call windows\prod-build.bat & goto menu )
if "%choice%"=="0" goto end

echo 无效选项，请重新选择
timeout /t 2 >nul
goto menu

:end
exit
