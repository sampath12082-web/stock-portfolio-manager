import axios from 'axios';
import type { ErrorResponse } from './types';

const client = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
});

client.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.data) {
      const err = error.response.data as ErrorResponse;
      return Promise.reject(new Error(err.message || 'Request failed'));
    }
    return Promise.reject(new Error('Unable to connect to server'));
  }
);

export default client;
