# Universal Logger for Java

Java 애플리케이션을 위한 강력한 로깅 라이브러리로, 자동 SQL 쿼리 캡처 및 다양한 출력 옵션을 제공합니다.

## 📌 Java 버전별 브랜치

| 브랜치 | Java 버전 | JAR 파일명 | 용도 |
|--------|----------|-----------|------|
| **java11** | Java 11+ | universal-logger-java-1.0.0-java11.jar | 최신 버전 (권장) |
| **java8** | Java 8 | universal-logger-java-1.0.0-java8.jar | 레거시 프로젝트 지원 |

**브랜치 전환 및 빌드:**
```bash
# Java 11 버전
git clone https://github.com/Tea-ho/universal-logger-java.git
cd universal-logger-java
git checkout java11
mvn clean package
# 생성 파일: target/universal-logger-java-1.0.0.jar

# Java 8 버전
git clone https://github.com/Tea-ho/universal-logger-java.git
cd universal-logger-java
git checkout java8
mvn clean package
# 생성 파일: target/universal-logger-java-1.0.0.jar
```

**기존 프로젝트에 적용:**
```bash
# 1. 프로젝트의 libs 디렉토리 생성
mkdir -p your-project/libs

# 2. Java 버전에 맞는 JAR 복사
cp target/universal-logger-java-1.0.0.jar your-project/libs/
```

## 주요 기능

- ✅ **자동 SQL 캡처**: MyBatis, JPA, Hibernate, JDBC 쿼리 자동 로깅
- ✅ **실제 호출자 추적**: Repository/Mapper/Service 클래스의 실제 호출 위치 표시
- ✅ **파라미터 바인딩 추적**: PreparedStatement 파라미터 자동 로깅
- ✅ **실행 시간 측정**: 쿼리 성능 모니터링 및 슬로우 쿼리 경고
- ✅ **다중 데이터베이스 지원**: MySQL, Oracle, MSSQL, PostgreSQL, H2
- ✅ **프레임워크 필터링**: MyBatis, Hibernate, Spring AOP, CGLIB 자동 필터링
- ✅ **프로젝트명 자동 감지**: `spring.application.name` 기반 로그 경로 생성
- ✅ **표준 로그 경로**: `C:/log/{projectName}/` 자동 생성
- ✅ **다양한 출력**: 콘솔, 파일, 에러 파일, 데이터베이스, 웹훅, 이메일
- ✅ **Spring Boot & Spring Framework 지원**: 자동 설정 및 수동 설정 모두 지원
- 🔒 **민감한 정보 자동 마스킹**
- 📊 **모듈별 로그 레벨 관리**
- ⚡ **SLF4J & Logback 기반 고성능**
- 🔄 **로그 파일 자동 로테이션**

## 🚀 빠른 시작

### 1. 설치

```bash
# Maven으로 빌드
mvn clean package

# 생성된 JAR를 프로젝트 libs에 복사
cp target/universal-logger-java-1.0.0.jar your-project/libs/
```

### 2. Maven 의존성 추가

```xml
<dependency>
    <groupId>com.universallogger</groupId>
    <artifactId>universal-logger</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 3. 3줄로 시작하기

```java
import com.universallogger.UniversalLogger;

UniversalLogger.init("logging.yml");  // 1줄: 설정 파일로 초기화
UniversalLogger.info("Hello World!"); // 2줄: 로그 사용
```

### 4. 설정 파일 생성

**logging.yml**
```yaml
version: "1.0"

global:
  appName: "JavaApp"

levels:
  root: "INFO"

outputs:
  console:
    enabled: true
    level: "INFO"
    format: "[{timestamp}] {level} - {message}"
    colors:
      INFO: "green"
      ERROR: "red"

  file:
    enabled: true
    level: "DEBUG"
    path: "logs/{appName}_{date}.log"
    format: "{timestamp} | {level} | {logger} | {message}"
