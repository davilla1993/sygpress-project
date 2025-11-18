export interface Customer {
  publicId: string;
  name: string;
  phoneNumber: string;
  address: string;
  createdAt: string;
  updatedAt: string;
}

export interface CustomerRequest {
  name: string;
  phoneNumber: string;
  address?: string;
}
