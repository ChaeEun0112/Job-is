import { afterEach, beforeEach, describe, expect, it, jest } from '@jest/globals';
import { formatDDayLabel } from '../formatDDayLabel';

describe('formatDDayLabel', () => {
  beforeEach(() => {
    jest.useFakeTimers();
    jest.setSystemTime(new Date('2026-08-05T12:00:00'));
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  it.each([
    [null, '상시'],
    ['잘못된 날짜', '상시'],
    ['2026-08-04T23:59:59', '마감'],
    ['2026-08-05T23:59:59', 'D-day'],
    ['2026-08-08T00:00:00', 'D-3'],
  ])('%s는 %s로 표시한다', (dueDateIso, expected) => {
    expect(formatDDayLabel(dueDateIso)).toBe(expected);
  });
});
