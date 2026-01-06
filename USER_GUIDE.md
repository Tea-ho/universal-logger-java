# 🪵 Universal Logger - User Guide

<aside>
⚠️ 주의사항

- Java 8 이상 환경에서 사용 가능합니다.
- Spring Boot 환경에서는 의존성 추가만으로 자동 활성화됩니다.
- SQL 자동 캡처는 트랜잭션 환경에서 정상 동작합니다.
- 로컬 JAR 사용 시 WEB-INF/lib에 포함되어야 합니다.

</aside>

## ✅ 로깅 프레임워크 선택

Universal Logger는 **Logback**과 **Log4j2** 중 하나를 선택할 수 있습니다.

### Logback (권장)

- Spring Boot 기본 프레임워크
- 안정적이고 검증된 구현
- 대부분의 프로젝트에 적합

### Log4j2

- 더 빠른 성능 (비동기 로깅)
- 대량 로그 처리에 유리
- 고성능이 필요한 경우 선택

## ✅ 라이브러리 설치

<aside>

### Maven (로컬 JAR)

---

**Logback 사용:**

```xml
<dependencies>
    <!-- Universal Logger Core -->
    <dependency>
        <groupId>com.universallogger</groupId>
        <artifactId>universal-logger-core</artifactId>
        <version>1.0.0</version>
        <scope>system</scope>
        <systemPath>${project.basedir}/libs/universal-logger-core-1.0.0.jar</systemPath>
    </dependency>

    <!-- Universal Logger Logback -->
    <dependency>
        <groupId>com.universallogger</groupId>
        <artifactId>universal-logger-logback</artifactId>
        <version>1.0.0</version>
        <scope>system</scope>
        <systemPath>${project.basedir}/libs/universal-logger-logback-1.0.0.jar</systemPath>
    </dependency>

    <!-- Logback Dependencies -->
    <dependency>
        <groupId>ch.qos.logback</groupId>
        <artifactId>logback-classic</artifactId>
        <version>1.2.12</version>
    </dependency>

    <!-- SLF4J API -->
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-api</artifactId>
        <version>1.7.36</version>
    </dependency>
</dependencies>

<build>
    <plugins>
        <!-- WAR 배포 시 JAR 포함 -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-war-plugin</artifactId>
            <version>3.3.2</version>
            <configuration>
                <webResources>
                    <resource>
                        <directory>${project.basedir}/libs</directory>
                        <targetPath>WEB-INF/lib</targetPath>
                        <includes>
                            <include>universal-logger-core-1.0.0.jar</include>
                            <include>universal-logger-logback-1.0.0.jar</include>
                        </includes>
                    </resource>
                </webResources>
            </configuration>
        </plugin>
    </plugins>
</build>
```

**Log4j2 사용:**

```xml
<dependencies>
    <!-- Universal Logger Core -->
    <dependency>
        <groupId>com.universallogger</groupId>
        <artifactId>universal-logger-core</artifactId>
        <version>1.0.0</version>
        <scope>system</scope>
        <systemPath>${project.basedir}/libs/universal-logger-core-1.0.0.jar</systemPath>
    </dependency>

    <!-- Universal Logger Log4j2 -->
    <dependency>
        <groupId>com.universallogger</groupId>
        <artifactId>universal-logger-log4j2</artifactId>
        <version>1.0.0</version>
        <scope>system</scope>
        <systemPath>${project.basedir}/libs/universal-logger-log4j2-1.0.0.jar</systemPath>
    </dependency>

    <!-- Log4j2 Dependencies -->
    <dependency>
        <groupId>org.apache.logging.log4j</groupId>
        <artifactId>log4j-slf4j-impl</artifactId>
        <version>2.17.1</version>
    </dependency>

    <!-- SLF4J API -->
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-api</artifactId>
        <version>1.7.36</version>
    </dependency>
</dependencies>

<build>
    <plugins>
        <!-- WAR 배포 시 JAR 포함 -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-war-plugin</artifactId>
            <version>3.3.2</version>
            <configuration>
                <webResources>
                    <resource>
                        <directory>${project.basedir}/libs</directory>
                        <targetPath>WEB-INF/lib</targetPath>
                        <includes>
                            <include>universal-logger-core-1.0.0.jar</include>
                            <include>universal-logger-log4j2-1.0.0.jar</include>
                        </includes>
                    </resource>
                </webResources>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### Gradle (로컬 JAR)

---

**Logback 사용:**

```gradle
configurations {
    all {
        exclude group: 'org.springframework.boot', module: 'spring-boot-starter-logging'
    }
}

