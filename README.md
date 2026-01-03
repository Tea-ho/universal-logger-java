# Universal Logger for Java

Java 애플리케이션을 위한 범용 로깅 라이브러리. 설정 파일 하나로 콘솔, 파일, 웹훅, 이메일 로깅과 SQL 자동 캡처를 지원합니다.

## 주요 특징

- **제로 설정**: Spring Boot에서 의존성 추가만으로 자동 활성화
- **SQL 자동 캡처**: MyBatis, JPA, Hibernate SQL을 자동으로 로깅
- **다양한 출력 지원**: 콘솔, 파일, 웹훅, 이메일
- **다중 포맷 지원**: YAML, XML, JSON 설정 파일
- **Logback 프로그래밍 방식 설정**: 코드나 YAML로 Logback 완전 제어

## 빠른 시작

### 1. Maven/Gradle 설치

**Maven:**
```xml
<dependency>
    <groupId>com.universallogger</groupId>
    <artifactId>universal-logger</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Gradle:**
```gradle
implementation 'com.universallogger:universal-logger:1.0.0'
```

**로컬 JAR 사용 (Maven):**
```xml
<dependency>
    <groupId>com.universallogger</groupId>
    <artifactId>universal-logger</artifactId>
    <version>1.0.0</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/libs/universal-logger-1.0.0.jar</systemPath>
</dependency>
```

### 2. 설정 파일 (선택 사항)

`src/main/resources/universal-logger.yml`:
```yaml
version: "1.0"

global:
  appName: "MyApp"

outputs:
  console:
    enabled: true
    level: "INFO"

  file:
    enabled: true
    path: "logs/{appName}.log"
```

### 3. 사용하기

**로깅은 @Slf4j 사용:**
```java
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MyService {
    public void doSomething() {
        log.info("Hello World!");
        log.debug("Debug message: {}", value);
        log.error("Error occurred", exception);
    }
}
```

**UniversalLogger는 설정 초기화만:**
```java
import com.universallogger.UniversalLogger;

// 설정 파일 지정 (선택 사항, 자동 탐색됨)
UniversalLogger.init("config/logging.yml");
```

---

## 사용법

### 방법 1: 제로 설정 (Spring Boot)

Spring Boot 프로젝트에서는 **의존성만 추가하면 자동으로 활성화**됩니다.

**application.yml (선택 사항):**
```yaml
spring:
  application:
    name: MyApp

universal-logger:
  enabled: true  # 기본값: true
  config-path: "config/universal-logger.yml"  # 기본값: config/universal-logger.yml
  sql-capture:
    enabled: true  # 기본값: true (SQL 자동 캡처)
```

**로깅은 @Slf4j 사용:**
```java
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserService {
    public void createUser(User user) {
        log.info("Creating user: {}", user.getName());
        // SQL 쿼리가 자동으로 로깅됨!
    }
}
```

### 방법 2: 설정만 초기화 (일반 Java)

설정 파일을 명시적으로 지정할 때 사용합니다.

```java
import com.universallogger.UniversalLogger;
import lombok.extern.slf4j.Slf4j;

// 초기화
UniversalLogger.init("logging.yml");

@Slf4j
public class MyService {
    public void process() {
        log.info("Processing request: {}", requestId);
    }
}
```

### 방법 3: 일반 Spring 프로젝트

Spring Boot가 아닌 일반 Spring 환경에서는 수동으로 설정을 임포트합니다.

```java
import com.universallogger.spring.UniversalLoggerConfiguration;
import lombok.extern.slf4j.Slf4j;

// Spring에서 Bean으로 등록
@Configuration
@Import(UniversalLoggerConfiguration.class)  // 일반 Spring용 자동 설정
public class AppConfig {
    // LoggerInstance Bean이 자동 생성됨
}

@Slf4j
@Service
public class MyService {
    public void process() {
        log.info("Message from service");
    }
}
```

### 방법 4: Builder 패턴 (코드로 설정)

설정 파일 없이 코드에서 직접 Logback을 설정할 때 사용합니다.

```java
import com.universallogger.LoggerInstance;
import com.universallogger.config.LoggerConfigBuilder;
import com.universallogger.config.LoggerConfig;
import lombok.extern.slf4j.Slf4j;

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

