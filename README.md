# Universal Logger for Java

Java 애플리케이션을 위한 강력한 로깅 라이브러리로, 자동 SQL 쿼리 캡처 및 다양한 출력 옵션을 제공합니다.

## 주요 기능

- ✅ **자동 SQL 캡처**: MyBatis, JPA, JDBC 쿼리 자동 로깅
- ✅ **파라미터 바인딩 추적**: PreparedStatement 파라미터 자동 로깅
- ✅ **실행 시간 측정**: 쿼리 성능 모니터링 및 슬로우 쿼리 경고
- ✅ **다중 데이터베이스 지원**: MySQL, Oracle, MSSQL, PostgreSQL, H2
- ✅ **프로젝트명 자동 감지**: `spring.application.name` 기반 로그 경로 생성
- ✅ **표준 로그 경로**: `C:/log/{projectName}/` 자동 생성
- ✅ **다양한 출력**: 콘솔, 파일, 에러 파일, 데이터베이스, 웹훅, 이메일
- ✅ **Spring Boot & Spring Framework 지원**: 자동 설정 및 수동 설정 모두 지원
- 🔒 **민감한 정보 자동 마스킹**
- 📊 **모듈별 로그 레벨 관리**
- ⚡ **SLF4J & Logback 기반 고성능**
- 🔄 **로그 파일 자동 로테이션**

## 🚀 빠른 시작

### 1. 설치

```bash
# 자동 설치
git clone <repository>
cd universal-logger-java
./install.sh

# 또는 Maven으로 빌드
mvn clean compile package
```

### 2. Maven 의존성 추가

```xml
<dependency>
    <groupId>com.universallogger</groupId>
    <artifactId>universal-logger</artifactId>
    <version>1.0.0</version>
</dependency>

<!-- Jackson 2.13.5 이상 필수 -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-core</artifactId>
    <version>2.13.5</version>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.13.5</version>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.dataformat</groupId>
    <artifactId>jackson-dataformat-yaml</artifactId>
    <version>2.13.5</version>
</dependency>
```

### 3. 3줄로 시작하기

```java
import com.universallogger.UniversalLogger;

UniversalLogger.init("logging.yml");  // 1줄: 설정 파일로 초기화
UniversalLogger.info("Hello World!"); // 2줄: 로그 사용
```

### 4. 설정 파일 생성

**logging.yml**
```yaml
version: "1.0"

global:
  appName: "JavaApp"

levels:
  root: "INFO"

outputs:
  console:
    enabled: true
    level: "INFO"
    format: "[{timestamp}] {level} - {message}"
    colors:
      INFO: "green"
      ERROR: "red"

  file:
    enabled: true
    level: "DEBUG"
    path: "logs/{appName}_{date}.log"
    format: "{timestamp} | {level} | {logger} | {message}"
```

## 📖 상세 사용법

### 🔍 SLF4J vs Logback - 어떻게 선택하나요?

**Universal Logger는 둘 다 지원합니다!**

```
┌──────────────────────────────────┐
│  Universal Logger                │
│  (SLF4J API + Logback 구현)      │
└──────────────┬───────────────────┘
               │
        ┌──────┴──────┐
        │             │
    SLF4J 사용    Logback 직접 사용
    프로젝트        프로젝트
        │             │
        └──────┬──────┘
               │
          ✅ 모두 호환!
```

**사용 가이드:**

| 상황 | 권장 방법 | 이유 |
|-----|---------|------|
| **신규 프로젝트** | SLF4J API 사용 | 추상화 계층, 유연성 |
| **기존 SLF4J 프로젝트** | 그대로 사용 | 기존 코드 수정 불필요 |
| **기존 Logback 프로젝트** | 그대로 사용 | 동일 Logback 인스턴스 공유 |
| **둘 다 혼용** | 문제없음 | 내부적으로 같은 구현체 사용 |

