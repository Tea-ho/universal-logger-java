# Universal Logger Java - 프로젝트 통합 가이드

## 🎯 목적

이 문서는 Universal Logger를 기존 프로젝트에 통합하는 방법을 단계별로 설명합니다.

---

## 📋 통합 방법 요약

### ✅ 지원하는 환경

| 항목 | 지원 여부 |
|-----|---------|
| **빌드 도구** | Maven, Gradle |
| **로깅 프레임워크** | SLF4J, Logback, Log4j (SLF4J 브릿지 사용 시) |
| **설정 파일** | YAML (.yml), XML (.xml), JSON (.json) |
| **Java 버전** | Java 8 이상 |
| **Spring Boot** | 모든 버전 |

---

## 🚀 빠른 통합 (3단계)

### 1단계: 의존성 추가

#### Maven 프로젝트
`pom.xml`에 추가:
```xml
<dependency>
    <groupId>com.universallogger</groupId>
    <artifactId>universal-logger</artifactId>
    <version>1.0.0</version>
</dependency>
```

#### Gradle 프로젝트
`build.gradle`에 추가:
```gradle
dependencies {
    implementation 'com.universallogger:universal-logger:1.0.0'
}
```

### 2단계: 설정 파일 생성

프로젝트 루트 또는 `config` 폴더에 `universal-logger.yml` 생성:

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
    path: "logs/{appName}.log"
```

### 3단계: 코드에서 사용

```java
import com.universallogger.UniversalLogger;

public class MyClass {
    public static void main(String[] args) {
        // 자동으로 설정 파일을 찾아 초기화됨
        UniversalLogger.info("Hello Universal Logger!");
    }
}
```

**끝!** 이제 로깅이 동작합니다.

---

## 📦 프로젝트 유형별 통합 가이드

### 1. 일반 Java 프로젝트

#### 프로젝트 구조
```
my-project/
├── src/
│   └── main/
│       └── java/
│           └── com/example/MyApp.java
├── config/
│   └── universal-logger.yml
├── pom.xml (또는 build.gradle)
└── logs/ (자동 생성)
```

#### 통합 코드
```java
package com.example;

import com.universallogger.UniversalLogger;
import org.slf4j.Logger;

public class MyApp {
    private static final Logger logger = UniversalLogger.getLogger("MyApp");

    public static void main(String[] args) {
        logger.info("애플리케이션 시작");

        try {
            // 비즈니스 로직
            doSomething();
        } catch (Exception e) {
            logger.error("오류 발생", e);
        }

        logger.info("애플리케이션 종료");
    }

    private static void doSomething() {
        logger.debug("doSomething 실행");
    }
}
```

---

### 2. Spring Boot 프로젝트

#### 프로젝트 구조
```
spring-boot-app/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/example/
│       │       ├── Application.java
│       │       ├── controller/
│       │       │   └── UserController.java
│       │       └── service/
│       │           └── UserService.java
│       └── resources/
│           ├── application.yml
│           └── universal-logger.yml
├── config/
│   ├── logging-dev.yml
│   └── logging-prod.yml
└── pom.xml
```

#### Application.java (메인 클래스)
```java
package com.example;

import com.universallogger.UniversalLogger;
import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class Application {

    private static final Logger logger = UniversalLogger.getLogger("Application");

    public static void main(String[] args) {
        // 환경별 설정 파일 로드
        String profile = System.getProperty("spring.profiles.active", "dev");
        String configFile = String.format("config/logging-%s.yml", profile);

        UniversalLogger.init(configFile);
        logger.info("Spring Boot 애플리케이션 시작 (profile: {})", profile);

        SpringApplication.run(Application.class, args);
    }
}
```

#### Controller 예시
```java
package com.example.controller;

