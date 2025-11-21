import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
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
      this.router.navigate(['/app/dashboard']);
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
      next: (response) => {
        // Vérifier si l'utilisateur doit changer son mot de passe
        if (response.mustChangePassword) {
          this.router.navigate(['/change-password']);
        } else {
          const returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/app/dashboard';
          this.router.navigateByUrl(returnUrl);
        }
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
          this.errorMessage.set('Acces refuse');
        } else if (error.status === 0) {
          this.errorMessage.set('Impossible de contacter le serveur. Verifiez votre connexion.');
        } else {
          this.errorMessage.set('Une erreur est survenue. Veuillez reessayer.');
        }
      }
    });
  }
}
