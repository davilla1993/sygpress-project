import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CustomerService, PageResponse } from '../../../core/services/customer.service';
import { AuthService } from '../../../core/services/auth.service';
import { Customer } from '../../../core/models';
import { ToastService } from '../../../shared/services/toast.service';
import { ConfirmModalComponent } from '../../../shared/components/confirm-modal/confirm-modal.component';

@Component({
  selector: 'app-customer-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, ConfirmModalComponent],
  templateUrl: './customer-list.component.html',
  styleUrls: ['./customer-list.component.css']
})
export class CustomerListComponent implements OnInit {
  customers = signal<Customer[]>([]);
  isLoading = signal(true);
  searchTerm = '';
  currentPage = signal(0);
  totalPages = signal(0);
  totalElements = signal(0);
  showDeleteModal = false;
  customerToDelete: Customer | null = null;
  private searchTimeout: any;

  constructor(
    private customerService: CustomerService,
    private toastService: ToastService,
    private authService: AuthService
  ) {}

  get isAdmin(): boolean {
    return this.authService.isAdmin();
  }

  ngOnInit(): void {
    this.loadCustomers();
  }

  loadCustomers(): void {
    this.isLoading.set(true);
    this.customerService.getCustomers(this.currentPage(), 10, this.searchTerm || undefined).subscribe({
      next: (response) => {
        this.customers.set(response.content);
        this.totalPages.set(response.totalPages);
        this.totalElements.set(response.totalElements);
        this.isLoading.set(false);
      },
      error: (error) => {
        const message = error.error?.message || 'Erreur lors du chargement des clients';
        this.toastService.error(message);
        this.isLoading.set(false);
      }
    });
  }

  onSearch(): void {
    clearTimeout(this.searchTimeout);
    this.searchTimeout = setTimeout(() => {
      this.currentPage.set(0);
      this.loadCustomers();
    }, 300);
  }

  goToPage(page: number): void {
    this.currentPage.set(page);
    this.loadCustomers();
  }

  confirmDelete(customer: Customer): void {
    this.customerToDelete = customer;
    this.showDeleteModal = true;
  }

  deleteCustomer(): void {
    if (!this.customerToDelete) return;

    this.customerService.deleteCustomer(this.customerToDelete.publicId).subscribe({
      next: () => {
        this.toastService.success('Client supprimé avec succès');
        this.showDeleteModal = false;
        this.customerToDelete = null;
        this.loadCustomers();
      },
      error: (error) => {
        const message = error.error?.message || 'Erreur lors de la suppression';
        this.toastService.error(message);
        this.showDeleteModal = false;
      }
    });
  }

  formatDate(date: string): string {
    if (!date) return '-';
    const parsedDate = new Date(date);
    if (isNaN(parsedDate.getTime())) return '-';
    return parsedDate.toLocaleDateString('fr-FR');
  }
}
