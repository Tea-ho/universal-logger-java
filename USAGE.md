# Universal Logger - 사용 가이드

## 개요
Universal Logger는 Java 애플리케이션에서 강력한 로깅 기능과 자동 SQL 쿼리 캡처를 제공하는 범용 로깅 라이브러리입니다.

## 주요 기능
- ✅ **자동 SQL 캡처**: MyBatis, JPA, JDBC 쿼리 자동 로깅
- ✅ **다중 데이터베이스 지원**: MySQL, Oracle, MSSQL, PostgreSQL, MongoDB
- ✅ **프로젝트명 자동 감지**: `spring.application.name` 기반
- ✅ **표준 로그 경로**: `C:/log/{projectName}/`
- ✅ **파라미터 바인딩 로깅**: PreparedStatement 파라미터 추적
- ✅ **실행 시간 측정**: 쿼리 성능 모니터링

---

## 1. Spring Boot 프로젝트에서 사용

### 1.1 의존성 추가

⚠️ **필수 요구사항**: Jackson 2.13.5 이상 버전 필요

**Gradle:**
```gradle
dependencies {
    implementation files('libs/universal-logger-java-1.0.0.jar')

    // Jackson 2.13.5 이상 필수
    implementation 'com.fasterxml.jackson.core:jackson-core:2.13.5'
    implementation 'com.fasterxml.jackson.core:jackson-databind:2.13.5'
    implementation 'com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.13.5'
}
```

**Maven:**
```xml
<dependency>
    <groupId>com.universallogger</groupId>
    <artifactId>universal-logger-java</artifactId>
    <version>1.0.0</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/libs/universal-logger-java-1.0.0.jar</systemPath>
</dependency>

<!-- Jackson 2.13.5 이상 필수 -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-core</artifactId>
    <version>2.13.5</version>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.13.5</version>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.dataformat</groupId>
    <artifactId>jackson-dataformat-yaml</artifactId>
    <version>2.13.5</version>
</dependency>
```

**기존 프로젝트에서 Jackson 버전 충돌 시:**
```xml
<!-- 기존 Jackson 버전을 2.13.5로 업그레이드 -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-core</artifactId>
    <version>2.13.5</version>  <!-- 2.5.1 등 구버전에서 업그레이드 -->
</dependency>
```

### 1.2 설정 파일 생성

`src/main/resources/config/universal-logger.yml`:

```yaml
version: "1.0"

global:
  appName: "MyApp"
  timezone: "Asia/Seoul"

levels:
  root: "INFO"

outputs:
  console:
    enabled: true
    level: "DEBUG"
    format: "[{timestamp}] [{level}] [{thread}] [{logger}:{line}] - {message}"

  file:
    enabled: true
    level: "DEBUG"
    path: "C:/log/MyApp/app.log"
    rotation:
      maxSize: "50MB"
      maxFiles: 30

  error_file:
    enabled: true
    level: "ERROR"
    path: "C:/log/MyApp/error/error.log"
    rotation:
      maxSize: "50MB"
      maxFiles: 30
```

### 1.3 자동 설정 (Spring Boot)

**Spring Boot는 자동으로 UniversalLogger를 활성화합니다!**

`application.yml` 또는 `application.properties`에 프로젝트명만 설정:

```yaml
spring:
  application:
    name: MyApp
```

**DataSource가 자동으로 wrapping되어 모든 SQL이 로깅됩니다.**

---

## 2. 일반 Spring 프로젝트에서 사용

### 2.1 JavaConfig 방식

```java
import com.universallogger.spring.EnableUniversalLogger;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableUniversalLogger
public class AppConfig {
    // 다른 설정들...
}
```

### 2.2 XML 설정 방식

`applicationContext.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="
           http://www.springframework.org/schema/beans
           http://www.springframework.org/schema/beans/spring-beans.xsd
           http://www.springframework.org/schema/context
           http://www.springframework.org/schema/context/spring-context.xsd">

    <!-- UniversalLogger 활성화 -->
    <context:component-scan base-package="com.universallogger.spring"/>

    <!-- 프로젝트 설정 -->
    <context:property-placeholder location="classpath:application.properties"/>

    <!-- DataSource 설정 -->
    <bean id="dataSource" class="com.zaxxer.hikari.HikariDataSource">
        <!-- ... -->
    </bean>
</beans>
```

`application.properties`:

```properties
spring.application.name=MyApp
universallogger.config.file=config/universal-logger.yml
universallogger.sql.enabled=true
```

---

## 3. 일반 Java 프로젝트에서 사용 (Spring 없이)

### 3.1 수동 초기화

```java
import com.universallogger.UniversalLogger;
import com.universallogger.sql.SqlCaptureProxy;

import javax.sql.DataSource;
import com.zaxxer.hikari.HikariDataSource;

public class Main {
    public static void main(String[] args) {
        // 1. UniversalLogger 초기화
        UniversalLogger.init("config/universal-logger.yml");

        // 2. DataSource 생성
        HikariDataSource originalDataSource = new HikariDataSource();
        originalDataSource.setJdbcUrl("jdbc:mysql://localhost:3306/mydb");
        originalDataSource.setUsername("user");
        originalDataSource.setPassword("password");

        // 3. SQL 캡처를 위해 Proxy로 wrapping
        DataSource dataSource = new SqlCaptureProxy(originalDataSource);

        // 4. 로깅 사용
        UniversalLogger.info("Application started");

        // 5. dataSource를 DAO/Repository에서 사용
        // SQL 쿼리가 자동으로 로깅됩니다
    }
}
```

