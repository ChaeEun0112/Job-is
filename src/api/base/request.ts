import { client } from '@/api/base/axios';
import { unwrapApiResponse } from '@/api/base/error';
import type { ApiEnvelope } from '@/api/types';
import '@/api/base/client';
import type { AxiosRequestConfig } from 'axios';

async function request<T>(config: AxiosRequestConfig) {
  return unwrapApiResponse(client.request<ApiEnvelope<T>>(config));
}

export const api = {
  get<T>(url: string, config?: AxiosRequestConfig) {
    return request<T>({ ...config, method: 'GET', url });
  },
  post<T>(url: string, data?: unknown, config?: AxiosRequestConfig) {
    return request<T>({ ...config, method: 'POST', url, data });
  },
  put<T>(url: string, data?: unknown, config?: AxiosRequestConfig) {
    return request<T>({ ...config, method: 'PUT', url, data });
  },
  patch<T>(url: string, data?: unknown, config?: AxiosRequestConfig) {
    return request<T>({ ...config, method: 'PATCH', url, data });
  },
  delete<T = void>(url: string, config?: AxiosRequestConfig) {
    return request<T>({ ...config, method: 'DELETE', url });
  },
};
