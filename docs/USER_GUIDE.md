# Universal Logger Java 사용자 가이드

## 📋 목차
1. [소개](#소개)
2. [설치 방법](#설치-방법)
3. [빠른 시작](#빠른-시작)
4. [설정 파일 작성](#설정-파일-작성)
5. [고급 사용법](#고급-사용법)
6. [FAQ](#faq)

---

## 소개

**Universal Logger**는 모든 Java 프로젝트에서 사용 가능한 범용 로깅 라이브러리입니다.

### 주요 특징
- ✅ **Maven/Gradle 모두 지원** - 빌드 도구 관계없이 사용 가능
- ✅ **SLF4J/Logback 호환** - 기존 로깅 코드와 완벽 호환
- ✅ **최소 설정** - XML 또는 YAML 파일만으로 완전 제어
- ✅ **자동 초기화** - 설정 파일 자동 감지 및 로드
- ✅ **객체지향 설계** - 확장 및 유지보수 용이

---

## 설치 방법

### Maven 프로젝트

`pom.xml`에 의존성 추가:

```xml
<dependency>
    <groupId>com.universallogger</groupId>
    <artifactId>universal-logger</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle 프로젝트

`build.gradle`에 의존성 추가:

```gradle
dependencies {
    implementation 'com.universallogger:universal-logger:1.0.0'
}
```

### JAR 파일 직접 추가

1. `universal-logger-1.0.0.jar` 파일을 다운로드
2. 프로젝트의 `lib` 폴더에 복사
3. 빌드 패스에 추가

---

## 빠른 시작

### 방법 1: 자동 초기화 (권장)

설정 파일만 프로젝트에 추가하면 자동으로 초기화됩니다.

#### 1단계: 설정 파일 생성

프로젝트 루트 또는 `config` 폴더에 `universal-logger.yml` 파일 생성:

```yaml
# universal-logger.yml
version: "1.0"

global:
  appName: "MyApp"

levels:
  root: "INFO"

outputs:
  console:
    enabled: true
    level: "INFO"

  file:
    enabled: true
    level: "DEBUG"
    path: "logs/{appName}.log"
```

#### 2단계: 코드에서 사용

```java
import com.universallogger.UniversalLogger;

public class MyApplication {
    public static void main(String[] args) {
        // 자동으로 universal-logger.yml을 찾아 초기화됨
        UniversalLogger.info("애플리케이션 시작!");
        UniversalLogger.debug("디버그 메시지");
        UniversalLogger.error("에러 발생!");
    }
}
```

### 방법 2: 수동 초기화

```java
import com.universallogger.UniversalLogger;

public class MyApplication {
    public static void main(String[] args) {
        // 설정 파일 경로 지정
        UniversalLogger.init("config/logging.yml");

        UniversalLogger.info("로거 초기화 완료");
    }
}
```

### 방법 3: SLF4J 스타일 사용

```java
import com.universallogger.UniversalLogger;
import org.slf4j.Logger;

public class UserService {
    private static final Logger logger = UniversalLogger.getLogger("UserService");

    public void createUser(String username) {
        logger.info("사용자 생성 시작: {}", username);

        try {
            // 비즈니스 로직
            logger.debug("사용자 검증 완료");
        } catch (Exception e) {
            logger.error("사용자 생성 실패: {}", e.getMessage(), e);
        }
    }
}
```

---

## 설정 파일 작성

### 자동 감지되는 설정 파일 (우선순위 순)

1. `universal-logger.yml`
2. `config/universal-logger.yml`
3. `universal-logger.xml`
4. `config/universal-logger.xml`
5. `logback.xml`
6. `config/logback.xml`
7. `minimal.yml`
8. `config/minimal.yml`

### YAML 설정 예시

#### 최소 설정 (minimal.yml)

```yaml
version: "1.0"

global:
  appName: "MyApp"

levels:
  root: "INFO"

outputs:
  console:
    enabled: true
    level: "INFO"

  file:
    enabled: true
    level: "DEBUG"
    path: "logs/{appName}.log"
```

#### 완전한 설정 (logging.yml)

```yaml
version: "1.0"

# 전역 설정
global:
  appName: "MyJavaApp"
  timezone: "Asia/Seoul"
  encoding: "UTF-8"

# 로그 레벨 설정 (모듈별)
levels:
  root: "INFO"
  database: "DEBUG"
  api: "INFO"
  security: "WARN"

# 출력 설정
outputs:
  # 콘솔 출력
  console:
    enabled: true
    level: "INFO"
    format: "[{timestamp}] {level} - {logger} - {message}"
    colors:
      DEBUG: "cyan"
      INFO: "green"
      WARN: "yellow"
      ERROR: "red"

  # 파일 출력
  file:
    enabled: true
    level: "DEBUG"
    path: "logs/{appName}_{date}.log"
    format: "{timestamp} | {level} | {logger} | {method}:{line} | {message}"
    rotation:
      maxSize: "10MB"
      maxFiles: 30
      compress: true

  # 에러 파일 (ERROR만)
  errorFile:
    enabled: true
    level: "ERROR"
    path: "logs/errors_{date}.log"
    format: "{timestamp} | {level} | {logger} | {method}:{line} | {message}"

  # Slack 웹훅
  webhook:
    enabled: false
    level: "ERROR"
    url: "https://hooks.slack.com/services/YOUR/SLACK/WEBHOOK"
    format: "🚨 *{level}* in {appName}\\n*Logger:* {logger}\\n*Message:* {message}"

  # 이메일 알림
  email:
    enabled: false
    level: "CRITICAL"
    smtp:
      host: "smtp.gmail.com"
      port: 587
      username: "your-email@gmail.com"
      password: "your-password"
    to: ["admin@company.com"]
    subject: "[{appName}] Critical Error Alert"

  # 데이터베이스 로깅
  database:
    enabled: false
    level: "INFO"
    database:
      type: "mysql"
      host: "localhost"
      database: "logs"
      table: "app_logs"
      username: "logger"
      password: "password"

# 민감 정보 필터
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

# 성능 설정
performance:
  async: true
  bufferSize: 1000
  flushInterval: 5
  maxQueueSize: 10000
```

### XML 설정 예시 (Logback 표준 형식)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <!-- 애플리케이션 이름 -->
    <property name="APP_NAME" value="MyApp" />

    <!-- 로그 파일 경로 -->
    <property name="LOG_PATH" value="logs" />

    <!-- 콘솔 출력 -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>[%d{yyyy-MM-dd HH:mm:ss}] %highlight(%-5level) %cyan(%logger{36}) - %msg%n</pattern>
        </encoder>
    </appender>

    <!-- 파일 출력 -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_PATH}/${APP_NAME}.log</file>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} | %-5level | %logger{36} | %msg%n</pattern>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${LOG_PATH}/${APP_NAME}-%d{yyyy-MM-dd}.%i.log.gz</fileNamePattern>
            <timeBasedFileNamingAndTriggeringPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                <maxFileSize>10MB</maxFileSize>
            </timeBasedFileNamingAndTriggeringPolicy>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
    </appender>

    <!-- 에러 파일 -->
    <appender name="ERROR_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_PATH}/${APP_NAME}-error.log</file>
        <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
            <level>ERROR</level>
        </filter>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} | %-5level | %logger{36} | %msg%n</pattern>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${LOG_PATH}/${APP_NAME}-error-%d{yyyy-MM-dd}.log.gz</fileNamePattern>
            <maxHistory>90</maxHistory>
        </rollingPolicy>
    </appender>

    <!-- 루트 로거 -->
    <root level="INFO">
        <appender-ref ref="CONSOLE" />
        <appender-ref ref="FILE" />
        <appender-ref ref="ERROR_FILE" />
    </root>
</configuration>
```

---

## 고급 사용법

### Spring Boot 통합

```java
@SpringBootApplication
public class Application {

    private static final Logger logger = UniversalLogger.getLogger("Application");

    public static void main(String[] args) {
        // Spring Boot 시작 전 로거 초기화
        UniversalLogger.init("config/logging.yml");

        logger.info("Spring Boot 애플리케이션 시작");
        SpringApplication.run(Application.class, args);
    }
}

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = UniversalLogger.getLogger("UserController");

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody UserDto dto) {
        logger.info("사용자 생성 요청: {}", dto.getUsername());

        try {
            User user = userService.create(dto);
            logger.info("사용자 생성 완료: id={}", user.getId());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            logger.error("사용자 생성 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
}
```

### 모듈별 로그 레벨 관리

```yaml
# logging.yml
levels:
  root: "INFO"        # 기본 레벨
  database: "DEBUG"   # DB 관련 상세 로그
  api: "INFO"         # API는 일반 로그
  security: "WARN"    # 보안은 경고만
```

```java
public class DatabaseService {
    private static final Logger logger = UniversalLogger.getLogger("database");

    public void executeQuery(String sql) {
        logger.debug("Executing query: {}", sql);  // DEBUG 레벨로 출력
    }
}

public class SecurityService {
    private static final Logger logger = UniversalLogger.getLogger("security");

    public void checkPermission(String user) {
        logger.debug("이 메시지는 출력되지 않음");  // WARN 이상만 출력
        logger.warn("권한 확인: {}", user);  // 출력됨
    }
}
```

### 포맷 변수 사용

YAML 설정의 `format` 필드에서 사용 가능한 변수:

| 변수 | 설명 | 예시 |
|-----|------|------|
| `{timestamp}` | 로그 시각 | 2025-09-30 14:30:00 |
| `{level}` | 로그 레벨 | INFO, ERROR |
| `{logger}` | 로거 이름 | UserService |
| `{class}` | 클래스명 | com.example.UserService |
| `{method}` | 메서드명 | createUser |
| `{line}` | 라인 번호 | 42 |
| `{thread}` | 스레드명 | main |
| `{message}` | 로그 메시지 | 사용자 생성 완료 |
| `{appName}` | 앱 이름 | MyApp |

예시:
```yaml
format: "{timestamp} | {level} | {logger} | {method}:{line} | {message}"
# 출력: 2025-09-30 14:30:00 | INFO | UserService | createUser:42 | 사용자 생성 완료
```

### 로그 파일 로테이션

```yaml
file:
  enabled: true
  path: "logs/{appName}_{date}.log"
  rotation:
    maxSize: "10MB"      # 파일당 최대 크기
    maxFiles: 30         # 최대 보관 파일 수 (30일)
    compress: true       # gzip 압축 활성화
```

생성되는 파일:
```
logs/
├── MyApp_2025-09-30.log           # 현재 로그
├── MyApp_2025-09-29.log.gz        # 이전 로그 (압축)
└── MyApp_2025-09-28.log.gz
```

### 민감 정보 자동 마스킹

```yaml
filters:
  sensitiveData:
    enabled: true
    patterns:
      - regex: "password=\\w+"
        replace: "password=***"
      - regex: "token=[A-Za-z0-9]+"
        replace: "token=***"
      - regex: "\\d{4}-\\d{4}-\\d{4}-\\d{4}"
        replace: "****-****-****-****"
```

```java
logger.info("로그인: username=john, password=secret123");
// 출력: 로그인: username=john, password=***

logger.info("API 호출: token=abc123xyz456");
// 출력: API 호출: token=***

logger.info("카드번호: 1234-5678-9012-3456");
// 출력: 카드번호: ****-****-****-****
```

---

## FAQ

### Q1. 설정 파일 없이 사용할 수 있나요?

**A:** 네, 가능합니다. 설정 파일이 없으면 기본 설정(콘솔 출력, INFO 레벨)으로 자동 초기화됩니다.

```java
// 설정 파일 없이 사용
UniversalLogger.info("기본 설정으로 동작");
```

### Q2. Maven과 Gradle 중 어떤 것을 사용해야 하나요?

**A:** 프로젝트에서 사용 중인 빌드 도구를 그대로 사용하면 됩니다. Universal Logger는 둘 다 완벽하게 지원합니다.

### Q3. XML과 YAML 중 어떤 것을 선택해야 하나요?

**A:** 둘 다 동일하게 동작합니다:
- **XML**: Logback 표준 형식, 기존 Logback 사용자에게 친숙
- **YAML**: 더 간결하고 읽기 쉬움, 최소 설정 가능

### Q4. 기존 Log4j/Logback 코드와 함께 사용할 수 있나요?

**A:** 네, Universal Logger는 SLF4J를 기반으로 하므로 기존 로깅 코드와 함께 사용 가능합니다.

```java
// 기존 SLF4J 코드
Logger slf4jLogger = LoggerFactory.getLogger(MyClass.class);
slf4jLogger.info("기존 로그");

// Universal Logger
Logger universalLogger = UniversalLogger.getLogger("MyModule");
universalLogger.info("새 로그");
```

### Q5. 설정 파일 위치를 변경하려면?

**A:** `init()` 메서드로 명시적으로 경로를 지정하면 됩니다.

```java
// 사용자 정의 경로
UniversalLogger.init("C:/config/my-logging.yml");

// 클래스패스 리소스
UniversalLogger.init("classpath:logging.yml");

// 상대 경로
UniversalLogger.init("config/logging.yml");
```

### Q6. 프로덕션 환경과 개발 환경에서 다른 설정을 사용하려면?

**A:** 환경 변수나 프로파일에 따라 다른 설정 파일을 로드하면 됩니다.

```java
String env = System.getenv("APP_ENV");
String configFile = "dev".equals(env)
    ? "config/logging-dev.yml"
    : "config/logging-prod.yml";

UniversalLogger.init(configFile);
```

또는 Spring Boot 프로파일 활용:
```java
@Configuration
public class LoggingConfig {
    @Value("${spring.profiles.active:dev}")
    private String profile;

    @PostConstruct
    public void init() {
        String configFile = String.format("config/logging-%s.yml", profile);
        UniversalLogger.init(configFile);
    }
}
```

### Q7. 로그가 출력되지 않아요!

**A:** 다음 사항을 확인하세요:

1. **설정 파일 위치 확인**
   ```
   프로젝트 루트/
   ├── universal-logger.yml  (O)
   └── config/
       └── universal-logger.yml  (O)
   ```

2. **로그 레벨 확인**
   ```yaml
   levels:
     root: "INFO"  # DEBUG 로그는 출력 안 됨
   ```

3. **출력 활성화 확인**
   ```yaml
   outputs:
     console:
       enabled: true  # false면 출력 안 됨
   ```

4. **초기화 메시지 확인**
   ```
   [Universal Logger] Initialized with config: universal-logger.yml
   ```

### Q8. JAR 파일을 직접 빌드하려면?

**A:** 프로젝트 디렉토리에서 다음 명령 실행:

Maven:
```bash
mvn clean package
# 생성: target/universal-logger-1.0.0.jar
```

Gradle:
```bash
gradle clean build
# 생성: build/libs/universal-logger-1.0.0.jar
```

---

## 문제 해결

### 로그 파일이 생성되지 않음

```yaml
outputs:
  file:
    enabled: true
    path: "logs/{appName}.log"  # 경로 확인
```

로그 디렉토리 쓰기 권한 확인:
```bash
mkdir logs
chmod 755 logs
```

### 한글이 깨져 보임

```yaml
global:
  encoding: "UTF-8"  # 인코딩 설정 확인
```

### 성능 문제

비동기 로깅 활성화:
```yaml
performance:
  async: true
  bufferSize: 1000
  maxQueueSize: 10000
```

---

## 지원

- **GitHub Issues**: https://github.com/yourusername/universal-logger-java/issues
- **문서**: https://github.com/yourusername/universal-logger-java/wiki
- **이메일**: support@universallogger.com

---

**Universal Logger Java** - 한 번 설정하면 모든 프로젝트에서 바로 사용! 🚀