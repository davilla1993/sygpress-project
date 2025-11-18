export interface LoginRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  type: string;
  publicId: string;
  username: string;
  email: string;
  fullName: string;
  role: string;
}

export interface User {
  publicId: string;
  username: string;
  email: string;
  fullName: string;
  role: Role;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export type Role = 'ADMIN' | 'USER';
