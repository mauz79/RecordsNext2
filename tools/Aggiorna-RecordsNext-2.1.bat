@echo off
setlocal
cd /d "%~dp0"

powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0Aggiorna-RecordsNext-2.1.ps1"

echo.
pause
endlocal