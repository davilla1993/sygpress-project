export interface Company {
  publicId: string;
  name: string;
  address: string;
  city: string;
  country: string;
  phoneNumber: string;
  email: string;
  website: string;
  logoUrl: string;
  slogan: string;
  vatRate: number;
  createdAt: string;
  updatedAt: string;
}

export interface CompanyRequest {
  name: string;
  address?: string;
  city?: string;
  country?: string;
  phoneNumber?: string;
  email?: string;
  website?: string;
  slogan?: string;
  vatRate?: number;
}
