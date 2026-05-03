/**
 * Consistent error shape matching backend ErrorResponse contract.
 */
export class ApiError extends Error {
  constructor(status, errorCode, message, correlationId, fieldErrors = null) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.errorCode = errorCode;
    this.correlationId = correlationId;
    this.fieldErrors = fieldErrors;
  }

  /**
   * Factory method to create ApiError from Axios error.
   */
  static fromAxiosError(error) {
    if (error.response?.data && typeof error.response.data === 'object') {
      const data = error.response.data;
      return new ApiError(
        error.response.status,
        data.errorCode || 'UNKNOWN_ERROR',
        data.message || error.message || 'An unexpected error occurred',
        data.correlationId || null,
        data.fieldErrors || null
      );
    }

    return new ApiError(
      error.response?.status || 500,
      'NETWORK_ERROR',
      error.message || 'Network error',
      null,
      null
    );
  }

  /**
   * Helper to find a specific field error.
   */
  getFieldError(fieldName) {
    if (!this.fieldErrors) return null;
    return this.fieldErrors.find((fe) => fe.field === fieldName)?.message || null;
  }
}
