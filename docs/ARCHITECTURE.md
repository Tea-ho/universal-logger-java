# Universal Logger Java - 아키텍처 문서

## 📐 설계 원칙

Universal Logger는 다음 원칙을 기반으로 설계되었습니다:

### 1. 객체지향 설계 (OOP)
- **인터페이스 기반 설계**: 확장성과 유지보수성 확보
- **SOLID 원칙 준수**: 견고하고 유연한 구조
- **디자인 패턴 적용**: Facade, Factory, Strategy, Builder 패턴

### 2. DB 독립성
- **추상화 계층**: `DatabaseConnection` 인터페이스
- **다중 DB 지원**: MySQL, Oracle, MSSQL, PostgreSQL, H2
- **Factory 패턴**: DB 타입에 따라 적절한 구현체 자동 생성

### 3. Zero Hard-code
- **상수 중앙 관리**: `LoggerConstants` 클래스
- **설정 기반 동작**: 모든 값은 설정 파일에서 로드
- **기본값 제공**: 설정 누락 시 안전한 기본값 사용

---

## 🏗️ 아키텍처 개요

```
┌─────────────────────────────────────────────────────┐
│                  Application Layer                   │
│         (User Code / Spring Boot / etc.)            │
└────────────────────┬────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────┐
│              Facade Layer (API)                      │
│                UniversalLogger                       │
│   - init()                                          │
│   - getLogger()                                     │
│   - info() / debug() / warn() / error()            │
└────────────────────┬────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────┐
│           Configuration Layer                        │
│   - ConfigLoader (Factory)                          │
│   - ConfigValidator                                 │
│   - LoggerConfig (Model)                            │
└────────────────────┬────────────────────────────────┘
                     │
          ┌──────────┼──────────┐
          │          │          │
┌─────────▼────┐ ┌──▼────┐ ┌───▼──────┐
│  Formatters  │ │Filters│ │Appenders │
│              │ │       │ │(Strategy)│
│ - Custom     │ │Message│ │          │
│ - Color      │ │Filter │ │ - Console│
└──────────────┘ └───────┘ │ - File   │
                            │ - DB     │
                            │ - Webhook│
                            │ - Email  │
                            └────┬─────┘
                                 │
                    ┌────────────▼─────────────┐
                    │ Database Abstraction     │
                    │ DatabaseConnection       │
                    │ (Interface)              │
                    └────┬─────────────────────┘
                         │
          ┌──────────────┼──────────────┐
          │              │              │
    ┌─────▼────┐  ┌──────▼────┐ ┌─────▼─────┐
    │  MySQL   │  │  Oracle   │ │   MSSQL   │
    │Connection│  │Connection │ │Connection │
    └──────────┘  └───────────┘ └───────────┘
                  (+ PostgreSQL, H2)
```

---

## 🔍 계층별 상세 설명

### 1. Facade Layer (API 계층)

**클래스**: `UniversalLogger`

**책임**:
- 사용자에게 간단한 API 제공
- 복잡한 초기화 로직 숨김
- 자동 초기화 및 설정 파일 감지

**주요 메서드**:
```java
// 자동 초기화
public static void autoInit()

// 수동 초기화
public static void init(String configPath)
public static void init(LoggerConfig config)

// 로거 인스턴스 반환
public static Logger getLogger(String name)
public static Logger getLogger(Class<?> clazz)

// 편의 메서드
public static void info(String message)
public static void error(String message, Throwable t)
```

**디자인 패턴**: Facade 패턴
- 복잡한 하위 시스템을 단순한 인터페이스로 제공

---

### 2. Configuration Layer (설정 계층)

#### 2.1 ConfigLoader (Factory)

**책임**:
- 다양한 형식의 설정 파일 로드 (YAML, XML, JSON)
- 클래스패스 및 파일 시스템 리소스 처리

**주요 메서드**:
```java
public static LoggerConfig load(String configPath)
private static LoggerConfig loadYaml(File file)
private static LoggerConfig loadXml(File file)
private static LoggerConfig loadJson(File file)
```

**디자인 패턴**: Factory 패턴
- 설정 파일 형식에 따라 적절한 로더 선택