```

## 📦 프로젝트 유형별 통합 가이드

### 버전별 요구사항 비교

| 항목 | Java 8 프로젝트 | Java 11+ 프로젝트 |
|-----|----------------|------------------|
| **Java 버전** | Java 8 (1.8) | Java 11 이상 |
| **Universal Logger JAR** | `universal-logger-java-1.0.0-java8.jar` | `universal-logger-java-1.0.0-java11.jar` |
| **Jackson 버전** | 2.13.5+ (필수) | 2.15.2+ |
| **Spring Framework** | Spring 4.x ~ 5.x | Spring Boot 2.x ~ 3.x |
| **빌드 도구** | Maven / Gradle | Maven / Gradle |
| **설정 방식** | XML 또는 YML | YML (권장) |

---

### 📘 Case 1: Java 8 + Maven + XML 프로젝트 (Spring Framework)

**실제 프로젝트 예시: PHM_QUARTZ**

이 가이드는 전통적인 Spring Framework 4.x 기반의 XML 설정을 사용하는 프로젝트에 적용합니다.

#### 1.1 프로젝트 구조

```
PHM_QUARTZ/
├── libs/
│   └── universal-logger-1.0.0.jar           # JAR 파일 배치
├── src/
│   └── main/
│       ├── java/
│       │   └── lims/quartz/
│       │       ├── controller/
│       │       ├── service/
│       │       └── dao/
│       ├── resources/
│       │   ├── spring/
│       │   │   ├── applicationContext.xml   # Spring 설정
│       │   │   └── context-mybatis.xml      # MyBatis 설정
│       │   ├── logback.xml                  # Logback 설정
│       │   └── config/
│       │       └── universal-logger.yml     # Universal Logger 설정
│       └── webapp/
│           └── WEB-INF/
│               └── web.xml
└── pom.xml
```

#### 1.2 pom.xml 설정

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/maven-v4_0_0.xsd">

    <modelVersion>4.0.0</modelVersion>
    <groupId>lims</groupId>
    <artifactId>Quartz</artifactId>
    <version>1.0.0-BUILD-SNAPSHOT</version>
    <packaging>war</packaging>

    <properties>
        <java-version>1.8</java-version>
        <org.springframework-version>4.3.4.RELEASE</org.springframework-version>
        <org.slf4j-version>1.7.25</org.slf4j-version>
    </properties>

    <dependencies>
        <!-- Spring Framework -->
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context</artifactId>
            <version>${org.springframework-version}</version>
        </dependency>

        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-webmvc</artifactId>
            <version>${org.springframework-version}</version>
        </dependency>

        <!-- MyBatis -->
        <dependency>
            <groupId>org.mybatis</groupId>
            <artifactId>mybatis</artifactId>
            <version>3.4.6</version>
        </dependency>

        <dependency>
            <groupId>org.mybatis</groupId>
            <artifactId>mybatis-spring</artifactId>
            <version>1.3.2</version>
        </dependency>

        <!-- JDBC -->
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-jdbc</artifactId>
            <version>${org.springframework-version}</version>
        </dependency>

        <!-- Logback (SLF4J 구현체) -->
        <dependency>
            <groupId>ch.qos.logback</groupId>
            <artifactId>logback-core</artifactId>
            <version>1.2.3</version>
        </dependency>

        <dependency>
            <groupId>ch.qos.logback</groupId>
            <artifactId>logback-classic</artifactId>
            <version>1.2.3</version>
        </dependency>

        <!-- ⚠️ Java 8에서 필수: Jackson 2.13.5+ -->
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

        <!-- Universal Logger (System Scope) -->
        <dependency>
            <groupId>com.universallogger</groupId>
            <artifactId>universal-logger</artifactId>
            <version>1.0.0</version>
            <scope>system</scope>
            <systemPath>${project.basedir}/libs/universal-logger-1.0.0.jar</systemPath>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.8.1</version>
                <configuration>
                    <source>1.8</source>
                    <target>1.8</target>
                    <encoding>UTF-8</encoding>
                </configuration>
            </plugin>

            <!-- WAR 빌드 시 libs 폴더의 JAR 포함 -->
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
                                <include>universal-logger-1.0.0.jar</include>
                            </includes>
                        </resource>
                    </webResources>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

#### 1.3 Spring XML 설정 (applicationContext.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="
           http://www.springframework.org/schema/beans
           http://www.springframework.org/schema/beans/spring-beans.xsd
           http://www.springframework.org/schema/context
           http://www.springframework.org/schema/context/spring-context.xsd">

    <!-- Universal Logger Configuration 활성화 -->
    <context:component-scan base-package="com.universallogger.spring"/>

    <!-- 애플리케이션 컴포넌트 스캔 -->
    <context:component-scan base-package="lims.quartz"/>

    <!-- DataSource 설정 (HikariCP, DBCP 등) -->
    <bean id="dataSource" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.microsoft.sqlserver.jdbc.SQLServerDriver"/>
        <property name="url" value="jdbc:sqlserver://localhost:1433;databaseName=LIMS"/>
        <property name="username" value="sa"/>
        <property name="password" value="password"/>
    </bean>

    <!-- MyBatis SqlSessionFactory -->
    <bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
        <property name="dataSource" ref="dataSource"/>
        <property name="configLocation" value="classpath:spring/context-mybatis.xml"/>
    </bean>

    <!-- MyBatis Mapper 스캔 -->
    <bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">
        <property name="basePackage" value="lims.quartz.dao"/>
    </bean>

    <!-- Universal Logger 프로퍼티 설정 -->
    <bean class="org.springframework.beans.factory.config.PropertyPlaceholderConfigurer">
        <property name="properties">
            <props>
                <prop key="spring.application.name">Quartz</prop>
                <prop key="universallogger.config.file">config/universal-logger.yml</prop>
                <prop key="universallogger.sql.enabled">true</prop>
            </props>
        </property>
    </bean>
</beans>
```

