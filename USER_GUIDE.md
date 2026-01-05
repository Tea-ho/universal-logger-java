# Universal Logger - User Guide

이 문서는 Universal Logger의 상세 사용법과 실전 예제를 제공합니다.

## 목차

1. [프로젝트 설정](#프로젝트-설정)
2. [기본 사용법](#기본-사용법)
3. [고급 설정](#고급-설정)
4. [Spring 통합](#spring-통합)
5. [SQL 로깅](#sql-로깅)
6. [알림 설정](#알림-설정)
7. [실전 예제](#실전-예제)
8. [문제 해결](#문제-해결)

## 프로젝트 설정

### 로깅 프레임워크 선택

먼저 Logback과 Log4j2 중 하나를 선택합니다.

#### Logback (권장)

**장점:**
- Spring Boot 기본 프레임워크
- 안정적이고 검증된 구현
- 풍부한 생태계

**Maven:**
```xml
<dependencies>
    <!-- Universal Logger Logback -->
    <dependency>
        <groupId>com.universallogger</groupId>
        <artifactId>universal-logger-logback</artifactId>
        <version>1.0.0</version>
    </dependency>

    <!-- SLF4J (이미 포함되어 있을 수 있음) -->
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-api</artifactId>
        <version>1.7.36</version>
    </dependency>
</dependencies>
```

**Gradle:**
```gradle
dependencies {
    implementation 'com.universallogger:universal-logger-logback:1.0.0'
    implementation 'org.slf4j:slf4j-api:1.7.36'
}
```

#### Log4j2

**장점:**
- 더 빠른 성능 (비동기 로깅)
- 더 낮은 메모리 사용량
- 플러그인 아키텍처

**Maven:**
```xml
<dependencies>
    <!-- Universal Logger Log4j2 -->
    <dependency>
        <groupId>com.universallogger</groupId>
        <artifactId>universal-logger-log4j2</artifactId>
        <version>1.0.0</version>
    </dependency>

    <!-- SLF4J -->
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-api</artifactId>
        <version>1.7.36</version>
    </dependency>

    <!-- Log4j2 to SLF4J Bridge -->
    <dependency>
        <groupId>org.apache.logging.log4j</groupId>
        <artifactId>log4j-slf4j-impl</artifactId>
        <version>2.17.1</version>
    </dependency>
</dependencies>
```

**Gradle:**
```gradle
dependencies {
    implementation 'com.universallogger:universal-logger-log4j2:1.0.0'
    implementation 'org.slf4j:slf4j-api:1.7.36'
    implementation 'org.apache.logging.log4j:log4j-slf4j-impl:2.17.1'
}
```

### 로컬 JAR 사용 (옵션)

```xml
<!-- Logback -->
<dependency>
    <groupId>com.universallogger</groupId>
    <artifactId>universal-logger-logback</artifactId>
    <version>1.0.0</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/libs/universal-logger-logback-1.0.0.jar</systemPath>
</dependency>
<dependency>
    <groupId>com.universallogger</groupId>
    <artifactId>universal-logger-core</artifactId>
    <version>1.0.0</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/libs/universal-logger-core-1.0.0.jar</systemPath>
</dependency>
```

## 기본 사용법

### 1. 설정 파일 생성

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
    format: "[%d{yyyy-MM-dd HH:mm:ss}] [%-5level] [%thread] [%logger{50}:%line] %msg%n"

  file:
    enabled: true
    level: "DEBUG"
    path: "C:/log/{appName}/application.log"
    rotation:
      maxSize: "50MB"
      maxFiles: 30
      compress: true

  errorFile:
    enabled: true
    level: "ERROR"
    path: "C:/log/{appName}/error/error.log"
```

### 2. SLF4J로 로깅

```java
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserService {

    public void createUser(String name, String email) {
        log.info("Creating user: name={}, email={}", name, email);

        try {
            // 비즈니스 로직
            saveToDatabase(name, email);
            log.debug("User saved successfully");
        } catch (Exception e) {
            log.error("Failed to create user", e);
        }
    }
}
```

### 3. 로그 레벨

```java
log.trace("Very detailed information");  // TRACE
log.debug("Debug information");          // DEBUG
log.info("General information");         // INFO
log.warn("Warning message");             // WARN
log.error("Error occurred", exception);  // ERROR
```

## 고급 설정

### 패키지별 로그 레벨 설정

```yaml
levels:
  root: "INFO"

  # 우리 애플리케이션
  com.myapp: "DEBUG"
  com.myapp.service: "TRACE"
  com.myapp.repository: "DEBUG"

  # 외부 라이브러리
  org.springframework: "WARN"
  org.hibernate: "WARN"
  org.hibernate.SQL: "DEBUG"
  org.hibernate.type.descriptor.sql.BasicBinder: "TRACE"
```

### 로그 포맷 커스터마이징

#### 간단한 포맷
```yaml
outputs:
  console:
    format: "%d{HH:mm:ss} %-5level %logger{0} - %msg%n"
```
출력:
```
14:23:45 INFO  UserService - Creating user: John
```

#### 상세 포맷
```yaml
outputs:
  console:
    format: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
```
출력:
```
2026-01-05 14:23:45.123 [http-nio-8080-exec-1] INFO  c.m.service.UserService - Creating user: John
```

#### 디버그 포맷 (파일명, 메서드, 라인 포함)
```yaml
outputs:
  file:
    format: "%d{HH:mm:ss} %-5level [%thread] %logger{36}.%method:%line - %msg%n"
```
출력:
```
14:23:45 DEBUG [main] c.m.service.UserService.createUser:42 - User validation passed
```

### 파일 로테이션 설정

```yaml
outputs:
  file:
    enabled: true
    path: "C:/log/{appName}/application.log"
    rotation:
      maxSize: "100MB"      # 파일 최대 크기
      maxFiles: 60          # 보관할 파일 개수
      compress: true        # 압축 활성화 (.gz)
```

**결과:**
```
application.log
application.log.2026-01-04.gz
application.log.2026-01-03.gz
...
application.log.2025-12-06.gz  (60일분)
```

### 민감 데이터 필터링

```yaml
filters:
  sensitiveData:
    enabled: true
    patterns:
      # 패스워드 마스킹
      - regex: "password=[^&\\s]+"
        replace: "password=***"

      # 토큰 마스킹
      - regex: "token=[^&\\s]+"
        replace: "token=***"

      # 신용카드 번호
      - regex: "\\d{4}-\\d{4}-\\d{4}-\\d{4}"
        replace: "****-****-****-****"

      # 이메일
      - regex: "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
        replace: "***@***.***"
```

**적용 전:**
```
User login: email=john@example.com, password=secret123
```

**적용 후:**
```
User login: email=***@***.***, password=***
```

### 성능 최적화

```yaml
performance:
  async: true              # 비동기 로깅 활성화
  bufferSize: 8192         # 버퍼 크기
  flushInterval: 1000      # 플러시 간격 (ms)
  maxQueueSize: 10000      # 큐 최대 크기
```

**비동기 로깅 효과:**
- 메인 스레드 블로킹 감소
- 대량 로그 처리 시 성능 향상
- 응답 시간 단축

## Spring 통합

### Spring Boot (자동 설정)

**의존성만 추가하면 자동 활성화됩니다.**

**application.yml (Logback):**
```yaml
spring:
  application:
    name: MyApp

# Universal Logger 설정 (선택 사항)
universal-logger:
  enabled: true                                    # 기본값: true
  config-path: "config/universal-logger.yml"      # 기본값
  sql-capture:
    enabled: true                                  # SQL 자동 캡처 (기본값: true)
```

**application.yml (Log4j2):**
```yaml
spring:
  application:
    name: MyApp

universal-logger-log4j2:
  enabled: true
  config-path: "config/universal-logger.yml"
  sql-capture:
    enabled: true
```

**프로젝트 구조:**
```
my-spring-boot-app/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/myapp/
│   │   │       ├── MyApplication.java
│   │   │       └── service/
│   │   │           └── UserService.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── config/
│   │           └── universal-logger.yml
│   └── test/
└── pom.xml
```

### 일반 Spring (수동 설정)

#### Logback
```java
import com.universallogger.logback.spring.UniversalLoggerConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(UniversalLoggerConfiguration.class)
public class AppConfig {
    // LoggerConfig Bean이 자동 생성됨
    // DataSource가 자동으로 SQL 캡처 프록시로 래핑됨
}
```

#### Log4j2
```java
import com.universallogger.log4j2.spring.UniversalLoggerConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(UniversalLoggerConfiguration.class)
public class AppConfig {
    // LoggerConfig Bean이 자동 생성됨
    // DataSource가 자동으로 SQL 캡처 프록시로 래핑됨
}
```

#### XML 설정
```xml
<!-- applicationContext.xml -->
<beans>
    <!-- Component Scan으로 자동 로드 -->
    <context:component-scan base-package="com.universallogger.logback.spring" />

    <!-- 또는 직접 Bean 등록 -->
    <bean class="com.universallogger.logback.spring.UniversalLoggerConfiguration" />
</beans>
```

### Spring에서 LoggerConfig 주입

```java
import com.universallogger.config.LoggerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LoggingService {

    @Autowired
    private LoggerConfig loggerConfig;

    public void printConfig() {
        System.out.println("App Name: " + loggerConfig.getGlobal().getAppName());
        System.out.println("Root Level: " + loggerConfig.getLevels().get("root"));
    }
}
```

## SQL 로깅

### 자동 캡처 (Spring Boot)

**application.yml:**
```yaml
universal-logger:
  sql-capture:
    enabled: true
```

**Spring Boot가 자동으로:**
1. DataSource Bean 발견
2. SqlCaptureProxy로 래핑
3. SQL 쿼리 자동 로깅

### 수동 캡처 (순수 Java)

```java
import com.universallogger.sql.SqlCaptureProxy;
import com.universallogger.config.LoggerConfig;
import com.universallogger.loaders.UniversalConfigLoader;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;

public class DatabaseConfig {

    public DataSource dataSource() {
        // 원본 DataSource 생성
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setUrl("jdbc:mysql://localhost:3306/mydb");
        dataSource.setUsername("user");
        dataSource.setPassword("password");

        // LoggerConfig 로드
        LoggerConfig config = new UniversalConfigLoader()
            .load("config/universal-logger.yml");

        // SqlCaptureProxy로 래핑
        return new SqlCaptureProxy(dataSource, config);
    }
}
```

### SQL 로그 출력 예시

```yaml
sql:
  parameterSubstitution: false  # 파라미터를 ? 로 표시
```

**출력:**
```
[2026-01-05 10:30:45] [INFO ] [main] [SQL:40] [SQL] SELECT * FROM users WHERE id = ? AND status = ?
[2026-01-05 10:30:45] [INFO ] [main] [SQL:45] [SQL-PARAMS] Parameters: [user-123, ACTIVE]
[2026-01-05 10:30:45] [INFO ] [main] [SQL:108] [SQL-ROWS] Total: 1
[2026-01-05 10:30:45] [INFO ] [main] [SQL:50] [SQL-TIME] 15ms
```

```yaml
sql:
  parameterSubstitution: true  # 파라미터를 실제 값으로 치환
```

**출력:**
```
[2026-01-05 10:30:45] [INFO ] [main] [SQL:40] [SQL] SELECT * FROM users WHERE id = 'user-123' AND status = 'ACTIVE'
[2026-01-05 10:30:45] [INFO ] [main] [SQL:108] [SQL-ROWS] Total: 1
[2026-01-05 10:30:45] [INFO ] [main] [SQL:50] [SQL-TIME] 15ms
```

### MyBatis 통합

**설정 없이 자동으로 작동합니다!**

```java
@Mapper
public interface UserMapper {
    @Select("SELECT * FROM users WHERE id = #{id}")
    User findById(String id);
}

// 사용
User user = userMapper.findById("user-123");
// SQL이 자동으로 로깅됨
```

### JPA/Hibernate 통합

**application.yml:**
```yaml
spring:
  jpa:
    show-sql: false  # Hibernate의 기본 SQL 로깅 끄기
    properties:
      hibernate:
        format_sql: true

# Universal Logger로 SQL 캡처
universal-logger:
  sql-capture:
    enabled: true
```

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    private String id;
    private String name;
    // ...
}

// 사용
userRepository.findById("user-123");
// SQL이 자동으로 로깅됨
```

## 알림 설정

### Slack 웹훅

**1. Slack Incoming Webhook 생성:**
1. Slack 워크스페이스 설정
2. Apps → Incoming Webhooks 검색
3. Add to Slack
4. 채널 선택
5. Webhook URL 복사

**2. Universal Logger 설정:**
```yaml
outputs:
  webhook:
    enabled: true
    level: "ERROR"
    webhook:
      url: "https://hooks.slack.com/services/T00000000/B00000000/XXXXXXXXXXXXXXXXXXXX"
      format: "🚨 *{level}* in {appName}\\n*Logger:* {logger}\\n*Message:* {message}"
```

**3. 테스트:**
```java
log.error("Test error for Slack notification");
```

**Slack 메시지:**
```
🚨 ERROR in MyApp
Logger: com.myapp.UserService
Message: Test error for Slack notification
```

### Discord 웹훅

**1. Discord Webhook 생성:**
1. Discord 서버 설정
2. 통합 → 웹훅 → 새 웹훅
3. 웹훅 URL 복사

**2. Universal Logger 설정:**
```yaml
outputs:
  webhook:
    enabled: true
    level: "ERROR"
    webhook:
      url: "https://discord.com/api/webhooks/123456789/abcdefghijk"
      format: "🚨 **{level}** in {appName}\\n**Logger:** {logger}\\n**Message:** {message}"
```

### 이메일 알림

**Gmail 예시:**

**1. Gmail 앱 비밀번호 생성:**
1. Google 계정 → 보안
2. 2단계 인증 활성화
3. 앱 비밀번호 생성
4. 비밀번호 저장

**2. Universal Logger 설정:**
```yaml
outputs:
  email:
    enabled: true
    level: "ERROR"
    email:
      smtp:
        host: "smtp.gmail.com"
        port: 587
        username: "your-email@gmail.com"
        password: "your-app-password"  # 앱 비밀번호
      from: "noreply@myapp.com"
      to:
        - "admin@example.com"
        - "dev-team@example.com"
      subject: "[{appName}] 에러 발생"
```

**3. 테스트:**
```java
log.error("Critical error occurred!");
```

**이메일 내용:**
```
제목: [MyApp] 에러 발생

본문:
Error Level: ERROR
Logger: com.myapp.UserService
Time: 2026-01-05 15:30:45
Message: Critical error occurred!

Stack Trace:
...
```

### 회사 SMTP 서버 예시

```yaml
outputs:
  email:
    enabled: true
    level: "ERROR"
    email:
      smtp:
        host: "smtps.hiworks.com"
        port: 465
        username: "alert@interfaceit.co.kr"
        password: "company-password"
      from: "alert@interfaceit.co.kr"
      to:
        - "manager@interfaceit.co.kr"
      subject: "[{appName}] 에러 발생"
```

## 실전 예제

### 예제 1: Spring Boot + MyBatis + Slack

**프로젝트 구조:**
```
my-app/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/myapp/
│   │   │       ├── Application.java
│   │   │       ├── controller/
│   │   │       │   └── UserController.java
│   │   │       ├── service/
│   │   │       │   └── UserService.java
│   │   │       └── mapper/
│   │   │           └── UserMapper.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── config/
│   │           └── universal-logger.yml
└── pom.xml
```

**pom.xml:**
```xml
<dependencies>
    <!-- Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- MyBatis -->
    <dependency>
        <groupId>org.mybatis.spring.boot</groupId>
        <artifactId>mybatis-spring-boot-starter</artifactId>
        <version>2.2.0</version>
    </dependency>

    <!-- Universal Logger Logback -->
    <dependency>
        <groupId>com.universallogger</groupId>
        <artifactId>universal-logger-logback</artifactId>
        <version>1.0.0</version>
    </dependency>

    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>
</dependencies>
```

**application.yml:**
```yaml
spring:
  application:
    name: MyApp
  datasource:
    url: jdbc:mysql://localhost:3306/mydb
    username: user
    password: password

universal-logger:
  enabled: true
  sql-capture:
    enabled: true
```

**config/universal-logger.yml:**
```yaml
version: "1.0"

global:
  appName: "MyApp"

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
    path: "C:/log/{appName}/application.log"

  webhook:
    enabled: true
    level: "ERROR"
    webhook:
      url: "https://hooks.slack.com/services/YOUR/WEBHOOK/URL"
      format: "🚨 *{level}* in {appName}\\n*Message:* {message}"
```

**UserService.java:**
```java
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserService {

    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public User findUser(String id) {
        log.info("Finding user: {}", id);

        try {
            User user = userMapper.findById(id);
            // SQL이 자동으로 로깅됨!

            if (user == null) {
                log.warn("User not found: {}", id);
                return null;
            }

            log.debug("User found: {}", user);
            return user;
        } catch (Exception e) {
            log.error("Failed to find user: {}", id, e);
            // Slack으로 에러 알림 전송됨!
            throw e;
        }
    }
}
```

### 예제 2: 멀티 환경 설정

**개발 환경:**
`config/universal-logger-dev.yml`
```yaml
version: "1.0"

global:
  appName: "MyApp-DEV"

levels:
  root: "DEBUG"
  com.myapp: "TRACE"

outputs:
  console:
    enabled: true
    level: "DEBUG"

  file:
    enabled: false  # 개발 환경에서는 파일 로그 비활성화

  webhook:
    enabled: false  # 개발 환경에서는 Slack 알림 비활성화
```

**운영 환경:**
`config/universal-logger-prod.yml`
```yaml
version: "1.0"

global:
  appName: "MyApp-PROD"

levels:
  root: "INFO"
  com.myapp: "INFO"

outputs:
  console:
    enabled: true
    level: "WARN"

  file:
    enabled: true
    path: "C:/log/{appName}/application.log"
    rotation:
      maxSize: "100MB"
      maxFiles: 60

  webhook:
    enabled: true
    level: "ERROR"
    webhook:
      url: "https://hooks.slack.com/services/PROD/WEBHOOK/URL"

  email:
    enabled: true
    level: "ERROR"
    email:
      smtp:
        host: "smtp.company.com"
        port: 587
        username: "alert@company.com"
        password: "${SMTP_PASSWORD}"
      from: "alert@company.com"
      to:
        - "ops-team@company.com"
      subject: "[PROD] {appName} 에러 발생"
```

**application.yml:**
```yaml
spring:
  profiles:
    active: ${APP_ENV:dev}

universal-logger:
  config-path: "config/universal-logger-${spring.profiles.active}.yml"
```

### 예제 3: 커스텀 로그 포맷

**JSON 스타일 로그:**
```yaml
outputs:
  file:
    enabled: true
    path: "C:/log/{appName}/application.json"
    format: '{"timestamp":"%d{yyyy-MM-dd HH:mm:ss}","level":"%-5level","thread":"%thread","logger":"%logger","message":"%msg"}%n'
```

**출력:**
```json
{"timestamp":"2026-01-05 10:30:45","level":"INFO ","thread":"main","logger":"c.m.UserService","message":"User created"}
{"timestamp":"2026-01-05 10:30:46","level":"ERROR","thread":"main","logger":"c.m.UserService","message":"Failed to save"}
```

## 문제 해결

### 1. 로그가 출력되지 않음

**증상**: 애플리케이션을 실행해도 로그가 표시되지 않음

**해결:**

1. **설정 파일 위치 확인:**
   ```bash
   # 설정 파일이 있는지 확인
   ls src/main/resources/config/universal-logger.yml
   ```

2. **application.yml 확인:**
   ```yaml
   universal-logger:
     enabled: true  # false가 아닌지 확인
   ```

3. **의존성 확인:**
   ```xml
   <!-- universal-logger-logback 또는 log4j2가 포함되어 있는지 확인 -->
   <dependency>
       <groupId>com.universallogger</groupId>
       <artifactId>universal-logger-logback</artifactId>
       <version>1.0.0</version>
   </dependency>
   ```

4. **디버그 모드 활성화:**
   ```yaml
   # universal-logger.yml
   global:
     debug: true  # 디버그 로그 활성화
   ```

### 2. SQL이 캡처되지 않음

**증상**: SQL 쿼리가 로그에 나타나지 않음

**해결:**

1. **SQL 캡처 활성화 확인:**
   ```yaml
   universal-logger:
     sql-capture:
       enabled: true
   ```

2. **DataSource Bean 확인:**
   ```java
   @Autowired
   private DataSource dataSource;

   @PostConstruct
   public void checkDataSource() {
       System.out.println("DataSource class: " + dataSource.getClass().getName());
       // SqlCaptureProxy가 나와야 함
   }
   ```

3. **로그 레벨 확인:**
   ```yaml
   levels:
     com.universallogger.sql: "DEBUG"
   ```

### 3. 웹훅이 전송되지 않음

**증상**: Slack/Discord로 알림이 오지 않음

**해결:**

1. **웹훅 URL 확인:**
   - Slack/Discord에서 웹훅 URL이 올바른지 확인
   - 테스트 요청 보내기:
   ```bash
   curl -X POST "YOUR_WEBHOOK_URL" \
     -H "Content-Type: application/json" \
     -d '{"text":"Test message"}'
   ```

2. **방화벽 확인:**
   - Outbound HTTPS(443 포트) 허용 여부 확인
   - 프록시 설정 필요 여부 확인

3. **로그 레벨 확인:**
   ```yaml
   outputs:
     webhook:
       level: "ERROR"  # ERROR 레벨만 전송됨
   ```

### 4. 이메일이 전송되지 않음

**증상**: 이메일 알림이 오지 않음

**해결:**

1. **SMTP 자격증명 확인:**
   ```yaml
   email:
     smtp:
       host: "smtp.gmail.com"
       port: 587  # Gmail은 587, 일부는 465
       username: "your-email@gmail.com"
       password: "your-app-password"  # 앱 비밀번호 사용
   ```

2. **Gmail 앱 비밀번호:**
   - Gmail은 일반 비밀번호가 아닌 앱 비밀번호 필요
   - 2단계 인증 활성화 후 앱 비밀번호 생성

3. **네트워크 확인:**
   ```bash
   # SMTP 서버 연결 테스트
   telnet smtp.gmail.com 587
   ```

### 5. 로그 파일이 생성되지 않음

**증상**: 로그 파일이 지정된 경로에 생성되지 않음

**해결:**

1. **디렉토리 권한 확인:**
   ```bash
   # Windows
   mkdir C:\log\MyApp

   # Linux/Mac
   sudo mkdir -p /var/log/myapp
   sudo chown user:user /var/log/myapp
   ```

2. **절대 경로 사용:**
   ```yaml
   file:
     path: "C:/log/{appName}/application.log"  # 절대 경로
   ```

3. **appName 변수 확인:**
   ```yaml
   global:
     appName: "MyApp"  # 설정되어 있는지 확인
   ```

### 6. 한글이 깨짐

**증상**: 로그에 한글이 ??? 또는 깨진 문자로 표시

**해결:**

1. **인코딩 설정:**
   ```yaml
   global:
     encoding: "UTF-8"
   ```

2. **JVM 옵션:**
   ```bash
   java -Dfile.encoding=UTF-8 -jar myapp.jar
   ```

3. **Windows 콘솔:**
   ```bash
   # 콘솔 인코딩 변경
   chcp 65001
   ```

## 모범 사례

### 1. 로그 레벨 사용 가이드

- **TRACE**: 매우 상세한 정보 (개발 중에만 사용)
- **DEBUG**: 디버깅 정보 (개발/테스트 환경)
- **INFO**: 일반적인 정보 (운영 환경 권장)
- **WARN**: 경고 (잠재적 문제)
- **ERROR**: 에러 (즉각적인 대응 필요)

### 2. 구조화된 로깅

**좋은 예:**
```java
log.info("User login successful: userId={}, ip={}, timestamp={}",
    userId, ipAddress, timestamp);
```

**나쁜 예:**
```java
log.info("User " + userId + " logged in from " + ipAddress);
```

### 3. 예외 로깅

**좋은 예:**
```java
try {
    // ...
} catch (Exception e) {
    log.error("Failed to process order: orderId={}", orderId, e);
    // 예외 객체를 마지막 파라미터로 전달
}
```

**나쁜 예:**
```java
catch (Exception e) {
    log.error("Error: " + e.getMessage());
    // 스택 트레이스가 손실됨
}
```

### 4. 성능 최적화

**조건부 로깅:**
```java
// 좋은 예
if (log.isDebugEnabled()) {
    log.debug("Complex calculation result: {}", expensiveMethod());
}

// 나쁜 예
log.debug("Complex calculation result: {}", expensiveMethod());
// expensiveMethod()가 항상 호출됨
```

### 5. 민감 데이터 보호

**필터 사용:**
```yaml
filters:
  sensitiveData:
    enabled: true
```

**코드에서 마스킹:**
```java
log.info("User registered: email={}", maskEmail(email));

private String maskEmail(String email) {
    return email.replaceAll("(.{3}).*(@.*)", "$1***$2");
}
```

## 다음 단계

- [아키텍처 가이드](ARCHITECTURE.md) - 프로젝트 구조 이해
- [GitHub Issues](https://github.com/your-repo/issues) - 문제 리포트
- [예제 프로젝트](examples/) - 실전 예제 코드

## 지원

문제가 해결되지 않으면 이슈를 등록해주세요:
https://github.com/your-repo/universal-logger-java/issues
