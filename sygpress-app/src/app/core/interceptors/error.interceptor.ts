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

      // 401 Unauthorized ou 403 Forbidden : token expiré/invalide → déconnexion
      if ((error.status === 401 || error.status === 403) && !isLoginRequest) {
        // Déconnecter l'utilisateur et rediriger vers login
        authService.logout();
      }

      const errorMessage = error.error?.message || error.message || 'Une erreur est survenue';
      console.error('HTTP Error:', errorMessage);

      return throwError(() => error);
    })
  );
};
