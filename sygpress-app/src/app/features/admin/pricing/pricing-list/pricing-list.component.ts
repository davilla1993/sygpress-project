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
    this.http.get<Pricing[]>(`${environment.apiUrl}/pricing`).subscribe({
      next: (pricings) => {
        this.pricings.set(pricings);
        this.isLoading.set(false);
      },
      error: (error) => {
        const message = error.error?.message || 'Erreur lors du chargement des tarifs';
        this.toastService.error(message);
        this.isLoading.set(false);
      }
    });
  }

  loadArticles(): void {
    this.http.get<Article[]>(`${environment.apiUrl}/articles`).subscribe({
      next: (articles) => this.articles.set(articles)
    });
  }

  loadServices(): void {
    this.http.get<Service[]>(`${environment.apiUrl}/services`).subscribe({
      next: (services) => this.services.set(services)
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
