#!/bin/bash

# Universal Logger Java 설치 스크립트

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

# Java 버전 확인
check_java() {
    log_info "Java 버전 확인 중..."

    if command -v java &> /dev/null; then
        JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1-2)
        log_info "Java 버전: $JAVA_VERSION"

        # Java 8 이상 확인
        if java -version 2>&1 | grep -q "1\.[8-9]\|1[0-9]\|[2-9][0-9]"; then
            log_info "✅ Java 버전 요구사항 충족"
        else
            log_error "❌ Java 8 이상이 필요합니다"
            exit 1
        fi
    else
        log_error "❌ Java가 설치되어 있지 않습니다"
        exit 1
    fi
}

# Maven 확인
check_maven() {
    log_info "Maven 확인 중..."

    if command -v mvn &> /dev/null; then
        MVN_VERSION=$(mvn -version | head -n 1 | cut -d' ' -f3)
        log_info "Maven 버전: $MVN_VERSION"
        log_info "✅ Maven 설치 확인"
    else
        log_warn "❌ Maven이 설치되어 있지 않습니다"
        log_info "Maven 자동 설치를 시도합니다..."
        install_maven
    fi
}

# Maven 설치
install_maven() {
    if [[ "$OSTYPE" == "linux-gnu"* ]]; then
        if command -v apt-get &> /dev/null; then
            sudo apt-get update
            sudo apt-get install -y maven
        elif command -v yum &> /dev/null; then
            sudo yum install -y maven
        else
            log_error "지원되지 않는 Linux 배포판입니다"
            exit 1
        fi
    elif [[ "$OSTYPE" == "darwin"* ]]; then
        if command -v brew &> /dev/null; then
            brew install maven
        else
            log_error "Homebrew가 설치되어 있지 않습니다"
            exit 1
        fi
    else
        log_error "지원되지 않는 운영체제입니다"
        exit 1
    fi
}

# 프로젝트 빌드
build_project() {
    log_info "프로젝트 빌드 중..."

    mvn clean compile

    log_info "✅ 프로젝트 빌드 완료"
}

# 의존성 설치
install_dependencies() {
    log_info "의존성 다운로드 중..."

    mvn dependency:resolve

    log_info "✅ 의존성 설치 완료"
}

# JAR 파일 생성
package_jar() {
    log_info "JAR 파일 생성 중..."

    mvn package -DskipTests

    if [ -f "target/universal-logger-1.0.0.jar" ]; then
        log_info "✅ JAR 파일 생성 완료: target/universal-logger-1.0.0.jar"
    else
        log_error "❌ JAR 파일 생성 실패"
        exit 1
    fi
}

# 설정 파일 복사
setup_configs() {
    log_info "설정 파일 설정 중..."

    # 홈 디렉토리에 설정 디렉토리 생성
    mkdir -p "$HOME/.universal-logger-java"

    # 설정 파일들 복사
    cp config/*.yml "$HOME/.universal-logger-java/" 2>/dev/null || true

    log_info "✅ 설정 파일이 $HOME/.universal-logger-java/ 에 복사되었습니다"
}

# 예시 실행
run_examples() {
    log_info "예시 실행 중..."

    log_info "빠른 시작 예시 실행:"
    mvn exec:java -Dexec.mainClass="com.universallogger.examples.QuickStart" -Dexec.classpathScope=compile

    echo ""
    log_info "기본 사용 예시 실행:"
    mvn exec:java -Dexec.mainClass="com.universallogger.examples.BasicUsage" -Dexec.classpathScope=compile

    log_info "✅ 예시 실행 완료"
}

# 설치 완료 메시지
show_completion() {
    echo ""
    log_info "🎉 Universal Logger Java 설치 완료!"
    echo ""
    echo "사용 방법:"
    echo "1. Maven 의존성 추가:"
    echo "   <dependency>"
    echo "       <groupId>com.universallogger</groupId>"
    echo "       <artifactId>universal-logger</artifactId>"
    echo "       <version>1.0.0</version>"
    echo "   </dependency>"
    echo ""
    echo "2. Java 코드에서 사용:"
    echo "   import com.universallogger.UniversalLogger;"
    echo "   UniversalLogger.init(\"logging.yml\");"
    echo "   UniversalLogger.info(\"Hello World!\");"
    echo ""
    echo "3. 설정 파일 위치: $HOME/.universal-logger-java/"
    echo "4. 예시 코드: $(pwd)/examples/"
    echo "5. JAR 파일: $(pwd)/target/universal-logger-1.0.0.jar"
    echo ""
    log_info "문서: https://github.com/yourusername/universal-logger-java"
}

# 메인 설치 프로세스
main() {
    echo "=== Universal Logger Java 설치 프로그램 ==="
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

# 도움말
if [[ "${1:-}" == "--help" ]] || [[ "${1:-}" == "-h" ]]; then
    echo "Universal Logger Java 설치 스크립트"
    echo ""
    echo "사용법: $0"
    echo ""
    echo "이 스크립트는 다음을 수행합니다:"
    echo "  ✅ Java 버전 확인 (8+)"
    echo "  ✅ Maven 설치 확인 및 자동 설치"
    echo "  ✅ 의존성 다운로드"
    echo "  ✅ 프로젝트 빌드"
    echo "  ✅ JAR 파일 생성"
    echo "  ✅ 설정 파일 복사"
    echo "  ✅ 예시 실행"
    echo ""
    echo "요구사항:"
    echo "  - Java 8 이상"
    echo "  - Maven (자동 설치 가능)"
    echo "  - 인터넷 연결"
    echo ""
    exit 0
fi

main "$@"