repositories {
    mavenCentral()
    flatDir {
        dirs 'libs'
    }
}

dependencies {
    implementation files('libs/universal-logger-core-1.0.0.jar')
    implementation files('libs/universal-logger-logback-1.0.0.jar')
    implementation 'ch.qos.logback:logback-classic:1.2.12'
    implementation 'org.slf4j:slf4j-api:1.7.36'
}
```

**Log4j2 사용:**

```gradle
configurations {
    all {
        exclude group: 'org.springframework.boot', module: 'spring-boot-starter-logging'
    }
}

repositories {
    mavenCentral()
    flatDir {
        dirs 'libs'
    }
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-log4j2'
    implementation files('libs/universal-logger-core-1.0.0.jar')
    implementation files('libs/universal-logger-log4j2-1.0.0.jar')
}
```

</aside>

## ✅ 기본 설정

<aside>

### 설정 파일 생성

---

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

  errorFile:
    enabled: true
    level: "ERROR"
    path: "C:/log/{appName}/error/error.log"
```

<aside>
💡 설정 파일 자동 탐색 순서

1. `config/universal-logger.yml`
2. `universal-logger.yml`
3. `classpath:config/universal-logger.yml`

</aside>

### Spring Boot 설정

---

**application.yml (Logback):**

```yaml
spring:
  application:
    name: MyApp

universal-logger:
  enabled: true
  config-path: "config/universal-logger.yml"
  sql-capture:
    enabled: true
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

</aside>

## ✅ 사용법

<aside>

### 기본 로깅

---

```java
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserService {

    public void createUser(String name) {
        log.info("Creating user: {}", name);

        try {
            // 비즈니스 로직
            saveUser(name);
            log.debug("User created successfully");
        } catch (Exception e) {
            log.error("Failed to create user: {}", name, e);
        }
    }
}
```

### 로그 레벨

---

| 레벨 | 용도 | 환경 |
|------|------|------|
| **TRACE** | 매우 상세한 정보 | 개발 전용 |
| **DEBUG** | 디버깅 정보 | 개발/테스트 |
| **INFO** | 일반 정보 | 운영 권장 |
| **WARN** | 경고 (잠재적 문제) | 모든 환경 |
| **ERROR** | 에러 (즉시 대응 필요) | 모든 환경 |

</aside>

## ✅ SQL 로깅

<aside>

### 자동 캡처 (Spring Boot)

---

**설정만 활성화하면 자동으로 SQL 캡처:**

```yaml
universal-logger:
  sql-capture:
    enabled: true
```

**출력 예시:**

```yaml
sql:
  parameterSubstitution: true  # 파라미터를 실제 값으로 치환
```

```
[2026-01-05 10:30:45] [INFO ] [main] [SQL] SELECT * FROM users WHERE id = 'user-123' AND status = 'ACTIVE'
[2026-01-05 10:30:45] [INFO ] [main] [SQL-ROWS] Total: 1
[2026-01-05 10:30:45] [INFO ] [main] [SQL-TIME] 15ms
```

### 수동 캡처 (순수 Java)

---

```java
import com.universallogger.sql.SqlCaptureProxy;
import com.universallogger.config.LoggerConfig;
import com.universallogger.loaders.UniversalConfigLoader;

