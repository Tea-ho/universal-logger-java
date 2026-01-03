# Universal Logger Java - 아키텍처 문서

## 설계 원칙

Universal Logger는 다음 원칙을 기반으로 설계되었습니다:

### 1. 관심사의 분리 (Separation of Concerns)
- **계층별 책임 분리**: API, 설정, 출력, SQL 캡처 계층 독립
- **인터페이스 기반 설계**: 확장성과 유지보수성 확보
- **의존성 주입 지원**: Spring DI 및 테스트 용이성 확보

### 2. Zero Configuration (제로 설정)
- **Spring Boot Auto Configuration**: 의존성 추가만으로 자동 활성화
- **설정 파일 자동 탐색**: 8가지 경로에서 자동으로 설정 파일 검색
- **다중 포맷 지원**: YAML, XML, JSON 자동 감지 및 파싱

### 3. Zero Hard-code
- **상수 중앙 관리**: `LoggerConstants` 클래스
- **설정 기반 동작**: 모든 값은 설정 파일에서 로드
- **기본값 병합**: JAR 내장 기본값과 사용자 설정 자동 병합

---

## 아키텍처 개요

```
┌──────────────────────────────────────────────────────────────────┐
│                      Application Layer                           │
│              (User Code / Spring Boot / Spring)                  │
└────────────────────────────┬─────────────────────────────────────┘
                             │
            ┌────────────────┼────────────────┐
            │                │                │
┌───────────▼──────┐  ┌──────▼──────┐  ┌─────▼────────────┐
│  Spring Boot     │  │   Spring    │  │   Static API     │
│  Integration     │  │ Integration │  │  UniversalLogger │
│                  │  │             │  │                  │
│ - Auto Config    │  │ - Manual    │  │ - init()         │
│ - Bean Registry  │  │   Config    │  │ - getLogger()    │
│ - DataSource     │  │ - @Import   │  │ - info/debug/... │
│   Auto Wrap      │  │             │  │                  │
└───────────┬──────┘  └──────┬──────┘  └─────┬────────────┘
            │                │                │
            └────────────────┼────────────────┘
                             │
            ┌────────────────▼─────────────────┐
            │      Core Layer (LoggerInstance) │
            │                                  │
            │  - Multi-instance Support        │
            │  - DI-friendly                   │
            │  - Configuration Management      │
            └────────────────┬─────────────────┘
                             │
          ┌──────────────────┼──────────────────┐
          │                  │                  │
┌─────────▼──────┐  ┌────────▼────────┐  ┌─────▼──────────┐
│ Configuration  │  │ SQL Capture     │  │ Output Layer   │
│ Layer          │  │ Layer           │  │                │
│                │  │                 │  │ - Appenders    │
│ - ConfigLoader │  │ - Proxy Pattern │  │ - Formatters   │
│ - Parsers      │  │ - Connection    │  │ - Filters      │
│   • YAML       │  │   Wrapping      │  │                │
│   • XML        │  │ - Statement     │  └────┬───────────┘
│   • JSON       │  │   Interception  │       │
│ - Validator    │  │ - Parameter     │       │
│ - Defaults     │  │   Logging       │       │
│   Merger       │  │                 │       │
└────────────────┘  └─────────────────┘       │
                                               │
                              ┌────────────────┼────────────┐
                              │                │            │
                        ┌─────▼────┐     ┌────▼────┐  ┌───▼──────┐
                        │ Console  │     │  File   │  │ Webhook  │
                        │ Appender │     │Appender │  │ Appender │
                        └──────────┘     └─────────┘  └──────────┘
                                                  (+ Email Appender)
```

---

## 계층별 상세 설명

### 1. Spring Integration Layer (Spring 통합 계층)

#### 1.1 Spring Boot Auto Configuration

**클래스**: `UniversalLoggerAutoConfiguration`

