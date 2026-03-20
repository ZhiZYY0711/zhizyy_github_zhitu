@echo off
chcp 65001 >nul
cd /d "%~dp0..\.."

echo ========================================
echo 智途云平台 - 停止后端服务
echo ========================================
echo.

echo 停止所有微服务窗口...
taskkill /FI "WINDOWTITLE eq Zhitu Auth*" /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq Zhitu Gateway*" /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq Zhitu System*" /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq Zhitu College*" /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq Zhitu Enterprise*" /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq Zhitu Student*" /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq Zhitu Platform*" /F >nul 2>&1
echo [✓] 后端服务已停止
echo.
pause
