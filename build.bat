@echo off
REM Universal Logger Java 빌드 스크립트 (Windows)

echo ======================================
echo Universal Logger Java 빌드 시작
echo ======================================

REM 빌드 도구 감지
if exist pom.xml (
    set BUILD_TOOL=maven
    echo 빌드 도구: Maven
    goto maven_build
) else if exist build.gradle (
    set BUILD_TOOL=gradle
    echo 빌드 도구: Gradle
    goto gradle_build
) else (
    echo 오류: pom.xml 또는 build.gradle 파일을 찾을 수 없습니다.
    exit /b 1
)

:maven_build
echo.
echo Maven 빌드 실행 중...
call mvn clean package -DskipTests

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ 빌드 성공!
    echo 생성된 파일:
    echo   - target\universal-logger-1.0.0.jar
    echo   - target\universal-logger-1.0.0-sources.jar
    echo   - target\universal-logger-1.0.0-javadoc.jar

    if exist target\universal-logger-1.0.0.jar (
        for %%A in (target\universal-logger-1.0.0.jar) do (
            echo.
            echo JAR 파일 크기: %%~zA bytes
        )
    )
) else (
    echo.
    echo ❌ 빌드 실패
    exit /b 1
)
goto end

:gradle_build
echo.
echo Gradle 빌드 실행 중...

if exist gradlew.bat (
    call gradlew.bat clean build -x test
) else (
    call gradle clean build -x test
)

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ 빌드 성공!
    echo 생성된 파일:
    echo   - build\libs\universal-logger-1.0.0.jar
    echo   - build\libs\universal-logger-1.0.0-sources.jar
    echo   - build\libs\universal-logger-1.0.0-javadoc.jar

    if exist build\libs\universal-logger-1.0.0.jar (
        for %%A in (build\libs\universal-logger-1.0.0.jar) do (
            echo.
            echo JAR 파일 크기: %%~zA bytes
        )
    )
) else (
    echo.
    echo ❌ 빌드 실패
    exit /b 1
)

:end
echo.
echo ======================================
echo 빌드 완료
echo ======================================
pause