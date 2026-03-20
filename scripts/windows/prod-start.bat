@echo off
chcp 65001 >nul
cd /d "%~dp0..\.."

echo ========================================
echo 智途云平台 - 启动生产环境
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

echo 检查镜像是否已构建...
docker images | findstr "zhitu" >nul 2>&1
if errorlevel 1 (
    echo [提示] 未找到镜像，开始构建...
    cd docker
    docker-compose build
    cd ..
)
echo.

echo 启动所有服务...
cd docker
docker-compose up -d
if errorlevel 1 (
    echo [错误] 服务启动失败
    cd ..
    pause
    exit /b 1
)
cd ..
echo.
echo [✓] 生产环境启动成功
echo.
echo ========================================
echo 服务地址:
echo   - 前端: http://localhost:80
echo   - 后端网关: http://localhost:8888
echo   - Nacos: http://localhost:8848/nacos
echo   - MinIO: http://localhost:9001
echo ========================================
echo.
pause
