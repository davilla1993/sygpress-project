import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-unauthorized',
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="min-h-screen flex items-center justify-center bg-gray-50">
      <div class="text-center">
        <h1 class="text-6xl font-bold text-red-600">403</h1>
        <h2 class="text-2xl font-semibold text-gray-800 mt-4">Accès refusé</h2>
        <p class="text-gray-600 mt-2">Vous n'avez pas les permissions nécessaires pour accéder à cette page.</p>
        <a routerLink="/dashboard" class="btn-primary mt-6 inline-block">
          Retour au tableau de bord
        </a>
      </div>
    </div>
  `
})
export class UnauthorizedComponent {}
