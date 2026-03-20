@echo off
chcp 65001 >nul
cd /d "%~dp0..\.."

echo ========================================
echo 智途云平台 - 启动完整开发环境
echo ========================================
echo.

REM 检查Docker
echo [1/4] 检查Docker环境...
docker info >nul 2>&1
if errorlevel 1 (
    echo [错误] Docker未运行，请先启动Docker Desktop
    pause
    exit /b 1
)
echo [✓] Docker运行正常
echo.

REM 启动基础设施
echo [2/4] 启动基础设施服务...
cd docker
docker-compose -f docker-compose.dev.yml up -d
cd ..
echo [✓] 基础设施服务启动成功
echo.

echo [3/4] 等待服务就绪 (约30秒)...
timeout /t 30 /nobreak >nul
echo.

REM 启动后端各微服务
echo [4/4] 启动各微服务...
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

cd ..
echo.
echo [✓] 所有微服务启动中...
echo.

timeout /t 20 /nobreak >nul

REM 启动前端
echo 启动前端服务...
cd frontend
start "Zhitu Frontend" cmd /k "pnpm run dev"
cd ..
echo [✓] 前端服务启动中...
echo.

echo ========================================
echo 开发环境启动完成！
echo ========================================
echo.
echo 服务地址:
echo   - 前端: http://localhost:5173
echo   - 后端网关: http://localhost:8888
echo   - Nacos: http://localhost:8848/nacos
echo   - MinIO: http://localhost:9001
echo.
pause
