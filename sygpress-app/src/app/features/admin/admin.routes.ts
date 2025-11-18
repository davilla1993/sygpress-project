import { Routes } from '@angular/router';

export const ADMIN_ROUTES: Routes = [
  {
    path: '',
    redirectTo: 'company',
    pathMatch: 'full'
  },
  {
    path: 'company',
    loadComponent: () => import('./company/company.component').then(m => m.CompanyComponent)
  },
  {
    path: 'users',
    loadComponent: () => import('./users/user-list/user-list.component').then(m => m.UserListComponent)
  },
  {
    path: 'articles',
    loadComponent: () => import('./articles/article-list/article-list.component').then(m => m.ArticleListComponent)
  },
  {
    path: 'services',
    loadComponent: () => import('./services/service-list/service-list.component').then(m => m.ServiceListComponent)
  },
  {
    path: 'pricing',
    loadComponent: () => import('./pricing/pricing-list/pricing-list.component').then(m => m.PricingListComponent)
  },
  {
    path: 'reports',
    loadComponent: () => import('./reports/reports.component').then(m => m.ReportsComponent)
  }
];
