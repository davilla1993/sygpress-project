import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../../environments/environment';
import { ToastService } from '../../../../shared/services/toast.service';

interface User {
  publicId: string;
  username: string;
  firstName: string;
  lastName: string;
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
  showEditModal = false;
  showPasswordModal = false;
  newUser = {
    firstName: '',
    lastName: '',
    username: '',
    email: '',
    password: '',
    role: 'USER'
  };
  editUser: any = null;
  passwordResetData: any = null;

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
    this.http.post<User>(`${environment.apiUrl}/admin/users`, this.newUser).subscribe({
      next: () => {
        this.showAddModal = false;
        this.newUser = { firstName: '', lastName: '', username: '', email: '', password: '', role: 'USER' };
        this.loadUsers();
        this.toastService.success('Utilisateur créé avec succès');
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

  openEditModal(user: User): void {
    this.editUser = {
      publicId: user.publicId,
      firstName: user.firstName,
      lastName: user.lastName,
      username: user.username,
      email: user.email,
      role: user.role
    };
    this.showEditModal = true;
  }

  updateUser(): void {
    if (!this.editUser) return;

    this.http.put<User>(`${environment.apiUrl}/users/${this.editUser.publicId}`, this.editUser).subscribe({
      next: () => {
        this.showEditModal = false;
        this.editUser = null;
        this.toastService.success('Utilisateur modifié avec succès');
        this.loadUsers();
      },
      error: (error) => {
        const message = error.error?.message || 'Erreur lors de la modification de l\'utilisateur';
        this.toastService.error(message);
      }
    });
  }

  resetPassword(user: User): void {
    const fullName = user.fullName || `${user.firstName} ${user.lastName}`.trim();
    if (!confirm(`Êtes-vous sûr de vouloir réinitialiser le mot de passe de "${fullName}" ?`)) {
      return;
    }

    this.http.post<any>(`${environment.apiUrl}/users/${user.publicId}/reset-password`, {}).subscribe({
      next: (response) => {
        this.passwordResetData = response;
        this.showPasswordModal = true;
        this.toastService.success('Mot de passe réinitialisé avec succès');
      },
      error: (error) => {
        const message = error.error?.message || 'Erreur lors de la réinitialisation du mot de passe';
        this.toastService.error(message);
      }
    });
  }

  copyToClipboard(text: string): void {
    navigator.clipboard.writeText(text).then(() => {
      this.toastService.success('Mot de passe copié dans le presse-papiers');
    }).catch(() => {
      this.toastService.error('Impossible de copier le mot de passe');
    });
  }

  closePasswordModal(): void {
    this.showPasswordModal = false;
    this.passwordResetData = null;
  }
}
