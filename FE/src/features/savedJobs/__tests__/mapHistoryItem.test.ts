import { describe, expect, it } from '@jest/globals';
import type { HistoryItem } from '@/api/types/savedJobs.types';
import { mapHistoryItem } from '../mapHistoryItem';

const historyItem: HistoryItem = {
  jobId: 482,
  companyName: '헬스커넥트',
  title: 'DevOps',
  actionType: 'VIEWED',
  reasonCode: null,
  comment: null,
  actionAt: '2026-08-06T06:46:00+09:00',
  expired: false,
};

describe('mapHistoryItem', () => {
  it('다시 보기 경로에 사용할 공고 ID를 유지한다', () => {
    expect(mapHistoryItem(historyItem)).toMatchObject({
      id: '482-2026-08-06T06:46:00+09:00',
      jobId: 482,
      title: '헬스커넥트 · DevOps',
      status: 'viewed',
    });
  });

  it('저장 액션은 열람·피드백 내역에서 제외한다', () => {
    expect(mapHistoryItem({ ...historyItem, actionType: 'SAVED' })).toBeNull();
  });
});
