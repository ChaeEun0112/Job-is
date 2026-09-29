import { afterEach, beforeEach, describe, expect, it, jest } from '@jest/globals';
import type { JobDetail as ApiJobDetail } from '@/api/types/jobs.types';
import { mapJobDetail } from '../mapJobDetail';

const job: ApiJobDetail = {
  id: 1,
  companyName: '잡이즈',
  position: '프론트엔드 개발자',
  careerLevel: '신입',
  employmentType: 'regular',
  remoteAvailable: true,
  sourceUrl: 'https://www.example.com/jobs/1',
  dueTime: '2026-08-07T00:00:00',
  intro: '함께 성장할 동료를 찾아요.\n',
  mainTasks: '웹 서비스 개발\n코드 리뷰',
  requirements: '',
  preferredPoints: '',
  benefits: '원격 근무',
  employeeCount: 20,
  companyType: '스타트업',
  industry: '채용 플랫폼',
  stockStatus: '',
  skillTags: ['React', 'TypeScript'],
  locationFull: '서울 강남구',
  matching: {
    matchScore: 82,
    rating: 4.1,
    matchReasons: ['React 경험이 맞아요'],
    fitCriteria: {
      jobType: 'MATCH',
      career: 'ESTIMATED',
      location: 'CAUTION',
      skills: 'UNKNOWN',
      preference: 'MATCH',
      salary: 'UNKNOWN',
    },
  },
};

describe('mapJobDetail', () => {
  beforeEach(() => {
    jest.useFakeTimers();
    jest.setSystemTime(new Date('2026-08-05T12:00:00.000Z'));
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  it('상세 API 응답을 표시용 모델로 변환하고 빈 본문 섹션은 제외한다', () => {
    const detail = mapJobDetail(job);

    expect(detail.location).toBe('서울 강남구 · 원격');
    expect(detail.sourceName).toBe('example.com');
    expect(detail.dDayLabel).toBe('D-2');
    expect(detail.employmentType).toBe('정규직');
    expect(detail.glanceItems).toContainEqual({ label: '고용 형태', value: '정규직' });
    expect(detail.glanceItems).toContainEqual({
      label: '연봉',
      value: '급여 비공개',
      isMuted: true,
    });
    expect(detail.matchScore).toBe(82);
    expect(detail.fitCriteria).toEqual([
      { title: '직무', status: 'met' },
      { title: '경력', status: 'estimated' },
      { title: '지역', status: 'caution' },
      { title: '스킬', status: 'unknown' },
      { title: '선호 조건', status: 'met' },
      { title: '연봉', status: 'unknown' },
    ]);
    expect(detail.contentSections).toEqual([
      { heading: '소개', items: ['함께 성장할 동료를 찾아요.'] },
      { heading: '주요 업무', items: ['웹 서비스 개발', '코드 리뷰'] },
      { heading: '복지', items: ['원격 근무'] },
    ]);
  });

  it('성향 진단 전에는 매칭 정보를 빈 값으로 처리한다', () => {
    const detail = mapJobDetail({ ...job, matching: null, sourceUrl: 'not-a-url' });

    expect(detail.sourceName).toBe('원문');
    expect(detail.rating).toBeUndefined();
    expect(detail.fitCriteria).toEqual([]);
    expect(detail.matchReasons).toEqual([]);
  });

  it('null 본문은 빈 섹션으로 처리한다', () => {
    const detail = mapJobDetail({
      ...job,
      intro: null,
      mainTasks: null,
      requirements: null,
      preferredPoints: null,
      benefits: null,
    });

    expect(detail.contentSections).toEqual([]);
  });
});
