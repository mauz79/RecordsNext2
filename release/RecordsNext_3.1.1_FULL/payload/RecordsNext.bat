@echo off
setlocal
cd /d "%~dp0"

if not exist "%~dp0RecordsNext.jar" (
    echo.
    echo ERRORE: RecordsNext.jar non trovato.
    echo RecordsNext.bat deve trovarsi nella stessa cartella di RecordsNext.jar.
    echo.
    pause
    exit /b 1
)

where java >nul 2>&1
if errorlevel 1 (
    echo.
    echo ERRORE: Java non trovato.
    echo RecordsNext 3.1 richiede Java 21 o superiore.
    echo.
    pause
    exit /b 1
)

for /f "tokens=3" %%V in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set "JAVA_VERSION=%%~V"
    goto :version_found
)

:version_found
if not defined JAVA_VERSION (
    echo.
    echo ERRORE: impossibile determinare la versione Java.
    echo.
    pause
    exit /b 1
)

for /f "tokens=1 delims=." %%M in ("%JAVA_VERSION%") do set "JAVA_MAJOR=%%M"
if "%JAVA_MAJOR%"=="1" (
    for /f "tokens=2 delims=." %%M in ("%JAVA_VERSION%") do set "JAVA_MAJOR=%%M"
)

if %JAVA_MAJOR% LSS 21 (
    echo.
    echo ERRORE: Java %JAVA_VERSION% non compatibile.
    echo RecordsNext 3.1 richiede Java 21 o superiore.
    echo.
    pause
    exit /b 1
)

if not exist "%~dp0runtime\ucanaccess\ucanaccess-2.0.9.5.jar" goto :ucan_missing
if not exist "%~dp0runtime\ucanaccess\lib\jackcess-2.1.0.jar" goto :ucan_missing
if not exist "%~dp0runtime\ucanaccess\lib\hsqldb.jar" goto :ucan_missing
if not exist "%~dp0runtime\ucanaccess\lib\commons-lang-2.6.jar" goto :ucan_missing
if not exist "%~dp0runtime\ucanaccess\lib\commons-logging-1.1.1.jar" goto :ucan_missing

set "RN_CP=%~dp0RecordsNext.jar;%~dp0runtime\ucanaccess\*;%~dp0runtime\ucanaccess\lib\*"
start "" javaw -cp "%RN_CP%" it.alterlega.recordsnext.gui.RecordsNext2Dashboard
endlocal
exit /b 0

:ucan_missing
echo.
echo ERRORE: runtime UCanAccess 2.0.9.5 incompleto.
echo Reinstalla RecordsNext 3.1 usando il pacchetto FULL.
echo.
pause
exit /b 1
