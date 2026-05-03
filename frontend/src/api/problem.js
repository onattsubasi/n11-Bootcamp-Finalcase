/**
 * Normalizes backend error responses into a consistent shape.
 * Follows RFC 7807 Problem Details format.
 */
export class ProblemDetail {
  constructor(status, type, title, detail, instance) {
    this.status = status;
    this.type = type;
    this.title = title;
    this.detail = detail;
    this.instance = instance;
  }

  static fromResponse(error) {
    if (error.response?.data && typeof error.response.data === 'object') {
      const data = error.response.data;
      return new ProblemDetail(
        data.status || error.response.status,
        data.type || 'about:blank',
        data.title || 'Error',
        data.detail || error.message || 'Unknown error',
        data.instance || error.config?.url
      );
    }

    return new ProblemDetail(
      error.response?.status || 500,
      'about:blank',
      error.message || 'Error',
      error.response?.statusText || 'Unknown error',
      undefined
    );
  }
}

export const isClientError = (status) => status >= 400 && status < 500;
export const isServerError = (status) => status >= 500 && status < 600;
