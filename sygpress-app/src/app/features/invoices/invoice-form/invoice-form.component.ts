import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, FormArray, ReactiveFormsModule, Validators } from '@angular/forms';
import { InvoiceService } from '../../../core/services/invoice.service';
import { CustomerService } from '../../../core/services/customer.service';
import { Customer, Pricing } from '../../../core/models';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { ToastService } from '../../../shared/services/toast.service';

@Component({
  selector: 'app-invoice-form',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule],
  template: `
    <div>
      <!-- Header -->
      <div class="mb-6">
        <h1 class="text-2xl font-bold text-gray-800">{{ isEditMode() ? 'Modifier la facture' : 'Nouvelle facture' }}</h1>
        <p class="text-gray-600">{{ isEditMode() ? 'Modifiez les informations de la facture' : 'Créez une nouvelle facture' }}</p>
      </div>

      <!-- Form -->
      <div class="card p-6">
        <form [formGroup]="form" (ngSubmit)="onSubmit()">
          <!-- Customer & Dates -->
          <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Client *</label>
              <select formControlName="customerPublicId" class="input">
                <option value="">Sélectionner un client</option>
                @for (customer of customers(); track customer.publicId) {
                  <option [value]="customer.publicId">{{ customer.name }} - {{ customer.phoneNumber }}</option>
                }
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Date de dépôt *</label>
              <input type="date" formControlName="depositDate" class="input" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Date de livraison *</label>
              <input type="date" formControlName="deliveryDate" class="input" />
            </div>
          </div>

          <!-- Invoice Lines -->
          <div class="mb-6">
            <div class="flex justify-between items-center mb-4">
              <h3 class="text-lg font-semibold text-gray-800">Articles</h3>
              <button type="button" (click)="addLine()" class="btn-secondary text-sm">
                + Ajouter article
              </button>
            </div>

            <div formArrayName="invoiceLines" class="space-y-3">
              @for (line of invoiceLinesArray.controls; track $index; let i = $index) {
                <div [formGroupName]="i" class="flex gap-4 items-start p-4 bg-gray-50 rounded-lg">
                  <div class="flex-1">
                    <label class="block text-sm text-gray-500 mb-1">Tarif</label>
                    <select formControlName="pricingPublicId" class="input text-sm">
                      <option value="">Sélectionner</option>
                      @for (pricing of pricings(); track pricing.publicId) {
                        <option [value]="pricing.publicId">
                          {{ pricing.article.name }} - {{ pricing.service.name }} ({{ formatMoney(pricing.price) }})
                        </option>
                      }
                    </select>
                  </div>
                  <div class="w-24">
                    <label class="block text-sm text-gray-500 mb-1">Quantité</label>
                    <input type="number" formControlName="quantity" min="1" class="input text-sm" />
                  </div>
                  <button type="button" (click)="removeLine(i)" class="mt-6 text-red-600 hover:text-red-800">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                    </svg>
                  </button>
                </div>
              }
            </div>
          </div>

          <!-- Additional Options -->
          <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Remise (FCFA)</label>
              <input type="number" formControlName="discount" min="0" class="input" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Acompte (FCFA)</label>
              <input type="number" formControlName="amountPaid" min="0" class="input" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Taux TVA (%)</label>
              <input type="number" formControlName="vatRate" min="0" max="100" class="input" />
            </div>
          </div>

          <!-- Observations -->
          <div class="mb-6">
            <label class="block text-sm font-medium text-gray-700 mb-2">Observations</label>
            <textarea formControlName="observations" rows="3" class="input"></textarea>
          </div>

          <!-- Actions -->
          <div class="flex justify-end gap-4 pt-6 border-t">
            <a routerLink="/invoices" class="btn-secondary">Annuler</a>
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
export class InvoiceFormComponent implements OnInit {
  form: FormGroup;
  isEditMode = signal(false);
  isSubmitting = signal(false);
  customers = signal<Customer[]>([]);
  pricings = signal<Pricing[]>([]);
  private invoiceId: string | null = null;

  constructor(
    private fb: FormBuilder,
    private invoiceService: InvoiceService,
    private customerService: CustomerService,
    private http: HttpClient,
    private route: ActivatedRoute,
    private router: Router,
    private toastService: ToastService
  ) {
    this.form = this.fb.group({
      customerPublicId: ['', Validators.required],
      depositDate: [this.getToday(), Validators.required],
      deliveryDate: ['', Validators.required],
      invoiceLines: this.fb.array([]),
      discount: [0],
      amountPaid: [0],
      vatRate: [0],
      observations: ['']
    });
  }

  ngOnInit(): void {
    this.loadCustomers();
    this.loadPricings();

    this.invoiceId = this.route.snapshot.paramMap.get('id');
    if (this.invoiceId) {
      this.isEditMode.set(true);
      this.loadInvoice();
    } else {
      this.addLine();
    }
  }

  get invoiceLinesArray(): FormArray {
    return this.form.get('invoiceLines') as FormArray;
  }

  loadCustomers(): void {
    this.customerService.getAllCustomers().subscribe({
      next: (customers) => this.customers.set(customers)
    });
  }

  loadPricings(): void {
    this.http.get<Pricing[]>(`${environment.apiUrl}/pricing`).subscribe({
      next: (pricings) => this.pricings.set(pricings)
    });
  }

  loadInvoice(): void {
    if (this.invoiceId) {
      this.invoiceService.getInvoice(this.invoiceId).subscribe({
        next: (invoice) => {
          this.form.patchValue({
            customerPublicId: invoice.customer.publicId,
            depositDate: invoice.depositDate.split('T')[0],
            deliveryDate: invoice.deliveryDate.split('T')[0],
            discount: invoice.discount,
            amountPaid: invoice.amountPaid,
            observations: invoice.observations
          });

          this.invoiceLinesArray.clear();
          invoice.invoiceLines.forEach(line => {
            this.invoiceLinesArray.push(this.fb.group({
              pricingPublicId: [line.pricing.publicId, Validators.required],
              quantity: [line.quantity, [Validators.required, Validators.min(1)]]
            }));
          });
        }
      });
    }
  }

  addLine(): void {
    this.invoiceLinesArray.push(this.fb.group({
      pricingPublicId: ['', Validators.required],
      quantity: [1, [Validators.required, Validators.min(1)]]
    }));
  }

  removeLine(index: number): void {
    if (this.invoiceLinesArray.length > 1) {
      this.invoiceLinesArray.removeAt(index);
    }
  }

  onSubmit(): void {
    if (this.form.invalid) return;

    this.isSubmitting.set(true);
    const data = this.form.value;

    const request$ = this.isEditMode()
      ? this.invoiceService.updateInvoice(this.invoiceId!, data)
      : this.invoiceService.createInvoice(data);

    request$.subscribe({
      next: () => {
        this.router.navigate(['/invoices']);
      },
      error: (error) => {
        const message = error.error?.message || 'Erreur lors de l\'enregistrement de la facture';
        this.toastService.error(message);
        this.isSubmitting.set(false);
      }
    });
  }

  getToday(): string {
    return new Date().toISOString().split('T')[0];
  }

  formatMoney(amount: number): string {
    return new Intl.NumberFormat('fr-FR').format(amount) + ' FCFA';
  }
}
