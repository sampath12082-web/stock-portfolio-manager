import axios from 'axios';
import type { ErrorResponse } from './types';

const client = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
});

client.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

client.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    if (error.response?.data) {
      const err = error.response.data as ErrorResponse;
      return Promise.reject(new Error(err.message || 'Request failed'));
    }
    return Promise.reject(new Error('Unable to connect to server'));
  }
);

export default client;
