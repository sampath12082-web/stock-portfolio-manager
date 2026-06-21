export interface UserResponse {
  id: number;
  email: string;
  firstName: string;
  lastName: string | null;
  phone: string | null;
  role: string;
  status: string;
  emailVerified: boolean;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  user: UserResponse;
}
