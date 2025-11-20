import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isAuthenticated()) {
    router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
    return false;
  }

  // Vérifier si l'utilisateur doit changer son mot de passe
  if (authService.mustChangePassword() && state.url !== '/change-password') {
    router.navigate(['/change-password']);
    return false;
  }

  return true;
};
