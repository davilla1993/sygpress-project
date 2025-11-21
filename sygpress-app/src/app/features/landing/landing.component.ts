import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ContactFormComponent } from './contact-form/contact-form.component';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [CommonModule, RouterLink, ContactFormComponent],
  templateUrl: './landing.component.html',
  styleUrl: './landing.component.css'
})
export class LandingComponent {
  protected readonly services = signal([
    {
      icon: 'M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z',
      title: 'Nettoyage à sec',
      description: 'Service professionnel de nettoyage à sec pour tous vos vêtements délicats.'
    },
    {
      icon: 'M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z',
      title: 'Repassage',
      description: 'Repassage impeccable de vos chemises, pantalons et tous types de vêtements.'
    },
    {
      icon: 'M5 3v4M3 5h4M6 17v4m-2-2h4m5-16l2.286 6.857L21 12l-5.714 2.143L13 21l-2.286-6.857L5 12l5.714-2.143L13 3z',
      title: 'Services spécialisés',
      description: 'Traitement des taches difficiles, lavage de rideaux, couettes et plus encore.'
    },
    {
      icon: 'M13 10V3L4 14h7v7l9-11h-7z',
      title: 'Service express',
      description: 'Besoin urgent? Profitez de notre service express en 24h.'
    }
  ]);

  protected readonly features = signal([
    {
      title: 'Qualité garantie',
      description: 'Nous utilisons des produits professionnels de haute qualité pour prendre soin de vos vêtements.'
    },
    {
      title: 'Service rapide',
      description: 'Délais respectés avec possibilité de service express pour vos urgences.'
    },
    {
      title: 'Tarifs transparents',
      description: 'Prix clairs et compétitifs sans frais cachés.'
    },
    {
      title: 'Suivi en ligne',
      description: 'Suivez l\'état de vos commandes en temps réel depuis votre compte.'
    }
  ]);

  scrollToContact(): void {
    const contactSection = document.getElementById('contact');
    if (contactSection) {
      contactSection.scrollIntoView({ behavior: 'smooth' });
    }
  }
}
