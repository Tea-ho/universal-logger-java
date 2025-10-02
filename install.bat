@echo off
REM Universal Logger Java - Local Maven Install Script (Windows)

echo ======================================
echo Universal Logger Java
echo Local Maven Install
echo ======================================
echo.

REM Use LIMS_STUDY's gradlew to build this project
set LIMS_GRADLEW=C:\Users\th\Desktop\testCode\LISM_STUDY\LIMS_STUDY\gradlew.bat
set CURRENT_DIR=%~dp0
if exist "%LIMS_GRADLEW%" (
    echo Using Gradle Wrapper from LIMS_STUDY project...
    cd /d "%CURRENT_DIR%"
    call "%LIMS_GRADLEW%" -p . clean build publishToMavenLocal -x test
    goto check_result
)

REM Check if gradle command exists
where gradle >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo Using Gradle...
    call gradle clean build publishToMavenLocal -x test
    goto check_result
)

REM Check if mvn command exists
where mvn >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo Using Maven...
    call mvn clean install -DskipTests
    goto check_result
)

echo ERROR: Neither Gradle nor Maven found!
echo Please install Gradle or Maven first.
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
    echo repositories {
    echo     mavenLocal^(^)
    echo }
    echo.
    echo dependencies {
    echo     implementation 'com.universallogger:universal-logger-java:1.0.0'
    echo }
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
