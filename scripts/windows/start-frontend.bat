@echo off
chcp 65001 >nul
cd /d "%~dp0..\.."

echo ========================================
echo 智途云平台 - 启动前端服务
echo ========================================
echo.

REM 检查依赖
echo [1/2] 检查依赖...
if not exist "frontend\node_modules" (
    echo [提示] 未找到node_modules，正在安装依赖...
    cd frontend
    call pnpm install
    cd ..
)
echo [✓] 依赖检查完成
echo.

REM 启动前端
echo [2/2] 启动前端服务...
cd frontend
start "Zhitu Frontend" cmd /k "pnpm run dev"
cd ..
echo [✓] 前端服务启动中...
echo.

echo ========================================
echo 前端服务启动完成！
echo ========================================
echo.
pause
