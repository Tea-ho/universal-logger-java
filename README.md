# Universal Logger for Java

Java 애플리케이션을 위한 범용 로깅 라이브러리. Logback과 Log4j2를 선택적으로 사용할 수 있으며, 설정 파일 하나로 콘솔, 파일, 웹훅, 이메일 로깅과 SQL 자동 캡처를 지원합니다.

## 주요 특징

- **멀티 로깅 프레임워크 지원**: Logback 또는 Log4j2 선택 가능
- **제로 설정**: Spring Boot에서 의존성 추가만으로 자동 활성화
- **SQL 자동 캡처**: MyBatis, JPA, Hibernate SQL을 자동으로 로깅
- **다양한 출력 지원**: 콘솔, 파일, 웹훅(Slack/Discord), 이메일
- **통합 설정**: YAML 파일 하나로 모든 로깅 프레임워크 설정
- **Spring/Spring Boot 완벽 통합**: Auto-Configuration 지원

## 프로젝트 구조

```
universal-logger-java/
├── universal-logger-core/          # 핵심 기능 및 공통 코드
│   ├── 설정 로더 (YAML/XML/JSON)
│   ├── SQL 캡처 프록시
│   └── 공통 유틸리티
├── universal-logger-logback/       # Logback 구현체
│   ├── Logback Configurator
│   ├── Custom Appenders
│   └── Spring/Spring Boot 통합
└── universal-logger-log4j2/        # Log4j2 구현체
    ├── Log4j2 ConfigurationFactory
    ├── Custom Appenders
    └── Spring/Spring Boot 통합
```

## 빠른 시작

### 1. 로깅 프레임워크 선택

Logback 또는 Log4j2 중 하나를 선택하여 의존성을 추가합니다.

#### 옵션 A: Logback 사용 (권장)

**Maven:**
```xml
<dependency>
    <groupId>com.universallogger</groupId>
    <artifactId>universal-logger-logback</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Gradle:**
```gradle
implementation 'com.universallogger:universal-logger-logback:1.0.0'
```

**로컬 JAR 사용:**
```xml
<dependency>
    <groupId>com.universallogger</groupId>
    <artifactId>universal-logger-logback</artifactId>
    <version>1.0.0</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/libs/universal-logger-logback-1.0.0.jar</systemPath>
</dependency>
<!-- Core 모듈도 필요 -->
<dependency>
    <groupId>com.universallogger</groupId>
    <artifactId>universal-logger-core</artifactId>
    <version>1.0.0</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/libs/universal-logger-core-1.0.0.jar</systemPath>
</dependency>
```

#### 옵션 B: Log4j2 사용

**Maven:**
```xml
<dependency>
    <groupId>com.universallogger</groupId>
    <artifactId>universal-logger-log4j2</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Gradle:**
```gradle
implementation 'com.universallogger:universal-logger-log4j2:1.0.0'
```

### 2. 설정 파일 (선택 사항)

`src/main/resources/config/universal-logger.yml`:
```yaml
version: "1.0"

global:
  appName: "MyApp"
  timezone: "Asia/Seoul"

levels:
  root: "INFO"
  com.myapp: "DEBUG"

sql:
  parameterSubstitution: true

outputs:
  console:
    enabled: true
    level: "INFO"

  file:
    enabled: true
    level: "DEBUG"
    path: "C:/log/{appName}/application.log"
    rotation:
      maxSize: "50MB"
      maxFiles: 30

  errorFile:
    enabled: true
    level: "ERROR"
    path: "C:/log/{appName}/error/error.log"
```

### 3. 사용하기

**SLF4J API 사용 (권장):**
```java
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserService {
    public void createUser(User user) {
        log.info("Creating user: {}", user.getName());
        log.debug("User details: {}", user);
        // SQL 쿼리가 자동으로 로깅됨!
    }
}
```

## 사용법

### Spring Boot (자동 설정)

Spring Boot 프로젝트에서는 **의존성만 추가하면 자동으로 활성화**됩니다.

**application.yml (Logback 사용 시):**
```yaml
spring:
  application:
    name: MyApp

universal-logger:
  enabled: true                                    # 기본값: true
  config-path: "config/universal-logger.yml"      # 기본값
  sql-capture:
    enabled: true                                  # SQL 자동 캡처 (기본값: true)
```

**application.yml (Log4j2 사용 시):**
```yaml
spring:
  application:
    name: MyApp

universal-logger-log4j2:
  enabled: true                                    # 기본값: true
  config-path: "config/universal-logger.yml"      # 기본값
  sql-capture:
    enabled: true                                  # SQL 자동 캡처 (기본값: true)
```

### 일반 Spring (수동 설정)

Spring Boot가 아닌 일반 Spring 환경에서는 Configuration 클래스를 임포트합니다.

