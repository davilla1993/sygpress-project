import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { InvoiceService } from '../../../core/services/invoice.service';
import { PageResponse } from '../../../core/services/customer.service';
import { Invoice, ProcessingStatus } from '../../../core/models';
import { ToastService } from '../../../shared/services/toast.service';

@Component({
  selector: 'app-invoice-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './invoice-list.component.html',
  styleUrls: ['./invoice-list.component.css']
})
export class InvoiceListComponent implements OnInit {
  invoices = signal<Invoice[]>([]);
  isLoading = signal(true);
  searchTerm = '';
  statusFilter = '';
  paymentFilter = '';
  createdByFilter = '';
  startDate = '';
  endDate = '';
  currentPage = signal(0);
  totalPages = signal(0);
  totalElements = signal(0);
  creators = signal<string[]>([]);
  private searchTimeout: any;

  constructor(
    private invoiceService: InvoiceService,
    private toastService: ToastService
  ) { }

  ngOnInit(): void {
    this.loadCreators();
    this.loadInvoices();
  }

  loadCreators(): void {
    this.invoiceService.getCreators().subscribe({
      next: (creators) => {
        this.creators.set(creators);
      },
      error: (error) => {
        console.error('Erreur lors du chargement des créateurs:', error);
      }
    });
  }

  loadInvoices(): void {
    this.isLoading.set(true);
    const status = this.statusFilter ? this.statusFilter as ProcessingStatus : undefined;
    const createdBy = this.createdByFilter || undefined;
    this.invoiceService.getInvoices(this.currentPage(), 10, this.searchTerm || undefined, status, createdBy).subscribe({
      next: (response) => {
        let filtered = response.content;

        // Client-side filtering for payment status and dates (ideally this should be backend)
        if (this.paymentFilter) {
          filtered = filtered.filter(inv =>
            this.paymentFilter === 'paid' ? inv.invoicePaid : !inv.invoicePaid
          );
        }

        if (this.startDate) {
          filtered = filtered.filter(inv =>
            new Date(inv.depositDate) >= new Date(this.startDate)
          );
        }

        if (this.endDate) {
          filtered = filtered.filter(inv =>
            new Date(inv.depositDate) <= new Date(this.endDate + 'T23:59:59')
          );
        }

        this.invoices.set(filtered);
        this.totalPages.set(response.totalPages);
        this.totalElements.set(response.totalElements);
        this.isLoading.set(false);
      },
      error: (error) => {
        const message = error.error?.message || 'Erreur lors du chargement des factures';
        this.toastService.error(message);
        this.isLoading.set(false);
      }
    });
  }

  onSearch(): void {
    clearTimeout(this.searchTimeout);
    this.searchTimeout = setTimeout(() => {
      this.currentPage.set(0);
      this.loadInvoices();
    }, 300);
  }

  goToPage(page: number): void {
    this.currentPage.set(page);
    this.loadInvoices();
  }

  setToday(): void {
    const today = new Date().toISOString().split('T')[0];
    this.startDate = today;
    this.endDate = today;
    this.onSearch();
  }

  setThisWeek(): void {
    const today = new Date();
    const firstDay = new Date(today.setDate(today.getDate() - today.getDay() + 1));
    const lastDay = new Date(today.setDate(today.getDate() - today.getDay() + 7));
    this.startDate = firstDay.toISOString().split('T')[0];
    this.endDate = lastDay.toISOString().split('T')[0];
    this.onSearch();
  }

  setThisMonth(): void {
    const today = new Date();
    const firstDay = new Date(today.getFullYear(), today.getMonth(), 1);
    const lastDay = new Date(today.getFullYear(), today.getMonth() + 1, 0);
    this.startDate = firstDay.toISOString().split('T')[0];
    this.endDate = lastDay.toISOString().split('T')[0];
    this.onSearch();
  }

  clearFilters(): void {
    this.searchTerm = '';
    this.statusFilter = '';
    this.paymentFilter = '';
    this.createdByFilter = '';
    this.startDate = '';
    this.endDate = '';
    this.onSearch();
  }

  printInvoice(invoice: Invoice): void {
    this.invoiceService.printInvoice(invoice.publicId).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.target = '_blank';
        link.click();
        setTimeout(() => window.URL.revokeObjectURL(url), 100);
      },
      error: (error) => {
        const message = error.error?.message || 'Erreur lors de l\'impression';
        this.toastService.error(message);
      }
    });
  }

  getInvoiceTotal(invoice: Invoice): number {
    const linesTotal = invoice.invoiceLines.reduce((sum, line) => sum + line.amount, 0);
    const feesTotal = invoice.additionalFees.reduce((sum, fee) => sum + fee.amount, 0);
    return linesTotal + feesTotal - (invoice.discount || 0) + (invoice.vatAmount || 0);
  }

  getStatusLabel(status: ProcessingStatus): string {
    const labels: Record<ProcessingStatus, string> = {
      'COLLECTE': 'Collecté',
      'EN_LAVAGE': 'En lavage',
      'EN_REPASSAGE': 'En repassage',
      'PRET': 'Prêt',
      'LIVRE': 'Livré',
      'RECUPERE': 'Récupéré'
    };
    return labels[status] || status;
  }

  getStatusClass(status: ProcessingStatus): string {
    const classes: Record<ProcessingStatus, string> = {
      'COLLECTE': 'bg-gray-100 text-gray-700',
      'EN_LAVAGE': 'bg-blue-100 text-blue-700',
      'EN_REPASSAGE': 'bg-yellow-100 text-yellow-700',
      'PRET': 'bg-green-100 text-green-700',
      'LIVRE': 'bg-purple-100 text-purple-700',
      'RECUPERE': 'bg-gray-100 text-gray-700'
    };
    return classes[status] || 'bg-gray-100 text-gray-700';
  }

  formatDate(date: string): string {
    return new Date(date).toLocaleDateString('fr-FR');
  }

  formatDateTime(dateTime: string): string {
    if (!dateTime) return 'N/A';
    return new Date(dateTime).toLocaleDateString('fr-FR');
  }

  formatTime(dateTime: string): string {
    if (!dateTime) return '';
    return new Date(dateTime).toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  }

  formatMoney(amount: number): string {
    return new Intl.NumberFormat('fr-FR').format(amount) + ' FCFA';
  }
}
