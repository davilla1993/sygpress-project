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
  template: `
    <div>
      <!-- Header -->
      <div class="flex justify-between items-center mb-6">
        <div>
          <h1 class="text-2xl font-bold text-gray-800">Factures</h1>
          <p class="text-gray-600">Gérez vos factures</p>
        </div>
        <a routerLink="new" class="btn-primary">
          <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
          </svg>
          Nouvelle facture
        </a>
      </div>

      <!-- Advanced Filters -->
      <div class="card p-4 mb-6">
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          <!-- Search -->
          <div class="relative">
            <input
              type="text"
              [(ngModel)]="searchTerm"
              (ngModelChange)="onSearch()"
              placeholder="N° facture ou client..."
              class="input pl-10"
            />
            <svg class="w-5 h-5 text-gray-400 absolute left-3 top-1/2 -translate-y-1/2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
          </div>

          <!-- Status Filter -->
          <select [(ngModel)]="statusFilter" (ngModelChange)="onSearch()" class="input">
            <option value="">Tous les statuts</option>
            <option value="COLLECTE">Collecté</option>
            <option value="EN_LAVAGE">En lavage</option>
            <option value="EN_REPASSAGE">En repassage</option>
            <option value="PRET">Prêt</option>
            <option value="LIVRE">Livré</option>
            <option value="RECUPERE">Récupéré</option>
          </select>

          <!-- Payment Filter -->
          <select [(ngModel)]="paymentFilter" (ngModelChange)="onSearch()" class="input">
            <option value="">Tous paiements</option>
            <option value="paid">Payé</option>
            <option value="unpaid">Impayé</option>
          </select>

          <!-- Date Range -->
          <div class="flex gap-2">
            <input
              type="date"
              [(ngModel)]="startDate"
              (ngModelChange)="onSearch()"
              class="input text-sm"
              placeholder="Date début"
            />
            <input
              type="date"
              [(ngModel)]="endDate"
              (ngModelChange)="onSearch()"
              class="input text-sm"
              placeholder="Date fin"
            />
          </div>
        </div>

        <!-- Quick Filters -->
        <div class="flex gap-2 mt-3">
          <button (click)="setToday()" class="text-sm text-primary-600 hover:text-primary-800">Aujourd'hui</button>
          <span class="text-gray-300">|</span>
          <button (click)="setThisWeek()" class="text-sm text-primary-600 hover:text-primary-800">Cette semaine</button>
          <span class="text-gray-300">|</span>
          <button (click)="setThisMonth()" class="text-sm text-primary-600 hover:text-primary-800">Ce mois</button>
          <span class="text-gray-300">|</span>
          <button (click)="clearFilters()" class="text-sm text-gray-600 hover:text-gray-800">Réinitialiser</button>
        </div>
      </div>

      <!-- Loading -->
      @if (isLoading()) {
        <div class="flex items-center justify-center py-12">
          <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
        </div>
      } @else {
        <!-- Table -->
        <div class="card overflow-hidden">
          <table class="w-full">
            <thead class="bg-gray-50">
              <tr>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">N° Facture</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Client</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Date dépôt</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Statut</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Montant</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Paiement</th>
                <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
              @for (invoice of invoices(); track invoice.publicId) {
                <tr class="hover:bg-gray-50">
                  <td class="px-6 py-4 whitespace-nowrap font-medium text-gray-900">{{ invoice.invoiceNumber }}</td>
                  <td class="px-6 py-4 whitespace-nowrap text-gray-500">{{ invoice.customer.name }}</td>
                  <td class="px-6 py-4 whitespace-nowrap text-gray-500">{{ formatDate(invoice.depositDate) }}</td>
                  <td class="px-6 py-4 whitespace-nowrap">
                    <span class="px-2 py-1 text-xs font-medium rounded-full" [ngClass]="getStatusClass(invoice.processingStatus)">
                      {{ getStatusLabel(invoice.processingStatus) }}
                    </span>
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap font-medium">{{ formatMoney(getInvoiceTotal(invoice)) }}</td>
                  <td class="px-6 py-4 whitespace-nowrap">
                    <span
                      class="px-2 py-1 text-xs font-medium rounded-full"
                      [class.bg-green-100]="invoice.invoicePaid"
                      [class.text-green-700]="invoice.invoicePaid"
                      [class.bg-red-100]="!invoice.invoicePaid"
                      [class.text-red-700]="!invoice.invoicePaid"
                    >
                      {{ invoice.invoicePaid ? 'Payé' : 'Impayé' }}
                    </span>
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                    <a [routerLink]="[invoice.publicId]" class="text-primary-600 hover:text-primary-900 mr-3">Voir</a>
                    <button (click)="printInvoice(invoice)" class="text-gray-600 hover:text-gray-900 mr-3">Imprimer</button>
                  </td>
                </tr>
              } @empty {
                <tr>
                  <td colspan="7" class="px-6 py-12 text-center text-gray-500">
                    Aucune facture trouvée
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>

        <!-- Pagination -->
        @if (totalPages() > 1) {
          <div class="flex justify-between items-center mt-4">
            <p class="text-sm text-gray-500">
              Page {{ currentPage() + 1 }} sur {{ totalPages() }} ({{ totalElements() }} factures)
            </p>
            <div class="flex gap-2">
              <button
                (click)="goToPage(currentPage() - 1)"
                [disabled]="currentPage() === 0"
                class="btn-secondary"
                [class.opacity-50]="currentPage() === 0"
              >
                Précédent
              </button>
              <button
                (click)="goToPage(currentPage() + 1)"
                [disabled]="currentPage() >= totalPages() - 1"
                class="btn-secondary"
                [class.opacity-50]="currentPage() >= totalPages() - 1"
              >
                Suivant
              </button>
            </div>
          </div>
        }
      }
    </div>
  `
})
export class InvoiceListComponent implements OnInit {
  invoices = signal<Invoice[]>([]);
  isLoading = signal(true);
  searchTerm = '';
  statusFilter = '';
  paymentFilter = '';
  startDate = '';
  endDate = '';
  currentPage = signal(0);
  totalPages = signal(0);
  totalElements = signal(0);
  private searchTimeout: any;

  constructor(
    private invoiceService: InvoiceService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.loadInvoices();
  }

  loadInvoices(): void {
    this.isLoading.set(true);
    const status = this.statusFilter ? this.statusFilter as ProcessingStatus : undefined;
    this.invoiceService.getInvoices(this.currentPage(), 10, this.searchTerm || undefined, status).subscribe({
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
      error: () => {
        this.toastService.error('Erreur lors du chargement des factures');
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
    this.startDate = '';
    this.endDate = '';
    this.onSearch();
  }

  printInvoice(invoice: Invoice): void {
    this.invoiceService.printInvoice(invoice.publicId).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        window.open(url, '_blank');
      },
      error: () => {
        this.toastService.error('Erreur lors de l\'impression');
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

  formatMoney(amount: number): string {
    return new Intl.NumberFormat('fr-FR').format(amount) + ' FCFA';
  }
}
