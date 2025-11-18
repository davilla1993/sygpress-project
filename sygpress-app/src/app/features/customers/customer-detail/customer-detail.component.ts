import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { CustomerService } from '../../../core/services/customer.service';
import { Customer } from '../../../core/models';

@Component({
  selector: 'app-customer-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div>
      @if (isLoading()) {
        <div class="flex items-center justify-center py-12">
          <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
        </div>
      } @else if (customer()) {
        <!-- Header -->
        <div class="flex justify-between items-start mb-6">
          <div>
            <h1 class="text-2xl font-bold text-gray-800">{{ customer()!.name }}</h1>
            <p class="text-gray-600">Détails du client</p>
          </div>
          <div class="flex gap-2">
            <a [routerLink]="['edit']" class="btn-secondary">
              <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
              </svg>
              Modifier
            </a>
            <button (click)="deleteCustomer()" class="btn-danger">
              <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
              Supprimer
            </button>
          </div>
        </div>

        <!-- Info Card -->
        <div class="card p-6">
          <h3 class="text-lg font-semibold text-gray-800 mb-4">Informations</h3>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label class="block text-sm font-medium text-gray-500">Nom</label>
              <p class="mt-1 text-gray-900">{{ customer()!.name }}</p>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-500">Téléphone</label>
              <p class="mt-1 text-gray-900">{{ customer()!.phoneNumber }}</p>
            </div>
            <div class="md:col-span-2">
              <label class="block text-sm font-medium text-gray-500">Adresse</label>
              <p class="mt-1 text-gray-900">{{ customer()!.address || '-' }}</p>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-500">Date de création</label>
              <p class="mt-1 text-gray-900">{{ formatDate(customer()!.createdAt) }}</p>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-500">Dernière modification</label>
              <p class="mt-1 text-gray-900">{{ formatDate(customer()!.updatedAt) }}</p>
            </div>
          </div>
        </div>

        <!-- Back button -->
        <div class="mt-6">
          <a routerLink="/customers" class="text-primary-600 hover:text-primary-800 flex items-center">
            <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
            </svg>
            Retour à la liste
          </a>
        </div>
      }
    </div>
  `
})
export class CustomerDetailComponent implements OnInit {
  customer = signal<Customer | null>(null);
  isLoading = signal(true);

  constructor(
    private customerService: CustomerService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadCustomer(id);
    }
  }

  loadCustomer(id: string): void {
    this.customerService.getCustomer(id).subscribe({
      next: (customer) => {
        this.customer.set(customer);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
        this.router.navigate(['/customers']);
      }
    });
  }

  deleteCustomer(): void {
    const customer = this.customer();
    if (customer && confirm(`Êtes-vous sûr de vouloir supprimer le client "${customer.name}" ?`)) {
      this.customerService.deleteCustomer(customer.publicId).subscribe({
        next: () => {
          this.router.navigate(['/customers']);
        }
      });
    }
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
}
