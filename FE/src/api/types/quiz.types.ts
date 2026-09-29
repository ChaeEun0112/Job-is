export type QuizSource = 'ONBOARDING' | 'PROFILE';

export interface QuizChoice {
  choiceValue: number;
  content: string;
}

export interface QuizQuestion {
  questionNo: number;
  question: string;
  choices: QuizChoice[];
  selectedChoiceValue?: number | null;
}

export interface QuizQuestionsResponse {
  testId: number;
  source: QuizSource;
  completed: boolean;
  answeredCount: number;
  totalCount: number;
  questions: QuizQuestion[];
}

export interface QuizResult {
  testId: number;
  completed: boolean;
  answeredCount: number;
  totalCount: number;
  resultType: {
    code: string;
    name: string;
    summary: string;
  };
  resultTags: string[];
}

export interface SaveQuizAnswerRequest {
  testId: number;
  questionNo: number;
  choiceValue: number;
}

export interface SaveQuizAnswerResponse extends SaveQuizAnswerRequest {
  answeredCount: number;
  totalCount: number;
  completed: boolean;
}

export interface ApplyQuizResultResponse {
  testId: number;
  resultType: string;
  resultTags: string[];
  jobTestCompleted: boolean;
  applied: boolean;
}
