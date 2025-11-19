import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../../environments/environment';
import { Service } from '../../../../core/models';
import { ToastService } from '../../../../shared/services/toast.service';

@Component({
  selector: 'app-service-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './service-list.component.html',
  styleUrls: ['./service-list.component.css']
})
export class ServiceListComponent implements OnInit {
  services = signal<Service[]>([]);
  isLoading = signal(true);
  showAddModal = false;
  editingService: Service | null = null;
  formData = {
    name: ''
  };

  constructor(
    private http: HttpClient,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.loadServices();
  }

  loadServices(): void {
    this.http.get<Service[]>(`${environment.apiUrl}/services`).subscribe({
      next: (services) => {
        this.services.set(services);
        this.isLoading.set(false);
      },
      error: (error) => {
        const message = error.error?.message || 'Erreur lors du chargement des services';
        this.toastService.error(message);
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
      },
      error: (error) => {
        const message = error.error?.message || 'Erreur lors de l\'enregistrement';
        this.toastService.error(message);
      }
    });
  }

  deleteService(service: Service): void {
    if (confirm(`Êtes-vous sûr de vouloir supprimer le service "${service.name}" ?`)) {
      this.http.delete(`${environment.apiUrl}/services/${service.publicId}`).subscribe({
        next: () => {
          this.loadServices();
        },
        error: (error) => {
          const message = error.error?.message || 'Erreur lors de la suppression';
          this.toastService.error(message);
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
