@echo off
chcp 65001 >nul
echo ==========================================
echo Zhitu Cloud - Development Mode
echo ==========================================
echo.
echo [INFO] Starting infrastructure services only
echo        Backend and frontend should run in IDE
echo.

REM Check Docker
docker info >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker is not running
    pause
    exit /b 1
)

echo [OK] Docker environment check passed
echo.

REM Navigate to docker directory
cd /d "%~dp0..\docker"

echo [INFO] Starting infrastructure services...
docker-compose -f docker-compose.dev.yml up -d

echo.
echo ==========================================
echo [SUCCESS] Infrastructure services started!
echo ==========================================
echo.
echo [Service Addresses]
echo   PostgreSQL:      localhost:15432
echo   Redis:           localhost:6379
echo   Nacos Console:   http://localhost:8848/nacos
echo   MinIO Console:   http://localhost:9001
echo.
echo [Default Credentials]
echo   Nacos:  nacos / nacos
echo   MinIO:  minioadmin / minioadmin
echo.
echo [Next Steps]
echo   1. Start backend services in IDE
echo   2. Run 'cd frontend ^&^& npm run dev' to start frontend
echo.
echo [Stop] cd docker ^&^& docker-compose -f docker-compose.dev.yml down
echo ==========================================
echo.
pause