import com.universallogger.UniversalLogger;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = UniversalLogger.getLogger("UserController");

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        logger.info("사용자 조회 요청: userId={}", id);

        try {
            User user = userService.findById(id);
            logger.debug("사용자 조회 성공: {}", user);
            return user;
        } catch (Exception e) {
            logger.error("사용자 조회 실패: userId={}", id, e);
            throw e;
        }
    }

    @PostMapping
    public User createUser(@RequestBody UserDto dto) {
        logger.info("사용자 생성 요청: username={}", dto.getUsername());

        try {
            User user = userService.create(dto);
            logger.info("사용자 생성 완료: userId={}", user.getId());
            return user;
        } catch (Exception e) {
            logger.error("사용자 생성 실패: username={}", dto.getUsername(), e);
            throw e;
        }
    }
}
```

#### Service 예시
```java
package com.example.service;

import com.universallogger.UniversalLogger;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private static final Logger logger = UniversalLogger.getLogger("UserService");

    public User create(UserDto dto) {
        logger.debug("사용자 생성 로직 시작: {}", dto);

        // DB 작업
        User user = userRepository.save(dto.toEntity());

        logger.info("사용자 DB 저장 완료: userId={}", user.getId());
        return user;
    }
}
```

#### 환경별 설정 파일

**config/logging-dev.yml** (개발 환경):
```yaml
version: "1.0"

global:
  appName: "MyApp-Dev"

levels:
  root: "DEBUG"
  database: "DEBUG"
  api: "DEBUG"

outputs:
  console:
    enabled: true
    level: "DEBUG"
```

**config/logging-prod.yml** (프로덕션 환경):
```yaml
version: "1.0"

global:
  appName: "MyApp-Prod"

levels:
  root: "INFO"
  database: "WARN"
  api: "INFO"

outputs:
  console:
    enabled: true
    level: "INFO"

  file:
    enabled: true
    level: "INFO"
    path: "/var/log/myapp/{appName}_{date}.log"
    rotation:
      maxSize: "50MB"
      maxFiles: 90

  errorFile:
    enabled: true
    level: "ERROR"
    path: "/var/log/myapp/errors_{date}.log"
```

---

### 3. Maven 멀티 모듈 프로젝트

#### 프로젝트 구조
```
my-parent/
├── pom.xml (부모 POM)
├── config/
│   └── universal-logger.yml (공통 설정)
├── module-api/
│   ├── pom.xml
│   └── src/...
├── module-service/
│   ├── pom.xml
│   └── src/...
└── module-core/
    ├── pom.xml
    └── src/...
```

#### 부모 pom.xml
```xml
<project>
    <groupId>com.example</groupId>
    <artifactId>my-parent</artifactId>
    <version>1.0.0</version>
    <packaging>pom</packaging>

    <modules>
        <module>module-api</module>
        <module>module-service</module>
        <module>module-core</module>
    </modules>

    <dependencyManagement>
        <dependencies>
            <!-- Universal Logger 버전 관리 -->
            <dependency>
                <groupId>com.universallogger</groupId>
                <artifactId>universal-logger</artifactId>
                <version>1.0.0</version>
            </dependency>
        </dependencies>
    </dependencyManagement>
</project>
```

#### 각 모듈의 pom.xml
```xml
<project>
    <parent>
        <groupId>com.example</groupId>
        <artifactId>my-parent</artifactId>
        <version>1.0.0</version>
    </parent>

    <artifactId>module-api</artifactId>

    <dependencies>
        <!-- 버전 생략 (부모에서 관리) -->
        <dependency>
            <groupId>com.universallogger</groupId>
            <artifactId>universal-logger</artifactId>
        </dependency>
    </dependencies>
</project>
```

#### 모듈별 로거 사용
```java
// module-api
Logger apiLogger = UniversalLogger.getLogger("api");

// module-service
Logger serviceLogger = UniversalLogger.getLogger("service");

// module-core
Logger coreLogger = UniversalLogger.getLogger("core");
```

---

### 4. 기존 Log4j/Logback 프로젝트 마이그레이션

#### Before (기존 코드)
```java
// Log4j
private static final Logger logger = Logger.getLogger(MyClass.class);

// Logback
private static final Logger logger = LoggerFactory.getLogger(MyClass.class);
```

#### After (Universal Logger)
```java
// 방법 1: 클래스 기반
private static final Logger logger = UniversalLogger.getLogger(MyClass.class);

