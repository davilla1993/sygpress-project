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
  templateUrl: './invoice-form.component.html',
  styleUrls: ['./invoice-form.component.css']
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
    this.http.get<Pricing[]>(`${environment.apiUrl}/pricing/all`).subscribe({
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
