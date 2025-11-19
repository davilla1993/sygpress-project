import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { CustomerService } from '../../../core/services/customer.service';
import { InvoiceService } from '../../../core/services/invoice.service';
import { Customer, Invoice } from '../../../core/models';
import { ToastService } from '../../../shared/services/toast.service';
import { ConfirmModalComponent } from '../../../shared/components/confirm-modal/confirm-modal.component';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-customer-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, ConfirmModalComponent],
  templateUrl: './customer-detail.component.html',
  styleUrls: ['./customer-detail.component.css']
})
export class CustomerDetailComponent implements OnInit {
  customer = signal<Customer | null>(null);
  invoices = signal<Invoice[]>([]);
  isLoading = signal(true);
  showDeleteModal = false;

  constructor(
    private customerService: CustomerService,
    private http: HttpClient,
    private toastService: ToastService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadCustomer(id);
      this.loadInvoices(id);
    }
  }

  loadCustomer(id: string): void {
    this.customerService.getCustomer(id).subscribe({
      next: (customer) => {
        this.customer.set(customer);
        this.isLoading.set(false);
      },
      error: (error) => {
        const message = error.error?.message || 'Erreur lors du chargement du client';
        this.toastService.error(message);
        this.isLoading.set(false);
        this.router.navigate(['/customers']);
      }
    });
  }

  loadInvoices(customerId: string): void {
    this.http.get<any>(`${environment.apiUrl}/customers/${customerId}/invoices`).subscribe({
      next: (response) => {
        const invoices = response.content || response;
        this.invoices.set(Array.isArray(invoices) ? invoices : []);
      },
      error: () => {
        // Silent fail for invoices
      }
    });
  }

  deleteCustomer(): void {
    const customer = this.customer();
    if (!customer) return;

    this.customerService.deleteCustomer(customer.publicId).subscribe({
      next: () => {
        this.toastService.success('Client supprimé avec succès');
        this.router.navigate(['/customers']);
      },
      error: (error) => {
        const message = error.error?.message || 'Erreur lors de la suppression';
        this.toastService.error(message);
        this.showDeleteModal = false;
      }
    });
  }

  getInvoiceTotal(invoice: Invoice): number {
    const linesTotal = invoice.invoiceLines.reduce((sum, line) => sum + line.amount, 0);
    const feesTotal = invoice.additionalFees.reduce((sum, fee) => sum + fee.amount, 0);
    return linesTotal + feesTotal - (invoice.discount || 0) + (invoice.vatAmount || 0);
  }

  getTotalSpent(): number {
    return this.invoices().reduce((sum, inv) => sum + this.getInvoiceTotal(inv), 0);
  }

  getTotalPaid(): number {
    return this.invoices().reduce((sum, inv) => sum + inv.amountPaid, 0);
  }

  getTotalUnpaid(): number {
    return this.invoices().reduce((sum, inv) => sum + inv.remainingAmount, 0);
  }

  formatDate(date: string): string {
    return new Date(date).toLocaleDateString('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  formatShortDate(date: string): string {
    return new Date(date).toLocaleDateString('fr-FR');
  }

  formatMoney(amount: number): string {
    return new Intl.NumberFormat('fr-FR').format(amount) + ' FCFA';
  }
}
