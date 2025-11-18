import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      // Ne pas déconnecter si c'est une erreur sur la route de login
      const isLoginRequest = req.url.includes('/auth/login');

      if (error.status === 401 && !isLoginRequest) {
        // Déconnecter uniquement si ce n'est pas une tentative de connexion
        authService.logout();
        router.navigate(['/login']);
      } else if (error.status === 403) {
        router.navigate(['/unauthorized']);
      }

      const errorMessage = error.error?.message || error.message || 'Une erreur est survenue';
      console.error('HTTP Error:', errorMessage);

      return throwError(() => error);
    })
  );
};
