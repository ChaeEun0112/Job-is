import { describe, expect, it } from '@jest/globals';
import { AxiosError, type AxiosResponse } from 'axios';
import type { ApiEnvelope } from '@/api/types';
import {
  ApiError,
  NetworkError,
  assertApiSuccess,
  isApiEnvelope,
  toRequestError,
  unwrapApiResponse,
} from '../error';

function createResponse<T>(data: ApiEnvelope<T>, status = 200): AxiosResponse<ApiEnvelope<T>> {
  return {
    data,
    status,
    statusText: '',
    headers: {},
    config: { headers: {} },
  };
}

describe('API 응답 에러 정규화', () => {
  it('유효한 API envelope만 식별한다', () => {
    expect(
      isApiEnvelope({ isSuccess: true, code: 'COMMON_200', message: '성공', data: null }),
    ).toBe(true);
    expect(isApiEnvelope({ isSuccess: true, code: 'COMMON_200' })).toBe(false);
  });

  it('HTTP 200이어도 isSuccess가 false면 ApiError로 처리한다', () => {
    const response = createResponse({
      isSuccess: false,
      code: 'PROFILE_400_1',
      message: '필수값을 입력해주세요.',
      data: null,
    });

    expect(() => assertApiSuccess(response)).toThrow(ApiError);
    expect(() => assertApiSuccess(response)).toThrow('필수값을 입력해주세요.');
  });

  it('성공 응답은 data만 언랩하고, HTTP 오류는 API 에러로 정규화한다', async () => {
    await expect(
      unwrapApiResponse(
        Promise.resolve(
          createResponse({
            isSuccess: true,
            code: 'COMMON_200',
            message: '성공',
            data: { id: 1 },
          }),
        ),
      ),
    ).resolves.toEqual({ id: 1 });

    const response = createResponse(
      {
        isSuccess: false,
        code: 'AUTH_403',
        message: '접근 권한이 없습니다.',
        data: null,
      },
      403,
    );
    const error = new AxiosError('Forbidden', undefined, undefined, undefined, response);

    expect(toRequestError(error)).toMatchObject({
      name: 'ApiError',
      status: 403,
      code: 'AUTH_403',
      message: '접근 권한이 없습니다.',
    });
  });

  it('응답이 없는 Axios 오류는 NetworkError로 정규화한다', () => {
    expect(toRequestError(new AxiosError('Network Error'))).toBeInstanceOf(NetworkError);
  });
});
