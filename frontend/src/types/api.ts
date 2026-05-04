export type ApiResponse<T> = {
  data: T;
  meta?: {
    correlationId?: string;
    timestamp?: string;
  };
  error?: {
    code?: string;
    message?: string;
    details?: any;
  };
};

export type PageResponse<T> = {
  items: T[];
  content: T[]; // Backward compatibility
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export interface ProblemDetail {
  type?: string;
  title?: string;
  status?: number;
  detail?: string;
  instance?: string;
  fields?: Record<string, string>;
  [key: string]: any;
}
