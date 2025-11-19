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
  templateUrl: './category-list.component.html',
  styleUrls: ['./category-list.component.css']
})
export class CategoryListComponent implements OnInit {
  categories = signal<(Category & { articleCount?: number })[]>([]);
  isLoading = signal(true);
  currentPage = signal(0);
  totalPages = signal(0);
  totalElements = signal(0);
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
    this.isLoading.set(true);
    this.http.get<any>(`${environment.apiUrl}/categories?page=${this.currentPage()}&size=15`).subscribe({
      next: (response) => {
        const categories = response.content || response;
        this.categories.set(Array.isArray(categories) ? categories : []);
        this.totalPages.set(response.totalPages || 1);
        this.totalElements.set(response.totalElements || categories.length);
        this.isLoading.set(false);
      },
      error: (error) => {
        const message = error.error?.message || 'Erreur lors du chargement des catégories';
        this.toastService.error(message);
        this.isLoading.set(false);
      }
    });
  }

  goToPage(page: number): void {
    this.currentPage.set(page);
    this.loadCategories();
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