// LoggerConfig 로드
LoggerConfig config = new UniversalConfigLoader()
    .load("config/universal-logger.yml");

// DataSource 래핑
DataSource original = ...;
DataSource wrapped = new SqlCaptureProxy(original, config);
```

<aside>
❗ **주의사항**

SQL 캡처는 다음 프레임워크에서 자동으로 작동합니다:
- MyBatis
- JPA (Hibernate, EclipseLink)
- Spring JDBC
- 순수 JDBC

</aside>

</aside>

## ✅ 알림 설정

<aside>

### Slack 웹훅

---

**1. Slack Incoming Webhook URL 생성**

1. Slack 워크스페이스 → Apps
2. Incoming Webhooks 검색 → Add to Slack
3. 채널 선택 → Webhook URL 복사

**2. 설정 파일에 추가**

```yaml
outputs:
  webhook:
    enabled: true
    level: "ERROR"
    webhook:
      url: "https://hooks.slack.com/services/YOUR/WEBHOOK/URL"
      format: "🚨 *{level}* in {appName}\\n*Logger:* {logger}\\n*Message:* {message}"
```

**3. 테스트**

```java
log.error("Test error for Slack notification");
```

### Discord 웹훅

---

**설정:**

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

---

**Gmail 설정:**

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
        password: "your-app-password"  # Gmail 앱 비밀번호
      from: "noreply@myapp.com"
      to:
        - "admin@example.com"
      subject: "[{appName}] 에러 발생"
```

<aside>
💡 **Gmail 앱 비밀번호 생성**

1. Google 계정 → 보안
2. 2단계 인증 활성화
3. 앱 비밀번호 생성

</aside>

</aside>

## ✅ 고급 설정

<aside>

### 패키지별 로그 레벨

---

```yaml
levels:
  root: "INFO"
  com.myapp: "DEBUG"
  com.myapp.service: "TRACE"
  org.springframework: "WARN"
  org.hibernate.SQL: "DEBUG"
```

### 민감 데이터 필터링

---

```yaml
filters:
  sensitiveData:
    enabled: true
    patterns:
      - regex: "password=[^&\\s]+"
        replace: "password=***"
      - regex: "\\d{4}-\\d{4}-\\d{4}-\\d{4}"
        replace: "****-****-****-****"
```

### 파일 로테이션

---

```yaml
outputs:
  file:
    rotation:
      maxSize: "100MB"      # 파일 최대 크기
      maxFiles: 60          # 보관 파일 개수 (60일)
      compress: true        # 압축 (.gz)
```

### 성능 최적화

---

```yaml
performance:
  async: true              # 비동기 로깅
  bufferSize: 8192
  flushInterval: 1000
  maxQueueSize: 10000
```

</aside>

## ✅ 멀티 환경 설정

<aside>

### 환경별 설정 파일

---

**개발 환경** (`config/universal-logger-dev.yml`):

```yaml
version: "1.0"

global:
  appName: "MyApp-DEV"

levels:
  root: "DEBUG"

outputs:
  console:
    enabled: true
  file:
    enabled: false
  webhook:
    enabled: false
```

**운영 환경** (`config/universal-logger-prod.yml`):

```yaml
version: "1.0"

global:
  appName: "MyApp-PROD"

levels:
  root: "INFO"

outputs:
  console:
    enabled: true
    level: "WARN"
  file:
    enabled: true
    path: "C:/log/{appName}/application.log"
  webhook:
    enabled: true
    level: "ERROR"
```

**application.yml:**

```yaml
spring:
  profiles:
    active: ${APP_ENV:dev}

universal-logger:
  config-path: "config/universal-logger-${spring.profiles.active}.yml"
```

</aside>

## ✅ 문제 해결

<aside>

### 로그가 출력되지 않음

---

**확인사항:**

1. **설정 파일 위치**
   ```bash
   ls src/main/resources/config/universal-logger.yml
   ```

2. **application.yml**
   ```yaml
   universal-logger:
     enabled: true
   ```