#### 2.2 ConfigValidator

**책임**:
- 설정의 유효성 검증
- 누락된 필수 값 검출
- 경고 및 오류 메시지 생성

**주요 메서드**:
```java
public static ValidationResult validate(LoggerConfig config)
private static void validateGlobalConfig(...)
private static void validateDatabaseConfig(...)
```

#### 2.3 LoggerConfig (Model)

**책임**:
- 설정 데이터 모델
- 중첩 클래스로 계층 구조 표현

**주요 클래스**:
```java
LoggerConfig
├── GlobalConfig
├── OutputConfig
│   ├── RotationConfig
│   ├── DatabaseConfig
│   ├── WebhookConfig
│   └── EmailConfig
├── FiltersConfig
└── PerformanceConfig
```

---

### 3. Appender Layer (출력 계층)

#### 인터페이스 설계

**LogAppender** (인터페이스):
```java
public interface LogAppender {
    void start();
    void stop();
    void append(ILoggingEvent event);
    String getName();
    boolean isStarted();
}
```

**구현체**:
- ConsoleAppender (기본 제공)
- RollingFileAppender (기본 제공)
- DatabaseAppender (커스텀)
- WebhookAppender (커스텀)
- EmailAppender (커스텀)

**디자인 패턴**: Strategy 패턴
- 런타임에 로그 출력 전략을 선택 가능

---

### 4. Database Abstraction Layer (DB 추상화 계층)

#### 인터페이스 계층

**DatabaseConnection** (인터페이스):
```java
public interface DatabaseConnection {
    Connection getConnection() throws SQLException;
    void close();
    void createTableIfNotExists() throws SQLException;
    boolean testConnection();
    String getDatabaseType();
}
```

#### 추상 클래스

**AbstractDatabaseConnection**:
```java
public abstract class AbstractDatabaseConnection
    implements DatabaseConnection {

    // 공통 로직 구현
    protected abstract String buildJdbcUrl();
    protected abstract String getCreateTableSQL();
}
```

#### 구현체 계층

각 DB별 구현체:

| DB | 클래스 | 기본 포트 | JDBC URL 패턴 |
|----|--------|----------|--------------|
| **MySQL** | MySQLConnection | 3306 | jdbc:mysql://host:port/db |
| **Oracle** | OracleConnection | 1521 | jdbc:oracle:thin:@host:port:sid |
| **MSSQL** | MSSQLConnection | 1433 | jdbc:sqlserver://host:port;databaseName=db |
| **PostgreSQL** | PostgreSQLConnection | 5432 | jdbc:postgresql://host:port/db |
| **H2** | H2Connection | - | jdbc:h2:file:./db 또는 jdbc:h2:mem:db |

**디자인 패턴**:
- **Template Method**: 공통 로직은 부모 클래스, DB별 차이는 자식 클래스
- **Factory**: DatabaseConnectionFactory가 타입에 따라 적절한 구현체 생성

#### Factory 클래스

**DatabaseConnectionFactory**:
```java
public class DatabaseConnectionFactory {
    public static DatabaseConnection createConnection(
        DatabaseConfig config
    ) {
        switch (config.getType().toLowerCase()) {
            case "mysql": return new MySQLConnection(config);
            case "oracle": return new OracleConnection(config);
            case "mssql": return new MSSQLConnection(config);
            case "postgresql": return new PostgreSQLConnection(config);
            case "h2": return new H2Connection(config);
            default: throw new IllegalArgumentException(...);
        }
    }
}
```

**사용 예시**:
```java
DatabaseConfig config = ...; // 설정에서 로드
DatabaseConnection connection =
    DatabaseConnectionFactory.createConnection(config);

// DB 타입에 관계없이 동일한 인터페이스 사용
connection.createTableIfNotExists();
Connection conn = connection.getConnection();
```

---

## 📦 패키지 구조

