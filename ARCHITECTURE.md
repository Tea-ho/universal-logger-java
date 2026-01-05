# Universal Logger - Architecture Guide

이 문서는 Universal Logger의 아키텍처, 설계 원칙, 모듈 구조를 설명합니다.

## 목차

1. [전체 아키텍처](#전체-아키텍처)
2. [모듈 구조](#모듈-구조)
3. [핵심 컴포넌트](#핵심-컴포넌트)
4. [설계 원칙](#설계-원칙)
5. [확장 포인트](#확장-포인트)
6. [동작 흐름](#동작-흐름)

## 전체 아키텍처

```
┌─────────────────────────────────────────────────────────────┐
│                     Application Layer                        │
│  (User Code with SLF4J API: @Slf4j, log.info(), etc.)       │
└───────────────────────────┬─────────────────────────────────┘
                            │
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                    SLF4J Abstraction Layer                   │
│              (org.slf4j.Logger interface)                    │
└───────────────────────────┬─────────────────────────────────┘
                            │
                ┌───────────┴───────────┐
                │                       │
                ↓                       ↓
    ┌─────────────────────┐  ┌─────────────────────┐
    │   Logback Binding   │  │   Log4j2 Binding    │
    │   (사용자 선택)      │  │   (사용자 선택)      │
    └──────────┬──────────┘  └──────────┬──────────┘
               │                        │
               ↓                        ↓
┌─────────────────────────────────────────────────────────────┐
│              Universal Logger Multi-Module                   │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │         universal-logger-core (공통 모듈)            │  │
│  │  • 설정 로더 (YAML/XML/JSON)                         │  │
│  │  • 공통 모델 (LoggerConfig)                          │  │
│  │  • SQL 캡처 프록시                                    │  │
│  │  • 유틸리티 (파서, 밸리데이터)                        │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │      universal-logger-logback (Logback 구현)         │  │
│  │  • Logback Configurator (ServiceLoader)              │  │
│  │  • Custom Appenders (Email, Webhook)                 │  │
│  │  • Spring Boot Auto-Configuration                    │  │
│  │  • Spring Integration                                │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │      universal-logger-log4j2 (Log4j2 구현)           │  │
│  │  • Log4j2 ConfigurationFactory (SPI)                 │  │
│  │  • Custom Plugin Appenders (Email, Webhook)          │  │
│  │  • Spring Boot Auto-Configuration                    │  │
│  │  • Spring Integration                                │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                            │
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                      Output Destinations                     │
│  • Console (STDOUT/STDERR)                                  │
│  • File (Rolling, Compression)                              │
│  • Email (SMTP)                                             │
│  • Webhook (HTTP POST: Slack, Discord, Teams)               │
└─────────────────────────────────────────────────────────────┘
```

## 모듈 구조

### 1. universal-logger-core

**역할**: 공통 기능 및 코어 로직

**주요 컴포넌트**:
```
universal-logger-core/
├── config/
│   ├── LoggerConfig.java              # 설정 모델 (POJO)
│   ├── LoggerConfigBuilder.java       # Builder 패턴
│   └── (Inner classes: OutputConfig, EmailConfig, etc.)
├── loaders/
│   ├── ConfigLoader.java              # 인터페이스
│   └── UniversalConfigLoader.java     # 구현체
├── parsers/
│   ├── ConfigParser.java              # 인터페이스
│   ├── YamlParser.java                # YAML 파싱
│   ├── XmlParser.java                 # XML 파싱
│   └── JsonParser.java                # JSON 파싱
├── sql/
│   ├── SqlCaptureProxy.java           # DataSource 프록시
│   ├── SqlCaptureConnection.java      # Connection 래퍼
│   ├── SqlCapturePreparedStatement.java
│   ├── SqlCaptureStatement.java
│   ├── SqlLogger.java                 # SQL 로깅 로직
│   ├── SqlFormatter.java              # SQL 포매팅
│   └── ParameterSubstitutor.java      # 파라미터 치환
├── validation/
│   └── ConfigValidator.java           # 설정 검증
└── resources/
    └── defaults/
        └── universal-logger-defaults.yml  # 기본 설정
```

**의존성**:
- SLF4J API 1.7.36+
- Jackson Databind 2.13.5+ (YAML 파싱)
- Jackson Dataformat XML (XML 파싱)

**특징**:
- 로깅 프레임워크 독립적
- 재사용 가능한 공통 로직
- 불변 설정 객체
- 스레드 안전

### 2. universal-logger-logback

**역할**: Logback 구현 및 통합

**주요 컴포넌트**:
```
universal-logger-logback/
├── configurator/
│   └── LogbackConfigurator.java       # ServiceLoader 진입점
├── appenders/
│   ├── EmailAppender.java             # 이메일 Appender
│   └── WebhookAppender.java           # 웹훅 Appender
├── spring/
│   ├── UniversalLoggerAutoConfiguration.java    # Spring Boot Auto-Config
│   ├── UniversalLoggerConfiguration.java        # Spring Config
│   └── UniversalLoggerProperties.java           # @ConfigurationProperties
└── resources/
    └── META-INF/
        ├── services/
        │   └── ch.qos.logback.classic.spi.Configurator  # ServiceLoader
        └── spring/
            └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

**동작 방식**:
1. **ServiceLoader 자동 발견**: Logback이 시작 시 `Configurator` SPI를 통해 자동 로드
2. **설정 로드 및 병합**: YAML 파일 + 기본값 병합
3. **Logback 프로그래밍 설정**: LoggerContext API로 Appender/Logger 구성
4. **Spring Boot 자동 설정**: BeanPostProcessor를 통해 DataSource 자동 래핑

**의존성**:
- universal-logger-core
- Logback Classic 1.2.12+
- Spring Boot (optional)
- JavaMail 1.6+ (Email 기능)
- Apache HttpClient 4.5+ (Webhook 기능)

### 3. universal-logger-log4j2

**역할**: Log4j2 구현 및 통합

**주요 컴포넌트**:
```
universal-logger-log4j2/
├── ConfigurationFactory.java          # Log4j2 ConfigurationFactory (SPI)
├── appenders/
│   ├── EmailAppender.java             # @Plugin Appender
│   └── WebhookAppender.java           # @Plugin Appender
├── spring/
│   ├── UniversalLoggerAutoConfiguration.java    # Spring Boot Auto-Config
│   ├── UniversalLoggerConfiguration.java        # Spring Config
│   └── UniversalLoggerProperties.java           # @ConfigurationProperties
└── resources/
    └── META-INF/
        ├── services/
        │   └── org.apache.logging.log4j.core.config.ConfigurationFactory
        └── spring/
            └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

**동작 방식**:
1. **ConfigurationFactory SPI**: Log4j2가 시작 시 자동으로 Factory 호출
2. **ConfigurationBuilder API**: Builder 패턴으로 Appender/Logger 구성
3. **Plugin Appenders**: `@Plugin` 어노테이션으로 등록된 커스텀 Appender
4. **Spring Boot 자동 설정**: BeanPostProcessor를 통해 DataSource 자동 래핑

**의존성**:
- universal-logger-core
- Log4j2 Core 2.17.1+
- Spring Boot (optional)
- JavaMail 1.6+ (Email 기능)
- Apache HttpClient 4.5+ (Webhook 기능)

## 핵심 컴포넌트

### 1. 설정 시스템

#### ConfigLoader
```java
public interface ConfigLoader {
    LoggerConfig load(String configPath);
    LoggerConfig loadDefaultConfig();
}
```

**역할**: 설정 파일 로드 및 기본값 병합

**구현**:
- `UniversalConfigLoader`: 멀티 포맷 지원 (YAML/XML/JSON)
- 파일 탐색 전략 (우선순위 기반)
- 설정 병합 로직 (프로젝트 설정 + 기본값)

#### LoggerConfig
```java
public class LoggerConfig {
    private GlobalConfig global;
    private Map<String, String> levels;
    private SqlConfig sql;
    private Map<String, OutputConfig> outputs;
    private FiltersConfig filters;
    private PerformanceConfig performance;
    // ... getters/setters
}
```

**특징**:
- 불변 객체 (Thread-safe)
- Builder 패턴 지원
- Jackson 어노테이션 기반 직렬화/역직렬화

### 2. SQL 캡처 시스템

#### SqlCaptureProxy
```java
public class SqlCaptureProxy implements DataSource {
    private final DataSource delegate;
    private final LoggerConfig config;

    @Override
    public Connection getConnection() throws SQLException {
        Connection conn = delegate.getConnection();
        return new SqlCaptureConnection(conn, config);
    }
}
```

**동작 흐름**:
```
DataSource (원본)
    ↓ wrap
SqlCaptureProxy
    ↓ getConnection()
SqlCaptureConnection
    ↓ prepareStatement()
SqlCapturePreparedStatement
    ↓ execute()
    → SQL 로깅 (SqlLogger)
    → 파라미터 치환 (ParameterSubstitutor)
    → 실행 시간 측정
    → 결과 행 수 로깅
```

**지원 기능**:
- PreparedStatement 파라미터 캡처
- SQL 포매팅 (가독성 향상)
- 파라미터 치환 (? → 실제 값)
- 실행 시간 측정
- 결과 행 수 추적

### 3. Appender 시스템

#### Logback Appenders
```java
public class EmailAppender extends AppenderBase<ILoggingEvent> {
    @Override
    protected void append(ILoggingEvent event) {
        // JavaMail API로 이메일 전송
    }
}

public class WebhookAppender extends AppenderBase<ILoggingEvent> {
    @Override
    protected void append(ILoggingEvent event) {
        // Apache HttpClient로 HTTP POST
    }
}
```

#### Log4j2 Appenders
```java
@Plugin(name = "UniversalEmail", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE)
public class EmailAppender extends AbstractAppender {
    @Override
    public void append(LogEvent event) {
        // JavaMail API로 이메일 전송
    }
}

@Plugin(name = "UniversalWebhook", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE)
public class WebhookAppender extends AbstractAppender {
    @Override
    public void append(LogEvent event) {
        // Apache HttpClient로 HTTP POST
    }
}
```

### 4. Spring 통합

#### Auto-Configuration (Spring Boot)
```java
@AutoConfiguration
@ConditionalOnClass({DataSource.class})
@ConditionalOnProperty(prefix = "universal-logger", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(UniversalLoggerProperties.class)
public class UniversalLoggerAutoConfiguration implements BeanPostProcessor {

    @Bean
    public LoggerConfig loggerConfig() {
        // 설정 로드 및 Bean 등록
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof DataSource) {
            return new SqlCaptureProxy((DataSource) bean, config);
        }
        return bean;
    }
}
```

**동작**:
1. `@AutoConfiguration`: Spring Boot가 자동으로 로드
2. `@ConditionalOnClass`: DataSource 클래스가 있을 때만 활성화
3. `@ConditionalOnProperty`: application.yml 설정으로 제어 가능
4. `BeanPostProcessor`: DataSource Bean을 자동으로 래핑

## 설계 원칙

### 1. 관심사의 분리 (Separation of Concerns)

- **Core**: 로깅 프레임워크 독립적 로직
- **Logback/Log4j2**: 각 프레임워크 특화 구현
- **Spring**: Spring 통합 로직

### 2. 의존성 역전 원칙 (Dependency Inversion)

```
Application (SLF4J API)
         ↓ depends on
    SLF4J Interface
         ↑ implements
Logback / Log4j2 (구현체)
         ↓ uses
   Universal Logger
```

### 3. 개방-폐쇄 원칙 (Open-Closed Principle)

- **확장에 열려있음**: 새로운 Appender, Parser 추가 가능
- **수정에 닫혀있음**: 기존 코드 변경 없이 확장

### 4. 단일 책임 원칙 (Single Responsibility)

- `ConfigLoader`: 설정 로드만 담당
- `SqlCaptureProxy`: SQL 캡처만 담당
- `Appender`: 특정 출력 채널만 담당

### 5. 인터페이스 분리 원칙 (Interface Segregation)

```java
// 작은 인터페이스로 분리
public interface ConfigLoader { ... }
public interface ConfigParser { ... }
public interface ConfigValidator { ... }
```

## 확장 포인트

### 1. 새로운 설정 포맷 추가

```java
// 1. ConfigParser 구현
public class TomlParser implements ConfigParser {
    @Override
    public LoggerConfig parse(String content) {
        // TOML 파싱 로직
    }
}

// 2. UniversalConfigLoader에 등록
private ConfigParser selectParser(String path) {
    if (path.endsWith(".toml")) {
        return new TomlParser();
    }
    // ...
}
```

### 2. 새로운 Appender 추가

#### Logback
```java
public class DatabaseAppender extends AppenderBase<ILoggingEvent> {
    @Override
    protected void append(ILoggingEvent event) {
        // 데이터베이스에 로그 저장
    }
}
```

#### Log4j2
```java
@Plugin(name = "UniversalDatabase", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE)
public class DatabaseAppender extends AbstractAppender {
    @Override
    public void append(LogEvent event) {
        // 데이터베이스에 로그 저장
    }
}
```

### 3. 커스텀 필터 추가

```java
public class CustomFilter extends Filter<ILoggingEvent> {
    @Override
    public FilterReply decide(ILoggingEvent event) {
        // 커스텀 필터링 로직
        return FilterReply.NEUTRAL;
    }
}
```

## 동작 흐름

### 1. Logback 초기화 흐름

```
[Application Start]
         ↓
[Logback 초기화]
         ↓
[ServiceLoader가 Configurator 탐색]
         ↓
[LogbackConfigurator.configure() 호출]
         ↓
[UniversalConfigLoader로 설정 로드]
         ↓
[설정 파일 탐색 (우선순위 기반)]
         ↓
[YAML/XML/JSON 파싱]
         ↓
[기본값과 병합]
         ↓
[LoggerContext API로 Logback 구성]
    • Appender 생성 및 등록
    • Logger 레벨 설정
    • Layout 설정
         ↓
[로깅 준비 완료]
```

### 2. Log4j2 초기화 흐름

```
[Application Start]
         ↓
[Log4j2 초기화]
         ↓
[ServiceLoader가 ConfigurationFactory 탐색]
         ↓
[ConfigurationFactory.getConfiguration() 호출]
         ↓
[UniversalConfigLoader로 설정 로드]
         ↓
[설정 파일 탐색 및 파싱]
         ↓
[기본값과 병합]
         ↓
[ConfigurationBuilder로 Log4j2 구성]
    • Appender 추가
    • Logger 레벨 설정
    • Layout 설정
         ↓
[로깅 준비 완료]
```

### 3. SQL 캡처 흐름

```
[SQL 실행 요청]
         ↓
[SqlCaptureProxy.getConnection()]
         ↓
[SqlCaptureConnection 생성]
         ↓
[connection.prepareStatement(sql)]
         ↓
[SqlCapturePreparedStatement 생성]
    • SQL 문자열 저장
    • 파라미터 리스트 초기화
         ↓
[setString(), setInt() 등 파라미터 설정]
    • 파라미터 저장 (index, value)
         ↓
[execute() / executeQuery() / executeUpdate()]
    • 실행 시작 시간 기록
    • 실제 SQL 실행 (delegate)
    • 실행 종료 시간 기록
    • 결과 행 수 추적
         ↓
[SqlLogger.log()]
    • [SQL] 원본 SQL
    • [SQL-PARAMS] 파라미터 목록
    • [SQL-FORMATTED] 파라미터 치환된 SQL
    • [SQL-ROWS] 결과 행 수
    • [SQL-TIME] 실행 시간
         ↓
[결과 반환]
```

### 4. Spring Boot 통합 흐름

```
[Spring Boot Application Start]
         ↓
[Auto-Configuration 스캔]
         ↓
[UniversalLoggerAutoConfiguration 활성화]
    • @ConditionalOnClass 체크
    • @ConditionalOnProperty 체크
         ↓
[loggerConfig() Bean 생성]
    • UniversalConfigLoader로 설정 로드
    • LoggerConfig Bean 등록
         ↓
[BeanPostProcessor.postProcessAfterInitialization()]
         ↓
[DataSource Bean 발견]
         ↓
[SqlCaptureProxy로 래핑]
    • 원본 DataSource를 SqlCaptureProxy로 교체
    • LoggerConfig 주입
         ↓
[애플리케이션에서 DataSource 사용]
    • SQL 자동 캡처 활성화
```

## 성능 고려사항

### 1. 설정 로드
- **캐싱**: 설정은 한 번만 로드되고 재사용
- **Lazy Loading**: 필요할 때만 파싱

### 2. SQL 캡처
- **Proxy 패턴**: 최소한의 오버헤드
- **조건부 로깅**: 로그 레벨에 따라 선택적 캡처
- **비동기 옵션**: 설정으로 비동기 로깅 활성화 가능

### 3. Appender
- **비동기 Appender**: 메인 스레드 블로킹 방지
- **버퍼링**: 대량 로그 처리 시 성능 향상
- **연결 풀링**: HTTP/SMTP 연결 재사용

## 스레드 안전성

### 1. 불변 설정
- `LoggerConfig`는 생성 후 변경 불가
- Thread-safe by design

### 2. 동기화
- SQL 캡처: Connection별 독립적 상태
- Appender: Logback/Log4j2가 스레드 안전성 보장

### 3. 공유 리소스
- HTTP Client: ThreadSafe HttpClient 사용
- SMTP Session: 요청마다 새로운 Session 생성

## 테스트 전략

### 1. 단위 테스트
- ConfigLoader 테스트
- ConfigParser 테스트
- SqlFormatter 테스트

### 2. 통합 테스트
- Logback/Log4j2 통합 테스트
- Spring Boot 자동 설정 테스트
- SQL 캡처 End-to-End 테스트

### 3. 성능 테스트
- SQL 캡처 오버헤드 측정
- Appender 처리량 테스트
- 메모리 사용량 프로파일링

## 문제 해결 가이드

### 디버그 모드 활성화

**Logback:**
```xml
<configuration debug="true">
    <!-- ... -->
</configuration>
```

**Log4j2:**
```xml
<Configuration status="DEBUG">
    <!-- ... -->
</Configuration>
```

### 설정 로드 확인
```java
LoggerConfig config = new UniversalConfigLoader().load("config/universal-logger.yml");
System.out.println(config); // 로드된 설정 출력
```

### SQL 캡처 확인
```java
// DataSource가 SqlCaptureProxy로 래핑되었는지 확인
System.out.println(dataSource.getClass().getName());
// 출력: com.universallogger.sql.SqlCaptureProxy
```

## 결론

Universal Logger는 모듈화된 아키텍처를 통해:
- **유연성**: Logback/Log4j2 선택 가능
- **확장성**: 새로운 기능 추가 용이
- **재사용성**: 공통 로직의 중앙화
- **테스트 용이성**: 각 레이어의 독립적 테스트

프로젝트에 맞는 로깅 프레임워크를 선택하고, 필요한 기능을 선택적으로 활성화할 수 있습니다.