// Logback 설정 적용
LoggerInstance.builder()
    .config(config)
    .build();

// 로깅은 @Slf4j 사용
@Slf4j
public class MyApp {
    public static void main(String[] args) {
        log.info("Application started");
    }
}
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

## 로그 포맷 패턴 가이드

Universal Logger는 **Logback 패턴**을 직접 사용합니다. 사용자 정의 패턴을 자유롭게 설정할 수 있습니다.

### 기본 패턴 예시

```yaml
outputs:
  console:
    format: "%d{yyyy-MM-dd HH:mm:ss} %highlight(%-5level) %cyan([%thread]) %boldMagenta(%logger{50}) %msg%n"

  file:
    format: "%d{yyyy-MM-dd HH:mm:ss} %-5level [%thread] %logger{50} %msg%n"
```

### 주요 패턴 변수

| 패턴 | 설명 | 예시 |
|------|------|------|
| `%d{날짜포맷}` | 타임스탬프 | `%d{yyyy-MM-dd HH:mm:ss.SSS}` |
| `%-5level` | 로그 레벨 (왼쪽 정렬, 5자리) | `INFO `, `ERROR` |
| `%thread` | 스레드 이름 | `main`, `http-nio-8080-exec-1` |
| `%logger{길이}` | 로거 이름 (최대 길이 지정) | `c.u.MyService` (50자로 축약) |
| `%logger{0}` | 로거 이름 (짧은 형식) | `MyService` |
| `%logger` | 로거 전체 이름 | `com.universallogger.MyService` |
| `%class` | 클래스 이름 | `com.example.MyClass` |
| `%method` | 메서드 이름 | `processRequest` |
| `%line` | 라인 번호 | `42` |
| `%msg` | 로그 메시지 | `User logged in` |
| `%n` | 줄바꿈 | (개행 문자) |
| `%ex` | 예외 스택 트레이스 | (전체 스택 트레이스) |
| `%ex{short}` | 예외 (짧은 형식) | (첫 줄만) |

### 컬러 지원 (콘솔 전용)

| 패턴 | 설명 | 사용 예시 |
|------|------|-----------|
| `%black(...)` | 검정 | `%black(%msg)` |
| `%red(...)` | 빨강 | `%red(%level)` |
| `%green(...)` | 초록 | `%green(%msg)` |
| `%yellow(...)` | 노랑 | `%yellow(%logger)` |
| `%blue(...)` | 파랑 | `%blue(%thread)` |
| `%magenta(...)` | 마젠타 | `%magenta(%class)` |
| `%cyan(...)` | 시안 | `%cyan(%method)` |
| `%white(...)` | 흰색 | `%white(%msg)` |
| `%boldRed(...)` | 굵은 빨강 | `%boldRed(%level)` |
| `%boldMagenta(...)` | 굵은 마젠타 | `%boldMagenta(%logger)` |
| `%highlight(...)` | 레벨별 자동 색상 | `%highlight(%-5level)` |

### 실전 패턴 예시

#### 1. 심플 패턴
```yaml
format: "%d{HH:mm:ss} %-5level %logger{0} - %msg%n"
```
출력:
```
14:23:45 INFO  MyService - User logged in
```

#### 2. 상세 패턴
```yaml
format: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
```
출력:
```
2025-01-15 14:23:45.123 [http-nio-8080-exec-1] INFO  c.u.MyService - User logged in
```

#### 3. 디버그용 패턴 (클래스명, 메서드, 라인 포함)
```yaml
format: "%d{HH:mm:ss} %-5level %logger.%method:%line - %msg%n"
```
출력:
```
14:23:45 DEBUG MyService.processRequest:42 - Processing user: 123
```

#### 4. 컬러풀 콘솔 패턴
```yaml
format: "%d{HH:mm:ss} %highlight(%-5level) %cyan([%thread]) %boldMagenta(%logger{30}) - %msg%n"
```
출력 (색상 적용):
```
14:23:45 INFO  [main] MyService - Application started
```

