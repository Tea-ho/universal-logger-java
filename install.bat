@echo off
REM Universal Logger Java - Local Maven Install Script (Windows)

echo ======================================
echo Universal Logger Java
echo Local Maven Install
echo ======================================
echo.

REM Check if system Maven exists
where mvn >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo Using Maven from system PATH...
    call mvn clean install -DskipTests
    goto check_result
)

REM Try IntelliJ bundled Maven
echo System Maven not found. Searching for IntelliJ bundled Maven...
set MAVEN_CMD=
for /f "delims=" %%i in ('dir /s /b "C:\Program Files\JetBrains\*mvn.cmd" 2^>nul ^| findstr /i "maven3\\bin\\mvn.cmd" ^| sort /r') do (
    set MAVEN_CMD=%%i
    goto maven_found
)

:maven_found
if defined MAVEN_CMD (
    echo Using IntelliJ bundled Maven: %MAVEN_CMD%
    call "%MAVEN_CMD%" clean install -DskipTests
    goto check_result
)

echo ERROR: Maven not found!
echo.
echo Please use one of the following options:
echo   1. IntelliJ Maven panel (Right panel ^> Maven ^> Lifecycle ^> install)
echo   2. Install Maven: https://maven.apache.org/download.cgi
echo.
exit /b 1

:check_result
if %ERRORLEVEL% EQU 0 (
    echo.
    echo ======================================
    echo SUCCESS: Library published to local Maven repository
    echo ======================================
    echo.
    echo You can now use it in other projects:
    echo.
    echo ^<dependency^>
    echo     ^<groupId^>io.universallogger^</groupId^>
    echo     ^<artifactId^>universal-logger-core^</artifactId^>
    echo     ^<version^>1.0.0^</version^>
    echo ^</dependency^>
    echo.
) else (
    echo.
    echo ======================================
    echo ERROR: Installation failed
    echo ======================================
    echo.
    exit /b 1
)

pause
