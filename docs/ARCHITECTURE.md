# Job-is-FE Architecture

## 1. Architecture Goal

`job-is-fe`는 React 19 + Vite 기반 CSR SPA다.

- 라우팅과 페이지 조립은 `pages`가 담당한다.
- 도메인 전용 UI/훅/로직은 `features/{domain}`에 둔다.
- 도메인에 종속되지 않는 공용 자원은 `components`, `hooks`, `utils`, `constants`에 둔다.
- API 요청 함수는 `api`에 둔다.
- 새 최상위 폴더를 추측해서 만들지 않고 아래 baseline을 따른다.

## 2. Current Folder Baseline

```text
src/
├── api/
│   ├── base/              # axios 인스턴스, isSuccess 검증·에러 정규화, 언랩 헬퍼(api.get/post/...)
│   ├── types/             # 도메인별 API 계약 타입 (예: auth.types.ts, jobs.types.ts)
│   ├── env.ts             # VITE_API_BASE_URL 등 환경 변수
│   └── {domain}.ts        # 도메인별 요청 함수 (예: auth.ts, jobs.ts, recommendations.ts)
├── assets/
│   ├── icons/             # SVG 아이콘 (vite-plugin-svgr로 컴포넌트처럼 import)
│   └── images/            # 이미지 등 나머지 정적 자산
├── components/
│   ├── common/           # 도메인에 종속되지 않는 전역 재사용 UI 컴포넌트
│   ├── feedback/          # 로딩/에러/빈 상태 등 피드백 UI 컴포넌트
│   └── layout/            # 헤더, 네비게이션 등 레이아웃 셸 컴포넌트
├── constants/            # 공용 상수 (queryKey.ts의 QUERY_KEYS: 중앙 TanStack Query key 레지스트리)
├── features/
│   └── {domain}/          # 특정 도메인(예: auth, jobs) 전용 컴포넌트/훅/로직
│       ├── components/
│       └── hooks/
├── hooks/                # 도메인에 종속되지 않는 공용 커스텀 훅
├── pages/
│   └── {PageName}/        # 페이지별 폴더 (예: pages/HomePage/HomePage.tsx)
├── providers/            # 앱 전역 Provider (QueryProvider 등)
├── routes/               # createBrowserRouter 라우트 정의
├── store/                # 도메인 경계를 넘어 앱 전역에서 참조하는 Zustand 스토어 (useAuthStore 등)
├── styles/               # 전역 스타일, Tailwind 진입점(index.css, 컬러 토큰 @theme)
├── utils/                # 공용 유틸리티 함수 (cn.ts 등)
├── App.tsx
├── main.tsx
└── vite-env.d.ts
```

## 3. 기술 스택

- **Framework**: React 19 + TypeScript
- **Bundler**: Vite (`resolve.tsconfigPaths`로 `@/*` alias, `vite-plugin-svgr`로 SVG 아이콘 컴포넌트 import)
- **Styling**: Tailwind CSS v4 (`@tailwindcss/vite`, 설정 파일 없음, 컬러 토큰은 `src/styles/index.css`의
  `@theme`에 정의), `class-variance-authority`(cva) + `clsx` + `tailwind-merge`(`src/utils/cn.ts`)로
  조건부 클래스 처리
- **Routing**: `react-router`(v8) `createBrowserRouter`, `src/routes/router.tsx`
- **Server State**: TanStack Query, `src/providers/QueryProvider.tsx`에서 `QueryClientProvider` 관리
- **Client State**: Zustand (인증 세션 상태 저장, 8절 State Policy 참고)
- **HTTP Client**: axios, `src/api/base/`(axios 인스턴스, CSRF·401 재발급, `isSuccess` 검증과
  `ApiError`/`NetworkError` 정규화, `api.get/post/put/patch/delete` 언랩 헬퍼) +
  `src/api/env.ts`(`VITE_API_BASE_URL`)
- **Lint/Format**: ESLint(flat config) + Prettier
- **Package Manager**: pnpm
- **Git Hooks**: husky + lint-staged(커밋 전 자동 lint/format) + commitlint(커밋 메시지 검사)

## 4. Layer Responsibility