3. **의존성 확인**
   ```xml
   <!-- universal-logger-logback 또는 log4j2 포함 여부 -->
   ```

### SQL이 캡처되지 않음

---

**해결방법:**

1. **SQL 캡처 활성화**
   ```yaml
   universal-logger:
     sql-capture:
       enabled: true
   ```

2. **DataSource 확인**
   ```java
   System.out.println(dataSource.getClass().getName());
   // SqlCaptureProxy가 출력되어야 함
   ```

### 웹훅이 전송되지 않음

---

**확인사항:**

1. **웹훅 URL 테스트**
   ```bash
   curl -X POST "YOUR_WEBHOOK_URL" \
     -H "Content-Type: application/json" \
     -d '{"text":"Test"}'
   ```

2. **방화벽 확인**
   - HTTPS(443) Outbound 허용 확인

3. **로그 레벨 확인**
   ```yaml
   webhook:
     level: "ERROR"  # ERROR 레벨만 전송
   ```

### 한글이 깨짐

---

**해결방법:**

```yaml
global:
  encoding: "UTF-8"
```

```bash
# JVM 옵션
java -Dfile.encoding=UTF-8 -jar myapp.jar
```

</aside>

## 📘 API 레퍼런스

<aside>

### LoggerConfig

---

**설정 로드:**

```java
import com.universallogger.loaders.UniversalConfigLoader;
import com.universallogger.config.LoggerConfig;

UniversalConfigLoader loader = new UniversalConfigLoader();
LoggerConfig config = loader.load("config/universal-logger.yml");
```

**Builder 패턴:**

```java
import com.universallogger.config.LoggerConfigBuilder;

LoggerConfig config = LoggerConfigBuilder.create()
    .appName("my-app")
    .rootLevel("INFO")
    .console()
        .enabled(true)
        .level("INFO")
        .and()
    .file()
        .enabled(true)
        .path("logs/app.log")
        .and()
    .build();
```

### SqlCaptureProxy

---

**DataSource 래핑:**

```java
import com.universallogger.sql.SqlCaptureProxy;

DataSource wrapped = new SqlCaptureProxy(originalDataSource, config);
```

### Spring Properties

---

**Logback:**

| 속성 | 기본값 | 설명 |
|------|--------|------|
| `universal-logger.enabled` | `true` | 활성화 여부 |
| `universal-logger.config-path` | `config/universal-logger.yml` | 설정 파일 경로 |
| `universal-logger.sql-capture.enabled` | `true` | SQL 캡처 활성화 |

**Log4j2:**

| 속성 | 기본값 | 설명 |
|------|--------|------|
| `universal-logger-log4j2.enabled` | `true` | 활성화 여부 |
| `universal-logger-log4j2.config-path` | `config/universal-logger.yml` | 설정 파일 경로 |
| `universal-logger-log4j2.sql-capture.enabled` | `true` | SQL 캡처 활성화 |

</aside>

## 📝 모범 사례

<aside>

### 구조화된 로깅

---

**좋은 예:**
```java
log.info("User login: userId={}, ip={}", userId, ipAddress);
```

**나쁜 예:**
```java
log.info("User " + userId + " logged in from " + ipAddress);
```

### 예외 로깅

---

**좋은 예:**
```java
try {
    // ...
} catch (Exception e) {
    log.error("Failed to process: orderId={}", orderId, e);
}
```

**나쁜 예:**
```java
catch (Exception e) {
    log.error("Error: " + e.getMessage());
}
```

### 조건부 로깅 (성능)

---

```java
if (log.isDebugEnabled()) {
    log.debug("Result: {}", expensiveMethod());
}
```

</aside>

## 🔗 참고 자료

- [README.md](README.md) - 프로젝트 소개
- [ARCHITECTURE.md](ARCHITECTURE.md) - 아키텍처 가이드
- [GitHub Issues](https://github.com/Tea-ho/universal-logger-java/issues) - 이슈 및 지원
