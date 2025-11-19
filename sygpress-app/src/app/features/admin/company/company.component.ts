import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CompanyService } from '../../../core/services/company.service';
import { Company } from '../../../core/models';
import { ToastService } from '../../../shared/services/toast.service';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-company',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './company.component.html',
  styleUrls: ['./company.component.css']
})
export class CompanyComponent implements OnInit {
  form: FormGroup;
  company = signal<Company | null>(null);
  isLoading = signal(true);
  isSubmitting = signal(false);

  // Computed pour construire l'URL complète du logo
  logoFullUrl = computed(() => {
    const comp = this.company();
    if (comp?.logoUrl) {
      // Si c'est déjà une URL complète, la retourner
      if (comp.logoUrl.startsWith('http')) {
        return comp.logoUrl;
      }
      // Sinon construire l'URL complète avec timestamp pour éviter le cache
      const baseUrl = environment.apiUrl.replace('/api', '');
      return `${baseUrl}${comp.logoUrl}?t=${Date.now()}`;
    }
    return null;
  });

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
        // 404 = pas encore d'entreprise, afficher le formulaire vide
        if (error.status === 404) {
          this.isLoading.set(false);
          return;
        }
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
