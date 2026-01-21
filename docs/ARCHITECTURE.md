# Universal Logger - Architecture Guide

이 문서는 Universal Logger의 아키텍처, 설계 원칙, 모듈 구조를 설명합니다.

## 목차

1. [전체 아키텍처](#전체-아키텍처)
2. [모듈 구조](#모듈-구조)
3. [핵심 컴포넌트](#핵심-컴포넌트)
4. [설계 원칙](#설계-원칙)
5. [동작 흐름](#동작-흐름)

## 전체 아키텍처

```
┌─────────────────────────────────────────────────────────────┐
│                     Application Layer                       │
│  (User Code with SLF4J API: @Slf4j, log.info(), etc.)       │
└───────────────────────────┬─────────────────────────────────┘
                            │
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                    SLF4J Abstraction Layer                  │
│              (org.slf4j.Logger interface)                   │
└───────────────────────────┬─────────────────────────────────┘
                            │
                ┌───────────┴───────────┐
                │                       │
                ↓                       ↓
    ┌─────────────────────┐  ┌─────────────────────┐
    │   Logback Binding   │  │   Log4j2 Binding    │
    │   (사용자 선택)       │  │   (사용자 선택)       │
    └──────────┬──────────┘  └──────────┬──────────┘
               │                        │
               ↓                        ↓
┌─────────────────────────────────────────────────────────────┐
│              Universal Logger Multi-Module                  │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐   │
│  │         universal-logger-core (Common)               │   │
│  │  • ConfigLoader                                      │   │
│  │  • LoggerConfig (설정 모델)                           │   │
│  │  • SQL Capture (Proxy, Connection, Statement)        │   │
│  │  • SqlLogger / SqlFormatter / ParameterSubstitutor   │   │
│  │  • ProfileResolver (프로파일 해석)                     │   │
│  │  • LoggerConstants (상수)                             │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐   │
│  │      universal-logger-logback                        │   │
│  │  • LoggerInstance                                    │   │
│  │  • AppenderFactory / DefaultAppenderFactory          │   │
│  │  • Custom Appenders (Email, Webhook)                 │   │
│  │  • Formatters (Console, File)                        │   │
│  │  • Spring / Spring Boot                              │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐   │
│  │      universal-logger-log4j2                         │   │
│  │  • Log4j2 ConfigurationFactory (SPI)                 │   │
│  │  • Custom Plugin Appenders (Email, Webhook)          │   │
│  │  • Spring / Spring Boot                              │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                            │
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                      Output Destinations                    │
│  • Console (STDOUT/STDERR)                                  │
│  • File (Rolling, Compression)                              │
│  • Email (SMTP)                                             │
│  • Webhook (HTTP POST: Slack, Discord, Teams)               │
└─────────────────────────────────────────────────────────────┘
```

## 모듈 구조

### 1. universal-logger-core

**역할**: 공통 기능 및 코어 로직 (로깅 프레임워크 독립적)

**주요 컴포넌트**:
```
universal-logger-core/
├── config/
│   ├── LoggerConfig.java                   # 설정 모델
├── constants/
│   └── LoggerConstants.java                # 상수 정의 (PropertyKeys, ConfigPath 등)
├── loaders/
│   ├── ConfigLoader.java                   # 설정 로더 인터페이스
│   └── UniversalConfigLoader.java          # 설정 로더 구현체
├── parsers/
│   ├── ConfigParser.java                   # 파서 인터페이스
│   ├── YamlParser.java                     # YAML 파싱
│   ├── XmlParser.java                      # XML 파싱
│   └── JsonParser.java                     # JSON 파싱
├── sql/
│   ├── SqlCaptureProxy.java                # DataSource 프록시
│   ├── SqlCaptureConnection.java           # Connection 래퍼
│   ├── SqlCaptureStatement.java            # Statement 래퍼
│   ├── SqlCapturePreparedStatement.java    # PreparedStatement 래퍼
│   ├── SqlCaptureCallableStatement.java    # CallableStatement 래퍼
│   ├── RowCountingResultSet.java           # ResultSet 행 수 추적
│   ├── SqlLogger.java                      # SQL 로깅 인터페이스
│   ├── Slf4jSqlLogger.java                 # SqlLogger SLF4J 구현체
│   ├── SqlFormatter.java                   # SQL 포매팅 인터페이스
│   ├── MyBatisSqlFormatter.java            # SqlFormatter MyBatis 스타일 구현체
│   ├── ParameterSubstitutor.java           # 파라미터 치환 인터페이스
│   └── DefaultParameterSubstitutor.java    # ParameterSubstitutor 기본 구현체
├── utils/
│   └── ProfileResolver.java                # 프로파일 해석 유틸리티
├── validation/
│   └── ConfigValidator.java                # 설정 검증
└── resources/
    └── defaults/
        └── universal-logger-defaults.yml   # 기본 설정
```

**의존성**:
- SLF4J API 1.7.36+
- Jackson Databind 2.13.5+ (YAML 파싱)
- Jackson Dataformat XML (XML 파싱)

**특징**:
- 로깅 프레임워크 독립적
- 인터페이스 기반 설계 (SqlLogger, SqlFormatter, ParameterSubstitutor)
- 재사용 가능한 공통 로직
- 스레드 안전

### 2. universal-logger-logback

**역할**: Logback 구현 및 Spring 통합

**주요 컴포넌트**:
```
universal-logger-logback/
├── LoggerInstance.java                # 핵심 로거 인스턴스 (Builder 패턴)
├── SensitiveDataLogbackFilter.java    # 민감 정보 마스킹 필터
├── appenders/
│   ├── AppenderFactory.java           # 앱펜더 팩토리 인터페이스
│   ├── DefaultAppenderFactory.java    # 앱펜더 팩토리 기본 구현체
│   ├── EmailAppender.java             # 이메일 Appender
│   └── WebhookAppender.java           # 웹훅 Appender (비동기)
├── formatters/
│   ├── ConsoleFormatter.java          # 콘솔 포맷터 (컬러 지원)
│   └── FileFormatter.java             # 파일 포맷터
├── spring/
│   ├── EnableUniversalLogger.java     # @EnableUniversalLogger 어노테이션
│   ├── SpringConfiguration.java       # Spring Framework 설정
│   ├── SpringBootConfiguration.java   # Spring Boot 자동 설정
│   ├── LoggerProperties.java          # 프로퍼티 인터페이스
│   ├── SpringProperties.java          # Spring Framework 프로퍼티 구현체
│   └── SpringBootProperties.java      # Spring Boot 프로퍼티 구현체
└── resources/
    └── META-INF/
        ├── services/
        │   └── ch.qos.logback.classic.spi.Configurator
        └── spring/
            └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

**동작 방식**:
1. **ServiceLoader 자동 발견**: Logback이 시작 시 `Configurator` SPI를 통해 자동 로드
2. **설정 로드 및 병합**: YAML 파일 + 기본값 병합
3. **LoggerInstance 빌더**: AppenderFactory를 통해 Appender 생성 및 구성
4. **Spring Boot 자동 설정**: BeanPostProcessor를 통해 DataSource 자동 래핑

**의존성**:
- universal-logger-core
- Logback Classic 1.2.12+
- Spring Boot (optional)
- JavaMail 1.6+ (Email 기능)
- Apache HttpClient 4.5+ (Webhook 기능)

### 3. universal-logger-log4j2

**역할**: Log4j2 구현 및 Spring 통합

**주요 컴포넌트**:
```
universal-logger-log4j2/
├── ConfigurationFactory.java          # Log4j2 ConfigurationFactory (SPI)
├── appenders/
│   ├── EmailAppender.java             # @Plugin Appender
│   └── WebhookAppender.java           # @Plugin Appender
├── spring/
│   ├── EnableUniversalLogger.java     # @EnableUniversalLogger 어노테이션
│   ├── SpringConfiguration.java       # Spring Framework 설정
│   ├── SpringBootConfiguration.java   # Spring Boot 자동 설정
│   ├── LoggerProperties.java          # 프로퍼티 인터페이스
│   ├── SpringProperties.java          # Spring Framework 프로퍼티 구현체
│   └── SpringBootProperties.java      # Spring Boot 프로퍼티 구현체
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

#### ConfigLoader 인터페이스
```java
public interface ConfigLoader {
    LoggerConfig load(String configPath);
    LoggerConfig load(String configPath, boolean mergeDefaults);
    LoggerConfig loadWithProfile(String configPath, String profile, boolean mergeDefaults);
    LoggerConfig loadDefaultConfig();
}
```

**역할**: 설정 파일 로드 및 기본값 병합

**구현**:
- `UniversalConfigLoader`: 멀티 포맷 지원 (YAML/XML/JSON)
- 파일 탐색 전략 (우선순위 기반)
- 설정 병합 로직 (defaults → base → profile)
- 프로파일 기반 설정 로드 지원

#### ProfileResolver
```java
public class ProfileResolver {
    public static String resolveProfile();
    public static String buildProfileConfigPath(String baseConfigPath, String profile);
    public static boolean isProfileActive(String profile);
}
```

**역할**: Spring 프로파일 자동 감지 및 설정 파일 경로 생성

**프로파일 탐지 우선순위**:
1. `logger.profile` 시스템 속성 (명시적 오버라이드)
2. `spring.profiles.active` 시스템 속성 (Spring Boot)
3. `SPRING_PROFILES_ACTIVE` 환경 변수 (Container 환경)
4. null (프로파일 없음, 기본 설정만 사용)

**로딩 순서**:
```
defaults/universal-logger-defaults.yml  (JAR 내장)
    ↓ merge
config/universal-logger.yml             (base config)
    ↓ merge
config/universal-logger-{profile}.yml   (profile-specific, optional)
```

#### LoggerConstants
```java
public final class LoggerConstants {
    public static final class ConfigPath {
        public static final String DEFAULT = "config/universal-logger.yml";
    }

    public static final class PropertyKeys {
        public static final String CONFIG_PATH = "universal-logger.config-path";
        public static final String SQL_CAPTURE_ENABLED = "universal-logger.sql-capture.enabled";
        public static final String LOGGER_PROFILE = "logger.profile";
        public static final String SPRING_PROFILES_ACTIVE = "spring.profiles.active";
    }

    public static final class EnvironmentKeys {
        public static final String SPRING_PROFILES_ACTIVE = "SPRING_PROFILES_ACTIVE";
    }
}
```

**역할**: 프로젝트 전체에서 사용하는 상수 정의

### 2. SQL 캡처 시스템

#### SqlCaptureProxy
```java
public class SqlCaptureProxy implements DataSource {
    private final DataSource targetDataSource;
    private final LoggerConfig config;

    public SqlCaptureProxy(DataSource targetDataSource, LoggerConfig config) { ... }

    @Override
    public Connection getConnection() throws SQLException {
        Connection connection = targetDataSource.getConnection();
        return enabled ? new SqlCaptureConnection(connection, config) : connection;
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
    ↓ prepareStatement() / createStatement() / prepareCall()
SqlCapturePreparedStatement / SqlCaptureStatement / SqlCaptureCallableStatement
    ↓ execute()
    → SQL 로깅 (SqlLogger 인터페이스)
    → 파라미터 치환 (ParameterSubstitutor 인터페이스)
    → SQL 포매팅 (SqlFormatter 인터페이스)
    → 실행 시간 측정
    → 결과 행 수 로깅 (RowCountingResultSet)
```


### 4. Spring 통합

#### @EnableUniversalLogger 어노테이션
```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(SpringConfiguration.class)
public @interface EnableUniversalLogger {
    String applicationName() default "";  // 애플리케이션 이름
    String configPath() default LoggerConstants.ConfigPath.DEFAULT;
    boolean enableSqlCapture() default true;
}
```

**역할**: Spring Framework 프로젝트에서 Universal Logger 활성화

**속성**:
- `applicationName`: 로그 파일 경로의 `{appName}` 플레이스홀더 치환에 사용
- `configPath`: 설정 파일 경로
- `enableSqlCapture`: SQL 캡처 활성화 여부

#### LoggerProperties 인터페이스
```java
public interface LoggerProperties {
    boolean isEnabled();
    String getApplicationName();
    void setApplicationName(String applicationName);
    String getConfigPath();
    void setConfigPath(String configPath);
    SqlCaptureConfig getSqlCapture();
    String getActiveProfile();
}
```

**구현체**:
- `SpringProperties` - Spring Framework용
- `SpringBootProperties` - Spring Boot용 (@ConfigurationProperties)

#### Spring Framework Configuration

Spring Framework(비-Boot) 프로젝트에서 Universal Logger를 사용하기 위한 설정입니다.

**사용 방법**:
```java
@Configuration
@EnableUniversalLogger(
    applicationName = "MyApp",
    configPath = "config/universal-logger.yml",
    enableSqlCapture = true
)
public class AppConfig {
    // 애플리케이션 설정
}
```

**SpringConfiguration 클래스**:
```java
@Configuration
public class SpringConfiguration implements ImportAware, EnvironmentAware, BeanPostProcessor {

    private String configPath;
    private boolean enableSqlCapture;
    private Environment environment;

    @Override
    public void setImportMetadata(AnnotationMetadata metadata) { ... }

    @Override
    public void setEnvironment(Environment environment) { ... }

    @Bean
    public SpringProperties springProperties() { ... }

    @Bean
    public LoggerConfig loggerConfig(SpringProperties properties) { ... }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) { ... }
}
```

**주요 특징**:
- `ImportAware`: `@EnableUniversalLogger` 어노테이션의 속성값(configPath, enableSqlCapture) 읽기
- `EnvironmentAware`: Spring Environment 주입 (프로파일 해석용)
- `BeanPostProcessor`: DataSource 자동 래핑
- 순수 POJO 기반 `SpringProperties` 사용 (Boot의 @ConfigurationProperties 없이)

**Spring Framework vs Spring Boot 비교**:

| 항목 | Spring Framework | Spring Boot |
|------|------------------|-------------|
| 활성화 방법 | `@EnableUniversalLogger` | 자동 (Auto-Configuration) |
| 프로퍼티 클래스 | `SpringProperties` | `SpringBootProperties` (@ConfigurationProperties) |
| 설정 소스 | 어노테이션 속성 | application.yml/properties |
| 설정 소스 | 어노테이션 속성 | application.yml/properties |
| Environment 주입 | `setEnvironment()` 수동 주입 | `@Autowired` 자동 주입 |
| DataSource 래핑 | `BeanPostProcessor` | `BeanPostProcessor` |

#### Spring Boot Auto-Configuration
```java
@AutoConfiguration
@ConditionalOnClass({DataSource.class})
@ConditionalOnProperty(prefix = "universal-logger", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(SpringBootProperties.class)
public class SpringBootConfiguration implements BeanPostProcessor, ApplicationContextAware {

    @Bean
    @ConditionalOnMissingBean
    public LoggerConfig loggerConfig() { ... }  // Logback
    // 또는
    public LoggerInstance loggerInstance() { ... }  // Log4j2

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) { ... }
}
```

**동작**:
1. `@AutoConfiguration`: Spring Boot가 자동으로 로드
2. `@ConditionalOnClass`: DataSource 클래스가 있을 때만 활성화
3. `@ConditionalOnProperty`: application.yml 설정으로 제어 가능
4. `BeanPostProcessor`: DataSource Bean을 자동으로 SqlCaptureProxy로 래핑

## 설계 원칙

### 1. 관심사의 분리 (Separation of Concerns)

- **Core**: 로깅 프레임워크 독립적 로직
- **Logback/Log4j2**: 각 프레임워크 특화 구현

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

- **확장에 열려있음**: 새로운 Appender, Parser, SqlLogger, SqlFormatter 추가 가능
- **수정에 닫혀있음**: 기존 코드 변경 없이 인터페이스 구현으로 확장

### 4. 단일 책임 원칙 (Single Responsibility)

- `ConfigLoader`: 설정 로드만 담당
- `SqlCaptureProxy`: SQL 캡처만 담당
- `SqlLogger`: SQL 로깅만 담당
- `SqlFormatter`: SQL 포매팅만 담당
- `ParameterSubstitutor`: 파라미터 치환만 담당
- `Appender`: 특정 출력 채널만 담당

### 5. 인터페이스 분리 원칙 (Interface Segregation)

```java
// 작은 인터페이스로 분리
public interface ConfigLoader { ... }
public interface ConfigParser { ... }
public interface SqlLogger { ... }
public interface SqlFormatter { ... }
public interface ParameterSubstitutor { ... }
public interface AppenderFactory { ... }
public interface LoggerProperties { ... }
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
[LoggerInstance.builder() 사용]
         ↓
[UniversalConfigLoader로 설정 로드]
         ↓
[ProfileResolver가 활성 프로파일 탐지]
    • logger.profile 시스템 속성
    • spring.profiles.active 속성
    • SPRING_PROFILES_ACTIVE 환경변수
         ↓
[설정 파일 탐색 및 로드]
    1. defaults/universal-logger-defaults.yml (JAR)
    2. config/universal-logger.yml (base)
    3. config/universal-logger-{profile}.yml (optional)
         ↓
[YAML/XML/JSON 파싱]
         ↓
[설정 병합 (defaults → base → profile)]
         ↓
[AppenderFactory로 Appender 생성]
    • ConsoleAppender (ConsoleFormatter)
    • FileAppender (FileFormatter, RollingPolicy)
    • EmailAppender
    • WebhookAppender
         ↓
[LoggerContext에 Appender 등록]
         ↓
[로깅 준비 완료]
```

### 2. SQL 캡처 흐름

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
    • 파라미터 Map 초기화
         ↓
[setString(), setInt() 등 파라미터 설정]
    • 파라미터 저장 (index → value)
         ↓
[execute() / executeQuery() / executeUpdate()]
    • 실행 시작 시간 기록
    • 실제 SQL 실행 (delegate)
    • 실행 종료 시간 기록
    • RowCountingResultSet으로 결과 행 수 추적
         ↓
[Slf4jSqlLogger.logAllResults()]
    • MyBatisSqlFormatter로 SQL 포매팅
    • DefaultParameterSubstitutor로 파라미터 치환
    • [SQL] 포매팅된 SQL
    • [SQL-ROWS] 결과 행 수 / 업데이트 수
    • [SQL-TIME] 실행 시간 (1초 이상 시 [SQL-SLOW] 경고)
         ↓
[결과 반환]
```

### 3. Spring Boot 통합 흐름

```
[Spring Boot Application Start]
         ↓
[Auto-Configuration 스캔]
         ↓
[SpringBootConfiguration 활성화]
    • @ConditionalOnClass 체크
    • @ConditionalOnProperty 체크
         ↓
[SpringBootProperties 바인딩]
    • application.yml에서 universal-logger.* 속성 읽기
         ↓
[loggerConfig() 또는 loggerInstance() Bean 생성]
    • UniversalConfigLoader로 설정 로드
    • LoggerConfig / LoggerInstance Bean 등록
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

### 4. Spring Framework 통합 흐름

```
[Spring Application Start]
         ↓
[@Configuration 클래스 스캔]
         ↓
[@EnableUniversalLogger 발견]
         ↓
[@Import(SpringConfiguration.class)]
         ↓
[SpringConfiguration 초기화]
         ↓
[ImportAware.setImportMetadata() 호출]
    • @EnableUniversalLogger 속성 읽기
    • configPath, enableSqlCapture 추출
         ↓
[EnvironmentAware.setEnvironment() 호출]
    • Spring Environment 주입
         ↓
[SpringProperties Bean 생성]
    • configPath 설정
    • sqlCapture.enabled 설정
    • Environment 수동 주입
         ↓
[LoggerConfig Bean 생성]
    • UniversalConfigLoader로 설정 로드
    • SpringProperties.getActiveProfile()로 프로파일 탐지
      (logger.profile → Spring Environment activeProfiles)
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

**Spring Framework vs Spring Boot 초기화 차이점**:

| 단계 | Spring Framework | Spring Boot |
|------|------------------|-------------|
| 활성화 트리거 | `@EnableUniversalLogger` 명시적 선언 | Auto-Configuration 자동 스캔 |
| 설정값 소스 | 어노테이션 속성 | application.yml/properties |
| 프로퍼티 바인딩 | `ImportAware` 인터페이스 | `@ConfigurationProperties` |
| Environment 주입 | `EnvironmentAware` 인터페이스 | `@Autowired` |
| 프로파일 탐지 | `SpringProperties.getActiveProfile()` | `SpringBootProperties.getActiveProfile()` |

> 프로파일 설정 방법은 [USER_GUIDE.md](USER_GUIDE.md#환경별-설정-분리)를 참고하세요.