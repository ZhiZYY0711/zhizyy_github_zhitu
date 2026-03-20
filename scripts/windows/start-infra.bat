@echo off
chcp 65001 >nul
cd /d "%~dp0..\.."

echo ========================================
echo 智途云平台 - 启动基础设施服务
echo ========================================
echo.

REM 检查Docker
echo [1/2] 检查Docker环境...
docker info >nul 2>&1
if errorlevel 1 (
    echo [错误] Docker未运行，请先启动Docker Desktop
    pause
    exit /b 1
)
echo [✓] Docker运行正常
echo.

REM 启动基础设施
echo [2/2] 启动基础设施服务...
cd docker
docker-compose -f docker-compose.dev.yml up -d
cd ..
echo [✓] 基础设施服务启动成功
echo.

echo ========================================
echo 基础设施服务启动完成！
echo ========================================
echo.
echo 服务地址:
echo   - PostgreSQL: localhost:15432
echo   - Redis: localhost:6379
echo   - Nacos: http://localhost:8848/nacos
echo   - MinIO: http://localhost:9001
echo.
pause
