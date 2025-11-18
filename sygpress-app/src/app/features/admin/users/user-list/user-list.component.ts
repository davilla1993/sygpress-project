import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../../environments/environment';

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
  template: `
    <div>
      <!-- Header -->
      <div class="flex justify-between items-center mb-6">
        <div>
          <h1 class="text-2xl font-bold text-gray-800">Utilisateurs</h1>
          <p class="text-gray-600">Gérez les utilisateurs du système</p>
        </div>
        <button (click)="showAddModal = true" class="btn-primary">
          <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
          </svg>
          Nouvel utilisateur
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
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Username</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Email</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Rôle</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Statut</th>
                <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
              @for (user of users(); track user.publicId) {
                <tr class="hover:bg-gray-50">
                  <td class="px-6 py-4 whitespace-nowrap font-medium text-gray-900">{{ user.fullName }}</td>
                  <td class="px-6 py-4 whitespace-nowrap text-gray-500">{{ user.username }}</td>
                  <td class="px-6 py-4 whitespace-nowrap text-gray-500">{{ user.email }}</td>
                  <td class="px-6 py-4 whitespace-nowrap">
                    <span class="px-2 py-1 text-xs font-medium rounded-full"
                      [class.bg-purple-100]="user.role === 'ADMIN'"
                      [class.text-purple-700]="user.role === 'ADMIN'"
                      [class.bg-blue-100]="user.role === 'USER'"
                      [class.text-blue-700]="user.role === 'USER'"
                    >
                      {{ user.role }}
                    </span>
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap">
                    <span class="px-2 py-1 text-xs font-medium rounded-full"
                      [class.bg-green-100]="user.active"
                      [class.text-green-700]="user.active"
                      [class.bg-red-100]="!user.active"
                      [class.text-red-700]="!user.active"
                    >
                      {{ user.active ? 'Actif' : 'Inactif' }}
                    </span>
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                    <button (click)="toggleStatus(user)" class="text-gray-600 hover:text-gray-900 mr-3">
                      {{ user.active ? 'Désactiver' : 'Activer' }}
                    </button>
                    <button (click)="deleteUser(user)" class="text-red-600 hover:text-red-900">Supprimer</button>
                  </td>
                </tr>
              } @empty {
                <tr>
                  <td colspan="6" class="px-6 py-12 text-center text-gray-500">
                    Aucun utilisateur trouvé
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      }

      <!-- Add Modal -->
      @if (showAddModal) {
        <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div class="bg-white rounded-lg p-6 w-full max-w-md">
            <h3 class="text-lg font-semibold mb-4">Nouvel utilisateur</h3>
            <div class="space-y-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Nom complet *</label>
                <input type="text" [(ngModel)]="newUser.fullName" class="input" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Username *</label>
                <input type="text" [(ngModel)]="newUser.username" class="input" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Email *</label>
                <input type="email" [(ngModel)]="newUser.email" class="input" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Mot de passe *</label>
                <input type="password" [(ngModel)]="newUser.password" class="input" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Rôle</label>
                <select [(ngModel)]="newUser.role" class="input">
                  <option value="USER">Utilisateur</option>
                  <option value="ADMIN">Administrateur</option>
                </select>
              </div>
            </div>
            <div class="flex justify-end gap-2 mt-6">
              <button (click)="showAddModal = false" class="btn-secondary">Annuler</button>
              <button (click)="createUser()" class="btn-primary">Créer</button>
            </div>
          </div>
        </div>
      }
    </div>
  `
})
export class UserListComponent implements OnInit {
  users = signal<User[]>([]);
  isLoading = signal(true);
  showAddModal = false;
  newUser = {
    fullName: '',
    username: '',
    email: '',
    password: '',
    role: 'USER'
  };

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.http.get<User[]>(`${environment.apiUrl}/users`).subscribe({
      next: (users) => {
        this.users.set(users);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      }
    });
  }

  createUser(): void {
    this.http.post<User>(`${environment.apiUrl}/auth/register`, this.newUser).subscribe({
      next: () => {
        this.showAddModal = false;
        this.newUser = { fullName: '', username: '', email: '', password: '', role: 'USER' };
        this.loadUsers();
      }
    });
  }

  toggleStatus(user: User): void {
    this.http.patch<User>(`${environment.apiUrl}/users/${user.publicId}/status`, {}).subscribe({
      next: () => {
        this.loadUsers();
      }
    });
  }

  deleteUser(user: User): void {
    if (confirm(`Êtes-vous sûr de vouloir supprimer l'utilisateur "${user.fullName}" ?`)) {
      this.http.delete(`${environment.apiUrl}/users/${user.publicId}`).subscribe({
        next: () => {
          this.loadUsers();
        }
      });
    }
  }
}
