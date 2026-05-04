import type { PageResponse } from '../../types/api';

/**
 * API Response Helpers
 * Handles backend response envelopes and pagination mapping.
 */

export const unwrapApiResponse = <T = any>(payload: any): T => {
  return payload?.data ?? payload;
};

export const unwrapPage = <T = any>(payload: any): PageResponse<T> => {
  const source = unwrapApiResponse<any>(payload);
  const items = source?.items ?? source?.content ?? [];

  return {
    ...source,
    content: Array.isArray(items) ? items : [],
    items: Array.isArray(items) ? items : [],
    totalElements: source?.totalElements ?? source?.page?.totalElements ?? items.length,
    totalPages: source?.totalPages ?? source?.page?.totalPages ?? 1,
    page: source?.page?.page ?? source?.page ?? 0,
    size: source?.page?.size ?? source?.size ?? items.length,
  };
};
