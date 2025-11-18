import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-600 to-slate-800 p-4">
      <div class="w-full max-w-md">
        <!-- Logo/Brand -->
        <div class="text-center mb-8">
          <h1 class="text-4xl font-bold text-white">SygPress</h1>
          <p class="text-slate-200 mt-2">Gestion de Pressing</p>
        </div>

        <!-- Login Card -->
        <div class="bg-white rounded-xl shadow-2xl p-8">
          <h2 class="text-2xl font-semibold text-gray-800 text-center mb-6">Connexion</h2>

          @if (errorMessage()) {
            <div class="mb-4 p-3 bg-red-50 border border-red-200 text-red-700 rounded-lg text-sm">
              {{ errorMessage() }}
            </div>
          }

          <form [formGroup]="loginForm" (ngSubmit)="onSubmit()">
            <!-- Username -->
            <div class="mb-4">
              <label for="username" class="form-label">Nom d'utilisateur</label>
              <input
                type="text"
                id="username"
                formControlName="username"
                class="form-input"
                [class.border-red-500]="isFieldInvalid('username')"
                placeholder="Entrez votre nom d'utilisateur"
              />
              @if (isFieldInvalid('username')) {
                <p class="form-error">Le nom d'utilisateur est requis</p>
              }
            </div>

            <!-- Password -->
            <div class="mb-6">
              <label for="password" class="form-label">Mot de passe</label>
              <div class="relative">
                <input
                  [type]="showPassword() ? 'text' : 'password'"
                  id="password"
                  formControlName="password"
                  class="form-input pr-10"
                  [class.border-red-500]="isFieldInvalid('password')"
                  placeholder="Entrez votre mot de passe"
                />
                <button
                  type="button"
                  (click)="togglePassword()"
                  class="absolute right-3 top-1/2 -translate-y-1/2 text-gray-500 hover:text-gray-700"
                >
                  @if (showPassword()) {
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21" />
                    </svg>
                  } @else {
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                    </svg>
                  }
                </button>
              </div>
              @if (isFieldInvalid('password')) {
                <p class="form-error">Le mot de passe est requis</p>
              }
            </div>

            <!-- Submit Button -->
            <button
              type="submit"
              [disabled]="isLoading()"
              class="w-full btn-primary py-3 flex items-center justify-center"
              [class.opacity-70]="isLoading()"
            >
              @if (isLoading()) {
                <svg class="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                Connexion en cours...
              } @else {
                Se connecter
              }
            </button>
          </form>
        </div>

        <!-- Footer -->
        <p class="text-center text-slate-300 text-sm mt-6">
          © 2024 SygPress. Tous droits réservés.
        </p>
      </div>
    </div>
  `
})
export class LoginComponent {
  loginForm: FormGroup;
  isLoading = signal(false);
  showPassword = signal(false);
  errorMessage = signal('');

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.loginForm = this.fb.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });

    // Redirect if already logged in
    if (this.authService.isAuthenticated()) {
      this.router.navigate(['/dashboard']);
    }
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.loginForm.get(fieldName);
    return !!(field && field.invalid && field.touched);
  }

  togglePassword(): void {
    this.showPassword.update(v => !v);
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      Object.keys(this.loginForm.controls).forEach(key => {
        this.loginForm.get(key)?.markAsTouched();
      });
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set('');

    this.authService.login(this.loginForm.value).subscribe({
      next: () => {
        const returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/dashboard';
        this.router.navigateByUrl(returnUrl);
      },
      error: (error) => {
        this.isLoading.set(false);
        // Utiliser le message du backend s'il existe
        const backendMessage = error.error?.message;
        if (backendMessage) {
          this.errorMessage.set(backendMessage);
        } else if (error.status === 401) {
          this.errorMessage.set('Nom d\'utilisateur ou mot de passe incorrect');
        } else if (error.status === 403) {
          this.errorMessage.set('Accès refusé');
        } else if (error.status === 0) {
          this.errorMessage.set('Impossible de contacter le serveur. Vérifiez votre connexion.');
        } else {
          this.errorMessage.set('Une erreur est survenue. Veuillez réessayer.');
        }
      }
    });
  }
}