**책임**:
- Spring Boot 프로젝트에서 자동 활성화
- `LoggerInstance` Bean 자동 생성
- `DataSource` 자동 래핑 (SQL 캡처)

**주요 기능**:
```java
@AutoConfiguration
@ConditionalOnClass({LoggerInstance.class, DataSource.class})
@ConditionalOnProperty(prefix = "universal-logger", name = "enabled",
                       havingValue = "true", matchIfMissing = true)
public class UniversalLoggerAutoConfiguration {

    @Bean
    public LoggerInstance loggerInstance() {
        // application.yml 설정 기반으로 LoggerInstance 생성
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        // DataSource를 SqlCaptureProxy로 자동 래핑
    }
}
```

**설정**:
```yaml
universal-logger:
  enabled: true
  config-path: "config/universal-logger.yml"
  sql-capture:
    enabled: true
```

#### 1.2 Spring Configuration (일반 Spring용)

**클래스**: `UniversalLoggerConfiguration`

**책임**:
- Spring Boot가 아닌 일반 Spring 프로젝트 지원
- `@Import`로 수동 임포트 필요

**사용법**:
```java
@Configuration
@Import(UniversalLoggerConfiguration.class)
public class AppConfig {
    // LoggerInstance Bean 자동 생성
}
```

### 2. Static API Layer (정적 API 계층)

**클래스**: `UniversalLogger`

**책임**:
- Logback 프로그래밍 방식 설정 초기화
- 싱글톤 패턴으로 간단한 API 제공
- 설정 파일 자동 탐색

**주요 메서드**:
```java
// 자동 초기화 (설정 파일 자동 탐색)
public static void autoInit()

// 수동 초기화 (Logback 설정 적용)
public static void init(String configPath)
public static void init(LoggerConfig config)

// 싱글톤 인스턴스 반환
public static LoggerInstance getInstance()

// 새 인스턴스 생성 (멀티 인스턴스)
public static LoggerInstance createInstance(String configPath)

// SLF4J 로거 반환 (설정 조회용)
public static Logger getLogger(String name)
public static Logger getLogger(Class<?> clazz)
```

**디자인 패턴**:
- **Facade 패턴**: 복잡한 Logback 설정을 단순한 인터페이스로 제공
- **Singleton 패턴**: Bill Pugh Singleton Holder (Thread-safe)

**사용 예시**:
```java
// Logback 설정 초기화
UniversalLogger.init("logging.yml");

// 로깅은 @Slf4j 사용
@Slf4j
public class MyService {
    public void process() {
        log.info("Processing...");
    }
}
```

### 3. Core Layer (핵심 계층)

**클래스**: `LoggerInstance`

**책임**:
- Logback 설정 관리 및 초기화
- 멀티 인스턴스 지원
- DI 친화적 설계

**주요 메서드**:
```java
// Builder 패턴으로 생성
public static Builder builder()

public static class Builder {
    public Builder configPath(String path)
    public Builder config(LoggerConfig config)
    public Builder appenderFactory(AppenderFactory factory)
    public Builder configLoader(ConfigLoader loader)
    public LoggerInstance build()
}

// SLF4J 로거 반환
public Logger getLogger(String name)
public Logger getLogger(Class<?> clazz)

// 설정 조회
public LoggerConfig getConfig()
```

**특징**:
- Spring Bean으로 등록 가능
- 테스트에서 여러 인스턴스 생성 가능
- 의존성 주입을 통한 커스터마이징

**사용 예시**:
```java
// Logback 설정 적용
LoggerInstance.builder()
    .configPath("config/logging.yml")
    .build();

// 로깅은 @Slf4j 사용
@Slf4j
public class MyService {
    public void process() {
        log.info("Processing...");
    }
}
```

---

### 4. SQL Capture Layer (SQL 캡처 계층)

**핵심 클래스**: `SqlCaptureProxy`

