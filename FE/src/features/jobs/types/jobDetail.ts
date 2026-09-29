export interface JobDetailGlanceItem {
  label: string;
  value: string;
  isMuted?: boolean;
}

export type JobDetailFitStatus = 'met' | 'estimated' | 'caution' | 'unknown';

export interface JobDetailFitCriterionItem {
  status: JobDetailFitStatus;
  title: string;
  description?: string;
}

export interface JobDetailContentSection {
  heading: string;
  items: string[];
}

export interface JobDetailCompanyInfo {
  industry: string;
  employeeCount: string;
  companySize: string;
  isListed: string;
}

export interface JobDetailSource {
  siteName: string;
  collectedDate?: string;
  notice: string;
  originalUrl: string;
}

export interface JobDetail {
  id: string;
  sourceName: string;
  postedDate?: string;
  rating?: number;
  title: string;
  subtitle: string;
  employmentType: string;
  location: string;
  dDayLabel: string;
  glanceItems: JobDetailGlanceItem[];
  fitCriteria: JobDetailFitCriterionItem[];
  matchScore?: number;
  matchReasons: string[];
  contentSections: JobDetailContentSection[];
  techStack: string[];
  company: JobDetailCompanyInfo;
  source: JobDetailSource;
}
