import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartData } from 'chart.js';
import { AuthService } from '../../core/services/auth.service';
import { DashboardService } from '../../core/services/dashboard.service';
import { AdminDashboard, UserDashboard } from '../../core/models';
import { ToastService } from '../../shared/services/toast.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, BaseChartDirective],
  template: `
    <div>
      <!-- Page Title -->
      <div class="mb-6">
        <h1 class="text-2xl font-bold text-gray-800">Tableau de bord</h1>
        <p class="text-gray-600">Bienvenue, {{ authService.currentUser()?.fullName }}</p>
      </div>

      @if (isLoading()) {
        <div class="flex items-center justify-center py-12">
          <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
        </div>
      } @else if (authService.isAdmin() && adminDashboard()) {
        <!-- Admin Dashboard -->
        <div>
          <!-- KPI Cards -->
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
            <!-- Total Revenue -->
            <div class="card p-6">
              <div class="flex items-center justify-between">
                <div>
                  <p class="text-sm text-gray-500">Chiffre d'affaires</p>
                  <p class="text-2xl font-bold text-gray-800">{{ formatMoney(adminDashboard()!.totalRevenue) }}</p>
                </div>
                <div class="p-3 bg-green-100 rounded-full">
                  <svg class="w-6 h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
              </div>
              <p class="text-sm text-gray-500 mt-2">
                Taux de paiement: <span class="font-medium text-green-600">{{ adminDashboard()!.paymentRate.toFixed(1) }}%</span>
              </p>
            </div>

            <!-- Total Invoices -->
            <div class="card p-6">
              <div class="flex items-center justify-between">
                <div>
                  <p class="text-sm text-gray-500">Total Factures</p>
                  <p class="text-2xl font-bold text-gray-800">{{ adminDashboard()!.totalInvoices }}</p>
                </div>
                <div class="p-3 bg-blue-100 rounded-full">
                  <svg class="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                  </svg>
                </div>
              </div>
              <p class="text-sm text-gray-500 mt-2">
                Aujourd'hui: <span class="font-medium">{{ adminDashboard()!.todayInvoices }}</span>
              </p>
            </div>

            <!-- Total Customers -->
            <div class="card p-6">
              <div class="flex items-center justify-between">
                <div>
                  <p class="text-sm text-gray-500">Total Clients</p>
                  <p class="text-2xl font-bold text-gray-800">{{ adminDashboard()!.totalCustomers }}</p>
                </div>
                <div class="p-3 bg-purple-100 rounded-full">
                  <svg class="w-6 h-6 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
                  </svg>
                </div>
              </div>
              <p class="text-sm text-gray-500 mt-2">
                Nouveaux ce mois: <span class="font-medium">{{ adminDashboard()!.monthNewCustomers }}</span>
              </p>
            </div>

            <!-- Unpaid Amount -->
            <div class="card p-6">
              <div class="flex items-center justify-between">
                <div>
                  <p class="text-sm text-gray-500">Impayés</p>
                  <p class="text-2xl font-bold text-red-600">{{ formatMoney(adminDashboard()!.totalUnpaid) }}</p>
                </div>
                <div class="p-3 bg-red-100 rounded-full">
                  <svg class="w-6 h-6 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
              </div>
              <p class="text-sm text-gray-500 mt-2">
                Payés: <span class="font-medium text-green-600">{{ formatMoney(adminDashboard()!.totalPaid) }}</span>
              </p>
            </div>
          </div>

          <!-- Charts Row -->
          <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
            <!-- Payment Distribution Chart -->
            <div class="card p-6">
              <h3 class="text-lg font-semibold text-gray-800 mb-4">Répartition des paiements</h3>
              <div class="h-64 flex items-center justify-center">
                <canvas baseChart
                  [data]="paymentChartData()"
                  [options]="doughnutChartOptions"
                  type="doughnut">
                </canvas>
              </div>
            </div>

            <!-- Revenue Comparison Chart -->
            <div class="card p-6">
              <h3 class="text-lg font-semibold text-gray-800 mb-4">Comparaison des revenus</h3>
              <div class="h-64">
                <canvas baseChart
                  [data]="revenueChartData()"
                  [options]="barChartOptions"
                  type="bar">
                </canvas>
              </div>
            </div>
          </div>

          <!-- Today & Month Stats -->
          <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
            <!-- Today's Stats -->
            <div class="card p-6">
              <h3 class="text-lg font-semibold text-gray-800 mb-4">Statistiques du jour</h3>
              <div class="space-y-3">
                <div class="flex justify-between items-center">
                  <span class="text-gray-600">Factures</span>
                  <span class="font-medium">{{ adminDashboard()!.todayInvoices }}</span>
                </div>
                <div class="flex justify-between items-center">
                  <span class="text-gray-600">Revenus</span>
                  <span class="font-medium">{{ formatMoney(adminDashboard()!.todayRevenue) }}</span>
                </div>
                <div class="flex justify-between items-center">
                  <span class="text-gray-600">Paiements</span>
                  <span class="font-medium text-green-600">{{ formatMoney(adminDashboard()!.todayPayments) }}</span>
                </div>
                <div class="flex justify-between items-center">
                  <span class="text-gray-600">Nouveaux clients</span>
                  <span class="font-medium">{{ adminDashboard()!.todayNewCustomers }}</span>
                </div>
              </div>
            </div>

            <!-- Month Stats -->
            <div class="card p-6">
              <h3 class="text-lg font-semibold text-gray-800 mb-4">Statistiques du mois</h3>
              <div class="space-y-3">
                <div class="flex justify-between items-center">
                  <span class="text-gray-600">Factures</span>
                  <span class="font-medium">{{ adminDashboard()!.monthInvoices }}</span>
                </div>
                <div class="flex justify-between items-center">
                  <span class="text-gray-600">Revenus</span>
                  <span class="font-medium">{{ formatMoney(adminDashboard()!.monthRevenue) }}</span>
                </div>
                <div class="flex justify-between items-center">
                  <span class="text-gray-600">Paiements</span>
                  <span class="font-medium text-green-600">{{ formatMoney(adminDashboard()!.monthPayments) }}</span>
                </div>
                <div class="flex justify-between items-center">
                  <span class="text-gray-600">Nouveaux clients</span>
                  <span class="font-medium">{{ adminDashboard()!.monthNewCustomers }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Top Customers & Recent Invoices -->
          <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <!-- Top Customers -->
            <div class="card p-6">
              <h3 class="text-lg font-semibold text-gray-800 mb-4">Top 5 Clients</h3>
              <div class="space-y-3">
                @for (customer of adminDashboard()!.topCustomers; track customer.name) {
                  <div class="flex justify-between items-center py-2 border-b border-gray-100 last:border-0">
                    <div>
                      <p class="font-medium text-gray-800">{{ customer.name }}</p>
                      <p class="text-sm text-gray-500">{{ customer.invoiceCount }} factures</p>
                    </div>
                    <span class="font-semibold text-primary-600">{{ formatMoney(customer.totalSpent) }}</span>
                  </div>
                }
              </div>
            </div>

            <!-- Recent Invoices -->
            <div class="card p-6">
              <h3 class="text-lg font-semibold text-gray-800 mb-4">Factures récentes</h3>
              <div class="space-y-3">
                @for (invoice of adminDashboard()!.recentInvoices.slice(0, 5); track invoice.invoiceNumber) {
                  <div class="flex justify-between items-center py-2 border-b border-gray-100 last:border-0">
                    <div>
                      <p class="font-medium text-gray-800">{{ invoice.invoiceNumber }}</p>
                      <p class="text-sm text-gray-500">{{ invoice.customerName }}</p>
                    </div>
                    <div class="text-right">
                      <p class="font-medium">{{ formatMoney(invoice.amount) }}</p>
                      <span
                        class="text-xs px-2 py-1 rounded-full"
                        [class.bg-green-100]="invoice.paid"
                        [class.text-green-700]="invoice.paid"
                        [class.bg-red-100]="!invoice.paid"
                        [class.text-red-700]="!invoice.paid"
                      >
                        {{ invoice.paid ? 'Payé' : 'Impayé' }}
                      </span>
                    </div>
                  </div>
                }
              </div>
            </div>
          </div>
        </div>
      } @else if (userDashboard()) {
        <!-- User Dashboard -->
        <div>
          <!-- Today Stats -->
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
            <div class="card p-6">
              <p class="text-sm text-gray-500">Factures du jour</p>
              <p class="text-2xl font-bold text-gray-800">{{ userDashboard()!.todayInvoices }}</p>
            </div>
            <div class="card p-6">
              <p class="text-sm text-gray-500">Revenus du jour</p>
              <p class="text-2xl font-bold text-gray-800">{{ formatMoney(userDashboard()!.todayRevenue) }}</p>
            </div>
            <div class="card p-6">
              <p class="text-sm text-gray-500">Paiements du jour</p>
              <p class="text-2xl font-bold text-green-600">{{ formatMoney(userDashboard()!.todayPayments) }}</p>
            </div>
            <div class="card p-6">
              <p class="text-sm text-gray-500">Livraisons du jour</p>
              <p class="text-2xl font-bold text-gray-800">{{ userDashboard()!.todayDeliveries }}</p>
            </div>
          </div>

          <!-- Alerts -->
          @if (userDashboard()!.alerts.length > 0) {
            <div class="mb-6 space-y-3">
              @for (alert of userDashboard()!.alerts; track alert.message) {
                <div
                  class="p-4 rounded-lg flex items-center"
                  [class.bg-yellow-50]="alert.type === 'WARNING'"
                  [class.border-yellow-200]="alert.type === 'WARNING'"
                  [class.bg-red-50]="alert.type === 'DANGER'"
                  [class.border-red-200]="alert.type === 'DANGER'"
                  [class.bg-blue-50]="alert.type === 'INFO'"
                  [class.border-blue-200]="alert.type === 'INFO'"
                  [class.border]="true"
                >
                  <svg
                    class="w-5 h-5 mr-3"
                    [class.text-yellow-600]="alert.type === 'WARNING'"
                    [class.text-red-600]="alert.type === 'DANGER'"
                    [class.text-blue-600]="alert.type === 'INFO'"
                    fill="none" stroke="currentColor" viewBox="0 0 24 24"
                  >
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                  </svg>
                  <span
                    [class.text-yellow-800]="alert.type === 'WARNING'"
                    [class.text-red-800]="alert.type === 'DANGER'"
                    [class.text-blue-800]="alert.type === 'INFO'"
                  >
                    {{ alert.message }}
                  </span>
                </div>
              }
            </div>
          }

          <!-- Processing Queues -->
          <div class="card p-6 mb-6">
            <h3 class="text-lg font-semibold text-gray-800 mb-4">État des traitements</h3>
            <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
              @for (queue of userDashboard()!.processingQueues; track queue.status) {
                <div class="text-center p-3 bg-gray-50 rounded-lg">
                  <p class="text-2xl font-bold text-primary-600">{{ queue.count }}</p>
                  <p class="text-sm text-gray-600">{{ queue.statusLabel }}</p>
                </div>
              }
            </div>
          </div>

          <!-- Deliveries Today & Pending Payments -->
          <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <!-- Deliveries Today -->
            <div class="card p-6">
              <h3 class="text-lg font-semibold text-gray-800 mb-4">Livraisons du jour</h3>
              @if (userDashboard()!.deliveriesToday.length === 0) {
                <p class="text-gray-500 text-center py-4">Aucune livraison prévue</p>
              } @else {
                <div class="space-y-3 max-h-64 overflow-y-auto">
                  @for (delivery of userDashboard()!.deliveriesToday; track delivery.invoiceNumber) {
                    <div class="flex justify-between items-center py-2 border-b border-gray-100 last:border-0">
                      <div>
                        <p class="font-medium text-gray-800">{{ delivery.invoiceNumber }}</p>
                        <p class="text-sm text-gray-500">{{ delivery.customerName }}</p>
                      </div>
                      <div class="text-right">
                        <p class="font-medium">{{ formatMoney(delivery.amount) }}</p>
                        <span
                          class="text-xs px-2 py-1 rounded-full"
                          [class.bg-green-100]="delivery.ready"
                          [class.text-green-700]="delivery.ready"
                          [class.bg-yellow-100]="!delivery.ready"
                          [class.text-yellow-700]="!delivery.ready"
                        >
                          {{ delivery.ready ? 'Prêt' : delivery.processingStatus }}
                        </span>
                      </div>
                    </div>
                  }
                </div>
              }
            </div>

            <!-- Pending Payments -->
            <div class="card p-6">
              <h3 class="text-lg font-semibold text-gray-800 mb-4">Paiements en attente</h3>
              @if (userDashboard()!.pendingPayments.length === 0) {
                <p class="text-gray-500 text-center py-4">Aucun paiement en attente</p>
              } @else {
                <div class="space-y-3 max-h-64 overflow-y-auto">
                  @for (payment of userDashboard()!.pendingPayments.slice(0, 5); track payment.invoiceNumber) {
                    <div class="flex justify-between items-center py-2 border-b border-gray-100 last:border-0">
                      <div>
                        <p class="font-medium text-gray-800">{{ payment.invoiceNumber }}</p>
                        <p class="text-sm text-gray-500">{{ payment.customerName }}</p>
                      </div>
                      <div class="text-right">
                        <p class="font-medium text-red-600">{{ formatMoney(payment.remainingAmount) }}</p>
                        @if (payment.daysOverdue > 0) {
                          <span class="text-xs text-red-500">{{ payment.daysOverdue }} jour(s) de retard</span>
                        }
                      </div>
                    </div>
                  }
                </div>
              }
            </div>
          </div>
        </div>
      }
    </div>
  `
})
export class DashboardComponent implements OnInit {
  isLoading = signal(true);
  adminDashboard = signal<AdminDashboard | null>(null);
  userDashboard = signal<UserDashboard | null>(null);