**책임**:
- JDBC `DataSource` 프록시로 SQL 자동 캡처
- MyBatis, JPA, Hibernate 등 모든 JDBC 기반 프레임워크 지원
- SQL, 파라미터, 실행 시간, 결과 자동 로깅

**아키텍처**:
```java
DataSource (원본)
    ↓ (래핑)
SqlCaptureProxy
    ↓ (Connection 래핑)
SqlCaptureConnection
    ↓ (Statement 래핑)
SqlCapturePreparedStatement
    ↓ (SQL 실행 전후 로깅)
실제 SQL 실행
```

**주요 클래스**:

1. **SqlCaptureProxy** - DataSource 프록시
```java
public class SqlCaptureProxy implements DataSource {
    private final DataSource targetDataSource;
    private final LoggerInstance loggerInstance;

    // Static API 지원
    public SqlCaptureProxy(DataSource dataSource)

    // Instance API 지원
    public SqlCaptureProxy(DataSource dataSource, LoggerInstance logger)
}
```

2. **SqlCaptureConnection** - Connection 래퍼
3. **SqlCapturePreparedStatement** - PreparedStatement 래퍼
4. **SqlFormatter** - SQL 포맷팅 (관심사 분리)
5. **ParameterSubstitutor** - 파라미터 치환 로직
6. **SqlLogger** - SQL 로깅 로직 (관심사 분리)

**로그 출력 예시**:
```
[SQL] SELECT * FROM users WHERE id = ?
[SQL-PARAMS] Parameters: [123]
[SQL-RESULT] Rows affected: 1
[SQL-TIME] Execution time: 15ms
```

**디자인 패턴**:
- **Proxy 패턴**: DataSource/Connection/Statement 래핑
- **Decorator 패턴**: 기존 기능에 로깅 기능 추가
- **Strategy 패턴**: Static API vs Instance API

---

### 5. Configuration Layer (설정 계층)

#### 5.1 UniversalConfigLoader

**책임**:
- 다양한 형식의 설정 파일 로드 (YAML, XML, JSON)
- 클래스패스 및 파일 시스템 리소스 자동 탐색
- 기본값 병합 (JAR 내장 defaults.yml)

**주요 메서드**:
```java
public LoggerConfig load(String configPath) throws Exception
public LoggerConfig load(String configPath, boolean mergeDefaults)
public LoggerConfig loadDefaultConfig()
```

**자동 탐색 순서**:
```
1. universal-logger.yml
2. config/universal-logger.yml
3. universal-logger.xml
4. config/universal-logger.xml
5. universal-logger.json
6. config/universal-logger.json
7. classpath:universal-logger.yml
8. classpath:logback.xml
```

**파서 자동 선택**:
```java
private Map<String, ConfigParser> parsers;
// .yml, .yaml → YamlParser
// .xml → XmlParser
// .json → JsonParser
```

**디자인 패턴**:
- **Factory 패턴**: 확장자에 따라 파서 선택
- **Strategy 패턴**: 파서별 전략

#### 5.2 ConfigParser 인터페이스

**구현체**:
- `YamlParser`: YAML 파싱 (SnakeYAML)
- `XmlParser`: XML 파싱
- `JsonParser`: JSON 파싱 (Jackson)

#### 5.3 ConfigValidator

**책임**:
- 설정의 유효성 검증
- 누락된 필수 값 검출
- 경고 및 오류 메시지 생성

**주요 메서드**:
```java
public static ValidationResult validate(LoggerConfig config)
private static void validateGlobalConfig(...)
private static void validateWebhookConfig(...)
private static void validateEmailConfig(...)
```

#### 5.4 LoggerConfig (Model)

**책임**:
- 설정 데이터 모델
- 중첩 클래스로 계층 구조 표현

**주요 클래스**:
```java
LoggerConfig
├── GlobalConfig
├── OutputConfig
│   ├── RotationConfig
│   ├── WebhookConfig
│   └── EmailConfig
├── FiltersConfig
├── PerformanceConfig
└── SqlConfig
```

