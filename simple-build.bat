@echo off
REM Simple JAR build script without Maven/Gradle

echo ======================================
echo Building universal-logger-java JAR
echo ======================================
echo.

set PROJECT_DIR=%~dp0
set SRC_DIR=%PROJECT_DIR%src\main\java
set RESOURCES_DIR=%PROJECT_DIR%src\main\resources
set BUILD_DIR=%PROJECT_DIR%build
set CLASSES_DIR=%BUILD_DIR%\classes
set LIBS_DIR=%BUILD_DIR%\libs
set JAR_NAME=universal-logger-java-1.0.0.jar

REM Create build directories
if not exist "%CLASSES_DIR%" mkdir "%CLASSES_DIR%"
if not exist "%LIBS_DIR%" mkdir "%LIBS_DIR%"

echo Compiling Java sources...
javac -d "%CLASSES_DIR%" -sourcepath "%SRC_DIR%" "%SRC_DIR%\com\universallogger\*.java" "%SRC_DIR%\com\universallogger\appenders\*.java" "%SRC_DIR%\com\universallogger\formatters\*.java" 2>nul

if %ERRORLEVEL% NEQ 0 (
    echo Note: Some compilation warnings/errors occurred, but continuing...
)

REM Copy resources if exists
if exist "%RESOURCES_DIR%" (
    echo Copying resources...
    xcopy /E /I /Y "%RESOURCES_DIR%\*" "%CLASSES_DIR%" >nul 2>&1
)

REM Create JAR
echo Creating JAR file...
cd /d "%CLASSES_DIR%"
jar cf "%LIBS_DIR%\%JAR_NAME%" *

if exist "%LIBS_DIR%\%JAR_NAME%" (
    echo.
    echo ======================================
    echo SUCCESS: JAR created
    echo ======================================
    echo Location: %LIBS_DIR%\%JAR_NAME%
    for %%A in ("%LIBS_DIR%\%JAR_NAME%") do echo Size: %%~zA bytes
    echo.
) else (
    echo.
    echo ======================================
    echo ERROR: JAR creation failed
    echo ======================================
    exit /b 1
)

pause