**Logback 사용:**
```java
import com.universallogger.logback.spring.UniversalLoggerConfiguration;

@Configuration
@Import(UniversalLoggerConfiguration.class)
public class AppConfig {
    // LoggerConfig Bean이 자동 생성됨
    // DataSource가 자동으로 SQL 캡처 프록시로 래핑됨
}
```

**Log4j2 사용:**
```java
import com.universallogger.log4j2.spring.UniversalLoggerConfiguration;

@Configuration
@Import(UniversalLoggerConfiguration.class)
public class AppConfig {
    // LoggerConfig Bean이 자동 생성됨
    // DataSource가 자동으로 SQL 캡처 프록시로 래핑됨
}
```

**application.properties:**
```properties
spring.application.name=MyApp
# Logback
universal-logger.config-path=config/universal-logger.yml
universal-logger.sql-capture.enabled=true

# 또는 Log4j2
universal-logger-log4j2.config-path=config/universal-logger.yml
universal-logger-log4j2.sql-capture.enabled=true
```

### 순수 Java (비 Spring 환경)

Spring을 사용하지 않는 경우, 설정 파일만 준비하면 자동으로 로깅이 설정됩니다.

#### Logback 자동 설정

Logback은 ServiceLoader를 통해 자동으로 Universal Logger 설정을 로드합니다.

**설정 파일 위치** (자동 탐색):
1. `config/universal-logger.yml`
2. `universal-logger.yml`
3. `classpath:config/universal-logger.yml`
4. `classpath:universal-logger.yml`

```java
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MyApp {
    public static void main(String[] args) {
        // 설정 파일이 있으면 자동으로 로드됨
        log.info("Application started");

        // SQL 캡처를 위해서는 DataSource 수동 래핑 필요
        DataSource original = ...;
        DataSource wrapped = new SqlCaptureProxy(original);
    }
}
```

#### Log4j2 자동 설정

Log4j2는 ConfigurationFactory를 통해 자동으로 설정을 로드합니다.

```java
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MyApp {
    public static void main(String[] args) {
        // Log4j2가 시작 시 자동으로 Universal Logger 설정 로드
        log.info("Application started");
    }
}
```

## 설정 파일

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
  org.hibernate.SQL: "DEBUG"
  org.hibernate.type.descriptor.sql.BasicBinder: "TRACE"

sql:
  parameterSubstitution: true

outputs:
  # 콘솔 출력
  console:
    enabled: true
    level: "INFO"
    format: "[%d{yyyy-MM-dd HH:mm:ss}] [%-5level] [%thread] [%logger{50}:%line] %msg%n"
    colors:
      DEBUG: "cyan"
      INFO: "green"
      WARN: "yellow"
      ERROR: "red"

  # 일반 파일 로그
  file:
    enabled: true
    level: "DEBUG"
    path: "C:/log/{appName}/application.log"
    format: "[%d{yyyy-MM-dd HH:mm:ss}] [%-5level] [%thread] [%logger{50}:%line] %msg%n"
    rotation:
      maxSize: "50MB"
      maxFiles: 30
      compress: true

  # 에러 파일 로그
  errorFile:
    enabled: true
    level: "ERROR"
    path: "C:/log/{appName}/error/error.log"
    format: "[%d{yyyy-MM-dd HH:mm:ss}] [%-5level] [%thread] [%logger{50}:%line] %msg%n%ex"
    rotation:
      maxSize: "50MB"
      maxFiles: 30
      compress: true

  # 웹훅 알림 (Slack, Discord 등)
  webhook:
    enabled: false
    level: "ERROR"
    webhook:
      url: "https://hooks.slack.com/services/YOUR/WEBHOOK/URL"
      format: "🚨 *{level}* in {appName}\\n*Logger:* {logger}\\n*Message:* {message}"

  # 이메일 알림
  email:
    enabled: false
    level: "ERROR"
    email:
      smtp:
        host: "smtp.gmail.com"
        port: 587
        username: "your-email@gmail.com"
        password: "your-app-password"
      from: "alert@myapp.com"
      to:
        - "admin@example.com"
        - "dev-team@example.com"
      subject: "[{appName}] Error Alert"

# 민감 데이터 필터링
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

### 설정 파일 자동 탐색

설정 파일을 자동으로 다음 순서로 탐색합니다:

| 우선순위 | 경로 |
|---------|------|
| 1 | `config/universal-logger.yml` |
| 2 | `universal-logger.yml` |
| 3 | `config/universal-logger.xml` |
| 4 | `universal-logger.xml` |
| 5 | `config/universal-logger.json` |
| 6 | `classpath:config/universal-logger.yml` |
| 7 | `classpath:universal-logger.yml` |

### 설정 우선순위 (병합)

프로젝트 설정과 JAR 내장 기본값이 **병합**됩니다:

