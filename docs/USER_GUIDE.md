# Universal Logger Java 사용자 가이드

## 목차
1. [소개](#소개)
2. [설치 방법](#설치-방법)
3. [설정 파일 작성](#설정-파일-작성)
4. [Spring 프로젝트 설정](#spring-프로젝트-설정)
5. [Spring Boot 프로젝트 설정](#spring-boot-프로젝트-설정)
6. [빠른 시작](#빠른-시작)
7. [고급 사용법](#고급-사용법)
8. [FAQ](#faq)

---

## 소개

**Universal Logger**는 모든 Java 프로젝트에서 사용 가능한 범용 로깅 라이브러리입니다.

### 주요 특징
- Maven/Gradle 모두 지원 - 빌드 도구 관계없이 사용 가능
- SLF4J/Logback 완벽 호환 - 기존 로깅 코드 그대로 사용
- Logback 프로그래밍 방식 설정 - YAML/XML/JSON으로 Logback 완전 제어
- 자동 초기화 - 설정 파일 자동 감지 및 로드
- 에러 알림 기능 - webhook, email을 이용한 error 알림 기능
- SQL 자동 캡처 - 실행 쿼리의 파라미터 자동 로깅

---

## 설치 방법

### Maven 프로젝트

#### 1. pom.xml 의존성 추가

```xml
<dependencies>
    <!-- Universal Logger 라이브러리 -->
    <dependency>
        <groupId>com.universallogger</groupId>
        <artifactId>universal-logger</artifactId>
        <version>1.0.0</version>
    </dependency>

    <!-- Jackson 의존성 (YAML 파싱용) -->
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-core</artifactId>
        <version>2.15.2</version>
    </dependency>
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>2.15.2</version>
    </dependency>
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-annotations</artifactId>
        <version>2.15.2</version>
    </dependency>
    <dependency>
        <groupId>com.fasterxml.jackson.dataformat</groupId>
        <artifactId>jackson-dataformat-yaml</artifactId>
        <version>2.15.2</version>
    </dependency>
</dependencies>
```

#### 2. WAR 파일 생성 설정 (Web Application인 경우)

```xml
<build>
    <plugins>
        <!-- WAR 플러그인 -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-war-plugin</artifactId>
            <version>3.3.2</version>
            <configuration>
                <failOnMissingWebXml>false</failOnMissingWebXml>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### Gradle 프로젝트

#### build.gradle 의존성 추가

```gradle
dependencies {
    // Universal Logger 라이브러리
    implementation 'com.universallogger:universal-logger:1.0.0'

    // Jackson 의존성 (YAML 파싱용) - 이미 포함되어 있으면 생략 가능
    implementation 'com.fasterxml.jackson.core:jackson-databind:2.15.2'
    implementation 'com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.15.2'
}
```

### JAR 파일 직접 추가

1. `universal-logger-1.0.0.jar` 파일을 다운로드
2. 프로젝트의 `libs` 폴더에 복사
3. 빌드 패스에 추가

---

## 설정 파일 작성

### 설정 파일 자동 감지 우선순위 (SEARCH_PATHS)

Universal Logger는 다음 경로에서 설정 파일을 자동으로 검색합니다 (우선순위 순):

#### YAML 설정 파일 (권장)
1. `universal-logger.yml` (프로젝트 루트)
2. `config/universal-logger.yml`
3. `src/main/resources/universal-logger.yml`
4. `classpath:universal-logger.yml`

#### XML 설정 파일 (차선책)
5. `universal-logger.xml` (프로젝트 루트)
6. `config/universal-logger.xml`
7. `src/main/resources/universal-logger.xml`
8. `classpath:universal-logger.xml`
9. `logback.xml`
10. `config/logback.xml`

#### 기본 설정
설정 파일이 없는 경우, 라이브러리 내장 기본 설정(`universal-logger-defaults.yml`)이 자동으로 사용됩니다.

### YAML 설정 방법 (universal-logger.yml)

#### 최소 설정 예시

프로젝트 루트 또는 `src/main/resources` 폴더에 `universal-logger.yml` 파일 생성:

```yaml
version: "1.0"

global:
  appName: "MyApp"
  timezone: "Asia/Seoul"
  encoding: "UTF-8"

levels:
  root: "INFO"

outputs:
  console:
    enabled: true
    level: "INFO"

  file:
    enabled: true
    level: "DEBUG"
    path: "C:/log/{appName}/application.log"
```

#### 완전한 설정 예시

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
  com.mycompany.database: "DEBUG"
  com.mycompany.api: "INFO"
  com.mycompany.security: "WARN"

# SQL 파라미터 치환 설정
sql:
  parameterSubstitution: true

# 출력 설정
outputs:
  # 콘솔 출력
  console:
    enabled: true
    level: "INFO"
    format: "[%d{yyyy-MM-dd HH:mm:ss}] %highlight([%-5level]) %cyan([%thread]) %boldMagenta([%logger{50}:%line]) %msg%n"
    colors:
      DEBUG: "cyan"
      INFO: "green"
      WARN: "yellow"
      ERROR: "red"

  # 파일 출력
  file:
    enabled: true
    level: "DEBUG"
    path: "C:/log/{appName}/application.log"
    format: "[%d{yyyy-MM-dd HH:mm:ss}] [%-5level] [%thread] [%logger{50}:%line] %msg%n"
    rotation:
      maxSize: "50MB"
      maxFiles: 30
      compress: true

  # 에러 파일 (ERROR만)
  errorFile:
    enabled: true
    level: "ERROR"
    path: "C:/log/{appName}/error.log"
    format: "[%d{yyyy-MM-dd HH:mm:ss}] [%-5level] [%thread] [%logger{50}:%line] %msg%n%ex"
    rotation:
      maxSize: "50MB"
      maxFiles: 30
      compress: true

  # webhook 웹훅 (선택사항)
  webhook:
    enabled: false
    level: "ERROR"
    webhook:
      url: "https://hooks.slack.com/services/YOUR/SLACK/WEBHOOK"
      format: "*{level}* in {appName}\\n*Logger:* {logger}\\n*Message:* {message}"

  # 이메일 알림 (선택사항)
  email:
    enabled: false
    level: "ERROR"
    email:
      smtp:
        host: "smtp.gmail.com"
        port: 587
        username: "your-email@gmail.com"
        password: "your-password"
      to: ["admin@company.com"]
      subject: "[{appName}] Error Alert"

# 성능 설정
performance:
  async: true
  bufferSize: 1000
  flushInterval: 5
  maxQueueSize: 10000
```

#### Format 패턴 옵션 상세 설명

YAML 설정 파일의 `format` 필드에서 사용 가능한 패턴 옵션:

| 패턴 | 설명 | 예시 |
|------|------|------|
| `%logger{length}` | logger name 표시. %logger{10}이면 10글자로 축약 | com.example.service.UserServiceImpl |
| `%thread` | 스레드 이름 출력 | main, http-nio-8080-exec-1 |
| `%-5level` | 로그 레벨 출력 (-5는 고정 출력폭) | [DEBUG], [INFO ] |
| `%msg` | 로그 메시지 | 사용자 생성 완료 |
| `%n` | 줄바꿈 (new line) |  |
| `${PID:-}` | 프로세스 ID(PID) 출력 | 12345 |
| `%d{pattern}` | 로그 시각 출력 | %d{yyyy-MM-dd HH:mm:ss} → 2025-12-25 14:30:00 |
| `%p` | 로그 레벨 (%level과 동일) | INFO, ERROR |
| `%F` | 로깅이 발생한 프로그램 파일명 | UserService.java |
| `%M` | 로깅이 발생한 메소드 이름 | createUser |
| `%l` | 로깅이 발생한 호출지 정보 | com.example.UserService.createUser(UserService.java:42) |
| `%L` | 로깅이 발생한 호출지 라인 번호 | 42 |
| `%t` | 스레드 이름 (%thread와 동일) | main |
| `%c` | 로깅이 발생한 카테고리 (logger name) | com.example.UserService |
| `%C` | 클래스 이름 출력 | UserService |
| `%m` | 로그 메시지 (%msg와 동일) | 사용자 생성 완료 |
| `%r` | 애플리케이션 시작 이후부터 로깅 발생 시점까지의 시간(ms) | 1234 |
| `%ex` | 예외 스택 트레이스 출력 |  |
| `%highlight()` | 로그 레벨에 따라 색상 강조 | %highlight([%-5level]) |
| `%cyan()` | cyan 색상 적용 | %cyan([%thread]) |
| `%boldMagenta()` | bold magenta 색상 적용 | %boldMagenta([%logger]) |

Format 예시:
```yaml
# 상세 포맷
format: "[%d{yyyy-MM-dd HH:mm:ss}] [%-5level] [%thread] [%logger{50}:%line] %msg%n"
# 출력: [2025-12-25 14:30:00] [INFO ] [main] [com.example.UserService:42] 사용자 생성 완료

# 색상 포맷 (콘솔용)
format: "[%d{yyyy-MM-dd HH:mm:ss}] %highlight([%-5level]) %cyan([%thread]) %boldMagenta([%logger{50}:%line]) %msg%n"

# 간단한 포맷
format: "%d{HH:mm:ss} %-5level %logger{36} - %msg%n"
# 출력: 14:30:00 INFO  com.example.UserService - 사용자 생성 완료
```

### 기본 설정 파일 (universal-logger-defaults.yml)

설정 파일을 지정하지 않으면 라이브러리 내장 기본 설정이 사용됩니다.
기본 설정 내용:

```yaml
version: "1.0"

global:
  appName: "UniversalLogger"
  timezone: "Asia/Seoul"
  encoding: "UTF-8"

levels:
  root: "INFO"

outputs:
  console:
    enabled: true
    level: "INFO"

  file:
    enabled: true
    level: "DEBUG"
    path: "C:/log/{appName}/application.log"

  errorFile:
    enabled: true
    level: "ERROR"
    path: "C:/log/{appName}/error.log"
```

### XML 설정 방법 (universal-logger.xml)

Logback 표준 XML 형식으로도 설정 가능합니다.

#### 기본 XML 설정 예시

프로젝트 루트 또는 `src/main/resources` 폴더에 `universal-logger.xml` 파일 생성:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <!-- 애플리케이션 이름 -->
    <property name="APP_NAME" value="MyApp" />

    <!-- 로그 파일 경로 -->
    <property name="LOG_PATH" value="C:/log/${APP_NAME}" />

    <!-- 콘솔 출력 -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
            <pattern>[%d{yyyy-MM-dd HH:mm:ss}] %highlight([%-5level]) %cyan([%thread]) %boldMagenta([%logger{50}:%line]) %msg%n</pattern>
            <charset>UTF-8</charset>
        </encoder>
    </appender>

    <!-- 파일 출력 -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_PATH}/application.log</file>
        <encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
            <pattern>[%d{yyyy-MM-dd HH:mm:ss}] [%-5level] [%thread] [%logger{50}:%line] %msg%n</pattern>
            <charset>UTF-8</charset>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>${LOG_PATH}/application-%d{yyyy-MM-dd}.%i.log.gz</fileNamePattern>
            <maxFileSize>50MB</maxFileSize>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
    </appender>

    <!-- 에러 파일 -->
    <appender name="ERROR_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_PATH}/error.log</file>
        <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
            <level>ERROR</level>
        </filter>
        <encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
            <pattern>[%d{yyyy-MM-dd HH:mm:ss}] [%-5level] [%thread] [%logger{50}:%line] %msg%n%ex</pattern>
            <charset>UTF-8</charset>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>${LOG_PATH}/error-%d{yyyy-MM-dd}.%i.log.gz</fileNamePattern>
            <maxFileSize>50MB</maxFileSize>
            <maxHistory>30</maxHistory>
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

## Spring 프로젝트 설정

### 요구사항
- Spring Framework 5.3.20 이상

### 1. ComponentScan 설정

Spring XML 설정 파일에서 Universal Logger 패키지를 스캔하도록 설정:

```xml
<!-- applicationContext.xml -->
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="
           http://www.springframework.org/schema/beans
           http://www.springframework.org/schema/beans/spring-beans.xsd
           http://www.springframework.org/schema/context
           http://www.springframework.org/schema/context/spring-context.xsd">

    <!-- Universal Logger 패키지 스캔 -->
    <context:component-scan base-package="com.universallogger.spring" />

    <!-- 애플리케이션 패키지 스캔 -->
    <context:component-scan base-package="com.mycompany" />
</beans>
```

또는 Java Config 사용:

```java
@Configuration
@ComponentScan(basePackages = {
    "com.universallogger.spring",
    "com.mycompany"
})
public class AppConfig {
    // 추가 Bean 설정
}
```

### 2. Properties 설정

`application.properties` 또는 `config.properties` 파일에 Universal Logger 설정 추가:

```properties
# Universal Logger 설정
universal.logger.config=classpath:universal-logger.yml
universal.logger.app-name=MySpringApp
universal.logger.timezone=Asia/Seoul
universal.logger.encoding=UTF-8

# 로그 레벨
universal.logger.level.root=INFO
universal.logger.level.com.mycompany=DEBUG

# SQL 로깅
universal.logger.sql.parameter-substitution=true
```

### 3. Spring Bean 설정

```java
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserService {
    public void createUser(String username) {
        log.info("사용자 생성 시작: {}", username);

        try {
            // 비즈니스 로직
            log.debug("사용자 검증 완료");
        } catch (Exception e) {
            log.error("사용자 생성 실패", e);
        }
    }
}
```

---

## Spring Boot 프로젝트 설정

### 요구사항
- Spring Boot 2.7.x 이상 (Java 8 호환)

### 1. application.yml 설정

`src/main/resources/application.yml` 파일에 Universal Logger 설정 추가:

```yaml
# Spring Boot 기본 설정
spring:
  application:
    name: my-springboot-app

# Universal Logger 설정
universal:
  logger:
    enabled: true
    config: classpath:universal-logger.yml
    app-name: ${spring.application.name}
    timezone: Asia/Seoul
    encoding: UTF-8

    # 로그 레벨
    level:
      root: INFO
      com.mycompany: DEBUG
      com.mycompany.database: TRACE

    # SQL 로깅
    sql:
      parameter-substitution: true

    # 출력 설정
    outputs:
      console:
        enabled: true
        level: INFO
      file:
        enabled: true
        level: DEBUG
        path: C:/log/${spring.application.name}/application.log
```

또는 `application.properties` 사용:

```properties
# Universal Logger 설정
universal.logger.enabled=true
universal.logger.config=classpath:universal-logger.yml
universal.logger.app-name=${spring.application.name}
universal.logger.timezone=Asia/Seoul
universal.logger.encoding=UTF-8

# 로그 레벨
universal.logger.level.root=INFO
universal.logger.level.com.mycompany=DEBUG

# SQL 로깅
universal.logger.sql.parameter-substitution=true

# 출력 설정
universal.logger.outputs.console.enabled=true
universal.logger.outputs.console.level=INFO
universal.logger.outputs.file.enabled=true
universal.logger.outputs.file.level=DEBUG
universal.logger.outputs.file.path=C:/log/${spring.application.name}/application.log
```

### 2. Auto Configuration

Spring Boot는 Universal Logger를 자동으로 감지하고 설정합니다.
별도의 `@ComponentScan` 설정이 필요 없습니다.

```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

### 3. 컨트롤러에서 사용

```java
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody UserDto dto) {
        log.info("사용자 생성 요청: {}", dto.getUsername());

        try {
            User user = userService.create(dto);
            log.info("사용자 생성 완료: id={}", user.getId());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("사용자 생성 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
}
```

---

## 빠른 시작

### 방법 1: 자동 초기화 (권장)

설정 파일만 프로젝트에 추가하면 자동으로 초기화됩니다.

#### 1단계: 설정 파일 생성

프로젝트 루트 또는 `src/main/resources` 폴더에 `universal-logger.yml` 파일 생성:

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
    path: "C:/log/{appName}/application.log"
```

#### 2단계: 코드에서 사용

```java
import com.universallogger.UniversalLogger;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MyApplication {
    public static void main(String[] args) {
        // 자동으로 universal-logger.yml을 찾아 초기화됨
        log.info("애플리케이션 시작!");
        log.debug("디버그 메시지");
        log.error("에러 발생!");
    }
}
```

### 방법 2: 수동 초기화

설정 파일 경로를 명시적으로 지정할 때 사용합니다.

```java
import com.universallogger.UniversalLogger;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MyApplication {
    public static void main(String[] args) {
        // 설정 파일 경로 지정
        UniversalLogger.init("config/logging.yml");

        log.info("로거 초기화 완료");
    }
}
```

### 방법 3: @Slf4j 사용 (권장)

**로깅은 항상 @Slf4j를 사용하세요.**

```java
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserService {
    public void createUser(String username) {
        log.info("사용자 생성 시작: {}", username);

        try {
            // 비즈니스 로직
            log.debug("사용자 검증 완료");
        } catch (Exception e) {
            log.error("사용자 생성 실패: {}", e.getMessage(), e);
        }
    }
}
```

---

## 고급 사용법

### 모듈별 로그 레벨 관리

```yaml
# universal-logger.yml
levels:
  root: "INFO"                      # 기본 레벨
  com.mycompany.database: "DEBUG"   # DB 관련 상세 로그
  com.mycompany.api: "INFO"         # API는 일반 로그
  com.mycompany.security: "WARN"    # 보안은 경고만
```

```java
public class DatabaseService {
    private static final Logger logger = UniversalLogger.getLogger("com.mycompany.database");

    public void executeQuery(String sql) {
        logger.debug("Executing query: {}", sql);  // DEBUG 레벨로 출력
    }
}

public class SecurityService {
    private static final Logger logger = UniversalLogger.getLogger("com.mycompany.security");

    public void checkPermission(String user) {
        logger.debug("이 메시지는 출력되지 않음");  // WARN 이상만 출력
        logger.warn("권한 확인: {}", user);  // 출력됨
    }
}
```

### 로그 파일 로테이션

```yaml
file:
  enabled: true
  path: "C:/log/{appName}/application.log"
  rotation:
    maxSize: "50MB"      # 파일당 최대 크기
    maxFiles: 30         # 최대 보관 파일 수 (30일)
    compress: true       # gzip 압축 활성화
```

생성되는 파일:
```
C:/log/MyApp/
├── application.log                    # 현재 로그
├── application-2025-12-24.0.log.gz    # 이전 로그 (압축)
└── application-2025-12-23.0.log.gz
```

### SQL 로깅 및 파라미터 치환

```yaml
sql:
  parameterSubstitution: true

levels:
  root: "INFO"
  com.zaxxer.hikari: "DEBUG"
  org.hibernate.SQL: "DEBUG"
  org.hibernate.type.descriptor.sql.BasicBinder: "TRACE"
```

```java
// SQL: SELECT * FROM users WHERE username = ? AND status = ?
// 파라미터: [john, ACTIVE]

// 로그 출력:
// SELECT * FROM users WHERE username = 'john' AND status = 'ACTIVE'
```

---

## FAQ

### Q1. 설정 파일 없이 사용할 수 있나요?

**A:** 네, 가능합니다. 설정 파일이 없으면 기본 설정(콘솔 출력, INFO 레벨)으로 자동 초기화됩니다.

```java
import lombok.extern.slf4j.Slf4j;

// 설정 파일 없이 사용
@Slf4j
public class MyApp {
    public static void main(String[] args) {
        log.info("기본 설정으로 동작");
    }
}
```

### Q2. Maven과 Gradle 중 어떤 것을 사용해야 하나요?

**A:** 프로젝트에서 사용 중인 빌드 도구를 그대로 사용하면 됩니다. Universal Logger는 둘 다 완벽하게 지원합니다.

### Q3. XML과 YAML 중 어떤 것을 선택해야 하나요?

**A:** 둘 다 동일하게 동작합니다:
- **XML**: Logback 표준 형식, 기존 Logback 사용자에게 친숙
- **YAML**: 더 간결하고 읽기 쉬움, 최소 설정 가능 (권장)

### Q4. 기존 Log4j/Logback 코드와 함께 사용할 수 있나요?

**A:** 네, Universal Logger는 Logback을 프로그래밍 방식으로 설정하는 라이브러리이므로 기존 @Slf4j 코드를 그대로 사용할 수 있습니다.

```java
import lombok.extern.slf4j.Slf4j;

// UniversalLogger는 Logback 설정만 담당
UniversalLogger.init("config/logging.yml");

// 로깅은 @Slf4j 사용
@Slf4j
public class MyService {
    public void process() {
        log.info("기존 코드 그대로 사용 가능");
    }
}
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
```yaml
# application-dev.yml
universal:
  logger:
    config: classpath:universal-logger-dev.yml

# application-prod.yml
universal:
  logger:
    config: classpath:universal-logger-prod.yml
```

### Q7. 로그가 출력되지 않아요!

**A:** 다음 사항을 확인하세요:

1. **설정 파일 위치 확인**
   ```
   프로젝트 루트/
   ├── universal-logger.yml  (O)
   └── src/main/resources/
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

### Q9. 한글이 깨져 보임

**A:** UTF-8 인코딩 설정 확인:

```yaml
global:
  encoding: "UTF-8"
```

XML 설정:
```xml
<encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
    <pattern>...</pattern>
    <charset>UTF-8</charset>
</encoder>
```

---

## 문제 해결

### 로그 파일이 생성되지 않음

```yaml
outputs:
  file:
    enabled: true
    path: "C:/log/{appName}/application.log"  # 경로 확인
```

로그 디렉토리 쓰기 권한 확인:
```bash
# Windows
mkdir C:\log\MyApp

# Linux/Mac
mkdir -p /var/log/myapp
chmod 755 /var/log/myapp
```

### 로그 파일 경로 관련

- Windows: `C:/log/{appName}/application.log` (슬래시 사용)
- Linux/Mac: `/var/log/{appName}/application.log`

---

## 지원

- GitHub Issues: https://github.com/yourusername/universal-logger-java/issues
- 문서: https://github.com/yourusername/universal-logger-java/wiki

---

**Universal Logger Java** - 한 번 설정하면 모든 프로젝트에서 바로 사용
