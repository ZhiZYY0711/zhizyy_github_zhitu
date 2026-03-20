@echo off
chcp 65001 >nul
cd /d "%~dp0..\.."

echo ========================================
echo 智途云平台 - 构建生产镜像
echo ========================================
echo.

echo 检查Docker环境...
docker info >nul 2>&1
if errorlevel 1 (
    echo [错误] Docker未运行，请先启动Docker Desktop
    pause
    exit /b 1
)
echo [✓] Docker运行正常
echo.

echo 开始构建所有镜像 (首次构建耗时较长)...
cd docker
docker-compose build --no-cache
if errorlevel 1 (
    echo [错误] 镜像构建失败，请检查Dockerfile和网络连接
    cd ..
    pause
    exit /b 1
)
cd ..
echo.
echo [✓] 所有镜像构建完成
echo.
pause
