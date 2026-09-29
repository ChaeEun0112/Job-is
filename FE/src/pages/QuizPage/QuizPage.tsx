import { useEffect, useState } from 'react';
import { useLocation, useNavigate, useSearchParams } from 'react-router';
import { Alert, Spinner } from '@/components/feedback';
import QuizQuestionnaire from '@/features/quiz/components/QuizQuestionnaire';
import QuizResult from '@/features/quiz/components/QuizResult';
import QuizStart from '@/features/quiz/components/QuizStart';
import useQuizApi from '@/features/quiz/hooks/useQuizApi';

type QuizScreen = 'start' | 'questions' | 'result';

export default function QuizPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const [searchParams] = useSearchParams();
  const isOnboardingRoute = location.pathname === '/onboarding/aptitude-test';
  const isLegacyOnboardingRoute =
    location.pathname === '/profile/aptitude-test' && searchParams.get('source') === 'ONBOARDING';
  const source = isOnboardingRoute || isLegacyOnboardingRoute ? 'ONBOARDING' : 'PROFILE';
  const [screen, setScreen] = useState<QuizScreen>('start');
  const [currentIndex, setCurrentIndex] = useState<number | null>(null);
  const [answerError, setAnswerError] = useState<string>();
  const [applyError, setApplyError] = useState<string>();

  useEffect(() => {
    if (isLegacyOnboardingRoute) {
      navigate('/onboarding/aptitude-test', { replace: true });
    }
  }, [isLegacyOnboardingRoute, navigate]);

  const {
    quizQuestionsQuery,
    quizResultQuery,
    saveAnswer,
    applyResult: submitQuizResult,
    retryQuiz: refetchQuiz,
    isSavingAnswer,
    isApplyingResult,
  } = useQuizApi(source, screen);

  const exitQuestionnaire = () => {
    setCurrentIndex(null);
    setAnswerError(undefined);
    setScreen('start');
  };

  const startQuestionnaire = () => {
    setAnswerError(undefined);
    setApplyError(undefined);
    setCurrentIndex(null);
    setScreen('questions');
  };

  const moveToPrevious = () => {
    if (activeQuestionIndex === 0) {
      exitQuestionnaire();
      return;
    }

    setCurrentIndex(activeQuestionIndex - 1);
  };

  const skipQuestion = () => {
    const quiz = quizQuestionsQuery.data;
    if (!quiz) return;

    if (activeQuestionIndex < quiz.questions.length - 1) {
      setCurrentIndex(activeQuestionIndex + 1);
      return;
    }

    navigate(source === 'ONBOARDING' ? '/onboarding' : '/profile');
  };

  const selectAnswer = async (choiceValue: number) => {
    const quiz = quizQuestionsQuery.data;
    const currentQuestion = quiz?.questions[activeQuestionIndex];
    if (!quiz || !currentQuestion) return;

    setAnswerError(undefined);

    try {
      const savedAnswer = await saveAnswer(quiz.testId, currentQuestion.questionNo, choiceValue);

      if (savedAnswer.completed) {
        setScreen('result');
        return;
      }

      if (activeQuestionIndex < quiz.questions.length - 1) {
        setCurrentIndex(activeQuestionIndex + 1);
      }
    } catch {
      setAnswerError('답변을 저장하지 못했어요. 잠시 후 다시 시도해주세요.');
    }
  };

  const applyResult = async () => {
    const quiz = quizQuestionsQuery.data;
    if (!quiz) return;

    setApplyError(undefined);

    try {
      await submitQuizResult(quiz.testId);
      navigate(source === 'ONBOARDING' ? '/onboarding' : '/profile');
    } catch {
      setApplyError('결과를 프로필에 반영하지 못했어요. 잠시 후 다시 시도해주세요.');
    }
  };

  const retryQuiz = async () => {
    setApplyError(undefined);
    await refetchQuiz();
    setCurrentIndex(null);
    setScreen('questions');
  };

  const quiz = quizQuestionsQuery.data;
  const firstUnansweredIndex = quiz?.questions.findIndex(
    (question) =>
      question.selectedChoiceValue === null || question.selectedChoiceValue === undefined,
  );
  const activeQuestionIndex =
    currentIndex ??
    (firstUnansweredIndex === undefined || firstUnansweredIndex < 0 ? 0 : firstUnansweredIndex);
  const isShowingResult = screen === 'result';

  return (
    <div className="flex flex-1 items-center justify-center bg-gray-50 px-5 py-12">
      {isShowingResult ? (
        quizResultQuery.isPending ? (
          <section className="flex min-h-74 w-full max-w-190 items-center justify-center rounded-md border border-gray-200 bg-white p-6">
            <Spinner />
          </section>
        ) : quizResultQuery.isError || !quizResultQuery.data ? (
          <section className="w-full max-w-190 rounded-md border border-gray-200 bg-white p-6">
            <Alert variant="danger" size="slim">
              퀴즈 결과를 불러오지 못했어요. 잠시 후 다시 시도해주세요.
            </Alert>
          </section>
        ) : (
          <QuizResult
            resultName={quizResultQuery.data.resultType.name}
            resultSummary={quizResultQuery.data.resultType.summary}
            resultTags={quizResultQuery.data.resultTags}
            onApply={() => void applyResult()}
            onRetry={() => void retryQuiz()}
            isApplying={isApplyingResult}
            errorMessage={applyError}
          />
        )
      ) : screen === 'questions' ? (
        quizQuestionsQuery.isPending || quizQuestionsQuery.isFetching ? (
          <section className="flex min-h-74 w-full max-w-190 items-center justify-center rounded-md border border-gray-200 bg-white p-6">
            <Spinner />
          </section>
        ) : quizQuestionsQuery.isError || !quiz || quiz.questions.length === 0 ? (
          <section className="w-full max-w-190 rounded-md border border-gray-200 bg-white p-6">
            <Alert variant="danger" size="slim">
              퀴즈 문항을 불러오지 못했어요. 잠시 후 다시 시도해주세요.
            </Alert>
          </section>
        ) : (
          <div className="flex w-full max-w-190 flex-col gap-3">
            <QuizQuestionnaire
              questions={quiz.questions}
              currentIndex={activeQuestionIndex}
              selectedAnswer={quiz.questions[activeQuestionIndex]?.selectedChoiceValue}
              isSavingAnswer={isSavingAnswer}
              onSelect={(choiceValue) => void selectAnswer(choiceValue)}
              onPrevious={moveToPrevious}
              onSkip={skipQuestion}
            />
            {answerError && (
              <Alert variant="danger" size="slim">
                {answerError}
              </Alert>
            )}
          </div>
        )
      ) : (
        <QuizStart onStart={startQuestionnaire} />
      )}
    </div>
  );
}
