# Job-is-BE

> 사용자 성향과 채용공고를 매칭하여 일일 추천 덱(Deck)으로 제공하는 구인구직 추천 플랫폼의 백엔드 서버

## 기술 스택

| 계층 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 4.0.8 |
| Security | Spring Security · OAuth2 · JWT · CSRF |
| Database | PostgreSQL · Spring Data JPA · QueryDSL · Flyway |
| Cache | Redis |
| Storage | AWS S3 (Presigned URL) |
| Mail | Spring Mail (Gmail SMTP) |
| AI/LLM | OpenAI GPT-4o-mini |
| Container | Docker (Multi-stage) · Docker Compose |
| Deploy | Blue-Green (Nginx) · GitHub Actions · AWS EC2 |
| Crawling | Python (JSONL 파싱 · 임베딩) |
| Build | Gradle |
| Test | JUnit 5 · H2 (테스트 DB) |
| Docs | SpringDoc OpenAPI 3 (Swagger) |

---

## 프로젝트 구조

```
src/main/java/com/leets7th/job_is_be/
├── domain/
│   ├── auth/          # OAuth2 로그인, JWT 토큰, 계정 복구
│   ├── user/          # 사용자 프로필, 이력서, 기술스택
│   ├── job/           # 채용공고, 저장, 크롤러, 링크 검증
│   ├── deck/          # 일일 추천 덱, 카드, 브리핑
│   ├── notification/  # 알림 설정, 수신거부
│   ├── mail/          # 이메일 발송 및 로그
│   ├── personality/   # 직무 성향 테스트
│   └── content/       # 콘텐츠
└── global/
    ├── config/        # Security, JWT, S3, OAuth, OpenAI, QueryDSL, Swagger 등
    ├── base/          # BaseEntity, BaseStatus
    ├── status/        # ErrorStatus, SuccessStatus
    ├── exception/     # GeneralException, GeneralExceptionAdvice
    ├── response/      # ApiResponse<T>, PageResponse<T>
    └── jwt/           # JwtTokenProvider, TokenTypeValidator
```

---

## 도메인 설명

| 도메인 | 설명 |
|--------|------|
| **auth** | Kakao·Google OAuth2 소셜 로그인, JWT 액세스/리프레시 토큰 발급·갱신, 계정 탈퇴·복구 |
| **user** | 사용자 프로필(경력, 기술스택, 선호 조건), 이력서/자소서 S3 업로드 |
| **job** | 채용공고 조회·검색·필터, 공고 저장, Python 크롤러 연동, 링크 유효성 검사 |
| **deck** | 사용자별 일일 추천 카드 덱 생성(배치), 카드 저장·폐기, AI 브리핑 |
| **notification** | 이메일 알림 설정, 스누즈, 토큰 기반 수신거부 |
| **mail** | 웰컴/일일 브리핑 메일 발송, 발송 이력 관리 |
| **personality** | 온보딩 직무 성향 테스트, 결과 프로필 반영 |

---

## 인증 흐름

- **Access Token**: `Authorization: Bearer <token>` 헤더 (30분 유효)
- **Refresh Token**: HttpOnly Secure 쿠키 (14일 유효)
- **CSRF 보호**: 토큰 갱신(`POST /api/auth/token/reissue`), 로그아웃(`POST /api/auth/logout`) 엔드포인트에만 적용. `X-CSRF-TOKEN` 헤더로 전달

### 공개 엔드포인트 (인증 불필요)

```
GET  /health
GET  /api/auth/csrf
POST /api/auth/restore
GET  /api/auth/dev/**
GET  /api/auth/oauth/**
POST /api/unsubscribe/**
GET  /swagger-ui/**
GET  /v3/api-docs/**
```

---

## 주요 API 엔드포인트

### 인증 (Auth)
| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/auth/oauth/{provider}/login` | OAuth 로그인 URL 발급 |
| POST | `/api/auth/oauth/{provider}/callback` | OAuth 콜백 처리 |
| POST | `/api/auth/token/reissue` | 액세스 토큰 갱신 |
| POST | `/api/auth/logout` | 로그아웃 |
| GET | `/api/auth/csrf` | CSRF 토큰 발급 |
| DELETE | `/api/auth/account` | 계정 탈퇴 |
| POST | `/api/auth/restore` | 계정 복구 |

### 사용자 (User)
| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/users/profile` | 내 프로필 조회 |
| PUT | `/api/users/profile` | 프로필 수정 |
| POST | `/api/users/resumes/presigned-url` | 이력서 업로드 Presigned URL |

### 채용공고 (Job)
| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/jobs` | 공고 목록/검색/필터 |
| GET | `/api/jobs/{id}` | 공고 상세 |
| POST | `/api/jobs/{id}/save` | 공고 저장 |
| DELETE | `/api/jobs/{id}/save` | 공고 저장 해제 |
| GET | `/api/jobs/saved` | 저장된 공고 목록 |
| GET | `/api/jobs/history` | 열람 이력 |
| GET | `/api/jobs/metadata` | 카테고리·지역 마스터 데이터 |

### 추천 덱·카드 (Deck / Card)
| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/briefing` | 일일 추천 덱 + AI 브리핑 조회 |
| PUT | `/api/cards/{id}/save` | 카드 저장 |
| PUT | `/api/cards/{id}/dismiss` | 카드 폐기 |

### 성향 테스트 (Personality)
| Method | Path | 설명 |
|--------|------|------|
| POST | `/api/personality-quizzes` | 테스트 시작 |
| PUT | `/api/personality-quizzes/{id}/complete` | 테스트 완료 |

