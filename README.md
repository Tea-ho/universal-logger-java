# Universal Logger for Java

Java 애플리케이션을 위한 범용 로깅 라이브러리. 설정 파일 하나로 콘솔, 파일, DB, 웹훅, 이메일 로깅을 지원합니다.

## 빠른 시작

### 1. JAR 설치

```bash
# 빌드
./gradlew shadowJar

# JAR 복사
cp build/libs/universal-logger-1.0.0.jar your-project/libs/
```

### 2. 의존성 추가

**Gradle:**
```gradle
implementation files('libs/universal-logger-1.0.0.jar')
```

**Maven:**
```xml
<dependency>
    <groupId>com.universallogger</groupId>
    <artifactId>universal-logger</artifactId>
    <version>1.0.0</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/libs/universal-logger-1.0.0.jar</systemPath>
</dependency>
```

### 3. 기본 사용

```java
import com.universallogger.UniversalLogger;

// 설정 파일로 초기화
UniversalLogger.init("logging.yml");

// 로깅
UniversalLogger.info("Hello World!");
UniversalLogger.debug("Debug message: {}", value);
UniversalLogger.error("Error occurred", exception);
```

---

## 사용법

### 방법 1: Static API (기존 방식)

가장 간단한 방법. 기존 코드와 100% 호환됩니다.

```java
import com.universallogger.UniversalLogger;
import org.slf4j.Logger;

// 초기화
UniversalLogger.init("logging.yml");

// 직접 로깅
UniversalLogger.info("Application started");
UniversalLogger.error("Error: {}", errorMsg);

// SLF4J Logger 사용
Logger logger = UniversalLogger.getLogger("MyService");
logger.info("Processing request: {}", requestId);
```

### 방법 2: 인스턴스 기반 (DI 지원)

테스트와 DI(의존성 주입)가 필요한 경우 사용합니다.

```java
import com.universallogger.LoggerInstance;
import com.universallogger.UniversalLogger;

// 새 인스턴스 생성
LoggerInstance logger = UniversalLogger.createInstance("logging.yml");

// 로깅
logger.info("Message from instance");

// Spring에서 Bean으로 등록
@Configuration
public class LoggerConfig {
    @Bean
    public LoggerInstance logger() {
        return UniversalLogger.createInstance("logging.yml");
    }
}
```

### 방법 3: Builder 패턴 (코드로 설정)

설정 파일 없이 코드에서 직접 설정할 때 사용합니다.

```java
import com.universallogger.LoggerInstance;
import com.universallogger.config.LoggerConfigBuilder;
import com.universallogger.config.LoggerConfig;

// Builder로 설정 생성
LoggerConfig config = LoggerConfigBuilder.create()
    .appName("my-app")
    .rootLevel("DEBUG")
    .console()
        .enabled(true)
        .level("INFO")
        .format("[%d{HH:mm:ss}] %-5level %logger - %msg%n")
        .and()
    .file()
        .enabled(true)
        .path("logs/app.log")
        .maxSize("10MB")
        .maxFiles(30)
        .and()
    .build();

// 인스턴스 생성
LoggerInstance logger = LoggerInstance.builder()
    .config(config)
    .build();

logger.info("Application started");
```

---

## 설정 파일

### 기본 설정 (logging.yml)

```yaml
version: "1.0"

global:
  appName: "MyApp"
  timezone: "Asia/Seoul"

levels:
  root: "INFO"
  com.myapp: "DEBUG"

outputs:
  console:
    enabled: true
    level: "INFO"
    format: "[%d{yyyy-MM-dd HH:mm:ss}] %-5level %logger{36} - %msg%n"
    colors:
      DEBUG: "cyan"
      INFO: "green"
      WARN: "yellow"
      ERROR: "red"

  file:
    enabled: true
    level: "DEBUG"
    path: "logs/{appName}.log"
    format: "%d{yyyy-MM-dd HH:mm:ss} | %-5level | %logger | %msg%n"
    rotation:
      maxSize: "10MB"
      maxFiles: 30
      compress: true

  error_file:
    enabled: true
    level: "ERROR"
    path: "logs/{appName}-error.log"
```

### 전체 설정 예시

```yaml
version: "1.0"

global:
  appName: "MyApp"
  timezone: "Asia/Seoul"
  encoding: "UTF-8"

levels:
  root: "INFO"
  com.myapp.service: "DEBUG"
  com.myapp.repository: "DEBUG"
  org.hibernate: "WARN"

outputs:
  console:
    enabled: true
    level: "INFO"
    format: "[%d{HH:mm:ss}] %-5level %logger{36} - %msg%n"
    colors:
      DEBUG: "cyan"
      INFO: "green"
      WARN: "yellow"
      ERROR: "red"

  file:
    enabled: true
    level: "DEBUG"
    path: "logs/{appName}.log"
    format: "%d{yyyy-MM-dd HH:mm:ss} | %-5level | %logger | %msg%n"
    rotation:
      maxSize: "20MB"
      maxFiles: 60
      compress: true

  error_file:
    enabled: true
    level: "ERROR"
    path: "logs/{appName}-error.log"

  webhook:
    enabled: false
    level: "ERROR"
    webhook:
      url: "https://hooks.slack.com/services/YOUR/WEBHOOK/URL"
      format: "json"

  email:
    enabled: false
    level: "ERROR"
    email:
      smtp:
        host: "smtp.gmail.com"
        port: 587
        username: "your-email@gmail.com"
        password: "app-password"
      to:
        - "admin@example.com"
      subject: "[{appName}] Error Alert"

  database:
    enabled: false
    level: "INFO"
    database:
      type: "mysql"      # mysql, oracle, postgresql, mssql, h2
      host: "localhost"
      port: 3306
      database: "logs"
      table: "app_logs"
      username: "root"
      password: "password"

filters:
  sensitiveData:
    enabled: true
    patterns:
      - regex: "password=\\w+"
        replace: "password=***"
      - regex: "\\d{4}-\\d{4}-\\d{4}-\\d{4}"
        replace: "****-****-****-****"

performance:
  async: false
  bufferSize: 8192
  flushInterval: 1000
```

