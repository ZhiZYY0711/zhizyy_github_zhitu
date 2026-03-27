@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ========================================
echo 智途平台 API文档获取工具
echo ========================================
echo.

set "GATEWAY=http://localhost:8888"
set "DOCS_DIR=..\docs\api"

:: 定义服务列表和端口
set "services[0].name=zhitu-auth"
set "services[0].port=8081"
set "services[1].name=zhitu-system"
set "services[1].port=8082"
set "services[2].name=zhitu-enterprise"
set "services[2].port=8084"
set "services[3].name=zhitu-student"
set "services[3].port=8085"
set "services[4].name=zhitu-platform"
set "services[4].port=8086"
set "services[5].name=zhitu-college"
set "services[5].port=8093"

:: 遍历服务列表
for /L %%i in (0,1,5) do (
    set "name=!services[%%i].name!"
    set "port=!services[%%i].port!"
    
    if defined name (
        echo [%%i/5] 正在获取 !name! 服务的API文档...
        
        :: 创建目录
        if not exist "%DOCS_DIR%\!name!" mkdir "%DOCS_DIR%\!name!"
        
        :: 获取API文档
        curl -s -o "%DOCS_DIR%\!name!\!name!.json" "http://localhost:!port!/v3/api-docs"
        
        if !errorlevel! equ 0 (
            echo [成功] !name!.json 已保存到 %DOCS_DIR%\!name!\
        ) else (
            echo [失败] 无法获取 !name! 的API文档，请检查服务是否启动
        )
        echo.
    )
)

echo ========================================
echo API文档获取完成！
echo 文档保存位置: %DOCS_DIR%
echo ========================================
pause
