# Universal Logger for Java

Java 애플리케이션을 위한 범용 로깅 라이브러리입니다. 
Logback과 Log4j2를 선택적으로 사용할 수 있으며, 설정 파일 하나로 콘솔, 파일, 웹훅, 이메일 로깅과 SQL 자동 캡처를 지원합니다.

## 목차

1. [주요 특징](#주요-특징)
2. [프로젝트 구조](#프로젝트-구조)
3. [요구사항](#요구사항)
4. [관련 문서](#관련-문서)

## 주요 특징

- **멀티 로깅 프레임워크 지원**: Logback 또는 Log4j2 선택 가능
- **제로 설정**: Spring Boot에서 의존성 추가만으로 자동 활성화
- **SQL 자동 캡처**: MyBatis, JPA, Spring JDBC SQL을 자동으로 로깅
- **다양한 출력 지원**: 콘솔, 파일, 웹훅(Slack/Discord), 이메일
- **통합 설정**: YAML 파일 하나로 모든 로깅 설정 관리
- **프로파일 기반 설정**: 환경별(dev/test/prod) 설정 자동 병합

## 프로젝트 구조

```
universal-logger-java/
├── universal-logger-core/          # 핵심 기능 및 공통 코드
│   ├── 설정 로더 (YAML/XML/JSON)
│   ├── SQL 캡처 프록시
│   └── 공통 유틸리티
├── universal-logger-logback/       # Logback 구현체
│   ├── Logback Configurator
│   ├── Custom Appenders
│   └── Spring/Spring Boot 통합
└── universal-logger-log4j2/        # Log4j2 구현체
    ├── Log4j2 ConfigurationFactory
    ├── Custom Appenders
    └── Spring/Spring Boot 통합
```

## 요구사항

| 항목 | 버전 |
|------|------|
| Java | 8 이상 |
| SLF4J | 1.7.36+ |
| Logback (선택 시) | 1.2.12+ |
| Log4j2 (선택 시) | 2.17.1+ |
| Spring Framework (선택) | 5.0+ |
| Spring Boot (선택) | 2.0+ |

## 관련 문서

- [USER_GUIDE.md](USER_GUIDE.md) - 상세 설치 및 사용법
- [ARCHITECTURE.md](ARCHITECTURE.md) - 아키텍처 및 설계 원칙
