import { afterEach, beforeEach, describe, expect, it, jest } from '@jest/globals';
import type { SavedJob } from '@/api/types/savedJobs.types';
import { mapSavedJob } from '../mapSavedJob';

const savedJob: SavedJob = {
  jobId: 1,
  companyName: '잡이즈',
  title: '프론트엔드 개발자',
  locationFull: '서울 강남구',
  careerLevel: '신입',
  employmentType: 'regular',
  savedAt: '2026-08-05T09:00:00',
  deadlineAt: '2026-08-07T23:59:59',
  expired: false,
  applyIntent: false,
};

describe('mapSavedJob', () => {
  beforeEach(() => {
    jest.useFakeTimers();
    jest.setSystemTime(new Date('2026-08-05T12:00:00'));
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  it('최근 저장·마감 임박·저장 상태 배지를 만든다', () => {
    expect(mapSavedJob(savedJob)).toEqual({
      id: '1',
      title: '잡이즈 · 프론트엔드 개발자',
      meta: '서울 강남구 · 신입 · 정규직',
      badges: [
        { label: '오늘 저장', color: 'primary' },
        { label: 'D-2 · 마감 임박', color: 'warn' },
        { label: '저장만', color: 'primary' },
      ],
      closed: false,
    });
  });

  it('마감된 공고와 지원 의향은 상태에 맞는 배지를 만든다', () => {
    expect(
      mapSavedJob({
        ...savedJob,
        savedAt: '2026-07-28T09:00:00',
        expired: true,
        applyIntent: true,
      }).badges,
    ).toEqual([
      { label: '7/28 저장', color: 'primary' },
      { label: '마감됨', color: 'disabled' },
      { label: '지원 의향', color: 'warn' },
    ]);
  });
});
