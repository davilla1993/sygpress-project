import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CustomerService, PageResponse } from '../../../core/services/customer.service';
import { Customer } from '../../../core/models';
import { ToastService } from '../../../shared/services/toast.service';
import { ConfirmModalComponent } from '../../../shared/components/confirm-modal/confirm-modal.component';

@Component({
  selector: 'app-customer-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, ConfirmModalComponent],
  template: `
    <div>
      <!-- Header -->
      <div class="flex justify-between items-center mb-6">
        <div>
          <h1 class="text-2xl font-bold text-gray-800">Clients</h1>
          <p class="text-gray-600">Gérez vos clients</p>
        </div>
        <a routerLink="new" class="btn-primary">
          <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
          </svg>
          Nouveau client
        </a>
      </div>

      <!-- Search -->
      <div class="card p-4 mb-6">
        <div class="relative">
          <input
            type="text"
            [(ngModel)]="searchTerm"
            (ngModelChange)="onSearch()"
            placeholder="Rechercher par nom ou téléphone..."
            class="input pl-10"
          />
          <svg class="w-5 h-5 text-gray-400 absolute left-3 top-1/2 -translate-y-1/2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
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
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Nom</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Téléphone</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Adresse</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Date création</th>
                <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
              @for (customer of customers(); track customer.publicId) {
                <tr class="hover:bg-gray-50">
                  <td class="px-6 py-4 whitespace-nowrap">
                    <div class="font-medium text-gray-900">{{ customer.name }}</div>
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap text-gray-500">{{ customer.phoneNumber }}</td>
                  <td class="px-6 py-4 whitespace-nowrap text-gray-500">{{ customer.address || '-' }}</td>
                  <td class="px-6 py-4 whitespace-nowrap text-gray-500">{{ formatDate(customer.createdAt) }}</td>
                  <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                    <a [routerLink]="[customer.publicId]" class="text-primary-600 hover:text-primary-900 mr-3">Voir</a>
                    <a [routerLink]="[customer.publicId, 'edit']" class="text-gray-600 hover:text-gray-900 mr-3">Modifier</a>
                    <button (click)="confirmDelete(customer)" class="text-red-600 hover:text-red-900">Supprimer</button>
                  </td>
                </tr>
              } @empty {
                <tr>
                  <td colspan="5" class="px-6 py-12 text-center text-gray-500">
                    Aucun client trouvé
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
              Page {{ currentPage() + 1 }} sur {{ totalPages() }} ({{ totalElements() }} clients)
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

      <!-- Confirm Delete Modal -->
      <app-confirm-modal
        [isOpen]="showDeleteModal"
        title="Supprimer le client"
        [message]="'Êtes-vous sûr de vouloir supprimer le client \\'' + (customerToDelete?.name || '') + '\\' ?'"
        confirmText="Supprimer"
        type="danger"
        (confirm)="deleteCustomer()"
        (cancel)="showDeleteModal = false"
      />
    </div>
  `
})
export class CustomerListComponent implements OnInit {
  customers = signal<Customer[]>([]);
  isLoading = signal(true);
  searchTerm = '';
  currentPage = signal(0);
  totalPages = signal(0);
  totalElements = signal(0);
  showDeleteModal = false;
  customerToDelete: Customer | null = null;
  private searchTimeout: any;

  constructor(
    private customerService: CustomerService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.loadCustomers();
  }

  loadCustomers(): void {
    this.isLoading.set(true);
    this.customerService.getCustomers(this.currentPage(), 10, this.searchTerm || undefined).subscribe({
      next: (response) => {
        this.customers.set(response.content);
        this.totalPages.set(response.totalPages);
        this.totalElements.set(response.totalElements);
        this.isLoading.set(false);
      },
      error: (error) => {
        const message = error.error?.message || 'Erreur lors du chargement des clients';
        this.toastService.error(message);
        this.isLoading.set(false);
      }
    });
  }

  onSearch(): void {
    clearTimeout(this.searchTimeout);
    this.searchTimeout = setTimeout(() => {
      this.currentPage.set(0);
      this.loadCustomers();
    }, 300);
  }

  goToPage(page: number): void {
    this.currentPage.set(page);
    this.loadCustomers();
  }

  confirmDelete(customer: Customer): void {
    this.customerToDelete = customer;
    this.showDeleteModal = true;
  }

  deleteCustomer(): void {
    if (!this.customerToDelete) return;

    this.customerService.deleteCustomer(this.customerToDelete.publicId).subscribe({
      next: () => {
        this.toastService.success('Client supprimé avec succès');
        this.showDeleteModal = false;
        this.customerToDelete = null;
        this.loadCustomers();
      },
      error: (error) => {
        const message = error.error?.message || 'Erreur lors de la suppression';
        this.toastService.error(message);
        this.showDeleteModal = false;
      }
    });
  }

  formatDate(date: string): string {
    return new Date(date).toLocaleDateString('fr-FR');
  }
}
