import type { SearchJobsParams } from '@/api/types/jobs.types';
import type { QuizSource } from '@/api/types/quiz.types';
import type { GetSavedJobsParams, HistoryFilter } from '@/api/types/savedJobs.types';

const authBase = () => ['auth'] as const;
const jobsBase = () => ['jobs'] as const;
const jobsFiltersBase = () => [...jobsBase(), 'filters'] as const;
const profileBase = () => ['profile'] as const;
const quizBase = () => ['quiz'] as const;
const recommendationsBase = () => ['recommendations'] as const;
const savedJobsBase = () => ['savedJobs'] as const;
const settingsBase = () => ['settings'] as const;

export const QUERY_KEYS = {
  AUTH: {
    BASE: authBase,
    SESSION: () => [...authBase(), 'session'] as const,
    ACCOUNT: () => [...authBase(), 'account'] as const,
  },
  JOBS: {
    BASE: jobsBase,
    SEARCH: (params: SearchJobsParams) => [...jobsBase(), 'search', params] as const,
    DETAIL: (jobId: number | null) => [...jobsBase(), 'detail', jobId] as const,
    FILTERS: {
      BASE: jobsFiltersBase,
      JOB_CATEGORIES: () => [...jobsFiltersBase(), 'job-categories'] as const,
      TECH_STACKS: () => [...jobsFiltersBase(), 'tech-stacks'] as const,
      REGIONS: () => [...jobsFiltersBase(), 'regions'] as const,
      CAREER_LEVELS: () => [...jobsFiltersBase(), 'career-levels'] as const,
      EMPLOYMENT_TYPES: () => [...jobsFiltersBase(), 'employment-types'] as const,
    },
  },
  PROFILE: {
    BASE: profileBase,
    DRAFT: () => [...profileBase(), 'draft'] as const,
    FILES: () => [...profileBase(), 'files'] as const,
  },
  QUIZ: {
    BASE: quizBase,
    QUESTIONS: (source: QuizSource) => [...quizBase(), 'questions', source] as const,
    RESULT: (testId: number | undefined) => [...quizBase(), 'result', testId] as const,
  },
  RECOMMENDATIONS: {
    BASE: recommendationsBase,
    BRIEFING: () => [...recommendationsBase(), 'briefing'] as const,
    BRIEFING_STATUS: () => [...recommendationsBase(), 'briefing-status'] as const,
    CONTENTS: () => [...recommendationsBase(), 'contents'] as const,
    CONTENT_DETAIL: (contentId: number) =>
      [...recommendationsBase(), 'contents', contentId] as const,
  },
  SAVED_JOBS: {
    BASE: savedJobsBase,
    LIST_BASE: () => [...savedJobsBase(), 'list'] as const,
    LIST: (params: GetSavedJobsParams) => [...savedJobsBase(), 'list', params] as const,
    HISTORY_BASE: () => [...savedJobsBase(), 'history'] as const,
    HISTORY: (filter: HistoryFilter) => [...savedJobsBase(), 'history', filter] as const,
  },
  SETTINGS: {
    BASE: settingsBase,
    NOTIFICATION: () => [...settingsBase(), 'notification'] as const,
  },
} as const;