  // Chart options
  doughnutChartOptions: ChartConfiguration<'doughnut'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'bottom'
      }
    }
  };

  barChartOptions: ChartConfiguration<'bar'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: false
      }
    },
    scales: {
      y: {
        beginAtZero: true,
        ticks: {
          callback: (value) => this.formatShortMoney(value as number)
        }
      }
    }
  };

  // Computed chart data
  paymentChartData = computed<ChartData<'doughnut'>>(() => {
    const dashboard = this.adminDashboard();
    if (!dashboard) {
      return { labels: [], datasets: [] };
    }
    return {
      labels: ['Payé', 'Impayé'],
      datasets: [{
        data: [dashboard.totalPaid, dashboard.totalUnpaid],
        backgroundColor: ['#22c55e', '#ef4444'],
        hoverBackgroundColor: ['#16a34a', '#dc2626']
      }]
    };
  });

  revenueChartData = computed<ChartData<'bar'>>(() => {
    const dashboard = this.adminDashboard();
    if (!dashboard) {
      return { labels: [], datasets: [] };
    }
    return {
      labels: ['Aujourd\'hui', 'Ce mois'],
      datasets: [{
        data: [dashboard.todayRevenue, dashboard.monthRevenue],
        backgroundColor: ['#3b82f6', '#8b5cf6'],
        borderRadius: 8
      }]
    };
  });

  constructor(
    public authService: AuthService,
    private dashboardService: DashboardService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.loadDashboard();
  }

  loadDashboard(): void {
    if (this.authService.isAdmin()) {
      this.dashboardService.getAdminDashboard().subscribe({
        next: (data) => {
          this.adminDashboard.set(data);
          this.isLoading.set(false);
        },
        error: (error) => {
          const message = error.error?.message || 'Erreur lors du chargement du tableau de bord';
          this.toastService.error(message);
          this.isLoading.set(false);
        }
      });
    } else {
      this.dashboardService.getUserDashboard().subscribe({
        next: (data) => {
          this.userDashboard.set(data);
          this.isLoading.set(false);
        },
        error: (error) => {
          const message = error.error?.message || 'Erreur lors du chargement du tableau de bord';
          this.toastService.error(message);
          this.isLoading.set(false);
        }
      });
    }
  }

  formatMoney(amount: number): string {
    if (amount == null) return '0 FCFA';
    return new Intl.NumberFormat('fr-FR').format(amount) + ' FCFA';
  }

  formatShortMoney(amount: number): string {
    if (amount >= 1000000) {
      return (amount / 1000000).toFixed(1) + 'M';
    } else if (amount >= 1000) {
      return (amount / 1000).toFixed(0) + 'K';
    }
    return amount.toString();
  }
}
