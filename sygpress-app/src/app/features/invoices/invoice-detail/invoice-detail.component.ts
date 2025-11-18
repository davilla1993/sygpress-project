import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { InvoiceService } from '../../../core/services/invoice.service';
import { Invoice, ProcessingStatus } from '../../../core/models';

@Component({
  selector: 'app-invoice-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div>
      @if (isLoading()) {
        <div class="flex items-center justify-center py-12">
          <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
        </div>
      } @else if (invoice()) {
        <!-- Header -->
        <div class="flex justify-between items-start mb-6">
          <div>
            <h1 class="text-2xl font-bold text-gray-800">Facture {{ invoice()!.invoiceNumber }}</h1>
            <p class="text-gray-600">{{ invoice()!.customer.name }}</p>
          </div>
          <div class="flex gap-2">
            <button (click)="printInvoice()" class="btn-secondary">
              <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 17h2a2 2 0 002-2v-4a2 2 0 00-2-2H5a2 2 0 00-2 2v4a2 2 0 002 2h2m2 4h6a2 2 0 002-2v-4a2 2 0 00-2-2H9a2 2 0 00-2 2v4a2 2 0 002 2zm8-12V5a2 2 0 00-2-2H9a2 2 0 00-2 2v4h10z" />
              </svg>
              Imprimer
            </button>
            <a [routerLink]="['edit']" class="btn-secondary">Modifier</a>
          </div>
        </div>

        <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <!-- Main Info -->
          <div class="lg:col-span-2 space-y-6">
            <!-- Status & Dates -->
            <div class="card p-6">
              <h3 class="text-lg font-semibold text-gray-800 mb-4">Informations</h3>
              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-gray-500">Date dépôt</label>
                  <p class="mt-1 text-gray-900">{{ formatDate(invoice()!.depositDate) }}</p>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-500">Date livraison</label>
                  <p class="mt-1 text-gray-900">{{ formatDate(invoice()!.deliveryDate) }}</p>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-500">Statut</label>
                  <div class="mt-1">
                    <select
                      [ngModel]="invoice()!.processingStatus"
                      (ngModelChange)="updateStatus($event)"
                      class="input py-1 text-sm"
                    >
                      <option value="COLLECTE">Collecté</option>
                      <option value="EN_LAVAGE">En lavage</option>
                      <option value="EN_REPASSAGE">En repassage</option>
                      <option value="PRET">Prêt</option>
                      <option value="LIVRE">Livré</option>
                      <option value="RECUPERE">Récupéré</option>
                    </select>
                  </div>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-500">Client</label>
                  <p class="mt-1 text-gray-900">{{ invoice()!.customer.name }}</p>
                  <p class="text-sm text-gray-500">{{ invoice()!.customer.phoneNumber }}</p>
                </div>
              </div>
              @if (invoice()!.observations) {
                <div class="mt-4 pt-4 border-t">
                  <label class="block text-sm font-medium text-gray-500">Observations</label>
                  <p class="mt-1 text-gray-900">{{ invoice()!.observations }}</p>
                </div>
              }
            </div>

            <!-- Lines -->
            <div class="card p-6">
              <h3 class="text-lg font-semibold text-gray-800 mb-4">Articles</h3>
              <table class="w-full">
                <thead>
                  <tr class="text-left text-sm text-gray-500">
                    <th class="pb-2">Article</th>
                    <th class="pb-2">Service</th>
                    <th class="pb-2 text-right">Prix unit.</th>
                    <th class="pb-2 text-right">Qté</th>
                    <th class="pb-2 text-right">Total</th>
                  </tr>
                </thead>
                <tbody class="divide-y">
                  @for (line of invoice()!.invoiceLines; track line.publicId) {
                    <tr>
                      <td class="py-2">{{ line.pricing.article.name }}</td>
                      <td class="py-2">{{ line.pricing.service.name }}</td>
                      <td class="py-2 text-right">{{ formatMoney(line.pricing.price) }}</td>
                      <td class="py-2 text-right">{{ line.quantity }}</td>
                      <td class="py-2 text-right font-medium">{{ formatMoney(line.amount) }}</td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>

            <!-- Additional Fees -->
            @if (invoice()!.additionalFees.length > 0) {
              <div class="card p-6">
                <h3 class="text-lg font-semibold text-gray-800 mb-4">Frais supplémentaires</h3>
                <div class="space-y-2">
                  @for (fee of invoice()!.additionalFees; track fee.publicId) {
                    <div class="flex justify-between">
                      <div>
                        <p class="font-medium">{{ fee.title }}</p>
                        @if (fee.description) {
                          <p class="text-sm text-gray-500">{{ fee.description }}</p>
                        }
                      </div>
                      <p class="font-medium">{{ formatMoney(fee.amount) }}</p>
                    </div>
                  }
                </div>
              </div>
            }
          </div>

          <!-- Summary -->
          <div class="space-y-6">
            <!-- Total -->
            <div class="card p-6">
              <h3 class="text-lg font-semibold text-gray-800 mb-4">Résumé</h3>
              <div class="space-y-2">
                <div class="flex justify-between text-sm">
                  <span class="text-gray-500">Sous-total</span>
                  <span>{{ formatMoney(getSubtotal()) }}</span>
                </div>
                @if (invoice()!.discount > 0) {
                  <div class="flex justify-between text-sm">
                    <span class="text-gray-500">Remise</span>
                    <span class="text-red-600">-{{ formatMoney(invoice()!.discount) }}</span>
                  </div>
                }
                @if (invoice()!.vatAmount > 0) {
                  <div class="flex justify-between text-sm">
                    <span class="text-gray-500">TVA</span>
                    <span>{{ formatMoney(invoice()!.vatAmount) }}</span>
                  </div>
                }
                <div class="flex justify-between font-bold text-lg pt-2 border-t">
                  <span>Total</span>
                  <span>{{ formatMoney(getTotal()) }}</span>
                </div>
              </div>
            </div>

            <!-- Payment -->
            <div class="card p-6">
              <h3 class="text-lg font-semibold text-gray-800 mb-4">Paiement</h3>
              <div class="space-y-2">
                <div class="flex justify-between text-sm">
                  <span class="text-gray-500">Payé</span>
                  <span class="text-green-600">{{ formatMoney(invoice()!.amountPaid) }}</span>
                </div>
                <div class="flex justify-between text-sm">
                  <span class="text-gray-500">Reste</span>
                  <span class="text-red-600">{{ formatMoney(invoice()!.remainingAmount) }}</span>
                </div>
                <div class="flex justify-between pt-2 border-t">
                  <span class="font-medium">Statut</span>
                  <span
                    class="px-2 py-1 text-xs font-medium rounded-full"
                    [class.bg-green-100]="invoice()!.invoicePaid"
                    [class.text-green-700]="invoice()!.invoicePaid"
                    [class.bg-red-100]="!invoice()!.invoicePaid"
                    [class.text-red-700]="!invoice()!.invoicePaid"
                  >
                    {{ invoice()!.invoicePaid ? 'Payé' : 'Impayé' }}
                  </span>
                </div>
              </div>

              @if (!invoice()!.invoicePaid) {
                <div class="mt-4 pt-4 border-t">
                  <label class="block text-sm font-medium text-gray-700 mb-2">Ajouter paiement</label>
                  <div class="flex gap-2">
                    <input
                      type="number"
                      [(ngModel)]="paymentAmount"
                      [max]="invoice()!.remainingAmount"
                      class="input flex-1"
                      placeholder="Montant"
                    />
                    <button (click)="addPayment()" class="btn-primary" [disabled]="!paymentAmount">
                      Payer
                    </button>
                  </div>
                </div>
              }
            </div>
          </div>
        </div>

        <!-- Back button -->
        <div class="mt-6">
          <a routerLink="/invoices" class="text-primary-600 hover:text-primary-800 flex items-center">
            <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
            </svg>
            Retour à la liste
          </a>
        </div>
      }
    </div>
  `
})
export class InvoiceDetailComponent implements OnInit {
  invoice = signal<Invoice | null>(null);
  isLoading = signal(true);
  paymentAmount: number = 0;

  constructor(
    private invoiceService: InvoiceService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadInvoice(id);
    }
  }

  loadInvoice(id: string): void {
    this.invoiceService.getInvoice(id).subscribe({
      next: (invoice) => {
        this.invoice.set(invoice);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
        this.router.navigate(['/invoices']);
      }
    });
  }

  updateStatus(status: ProcessingStatus): void {
    const inv = this.invoice();
    if (inv) {
      this.invoiceService.updateStatus(inv.publicId, status).subscribe({
        next: (updated) => {
          this.invoice.set(updated);
        }
      });
    }
  }

  addPayment(): void {
    const inv = this.invoice();
    if (inv && this.paymentAmount > 0) {
      this.invoiceService.addPayment(inv.publicId, { amount: this.paymentAmount }).subscribe({
        next: (updated) => {
          this.invoice.set(updated);
          this.paymentAmount = 0;
        }
      });
    }
  }

  printInvoice(): void {
    const inv = this.invoice();
    if (inv) {
      this.invoiceService.printInvoice(inv.publicId).subscribe({
        next: (blob) => {
          const url = window.URL.createObjectURL(blob);
          window.open(url, '_blank');
        }
      });
    }
  }

  getSubtotal(): number {
    const inv = this.invoice();
    if (!inv) return 0;
    const linesTotal = inv.invoiceLines.reduce((sum, line) => sum + line.amount, 0);
    const feesTotal = inv.additionalFees.reduce((sum, fee) => sum + fee.amount, 0);
    return linesTotal + feesTotal;
  }

  getTotal(): number {
    const inv = this.invoice();
    if (!inv) return 0;
    return this.getSubtotal() - (inv.discount || 0) + (inv.vatAmount || 0);
  }

  formatDate(date: string): string {
    return new Date(date).toLocaleDateString('fr-FR');
  }

  formatMoney(amount: number): string {
    return new Intl.NumberFormat('fr-FR').format(amount) + ' FCFA';
  }
}