#### 1.4 MyBatis 설정 (context-mybatis.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE configuration PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
    "http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>
    <settings>
        <setting name="jdbcTypeForNull" value="NULL" />
        <setting name="mapUnderscoreToCamelCase" value="true"/>
    </settings>
</configuration>
```

#### 1.5 Universal Logger 설정 (config/universal-logger.yml)

```yaml
version: "1.0"

global:
  appName: "Quartz"

levels:
  root: "INFO"
  com.universallogger: "DEBUG"
  lims.quartz: "DEBUG"

outputs:
  console:
    enabled: true
    level: "INFO"
    format: "[{timestamp}] {level} - {logger} | {message}"
    colors:
      DEBUG: "cyan"
      INFO: "green"
      WARN: "yellow"
      ERROR: "red"

  file:
    enabled: true
    level: "DEBUG"
    path: "C:/log/Quartz/application_{date}.log"
    format: "{timestamp} | {level} | {logger} | {message}"
    rotation:
      maxFileSize: "20MB"
      maxHistory: 60

  errorFile:
    enabled: true
    level: "ERROR"
    path: "C:/log/Quartz/error_{date}.log"
```

#### 1.6 Logback 설정 (logback.xml) - 선택사항

Universal Logger를 사용하면 logback.xml을 대체할 수 있지만, 기존 설정을 유지하고 싶다면 그대로 사용 가능합니다.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <property name="logsPath" value="C:/log/Quartz"/>

    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>[%d{yyyy-MM-dd HH:mm:ss}] [%-5level] [%thread] %logger{50}:%line - %msg%n</pattern>
        </encoder>
    </appender>

    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${logsPath}/application.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${logsPath}/application.%d{yyyy-MM-dd}_%i.log</fileNamePattern>
            <timeBasedFileNamingAndTriggeringPolicy
                class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                <maxFileSize>20MB</maxFileSize>
            </timeBasedFileNamingAndTriggeringPolicy>
            <maxHistory>60</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>[%d{yyyy-MM-dd HH:mm:ss}] [%-5level] %logger{50}:%line - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
    </root>
</configuration>
```

#### 1.7 Java 코드에서 사용

```java
package lims.quartz.service;

import com.universallogger.UniversalLogger;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lims.quartz.dao.TestMapper;

@Service
public class TestService {

    // SLF4J 스타일 로거 (권장)
    private static final Logger logger = UniversalLogger.getLogger("TestService");

    @Autowired
    private TestMapper testMapper;

    public void processTest(String testId) {
        logger.info("테스트 처리 시작: {}", testId);

        try {
            // MyBatis 쿼리 자동 로깅
            // SQL: SELECT * FROM tests WHERE test_id = ? | Parameters: {1=TEST001}
            // TestService.processTest:20 | [SQL-TIME] 15ms
            testMapper.selectByTestId(testId);

            logger.info("테스트 처리 완료: {}", testId);
        } catch (Exception e) {
            logger.error("테스트 처리 실패: {}", e.getMessage(), e);
            throw e;
        }
    }
}
```

