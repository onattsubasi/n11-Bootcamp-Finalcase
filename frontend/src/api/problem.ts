import axios from 'axios';
import { ProblemDetail } from '../types/api';

/**
 * Turns any thrown value into a user-readable message.
 * Handles Axios errors (checking for backend ProblemDetail), native Errors, and strings.
 */
export function errorMessage(err: unknown, fallback = 'Bir hata oluştu.'): string {
  if (axios.isAxiosError(err)) {
    const data = err.response?.data as ProblemDetail | any;
    // Check if it's our standard ApiResponse error or RFC 7807 ProblemDetail
    return data?.detail || data?.error?.message || err.message || fallback;
  }
  
  if (err instanceof Error) return err.message || fallback;
  if (typeof err === 'string') return err;
  
  return fallback;
}

/**
 * Returns field-level errors if the server sent them (validation failures).
 */
export function errorFields(err: unknown): Record<string, string> | undefined {
  if (axios.isAxiosError(err)) {
    const data = err.response?.data as ProblemDetail | any;
    return data?.fields || data?.error?.details;
  }
  return undefined;
}