#### 5. 프로덕션 파일 패턴
```yaml
format: "%d{yyyy-MM-dd HH:mm:ss} | %-5level | [%thread] | %logger{50} | %msg%n"
```
출력:
```
2025-01-15 14:23:45 | INFO  | [main] | com.universallogger.MyService | Application started
```

#### 6. JSON 스타일 패턴
```yaml
format: '{"timestamp":"%d{yyyy-MM-dd HH:mm:ss}","level":"%-5level","thread":"%thread","logger":"%logger","message":"%msg"}%n'
```
출력:
```json
{"timestamp":"2025-01-15 14:23:45","level":"INFO ","thread":"main","logger":"MyService","message":"User logged in"}
```

### 날짜 포맷 커스터마이징

| 패턴 | 출력 예시 |
|------|-----------|
| `%d{yyyy-MM-dd HH:mm:ss}` | `2025-01-15 14:23:45` |
| `%d{yyyy-MM-dd HH:mm:ss.SSS}` | `2025-01-15 14:23:45.123` |
| `%d{HH:mm:ss}` | `14:23:45` |
| `%d{HH:mm:ss.SSS}` | `14:23:45.123` |
| `%d{yyyy/MM/dd}` | `2025/01/15` |
| `%d{ISO8601}` | `2025-01-15T14:23:45,123+0900` |

### MDC (Mapped Diagnostic Context) 사용

애플리케이션 컨텍스트 정보를 로그에 추가할 수 있습니다.

```java
// MDC에 값 설정
import org.slf4j.MDC;

MDC.put("userId", "user123");
MDC.put("requestId", "req-456");
logger.info("Processing request");
MDC.clear();
```

```yaml
# 패턴에 MDC 변수 사용
format: "%d{HH:mm:ss} [%X{requestId}] [%X{userId}] %-5level %logger - %msg%n"
```

출력:
```
14:23:45 [req-456] [user123] INFO  MyService - Processing request
```

### 로거 이름 길이 조정

| 패턴 | 입력 | 출력 |
|------|------|------|
| `%logger` | `com.example.service.UserService` | `com.example.service.UserService` (전체) |
| `%logger{0}` | `com.example.service.UserService` | `UserService` (클래스명만) |
| `%logger{10}` | `com.example.service.UserService` | `c.e.s.UserService` (10자로 축약) |
| `%logger{30}` | `com.example.service.UserService` | `c.e.service.UserService` (30자로 축약) |

### 참고 자료

Logback 공식 문서: https://logback.qos.ch/manual/layouts.html#conversionWord

---

## 주요 기능

### 1. SQL 자동 캡처

MyBatis, JPA, Hibernate 등에서 실행되는 SQL을 **자동으로 캡처**합니다.

**Spring Boot (자동 활성화):**
```yaml
universal-logger:
  sql-capture:
    enabled: true  # 기본값: true
```

**일반 Java (수동 설정):**
```java
import com.universallogger.sql.SqlCaptureProxy;

// DataSource 래핑
DataSource originalDataSource = ...;
DataSource loggingDataSource = new SqlCaptureProxy(originalDataSource);
```

**로그 출력 예시:**
```
2025-01-15 10:30:45 | INFO | [SQL] SELECT * FROM users WHERE id = ?
2025-01-15 10:30:45 | INFO | [SQL-PARAMS] Parameters: [123]
2025-01-15 10:30:45 | INFO | [SQL-RESULT] Rows affected: 1
2025-01-15 10:30:45 | INFO | [SQL-TIME] Execution time: 15ms
```

**지원 프레임워크:**
- MyBatis
- JPA (Hibernate, EclipseLink)
- Spring JDBC
- 순수 JDBC

### 2. 다중 포맷 지원 (YAML, XML, JSON)

설정 파일을 **YAML, XML, JSON** 형식으로 작성할 수 있습니다.

**자동 탐색 순서:**
```
1. universal-logger.yml
2. config/universal-logger.yml
3. universal-logger.xml
4. config/universal-logger.xml
5. universal-logger.json
6. classpath:universal-logger.yml
```