#### 1.8 빌드 및 배포

```bash
# Maven 빌드
mvn clean package

# WAR 파일 생성 확인
ls -l target/Quartz.war

# Tomcat 배포
cp target/Quartz.war $CATALINA_HOME/webapps/

# 로그 확인
tail -f C:/log/Quartz/application_2025-10-13.log
```

---

### 📗 Case 2: Java 11 + Gradle + YML 프로젝트 (Spring Boot)

**실제 프로젝트 예시: LIMS_STUDY**

이 가이드는 현대적인 Spring Boot 2.x 기반의 YML 설정을 사용하는 프로젝트에 적용합니다.

#### 2.1 프로젝트 구조

```
LIMS_STUDY/
├── libs/
│   └── universal-logger-1.0.0.jar           # JAR 파일 배치
├── src/
│   └── main/
│       ├── java/
│       │   └── com/lims/lims_study/
│       │       ├── LimsStudyApplication.java
│       │       ├── presentation/
│       │       ├── application/
│       │       ├── domain/
│       │       └── infrastructure/
│       └── resources/
│           ├── application.yml              # Spring Boot 메인 설정
│           └── config/
│               └── universal-logger.yml     # Universal Logger 설정 (선택)
├── build.gradle
└── settings.gradle
```

#### 2.2 build.gradle 설정

```gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '2.7.18'
    id 'io.spring.dependency-management' version '1.0.15.RELEASE'
}

group = 'com.lims'
version = '0.0.1-SNAPSHOT'

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(11)
    }
}

configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Starters
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-security'

    // MyBatis Spring Boot Starter
    implementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter:2.3.2'

    // Database
    runtimeOnly 'com.mysql:mysql-connector-j'

    // Lombok
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    // Universal Logger (libs 폴더에서 로드)
    implementation files('libs/universal-logger-1.0.0.jar')

    // Test
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

tasks.named('test') {
    useJUnitPlatform()
}
```

#### 2.3 application.yml 설정

```yaml
spring:
  application:
    name: LIMS_STUDY

  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/lims?useSSL=false&serverTimezone=Asia/Seoul&allowPublicKeyRetrieval=true
    username: root
    password: password
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000

mybatis:
  mapper-locations: classpath:mappers/**/*.xml
  type-aliases-package: com.lims.lims_study.domain.*.model
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl

# Universal Logger 설정
universal-logger:
  sql-logging-enabled: true
  auto-init: true
  config-file: config/universal-logger.yml

# 기본 로깅 레벨 (Spring Boot 내장)
logging:
  level:
    root: INFO
    com.lims.lims_study: DEBUG
    com.universallogger: DEBUG
```

#### 2.4 Universal Logger 설정 (config/universal-logger.yml)

```yaml
version: "1.0"

global:
  appName: "LIMS_STUDY"

levels:
  root: "INFO"
  com.universallogger: "DEBUG"
  com.lims.lims_study: "DEBUG"

outputs:
  console:
    enabled: true
    level: "DEBUG"
    format: "[{timestamp}] {level} - {logger} | {message}"
    colors:
      DEBUG: "cyan"
      INFO: "green"
      WARN: "yellow"
      ERROR: "red"

  file:
    enabled: true
    level: "DEBUG"
    path: "C:/log/LIMS_STUDY/application_{date}.log"
    format: "{timestamp} | {level} | {logger}:{line} | {message}"
    rotation:
      maxFileSize: "50MB"
      maxHistory: 30

  errorFile:
    enabled: true
    level: "ERROR"
    path: "C:/log/LIMS_STUDY/error_{date}.log"
    format: "{timestamp} | {level} | {logger}:{line} | {message} | {exception}"
```

#### 2.5 Spring Boot 메인 클래스

```java
package com.lims.lims_study;

import com.universallogger.UniversalLogger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import com.universallogger.spring.UniversalLoggerConfiguration;

@SpringBootApplication
@Import(UniversalLoggerConfiguration.class)  // Universal Logger 활성화
public class LimsStudyApplication {

    public static void main(String[] args) {
        // Universal Logger 초기화 (선택: application.yml로 자동 초기화 가능)
        UniversalLogger.info("=== LIMS_STUDY Application Starting ===");

        SpringApplication.run(LimsStudyApplication.class, args);

        UniversalLogger.info("=== LIMS_STUDY Application Started ===");
    }
}
```