### 알림 (Notification)
| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/users/notification-settings` | 알림 설정 조회 |
| PUT | `/api/users/notification-settings` | 알림 설정 수정 |
| POST | `/api/unsubscribe/{token}` | 수신거부 (로그인 불필요) |

### 관리자 (Admin)
| Method | Path | 설명 |
|--------|------|------|
| POST | `/api/admin/jobs/sync` | 공고 동기화 (크롤러 연동) |
| POST | `/api/admin/jobs/crawler` | 크롤러 실행 |

---

## 로컬 실행

### 사전 요건

- Java 21
- PostgreSQL
- Redis

### 1. 환경 설정

`application-dev.yml`은 `.gitignore`에 포함되어 있습니다. 아래 내용으로 `src/main/resources/application-dev.yml`을 생성하세요.

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/{DB_NAME}
    username: {DB_USER}
    password: {DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

server:
  port: 8081
```

또는 환경변수로 주입할 수 있습니다:

```
DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD
JWT_SECRET
OPENAI_API_KEY
```

### 2. 실행

```bash
# Windows
gradlew.bat bootRun

# macOS / Linux
./gradlew bootRun
```

기본 프로필은 `dev`입니다. Swagger UI: `http://localhost:8081/swagger-ui.html`

### 3. 테스트

```bash
./gradlew test

# 단일 클래스
./gradlew test --tests "com.leets7th.job_is_be.JobIsBeApplicationTests"
```

### Docker로 로컬 실행

```bash
# Redis + API 단일 인스턴스 (포트 8081)
docker compose up -d
```

---

## 환경 변수

| 변수 | 설명 |
|------|------|
| `DB_HOST` / `DB_PORT` / `DB_NAME` | PostgreSQL 연결 |
| `DB_USER` / `DB_PASSWORD` | DB 인증 |
| `REDIS_HOST` / `REDIS_PORT` / `REDIS_PASSWORD` | Redis 연결 |
| `JWT_SECRET` | JWT 서명 키 |
| `JWT_COOKIE_SECURE` | Refresh Token 쿠키 Secure 속성 (`true` in prod) |
| `KAKAO_CLIENT_ID` / `KAKAO_CLIENT_SECRET` / `KAKAO_REDIRECT_URI` | Kakao OAuth |
| `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` / `GOOGLE_REDIRECT_URI` | Google OAuth |
| `OAUTH_FRONTEND_SUCCESS_URI` / `OAUTH_FRONTEND_FAILURE_URI` | OAuth 완료 후 리다이렉트 |
| `OPENAI_API_KEY` / `OPENAI_MODEL` | OpenAI 연동 |
| `MAIL_ENABLED` / `MAIL_USERNAME` / `MAIL_APP_PASSWORD` | 메일 발송 |
| `AWS_S3_BUCKET` / `AWS_REGION` | S3 파일 저장 |
| `CORS_ALLOWED_ORIGINS` | 허용 Origin 목록 |

---

## 데이터베이스 마이그레이션

Flyway로 관리됩니다. 애플리케이션 시작 시 자동 적용됩니다.

| 버전 | 내용 |
|------|------|
| V1 | 전체 초기 스키마 (users, jobs, decks, cards 등) |
| V2 | `job_postings` → `jobs` 테이블 마이그레이션 |
| V3 | 타임스탬프 타입 통일 (`TIMESTAMP` → `TIMESTAMPTZ`) |
| V4 | 채용공고 장문 텍스트 컬럼 매핑 수정 |

---

## 배포

### CI/CD (GitHub Actions)

`develop` 또는 `main` 브랜치에 push 시 자동 실행됩니다.

1. **test** — JUnit 테스트 실행
2. **build-and-push** — Docker 이미지 빌드 후 GHCR 푸시
   - `develop` → `dev-latest`
   - `main` → `prod-latest`
3. **deploy-dev / deploy-prod** — AWS EC2 SSH 접속 후 배포

### Blue-Green 무중단 배포 (개발 서버)

Nginx를 활용한 블루그린 배포 구조입니다. (`deploy/dev/`)

```
Nginx (80)
  ├── blue  (포트 18080)  ← 비활성
  └── green (포트 18081)  ← 활성 (active.conf)
```

```bash
# 블루그린 전환 (새 버전 배포)
./deploy/dev/scripts/switch.sh

# 롤백
./deploy/dev/scripts/rollback.sh
```

전환 흐름:
1. 비활성 컨테이너에 새 이미지 적용
2. 헬스체크 통과 대기 (`GET /health`, 최대 60초)
3. Nginx `active.conf` 전환
4. 실패 시 자동 롤백

---

## 매칭 엔진 (Python)

`database/matching/` 디렉토리에 Python 기반 추천 매칭 엔진이 있습니다.

- `engine/embed.py` — 채용공고 임베딩 생성
- `engine/recommend.py` — 사용자-공고 매칭 알고리즘
- `engine/llm_select.py` — OpenAI 기반 최종 선택
- `scripts/run_pipeline.py` — 크롤링 파이프라인 실행

```bash
cd database/matching
pip install -r requirements.txt
python scripts/run_pipeline.py
```

---

## API 문서

서버 실행 후 아래 주소에서 Swagger UI를 확인할 수 있습니다.

- 로컬: `http://localhost:8081/swagger-ui.html`
- 개발 서버: `/swagger-ui.html`