---

### 6. Appender Layer (출력 계층)

#### 인터페이스 설계

**AppenderFactory** (인터페이스):
```java
public interface AppenderFactory {
    Appender<ILoggingEvent> createConsoleAppender(...);
    Appender<ILoggingEvent> createFileAppender(...);
    Appender<ILoggingEvent> createErrorFileAppender(...);
    Appender<ILoggingEvent> createWebhookAppender(...);
    Appender<ILoggingEvent> createEmailAppender(...);
}
```

**구현체**:
- ConsoleAppender (기본 제공)
- RollingFileAppender (기본 제공)
- WebhookAppender (커스텀)
- EmailAppender (커스텀)

**디자인 패턴**: Strategy 패턴
- 런타임에 로그 출력 전략을 선택 가능

---

## 패키지 구조

```
com.universallogger/
│
├── UniversalLogger.java            # Static API (Facade)
├── LoggerInstance.java             # Core (Instance API, DI-friendly)
│
├── spring/                          # Spring 통합
│   ├── UniversalLoggerAutoConfiguration.java    # Spring Boot Auto Config
│   ├── UniversalLoggerConfiguration.java        # 일반 Spring Config
│   └── UniversalLoggerProperties.java           # application.yml 매핑
│
├── config/                          # 설정 모델
│   ├── LoggerConfig.java           # 설정 데이터 모델
│   └── LoggerConfigBuilder.java    # Builder 패턴
│
├── loaders/                         # 설정 로더
│   ├── ConfigLoader.java           # 인터페이스
│   └── UniversalConfigLoader.java  # 구현체 (YAML/XML/JSON 지원)
│
├── parsers/                         # 설정 파서
│   ├── ConfigParser.java           # 인터페이스
│   ├── YamlParser.java
│   ├── XmlParser.java
│   └── JsonParser.java
│
├── sql/                             # SQL 캡처 (Proxy 패턴)
│   ├── SqlCaptureProxy.java        # DataSource 프록시
│   ├── SqlCaptureConnection.java   # Connection 래퍼
│   ├── SqlCaptureStatement.java
│   ├── SqlCapturePreparedStatement.java
│   ├── SqlCaptureCallableStatement.java
│   ├── SqlFormatter.java           # SQL 포맷팅 로직
│   ├── SqlLogger.java              # SQL 로깅 로직
│   ├── ParameterSubstitutor.java   # 파라미터 치환
│   └── RowCountingResultSet.java   # ResultSet 래퍼
│
├── appenders/                       # 출력 (Strategy 패턴)
│   ├── AppenderFactory.java        # 인터페이스
│   ├── DefaultAppenderFactory.java # 구현체
│   ├── WebhookAppender.java
│   └── EmailAppender.java
│
├── formatters/                      # 포맷터
│   ├── ConsoleFormatter.java       # 콘솔 포맷터 (UTF-8)
│   └── FileFormatter.java          # 파일 포맷터 (UTF-8)
│
├── filters/                         # 필터 (Deprecated)
│   ├── MessageFilter.java          # 인터페이스 (@Deprecated)
│   ├── SensitiveDataFilter.java    # 민감 정보 마스킹 (@Deprecated)
│   └── SensitiveDataLogbackFilter.java  # Logback Filter 구현
│
├── validation/                      # 검증
│   └── ConfigValidator.java
│
└── constants/                       # 상수
    └── LoggerConstants.java
```

---

## SOLID 원칙 적용

### 1. Single Responsibility Principle (단일 책임 원칙)

각 클래스는 하나의 책임만 가짐:

- `UniversalLogger`: API 제공
- `ConfigLoader`: 설정 로드
- `ConfigValidator`: 설정 검증
- `SqlCaptureProxy`: SQL 캡처
- `WebhookAppender`: 웹훅 전송