#### 2.6 Java 코드에서 사용

```java
package com.lims.lims_study.application.service;

import com.universallogger.UniversalLogger;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.lims.lims_study.domain.test.repository.TestRepository;
import com.lims.lims_study.domain.test.model.Test;

@Service
public class TestService {

    // SLF4J 스타일 로거
    private static final Logger logger = UniversalLogger.getLogger("TestService");

    @Autowired
    private TestRepository testRepository;

    public Test createTest(Test test) {
        logger.info("새 시험 생성 요청: testName={}", test.getTestName());

        try {
            // MyBatis 쿼리 자동 로깅
            // [SQL] INSERT INTO tests (test_name, ...) VALUES (?, ...) | Parameters: {1=혈액검사}
            // TestService.createTest:22 | [SQL-TIME] 8ms
            testRepository.insert(test);

            logger.info("시험 생성 완료: testId={}", test.getTestId());
            return test;
        } catch (Exception e) {
            logger.error("시험 생성 실패: {}", e.getMessage(), e);
            throw e;
        }
    }

    public List<Test> getRecentTests(int limit) {
        logger.debug("최근 시험 조회 요청: limit={}", limit);

        // [SQL] SELECT * FROM tests ORDER BY created_at DESC LIMIT ? | Parameters: {1=10}
        List<Test> tests = testRepository.findRecentTests(limit);

        logger.debug("조회된 시험 수: {}", tests.size());
        return tests;
    }
}
```

#### 2.7 REST Controller 예시

```java
package com.lims.lims_study.presentation.controller;

import com.universallogger.UniversalLogger;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.lims.lims_study.application.service.TestService;

@RestController
@RequestMapping("/api/tests")
public class TestController {

    private static final Logger logger = UniversalLogger.getLogger("TestController");

    @Autowired
    private TestService testService;

    @PostMapping
    public ResponseEntity<Test> createTest(@RequestBody Test test) {
        logger.info("POST /api/tests - 시험 생성 요청");

        Test created = testService.createTest(test);

        logger.info("POST /api/tests - 시험 생성 완료: testId={}", created.getTestId());
        return ResponseEntity.ok(created);
    }

    @GetMapping
    public ResponseEntity<List<Test>> getTests(@RequestParam(defaultValue = "10") int limit) {
        logger.info("GET /api/tests?limit={}", limit);

        List<Test> tests = testService.getRecentTests(limit);

        return ResponseEntity.ok(tests);
    }
}
```

#### 2.8 빌드 및 실행

```bash
# Gradle 빌드
./gradlew clean build

# JAR 파일 생성 확인
ls -l build/libs/LIMS_STUDY-0.0.1-SNAPSHOT.jar

# Spring Boot 애플리케이션 실행
java -jar build/libs/LIMS_STUDY-0.0.1-SNAPSHOT.jar

# 또는 Gradle로 직접 실행
./gradlew bootRun

# 로그 확인
tail -f C:/log/LIMS_STUDY/application_2025-10-13.log
```

---

### 🔍 두 가지 방식 비교 요약

| 구분 | Java 8 + XML (PHM_QUARTZ) | Java 11 + YML (LIMS_STUDY) |
|-----|---------------------------|---------------------------|
| **Spring** | Spring Framework 4.3.4 | Spring Boot 2.7.18 |
| **설정 방식** | XML (applicationContext.xml) | YML (application.yml) |
| **빌드 도구** | Maven | Gradle |
| **패키징** | WAR (Tomcat 배포) | JAR (내장 Tomcat) |
| **MyBatis** | mybatis 3.4.6 + mybatis-spring 1.3.2 | mybatis-spring-boot-starter 2.3.2 |
| **데이터베이스** | MSSQL | MySQL |
| **Jackson 버전** | 2.13.5 (필수) | 2.15.2 (Spring Boot 기본) |
| **초기화** | XML Bean 설정 | @Import 또는 자동 설정 |
| **프로퍼티** | PropertyPlaceholderConfigurer | application.yml |
| **SQL 로깅** | DataSource 자동 래핑 | DataSource 자동 래핑 |

