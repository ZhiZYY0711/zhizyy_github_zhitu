@echo off
chcp 65001 >nul
echo ==========================================
echo Docker Mirror Setup Guide
echo ==========================================
echo.
echo [ISSUE] Cannot pull Docker images from Docker Hub
echo [SOLUTION] Configure Docker registry mirrors
echo.
echo ==========================================
echo Step 1: Open Docker Desktop
echo ==========================================
echo   1. Right-click Docker Desktop icon in system tray
echo   2. Click "Settings" or "Preferences"
echo.
pause
echo.
echo ==========================================
echo Step 2: Configure Docker Engine
echo ==========================================
echo   1. Click "Docker Engine" in left sidebar
echo   2. Add the following configuration:
echo.
echo   {
echo     "registry-mirrors": [
echo       "https://docker.mirrors.ustc.edu.cn",
echo       "https://hub-mirror.c.163.com",
echo       "https://mirror.ccs.tencentyun.com"
echo     ]
echo   }
echo.
echo   3. Click "Apply & Restart"
echo.
pause
echo.
echo ==========================================
echo Step 3: Verify Configuration
echo ==========================================
echo   Run: docker info
echo   Look for "Registry Mirrors" section
echo.
echo ==========================================
echo Alternative: Use Aliyun Mirror
echo ==========================================
echo   1. Visit: https://cr.console.aliyun.com/cn-hangzhou/instances/mirrors
echo   2. Login with Aliyun account
echo   3. Get your personal mirror address
echo   4. Add to Docker Engine configuration
echo.
echo ==========================================
echo [NOTE] After configuration, restart the script:
echo   cd scripts
echo   start.bat
echo ==========================================
echo.
pause
