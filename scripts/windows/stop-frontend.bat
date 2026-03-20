@echo off
chcp 65001 >nul
cd /d "%~dp0..\.."

echo ========================================
echo 智途云平台 - 停止前端服务
echo ========================================
echo.

echo 停止前端服务...
taskkill /FI "WINDOWTITLE eq Zhitu Frontend*" /F >nul 2>&1
echo [✓] 前端服务已停止
echo.
pause