---

## 📖 상세 사용법

### 🔍 SLF4J vs Logback - 어떻게 선택하나요?

**Universal Logger는 둘 다 지원합니다!**

```
┌──────────────────────────────────┐
│  Universal Logger                │
│  (SLF4J API + Logback 구현)      │
└──────────────┬───────────────────┘
               │
        ┌──────┴──────┐
        │             │
    SLF4J 사용    Logback 직접 사용
    프로젝트        프로젝트
        │             │
        └──────┬──────┘
               │
          ✅ 모두 호환!
```

**사용 가이드:**

| 상황 | 권장 방법 | 이유 |
|-----|---------|------|
| **신규 프로젝트** | SLF4J API 사용 | 추상화 계층, 유연성 |
| **기존 SLF4J 프로젝트** | 그대로 사용 | 기존 코드 수정 불필요 |
| **기존 Logback 프로젝트** | 그대로 사용 | 동일 Logback 인스턴스 공유 |
| **둘 다 혼용** | 문제없음 | 내부적으로 같은 구현체 사용 |

### 기본 로깅 (UniversalLogger 직접 사용)

```java
import com.universallogger.UniversalLogger;

// 설정 파일로 초기화
UniversalLogger.init("config/logging.yml");

// 기본 로그 레벨들
UniversalLogger.debug("디버그 메시지");
UniversalLogger.info("정보 메시지");
UniversalLogger.warn("경고 메시지");
UniversalLogger.error("에러 메시지");
```

### SLF4J 스타일 사용 (권장)

```java
import com.universallogger.UniversalLogger;
import org.slf4j.Logger;

// 모듈별로 다른 설정 적용
Logger dbLogger = UniversalLogger.getLogger("database");
Logger apiLogger = UniversalLogger.getLogger("api");

dbLogger.debug("데이터베이스 연결 중...");
apiLogger.info("API 요청 처리 중...");

// 포맷팅된 로그
apiLogger.info("사용자 {}가 {}에 접근했습니다", username, endpoint);
```

### 🔥 자동 SQL 캡처 - 실제 호출자 추적

**Universal Logger의 가장 강력한 기능!**

```java
// DashboardService.java (Line 44)
@Service
public class DashboardService {
    @Autowired
    private TestRepository testRepository;

    public DashboardData getDashboardData() {
        // Repository 호출
        List<RecentTestDto> recentTests = testRepository.findRecentTests(5);
        return new DashboardData(recentTests);
    }
}

// TestRepository.java (MyBatis Mapper)
@Mapper
public interface TestRepository {
    List<RecentTestDto> findRecentTests(@Param("limit") int limit);
}
```

**로그 출력 예시:**

```
2025-10-13 22:30:45 | INFO  | DashboardService.getDashboardData:44 | [SQL] SELECT * FROM tests ORDER BY created_at DESC LIMIT ? | Parameters: {1=5}
2025-10-13 22:30:45 | INFO  | DashboardService.getDashboardData:44 | [SQL-TIME] 15ms
```

**핵심 기능:**
- ✅ **실제 호출자 표시**: `DashboardService.getDashboardData:44` (클래스.메서드:라인번호)
- ✅ **MyBatis Proxy 제거**: `MapperProxy$$` 같은 내부 클래스 필터링
- ✅ **Spring AOP 제거**: `CGLIB$$`, `EnhancerBy` 등 프록시 클래스 필터링
- ✅ **우선순위 탐색**: Mapper > Repository > Dao > Service 순으로 최적의 호출자 선택
- ✅ **다중 프레임워크 지원**: MyBatis, JPA, Hibernate, Spring Data JPA

**지원 프레임워크:**

| 프레임워크 | 필터링 대상 | 결과 |
|-----------|-----------|------|
| **MyBatis** | MapperProxy, MapperMethod | ✅ 실제 Service/Controller 표시 |
| **Hibernate** | org.hibernate.* | ✅ Repository/Service 표시 |
| **Spring Data JPA** | org.springframework.data.jpa.* | ✅ Repository 메서드 표시 |
| **Spring AOP** | CGLIB, EnhancerBy, Aspect, Interceptor | ✅ 실제 비즈니스 로직 표시 |
| **HikariCP** | com.zaxxer.hikari | ✅ 커넥션 풀 제거 |

