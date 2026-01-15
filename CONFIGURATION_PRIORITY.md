# Universal Logger Configuration Priority

## 설정 파일 로딩 우선순위

### 최종 적용 우선순위 (높은 것이 우선)

```
1. config/universal-logger-{profile}.yml  (프로파일별 설정)
2. config/universal-logger.yml             (프로젝트 기본 설정)
3. defaults/universal-logger-defaults.yml  (라이브러리 기본값)
```

**병합 방식:**
- defaults → base → profile 순으로 로드
- 나중에 로드된 설정이 이전 설정을 덮어씀
- 따라서 profile 설정이 최우선 적용

### configPath 결정 우선순위

configPath 자체를 어디서 가져올지 결정하는 우선순위:

```
1. System Property         -Duniversal-logger.config-path=custom.yml
2. application-{env}.yml   universal-logger.config-path: custom-{env}.yml
3. application.yml         universal-logger.config-path: custom.yml
4. @EnableUniversalLogger  configPath = "custom.yml"
5. Default                 config/universal-logger.yml
```

### 프로파일 결정 우선순위

어떤 프로파일을 사용할지 결정하는 우선순위:

```
1. -Dlogger.profile=dev
2. Spring Environment (web.xml, -Dspring.profiles.active, SPRING_PROFILES_ACTIVE)
3. No profile
```

## 환경별 설정 예시

### Case 1: 표준 방식 (권장)

**파일 구조:**
```
src/main/resources/
├── config/
│   ├── universal-logger.yml        # 공통 설정
│   ├── universal-logger-local.yml  # 로컬 환경
│   ├── universal-logger-dev.yml    # 개발 환경
│   └── universal-logger-prd.yml    # 운영 환경
└── application.yml
```

**실행:**
```bash
-Dspring.profiles.active=dev
```

**로딩 순서:**
1. defaults/universal-logger-defaults.yml (JAR)
2. config/universal-logger.yml (공통)
3. config/universal-logger-dev.yml (dev 덮어씀)

### Case 2: 환경별 다른 configPath 사용

**파일 구조:**
```
src/main/resources/
├── config/
│   ├── dev/
│   │   ├── logger.yml
│   │   └── logger-dev.yml
│   └── prd/
│       ├── logger.yml
│       └── logger-prd.yml
├── application-dev.yml
└── application-prd.yml
```

**application-dev.yml:**
```yaml
spring:
  profiles:
    active: dev

universal-logger:
  config-path: "config/dev/logger.yml"
```

**application-prd.yml:**
```yaml
spring:
  profiles:
    active: prd

universal-logger:
  config-path: "config/prd/logger.yml"
```

**로딩 순서 (dev):**
1. defaults/universal-logger-defaults.yml
2. config/dev/logger.yml
3. config/dev/logger-dev.yml

### Case 3: 시스템 프로퍼티로 오버라이드

```bash
-Dspring.profiles.active=dev \
-Duniversal-logger.config-path=/etc/app/custom-logger.yml
```

**로딩 순서:**
1. defaults/universal-logger-defaults.yml
2. /etc/app/custom-logger.yml
3. /etc/app/custom-logger-dev.yml

## 파일 검색 순서

각 config 파일은 다음 순서로 검색:

```
1. Classpath (src/main/resources/)
2. File System (절대 경로 또는 상대 경로)
```

**예시:**
- `config/universal-logger.yml` → classpath 우선
- `/etc/app/logger.yml` → file system

## 설정 값 우선순위

개별 설정 항목의 우선순위:

```
profile config > base config > defaults

예: console.level
- defaults.yml: INFO
- universal-logger.yml: DEBUG
- universal-logger-dev.yml: TRACE
→ 최종값: TRACE (dev 환경)
```
