import { api } from '@/api/base/request';
import type {
  ApplyQuizResultResponse,
  QuizQuestionsResponse,
  QuizResult,
  QuizSource,
  SaveQuizAnswerRequest,
  SaveQuizAnswerResponse,
} from './types/quiz.types';

export async function getQuizQuestions(source: QuizSource) {
  return api.get<QuizQuestionsResponse>('/api/quiz/questions', {
    params: { source },
  });
}

export async function saveQuizAnswer(request: SaveQuizAnswerRequest) {
  return api.post<SaveQuizAnswerResponse>('/api/quiz/answers', request);
}

export async function getQuizResult(testId: number) {
  return api.get<QuizResult>('/api/quiz/result', {
    params: { testId },
  });
}

export async function applyQuizResult(testId: number) {
  return api.post<ApplyQuizResultResponse>('/api/quiz/result/apply', { testId });
}
