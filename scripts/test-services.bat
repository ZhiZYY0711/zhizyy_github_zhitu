@echo off
chcp 65001 >nul
echo ==========================================
echo Zhitu Cloud - Health Check
echo ==========================================
echo.

echo [INFO] Checking service status...
echo.

echo [Infrastructure Services]
call :check_service "Nacos      " "http://localhost:8848/nacos"
call :check_service "MinIO      " "http://localhost:9001"
echo.

echo [Backend Microservices]
call :check_service "Gateway    " "http://localhost:8888/actuator/health"
call :check_service "Auth       " "http://localhost:9200/actuator/health"
call :check_service "System     " "http://localhost:9201/actuator/health"
call :check_service "College    " "http://localhost:9202/actuator/health"
call :check_service "Enterprise " "http://localhost:9203/actuator/health"
call :check_service "Student    " "http://localhost:9204/actuator/health"
call :check_service "Platform   " "http://localhost:9205/actuator/health"
echo.

echo [Frontend Service]
call :check_service "Frontend   " "http://localhost"
echo.

echo ==========================================
echo [SUCCESS] Health check completed
echo ==========================================
echo.
pause
exit /b

:check_service
echo | set /p="Checking %~1 ... "
curl -s -f -o nul %~2
if %errorlevel% equ 0 (
    echo [OK]
) else (
    echo [FAIL]
)
exit /b
