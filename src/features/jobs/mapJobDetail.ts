import type {
  CriteriaMatrix,
  FitCriteriaVerdict,
  JobDetail as ApiJobDetail,
} from '@/api/types/jobs.types';
import type {
  JobDetail,
  JobDetailContentSection,
  JobDetailFitCriterionItem,
  JobDetailFitStatus,
} from '@/features/jobs/types/jobDetail';
import { formatDDayLabel } from '@/utils/formatDDayLabel';
import { formatEmploymentType } from '@/utils/formatEmploymentType';

const VERDICT_TO_STATUS: Record<FitCriteriaVerdict, JobDetailFitStatus> = {
  MATCH: 'met',
  ESTIMATED: 'estimated',
  CAUTION: 'caution',
  UNKNOWN: 'unknown',
};

const CRITERIA_LABELS: Record<keyof CriteriaMatrix, string> = {
  jobType: '직무',
  career: '경력',
  location: '지역',
  skills: '스킬',
  preference: '선호 조건',
  salary: '연봉',
};

function mapFitCriteria(criteria: CriteriaMatrix): JobDetailFitCriterionItem[] {
  return (Object.keys(CRITERIA_LABELS) as (keyof CriteriaMatrix)[]).map((key) => ({
    status: VERDICT_TO_STATUS[criteria[key]],
    title: CRITERIA_LABELS[key],
  }));
}

function splitToLines(text: string | null): string[] {
  return (text ?? '')
    .split('\n')
    .map((line) => line.trim())
    .filter(Boolean);
}

function buildContentSections(job: ApiJobDetail): JobDetailContentSection[] {
  const sections: { heading: string; text: string | null }[] = [
    { heading: '소개', text: job.intro },
    { heading: '주요 업무', text: job.mainTasks },
    { heading: '자격 요건', text: job.requirements },
    { heading: '우대 사항', text: job.preferredPoints },
    { heading: '복지', text: job.benefits },
  ];

  return sections
    .map(({ heading, text }) => ({ heading, items: splitToLines(text) }))
    .filter((section) => section.items.length > 0);
}

function extractSiteName(sourceUrl: string): string {
  try {
    return new URL(sourceUrl).hostname.replace(/^www\./, '');
  } catch {
    return '원문';
  }
}

export function mapJobDetail(job: ApiJobDetail): JobDetail {
  const location = job.remoteAvailable ? `${job.locationFull} · 원격` : job.locationFull;

  return {
    id: String(job.id),
    sourceName: extractSiteName(job.sourceUrl),
    rating: job.matching?.rating,
    title: job.position,
    subtitle: `${job.companyName} · ${job.industry} · ${job.locationFull}`,
    employmentType: formatEmploymentType(job.employmentType),
    location,
    dDayLabel: formatDDayLabel(job.dueTime),
    glanceItems: [
      { label: '직무', value: job.position },
      { label: '경력', value: job.careerLevel },
      { label: '지역', value: location },
      { label: '고용 형태', value: formatEmploymentType(job.employmentType) },
      { label: '마감', value: formatDDayLabel(job.dueTime) },
      { label: '연봉', value: '급여 비공개', isMuted: true },
    ],
    fitCriteria: job.matching ? mapFitCriteria(job.matching.fitCriteria) : [],
    matchScore: job.matching?.matchScore,
    matchReasons: job.matching?.matchReasons ?? [],
    contentSections: buildContentSections(job),
    techStack: job.skillTags,
    company: {
      industry: job.industry,
      employeeCount: job.employeeCount > 0 ? `${job.employeeCount}명` : '정보 없음',
      companySize: job.companyType || '정보 없음',
      isListed: job.stockStatus || '정보 없음',
    },
    source: {
      siteName: extractSiteName(job.sourceUrl),
      notice: '정확한 조건은 원문에서 최종 확인하세요',
      originalUrl: job.sourceUrl,
    },
  };
}