### 설정 파일 자동 탐색

`init()` 호출 없이 바로 로깅 메서드를 사용하면 다음 순서로 설정 파일을 자동 탐색합니다:

| 우선순위 | 경로 |
|---------|------|
| 1 | `universal-logger.yml` |
| 2 | `config/universal-logger.yml` |
| 3 | `universal-logger.xml` |
| 4 | `config/universal-logger.xml` |
| 5 | `logback.xml` |
| 6 | `config/logback.xml` |
| 7 | `classpath:universal-logger.yml` |
| 8 | `classpath:logback.xml` |

### 설정 우선순위

프로젝트 설정 파일과 JAR 내장 기본값이 병합됩니다. **프로젝트 설정이 우선 적용**됩니다.

```
프로젝트 설정 파일 (universal-logger.yml)
        ↓ 병합 (merge)
JAR 내장 기본값 (defaults/universal-logger-defaults.yml)
```

**예시:**

JAR 기본값:
```yaml
levels:
  root: "INFO"
outputs:
  console:
    enabled: true
    level: "INFO"
  file:
    enabled: true
    path: "logs/{appName}.log"
```

프로젝트 설정:
```yaml
levels:
  root: "DEBUG"          # ← 프로젝트 값 적용
outputs:
  console:
    level: "WARN"        # ← 프로젝트 값 적용
  # file 설정 생략       → JAR 기본값 사용
```

결과:
- `root` 레벨: `DEBUG` (프로젝트 설정)
- `console.level`: `WARN` (프로젝트 설정)
- `file` 설정: JAR 기본값 그대로 사용

---

## 주요 기능

### SQL 자동 캡처

MyBatis, JPA, Hibernate 등에서 실행되는 SQL을 자동으로 캡처합니다.

```
2025-01-15 10:30:45 | INFO | UserService.findUser:25 | [SQL] SELECT * FROM users WHERE id = ? | Parameters: {1=123}
2025-01-15 10:30:45 | INFO | UserService.findUser:25 | [SQL-TIME] 15ms
```

### 다중 데이터베이스 지원

MySQL, Oracle, PostgreSQL, MSSQL, H2를 지원합니다.

```yaml
database:
  type: "postgresql"  # mysql, oracle, postgresql, mssql, h2
  host: "localhost"
  port: 5432
  database: "logs"
```

### 민감 정보 마스킹

```yaml
filters:
  sensitiveData:
    enabled: true
    patterns:
      - regex: "password=\\w+"
        replace: "password=***"
```

### 웹훅 알림 (Slack, Discord 등)

```yaml
webhook:
  enabled: true
  level: "ERROR"
  webhook:
    url: "https://hooks.slack.com/services/..."
    format: "json"
```

---

## API 레퍼런스

### UniversalLogger (Static API)

| 메서드 | 설명 |
|--------|------|
| `init(String configPath)` | 설정 파일로 초기화 |
| `init(LoggerConfig config)` | 설정 객체로 초기화 |
| `getInstance()` | 싱글톤 LoggerInstance 반환 |
| `createInstance(String configPath)` | 새 LoggerInstance 생성 |
| `createInstance(LoggerConfig config)` | 새 LoggerInstance 생성 |
| `getLogger(String name)` | SLF4J Logger 반환 |
| `getLogger(Class<?> clazz)` | SLF4J Logger 반환 |
| `trace/debug/info/warn/error(...)` | 로깅 메서드 |
| `isInitialized()` | 초기화 상태 확인 |
| `reset()` | 싱글톤 리셋 (테스트용) |

### LoggerInstance (Instance API)

| 메서드 | 설명 |
|--------|------|
| `builder()` | Builder 생성 |
| `getLogger(String name)` | SLF4J Logger 반환 |
| `trace/debug/info/warn/error(...)` | 로깅 메서드 |
| `getConfig()` | 현재 설정 반환 |
| `isInitialized()` | 초기화 상태 확인 |

### LoggerInstance.Builder

| 메서드 | 설명 |
|--------|------|
| `configPath(String path)` | 설정 파일 경로 |
| `config(LoggerConfig config)` | 설정 객체 |
| `appenderFactory(AppenderFactory factory)` | 커스텀 Factory (DI/테스트용) |
| `build()` | LoggerInstance 생성 |

### LoggerConfigBuilder

```java
LoggerConfigBuilder.create()
    .appName("app-name")
    .rootLevel("INFO")
    .level("com.myapp", "DEBUG")
    .console().enabled(true).level("INFO").format("...").and()
    .file().enabled(true).path("logs/app.log").maxSize("10MB").and()
    .errorFile().enabled(true).level("ERROR").and()
    .webhook().enabled(true).url("...").and()
    .email().enabled(true).smtpHost("...").to("admin@...").and()
    .database().enabled(true).type("mysql").host("localhost").and()
    .async(true)
    .sensitiveDataFilter(true)
    .build();
```

---

## 브랜치 정보

| 브랜치 | Java 버전 | 용도 |
|--------|----------|------|
| main | Java 8+ | 안정 버전 |
| java11 | Java 11+ | Java 11 최적화 |
| java8 | Java 8 | 레거시 지원 |

---

## 요구사항

- Java 8 이상
- SLF4J 1.7.36+
- Logback 1.2.12+
- Jackson 2.13.5+ (YAML 설정 사용 시)

---

## 라이센스

MIT License
