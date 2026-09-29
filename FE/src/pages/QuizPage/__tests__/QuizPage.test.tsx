import { beforeAll, describe, expect, it, jest } from '@jest/globals';
import { fireEvent, render, screen } from '@testing-library/react';
import { type ReactNode } from 'react';
import type useQuizApiHook from '@/features/quiz/hooks/useQuizApi';
import type QuizPageComponent from '../QuizPage';

jest.mock('react-router', () => ({
  useLocation: () => ({ pathname: '/profile/aptitude-test' }),
  useNavigate: () => jest.fn(),
  useSearchParams: () => [new URLSearchParams()],
}));
jest.mock('@/components/feedback', () => ({
  Alert: ({ children }: { children: ReactNode }) => <p>{children}</p>,
  Spinner: () => <div role="status" aria-label="로딩 중" />,
}));
jest.mock('@/features/quiz/hooks/useQuizApi', () => jest.fn());
jest.mock('@/features/quiz/components/QuizQuestionnaire', () => () => <p>문항 화면</p>);
jest.mock('@/features/quiz/components/QuizStart', () => ({ onStart }: { onStart: () => void }) => (
  <button type="button" onClick={onStart}>
    시작하기
  </button>
));
jest.mock('@/features/quiz/components/QuizResult', () => () => <p>결과 화면</p>);

const mockedUseQuizApi = jest.mocked(
  jest.requireMock('@/features/quiz/hooks/useQuizApi') as typeof useQuizApiHook,
);

let QuizPage: typeof QuizPageComponent;

describe('QuizPage', () => {
  beforeAll(async () => {
    ({ default: QuizPage } = await import('../QuizPage'));
  });

  it('재응시 문항을 불러오는 동안 캐시된 결과 화면을 노출하지 않는다', () => {
    mockedUseQuizApi.mockReturnValue({
      quizQuestionsQuery: {
        data: {
          testId: 1,
          completed: true,
          questions: [],
        },
        isPending: false,
        isFetching: true,
        isError: false,
      },
      quizResultQuery: { isPending: false, isError: false, data: undefined },
      saveAnswer: jest.fn(),
      applyResult: jest.fn(),
      retryQuiz: jest.fn(),
      isSavingAnswer: false,
      isApplyingResult: false,
    } as never);

    render(<QuizPage />);

    fireEvent.click(screen.getByRole('button', { name: '시작하기' }));

    expect(screen.getByRole('status', { name: '로딩 중' })).toBeTruthy();
    expect(screen.queryByText('결과 화면')).toBeNull();
  });
});
