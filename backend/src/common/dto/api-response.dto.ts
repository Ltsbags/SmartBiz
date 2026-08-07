export class ApiResponseDto<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;

  constructor(data: T, message = 'Success', success = true) {
    this.success = success;
    this.message = message;
    this.data = data;
    this.timestamp = new Date().toISOString();
  }
}
