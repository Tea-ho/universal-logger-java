@echo off
REM Universal Logger Java Build Script (Windows)

echo ======================================
echo Universal Logger Java Build
echo ======================================

REM Detect build tool
if exist pom.xml (
    set BUILD_TOOL=maven
    echo Build tool: Maven
    goto maven_build
) else if exist build.gradle (
    set BUILD_TOOL=gradle
    echo Build tool: Gradle
    goto gradle_build
) else (
    echo ERROR: pom.xml or build.gradle not found.
    exit /b 1
)

:maven_build
echo.
echo Running Maven build...

REM Try system Maven first
where mvn >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo Maven path: System PATH
    call mvn clean package -DskipTests
    goto maven_build_check
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
    echo Maven path: %MAVEN_CMD%
    call "%MAVEN_CMD%" clean package -DskipTests
    goto maven_build_check
) else (
    echo.
    echo ERROR: Maven not found.
    echo.
    echo Solutions:
    echo   1. Use IntelliJ Maven panel (Recommended)
    echo   2. Install Maven and add to PATH
    echo   3. Manually specify IntelliJ bundled Maven path
    echo.
    exit /b 1
)

:maven_build_check

if %ERRORLEVEL% EQU 0 (
    echo.
    echo BUILD SUCCESS
    echo Generated files:
    echo   - target\universal-logger-1.0.0.jar
    echo   - target\universal-logger-1.0.0-sources.jar
    echo   - target\universal-logger-1.0.0-javadoc.jar

    if exist target\universal-logger-1.0.0.jar (
        for %%A in (target\universal-logger-1.0.0.jar) do (
            echo.
            echo JAR file size: %%~zA bytes
        )
    )
) else (
    echo.
    echo BUILD FAILED
    exit /b 1
)
goto end

:gradle_build
echo.
echo Running Gradle build...

if exist gradlew.bat (
    call gradlew.bat clean build -x test
) else (
    call gradle clean build -x test
)

if %ERRORLEVEL% EQU 0 (
    echo.
    echo BUILD SUCCESS
    echo Generated files:
    echo   - build\libs\universal-logger-1.0.0.jar
    echo   - build\libs\universal-logger-1.0.0-sources.jar
    echo   - build\libs\universal-logger-1.0.0-javadoc.jar

    if exist build\libs\universal-logger-1.0.0.jar (
        for %%A in (build\libs\universal-logger-1.0.0.jar) do (
            echo.
            echo JAR file size: %%~zA bytes
        )
    )
) else (
    echo.
    echo BUILD FAILED
    exit /b 1
)

:end
echo.
echo ======================================
echo Build Complete
echo ======================================
pause
