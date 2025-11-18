import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="min-h-screen bg-gray-50 flex">
      <!-- Sidebar -->
      <aside
        class="fixed inset-y-0 left-0 z-50 w-64 bg-white shadow-lg transform transition-transform duration-300 lg:translate-x-0 lg:static lg:inset-auto"
        [class.-translate-x-full]="!sidebarOpen()"
        [class.translate-x-0]="sidebarOpen()"
      >
        <!-- Logo -->
        <div class="h-16 flex items-center justify-center border-b border-gray-200">
          <h1 class="text-xl font-bold text-primary-600">SygPress</h1>
        </div>

        <!-- Navigation -->
        <nav class="p-4 space-y-1">
          <!-- Dashboard -->
          <a
            routerLink="/dashboard"
            routerLinkActive="bg-primary-50 text-primary-600"
            class="flex items-center px-4 py-3 text-gray-700 rounded-lg hover:bg-gray-100 transition-colors"
          >
            <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
            </svg>
            Tableau de bord
          </a>

          <!-- Invoices -->
          <a
            routerLink="/invoices"
            routerLinkActive="bg-primary-50 text-primary-600"
            class="flex items-center px-4 py-3 text-gray-700 rounded-lg hover:bg-gray-100 transition-colors"
          >
            <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
            Factures
          </a>

          <!-- Customers -->
          <a
            routerLink="/customers"
            routerLinkActive="bg-primary-50 text-primary-600"
            class="flex items-center px-4 py-3 text-gray-700 rounded-lg hover:bg-gray-100 transition-colors"
          >
            <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
            </svg>
            Clients
          </a>

          <!-- Admin Section -->
          @if (authService.isAdmin()) {
            <div class="pt-4 mt-4 border-t border-gray-200">
              <p class="px-4 text-xs font-semibold text-gray-400 uppercase tracking-wider mb-2">Administration</p>

              <a
                routerLink="/admin/company"
                routerLinkActive="bg-primary-50 text-primary-600"
                class="flex items-center px-4 py-3 text-gray-700 rounded-lg hover:bg-gray-100 transition-colors"
              >
                <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
                </svg>
                Entreprise
              </a>

              <a
                routerLink="/admin/articles"
                routerLinkActive="bg-primary-50 text-primary-600"
                class="flex items-center px-4 py-3 text-gray-700 rounded-lg hover:bg-gray-100 transition-colors"
              >
                <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z" />
                </svg>
                Articles
              </a>

              <a
                routerLink="/admin/services"
                routerLinkActive="bg-primary-50 text-primary-600"
                class="flex items-center px-4 py-3 text-gray-700 rounded-lg hover:bg-gray-100 transition-colors"
              >
                <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 13.255A23.931 23.931 0 0112 15c-3.183 0-6.22-.62-9-1.745M16 6V4a2 2 0 00-2-2h-4a2 2 0 00-2 2v2m4 6h.01M5 20h14a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                </svg>
                Services
              </a>

              <a
                routerLink="/admin/pricing"
                routerLinkActive="bg-primary-50 text-primary-600"
                class="flex items-center px-4 py-3 text-gray-700 rounded-lg hover:bg-gray-100 transition-colors"
              >
                <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                Tarification
              </a>

              <a
                routerLink="/admin/users"
                routerLinkActive="bg-primary-50 text-primary-600"
                class="flex items-center px-4 py-3 text-gray-700 rounded-lg hover:bg-gray-100 transition-colors"
              >
                <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
                </svg>
                Utilisateurs
              </a>

              <a
                routerLink="/admin/reports"
                routerLinkActive="bg-primary-50 text-primary-600"
                class="flex items-center px-4 py-3 text-gray-700 rounded-lg hover:bg-gray-100 transition-colors"
              >
                <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 17v-2m3 2v-4m3 4v-6m2 10H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                </svg>
                Rapports
              </a>
            </div>
          }
        </nav>
      </aside>

      <!-- Overlay for mobile -->
      @if (sidebarOpen()) {
        <div
          class="fixed inset-0 bg-black bg-opacity-50 z-40 lg:hidden"
          (click)="toggleSidebar()"
        ></div>
      }

      <!-- Main Content -->
      <div class="flex-1 flex flex-col min-h-screen">
        <!-- Header -->
        <header class="h-16 bg-white shadow-sm flex items-center justify-between px-4 lg:px-6">
          <!-- Mobile menu button -->
          <button
            (click)="toggleSidebar()"
            class="lg:hidden p-2 rounded-lg hover:bg-gray-100"
          >
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
            </svg>
          </button>

          <div class="hidden lg:block"></div>

          <!-- User menu -->
          <div class="flex items-center space-x-4">
            <div class="text-right hidden sm:block">
              <p class="text-sm font-medium text-gray-700">{{ authService.currentUser()?.fullName }}</p>
              <p class="text-xs text-gray-500">{{ authService.currentUser()?.role }}</p>
            </div>
            <button
              (click)="logout()"
              class="p-2 rounded-lg hover:bg-gray-100 text-gray-600 hover:text-red-600 transition-colors"
              title="Déconnexion"
            >
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
              </svg>
            </button>
          </div>
        </header>

        <!-- Page Content -->
        <main class="flex-1 p-4 lg:p-6">
          <router-outlet />
        </main>
      </div>
    </div>
  `
})
export class LayoutComponent {
  sidebarOpen = signal(false);

  constructor(public authService: AuthService) {}

  toggleSidebar(): void {
    this.sidebarOpen.update(v => !v);
  }

  logout(): void {
    this.authService.logout();
  }
}
