# LIMS 프로젝트 Universal Logger 마이그레이션 가이드

## 🎯 목표
기존 logback.xml을 Universal Logger로 전환하여 데이터베이스 로깅 등 확장 기능 사용

---

## 📋 마이그레이션 단계

### Step 1: 설정 파일 준비

**1-1. 기존 logback.xml 백업**
```bash
cd src/main/resources
cp logback.xml logback.xml.backup
```

**1-2. YAML 설정 파일 생성**

`src/main/resources/logging.yml` 생성:

```yaml
version: "1.0"

global:
  appName: "bioChem"
  timezone: "Asia/Seoul"

levels:
  root: "INFO"
  lims: "DEBUG"
  rdms: "DEBUG"

outputs:
  console:
    enabled: true
    level: "DEBUG"
    format: "[{timestamp}] [{level}] [{thread}] [{logger}:{line}] - {message}"
    colors:
      DEBUG: "cyan"
      INFO: "green"
      WARN: "yellow"
      ERROR: "red"

  file:
    enabled: true
    level: "DEBUG"
    path: "C:/logs/Lims/{appName}_logs.{date}.log"
    format: "[{timestamp}] [{level}] [{thread}] [{logger}:{line}] - {message}"
    rotation:
      maxFileSize: "50MB"
      maxFiles: 30
      compress: true

  errorFile:
    enabled: true
    level: "ERROR"
    path: "C:/logs/Lims/error/{appName}_errlogs.{date}.log"
    format: "[{timestamp}] [{level}] [{thread}] [{logger}:{line}] - {message}"
    rotation:
      maxFileSize: "50MB"
      maxFiles: 30
      compress: true

  # 데이터베이스 로깅 (선택)
  database:
    enabled: true
    level: "INFO"
    database:
      type: "mysql"
      host: "localhost"
      port: 3306
      database: "lims_logs"
      table: "app_logs"
      username: "logger_user"
      password: "your_password"
```

---

### Step 2: pom.xml 수정

**2-1. Universal Logger 의존성 추가**

```xml
<dependencies>
    <!-- 기존 의존성들... -->

    <!-- Universal Logger 추가 -->
    <dependency>
        <groupId>com.universallogger</groupId>
        <artifactId>universal-logger</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>
```

**2-2. Maven 빌드**
```bash
mvn clean install
```

---

### Step 3: 코드 수정

**3-1. Application 메인 클래스 수정**

```java
// Before (기존)
@SpringBootApplication
public class LimsApplication {
    public static void main(String[] args) {
        SpringApplication.run(LimsApplication.class, args);
    }
}

// After (변경)
import com.universallogger.UniversalLogger;

@SpringBootApplication
public class LimsApplication {
    static {
        // Universal Logger 초기화
        UniversalLogger.init("logging.yml");
    }

    public static void main(String[] args) {
        SpringApplication.run(LimsApplication.class, args);
    }
}
```

**3-2. 기존 코드는 변경 불필요**

```java
// 기존 코드 그대로 동작
public class RequestService {
    private static final Logger logger =
        LoggerFactory.getLogger(RequestService.class);

    public void processRequest() {
        logger.debug("요청 처리");  // logging.yml 설정 자동 적용
    }
}
```

**3-3. 새 코드는 UniversalLogger 사용 (선택)**

```java
import com.universallogger.UniversalLogger;
import org.slf4j.Logger;

public class NewService {
    private static final Logger logger =
        UniversalLogger.getLogger("lims");

    public void newFeature() {
        logger.info("새로운 기능");
    }
}
```

---

### Step 4: 테스트

**4-1. 로그 출력 확인**
```bash
# 애플리케이션 실행
java -jar lims-app.jar

# 로그 파일 확인
ls -la C:/logs/Lims/
# bioChem_logs.2025-10-01.log
# error/bioChem_errlogs.2025-10-01.log
```

**4-2. 데이터베이스 확인 (DB 로깅 활성화한 경우)**
```sql
USE lims_logs;
SELECT * FROM app_logs ORDER BY log_timestamp DESC LIMIT 10;
```

---

## 🔄 롤백 방법

문제 발생 시 즉시 롤백:

**1. 설정 파일 복원**
```bash
cd src/main/resources
mv logback.xml.backup logback.xml
rm logging.yml  # 또는 이름 변경
```

**2. 코드 복원**
```java
// LimsApplication.java에서 초기화 코드 제거
static {
    // UniversalLogger.init("logging.yml");  // 주석 처리
}
```

**3. 재빌드 및 재시작**
```bash
mvn clean package
java -jar lims-app.jar
```

---

## 📊 마이그레이션 체크리스트

### 사전 준비
- [ ] 기존 logback.xml 백업
- [ ] logging.yml 생성
- [ ] pom.xml에 의존성 추가
- [ ] Maven 빌드 성공

### 코드 수정
- [ ] LimsApplication에 초기화 코드 추가
- [ ] 컴파일 오류 없음
- [ ] 기존 Logger 코드 동작 확인

### 테스트
- [ ] 콘솔 로그 출력 확인
- [ ] 파일 로그 생성 확인
- [ ] 에러 로그 분리 확인
- [ ] DB 로그 저장 확인 (선택)
- [ ] 성능 이슈 없음

### 배포
- [ ] 개발 환경 테스트 완료
- [ ] 스테이징 환경 테스트 완료
- [ ] 프로덕션 배포

---

## 🎁 추가 기능

마이그레이션 후 사용 가능한 기능:

### 1. 데이터베이스 로깅
```yaml
database:
  enabled: true
  level: "INFO"
  database:
    type: "mysql"
    host: "localhost"
    database: "lims_logs"
    username: "logger_user"
    password: "password"
```

### 2. Slack 알림
```yaml
webhook:
  enabled: true
  level: "ERROR"
  url: "https://hooks.slack.com/services/YOUR/SLACK/WEBHOOK"
  format: "🚨 *{level}* in LIMS\n*Message:* {message}"
```

### 3. 이메일 알림
```yaml
email:
  enabled: true
  level: "ERROR"
  smtp:
    host: "smtp.gmail.com"
    port: 587
    username: "your-email@gmail.com"
    password: "your-password"
    to: ["admin@company.com"]
```

### 4. 민감정보 마스킹
```yaml
filters:
  sensitiveData:
    enabled: true
    patterns:
      - regex: "password=\\w+"
        replace: "password=***"
      - regex: "\\d{3}-\\d{4}-\\d{4}"
        replace: "***-****-****"
```

---

## 🆘 문제 해결

### 문제 1: 로그가 출력되지 않음
**해결:**
```java
// 초기화 확인
UniversalLogger.init("logging.yml");  // 파일명 정확한지 확인
```

### 문제 2: 설정 파일을 찾을 수 없음
**해결:**
```java
// 클래스패스 리소스로 로드
UniversalLogger.init("classpath:logging.yml");
```

### 문제 3: DB 연결 실패
**해결:**
```yaml
# DB 연결 정보 확인
database:
  type: "mysql"  # 정확한 타입
  host: "localhost"  # 정확한 호스트
  port: 3306  # 정확한 포트
  username: "correct_user"  # 정확한 계정
  password: "correct_password"  # 정확한 비밀번호
```

---

## 📞 지원

문제 발생 시:
1. GitHub Issues: [repository-url]/issues
2. 문서: [repository-url]/docs
3. 예시 코드: [repository-url]/examples
