# Universal Logger - User Guide

## 개요

Universal Logger는 **Spring / Java 환경에서 공통 로깅과 SQL 캡처 기능을 제공하는 로깅 라이브러리**입니다. Logback 또는 Log4j2 중 하나를 선택하여 사용할 수 있으며, 설정 파일 기반으로 일관된 로그 포맷과 출력 정책을 관리할 수 있습니다.

## 목차

1. [주의사항](#주의사항)
2. [로깅 프레임워크 선택](#로깅-프레임워크-선택)
3. [라이브러리 빌드 및 설치](#라이브러리-빌드-및-설치)
4. [빌드 도구별 설정](#빌드-도구별-설정)
5. [기본 설정](#기본-설정)
   - [환경별 설정 분리](#환경별-설정-분리)
6. [사용법](#사용법)

## 주의사항

> **Warning**
> - **Java 8 이상** 환경에서 사용 가능합니다.
> - Spring Boot 환경에서는 의존성 추가만으로 자동 활성화됩니다.
> - SQL 자동 캡처는 트랜잭션 환경에서 정상 동작합니다.
> - Logback과 Log4j2는 **동시에 사용할 수 없습니다. (택 1)**

## 로깅 프레임워크 선택

Universal Logger는 아래 로깅 프레임워크 중 **하나를 선택**하여 사용합니다.

- **Logback**
- **Log4j2**

> 선택한 로깅 프레임워크에 맞는 전용 라이브러리만 추가해야 합니다.

## 라이브러리 빌드 및 설치

### 라이브러리 빌드

사용 중인 OS 환경에 맞는 스크립트를 실행합니다.

```bash
# Windows
./build.bat

# Linux / macOS
./build.sh
```

> 빌드가 완료되면 각 모듈의 `/target` 경로에 JAR 파일이 생성됩니다.

### 생성되는 라이브러리

- **로깅 프레임워크별 라이브러리 (택1)**
  - Logback 사용 시: `universal-logger-logback-{version}.jar`
  - Log4j2 사용 시: `universal-logger-log4j2-{version}.jar`

### 프로젝트에 라이브러리 추가

- 프로젝트 **루트 경로**에 `libs` 디렉토리를 생성합니다.
- 선택한 로깅 프레임워크 라이브러리 1종을 `libs` 폴더에 복사합니다.

## 빌드 도구별 설정

### Maven 환경

**Logback 사용**

```xml
<dependencies>
    <!-- Universal Logger Logback -->
    <dependency>
        <groupId>com.universallogger</groupId>
        <artifactId>universal-logger-logback</artifactId>
        <version>1.0.0</version>
        <scope>system</scope>
        <systemPath>${project.basedir}/libs/universal-logger-logback-1.0.0.jar</systemPath>
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
                            <include>universal-logger-logback-1.0.0.jar</include>
                        </includes>
                    </resource>
                </webResources>
            </configuration>
        </plugin>
    </plugins>
</build>
```

**Log4j2 사용**

```xml
<dependencies>
    <!-- Universal Logger Log4j2 -->
    <dependency>
        <groupId>com.universallogger</groupId>
        <artifactId>universal-logger-log4j2</artifactId>
        <version>1.0.0</version>
        <scope>system</scope>
        <systemPath>${project.basedir}/libs/universal-logger-log4j2-1.0.0.jar</systemPath>
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
                            <include>universal-logger-log4j2-1.0.0.jar</include>
                        </includes>
                    </resource>
                </webResources>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### Gradle 환경

**Logback 사용**

```groovy
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
    implementation files('libs/universal-logger-logback-1.0.0.jar')
}
```

**Log4j2 사용**

```groovy
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
    implementation files('libs/universal-logger-log4j2-1.0.0.jar')
}
```

## 기본 설정

### 설정 파일 생성

`src/main/resources/config/universal-logger.yml`

```yaml
version: "1.0"

global:
  appName: "MyApp"
  timezone: "Asia/Seoul"

levels:
  root: "INFO"
  com.myapp: "DEBUG"

outputs:
  console:
    enabled: true
    level: "DEBUG"

  file:
    enabled: true
    level: "DEBUG"
    path: "C:/log/{appName}/application.log"
    rotation:
      maxSize: "50MB"
      maxFiles: 30
      compress: false

  errorFile:
    enabled: true
    level: "ERROR"
    path: "C:/log/{appName}/error/error.log"
    rotation:
      maxSize: "50MB"
      maxFiles: 30
      compress: true

  webhook:
    enabled: false
    level: "ERROR"
    url: "https://hooks.slack.com/services/{secretKey}"
    webhook:
      format: |
        [ERROR - PHM_LIMS ]

        Time: {timestamp}
        Logger: {logger}

        Message:
        {message}

        Exception:
        {exception}
```

### 환경별 설정 분리

운영 환경(DEV / TEST / PROD 등)에 따라 서로 다른 로깅 설정이 필요한 경우,
**공통 설정 파일 + 환경별 설정 파일** 구조로 설정할 수 있습니다.

#### 프로파일 설정이 필요한 상황

| 상황 | 설명 |
|------|------|
| 로그 레벨 분리 | 개발 환경에서는 DEBUG, 운영 환경에서는 INFO/WARN 레벨 사용 |
| 로그 보관 정책 차이 | 운영 환경에서 더 긴 보관 기간, 더 큰 파일 크기 설정 |
| 알림 설정 차이 | 운영 환경에서만 Slack/Webhook 알림 활성화 |
| 로그 경로 분리 | 환경별로 다른 로그 저장 경로 사용 |
| 압축 정책 차이 | 운영 환경에서 로그 압축 활성화로 디스크 절약 |

#### 설정 파일 구조

**1) 공통 설정 파일 (필수)**

```
universal-logger.yml
```

- 모든 환경에서 공통으로 사용하는 설정을 정의합니다.
- 환경별로 달라질 수 있는 값은 최소화하는 것을 권장합니다.

**2) 환경별 설정 파일 (선택)**

아래와 같이 환경별 설정 파일을 추가로 생성합니다.

```
universal-logger-dev.yml
universal-logger-local.yml
universal-logger-prod.yml
```

예시) `universal-logger-prod.yml`

```yaml
outputs:
  file:
    rotation:
      maxSize: "200MB"
      maxFiles: 90  # Keep more logs in production
      compress: false

  errorFile:
    rotation:
      maxSize: "100MB"
      maxFiles: 90
      compress: false

  webhook:
    enabled: true
```

#### 프로파일 활성화 방법

**Spring Boot 환경**

`application.yml` 또는 `application.properties`에서 활성 프로파일을 설정합니다.

```yaml
spring:
  profiles:
    active: prod
```

```properties
spring.profiles.active=prod
```
**Spring 환경**

**web.xml**

```xml
<context-param>
    <param-name>spring.profiles.active</param-name>
    <param-value>${environment}</param-value>
</context-param>
```

> **프로파일 탐지 우선순위**:
> 1. `logger.profile` 시스템 속성 (명시적 오버라이드)
> 2. `spring.profiles.active` 시스템 속성

### Spring Boot 설정

`application.yml`

```yaml
spring:
  application:
    name: MyApp

universal-logger:
  enabled: true
  application-name: MyApp  # 애플리케이션 이름 (로그 경로 {appName} 치환용)
  config-path: "config/universal-logger.yml"
  sql-capture:
    enabled: false # true가 기본옵션으로, 자동 캡쳐 사용 필요 없을 경우 false 처리
```

> `application-name`을 설정하지 않으면 `spring.application.name` 값이 사용됩니다.

### Spring 설정

**프로젝트 설정 파일**

ex> `project.properties`

```properties
spring.application.name=MyApp
universal-logger.config-path=config/universal-logger.yml
universal-logger.sql-capture.enabled=false
```

**Spring MVC 설정을 커스터마이징하기 위한 확장 클래스**

ex> `ServerConfig.java`

```java
import com.universallogger.logback.spring.EnableUniversalLogger;

@EnableUniversalLogger(
    applicationName = "MyApp",  // 애플리케이션 이름 (로그 경로 {appName} 치환용)
    configPath = "config/universal-logger.yml",
    enableSqlCapture = true
)
public class ServerConfig extends WebMvcConfigurerAdapter {

}
```

## 사용법

### 기본 로깅

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

**로그 레벨**

| 레벨 | 용도 | 환경 |
|------|------|------|
| TRACE | 매우 상세한 정보 | 필요시 |
| DEBUG | 디버깅 정보 | 개발/테스트 |
| INFO | 일반 정보 | 운영 권장 |
| WARN | 경고 (잠재적 문제) | 모든 환경 |
| ERROR | 에러 (즉시 대응 필요) | 모든 환경 |

### SQL 로깅

파라미터를 실제 값으로 치환한 쿼리를 캡처합니다.

```
[2026-01-13 10:04:36] [DEBUG] [RMI TCP Connection(2)-127.0.0.1] [lims.com.dao.LocaleDao.getListLanguage:143] ==>  Preparing: SELECT /*LocaleDao.getListLanguage*/ /*LIMS 언어 조회*/ A.COMN_CD AS CMMN_CODE, A.COMN_CD_NM AS CMMN_CODE_NM, A.TMPR_FIELD1_VALUE AS LANG_VALUE FROM COM_SY_COMN_CD A WHERE A.UP_COMN_CD = 'NTLG' AND A.USE_YN = 'Y'
[2026-01-13 10:04:36] [DEBUG] [RMI TCP Connection(2)-127.0.0.1] [lims.com.dao.LocaleDao.getListLanguage:143] ==> Parameters:
[2026-01-13 10:04:36] [INFO ] [RMI TCP Connection(2)-127.0.0.1] [UniversalSqlLogger:108] [SQL] SELECT /*LocaleDao.getListLanguage*/
        /*LIMS 언어 조회*/
        A.COMN_CD AS CMMN_CODE,
        A.COMN_CD_NM AS CMMN_CODE_NM,
         A.TMPR_FIELD1_VALUE AS LANG_VALUE
     FROM COM_SY_COMN_CD A
     WHERE A.UP_COMN_CD = 'NTLG'
     AND A.USE_YN = 'Y'
[SQL-ROWS] Total: 2
[SQL-TIME] 105ms
[2026-01-13 10:04:36] [DEBUG] [RMI TCP Connection(2)-127.0.0.1] [lims.com.dao.LocaleDao.getListLanguage:143] <==      Total: 2
```

**지원 프레임워크**

- MyBatis
- JPA
- Spring JDBC
- 순수 JDBC

### 알림 기능

ERROR 로그 발생 시 Slack 알림을 전송할 수 있습니다.

```yaml
outputs:
    # 다른 설정 생략
  webhook:
    enabled: true
    level: "ERROR"
    url: "https://hooks.slack.com/services/{secretKey}"
    webhook:
      format: |
        [ERROR - PHM_LIMS ]

        Time: {timestamp}
        Logger: {logger}

        Message:
        {message}

        Exception:
        {exception}
```