| 위치                                                            | 책임                                                                                                                                                                                                                            |
| --------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `pages/{PageName}`                                              | 컴포넌트/훅/feature 모듈을 조합해 라우트 화면을 구성한다. 비즈니스 로직을 직접 갖지 않는다                                                                                                                                      |
| `routes`                                                        | `createBrowserRouter`로 경로와 페이지를 연결한다. 실제로 쓰는 라우트만 등록한다                                                                                                                                                 |
| `providers`                                                     | 앱 전역 Provider를 관리한다. `App.tsx`가 조합한다                                                                                                                                                                               |
| `features/{domain}`                                             | 특정 도메인 전용 UI/훅/로직을 담당한다. 다른 도메인 폴더를 직접 import하지 않는다                                                                                                                                               |
| `components/common`, `components/feedback`, `components/layout` | 도메인 데이터 모델을 모르는 순수 재사용 UI. props로만 데이터를 받는다                                                                                                                                                           |
| `hooks`                                                         | 도메인에 종속되지 않는 공용 로직                                                                                                                                                                                                |
| `api`                                                           | API 클라이언트와 요청 함수. React 컴포넌트를 import하지 않는다. 컴포넌트는 endpoint 경로를 직접 알지 못하고 이 레이어를 통해서만 호출한다. 계약 타입은 `api/types/{domain}.types.ts`, 클라이언트 공통 로직은 `api/base/`에 둔다 |
| `store`                                                         | 도메인 경계를 넘어 앱 전역에서 참조하는 Zustand 스토어. 특정 도메인에서만 쓰는 로컬 상태는 `features/{domain}`에 둔다                                                                                                           |
| `utils`, `constants`, `assets`, `styles`                        | 순수 유틸/상수/자산/전역 스타일                                                                                                                                                                                                 |

## 5. 도메인 폴더 정책

- `features/{domain}` 하위 구조는 실제로 필요한 시점에 만든다. 빈 도메인 폴더를 미리 채우지 않는다.
- 도메인 전용 컴포넌트/훅이 다른 도메인에서도 필요해지면 `components/common` 또는 `hooks`로 승격한다.

## 6. Import 규칙

- alias는 `@/*`(`src/*`)만 정의되어 있다. 가독성이 좋아지는 경우 alias를 쓰고, 같은/인접 폴더
  파일은 상대 import를 우선한다.
- 새 alias(`@components`, `@hooks` 등)를 임의로 추가하지 않는다.
- `import/order` ESLint 규칙이 그룹/알파벳 순 정렬을 강제한다.

## 7. Component Policy

- 컴포넌트는 화살표 함수가 아닌 `function` 선언으로 작성한다.
- `className`, 필요하면 `ref`를 prop으로 항상 노출한다.
- variant가 2~3개를 넘으면 `cva`로, 조건부 클래스가 단순하면 `cn()` + 삼항/객체로 처리한다.
- 상태 전이가 많은 컴포넌트는 `data-*` 속성 + Tailwind 임의 변형자를 쓴다.
- 상세 규칙은 `docs/CONVENTIONS.md`를 따른다.

## 8. State Policy

| 상태                                          | 위치                                      |
| --------------------------------------------- | ----------------------------------------- |
| 서버에서 온 데이터                            | TanStack Query (`useQuery`/`useMutation`) |
| 폼 입력 중인 값                               | 해당 컴포넌트/훅의 local state            |
| URL로 표현 가능한 값                          | route params 또는 search params           |
| 모달 열림/닫힘                                | 해당 컴포넌트의 local state               |
| 여러 라우트에서 공유하는 순수 클라이언트 상태 | Zustand (`src/store`)                     |
| 인증 세션 식별·라우팅 상태                    | Zustand (`src/store/useAuthStore.ts`)     |

TanStack Query key는 `src/constants/queryKey.ts`의 `QUERY_KEYS` 객체에서 도메인별로
계층적으로 관리한다. 개별 훅에서 문자열 배열을 직접 작성하지 않는다.

서버 상태를 Zustand에 복제하지 않는다. 서버 API가 생긴 화면 전환 상태는 TanStack Query의
mutation과 cache를 우선 검토한다. 폼 입력·드롭다운·모달처럼 해당 화면에서만 필요한 상태는
도메인 훅 또는 컴포넌트의 local state로 관리한다.

인증 세션의 access token, user ID, 온보딩 완료 여부는 여러 라우트의 인증·진입 경로를
결정하는 클라이언트 세션 상태이므로 Zustand에 유지한다. 이 값은 `/api/auth/me` 등으로
재검증할 수 있으며, 프로필·공고처럼 일반 서버 응답을 Zustand에 복제하는 용도로 확장하지 않는다.

도메인 경계를 넘어 앱 전역에서 참조하는 Zustand 스토어는 `src/store/{useXxxStore}.ts`에
flat하게 모은다(예: `useAuthStore`, 토스트류 알림 상태를 담는 `useAlertStore`). 특정
도메인 안에서만 읽고 쓰는 로컬 상태는 계속 `features/{domain}`에 둔다. 새 전역 스토어는
실제로 여러 라우트/도메인이 공유해야 하는 시점에만 만들고, 미리 빈 스토어를 만들어두지
않는다.

## 9. Testing Policy

Jest + `@testing-library/react` + `@jest/globals`가 설치되어 있다. 단위 테스트는 구현
파일이 있는 폴더의 `__tests__/`에 `.test.ts`/`.test.tsx`로 둔다.

```text
src/utils/formatDDayLabel.ts
src/utils/__tests__/formatDDayLabel.test.ts
```

query key, 순수 유틸리티, API 응답 매퍼(`mapJobDetail` 등), 상태 전이처럼 회귀 위험이 큰
로직을 우선 검증한다. UI 스냅샷 테스트는 우선순위가 아니다.
