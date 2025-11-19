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
  templateUrl: './article-list.component.html',
  styleUrls: ['./article-list.component.css']
})
export class ArticleListComponent implements OnInit {
  articles = signal<Article[]>([]);
  categories = signal<Category[]>([]);
  isLoading = signal(true);
  currentPage = signal(0);
  totalPages = signal(0);
  totalElements = signal(0);
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
    this.isLoading.set(true);
    this.http.get<any>(`${environment.apiUrl}/articles?page=${this.currentPage()}&size=15`).subscribe({
      next: (response) => {
        const articles = response.content || response;
        this.articles.set(Array.isArray(articles) ? articles : []);
        this.totalPages.set(response.totalPages || 1);
        this.totalElements.set(response.totalElements || articles.length);
        this.isLoading.set(false);
      },
      error: (error) => {
        const message = error.error?.message || 'Erreur lors du chargement des articles';
        this.toastService.error(message);
        this.isLoading.set(false);
      }
    });
  }

  goToPage(page: number): void {
    this.currentPage.set(page);
    this.loadArticles();
  }

  loadCategories(): void {
    this.http.get<any>(`${environment.apiUrl}/categories`).subscribe({
      next: (response) => {
        const categories = response.content || response;
        this.categories.set(Array.isArray(categories) ? categories : []);
      },
      error: (error) => {
        const message = error.error?.message || 'Erreur lors du chargement des catégories';
        this.toastService.error(message);
      }
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
