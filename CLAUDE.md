# Universal Logger Java - CLAUDE.md

## 프로젝트 개요
Logback/Log4j2 기반 통합 로깅 라이브러리 + SQL 자동 캡처. Java 8+, Maven 멀티모듈.

## 모듈 구조
```
universal-logger-java/
├── universal-logger-core/       # 공통 모듈 (설정, SQL 캡처, 파서, 필터)
├── universal-logger-logback/    # Logback 구현 (Appender, Spring 통합)
└── universal-logger-log4j2/     # Log4j2 구현 (Appender, Spring 통합)
```

## 빌드 & 실행

```bash
# 빌드 (테스트 스킵)
mvn clean package -DskipTests

# 빌드 (테스트 포함)
mvn clean package

# 로컬 설치
mvn clean install -DskipTests

# Windows 빌드 스크립트
build.bat
```

## 기술 스택
- **Java**: 8 (source/target)
- **빌드**: Maven 3.x, maven-shade-plugin (의존성 쉐이딩)
- **로깅**: SLF4J 1.7.36, Logback 1.2.12, Log4j2 2.20.0
- **설정 파싱**: Jackson 2.15.2 (YAML/JSON/XML), SnakeYAML 2.0
- **HTTP**: Apache HttpClient 4.5.14
- **메일**: JavaMail 1.4.7
- **Spring**: Spring 5.3.31, Spring Boot 2.7.18 (선택적)
- **DB**: MySQL Connector 8.0.33 (선택적)
- **테스트**: JUnit 4.13.2

## 패키지 구조

### Core (`io.universallogger`)
| 패키지 | 역할 |
|---------|------|
| `config` | LoggerConfig (설정 모델), LoggerConfigBuilder |
| `constants` | LoggerConstants (LogLevel, ConfigPath, Defaults 등) |
| `loaders` | UniversalConfigLoader (YAML/JSON/XML 멀티포맷 로드) |
| `parsers` | YamlParser, JsonParser, XmlParser |
| `sql` | SqlCaptureProxy, SqlCapture*Statement, SqlLogger, SqlFormatter |
| `filters` | SensitiveDataFilter (정규식 기반 마스킹) |
| `utils` | ProfileResolver (프로파일 자동 감지) |
| `validation` | ConfigValidator |

### Logback (`io.universallogger.logback`)
| 패키지 | 역할 |
|---------|------|
| (루트) | LoggerInstance (Builder 패턴, 핵심 초기화) |
| `appenders` | DefaultAppenderFactory, WebhookAppender, EmailAppender |
| `formatters` | ConsoleFormatter, FileFormatter |
| `spring` | SpringBootConfiguration, SpringConfiguration, @EnableUniversalLogger |

### Log4j2 (`io.universallogger.log4j2`)
| 패키지 | 역할 |
|---------|------|
| (루트) | ConfigurationFactory (@Plugin, SPI) |
| `appenders` | WebhookAppender, EmailAppender (@Plugin) |
| `spring` | SpringBootConfiguration, SpringConfiguration, @EnableUniversalLogger |

## 설계 패턴
- **Strategy**: ConfigParser, SqlLogger, SqlFormatter, ParameterSubstitutor
- **Factory**: AppenderFactory
- **Proxy**: SqlCaptureProxy → Connection → Statement 래핑 체인
- **Builder**: LoggerInstance.Builder, LoggerConfig.Builder

## 코드 컨벤션
- 패키지: 소문자 (`io.universallogger.config`)
- 클래스: PascalCase, 인터페이스에 I 프리픽스 없음
- 메서드: camelCase
- 상수: UPPER_SNAKE_CASE
- JavaDoc: 모든 공개 API에 작성
- 주석: 한국어 가능
- 인코딩: UTF-8

## 설정 시스템

### 설정 파일 로드 순서 (병합)
1. `defaults/universal-logger-defaults.yml` (JAR 내장 기본값)
2. `META-INF/universal-logger/universal-logger.yml` (사용자 기본)
3. `universal-logger-{profile}.yml` (프로파일별, 선택)

### 프로파일 감지 우선순위
1. `logger.profile` 시스템 속성
2. `spring.profiles.active` 시스템 속성
3. `SPRING_PROFILES_ACTIVE` 환경 변수

### Spring Boot 설정
```yaml
universal-logger:
  enabled: true
  application-name: "MyApp"
  config-path: "META-INF/universal-logger/universal-logger.yml"
  sql-capture:
    enabled: true
```

## 쉐이딩 전략 (충돌 방지)
```
com.fasterxml.jackson → io.universallogger.shaded.jackson
org.yaml.snakeyaml   → io.universallogger.shaded.snakeyaml
org.apache.http      → io.universallogger.shaded.http
javax.mail           → io.universallogger.shaded.mail
```
Spring, SLF4J, MySQL Connector는 제외 (사용자 제공).

## SPI 등록
- **Logback**: `META-INF/services/ch.qos.logback.classic.spi.Configurator`
- **Log4j2**: `META-INF/services/org.apache.logging.log4j.core.config.ConfigurationFactory`
- **Spring Boot**: `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

## 주의사항
- Logback과 Log4j2는 동시 사용 불가 (택 1)
- SQL 캡처는 JDBC DataSource 필수
- 테스트 코드 미작성 상태 (빌드 시 `-DskipTests` 사용)
- 기본 로그 경로: `C:/log/{appName}/`

## 작업 시 체크리스트
- [ ] 변경 후 `mvn clean package -DskipTests`로 빌드 확인
- [ ] 인터페이스 변경 시 Core/Logback/Log4j2 세 모듈 모두 반영
- [ ] 새 의존성 추가 시 쉐이딩 설정 검토
- [ ] Spring 통합 변경 시 Boot/Framework 양쪽 반영
