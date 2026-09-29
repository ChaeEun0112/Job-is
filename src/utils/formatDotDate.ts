const DATE_ONLY_PATTERN = /^(\d{4})-(\d{2})-(\d{2})$/;

export function formatDotDate(dateIso: string): string {
  // 날짜만 있는 값(YYYY-MM-DD)은 Date로 파싱하면 UTC 자정으로 해석되어 로컬 타임존에 따라
  // 날짜가 밀릴 수 있어, 문자열에서 바로 구성요소를 추출한다.
  const dateOnlyMatch = DATE_ONLY_PATTERN.exec(dateIso);
  if (dateOnlyMatch) {
    const [, year, month, day] = dateOnlyMatch;
    return `${year}. ${month}. ${day}`;
  }

  const date = new Date(dateIso);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}. ${month}. ${day}`;
}
