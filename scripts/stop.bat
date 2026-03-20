@echo off
chcp 65001 >nul
echo ==========================================
echo Zhitu Cloud - Stop All Services
echo ==========================================

cd /d "%~dp0..\docker"

echo [INFO] Stopping all services...
docker-compose down

echo.
echo [SUCCESS] All services stopped
echo.
echo [Tips]
echo   Restart: cd scripts ^&^& start.bat
echo   Remove data: cd docker ^&^& docker-compose down -v
echo ==========================================
echo.
pause
