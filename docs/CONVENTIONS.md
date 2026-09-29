# Coding Convention

## 1. 네이밍 규칙

### 변수와 함수

변수와 함수는 `camelCase`를 사용합니다.

```ts
const userName = 'kyeongjoon';

const getUserInfo = () => {};
const loadFriendList = () => {};
```

의미가 불분명한 이름 사용은 지양합니다.

```ts
// 지양
const a = [];
const data = [];

// 권장
const userList = [];
const selectedDate = '';
```

단, 반복문에서 짧게 사용되는 인덱스 변수는 예외적으로 허용합니다.

```ts
for (let i = 0; i < list.length; i++) {
  // ...
}
```

### 컴포넌트

컴포넌트는 `PascalCase`를 사용합니다.

```text
LoginPage.tsx
UserProfile.tsx
CalendarHeader.tsx
```

### 상수

상수는 `UPPER_SNAKE_CASE`를 사용합니다.

```ts
const MAX_COUNT = 10;
const DEFAULT_PAGE_SIZE = 20;
```

### 아이콘 파일

`src/assets/icons`의 SVG 파일명은 `kebab-case`를 사용합니다.

```text
icon-home.svg
icon-chevron-left.svg
```

컴포넌트로 import할 때 이름만 PascalCase로 붙입니다.

```ts
import HomeIcon from '@/assets/icons/icon-home.svg?react';
```

## 2. 주석 작성 기준

주석은 "무엇을 하는 코드인지"보다 **왜 이렇게 작성했는지** 중심으로 작성합니다.

```ts
// 서버에서 빈 문자열을 유효한 값으로 처리하므로 null 대신 빈 문자열 전달
const description = inputValue ?? '';
```

단순히 코드를 그대로 설명하는 주석은 지양합니다.

```ts
// 지양
// count에 1을 더함
count += 1;
```

## 3. 로그 관리

- 개발 중 사용한 `console.log`는 커밋 전 제거합니다.
- 에러 확인이 필요한 경우 `console.error`를 사용합니다.
- 운영 환경에 불필요한 로그가 남지 않도록 확인합니다.

```ts
try {
  await login();
} catch (error) {
  console.error('로그인 요청 실패:', error);
}
```

## 4. 코드 작성 원칙

### 하나의 함수는 하나의 역할만 담당

```ts
// 지양
const handleSubmit = () => {
  validateForm();
  requestLogin();
  saveUserInfo();
  navigateToHome();
};

// 권장
const handleSubmit = () => {
  if (!validateForm()) return;

  loginUser();
};
```

### 중복 코드 분리

반복되는 로직은 함수, 훅, 유틸 등으로 분리합니다.

```ts
const formatDate = (date: Date) => {
  return date.toISOString().split('T')[0];
};
```

### UI 로직과 데이터 처리 로직 분리

컴포넌트 안에 API 요청, 데이터 가공, 복잡한 조건문이 과도하게 들어가지 않도록 관리합니다.

```text
컴포넌트 → 화면 표현
hooks       → 상태 및 이벤트 로직
api         → 서버 요청
utils       → 순수 유틸 함수
```

### 불필요한 중첩 최소화

```ts
// 지양
if (user) {
  if (user.isActive) {
    if (user.role === 'admin') {
      openAdminPage();
    }
  }
}

// 권장
if (!user || !user.isActive) return;
if (user.role !== 'admin') return;

openAdminPage();
```

## 5. Code Review Checklist

PR 리뷰 시 아래 항목을 확인합니다.

- 브랜치명, 커밋 메시지, PR 제목의 컨벤션 준수 여부
- 작업 내용과 Issue의 일치 여부
- 네이밍의 직관성
- 중복 코드 여부
- 불필요한 주석 또는 로그 존재 여부
- UI 변경 시 스크린샷 첨부 여부
- 함수와 컴포넌트의 책임 분리 여부
- 예외 상황 또는 에러 처리 필요 여부