### 기본 로깅 (UniversalLogger 직접 사용)

```java
import com.universallogger.UniversalLogger;

// 설정 파일로 초기화
UniversalLogger.init("config/logging.yml");

// 기본 로그 레벨들
UniversalLogger.debug("디버그 메시지");
UniversalLogger.info("정보 메시지");
UniversalLogger.warn("경고 메시지");
UniversalLogger.error("에러 메시지");
```

### SLF4J 스타일 사용 (권장)

```java
import com.universallogger.UniversalLogger;
import org.slf4j.Logger;

// 모듈별로 다른 설정 적용
Logger dbLogger = UniversalLogger.getLogger("database");
Logger apiLogger = UniversalLogger.getLogger("api");

dbLogger.debug("데이터베이스 연결 중...");
apiLogger.info("API 요청 처리 중...");

// 포맷팅된 로그
apiLogger.info("사용자 {}가 {}에 접근했습니다", username, endpoint);
```

### Logback 직접 사용 (기존 프로젝트)

**기존 Logback을 사용하는 프로젝트에서도 그대로 사용 가능합니다!**

```java
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

public class MyService {
    // 기존 Logback 코드 - 수정 불필요!
    private static final Logger logger =
        (Logger) LoggerFactory.getLogger(MyService.class);

    public void doSomething() {
        // Universal Logger의 설정이 자동으로 적용됨
        logger.info("기존 Logback 코드가 그대로 동작합니다!");
        logger.debug("설정 파일의 레벨/포맷이 적용됨");
    }
}
```

**혼용 사용 예시 (둘 다 사용 가능):**

```java
import com.universallogger.UniversalLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MixedUsage {
    // SLF4J 스타일 (권장)
    private static final Logger slf4jLogger =
        UniversalLogger.getLogger("slf4j-style");

    // Logback 직접 사용 (기존 코드)
    private static final org.slf4j.Logger logbackLogger =
        LoggerFactory.getLogger(MixedUsage.class);

    static {
        // 한 번만 초기화하면 둘 다 적용됨
        UniversalLogger.init("logging.yml");
    }

    public void test() {
        // 둘 다 같은 설정으로 동작
        slf4jLogger.info("SLF4J 스타일 로그");
        logbackLogger.info("Logback 직접 사용 로그");
        // → 모두 logging.yml의 설정 적용됨!
    }
}
```

### Spring Boot 통합

```java
@RestController
public class UserController {

    private static final Logger logger = UniversalLogger.getLogger("api");

    static {
        UniversalLogger.init("classpath:logging.yml");
    }

    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        logger.info("새 사용자 생성 요청: {}", user.getUsername());

        try {
            User savedUser = userService.save(user);
            logger.info("사용자 생성 완료: {}", savedUser.getId());
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            logger.error("사용자 생성 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
```

## ⚙️ 설정 옵션

### 출력 대상

| 출력 대상 | 설명 | 설정 키 |
|----------|------|---------|
| 콘솔 | 터미널 출력 (컬러 지원) | `console` |
| 파일 | 로그 파일 (로테이션 지원) | `file` |
| 에러 파일 | 에러 전용 파일 | `errorFile` |
| 데이터베이스 | MySQL, SQLite | `database` |
| 웹훅 | Slack, Discord 등 | `webhook` |
| 이메일 | SMTP 알림 | `email` |

### 포맷 변수

```yaml
format: "{timestamp} | {level} | {logger} | {method}:{line} | {message}"
```

사용 가능한 변수:
- `{timestamp}` - 시간
- `{level}` - 로그 레벨
- `{logger}` - 로거명
- `{class}` - 클래스명
- `{method}` - 메서드명
- `{line}` - 라인 번호
- `{thread}` - 스레드명
- `{message}` - 로그 메시지
- `{appName}` - 애플리케이션 이름

## 📁 프로젝트 구조