```
프로젝트 설정 파일 (config/universal-logger.yml)
        ↓ 병합 (merge)
JAR 내장 기본값 (defaults/universal-logger-defaults.yml)
```

프로젝트에서 일부 값만 설정하면, 나머지는 기본값이 사용됩니다.

## 주요 기능

### 1. SQL 자동 캡처

MyBatis, JPA, Hibernate 등에서 실행되는 SQL을 **자동으로 캡처**합니다.

**Spring Boot (자동):**
```yaml
# Logback
universal-logger:
  sql-capture:
    enabled: true  # 기본값

# Log4j2
universal-logger-log4j2:
  sql-capture:
    enabled: true  # 기본값
```

**순수 Java (수동):**
```java
import com.universallogger.sql.SqlCaptureProxy;
import com.universallogger.config.LoggerConfig;

// LoggerConfig 로드
LoggerConfig config = new UniversalConfigLoader().load("config/universal-logger.yml");

// DataSource 래핑
DataSource original = ...;
DataSource wrapped = new SqlCaptureProxy(original, config);
```

**로그 출력 예시:**
```
[2026-01-05 10:30:45] [INFO ] [main] [SQL:40] [SQL] SELECT * FROM users WHERE id = ?
[2026-01-05 10:30:45] [INFO ] [main] [SQL:45] [SQL-PARAMS] Parameters: [user-123]
[2026-01-05 10:30:45] [INFO ] [main] [SQL:108] [SQL-ROWS] Total: 1
[2026-01-05 10:30:45] [INFO ] [main] [SQL:50] [SQL-TIME] 15ms
```

**파라미터 치환 (보기 좋은 SQL):**
```yaml
sql:
  parameterSubstitution: true  # ? 대신 실제 값 표시
```

출력:
```sql
SELECT * FROM users WHERE id = 'user-123' AND status = 'ACTIVE'
```

### 2. 웹훅 알림 (Slack, Discord)

에러 발생 시 **즉시 알림**을 받을 수 있습니다.

**Slack 예시:**
```yaml
outputs:
  webhook:
    enabled: true
    level: "ERROR"
    webhook:
      url: "https://hooks.slack.com/services/T00000000/B00000000/XXXXXXXXXXXXXXXXXXXX"
      format: "🚨 *{level}* in {appName}\\n*Logger:* {logger}\\n*Message:* {message}"
```

**Discord 예시:**
```yaml
outputs:
  webhook:
    enabled: true
    level: "ERROR"
    webhook:
      url: "https://discord.com/api/webhooks/123456789/abcdefghijk"
      format: "🚨 **{level}** in {appName}\\n**Logger:** {logger}\\n**Message:** {message}"
```

### 3. 이메일 알림

심각한 에러를 **이메일로 전송**합니다.

```yaml
outputs:
  email:
    enabled: true
    level: "ERROR"
    email:
      smtp:
        host: "smtp.gmail.com"
        port: 587
        username: "alerts@myapp.com"
        password: "your-app-password"
      from: "noreply@myapp.com"
      to:
        - "admin@example.com"
      subject: "[{appName}] 에러 발생"
```

### 4. 다중 포맷 지원 (YAML, XML, JSON)

```yaml
# universal-logger.yml
version: "1.0"
global:
  appName: "MyApp"
```

```xml
<!-- universal-logger.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<loggerConfig>
    <version>1.0</version>
    <global>
        <appName>MyApp</appName>
    </global>
</loggerConfig>
```

```json
{
  "version": "1.0",
  "global": {
    "appName": "MyApp"
  }
}
```

## Logback vs Log4j2

### Logback 선택 시

**장점:**
- Spring Boot의 기본 로깅 프레임워크
- 안정적이고 검증된 구현
- 더 나은 문서화

**추천 상황:**
- Spring Boot 프로젝트
- 대부분의 일반 애플리케이션

### Log4j2 선택 시

**장점:**
- 더 빠른 성능 (비동기 로깅)
- 더 낮은 메모리 사용량
- 플러그인 아키텍처

**추천 상황:**
- 고성능이 중요한 애플리케이션
- 대량의 로그를 생성하는 시스템
- 레거시 Log4j2 프로젝트

## 로그 포맷 패턴

### 기본 패턴

```yaml
format: "[%d{yyyy-MM-dd HH:mm:ss}] [%-5level] [%thread] [%logger{50}:%line] %msg%n"
```

출력:
```
[2026-01-05 10:30:45] [INFO ] [main] [com.myapp.UserService:42] User created successfully
```

### 주요 패턴 변수

