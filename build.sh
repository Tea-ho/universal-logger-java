#!/bin/bash
# Universal Logger Java 빌드 스크립트

echo "======================================"
echo "Universal Logger Java 빌드 시작"
echo "======================================"

# 빌드 도구 감지
if [ -f "pom.xml" ]; then
    BUILD_TOOL="maven"
    echo "빌드 도구: Maven"
elif [ -f "build.gradle" ]; then
    BUILD_TOOL="gradle"
    echo "빌드 도구: Gradle"
else
    echo "오류: pom.xml 또는 build.gradle 파일을 찾을 수 없습니다."
    exit 1
fi

# Maven 빌드
if [ "$BUILD_TOOL" = "maven" ]; then
    echo ""
    echo "Maven 빌드 실행 중..."
    mvn clean package -DskipTests

    if [ $? -eq 0 ]; then
        echo ""
        echo "✅ 빌드 성공!"
        echo "생성된 파일:"
        echo "  - target/universal-logger-1.0.0.jar"
        echo "  - target/universal-logger-1.0.0-sources.jar"
        echo "  - target/universal-logger-1.0.0-javadoc.jar"

        # JAR 파일 정보 출력
        if [ -f "target/universal-logger-1.0.0.jar" ]; then
            SIZE=$(du -h target/universal-logger-1.0.0.jar | cut -f1)
            echo ""
            echo "JAR 파일 크기: $SIZE"
        fi
    else
        echo ""
        echo "❌ 빌드 실패"
        exit 1
    fi
fi

# Gradle 빌드
if [ "$BUILD_TOOL" = "gradle" ]; then
    echo ""
    echo "Gradle 빌드 실행 중..."

    if [ -f "gradlew" ]; then
        ./gradlew clean build -x test
    else
        gradle clean build -x test
    fi

    if [ $? -eq 0 ]; then
        echo ""
        echo "✅ 빌드 성공!"
        echo "생성된 파일:"
        echo "  - build/libs/universal-logger-1.0.0.jar"
        echo "  - build/libs/universal-logger-1.0.0-sources.jar"
        echo "  - build/libs/universal-logger-1.0.0-javadoc.jar"

        # JAR 파일 정보 출력
        if [ -f "build/libs/universal-logger-1.0.0.jar" ]; then
            SIZE=$(du -h build/libs/universal-logger-1.0.0.jar | cut -f1)
            echo ""
            echo "JAR 파일 크기: $SIZE"
        fi
    else
        echo ""
        echo "❌ 빌드 실패"
        exit 1
    fi
fi

echo ""
echo "======================================"
echo "빌드 완료"
echo "======================================"