**자동 적용 - 코드 변경 불필요!**

DataSource를 사용하는 모든 Java 프로젝트에서 자동으로 SQL 캡처가 작동합니다:

```java
// Spring Boot에서 자동 적용
@Configuration
public class DataSourceConfig {
    @Bean
    public DataSource dataSource() {
        // HikariCP, DBCP 등 어떤 DataSource든 자동 래핑
        return new HikariDataSource(config);
    }
}
```

### Logback 직접 사용 (기존 프로젝트)

**기존 Logback을 사용하는 프로젝트에서도 그대로 사용 가능합니다!**

```java
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

public class MyService {
    // 기존 Logback 코드 - 수정 불필요!
    private static final Logger logger =
        (Logger) LoggerFactory.getLogger(MyService.class);

    public void doSomething() {
        // Universal Logger의 설정이 자동으로 적용됨
        logger.info("기존 Logback 코드가 그대로 동작합니다!");
        logger.debug("설정 파일의 레벨/포맷이 적용됨");
    }
}
```

### Spring Boot 통합

```java
@RestController
public class UserController {

    private static final Logger logger = UniversalLogger.getLogger("api");

    static {
        UniversalLogger.init("classpath:logging.yml");
    }

    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        logger.info("새 사용자 생성 요청: {}", user.getUsername());

        try {
            User savedUser = userService.save(user);
            logger.info("사용자 생성 완료: {}", savedUser.getId());
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            logger.error("사용자 생성 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
```

## ⚙️ 설정 옵션

### 출력 대상

| 출력 대상 | 설명 | 설정 키 |
|----------|------|---------|
| 콘솔 | 터미널 출력 (컬러 지원) | `console` |
| 파일 | 로그 파일 (로테이션 지원) | `file` |
| 에러 파일 | 에러 전용 파일 | `errorFile` |
| 데이터베이스 | MySQL, SQLite | `database` |
| 웹훅 | Slack, Discord 등 | `webhook` |
| 이메일 | SMTP 알림 | `email` |

### 포맷 변수

```yaml
format: "{timestamp} | {level} | {logger} | {method}:{line} | {message}"
```

사용 가능한 변수:
- `{timestamp}` - 시간
- `{level}` - 로그 레벨
- `{logger}` - 로거명 (SQL 로깅 시 실제 호출자: 클래스.메서드:라인번호)
- `{class}` - 클래스명
- `{method}` - 메서드명
- `{line}` - 라인 번호
- `{thread}` - 스레드명
- `{message}` - 로그 메시지
- `{appName}` - 애플리케이션 이름

## 📁 프로젝트 구조

```
universal-logger-java/
├── src/main/java/com/universallogger/
│   ├── UniversalLogger.java          # 메인 라이브러리
│   ├── config/
│   │   └── LoggerConfig.java         # 설정 클래스들
│   ├── utils/
│   │   ├── ConfigLoader.java         # 설정 파일 로더
│   │   └── MessageFilter.java        # 메시지 필터
│   ├── formatters/
│   │   ├── CustomFormatter.java      # 커스텀 포맷터
│   │   └── ColorFormatter.java       # 컬러 포맷터
│   ├── appenders/
│   │   ├── WebhookAppender.java      # 웹훅 어펜더
│   │   ├── EmailAppender.java        # 이메일 어펜더
│   │   └── DatabaseAppender.java     # DB 어펜더
│   ├── sql/
│   │   └── SqlCaptureProxy.java      # SQL 자동 캡처 프록시
│   └── spring/
│       ├── UniversalLoggerAutoConfiguration.java
│       └── UniversalLoggerConfiguration.java
├── config/
│   ├── logging.yml                   # 완전한 설정 예시
│   └── simple.yml                    # 간단한 설정 예시
├── examples/
│   ├── QuickStart.java               # 빠른 시작
│   ├── BasicUsage.java               # 기본 사용법
│   └── WebAppExample.java            # 웹앱 예시
├── pom.xml                           # Maven 설정
└── README.md                         # 이 파일
```

## 🔧 고급 설정

### Slack 웹훅 연동