### 2. Open-Closed Principle (개방-폐쇄 원칙)

확장에는 열려있고 수정에는 닫혀있음:

```java
// 새로운 Appender 추가 시 기존 코드 수정 불필요
public class SyslogAppender implements Appender<ILoggingEvent> {
    // Syslog 전용 구현
}

// Factory에만 case 추가
if ("syslog".equals(name)) {
    return createSyslogAppender(...);
}
```

### 3. Liskov Substitution Principle (리스코프 치환 원칙)

인터페이스를 구현한 모든 클래스는 상호 치환 가능:

```java
// 어떤 Appender든 동일한 방식으로 사용
Appender<ILoggingEvent> appender = factory.createAppender(config);
appender.start();  // 모든 구현체에서 동일하게 동작
```

### 4. Interface Segregation Principle (인터페이스 분리 원칙)

클라이언트가 사용하지 않는 메서드에 의존하지 않음:

```java
// ConfigLoader: 설정 로딩에 필요한 메서드만
// AppenderFactory: Appender 생성에 필요한 메서드만
```

### 5. Dependency Inversion Principle (의존성 역전 원칙)

구체적인 클래스가 아닌 추상화에 의존:

```java
// 구체적인 YamlParser가 아닌
// ConfigParser 인터페이스에 의존
ConfigParser parser = selectParser(extension);
```

---

## 디자인 패턴

### 1. Singleton Pattern (Bill Pugh Holder)

**적용**: `UniversalLogger` 클래스

**목적**: Thread-safe한 싱글톤 구현

```java
public class UniversalLogger {
    private static class Holder {
        private static LoggerInstance INSTANCE;
    }

    public static LoggerInstance getInstance() {
        if (Holder.INSTANCE == null) {
            Holder.INSTANCE = createDefaultInstance();
        }
        return Holder.INSTANCE;
    }
}
```

**특징**:
- Lazy Initialization
- Thread-safe (클래스 로딩 메커니즘 활용)
- Double-checked locking 불필요

### 2. Facade Pattern

**적용**: `UniversalLogger` 클래스

**목적**: 복잡한 Logback 설정을 단순한 인터페이스로 제공

```java
// 한 줄로 Logback 설정 초기화
UniversalLogger.init("logging.yml");

// 내부적으로는 복잡한 처리
// - 설정 파일 자동 탐색
// - ConfigLoader 호출
// - Logback 프로그래밍 방식 설정
// - Appender 초기화
// - Formatter 설정

// 로깅은 표준 @Slf4j 사용
@Slf4j
public class MyService {
    public void process() {
        log.info("Simple logging with @Slf4j");
    }
}
```

### 3. Factory Pattern

**적용**:
- `UniversalConfigLoader`: 확장자에 따라 파서 선택
- `AppenderFactory`: Appender 생성 로직 캡슐화

**목적**: 객체 생성 로직을 캡슐화

```java
// 파일 확장자에 따라 자동으로 파서 선택
ConfigParser parser = selectParser(extension);
```

### 4. Proxy Pattern (+ Decorator)

**적용**: SQL 캡처 계층 전체

**목적**: 기존 객체에 로깅 기능 투명하게 추가

```java
// DataSource 프록시
public class SqlCaptureProxy implements DataSource {
    private final DataSource targetDataSource;

    public Connection getConnection() {
        Connection conn = targetDataSource.getConnection();
        return new SqlCaptureConnection(conn, loggerInstance);
    }
}

// Connection 프록시
public class SqlCaptureConnection implements Connection {
    private final Connection target;

    public PreparedStatement prepareStatement(String sql) {
        PreparedStatement stmt = target.prepareStatement(sql);
        return new SqlCapturePreparedStatement(stmt, sql, logger);
    }
}
```

**특징**:
- 원본 객체 수정 없이 기능 추가
- 투명한 래핑 (클라이언트는 프록시인지 모름)
- Decorator와 유사하지만 지연 초기화/접근 제어 목적

