import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../../environments/environment';
import { Pricing, Article, Service } from '../../../../core/models';
import { ToastService } from '../../../../shared/services/toast.service';

@Component({
  selector: 'app-pricing-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './pricing-list.component.html',
  styleUrls: ['./pricing-list.component.css']
})
export class PricingListComponent implements OnInit {
  pricings = signal<Pricing[]>([]);
  articles = signal<Article[]>([]);
  services = signal<Service[]>([]);
  isLoading = signal(true);
  currentPage = signal(0);
  totalPages = signal(0);
  totalElements = signal(0);
  selectedServiceFilter = signal<string>('');
  showAddModal = false;
  editingPricing: Pricing | null = null;
  formData = {
    articlePublicId: '',
    servicePublicId: '',
    price: 0
  };

  constructor(
    private http: HttpClient,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.loadPricings();
    this.loadArticles();
    this.loadServices();
  }

  loadPricings(): void {
    this.isLoading.set(true);
    const serviceFilter = this.selectedServiceFilter();
    const baseUrl = serviceFilter
      ? `${environment.apiUrl}/pricing/service/${serviceFilter}`
      : `${environment.apiUrl}/pricing`;

    this.http.get<any>(`${baseUrl}?page=${this.currentPage()}&size=15`).subscribe({
      next: (response) => {
        const pricings = response.content || response;
        this.pricings.set(Array.isArray(pricings) ? pricings : []);
        this.totalPages.set(response.totalPages || 1);
        this.totalElements.set(response.totalElements || pricings.length);
        this.isLoading.set(false);
      },
      error: (error) => {
        const message = error.error?.message || 'Erreur lors du chargement des tarifs';
        this.toastService.error(message);
        this.isLoading.set(false);
      }
    });
  }

  onServiceFilterChange(servicePublicId: string): void {
    this.selectedServiceFilter.set(servicePublicId);
    this.currentPage.set(0);
    this.loadPricings();
  }

  goToPage(page: number): void {
    this.currentPage.set(page);
    this.loadPricings();
  }

  loadArticles(): void {
    this.http.get<any>(`${environment.apiUrl}/articles`).subscribe({
      next: (response) => {
        const articles = response.content || response;
        this.articles.set(Array.isArray(articles) ? articles : []);
      }
    });
  }

  loadServices(): void {
    this.http.get<any>(`${environment.apiUrl}/services`).subscribe({
      next: (response) => {
        const services = response.content || response;
        this.services.set(Array.isArray(services) ? services : []);
      }
    });
  }

  editPricing(pricing: Pricing): void {
    this.editingPricing = pricing;
    this.formData = {
      articlePublicId: pricing.article.publicId,
      servicePublicId: pricing.service.publicId,
      price: pricing.price
    };
    this.showAddModal = true;
  }

  savePricing(): void {
    const request$ = this.editingPricing
      ? this.http.put(`${environment.apiUrl}/pricing/${this.editingPricing.publicId}`, this.formData)
      : this.http.post(`${environment.apiUrl}/pricing`, this.formData);

    request$.subscribe({
      next: () => {
        this.closeModal();
        this.loadPricings();
      },
      error: (error) => {
        const message = error.error?.message || 'Erreur lors de l\'enregistrement';
        this.toastService.error(message);
      }
    });
  }

  deletePricing(pricing: Pricing): void {
    if (confirm(`Êtes-vous sûr de vouloir supprimer ce tarif ?`)) {
      this.http.delete(`${environment.apiUrl}/pricing/${pricing.publicId}`).subscribe({
        next: () => {
          this.loadPricings();
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
    this.editingPricing = null;
    this.formData = { articlePublicId: '', servicePublicId: '', price: 0 };
  }

  formatMoney(amount: number): string {
    return new Intl.NumberFormat('fr-FR').format(amount) + ' FCFA';
  }
}
