import axios, { type AxiosResponse } from 'axios';
import type { ApiEnvelope } from '@/api/types';

export class ApiError extends Error {
  readonly status: number;
  readonly code: string;

  constructor({ status, code, message }: { status: number; code: string; message: string }) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.code = code;
  }
}

export class NetworkError extends Error {
  readonly code: string;

  constructor(message = '네트워크 연결을 확인해주세요.') {
    super(message);
    this.name = 'NetworkError';
    this.code = 'NETWORK_ERROR';
  }
}

export function isApiEnvelope(data: unknown): data is ApiEnvelope<unknown> {
  return (
    typeof data === 'object' &&
    data !== null &&
    'isSuccess' in data &&
    typeof data.isSuccess === 'boolean' &&
    'code' in data &&
    typeof data.code === 'string' &&
    'message' in data &&
    typeof data.message === 'string'
  );
}

export function assertApiSuccess(response: AxiosResponse) {
  if (isApiEnvelope(response.data) && !response.data.isSuccess) {
    throw new ApiError({
      status: response.status,
      code: response.data.code,
      message: response.data.message,
    });
  }

  return response;
}

export function toRequestError(error: unknown) {
  if (error instanceof ApiError || error instanceof NetworkError) return error;
  if (!axios.isAxiosError(error)) return error;

  if (!error.response) {
    return new NetworkError(error.message);
  }

  const envelope = isApiEnvelope(error.response.data) ? error.response.data : null;
  return new ApiError({
    status: error.response.status,
    code: envelope?.code ?? `HTTP_${error.response.status}`,
    message: envelope?.message ?? error.message,
  });
}

export async function unwrapApiResponse<T>(request: Promise<AxiosResponse<ApiEnvelope<T>>>) {
  try {
    const response = await request;
    assertApiSuccess(response);
    return response.data.data;
  } catch (error) {
    throw toRequestError(error);
  }
}
