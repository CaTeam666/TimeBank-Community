@echo off
setlocal
cd /d "%~dp0"
start "TimeBank Backend API :8080" powershell.exe -NoExit -ExecutionPolicy Bypass -File "%~dp0start-backend.ps1"
timeout /t 2 >nul
start "TimeBank Family Web :3000" powershell.exe -NoExit -ExecutionPolicy Bypass -File "%~dp0start-family.ps1" %*
start "TimeBank Admin Web :3002" powershell.exe -NoExit -ExecutionPolicy Bypass -File "%~dp0start-admin.ps1" %*
