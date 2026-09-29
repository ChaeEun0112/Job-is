const EMPLOYMENT_TYPE_LABELS: Record<string, string> = {
  regular: '정규직',
  contract: '계약직',
  intern: '인턴',
};

export function formatEmploymentType(employmentType: string): string {
  const normalizedEmploymentType = employmentType.toLowerCase();

  return Object.hasOwn(EMPLOYMENT_TYPE_LABELS, normalizedEmploymentType)
    ? EMPLOYMENT_TYPE_LABELS[normalizedEmploymentType]
    : employmentType;
}
