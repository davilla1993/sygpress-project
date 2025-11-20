import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { InvoiceService } from '../../../core/services/invoice.service';
import { Invoice, ProcessingStatus } from '../../../core/models';
import { ToastService } from '../../../shared/services/toast.service';

@Component({
  selector: 'app-invoice-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './invoice-detail.component.html',
  styleUrls: ['./invoice-detail.component.css']
})
export class InvoiceDetailComponent implements OnInit {
  invoice = signal<Invoice | null>(null);
  isLoading = signal(true);
  paymentAmount: number = 0;

  constructor(
    private invoiceService: InvoiceService,
    private route: ActivatedRoute,
    private router: Router,
    private toastService: ToastService
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
      error: (error) => {
        const message = error.error?.message || 'Erreur lors du chargement de la facture';
        this.toastService.error(message);
        this.isLoading.set(false);
        this.router.navigate(['/invoices']);
      }
    });
  }

  updateStatus(status: ProcessingStatus): void {
    const inv = this.invoice();
    if (inv) {
      // Demander confirmation avant de changer le statut
      const statusLabels: { [key: string]: string } = {
        'DEPOT': 'Dépôt',
        'EN_LAVAGE': 'En lavage',
        'EN_REPASSAGE': 'En repassage',
        'PRET': 'Prêt',
        'LIVRE': 'Livré',
        'COLLECTE': 'Collecté'
      };

      const statusLabel = statusLabels[status] || status;

      if (!confirm(`Êtes-vous sûr de vouloir changer le statut de cette facture à "${statusLabel}" ?`)) {
        // Réinitialiser le select à la valeur actuelle
        // Force Angular to re-render the select with the current value
        const currentStatus = inv.processingStatus;
        setTimeout(() => {
          const selectElement = document.querySelector('select[ng-reflect-model]') as HTMLSelectElement;
          if (selectElement) {
            selectElement.value = currentStatus;
          }
        });
        return;
      }

      this.invoiceService.updateStatus(inv.publicId, status).subscribe({
        next: (updated) => {
          this.invoice.set(updated);
          this.toastService.success('Statut modifié avec succès');
        },
        error: (error) => {
          const message = error.error?.message || 'Erreur lors de la mise à jour du statut';
          this.toastService.error(message);
        }
      });
    }
  }

  addPayment(): void {
    const inv = this.invoice();
    if (inv && this.paymentAmount > 0) {
      // Demander confirmation avant d'ajouter le paiement
      const formattedAmount = this.formatMoney(this.paymentAmount);
      if (!confirm(`Êtes-vous sûr de vouloir enregistrer un paiement de ${formattedAmount} ?`)) {
        return;
      }

      this.invoiceService.addPayment(inv.publicId, { amount: this.paymentAmount }).subscribe({
        next: (updated) => {
          this.invoice.set(updated);
          this.paymentAmount = 0;
          this.toastService.success('Paiement enregistré avec succès');
        },
        error: (error) => {
          const message = error.error?.message || 'Erreur lors de l\'ajout du paiement';
          this.toastService.error(message);
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
        },
        error: (error) => {
          const message = error.error?.message || 'Erreur lors de l\'impression';
          this.toastService.error(message);
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

  canEditInvoice(): boolean {
    // Une facture ne peut plus être modifiée dès qu'elle est enregistrée en BDD (statut != DRAFT)
    // En effet, une facture enregistrée en BDD a par défaut le statut DEPOT
    return false;
  }
}
