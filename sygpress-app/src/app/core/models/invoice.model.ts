export interface Invoice {
  publicId: string;
  invoiceNumber: string;
  customer: CustomerSummary;
  depositDate: string;
  deliveryDate: string;
  processingStatus: ProcessingStatus;
  invoiceLines: InvoiceLine[];
  additionalFees: AdditionalFee[];
  discount: number;
  vatRate: number;  // Taux de TVA en %
  amountPaid: number;
  remainingAmount: number;
  invoicePaid: boolean;
  observations: string;

  // Champs calculés
  subtotalAmount: number;  // Montant HT
  vatAmount: number;       // Montant TVA calculé
  totalAmount: number;     // Montant TTC

  createdAt: string;
  updatedAt: string;
  createdBy: string;
}

export interface InvoiceLine {
  publicId: string;
  pricing: Pricing;
  quantity: number;
  amount: number;
}

export interface Pricing {
  publicId: string;
  article: Article;
  service: Service;
  price: number;
}

export interface Article {
  publicId: string;
  name: string;
  category: Category;
}

export interface Category {
  publicId: string;
  name: string;
}

export interface Service {
  publicId: string;
  name: string;
}

export interface AdditionalFee {
  publicId: string;
  title: string;
  description: string;
  amount: number;
}

export interface CustomerSummary {
  publicId: string;
  name: string;
  phoneNumber: string;
}

export type ProcessingStatus = 'COLLECTE' | 'EN_LAVAGE' | 'EN_REPASSAGE' | 'PRET' | 'LIVRE' | 'RECUPERE';

export interface InvoiceRequest {
  customerPublicId: string;
  depositDate: string;
  deliveryDate: string;
  invoiceLines: InvoiceLineRequest[];
  additionalFees?: AdditionalFeeRequest[];
  discount?: number;
  vatRate?: number;
  amountPaid?: number;
  observations?: string;
}

export interface InvoiceLineRequest {
  pricingPublicId: string;
  quantity: number;
}

export interface AdditionalFeeRequest {
  title: string;
  description?: string;
  amount: number;
}

export interface PaymentRequest {
  amount: number;
}
