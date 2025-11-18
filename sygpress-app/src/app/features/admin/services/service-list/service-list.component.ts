import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../../environments/environment';
import { Service } from '../../../../core/models';

@Component({
  selector: 'app-service-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div>
      <!-- Header -->
      <div class="flex justify-between items-center mb-6">
        <div>
          <h1 class="text-2xl font-bold text-gray-800">Services</h1>
          <p class="text-gray-600">Gérez vos services (lavage, repassage, etc.)</p>
        </div>
        <button (click)="showAddModal = true" class="btn-primary">
          <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
          </svg>
          Nouveau service
        </button>
      </div>

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
                <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
              @for (service of services(); track service.publicId) {
                <tr class="hover:bg-gray-50">
                  <td class="px-6 py-4 whitespace-nowrap font-medium text-gray-900">{{ service.name }}</td>
                  <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                    <button (click)="editService(service)" class="text-primary-600 hover:text-primary-900 mr-3">Modifier</button>
                    <button (click)="deleteService(service)" class="text-red-600 hover:text-red-900">Supprimer</button>
                  </td>
                </tr>
              } @empty {
                <tr>
                  <td colspan="2" class="px-6 py-12 text-center text-gray-500">
                    Aucun service trouvé
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      }

      <!-- Add/Edit Modal -->
      @if (showAddModal) {
        <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div class="bg-white rounded-lg p-6 w-full max-w-md">
            <h3 class="text-lg font-semibold mb-4">{{ editingService ? 'Modifier' : 'Nouveau' }} service</h3>
            <div class="space-y-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Nom *</label>
                <input type="text" [(ngModel)]="formData.name" class="input" />
              </div>
            </div>
            <div class="flex justify-end gap-2 mt-6">
              <button (click)="closeModal()" class="btn-secondary">Annuler</button>
              <button (click)="saveService()" class="btn-primary">{{ editingService ? 'Modifier' : 'Créer' }}</button>
            </div>
          </div>
        </div>
      }
    </div>
  `
})
export class ServiceListComponent implements OnInit {
  services = signal<Service[]>([]);
  isLoading = signal(true);
  showAddModal = false;
  editingService: Service | null = null;
  formData = {
    name: ''
  };

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadServices();
  }

  loadServices(): void {
    this.http.get<Service[]>(`${environment.apiUrl}/services`).subscribe({
      next: (services) => {
        this.services.set(services);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      }
    });
  }

  editService(service: Service): void {
    this.editingService = service;
    this.formData = { name: service.name };
    this.showAddModal = true;
  }

  saveService(): void {
    const request$ = this.editingService
      ? this.http.put(`${environment.apiUrl}/services/${this.editingService.publicId}`, this.formData)
      : this.http.post(`${environment.apiUrl}/services`, this.formData);

    request$.subscribe({
      next: () => {
        this.closeModal();
        this.loadServices();
      }
    });
  }

  deleteService(service: Service): void {
    if (confirm(`Êtes-vous sûr de vouloir supprimer le service "${service.name}" ?`)) {
      this.http.delete(`${environment.apiUrl}/services/${service.publicId}`).subscribe({
        next: () => {
          this.loadServices();
        }
      });
    }
  }

  closeModal(): void {
    this.showAddModal = false;
    this.editingService = null;
    this.formData = { name: '' };
  }
}
