@echo off
chcp 65001 >nul
echo ==========================================
echo Zhitu Cloud Platform - Start All Services
echo ==========================================

REM Check if Docker is running
docker info >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker is not running. Please start Docker Desktop first.
    pause
    exit /b 1
)

REM Check Docker Compose
docker-compose version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker Compose is not available.
    pause
    exit /b 1
)

echo [OK] Docker environment check passed
echo.

REM Navigate to docker directory
cd /d "%~dp0..\docker"

echo [INFO] Building and starting all services...
echo [INFO] This may take 5-10 minutes on first run...
echo.

REM Build and start all services
docker-compose up -d --build

echo.
echo ==========================================
echo [SUCCESS] All services started!
echo ==========================================
echo.
echo [服务访问地址]
echo   Frontend:        http://localhost
echo   API Gateway:     http://localhost:8888
echo   Nacos Console:   http://localhost:8848/nacos
echo   MinIO Console:   http://localhost:9001
echo.
echo [Default Credentials]
echo   Nacos:  nacos / nacos
echo   MinIO:  minioadmin / minioadmin
echo.
echo [Common Commands]
echo   View status: cd docker ^&^& docker-compose ps
echo   View logs:   cd docker ^&^& docker-compose logs -f [service]
echo   Stop all:    cd docker ^&^& docker-compose down
echo.
echo [Note] First startup needs 5-10 minutes to build images
echo        Backend services will register to Nacos after it starts
echo ==========================================
echo.
pause
