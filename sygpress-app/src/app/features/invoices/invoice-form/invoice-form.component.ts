import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, FormArray, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';
import { InvoiceService } from '../../../core/services/invoice.service';
import { CustomerService } from '../../../core/services/customer.service';
import { Customer, Pricing } from '../../../core/models';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { ToastService } from '../../../shared/services/toast.service';

@Component({
  selector: 'app-invoice-form',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule, FormsModule],
  templateUrl: './invoice-form.component.html',
  styleUrls: ['./invoice-form.component.css']
})
export class InvoiceFormComponent implements OnInit {
  form: FormGroup;
  isEditMode = signal(false);
  isSubmitting = signal(false);
  customers = signal<Customer[]>([]);
  pricings = signal<Pricing[]>([]);
  customerSearch = signal('');
  private invoiceId: string | null = null;

  filteredCustomers = computed(() => {
    const search = this.customerSearch().toLowerCase().trim();
    if (!search) {
      return this.customers();
    }
    return this.customers().filter(c =>
      c.name.toLowerCase().includes(search) ||
      c.phoneNumber.includes(search)
    );
  });

  // Calcul du total dynamique
  calculatedTotal = signal(0);
  calculatedSubtotal = signal(0);
  calculatedFeesTotal = signal(0);
  calculatedVat = signal(0);

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
      additionalFees: this.fb.array([]),
      discount: [0],
      amountPaid: [0],
      vatRate: [0],
      observations: ['']
    });

    // Écouter les changements pour recalculer le total
    this.form.valueChanges.subscribe(() => {
      this.updateCalculatedTotal();
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

  get additionalFeesArray(): FormArray {
    return this.form.get('additionalFees') as FormArray;
  }

  loadCustomers(): void {
    this.customerService.getAllCustomers().subscribe({
      next: (customers) => this.customers.set(Array.isArray(customers) ? customers : []),
      error: () => this.customers.set([])
    });
  }

  loadPricings(): void {
    this.http.get<Pricing[]>(`${environment.apiUrl}/pricing/all`).subscribe({
      next: (pricings) => {
        this.pricings.set(Array.isArray(pricings) ? pricings : []);
        this.updateCalculatedTotal();
      },
      error: () => this.pricings.set([])
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

  addFee(): void {
    this.additionalFeesArray.push(this.fb.group({
      title: ['', Validators.required],
      description: [''],
      amount: [0, [Validators.required, Validators.min(0)]]
    }));
  }

  removeFee(index: number): void {
    this.additionalFeesArray.removeAt(index);
  }

  updateCalculatedTotal(): void {
    const formValue = this.form.value;
    const pricingsMap = new Map(this.pricings().map(p => [p.publicId, p.price]));

    // Calcul du sous-total des lignes
    let subtotal = 0;
    for (const line of formValue.invoiceLines || []) {
      const price = pricingsMap.get(line.pricingPublicId) || 0;
      subtotal += price * (line.quantity || 0);
    }

    // Calcul des frais supplémentaires
    let feesTotal = 0;
    for (const fee of formValue.additionalFees || []) {
      feesTotal += fee.amount || 0;
    }

    // Calcul de la TVA
    const vatRate = formValue.vatRate || 0;
    const vatAmount = (subtotal + feesTotal) * (vatRate / 100);

    // Calcul du total
    const discount = formValue.discount || 0;
    const total = subtotal + feesTotal + vatAmount - discount;

    this.calculatedSubtotal.set(subtotal);
    this.calculatedFeesTotal.set(feesTotal);
    this.calculatedVat.set(vatAmount);
    this.calculatedTotal.set(total);
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
