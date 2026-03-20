@echo off
chcp 65001 >nul
cd /d "%~dp0..\.."

echo ========================================
echo 智途云平台 - 停止所有服务
echo ========================================
echo.

echo [1/3] 停止前端服务...
taskkill /FI "WINDOWTITLE eq Zhitu Frontend*" /F >nul 2>&1
echo [✓] 前端服务已停止
echo.

echo [2/3] 停止后端微服务...
taskkill /FI "WINDOWTITLE eq Zhitu Auth*" /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq Zhitu Gateway*" /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq Zhitu System*" /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq Zhitu College*" /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq Zhitu Enterprise*" /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq Zhitu Student*" /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq Zhitu Platform*" /F >nul 2>&1
echo [✓] 后端服务已停止
echo.

echo [3/3] 停止基础设施服务...
cd docker
docker-compose -f docker-compose.dev.yml down
cd ..
echo [✓] 基础设施服务已停止
echo.

echo ========================================
echo 所有服务已停止
echo ========================================
echo.
pause
