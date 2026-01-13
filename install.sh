#!/bin/bash

# Universal Logger Java Installation Script

set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check Java version
check_java() {
    log_info "Checking Java version..."

    if command -v java &> /dev/null; then
        JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1-2)
        log_info "Java version: $JAVA_VERSION"

        # Check Java 8 or higher
        if java -version 2>&1 | grep -q "1\.[8-9]\|1[0-9]\|[2-9][0-9]"; then
            log_info "Java version requirement met"
        else
            log_error "Java 8 or higher is required"
            exit 1
        fi
    else
        log_error "Java is not installed"
        exit 1
    fi
}

# Check Maven
check_maven() {
    log_info "Checking Maven..."

    # Try system Maven first
    if command -v mvn &> /dev/null; then
        MVN_VERSION=$(mvn -version | head -n 1 | cut -d' ' -f3)
        log_info "Maven version: $MVN_VERSION"
        log_info "Maven installation confirmed (System PATH)"
        MAVEN_CMD="mvn"
        return
    fi

    # Try IntelliJ bundled Maven
    log_info "System Maven not found. Searching for IntelliJ bundled Maven..."
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

    if [ -n "$MAVEN_CMD" ]; then
        log_info "IntelliJ bundled Maven found: $MAVEN_CMD"
        return
    fi

    # Maven not found - attempt auto-install
    log_warn "Maven not found"
    log_info "Attempting to auto-install Maven..."
    install_maven

    # Set MAVEN_CMD after installation
    MAVEN_CMD="mvn"
}

# Install Maven
install_maven() {
    if [[ "$OSTYPE" == "linux-gnu"* ]]; then
        if command -v apt-get &> /dev/null; then
            sudo apt-get update
            sudo apt-get install -y maven
        elif command -v yum &> /dev/null; then
            sudo yum install -y maven
        else
            log_error "Unsupported Linux distribution"
            exit 1
        fi
    elif [[ "$OSTYPE" == "darwin"* ]]; then
        if command -v brew &> /dev/null; then
            brew install maven
        else
            log_error "Homebrew is not installed"
            exit 1
        fi
    else
        log_error "Unsupported operating system"
        exit 1
    fi
}

# Build project
build_project() {
    log_info "Building project..."

    $MAVEN_CMD clean compile

    log_info "Project build complete"
}

# Install dependencies
install_dependencies() {
    log_info "Downloading dependencies..."

    $MAVEN_CMD dependency:resolve

    log_info "Dependencies installation complete"
}

# Create JAR file
package_jar() {
    log_info "Creating JAR file..."

    $MAVEN_CMD package -DskipTests

    if [ -f "target/universal-logger-1.0.0.jar" ]; then
        log_info "JAR file created: target/universal-logger-1.0.0.jar"
    else
        log_error "JAR file creation failed"
        exit 1
    fi
}

# Setup config files
setup_configs() {
    log_info "Setting up configuration files..."

    # Create config directory in home
    mkdir -p "$HOME/.universal-logger-java"

    # Copy config files
    cp config/*.yml "$HOME/.universal-logger-java/" 2>/dev/null || true

    log_info "Configuration files copied to $HOME/.universal-logger-java/"
}

# Run examples
run_examples() {
    log_info "Running examples..."

    log_info "Running quick start example:"
    $MAVEN_CMD exec:java -Dexec.mainClass="com.universallogger.examples.QuickStart" -Dexec.classpathScope=compile

    echo ""
    log_info "Running basic usage example:"
    $MAVEN_CMD exec:java -Dexec.mainClass="com.universallogger.examples.BasicUsage" -Dexec.classpathScope=compile

    log_info "Examples execution complete"
}

# Show completion message
show_completion() {
    echo ""
    log_info "Universal Logger Java installation complete!"
    echo ""
    echo "Usage:"
    echo "1. Add Maven dependency:"
    echo "   <dependency>"
    echo "       <groupId>com.universallogger</groupId>"
    echo "       <artifactId>universal-logger</artifactId>"
    echo "       <version>1.0.0</version>"
    echo "   </dependency>"
    echo ""
    echo "2. Use in Java code:"
    echo "   import com.universallogger.UniversalLogger;"
    echo "   UniversalLogger.init(\"logging.yml\");"
    echo "   UniversalLogger.info(\"Hello World!\");"
    echo ""
    echo "3. Config file location: $HOME/.universal-logger-java/"
    echo "4. Example code: $(pwd)/examples/"
    echo "5. JAR file: $(pwd)/target/universal-logger-1.0.0.jar"
    echo ""
    log_info "Documentation: https://github.com/yourusername/universal-logger-java"
}

# Main installation process
main() {
    echo "=== Universal Logger Java Installation ==="
    echo ""

    check_java
    check_maven
    install_dependencies
    build_project
    package_jar
    setup_configs
    run_examples
    show_completion
}

# Help
if [[ "${1:-}" == "--help" ]] || [[ "${1:-}" == "-h" ]]; then
    echo "Universal Logger Java Installation Script"
    echo ""
    echo "Usage: $0"
    echo ""
    echo "This script performs the following:"
    echo "  - Check Java version (8+)"
    echo "  - Check Maven installation and auto-install"
    echo "  - Download dependencies"
    echo "  - Build project"
    echo "  - Create JAR file"
    echo "  - Copy configuration files"
    echo "  - Run examples"
    echo ""
    echo "Requirements:"
    echo "  - Java 8 or higher"
    echo "  - Maven (auto-install available)"
    echo "  - Internet connection"
    echo ""
    exit 0
fi

main "$@"
