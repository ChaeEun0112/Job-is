import axios, { type AxiosInstance } from 'axios';
import { ENV } from '@/api/env';

function createAxiosClient(): AxiosInstance {
  return axios.create({
    baseURL: ENV.API_BASE_URL,
    headers: { 'Content-Type': 'application/json' },
    withCredentials: true,
    timeout: 10000,
  });
}

export const client = createAxiosClient();
export const tokenClient = createAxiosClient();