| 패턴 | 설명 | 예시 |
|------|------|------|
| `%d{패턴}` | 날짜/시간 | `%d{yyyy-MM-dd HH:mm:ss}` |
| `%-5level` | 로그 레벨 (왼쪽 정렬) | `INFO `, `ERROR` |
| `%thread` | 스레드 이름 | `main`, `http-nio-8080-exec-1` |
| `%logger{길이}` | 로거 이름 (축약) | `c.m.UserService` |
| `%logger{0}` | 클래스명만 | `UserService` |
| `%line` | 라인 번호 | `42` |
| `%msg` | 메시지 | `User created` |
| `%n` | 줄바꿈 | |
| `%ex` | 예외 스택 트레이스 | (전체) |

### 실전 패턴 예시

**심플:**
```yaml
format: "%d{HH:mm:ss} %-5level %logger{0} - %msg%n"
```

**상세:**
```yaml
format: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
```

**디버그용 (파일명, 라인 포함):**
```yaml
format: "%d{HH:mm:ss} %-5level [%thread] %logger{36}.%method:%line - %msg%n"
```

## API 사용 (고급)

### 설정 로드

```java
import com.universallogger.loaders.UniversalConfigLoader;
import com.universallogger.config.LoggerConfig;

// YAML 파일 로드
UniversalConfigLoader loader = new UniversalConfigLoader();
LoggerConfig config = loader.load("config/universal-logger.yml");

// 기본 설정 로드
LoggerConfig defaultConfig = loader.loadDefaultConfig();
```

### Builder 패턴으로 설정 생성

```java
import com.universallogger.config.LoggerConfigBuilder;

LoggerConfig config = LoggerConfigBuilder.create()
    .appName("my-app")
    .rootLevel("INFO")
    .level("com.myapp", "DEBUG")
    .console()
        .enabled(true)
        .level("INFO")
        .format("[%d{HH:mm:ss}] %-5level - %msg%n")
        .and()
    .file()
        .enabled(true)
        .path("logs/app.log")
        .maxSize("10MB")
        .maxFiles(30)
        .and()
    .build();
```

### SQL 캡처 프록시 (수동)

```java
import com.universallogger.sql.SqlCaptureProxy;

// DataSource 래핑
DataSource original = ...;
DataSource wrapped = new SqlCaptureProxy(original, loggerConfig);

// 사용
try (Connection conn = wrapped.getConnection()) {
    // SQL이 자동으로 로깅됨
}
```

## 문제 해결

### Spring Boot에서 로그가 출력되지 않음

**원인**: `application.yml`에서 로깅이 비활성화되었거나 설정 파일을 찾지 못함

**해결:**
```yaml
# Logback
universal-logger:
  enabled: true
  config-path: "config/universal-logger.yml"

# Log4j2
universal-logger-log4j2:
  enabled: true
  config-path: "config/universal-logger.yml"
```

### SQL이 캡처되지 않음

**원인**: SQL 캡처가 비활성화되었거나 DataSource가 래핑되지 않음

**해결:**
```yaml
# Spring Boot (자동)
universal-logger:
  sql-capture:
    enabled: true

# 순수 Java (수동 래핑 필요)
DataSource wrapped = new SqlCaptureProxy(original, config);
```

### 웹훅/이메일이 전송되지 않음

**원인**: 방화벽, 네트워크 문제 또는 잘못된 자격증명

**해결:**
1. 웹훅 URL 확인
2. 네트워크 연결 확인
3. SMTP 자격증명 확인
4. 방화벽 Outbound 규칙 확인

### 로그 파일이 생성되지 않음

**원인**: 디렉토리 권한 또는 경로 문제

**해결:**
1. 로그 디렉토리 권한 확인
2. 절대 경로 사용: `C:/log/{appName}/application.log`
3. `{appName}` 변수 확인

## 브랜치 정보

| 브랜치 | Java 버전 | 용도 |
|--------|----------|------|
| main | Java 8+ | 안정 버전 (단일 모듈) |
| feature/multi-module | Java 8+ | 멀티 모듈 구조 (개발 중) |
| java11 | Java 11+ | Java 11 최적화 |
| java8 | Java 8 | 레거시 지원 |

## 요구사항

**공통:**
- Java 8 이상
- SLF4J 1.7.36+
- Jackson 2.13.5+ (YAML 파싱)

**Logback 모듈:**
- Logback 1.2.12+

**Log4j2 모듈:**
- Log4j2 2.17.1+
- Apache HttpClient 4.5+ (Webhook 기능)
- JavaMail 1.6+ (Email 기능)

**Spring 통합 (선택):**
- Spring Framework 5.0+
- Spring Boot 2.0+ (Auto-Configuration)

## 라이센스

MIT License

## 관련 문서

- [아키텍처 가이드](ARCHITECTURE.md) - 프로젝트 구조 및 설계
- [사용자 가이드](USER_GUIDE.md) - 상세 사용법 및 예제

## 기여

이슈 및 PR은 언제든 환영합니다!

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request
