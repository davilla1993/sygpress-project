import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CustomerService } from '../../../core/services/customer.service';
import { ToastService } from '../../../shared/services/toast.service';

@Component({
  selector: 'app-customer-form',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule],
  template: `
    <div>
      <!-- Header -->
      <div class="mb-6">
        <h1 class="text-2xl font-bold text-gray-800">{{ isEditMode() ? 'Modifier le client' : 'Nouveau client' }}</h1>
        <p class="text-gray-600">{{ isEditMode() ? 'Modifiez les informations du client' : 'Créez un nouveau client' }}</p>
      </div>

      <!-- Form -->
      <div class="card p-6">
        <form [formGroup]="form" (ngSubmit)="onSubmit()">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <!-- Name -->
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Nom *</label>
              <input
                type="text"
                formControlName="name"
                class="input"
                [class.border-red-500]="form.get('name')?.invalid && form.get('name')?.touched"
              />
              @if (form.get('name')?.invalid && form.get('name')?.touched) {
                <p class="text-red-500 text-sm mt-1">Le nom est requis</p>
              }
            </div>

            <!-- Phone -->
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Téléphone *</label>
              <input
                type="tel"
                formControlName="phoneNumber"
                class="input"
                [class.border-red-500]="form.get('phoneNumber')?.invalid && form.get('phoneNumber')?.touched"
              />
              @if (form.get('phoneNumber')?.invalid && form.get('phoneNumber')?.touched) {
                <p class="text-red-500 text-sm mt-1">Le téléphone est requis</p>
              }
            </div>

            <!-- Address -->
            <div class="md:col-span-2">
              <label class="block text-sm font-medium text-gray-700 mb-2">Adresse</label>
              <textarea
                formControlName="address"
                rows="3"
                class="input"
              ></textarea>
            </div>
          </div>

          <!-- Actions -->
          <div class="flex justify-end gap-4 mt-6 pt-6 border-t">
            <a routerLink="/customers" class="btn-secondary">Annuler</a>
            <button
              type="submit"
              [disabled]="form.invalid || isSubmitting()"
              class="btn-primary"
              [class.opacity-50]="form.invalid || isSubmitting()"
            >
              @if (isSubmitting()) {
                <div class="animate-spin rounded-full h-5 w-5 border-b-2 border-white mr-2"></div>
              }
              {{ isEditMode() ? 'Modifier' : 'Créer' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  `
})
export class CustomerFormComponent implements OnInit {
  form: FormGroup;
  isEditMode = signal(false);
  isSubmitting = signal(false);
  private customerId: string | null = null;

  constructor(
    private fb: FormBuilder,
    private customerService: CustomerService,
    private toastService: ToastService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.form = this.fb.group({
      name: ['', Validators.required],
      phoneNumber: ['', Validators.required],
      address: ['']
    });
  }

  ngOnInit(): void {
    this.customerId = this.route.snapshot.paramMap.get('id');
    if (this.customerId) {
      this.isEditMode.set(true);
      this.loadCustomer();
    }
  }

  loadCustomer(): void {
    if (this.customerId) {
      this.customerService.getCustomer(this.customerId).subscribe({
        next: (customer) => {
          this.form.patchValue({
            name: customer.name,
            phoneNumber: customer.phoneNumber,
            address: customer.address
          });
        },
        error: (error) => {
          const message = error.error?.message || 'Erreur lors du chargement du client';
          this.toastService.error(message);
          this.router.navigate(['/customers']);
        }
      });
    }
  }

  onSubmit(): void {
    if (this.form.invalid) return;

    this.isSubmitting.set(true);
    const data = this.form.value;

    const request$ = this.isEditMode()
      ? this.customerService.updateCustomer(this.customerId!, data)
      : this.customerService.createCustomer(data);

    request$.subscribe({
      next: () => {
        this.toastService.success(this.isEditMode() ? 'Client modifié avec succès' : 'Client créé avec succès');
        this.router.navigate(['/customers']);
      },
      error: (error) => {
        const message = error.error?.message || 'Erreur lors de l\'enregistrement';
        this.toastService.error(message);
        this.isSubmitting.set(false);
      }
    });
  }
}
