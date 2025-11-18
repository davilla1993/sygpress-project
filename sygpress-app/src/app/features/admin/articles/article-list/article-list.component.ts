import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../../environments/environment';
import { Article, Category } from '../../../../core/models';
import { ToastService } from '../../../../shared/services/toast.service';

@Component({
  selector: 'app-article-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div>
      <!-- Header -->
      <div class="flex justify-between items-center mb-6">
        <div>
          <h1 class="text-2xl font-bold text-gray-800">Articles</h1>
          <p class="text-gray-600">Gérez vos articles (vêtements, etc.)</p>
        </div>
        <button (click)="showAddModal = true" class="btn-primary">
          <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
          </svg>
          Nouvel article
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
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Catégorie</th>
                <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
              @for (article of articles(); track article.publicId) {
                <tr class="hover:bg-gray-50">
                  <td class="px-6 py-4 whitespace-nowrap font-medium text-gray-900">{{ article.name }}</td>
                  <td class="px-6 py-4 whitespace-nowrap text-gray-500">{{ article.category?.name || '-' }}</td>
                  <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                    <button (click)="editArticle(article)" class="text-primary-600 hover:text-primary-900 mr-3">Modifier</button>
                    <button (click)="deleteArticle(article)" class="text-red-600 hover:text-red-900">Supprimer</button>
                  </td>
                </tr>
              } @empty {
                <tr>
                  <td colspan="3" class="px-6 py-12 text-center text-gray-500">
                    Aucun article trouvé
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
            <h3 class="text-lg font-semibold mb-4">{{ editingArticle ? 'Modifier' : 'Nouvel' }} article</h3>
            <div class="space-y-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Nom *</label>
                <input type="text" [(ngModel)]="formData.name" class="input" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Catégorie</label>
                <select [(ngModel)]="formData.categoryPublicId" class="input">
                  <option value="">Sans catégorie</option>
                  @for (category of categories(); track category.publicId) {
                    <option [value]="category.publicId">{{ category.name }}</option>
                  }
                </select>
              </div>
            </div>
            <div class="flex justify-end gap-2 mt-6">
              <button (click)="closeModal()" class="btn-secondary">Annuler</button>
              <button (click)="saveArticle()" class="btn-primary">{{ editingArticle ? 'Modifier' : 'Créer' }}</button>
            </div>
          </div>
        </div>
      }
    </div>
  `
})
export class ArticleListComponent implements OnInit {
  articles = signal<Article[]>([]);
  categories = signal<Category[]>([]);
  isLoading = signal(true);
  showAddModal = false;
  editingArticle: Article | null = null;
  formData = {
    name: '',
    categoryPublicId: ''
  };

  constructor(
    private http: HttpClient,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.loadArticles();
    this.loadCategories();
  }

  loadArticles(): void {
    this.http.get<Article[]>(`${environment.apiUrl}/articles`).subscribe({
      next: (articles) => {
        this.articles.set(articles);
        this.isLoading.set(false);
      },
      error: (error) => {
        const message = error.error?.message || 'Erreur lors du chargement des articles';
        this.toastService.error(message);
        this.isLoading.set(false);
      }
    });
  }

  loadCategories(): void {
    this.http.get<Category[]>(`${environment.apiUrl}/categories`).subscribe({
      next: (categories) => this.categories.set(categories)
    });
  }

  editArticle(article: Article): void {
    this.editingArticle = article;
    this.formData = {
      name: article.name,
      categoryPublicId: article.category?.publicId || ''
    };
    this.showAddModal = true;
  }

  saveArticle(): void {
    const request$ = this.editingArticle
      ? this.http.put(`${environment.apiUrl}/articles/${this.editingArticle.publicId}`, this.formData)
      : this.http.post(`${environment.apiUrl}/articles`, this.formData);

    request$.subscribe({
      next: () => {
        this.closeModal();
        this.loadArticles();
      },
      error: (error) => {
        const message = error.error?.message || 'Erreur lors de l\'enregistrement';
        this.toastService.error(message);
      }
    });
  }

  deleteArticle(article: Article): void {
    if (confirm(`Êtes-vous sûr de vouloir supprimer l'article "${article.name}" ?`)) {
      this.http.delete(`${environment.apiUrl}/articles/${article.publicId}`).subscribe({
        next: () => {
          this.loadArticles();
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
    this.editingArticle = null;
    this.formData = { name: '', categoryPublicId: '' };
  }
}