```yaml
outputs:
  webhook:
    enabled: true
    level: "ERROR"
    url: "https://hooks.slack.com/services/YOUR/SLACK/WEBHOOK"
    format: "🚨 *{level}* in {appName}\\n*Logger:* {logger}\\n*Message:* {message}"
```

### 데이터베이스 로깅

```yaml
outputs:
  database:
    enabled: true
    level: "INFO"
    database:
      type: "mysql"
      host: "localhost"
      database: "logs"
      table: "app_logs"
      username: "logger"
      password: "password"
```

### 민감한 정보 마스킹

```yaml
filters:
  sensitiveData:
    enabled: true
    patterns:
      - regex: "password=\\w+"
        replace: "password=***"
      - regex: "token=\\w+"
        replace: "token=***"
      - regex: "\\d{4}-\\d{4}-\\d{4}-\\d{4}"
        replace: "****-****-****-****"
```

## 🧪 테스트

```bash
# 빠른 테스트
mvn exec:java -Dexec.mainClass="com.universallogger.examples.QuickStart"

# 기본 기능 테스트
mvn exec:java -Dexec.mainClass="com.universallogger.examples.BasicUsage"

# 웹앱 시뮬레이션
mvn exec:java -Dexec.mainClass="com.universallogger.examples.WebAppExample"
```

## 📋 요구사항

### java11 브랜치 (Java 11+)
- Java 11 이상
- Maven 3.6+
- SLF4J 1.7.36 (포함됨)
- Logback 1.2.12 (포함됨)
- Jackson 2.15.2 (포함됨)

### java8 브랜치 (Java 8)
- Java 8 (1.8) 이상
- Maven 3.6+
- SLF4J 1.7.36 (포함됨)
- Logback 1.2.12 (포함됨)
- **Jackson 2.13.5 이상** (필수, 포함됨)
  - jackson-core 2.13.5+
  - jackson-databind 2.13.5+
  - jackson-dataformat-yaml 2.13.5+

⚠️ **Java 8 사용 시 주의**:
- Jackson 버전이 2.13.5 미만인 경우 `NoSuchMethodError` 발생
- 기존 프로젝트에서 Jackson 2.5.x 등 구버전 사용 시 버전 업그레이드 필요

## 🌟 핵심 기능 요약

### 1. 자동 SQL 캡처 + 실제 호출자 추적

모든 SQL 쿼리를 자동으로 캡처하고, 실제 비즈니스 로직의 정확한 위치를 표시합니다:

```
✅ DashboardService.getDashboardData:44 | [SQL] SELECT * FROM tests ...
❌ MapperProxy$$Lambda$123.invoke | [SQL] SELECT * FROM tests ...  (이전 방식)
```

### 2. 다중 프레임워크 지원

MyBatis, JPA, Hibernate, Spring Data JPA 등 모든 Java ORM/데이터베이스 프레임워크와 호환됩니다.

### 3. Zero Configuration

DataSource만 있으면 자동으로 SQL 로깅이 활성화됩니다. 추가 설정 불필요!

### 4. 호환성

- ✅ Java 8, 11, 17+ 모두 지원
- ✅ Spring Boot & Spring Framework
- ✅ 기존 SLF4J/Logback 프로젝트와 100% 호환
- ✅ Maven & Gradle

## 📊 로그 출력 예시

### 일반 로그
```
2025-10-13 22:30:45 | INFO  | UserService.createUser:23 | 새 사용자 생성: john_doe
2025-10-13 22:30:45 | ERROR | PaymentService.processPayment:45 | 결제 처리 실패: 잔액 부족
```

### SQL 로그 (자동 캡처)
```
2025-10-13 22:30:45 | INFO  | TestRepository.findAll:15 | [SQL] SELECT * FROM tests WHERE status = ? | Parameters: {1=ACTIVE}
2025-10-13 22:30:45 | INFO  | TestRepository.findAll:15 | [SQL-TIME] 12ms
2025-10-13 22:30:50 | WARN  | UserRepository.findByEmail:28 | [SQL-SLOW] Execution time: 1523ms - SELECT * FROM users WHERE email = ?
```

## 🤝 기여

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'feat: Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Create a Pull Request

## 📄 라이센스

MIT License

---

**Universal Logger Java** - 한 번 설정하면 모든 Java 프로젝트에서 바로 사용! ☕🚀