**XML 예시:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<loggerConfig>
    <global>
        <appName>MyApp</appName>
    </global>
    <outputs>
        <console enabled="true" level="INFO"/>
        <file enabled="true" path="logs/app.log"/>
    </outputs>
</loggerConfig>
```

### 3. 웹훅 알림 (Slack, Discord 등)

에러 발생 시 **Slack, Discord, Teams**로 즉시 알림을 받을 수 있습니다.

```yaml
outputs:
  webhook:
    enabled: true
    level: "ERROR"
    webhook:
      url: "https://hooks.slack.com/services/YOUR/WEBHOOK/URL"
      format: "json"
```

---

## Spring Boot 통합

### 자동 설정

Spring Boot 프로젝트에서는 **의존성만 추가하면 자동으로 활성화**됩니다.

**기능:**
- `LoggerInstance` Bean 자동 생성
- DataSource 자동 래핑 (SQL 캡처)
- application.yml 설정 지원

**application.yml 설정:**
```yaml
spring:
  application:
    name: MyApp

universal-logger:
  enabled: true  # UniversalLogger 활성화 (기본값: true)
  config-path: "config/universal-logger.yml"  # 설정 파일 경로
  sql-capture:
    enabled: true  # SQL 자동 캡처 (기본값: true)
```

### 일반 Spring 프로젝트

Spring Boot가 아닌 일반 Spring에서는 `UniversalLoggerConfiguration`을 임포트합니다.

```java
@Configuration
@Import(UniversalLoggerConfiguration.class)
public class AppConfig {
    // LoggerInstance Bean이 자동 생성됨
}
```

**application.properties:**
```properties
spring.application.name=MyApp
universal-logger.config-path=config/universal-logger.yml
universal-logger.sql-capture.enabled=true
```

---

## API 레퍼런스

### UniversalLogger (Static API)

Logback 설정을 초기화하는 정적 API입니다.

| 메서드 | 설명 |
|--------|------|
| `init(String configPath)` | 설정 파일로 Logback 초기화 |
| `init(LoggerConfig config)` | 설정 객체로 Logback 초기화 |
| `getInstance()` | 싱글톤 LoggerInstance 반환 |
| `createInstance(String configPath)` | 새 LoggerInstance 생성 (독립 인스턴스) |
| `createInstance(LoggerConfig config)` | 새 LoggerInstance 생성 |
| `getLogger(String name)` | SLF4J Logger 반환 |
| `getLogger(Class<?> clazz)` | SLF4J Logger 반환 |
| `isInitialized()` | 초기화 상태 확인 |
| `reset()` | 싱글톤 리셋 (테스트용) |

### LoggerInstance (Instance API)

인스턴스 기반 API로 DI 및 멀티 인스턴스를 지원합니다.

| 메서드 | 설명 |
|--------|------|
| `builder()` | Builder 패턴으로 생성 |
| `getLogger(String name)` | SLF4J Logger 반환 |
| `getLogger(Class<?> clazz)` | SLF4J Logger 반환 |
| `getConfig()` | 현재 설정 반환 |
| `isInitialized()` | 초기화 상태 확인 |

### LoggerInstance.Builder

| 메서드 | 설명 |
|--------|------|
| `configPath(String path)` | 설정 파일 경로 지정 |
| `config(LoggerConfig config)` | 설정 객체 직접 지정 |
| `appenderFactory(AppenderFactory)` | 커스텀 Appender Factory (DI/테스트) |
| `configLoader(ConfigLoader)` | 커스텀 Config Loader (DI/테스트) |
| `build()` | LoggerInstance 생성 |

### SqlCaptureProxy

DataSource를 래핑하여 SQL을 자동 캡처합니다.

```java
// Static API 사용 (UniversalLogger 싱글톤)
DataSource wrapped = new SqlCaptureProxy(originalDataSource);

// 인스턴스 기반 (LoggerInstance 주입)
DataSource wrapped = new SqlCaptureProxy(originalDataSource, loggerInstance);
```

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