### 5. Strategy Pattern

**적용**:
- `AppenderFactory`: Appender 생성 전략
- `ConfigParser`: 파싱 전략
- `MessageFilter`: 필터링 전략

**목적**: 런타임에 알고리즘(전략) 선택

```java
// 설정에 따라 다른 출력 전략 사용
outputs:
  console: ...    # ConsoleAppender
  file: ...       # FileAppender
  webhook: ...    # WebhookAppender

// 파일 확장자에 따라 파서 전략 선택
.yml  → YamlParser
.xml  → XmlParser
.json → JsonParser
```

### 6. Builder Pattern

**적용**:
- `LoggerInstance.Builder`: 인스턴스 생성
- `LoggerConfigBuilder`: 설정 객체 생성

**목적**: 복잡한 객체를 단계적으로 생성

```java
// LoggerInstance Builder
LoggerInstance instance = LoggerInstance.builder()
    .configPath("config/logging.yml")
    .appenderFactory(customFactory)
    .messageFilter(customFilter)
    .build();

// Config Builder
LoggerConfig config = LoggerConfigBuilder.create()
    .appName("MyApp")
    .rootLevel("INFO")
    .console().enabled(true).and()
    .file().path("logs/app.log").and()
    .build();
```

### 7. Dependency Injection (DI) Pattern

**적용**: `LoggerInstance` 전체 설계

**목적**: 테스트 용이성, 유연성 확보

```java
public class LoggerInstance {
    private final AppenderFactory appenderFactory;
    private final ConfigLoader configLoader;
    private final MessageFilter messageFilter;

    // Builder를 통한 의존성 주입
    private LoggerInstance(Builder builder) {
        this.appenderFactory = builder.appenderFactory != null
            ? builder.appenderFactory
            : new DefaultAppenderFactory();
        this.configLoader = builder.configLoader != null
            ? builder.configLoader
            : new UniversalConfigLoader();
        // ...
    }
}
```

**Spring 통합**:
```java
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserService {
    public void process() {
        // LoggerInstance는 Spring이 자동으로 생성 및 Logback 설정
        // 로깅은 @Slf4j 사용
        log.info("Processing...");
    }
}
```

---

## Hard-code 제거 전략

### 1. 상수 중앙 관리

**LoggerConstants** 클래스:
```java
public final class LoggerConstants {
    public static final class Defaults {
        public static final String APP_NAME = "UniversalLogger";
        public static final String LOG_LEVEL = "INFO";
        // ...
    }

    public static final class LogLevel {
        public static final String TRACE = "TRACE";
        public static final String DEBUG = "DEBUG";
        // ...
    }
}
```

**사용 예시**:
```java
// Hard-coded 값 대신 상수 사용
String appName = LoggerConstants.Defaults.APP_NAME;
String level = LoggerConstants.LogLevel.INFO;
```

### 2. 설정 기반 동작

모든 동작은 설정 파일에서 제어:

```yaml
# 모든 값을 설정 파일에서 지정
global:
  appName: "MyApp"        # 앱 이름
  timezone: "Asia/Seoul"  # 타임존
  encoding: "UTF-8"       # 인코딩

outputs:
  console:
    enabled: true
    level: "INFO"
  file:
    enabled: true
    path: "logs/{appName}.log"
```

### 3. 기본값 제공

설정 누락 시 안전한 기본값 사용:

```java
// 설정에서 로드, 없으면 기본값
String appName = config.getGlobal().getAppName();
if (appName == null || appName.trim().isEmpty()) {
    appName = LoggerConstants.Defaults.APP_NAME;
}
```

---

## 확장 가이드

### 새로운 Appender 추가

1. **인터페이스 구현**:
```java
public class SyslogAppender implements Appender<ILoggingEvent> {
    public void append(ILoggingEvent event) {
        // Syslog 전송 로직
    }
}
```

