@echo off
chcp 65001 >nul
:menu
cls
echo ========================================
echo 智途云平台 - 停止服务
echo ========================================
echo.
echo 【开发环境】
echo   1. 停止所有开发服务
echo   2. 仅停止基础设施
echo   3. 仅停止后端
echo   4. 仅停止前端
echo.
echo 【生产环境】
echo   5. 停止生产环境
echo.
echo   6. 查看服务状态
echo   0. 退出
echo.
echo ========================================
set /p choice=请输入选项 (0-6): 

if "%choice%"=="1" ( cls & call windows\stop-all.bat & goto menu )
if "%choice%"=="2" ( cls & call windows\stop-infra.bat & goto menu )
if "%choice%"=="3" ( cls & call windows\stop-backend.bat & goto menu )
if "%choice%"=="4" ( cls & call windows\stop-frontend.bat & goto menu )
if "%choice%"=="5" ( cls & call windows\prod-stop.bat & goto menu )
if "%choice%"=="6" (
    cls
    echo ========================================
    echo 服务状态
    echo ========================================
    echo.
    docker ps --filter "name=zhitu-" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
    echo.
    pause
    goto menu
)
if "%choice%"=="0" goto end

echo 无效选项，请重新选择
timeout /t 2 >nul
goto menu

:end
exit
