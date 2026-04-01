@echo off
chcp 65001 >nul
title 时间银行项目 - 一键启动所有服务

echo ======================================
echo   时间银行项目 - 服务启动脚本
echo ======================================
echo.

echo [1/4] 正在启动MySQL MCP工具...
start "MySQL MCP Server" cmd /k "cd C:\Users\CaTeam\MySQL_MCP && npm start"
echo ✓ MySQL MCP工具启动中...
echo.

echo [2/4] 正在启动后端API服务...
start "Backend API" cmd /k "cd c:\Users\CaTeam\Desktop\ai project\ai-end && mvn spring-boot:run"
echo ✓ 后端API服务启动中...
echo.

echo 等待后端服务启动（10秒）...
timeout /t 10 /nobreak >nul
echo.

echo [3/4] 正在启动用户端前端...
start "Frontend - TimeBank Family" cmd /k "cd c:\Users\CaTeam\Desktop\ai project\timebank-family && npm run dev"
echo ✓ 用户端前端启动中...
echo.

echo [4/4] 正在启动管理后台...
start "Admin Dashboard" cmd /k "cd c:\Users\CaTeam\Desktop\ai project\时间银行后台 && npm run dev"
echo ✓ 管理后台启动中...
echo.

echo ======================================
echo   所有服务已启动完成！
echo ======================================
echo.
echo 服务访问地址：
echo   - 用户端前端: http://localhost:3000
echo   - 管理后台:   http://localhost:3002
echo   - 后端API:    http://localhost:8080
echo.
echo 提示：
echo   - 浏览器将自动打开前端页面
echo   - 各服务运行在独立的终端窗口中
echo   - 关闭对应窗口或按Ctrl+C可停止服务
echo.
echo 按任意键关闭此窗口...
pause >nul