2. **Factory에 추가**:
```java
public Appender<ILoggingEvent> createSyslogAppender(...) {
    SyslogAppender appender = new SyslogAppender();
    // 설정 적용
    return appender;
}
```

3. **설정에 추가**:
```yaml
outputs:
  syslog:
    enabled: true
    host: "syslog.example.com"
```

### 새로운 파서 추가

1. **ConfigParser 구현**:
```java
public class TomlParser implements ConfigParser {
    public LoggerConfig parse(String content) {
        // TOML 파싱 로직
    }
}
```

2. **ConfigLoader에 등록**:
```java
parsers.put(".toml", new TomlParser());
```

---

## 성능 고려사항

### 1. 비동기 로깅

```yaml
performance:
  async: true           # 비동기 활성화
  bufferSize: 1000      # 버퍼 크기
  maxQueueSize: 10000   # 큐 크기
```

### 2. 메모리 효율

- ConcurrentHashMap으로 Logger 인스턴스 재사용
- 설정은 한 번만 로드하여 메모리 효율성 확보

---

## 테스트 전략

### 1. 단위 테스트

각 계층별로 독립적인 테스트:
```java
@Test
public void testConfigLoader() {
    ConfigLoader loader = new UniversalConfigLoader();
    LoggerConfig config = loader.load("test-config.yml");
    assertNotNull(config);
}
```

### 2. 통합 테스트

전체 흐름 테스트:
```java
@Test
public void testEndToEnd() {
    UniversalLogger.init("test-config.yml");
    UniversalLogger.info("Test message");
    // 로그 파일 확인
}
```

---

## 결론

Universal Logger Java는:
- **Logback 프로그래밍 방식 설정**: YAML/XML/JSON으로 Logback 완전 제어
- **Zero 설정**: Spring Boot Auto Configuration으로 즉시 사용 가능
- **SQL 자동 캡처**: Proxy 패턴으로 모든 JDBC 기반 프레임워크 지원
- **다중 포맷**: YAML, XML, JSON 자동 감지 및 파싱
- **@Slf4j 완벽 호환**: 표준 SLF4J API 그대로 사용
- **관심사 분리**: 각 계층이 독립적으로 동작하고 테스트 가능
- **객체지향 설계**: 7가지 디자인 패턴 적용으로 확장성 확보
- **Zero Hard-code**: LoggerConstants + 설정 기반 동작
- **SOLID 원칙**: 인터페이스 기반 설계로 유지보수 용이

**Logback 설정을 간편하게 관리하는 엔터프라이즈급 라이브러리입니다.**

---

## 주요 개선 사항 (최신 버전)

### 1. Spring Boot 완벽 통합
- **Auto Configuration**: 의존성 추가만으로 자동 활성화
- **DataSource 자동 래핑**: SQL 자동 캡처
- **Properties 바인딩**: application.yml 설정 지원

### 2. SQL 캡처 시스템
- **Proxy 패턴**: 투명한 SQL 로깅
- **관심사 분리**: Formatter, Logger, Substitutor 분리
- **성능 최적화**: 파라미터 치환 로직 개선

### 3. 설정 시스템 개선
- **다중 포맷**: YAML, XML, JSON 자동 감지
- **기본값 병합**: JAR 내장 defaults.yml과 자동 병합
- **파서 추상화**: ConfigParser 인터페이스로 확장 가능

### 4. 아키텍처 개선
- **인스턴스 기반 설계**: LoggerInstance 중심으로 리팩토링
- **의존성 주입**: Builder를 통한 DI 지원
- **멀티 인스턴스**: 여러 인스턴스 동시 사용 가능

### 5. 코드 품질
- **주석 한글화**: 모든 주석을 한글로 통일
- **UTF-8 지원**: Formatter에 명시적 인코딩 설정
- **패키지 재구성**: 기능별 명확한 패키지 분리
