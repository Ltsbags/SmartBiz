export interface UserProfile {
  id: string;
  email: string;
  name: string;
  companyName: string;
  gstin: string;
  role: 'CUSTOMER_ADMIN' | 'PURCHASING_AGENT' | 'FINANCE_VIEWER';
  creditLimit: number;
  creditBalance: number;
  availableCredit: number;
  paymentTerms: string;
}

export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

export interface LoginResponse {
  user: UserProfile;
  tokens: AuthTokens;
}