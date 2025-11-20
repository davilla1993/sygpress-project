import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartData, Chart, ArcElement, BarElement, CategoryScale, LinearScale, DoughnutController, BarController, Legend, Tooltip } from 'chart.js';
import { AuthService } from '../../core/services/auth.service';
import { DashboardService } from '../../core/services/dashboard.service';
import { AdminDashboard, UserDashboard } from '../../core/models';
import { ToastService } from '../../shared/services/toast.service';

// Register Chart.js components
Chart.register(
  ArcElement,
  BarElement,
  CategoryScale,
  LinearScale,
  DoughnutController,
  BarController,
  Legend,
  Tooltip
);

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, BaseChartDirective],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
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
