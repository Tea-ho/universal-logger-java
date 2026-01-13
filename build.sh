#!/bin/bash
# Universal Logger Java Build Script

echo "======================================"
echo "Universal Logger Java Build"
echo "======================================"

# Detect build tool
if [ -f "pom.xml" ]; then
    BUILD_TOOL="maven"
    echo "Build tool: Maven"
elif [ -f "build.gradle" ]; then
    BUILD_TOOL="gradle"
    echo "Build tool: Gradle"
else
    echo "ERROR: pom.xml or build.gradle not found."
    exit 1
fi

# Maven build
if [ "$BUILD_TOOL" = "maven" ]; then
    echo ""
    echo "Running Maven build..."

    # Try system Maven first
    if command -v mvn &> /dev/null; then
        echo "Maven path: System PATH"
        MAVEN_CMD="mvn"
    else
        echo "System Maven not found. Searching for IntelliJ bundled Maven..."
        MAVEN_CMD=""

        # macOS: Search in /Applications
        if [ -z "$MAVEN_CMD" ] && [ "$(uname)" = "Darwin" ]; then
            MAVEN_CMD=$(find /Applications -name "mvn" -path "*/maven3/bin/mvn" 2>/dev/null | sort -r | head -n 1)
        fi

        # Linux: Search in common installation directories
        if [ -z "$MAVEN_CMD" ] && [ "$(uname)" = "Linux" ]; then
            for dir in /opt "$HOME/opt" "$HOME/.local" /usr/local; do
                if [ -d "$dir" ]; then
                    found=$(find "$dir" -name "mvn" -path "*/maven3/bin/mvn" 2>/dev/null | sort -r | head -n 1)
                    if [ -n "$found" ]; then
                        MAVEN_CMD="$found"
                        break
                    fi
                fi
            done
        fi

        if [ -z "$MAVEN_CMD" ]; then
            echo ""
            echo "ERROR: Maven not found."
            echo ""
            echo "Solutions:"
            echo "  1. Use IntelliJ Maven panel (Recommended)"
            echo "  2. Install Maven: brew install maven (macOS) or apt install maven (Ubuntu)"
            echo "  3. Manually specify IntelliJ bundled Maven path"
            echo ""
            exit 1
        fi

        echo "Maven path: $MAVEN_CMD"
    fi

    $MAVEN_CMD clean package -DskipTests

    if [ $? -eq 0 ]; then
        echo ""
        echo "BUILD SUCCESS"
        echo "Generated files:"
        echo "  - target/universal-logger-1.0.0.jar"
        echo "  - target/universal-logger-1.0.0-sources.jar"
        echo "  - target/universal-logger-1.0.0-javadoc.jar"

        # JAR file info
        if [ -f "target/universal-logger-1.0.0.jar" ]; then
            SIZE=$(du -h target/universal-logger-1.0.0.jar | cut -f1)
            echo ""
            echo "JAR file size: $SIZE"
        fi
    else
        echo ""
        echo "BUILD FAILED"
        exit 1
    fi
fi

# Gradle build
if [ "$BUILD_TOOL" = "gradle" ]; then
    echo ""
    echo "Running Gradle build..."

    if [ -f "gradlew" ]; then
        ./gradlew clean build -x test
    else
        gradle clean build -x test
    fi

    if [ $? -eq 0 ]; then
        echo ""
        echo "BUILD SUCCESS"
        echo "Generated files:"
        echo "  - build/libs/universal-logger-1.0.0.jar"
        echo "  - build/libs/universal-logger-1.0.0-sources.jar"
        echo "  - build/libs/universal-logger-1.0.0-javadoc.jar"

        # JAR file info
        if [ -f "build/libs/universal-logger-1.0.0.jar" ]; then
            SIZE=$(du -h build/libs/universal-logger-1.0.0.jar | cut -f1)
            echo ""
            echo "JAR file size: $SIZE"
        fi
    else
        echo ""
        echo "BUILD FAILED"
        exit 1
    fi
fi

echo ""
echo "======================================"
echo "Build Complete"
echo "======================================"
