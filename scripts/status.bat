@echo off
chcp 65001 >nul
echo ==========================================
echo Zhitu Cloud - Service Status
echo ==========================================
echo.

cd /d "%~dp0..\docker"

echo [Service Status]
echo.
docker-compose ps

echo.
echo ==========================================
echo [Common Commands]
echo   View logs:    cd docker ^&^& docker-compose logs -f [service]
echo   Restart:      cd docker ^&^& docker-compose restart [service]
echo   Stop service: cd docker ^&^& docker-compose stop [service]
echo ==========================================
echo.
pause
