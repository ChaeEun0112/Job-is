import { describe, expect, it } from '@jest/globals';
import { QUERY_KEYS } from '../queryKey';

describe('QUERY_KEYS', () => {
  it('도메인 base key 아래에 상세 key를 구성한다', () => {
    expect(QUERY_KEYS.PROFILE.FILES()).toEqual(['profile', 'files']);
    expect(QUERY_KEYS.JOBS.FILTERS.REGIONS()).toEqual(['jobs', 'filters', 'regions']);
    expect(QUERY_KEYS.SAVED_JOBS.HISTORY('VIEWED')).toEqual(['savedJobs', 'history', 'VIEWED']);
  });

  it('검색 결과를 구분하는 파라미터를 key에 포함한다', () => {
    const defaultSearch = QUERY_KEYS.JOBS.SEARCH({ page: 0, size: 24, sort: 'FIT' });
    const keywordSearch = QUERY_KEYS.JOBS.SEARCH({
      keyword: '프론트엔드',
      page: 0,
      size: 24,
      sort: 'FIT',
    });

    expect(defaultSearch).not.toEqual(keywordSearch);
    expect(keywordSearch).toEqual([
      'jobs',
      'search',
      { keyword: '프론트엔드', page: 0, size: 24, sort: 'FIT' },
    ]);
  });
});