// 방법 2: 이름 기반 (권장)
private static final Logger logger = UniversalLogger.getLogger("MyClass");
```

#### 점진적 마이그레이션
```java
public class MyClass {
    // 기존 로거 (변경 불필요)
    private static final Logger oldLogger = LoggerFactory.getLogger(MyClass.class);

    // 새 로거 (Universal Logger)
    private static final Logger newLogger = UniversalLogger.getLogger("MyClass");

    public void method1() {
        oldLogger.info("기존 로거 사용");  // 여전히 동작
    }

    public void method2() {
        newLogger.info("새 로거 사용");  // 새로운 기능 활용
    }
}
```

---

## 🔧 고급 통합

### 1. 동적 설정 변경

```java
import com.universallogger.UniversalLogger;
import com.universallogger.config.LoggerConfig;

public class DynamicConfigExample {
    public static void main(String[] args) {
        // 런타임에 설정 변경
        LoggerConfig config = new LoggerConfig();
        config.getGlobal().setAppName("DynamicApp");

        LoggerConfig.OutputConfig consoleConfig = new LoggerConfig.OutputConfig();
        consoleConfig.setEnabled(true);
        consoleConfig.setLevel("DEBUG");
        config.getOutputs().put("console", consoleConfig);

        UniversalLogger.init(config);
    }
}
```

### 2. 프로그래매틱 초기화

```java
public class ProgrammaticInitExample {
    public static void main(String[] args) {
        // 조건부 초기화
        if (isProduction()) {
            UniversalLogger.init("config/logging-prod.yml");
        } else {
            UniversalLogger.init("config/logging-dev.yml");
        }
    }

    private static boolean isProduction() {
        return "production".equals(System.getenv("ENV"));
    }
}
```

### 3. 커스텀 Appender 추가

```java
// 향후 확장 가능
// 현재는 기본 Appender 사용
```

---

## ⚠️ 주의사항

### 1. 중복 초기화 방지

```java
// ❌ 잘못된 예시 - 여러 번 초기화
UniversalLogger.init("config1.yml");
UniversalLogger.init("config2.yml");  // 무시됨

// ✅ 올바른 예시 - 한 번만 초기화
UniversalLogger.init("config.yml");
```

### 2. Thread-Safety

```java
// UniversalLogger는 Thread-Safe 합니다
// 멀티스레드 환경에서도 안전하게 사용 가능
ExecutorService executor = Executors.newFixedThreadPool(10);
for (int i = 0; i < 100; i++) {
    executor.submit(() -> {
        UniversalLogger.info("안전한 로깅");
    });
}
```

### 3. 성능 고려사항

```yaml
# 대용량 로그 처리 시 성능 설정
performance:
  async: true           # 비동기 로깅 활성화
  bufferSize: 5000      # 버퍼 크기 증가
  maxQueueSize: 50000   # 큐 크기 증가
```

---

## 🧪 테스트

### JUnit 테스트
```java
import org.junit.Before;
import org.junit.Test;

public class MyServiceTest {

    @Before
    public void setUp() {
        // 테스트용 설정 초기화
        UniversalLogger.init("config/logging-test.yml");
    }

    @Test
    public void testSomething() {
        Logger logger = UniversalLogger.getLogger("Test");
        logger.info("테스트 실행 중");

        // 테스트 로직
    }
}
```

---

## 📚 참고 문서

- [사용자 가이드](docs/USER_GUIDE.md)
- [API 문서](docs/API.md)
- [예제 코드](examples/)

---

## 🆘 문제 해결

### 로그가 출력되지 않을 때

1. 설정 파일 위치 확인
2. 로그 레벨 확인
3. 출력 활성화 상태 확인
4. 초기화 메시지 확인:
   ```
   [Universal Logger] Initialized with config: ...
   ```

### 의존성 충돌 시

```xml
<!-- Maven: 특정 의존성 제외 -->
<dependency>
    <groupId>com.universallogger</groupId>
    <artifactId>universal-logger</artifactId>
    <version>1.0.0</version>
    <exclusions>
        <exclusion>
            <groupId>ch.qos.logback</groupId>
            <artifactId>logback-classic</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

---

**Universal Logger Java** - 모든 프로젝트에 쉽게 통합! 🚀