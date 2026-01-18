import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { ToastService } from '../../../shared/services/toast.service';

interface SalesReport {
  totalInvoices: number;
  totalRevenue: number;
  totalPaid: number;
  totalUnpaid: number;
  averageInvoiceAmount: number;
}

interface CustomerReport {
  totalCustomers: number;
  newCustomers: number;
  topCustomers: {
    customerName: string;
    customerPhone: string;
    invoiceCount: number;
    totalSpent: number;
    totalUnpaid: number;
  }[];
}

interface ServiceReport {
  startDate: string;
  endDate: string;
  totalServices: number;
  totalRevenue: number;
  serviceStats: {
    serviceName: string;
    quantity: number;
    amount: number;
    percentage: number;
  }[];
  articleStats: {
    articleName: string;
    quantity: number;
    amount: number;
    percentage: number;
  }[];
  combinationStats: {
    serviceName: string;
    articleName: string;
    quantity: number;
    amount: number;
  }[];
}

interface UserReport {
  startDate: string;
  endDate: string;
  totalUsers: number;
  totalRevenue: number;
  userStats: {
    userId: number;
    userName: string;
    userEmail: string;
    userRole: string;
    invoiceCount: number;
    totalRevenue: number;
    totalPaid: number;
    totalUnpaid: number;
    averageInvoiceAmount: number;
    percentage: number;
  }[];
}

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reports.component.html',
  styleUrls: ['./reports.component.css']
})
export class ReportsComponent {
  activeReport: 'sales' | 'customers' | 'services' | 'users' = 'sales';
  startDate = this.getFirstDayOfMonth();
  endDate = this.getToday();
  isLoading = signal(false);
  salesReport = signal<SalesReport | null>(null);
  customerReport = signal<CustomerReport | null>(null);
  serviceReport = signal<ServiceReport | null>(null);
  userReport = signal<UserReport | null>(null);

  constructor(
    private http: HttpClient,
    private toastService: ToastService
  ) {
    this.loadSalesReport();
  }

  loadSalesReport(): void {
    this.isLoading.set(true);
    const params = this.getDateParams();
    this.http.get<SalesReport>(`${environment.apiUrl}/reports/sales`, { params }).subscribe({
      next: (data) => {
        this.salesReport.set(data);
        this.isLoading.set(false);
      },
      error: (error) => {
        const message = error.error?.message || 'Erreur lors du chargement du rapport des ventes';
        this.toastService.error(message);
        this.isLoading.set(false);
      }
    });
  }

  loadCustomerReport(): void {
    this.isLoading.set(true);
    const params = this.getDateParams();
    this.http.get<CustomerReport>(`${environment.apiUrl}/reports/customers`, { params }).subscribe({
      next: (data) => {
        this.customerReport.set(data);
        this.isLoading.set(false);
      },
      error: (error) => {
        const message = error.error?.message || 'Erreur lors du chargement du rapport clients';
        this.toastService.error(message);
        this.isLoading.set(false);
      }
    });
  }

  loadServiceReport(): void {
    this.isLoading.set(true);
    const params = this.getDateParams();
    this.http.get<ServiceReport>(`${environment.apiUrl}/reports/services`, { params }).subscribe({
      next: (data) => {
        this.serviceReport.set(data);
        this.isLoading.set(false);
      },
      error: (error) => {
        const message = error.error?.message || 'Erreur lors du chargement du rapport des services';
        this.toastService.error(message);
        this.isLoading.set(false);
      }
    });
  }

  loadUserReport(): void {
    this.isLoading.set(true);
    const params = this.getDateParams();
    this.http.get<UserReport>(`${environment.apiUrl}/reports/users`, { params }).subscribe({
      next: (data) => {
        this.userReport.set(data);
        this.isLoading.set(false);
      },
      error: (error) => {
        const message = error.error?.message || 'Erreur lors du chargement du rapport utilisateurs';
        this.toastService.error(message);
        this.isLoading.set(false);
      }
    });
  }

  printReport(type: string): void {
    const params = this.getDateParams();
    this.http.get(`${environment.apiUrl}/reports/${type}/pdf`, { params, responseType: 'blob' }).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.target = '_blank';
        link.click();
        setTimeout(() => window.URL.revokeObjectURL(url), 100);
      },
      error: (error) => {
        const message = error.error?.message || 'Erreur lors de l\'impression du rapport';
        this.toastService.error(message);
      }
    });
  }

  onDateChange(): void {
    if (this.activeReport === 'sales') {
      this.loadSalesReport();
    } else if (this.activeReport === 'customers') {
      this.loadCustomerReport();
    } else if (this.activeReport === 'services') {
      this.loadServiceReport();
    } else if (this.activeReport === 'users') {
      this.loadUserReport();
    }
  }

  setToday(): void {
    this.startDate = this.getToday();
    this.endDate = this.getToday();
    this.onDateChange();
  }

  setThisMonth(): void {
    this.startDate = this.getFirstDayOfMonth();
    this.endDate = this.getToday();
    this.onDateChange();
  }

  private getDateParams(): HttpParams {
    return new HttpParams()
      .set('startDate', this.startDate)
      .set('endDate', this.endDate);
  }

  private getToday(): string {
    return new Date().toISOString().split('T')[0];
  }

  private getFirstDayOfMonth(): string {
    const date = new Date();
    return new Date(date.getFullYear(), date.getMonth(), 1).toISOString().split('T')[0];
  }

  formatMoney(amount: number): string {
    if (amount == null) return '0 FCFA';
    return new Intl.NumberFormat('fr-FR').format(amount) + ' FCFA';
  }

  getFormattedPeriod(): string {
    const start = new Date(this.startDate);
    const end = new Date(this.endDate);
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    // Vérifier si c'est aujourd'hui
    if (this.startDate === this.endDate && start.getTime() === today.getTime()) {
      return 'Aujourd\'hui';
    }

    // Vérifier si c'est le mois en cours
    const firstDayOfMonth = new Date(today.getFullYear(), today.getMonth(), 1);
    if (this.startDate === firstDayOfMonth.toISOString().split('T')[0] &&
      this.endDate === this.getToday()) {
      const monthNames = ['Janvier', 'Février', 'Mars', 'Avril', 'Mai', 'Juin',
        'Juillet', 'Août', 'Septembre', 'Octobre', 'Novembre', 'Décembre'];
      return `${monthNames[today.getMonth()]} ${today.getFullYear()}`;
    }

    // Sinon afficher la période
    return `Du ${start.toLocaleDateString('fr-FR')} au ${end.toLocaleDateString('fr-FR')}`;
  }
}
