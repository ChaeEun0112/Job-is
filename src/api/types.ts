export interface ApiEnvelope<T> {
  isSuccess: boolean;
  code: string;
  message: string;
  data: T;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  isLast: boolean;
}