---

## 4. SQL 로깅 예시

### 4.1 콘솔 출력

```
[2025-10-02 15:35:19] [INFO] [http-nio-8080-exec-1] [universal:400] - [SQL] SELECT * FROM users WHERE id = ? | Parameters: {1=123}
[2025-10-02 15:35:19] [INFO] [http-nio-8080-exec-1] [universal:400] - [SQL-TIME] 15ms

[2025-10-02 15:35:20] [INFO] [http-nio-8080-exec-2] [universal:400] - [SQL] INSERT INTO orders (user_id, product_id, quantity) VALUES (?, ?, ?) | Parameters: {1=123, 2=456, 3=5}
[2025-10-02 15:35:20] [INFO] [http-nio-8080-exec-2] [universal:400] - [SQL-TIME] 8ms

[2025-10-02 15:35:21] [WARN] [http-nio-8080-exec-3] [universal:400] - [SQL-SLOW] Execution time: 1523ms - SELECT * FROM large_table WHERE complex_condition = ?
```

### 4.2 로그 파일

`C:/log/MyApp/app.log`:

```
[2025-10-02 15:35:19] [INFO] [http-nio-8080-exec-1] [universal:400] - [SQL] SELECT * FROM users WHERE id = ? | Parameters: {1=123}
[2025-10-02 15:35:19] [INFO] [http-nio-8080-exec-1] [universal:400] - [SQL-TIME] 15ms
```

---

## 5. 고급 설정

### 5.1 커스텀 로그 경로

```yaml
outputs:
  file:
    enabled: true
    path: "/var/log/myapp/application.log"  # Linux
    # 또는
    path: "D:/logs/myapp/application.log"   # Windows
```

### 5.2 데이터베이스 로깅 (로그를 DB에 저장)

```yaml
outputs:
  database:
    enabled: true
    level: "INFO"
    database:
      type: "mysql"
      host: "127.0.0.1"
      port: 3306
      database: "logs_db"
      table: "app_logs"
      username: "logger"
      password: "password"
```

### 5.3 슬로우 쿼리 임계값 조정

SqlCaptureProxy에서 1초 이상 걸리는 쿼리는 자동으로 `WARN` 레벨로 로깅됩니다.

임계값을 변경하려면 소스 수정:

```java
// SqlCaptureProxy.java
private void logExecutionTime(long timeMs) {
    if (timeMs > 500) {  // 500ms로 변경
        UniversalLogger.warn("[SQL-SLOW] Execution time: " + timeMs + "ms - " + sql);
    } else {
        UniversalLogger.info("[SQL-TIME] " + timeMs + "ms");
    }
}
```

---

## 6. 로깅 API

### 6.1 기본 로깅

```java
import com.universallogger.UniversalLogger;

// 레벨별 로깅
UniversalLogger.debug("Debug message");
UniversalLogger.info("Info message");
UniversalLogger.warn("Warning message");
UniversalLogger.error("Error message");
```

### 6.2 예외 로깅

```java
try {
    // 코드
} catch (Exception e) {
    UniversalLogger.error("Error occurred", e);
}
```

---

## 7. 트러블슈팅

### 7.1 NoSuchMethodError: YAMLFactory._createContentReference

**증상:**
```
java.lang.NoSuchMethodError: 'com.fasterxml.jackson.core.io.ContentReference
com.fasterxml.jackson.dataformat.yaml.YAMLFactory._createContentReference(java.lang.Object)'
```

**원인:** Jackson 버전이 2.13.5 미만

**해결방법:** pom.xml에서 Jackson 버전 업그레이드
```xml
<!-- 기존 버전 -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-core</artifactId>
    <version>2.5.1</version>  <!-- ❌ 구버전 -->
</dependency>

<!-- 업그레이드 후 -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-core</artifactId>
    <version>2.13.5</version>  <!-- ✅ 2.13.5 이상 -->
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.13.5</version>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.dataformat</groupId>
    <artifactId>jackson-dataformat-yaml</artifactId>
    <version>2.13.5</version>  <!-- 추가 필요 -->
</dependency>
```

### 7.2 SQL이 로그에 안 나타남

**확인 사항:**
1. DataSource가 SqlCaptureProxy로 wrapping 되었는지 확인
   ```
   [UniversalLogger] Wrapping DataSource: dataSource
   ```

2. 로그 레벨 확인
   ```yaml
   outputs:
     console:
       level: "DEBUG"  # INFO 이상으로 설정
   ```

3. 로그 파일 확인
   ```
   C:/log/{프로젝트명}/app.log
   ```

### 7.3 콘솔에만 안 나타남

- 로그 파일을 확인하세요. 파일에는 기록됩니다.
- 콘솔 출력 enabled 확인:
  ```yaml
  outputs:
    console:
      enabled: true
  ```

### 7.3 프로젝트명이 잘못 인식됨

`application.yml` 또는 `application.properties`:

```yaml
spring:
  application:
    name: CorrectProjectName
```

---

## 8. 지원하는 기술 스택

- ✅ Spring Boot 2.x, 3.x
- ✅ Spring Framework 4.x, 5.x, 6.x
- ✅ MyBatis 3.x
- ✅ JPA / Hibernate
- ✅ JDBC
- ✅ MySQL, MariaDB
- ✅ Oracle
- ✅ Microsoft SQL Server
- ✅ PostgreSQL
- ✅ H2 (테스트용)

---

## 9. 라이센스

MIT License

---

## 10. 문의 및 기여

- Issues: GitHub Issues
- Pull Requests: Welcome!
