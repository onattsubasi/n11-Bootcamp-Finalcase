import type { AxiosError } from 'axios';

export type ApiFieldError = {
  field: string;
  message: string;
};

export type BackendErrorPayload = {
  success?: boolean;
  status?: number;
  errorCode?: string;
  message?: string;
  correlationId?: string;
  errors?: ApiFieldError[];
  fieldErrors?: ApiFieldError[];
};

export class ApiError extends Error {
  status: number;
  errorCode: string;
  correlationId: string | null;
  fieldErrors: ApiFieldError[] | null;

  constructor(
    status: number,
    errorCode: string,
    message: string,
    correlationId: string | null = null,
    fieldErrors: ApiFieldError[] | null = null,
  ) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.errorCode = errorCode;
    this.correlationId = correlationId;
    this.fieldErrors = fieldErrors;
  }

  static fromAxiosError(error: AxiosError<BackendErrorPayload>): ApiError {
    if (error.response?.data && typeof error.response.data === 'object') {
      const data = error.response.data;
      return new ApiError(
        error.response.status,
        data.errorCode || 'UNKNOWN_ERROR',
        data.message || error.message || 'An unexpected error occurred',
        data.correlationId || null,
        data.errors || data.fieldErrors || null,
      );
    }

    return new ApiError(
      error.response?.status || 500,
      'NETWORK_ERROR',
      error.message || 'Network error',
      null,
      null,
    );
  }

  getFieldError(fieldName: string): string | null {
    if (!this.fieldErrors) return null;
    return this.fieldErrors.find((fieldError) => fieldError.field === fieldName)?.message || null;
  }
}

export const getApiErrorPayload = (error: unknown): BackendErrorPayload | undefined => {
  const maybeAxiosError = error as AxiosError<BackendErrorPayload>;
  return maybeAxiosError.response?.data;
};

export const getApiFieldErrors = (error: unknown): Record<string, string[]> => {
  const payload = getApiErrorPayload(error);
  const errors = payload?.errors || payload?.fieldErrors || [];

  return errors.reduce<Record<string, string[]>>((acc, item) => {
    if (!item.field || !item.message) return acc;
    acc[item.field] = [...(acc[item.field] ?? []), item.message];
    return acc;
  }, {});
};

export const getApiErrorMessage = (error: unknown, fallback = 'Something went wrong'): string => {
  const payload = getApiErrorPayload(error);
  const errors = payload?.errors || payload?.fieldErrors || [];

  if (errors.length > 0) {
    return errors.map((item) => item.message).filter(Boolean).join('\n');
  }

  if (payload?.message) return payload.message;
  if (error instanceof Error) return error.message;

  return fallback;
};
