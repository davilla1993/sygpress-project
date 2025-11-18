import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../../environments/environment';
import { Category } from '../../../../core/models';
import { ToastService } from '../../../../shared/services/toast.service';
import { ConfirmModalComponent } from '../../../../shared/components/confirm-modal/confirm-modal.component';

@Component({
  selector: 'app-category-list',
  standalone: true,
  imports: [CommonModule, FormsModule, ConfirmModalComponent],
  template: `
    <div>
      <!-- Header -->
      <div class="flex justify-between items-center mb-6">
        <div>
          <h1 class="text-2xl font-bold text-gray-800">Catégories</h1>
          <p class="text-gray-600">Gérez les catégories d'articles</p>
        </div>
        <button (click)="showAddModal = true" class="btn-primary">
          <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
          </svg>
          Nouvelle catégorie
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
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Nb Articles</th>
                <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
              @for (category of categories(); track category.publicId) {
                <tr class="hover:bg-gray-50">
                  <td class="px-6 py-4 whitespace-nowrap font-medium text-gray-900">{{ category.name }}</td>
                  <td class="px-6 py-4 whitespace-nowrap text-gray-500">{{ category.articleCount || 0 }}</td>
                  <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                    <button (click)="editCategory(category)" class="text-primary-600 hover:text-primary-900 mr-3">Modifier</button>
                    <button (click)="confirmDelete(category)" class="text-red-600 hover:text-red-900">Supprimer</button>
                  </td>
                </tr>
              } @empty {
                <tr>
                  <td colspan="3" class="px-6 py-12 text-center text-gray-500">
                    Aucune catégorie trouvée
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
            <h3 class="text-lg font-semibold mb-4">{{ editingCategory ? 'Modifier' : 'Nouvelle' }} catégorie</h3>
            <div class="space-y-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Nom *</label>
                <input type="text" [(ngModel)]="formData.name" class="input" />
              </div>
            </div>
            <div class="flex justify-end gap-2 mt-6">
              <button (click)="closeModal()" class="btn-secondary">Annuler</button>
              <button (click)="saveCategory()" class="btn-primary" [disabled]="!formData.name">
                {{ editingCategory ? 'Modifier' : 'Créer' }}
              </button>
            </div>
          </div>
        </div>
      }

      <!-- Confirm Delete Modal -->
      <app-confirm-modal
        [isOpen]="showDeleteModal"
        title="Supprimer la catégorie"
        [message]="'Êtes-vous sûr de vouloir supprimer la catégorie \\'' + (categoryToDelete?.name || '') + '\\' ?'"
        confirmText="Supprimer"
        type="danger"
        (confirm)="deleteCategory()"
        (cancel)="showDeleteModal = false"
      />
    </div>
  `
})
export class CategoryListComponent implements OnInit {
  categories = signal<(Category & { articleCount?: number })[]>([]);
  isLoading = signal(true);
  showAddModal = false;
  showDeleteModal = false;
  editingCategory: Category | null = null;
  categoryToDelete: Category | null = null;
  formData = {
    name: ''
  };

  constructor(
    private http: HttpClient,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories(): void {
    this.http.get<(Category & { articleCount?: number })[]>(`${environment.apiUrl}/categories`).subscribe({
      next: (categories) => {
        this.categories.set(categories);
        this.isLoading.set(false);
      },
      error: (error) => {
        const message = error.error?.message || 'Erreur lors du chargement des catégories';
        this.toastService.error(message);
        this.isLoading.set(false);
      }
    });
  }

  editCategory(category: Category): void {
    this.editingCategory = category;
    this.formData = { name: category.name };
    this.showAddModal = true;
  }

  saveCategory(): void {
    if (!this.formData.name) return;

    const request$ = this.editingCategory
      ? this.http.put(`${environment.apiUrl}/categories/${this.editingCategory.publicId}`, this.formData)
      : this.http.post(`${environment.apiUrl}/categories`, this.formData);

    request$.subscribe({
      next: () => {
        this.toastService.success(
          this.editingCategory ? 'Catégorie modifiée avec succès' : 'Catégorie créée avec succès'
        );
        this.closeModal();
        this.loadCategories();
      },
      error: (error) => {
        const message = error.error?.message || 'Erreur lors de l\'enregistrement';
        this.toastService.error(message);
      }
    });
  }

  confirmDelete(category: Category): void {
    this.categoryToDelete = category;
    this.showDeleteModal = true;
  }

  deleteCategory(): void {
    if (!this.categoryToDelete) return;

    this.http.delete(`${environment.apiUrl}/categories/${this.categoryToDelete.publicId}`).subscribe({
      next: () => {
        this.toastService.success('Catégorie supprimée avec succès');
        this.showDeleteModal = false;
        this.categoryToDelete = null;
        this.loadCategories();
      },
      error: (error) => {
        const message = error.error?.message || 'Erreur lors de la suppression';
        this.toastService.error(message);
        this.showDeleteModal = false;
      }
    });
  }

  closeModal(): void {
    this.showAddModal = false;
    this.editingCategory = null;
    this.formData = { name: '' };
  }
}