```
com.universallogger/
├── UniversalLogger.java           # Facade
│
├── config/                         # 설정
│   └── LoggerConfig.java
│
├── constants/                      # 상수 (Hard-code 제거)
│   └── LoggerConstants.java
│
├── validation/                     # 검증
│   └── ConfigValidator.java
│
├── utils/                          # 유틸리티
│   ├── ConfigLoader.java          # Factory
│   └── MessageFilter.java
│
├── formatters/                     # 포맷터
│   ├── CustomFormatter.java
│   └── ColorFormatter.java
│
└── appenders/                      # 출력 (Strategy)
    ├── LogAppender.java           # 인터페이스
    ├── DatabaseAppender.java
    ├── WebhookAppender.java
    ├── EmailAppender.java
    │
    └── database/                   # DB 추상화
        ├── DatabaseConnection.java        # 인터페이스
        ├── DatabaseConnectionFactory.java # Factory
        ├── AbstractDatabaseConnection.java # Template Method
        ├── MySQLConnection.java
        ├── OracleConnection.java
        ├── MSSQLConnection.java
        ├── PostgreSQLConnection.java
        └── H2Connection.java
```

---

## 🎯 SOLID 원칙 적용

### 1. Single Responsibility Principle (단일 책임 원칙)

각 클래스는 하나의 책임만 가짐:

- `UniversalLogger`: API 제공
- `ConfigLoader`: 설정 로드
- `ConfigValidator`: 설정 검증
- `DatabaseConnectionFactory`: DB 연결 객체 생성
- `MySQLConnection`: MySQL 연결 처리

### 2. Open-Closed Principle (개방-폐쇄 원칙)

확장에는 열려있고 수정에는 닫혀있음:

```java
// 새로운 DB 추가 시 기존 코드 수정 불필요
public class MongoDBConnection extends AbstractDatabaseConnection {
    // MongoDB 전용 구현
}

// Factory에만 case 추가
case "mongodb": return new MongoDBConnection(config);
```

### 3. Liskov Substitution Principle (리스코프 치환 원칙)

인터페이스를 구현한 모든 클래스는 상호 치환 가능:

```java
// 어떤 DB든 동일한 방식으로 사용
DatabaseConnection conn = factory.createConnection(config);
conn.testConnection();  // 모든 구현체에서 동일하게 동작
```

### 4. Interface Segregation Principle (인터페이스 분리 원칙)

클라이언트가 사용하지 않는 메서드에 의존하지 않음:

```java
// DatabaseConnection: 데이터베이스 연결에 필요한 메서드만
// LogAppender: 로그 출력에 필요한 메서드만
```

### 5. Dependency Inversion Principle (의존성 역전 원칙)

구체적인 클래스가 아닌 추상화에 의존:

```java
// 구체적인 MySQLConnection이 아닌
// DatabaseConnection 인터페이스에 의존
DatabaseConnection connection = factory.createConnection(config);
```

---

## 🔧 디자인 패턴

### 1. Facade Pattern

**적용**: `UniversalLogger` 클래스

**목적**: 복잡한 하위 시스템을 단순한 인터페이스로 제공

```java
// 복잡한 로직을 숨김
UniversalLogger.info("Simple API");

// 내부적으로는 복잡한 처리
// - 설정 파일 자동 감지
// - ConfigLoader 호출
// - Logback 설정
// - Appender 초기화
```

### 2. Factory Pattern

**적용**:
- `ConfigLoader`: 설정 파일 형식에 따라 로더 생성
- `DatabaseConnectionFactory`: DB 타입에 따라 연결 객체 생성

**목적**: 객체 생성 로직을 캡슐화

```java
// 사용자는 어떤 구현체가 생성되는지 몰라도 됨
DatabaseConnection conn =
    DatabaseConnectionFactory.createConnection(config);
```

### 3. Strategy Pattern

**적용**: `LogAppender` 계층

**목적**: 런타임에 알고리즘(전략) 선택

```java
// 설정에 따라 다른 출력 전략 사용
outputs:
  console: ...    # ConsoleAppender
  file: ...       # FileAppender
  database: ...   # DatabaseAppender
```

### 4. Template Method Pattern

**적용**: `AbstractDatabaseConnection`

**목적**: 공통 로직은 상위 클래스, 가변 로직은 하위 클래스

