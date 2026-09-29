export interface JobCategory {
  id: number;
  name: string;
}

export interface TechStack {
  code: string;
  name: string;
}

export interface Region {
  id: number;
  name: string;
}

export interface CareerLevelMetadata {
  key: string;
  description: string;
  minYears: number;
  maxYears: number;
}

export interface JobSummary {
  id: number;
  companyName: string;
  position: string;
  careerLevel: string;
  employmentType: string;
  remoteAvailable: boolean;
  dueTime: string;
  thumbnailUrl: string;
  skillTags: string[];
  locationCity: string;
  locationDistrict: string;
  fitScore: number | null;
}

export type JobCareerRange = 'NEWCOMER' | 'JUNIOR' | 'SENIOR';
export type JobSortOption = 'FIT' | 'RECENT' | 'DEADLINE';

export interface SearchJobsParams {
  keyword?: string;
  categoryChildren?: string[];
  cities?: string[];
  districts?: string[];
  careerRanges?: JobCareerRange[];
  employmentTypes?: string[];
  remoteOnly?: boolean;
  includeAlwaysOpen?: boolean;
  sort?: JobSortOption;
  page?: number;
  size?: number;
}

export type FitCriteriaVerdict = 'MATCH' | 'ESTIMATED' | 'CAUTION' | 'UNKNOWN';

export interface CriteriaMatrix {
  jobType: FitCriteriaVerdict;
  career: FitCriteriaVerdict;
  location: FitCriteriaVerdict;
  skills: FitCriteriaVerdict;
  preference: FitCriteriaVerdict;
  salary: FitCriteriaVerdict;
}

export interface JobMatching {
  matchScore: number;
  rating: number;
  matchReasons: string[];
  fitCriteria: CriteriaMatrix;
}

export interface JobDetail {
  id: number;
  companyName: string;
  position: string;
  careerLevel: string;
  employmentType: string;
  remoteAvailable: boolean;
  sourceUrl: string;
  dueTime: string;
  intro: string | null;
  mainTasks: string | null;
  requirements: string | null;
  preferredPoints: string | null;
  benefits: string | null;
  employeeCount: number;
  companyType: string;
  industry: string;
  stockStatus: string;
  skillTags: string[];
  locationFull: string;
  matching: JobMatching | null;
}

export interface JobLinkValidation {
  isValid: boolean;
  statusCode: number | null;
  message: string;
}

export interface JobInteraction {
  jobId: number;
  viewed: boolean;
  applyIntent: boolean;
  applied: boolean;
  applicable: boolean;
}
