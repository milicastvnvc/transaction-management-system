import { HttpErrorResponse } from '@angular/common/http';

import { ApiError, ValidationError } from '../models/api-error';

export function getApiErrorMessage(error: unknown, fallbackMessage: string): string {
  if (!(error instanceof HttpErrorResponse)) {
    return fallbackMessage;
  }

  const apiError = error.error as ApiError | null;

  if (apiError?.message) {
    return apiError.message;
  }

  if (error.status === 0) {
    return 'The backend service is unavailable. Please check that it is running.';
  }

  return fallbackMessage;
}

export function isValidationError(error: unknown): error is HttpErrorResponse & {
  error: ValidationError;
} {
  if (!(error instanceof HttpErrorResponse)) {
    return false;
  }

  const response = error.error as ValidationError | null;

  return error.status === 400 && response?.fieldErrors != null;
}