```java
public abstract class AbstractDatabaseConnection {
    // Template Method (공통 로직)
    public Connection getConnection() {
        String url = buildJdbcUrl();  // 하위 클래스 구현
        return DriverManager.getConnection(url, ...);
    }

    // Hook Method (하위 클래스에서 구현)
    protected abstract String buildJdbcUrl();
}
```

### 5. Builder Pattern

**적용**: `LoggerConfig` 계층

**목적**: 복잡한 객체를 단계적으로 생성

```java
LoggerConfig config = new LoggerConfig();
config.getGlobal().setAppName("MyApp");
config.getLevels().put("root", "INFO");
// ...
```

---

## 🔒 Hard-code 제거 전략

### 1. 상수 중앙 관리

**LoggerConstants** 클래스:
```java
public final class LoggerConstants {
    public static final class Defaults {
        public static final String APP_NAME = "UniversalLogger";
        public static final String LOG_LEVEL = "INFO";
        // ...
    }

    public static final class DatabasePort {
        public static final int MYSQL = 3306;
        public static final int ORACLE = 1521;
        // ...
    }
}
```

**사용 예시**:
```java
// ❌ Hard-coded
String appName = "MyApp";
int port = 3306;

// ✅ 상수 사용
String appName = LoggerConstants.Defaults.APP_NAME;
int port = LoggerConstants.DatabasePort.MYSQL;
```

### 2. 설정 기반 동작

모든 동작은 설정 파일에서 제어:

```yaml
# 모든 값을 설정 파일에서 지정
global:
  appName: "MyApp"        # 앱 이름
  timezone: "Asia/Seoul"  # 타임존
  encoding: "UTF-8"       # 인코딩

database:
  type: "mysql"           # DB 타입
  host: "localhost"       # 호스트
  port: 3306              # 포트
  # ...
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

## 🎓 확장 가이드

### 새로운 데이터베이스 추가

1. **구현체 생성**:
```java
public class MongoDBConnection extends AbstractDatabaseConnection {
    @Override
    protected String buildJdbcUrl() {
        return "mongodb://...";
    }

    @Override
    protected String getCreateTableSQL() {
        // MongoDB는 자동 생성
        return null;
    }
}
```

2. **Factory에 등록**:
```java
case "mongodb":
    return new MongoDBConnection(config);
```

3. **상수 추가**:
```java
public static final class DatabaseType {
    public static final String MONGODB = "mongodb";
}
```

### 새로운 Appender 추가

1. **인터페이스 구현**:
```java
public class SyslogAppender implements LogAppender {
    public void append(ILoggingEvent event) {
        // Syslog 전송 로직
    }
}
```

2. **설정에 추가**:
```yaml
outputs:
  syslog:
    enabled: true
    host: "syslog.example.com"
```

---

## 📊 성능 고려사항

### 1. 비동기 로깅

```yaml
performance:
  async: true           # 비동기 활성화
  bufferSize: 1000      # 버퍼 크기
  maxQueueSize: 10000   # 큐 크기
```

### 2. Connection Pooling

데이터베이스 연결 풀링 적용 권장:
```java
// HikariCP 등 사용
```

### 3. 메모리 효율

- ConcurrentHashMap으로 Logger 인스턴스 재사용
- 설정은 한 번만 로드하여 메모리 효율성 확보

---

## 🔍 테스트 전략

### 1. 단위 테스트

각 계층별로 독립적인 테스트:
```java
@Test
public void testMySQLConnection() {
    DatabaseConfig config = createMySQLConfig();
    DatabaseConnection conn = new MySQLConnection(config);
    assertTrue(conn.testConnection());
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

## 📝 결론

Universal Logger Java는:
- ✅ **객체지향 설계**: 인터페이스, 추상화, 디자인 패턴 적용
- ✅ **DB 독립성**: 5개 DB 지원, 쉬운 확장
- ✅ **Zero Hard-code**: 모든 값은 설정 또는 상수로 관리
- ✅ **SOLID 원칙**: 유지보수 용이한 구조
- ✅ **확장 가능**: 새로운 DB, Appender 추가 용이

**견고하고 확장 가능한 엔터프라이즈급 로깅 라이브러리입니다.** 🚀