export interface ApiError {
  status: number;
  message: string;
}

export interface ValidationError extends ApiError {
  fieldErrors: Record<string, string>;
}
