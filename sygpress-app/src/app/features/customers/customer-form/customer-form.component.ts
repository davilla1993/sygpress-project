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
  templateUrl: './customer-form.component.html',
  styleUrls: ['./customer-form.component.css']
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
