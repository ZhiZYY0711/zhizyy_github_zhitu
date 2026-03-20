@echo off
chcp 65001 >nul
cd /d "%~dp0..\.."

echo ========================================
echo 智途云平台 - 启动后端服务
echo ========================================
echo.

echo [1/3] 检查Docker环境...
docker info >nul 2>&1
if errorlevel 1 (
    echo [错误] Docker未运行，请先启动Docker Desktop
    pause
    exit /b 1
)
echo [✓] Docker运行正常
echo.

echo [2/3] 启动基础设施服务...
cd docker
docker-compose -f docker-compose.dev.yml up -d
cd ..
echo [✓] 基础设施服务启动成功
echo.

echo 等待服务就绪 (约30秒)...
timeout /t 30 /nobreak >nul
echo.

echo [3/3] 启动各微服务...
echo.

echo 启动 Auth 服务 (8081)...
start "Zhitu Auth" cmd /k "cd /d %cd%\backend\zhitu-auth && mvn spring-boot:run -Dspring-boot.run.profiles=dev"

timeout /t 5 /nobreak >nul

echo 启动 Gateway 服务 (8080)...
start "Zhitu Gateway" cmd /k "cd /d %cd%\backend\zhitu-gateway && mvn spring-boot:run -Dspring-boot.run.profiles=dev"

echo 启动 System 服务 (8082)...
start "Zhitu System" cmd /k "cd /d %cd%\backend\zhitu-modules\zhitu-system && mvn spring-boot:run -Dspring-boot.run.profiles=dev"

echo 启动 College 服务 (8083)...
start "Zhitu College" cmd /k "cd /d %cd%\backend\zhitu-modules\zhitu-college && mvn spring-boot:run -Dspring-boot.run.profiles=dev"

echo 启动 Enterprise 服务 (8084)...
start "Zhitu Enterprise" cmd /k "cd /d %cd%\backend\zhitu-modules\zhitu-enterprise && mvn spring-boot:run -Dspring-boot.run.profiles=dev"

echo 启动 Student 服务 (8085)...
start "Zhitu Student" cmd /k "cd /d %cd%\backend\zhitu-modules\zhitu-student && mvn spring-boot:run -Dspring-boot.run.profiles=dev"

echo 启动 Platform 服务 (8086)...
start "Zhitu Platform" cmd /k "cd /d %cd%\backend\zhitu-modules\zhitu-platform && mvn spring-boot:run -Dspring-boot.run.profiles=dev"

echo.
echo [✓] 所有微服务启动中...
echo.
echo ========================================
echo 服务端口:
echo   - Gateway:    http://localhost:8888
echo   - Auth:       http://localhost:8081
echo   - System:     http://localhost:8082
echo   - College:    http://localhost:8093
echo   - Enterprise: http://localhost:8084
echo   - Student:    http://localhost:8085
echo   - Platform:   http://localhost:8086
echo ========================================
echo.
pause
