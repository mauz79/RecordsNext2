@echo off
setlocal EnableExtensions EnableDelayedExpansion
cd /d "%~dp0"
if not exist "%~dp0RecordsNext.jar" goto jar_missing
if not exist "%~dp0runtime\ucanaccess\ucanaccess-2.0.9.5.jar" goto ucan_missing
where javaw >nul 2>&1
if errorlevel 1 goto java_missing
set "JAVA_VERSION="
for /f "tokens=3" %%V in ('java -version 2^>^&1 ^| findstr /i /c:"version"') do if not defined JAVA_VERSION set "JAVA_VERSION=%%~V"
if not defined JAVA_VERSION goto java_missing
for /f "tokens=1 delims=." %%M in ("!JAVA_VERSION!") do set "JAVA_MAJOR=%%M"
set /a JAVA_NUMBER=!JAVA_MAJOR! 2>nul
if !JAVA_NUMBER! LSS 21 goto java_old
set "RN_CP=%~dp0RecordsNext.jar;%~dp0runtime\ucanaccess\*;%~dp0runtime\ucanaccess\lib\*"
start "RecordsNext 4.0" /D "%~dp0" javaw -cp "%RN_CP%" it.alterlega.recordsnext.gui.RecordsNext2Dashboard
exit /b 0
:jar_missing
echo ERRORE: RecordsNext.jar mancante.
goto failed
:ucan_missing
echo ERRORE: runtime UCanAccess mancante.
goto failed
:java_old
echo ERRORE: serve Java 21 o superiore. Installato: !JAVA_VERSION!
goto failed
:java_missing
echo ERRORE: Java 21 o superiore non trovato nel PATH.
:failed
pause
exit /b 1
