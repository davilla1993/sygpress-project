import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../../environments/environment';
import { ToastService } from '../../../../shared/services/toast.service';

interface User {
  publicId: string;
  username: string;
  fullName: string;
  email: string;
  role: string;
  active: boolean;
  createdAt: string;
}

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.css']
})
export class UserListComponent implements OnInit {
  users = signal<User[]>([]);
  isLoading = signal(true);
  currentPage = signal(0);
  totalPages = signal(0);
  totalElements = signal(0);
  showAddModal = false;
  newUser = {
    fullName: '',
    username: '',
    email: '',
    password: '',
    role: 'USER'
  };

  constructor(
    private http: HttpClient,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.isLoading.set(true);
    this.http.get<any>(`${environment.apiUrl}/users?page=${this.currentPage()}&size=15`).subscribe({
      next: (response) => {
        const users = response.content || response;
        this.users.set(Array.isArray(users) ? users : []);
        this.totalPages.set(response.totalPages || 1);
        this.totalElements.set(response.totalElements || users.length);
        this.isLoading.set(false);
      },
      error: (error) => {
        const message = error.error?.message || 'Erreur lors du chargement des utilisateurs';
        this.toastService.error(message);
        this.isLoading.set(false);
      }
    });
  }

  goToPage(page: number): void {
    this.currentPage.set(page);
    this.loadUsers();
  }

  createUser(): void {
    this.http.post<User>(`${environment.apiUrl}/auth/register`, this.newUser).subscribe({
      next: () => {
        this.showAddModal = false;
        this.newUser = { fullName: '', username: '', email: '', password: '', role: 'USER' };
        this.loadUsers();
      },
      error: (error) => {
        const message = error.error?.message || 'Erreur lors de la création de l\'utilisateur';
        this.toastService.error(message);
      }
    });
  }

  toggleStatus(user: User): void {
    this.http.patch<User>(`${environment.apiUrl}/users/${user.publicId}/status`, {}).subscribe({
      next: () => {
        this.loadUsers();
      },
      error: (error) => {
        const message = error.error?.message || 'Erreur lors de la mise à jour du statut';
        this.toastService.error(message);
      }
    });
  }

  deleteUser(user: User): void {
    if (confirm(`Êtes-vous sûr de vouloir supprimer l'utilisateur "${user.fullName}" ?`)) {
      this.http.delete(`${environment.apiUrl}/users/${user.publicId}`).subscribe({
        next: () => {
          this.loadUsers();
        },
        error: (error) => {
          const message = error.error?.message || 'Erreur lors de la suppression';
          this.toastService.error(message);
        }
      });
    }
  }
}
