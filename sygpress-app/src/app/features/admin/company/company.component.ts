import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CompanyService } from '../../../core/services/company.service';
import { Company } from '../../../core/models';
import { ToastService } from '../../../shared/services/toast.service';

@Component({
  selector: 'app-company',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div>
      <!-- Header -->
      <div class="mb-6">
        <h1 class="text-2xl font-bold text-gray-800">Informations de l'entreprise</h1>
        <p class="text-gray-600">Gérez les informations qui apparaîtront sur vos factures</p>
      </div>

      @if (isLoading()) {
        <div class="flex items-center justify-center py-12">
          <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
        </div>
      } @else {
        <div class="card p-6">
          <form [formGroup]="form" (ngSubmit)="onSubmit()">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <!-- Logo -->
              <div class="md:col-span-2">
                <label class="block text-sm font-medium text-gray-700 mb-2">Logo</label>
                <div class="flex items-center gap-4">
                  @if (company()?.logoUrl) {
                    <img [src]="company()!.logoUrl" alt="Logo" class="h-20 w-20 object-contain border rounded" />
                  } @else {
                    <div class="h-20 w-20 bg-gray-100 rounded flex items-center justify-center">
                      <svg class="w-8 h-8 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                      </svg>
                    </div>
                  }
                  <input
                    type="file"
                    accept="image/*"
                    (change)="onLogoChange($event)"
                    class="text-sm"
                  />
                </div>
              </div>

              <!-- Name -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Nom de l'entreprise *</label>
                <input type="text" formControlName="name" class="input" />
              </div>

              <!-- Slogan -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Slogan</label>
                <input type="text" formControlName="slogan" class="input" />
              </div>

              <!-- Address -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Adresse</label>
                <input type="text" formControlName="address" class="input" />
              </div>

              <!-- City -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Ville</label>
                <input type="text" formControlName="city" class="input" />
              </div>

              <!-- Country -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Pays</label>
                <input type="text" formControlName="country" class="input" />
              </div>

              <!-- Phone -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Téléphone</label>
                <input type="tel" formControlName="phoneNumber" class="input" />
              </div>

              <!-- Email -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Email</label>
                <input type="email" formControlName="email" class="input" />
              </div>

              <!-- Website -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Site web</label>
                <input type="url" formControlName="website" class="input" />
              </div>

              <!-- VAT Rate -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Taux TVA par défaut (%)</label>
                <input type="number" formControlName="vatRate" min="0" max="100" class="input" />
              </div>
            </div>

            <!-- Actions -->
            <div class="flex justify-end mt-6 pt-6 border-t">
              <button
                type="submit"
                [disabled]="form.invalid || isSubmitting()"
                class="btn-primary"
                [class.opacity-50]="form.invalid || isSubmitting()"
              >
                @if (isSubmitting()) {
                  <div class="animate-spin rounded-full h-5 w-5 border-b-2 border-white mr-2"></div>
                }
                Enregistrer
              </button>
            </div>
          </form>
        </div>
      }
    </div>
  `
})
export class CompanyComponent implements OnInit {
  form: FormGroup;
  company = signal<Company | null>(null);
  isLoading = signal(true);
  isSubmitting = signal(false);

  constructor(
    private fb: FormBuilder,
    private companyService: CompanyService,
    private toastService: ToastService
  ) {
    this.form = this.fb.group({
      name: ['', Validators.required],
      slogan: [''],
      address: [''],
      city: [''],
      country: [''],
      phoneNumber: [''],
      email: [''],
      website: [''],
      vatRate: [0]
    });
  }

  ngOnInit(): void {
    this.loadCompany();
  }

  loadCompany(): void {
    this.companyService.getCompany().subscribe({
      next: (company) => {
        this.company.set(company);
        this.form.patchValue({
          name: company.name,
          slogan: company.slogan,
          address: company.address,
          city: company.city,
          country: company.country,
          phoneNumber: company.phoneNumber,
          email: company.email,
          website: company.website,
          vatRate: company.vatRate
        });
        this.isLoading.set(false);
      },
      error: (error) => {
        const message = error.error?.message || 'Erreur lors du chargement des informations';
        this.toastService.error(message);
        this.isLoading.set(false);
      }
    });
  }

  onLogoChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      const file = input.files[0];

      // Validation côté client
      if (!file.type.startsWith('image/')) {
        this.toastService.error('Le fichier doit être une image (PNG, JPG, etc.)');
        return;
      }

      if (file.size > 5 * 1024 * 1024) { // 5MB max
        this.toastService.error('Le fichier ne doit pas dépasser 5 Mo');
        return;
      }

      this.companyService.uploadLogo(file).subscribe({
        next: (company) => {
          this.company.set(company);
          this.toastService.success('Logo téléchargé avec succès');
        },
        error: (error) => {
          let message = 'Erreur lors du téléchargement du logo';
          if (error.status === 400) {
            message = 'Fichier invalide. Veuillez sélectionner une image.';
          } else if (error.error?.message) {
            message = error.error.message;
          }
          this.toastService.error(message);
        }
      });
    }
  }

  onSubmit(): void {
    if (this.form.invalid) return;

    this.isSubmitting.set(true);
    this.companyService.updateCompany(this.form.value).subscribe({
      next: (company) => {
        this.company.set(company);
        this.toastService.success('Informations enregistrées avec succès');
        this.isSubmitting.set(false);
      },
      error: (error) => {
        const message = error.error?.message || 'Erreur lors de l\'enregistrement';
        this.toastService.error(message);
        this.isSubmitting.set(false);
      }
    });
  }
}
