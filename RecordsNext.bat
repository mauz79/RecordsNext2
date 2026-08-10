@echo off
setlocal

cd /d "%~dp0"

if not exist "%~dp0RecordsNext.jar" (
    echo.
    echo ERRORE: RecordsNext.jar non trovato.
    echo.
    echo RecordsNext.bat deve trovarsi nella stessa cartella di RecordsNext.jar.
    echo.
    pause
    exit /b 1
)

where java >nul 2>&1
if errorlevel 1 (
    echo.
    echo RecordsNext 2.0 richiede Java 21 o superiore.
    echo Java non e' stato trovato nel PATH di Windows.
    echo.
    echo Installa Java 21 o superiore e riavvia RecordsNext.
    echo.
    pause
    exit /b 1
)

if not exist "%~dp0runtime\ucanaccess\ucanaccess-2.0.9.5.jar" (
    echo.
    echo ERRORE: UCanAccess 2.0.9.5 non trovato.
    echo.
    echo Reinstalla RecordsNext 2.0.
    echo.
    pause
    exit /b 1
)

set "RN_CP=%~dp0RecordsNext.jar;%~dp0runtime\ucanaccess\*;%~dp0runtime\ucanaccess\lib\*"

start "" javaw -cp "%RN_CP%" it.alterlega.recordsnext.gui.RecordsNext2Dashboard

endlocal
exit /b 0