```
universal-logger-java/
├── src/main/java/com/universallogger/
│   ├── UniversalLogger.java          # 메인 라이브러리
│   ├── config/
│   │   └── LoggerConfig.java         # 설정 클래스들
│   ├── utils/
│   │   ├── ConfigLoader.java         # 설정 파일 로더
│   │   └── MessageFilter.java        # 메시지 필터
│   ├── formatters/
│   │   ├── CustomFormatter.java      # 커스텀 포맷터
│   │   └── ColorFormatter.java       # 컬러 포맷터
│   └── appenders/
│       ├── WebhookAppender.java      # 웹훅 어펜더
│       ├── EmailAppender.java        # 이메일 어펜더
│       └── DatabaseAppender.java     # DB 어펜더
├── config/
│   ├── logging.yml                   # 완전한 설정 예시
│   └── simple.yml                    # 간단한 설정 예시
├── examples/
│   ├── QuickStart.java               # 빠른 시작
│   ├── BasicUsage.java               # 기본 사용법
│   └── WebAppExample.java            # 웹앱 예시
├── pom.xml                           # Maven 설정
├── install.sh                        # 자동 설치 스크립트
└── README.md                         # 이 파일
```

## 🔧 고급 설정

### Slack 웹훅 연동

```yaml
outputs:
  webhook:
    enabled: true
    level: "ERROR"
    url: "https://hooks.slack.com/services/YOUR/SLACK/WEBHOOK"
    format: "🚨 *{level}* in {appName}\\n*Logger:* {logger}\\n*Message:* {message}"
```

### 데이터베이스 로깅

```yaml
outputs:
  database:
    enabled: true
    level: "INFO"
    database:
      type: "mysql"
      host: "localhost"
      database: "logs"
      table: "app_logs"
      username: "logger"
      password: "password"
```

### 민감한 정보 마스킹

```yaml
filters:
  sensitiveData:
    enabled: true
    patterns:
      - regex: "password=\\w+"
        replace: "password=***"
      - regex: "token=\\w+"
        replace: "token=***"
      - regex: "\\d{4}-\\d{4}-\\d{4}-\\d{4}"
        replace: "****-****-****-****"
```

## 🧪 테스트

```bash
# 빠른 테스트
mvn exec:java -Dexec.mainClass="com.universallogger.examples.QuickStart"

# 기본 기능 테스트
mvn exec:java -Dexec.mainClass="com.universallogger.examples.BasicUsage"

# 웹앱 시뮬레이션
mvn exec:java -Dexec.mainClass="com.universallogger.examples.WebAppExample"
```

## 📋 요구사항

- Java 8+
- Maven 3.6+
- SLF4J (포함됨)
- Logback (포함됨)
- **Jackson 2.13.5 이상** (YAML/JSON 파싱용) - **필수**
  - jackson-core 2.13.5+
  - jackson-databind 2.13.5+
  - jackson-dataformat-yaml 2.13.5+

⚠️ **중요**: Jackson 버전이 2.13.5 미만인 경우 `NoSuchMethodError` 발생
- 기존 프로젝트에서 Jackson 2.5.x 등 구버전 사용 시 버전 업그레이드 필요

## 🆚 Python vs Java

| 기능 | Python | Java |
|------|---------|------|
| **설정 파일** | YAML, XML, JSON | YAML, XML, JSON |
| **사용법** | `logger.init()` | `UniversalLogger.init()` |
| **출력 대상** | 콘솔, 파일, DB, 웹훅, 이메일 | 콘솔, 파일, DB, 웹훅, 이메일 |
| **성능** | asyncio 지원 | SLF4J/Logback 고성능 |
| **Spring 통합** | ❌ | ✅ 완벽 지원 |
| **컬러 출력** | ✅ | ✅ |

## 🤝 기여

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📄 라이센스

MIT License

---

**Universal Logger Java** - 한 번 설정하면 모든 Java 프로젝트에서 바로 사용! ☕🚀