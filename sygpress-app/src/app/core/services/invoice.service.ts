import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Invoice, InvoiceRequest, PaymentRequest, ProcessingStatus } from '../models';
import { PageResponse } from './customer.service';

@Injectable({
  providedIn: 'root'
})
export class InvoiceService {
  private readonly apiUrl = `${environment.apiUrl}/invoices`;

  constructor(private http: HttpClient) {}

  getInvoices(page: number = 0, size: number = 10, search?: string, status?: ProcessingStatus): Observable<PageResponse<Invoice>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (search) {
      params = params.set('search', search);
    }
    if (status) {
      params = params.set('status', status);
    }

    return this.http.get<PageResponse<Invoice>>(this.apiUrl, { params });
  }

  getInvoice(publicId: string): Observable<Invoice> {
    return this.http.get<Invoice>(`${this.apiUrl}/${publicId}`);
  }

  createInvoice(invoice: InvoiceRequest): Observable<Invoice> {
    return this.http.post<Invoice>(this.apiUrl, invoice);
  }

  updateInvoice(publicId: string, invoice: InvoiceRequest): Observable<Invoice> {
    return this.http.put<Invoice>(`${this.apiUrl}/${publicId}`, invoice);
  }

  deleteInvoice(publicId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${publicId}`);
  }

  updateStatus(publicId: string, status: ProcessingStatus): Observable<Invoice> {
    return this.http.patch<Invoice>(`${this.apiUrl}/${publicId}/status`, { status });
  }

  addPayment(publicId: string, payment: PaymentRequest): Observable<Invoice> {
    return this.http.post<Invoice>(`${this.apiUrl}/${publicId}/payments`, payment);
  }

  printInvoice(publicId: string): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${publicId}/print`, {
      responseType: 'blob'
    });
  }
}
