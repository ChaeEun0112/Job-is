import { afterEach, beforeEach, describe, expect, it, jest } from '@jest/globals';
import type { JobSummary } from '@/api/types/jobs.types';
import { mapJobSummary } from '../mapJobSummary';

const job: JobSummary = {
  id: 1,
  companyName: '잡이즈',
  position: '프론트엔드 개발자',
  careerLevel: '신입',
  employmentType: 'regular',
  remoteAvailable: true,
  dueTime: '2026-08-07T00:00:00',
  thumbnailUrl: 'https://example.com/logo.png',
  skillTags: ['React'],
  locationCity: '서울',
  locationDistrict: '강남구',
  fitScore: 82.4,
};

describe('mapJobSummary', () => {
  beforeEach(() => {
    jest.useFakeTimers();
    jest.setSystemTime(new Date('2026-08-05T12:00:00.000Z'));
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  it('API 공고를 탐색 화면 모델로 변환한다', () => {
    expect(mapJobSummary(job)).toEqual({
      id: 1,
      thumbnailUrl: 'https://example.com/logo.png',
      dDayLabel: 'D-2',
      matchScoreLabel: '적합도 82%',
      title: '프론트엔드 개발자',
      companyName: '잡이즈',
      employmentInfo: '서울 강남구 · 정규직 · 신입',
      isRemote: true,
    });
  });

  it('적합도가 없으면 점수 라벨을 표시하지 않는다', () => {
    expect(
      mapJobSummary({ ...job, fitScore: null, locationDistrict: '' }).matchScoreLabel,
    ).toBeUndefined();
    expect(mapJobSummary({ ...job, fitScore: null, locationDistrict: '' }).employmentInfo).toBe(
      '서울 · 정규직 · 신입',
    );
  });
});
