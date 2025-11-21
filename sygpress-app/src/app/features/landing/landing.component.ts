import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './landing.component.html',
  styleUrl: './landing.component.css'
})
export class LandingComponent {
  features = [
    {
      icon: '👥',
      title: 'Gestion Clients',
      description: 'Centralisez toutes les informations de vos clients et suivez leur historique complet.'
    },
    {
      icon: '👔',
      title: 'Catalogue Articles',
      description: 'Gérez facilement vos articles par catégories avec une tarification flexible.'
    },
    {
      icon: '🧺',
      title: 'Services Multiples',
      description: 'Nettoyage à sec, lavage, repassage - configurez tous vos services.'
    },
    {
      icon: '💰',
      title: 'Facturation Automatique',
      description: 'Génération automatique de factures PDF avec calculs précis et sans erreur.'
    },
    {
      icon: '📊',
      title: 'Tableau de Bord',
      description: 'Visualisez vos KPIs en temps réel : CA, factures, nouveaux clients.'
    },
    {
      icon: '📈',
      title: 'Rapports Détaillés',
      description: 'Analyses complètes et exports pour optimiser votre activité.'
    }
  ];

  pricingPlans = [
    {
      name: 'Starter',
      price: 'Gratuit',
      description: 'Parfait pour tester',
      features: [
        'Jusqu\'à 50 clients',
        'Gestion de base',
        'Support email',
        'Facturation simple'
      ],
      cta: 'Commencer',
      highlighted: false
    },
    {
      name: 'Professional',
      price: '29€',
      period: '/mois',
      description: 'Pour les pressings en croissance',
      features: [
        'Clients illimités',
        'Toutes les fonctionnalités',
        'Support prioritaire',
        'Rapports avancés',
        'Exports illimités',
        'Multi-utilisateurs'
      ],
      cta: 'Essayer gratuitement',
      highlighted: true
    },
    {
      name: 'Enterprise',
      price: 'Sur mesure',
      description: 'Solution personnalisée',
      features: [
        'Tout de Professional',
        'Formation personnalisée',
        'Support dédié 24/7',
        'API personnalisée',
        'Intégrations sur mesure'
      ],
      cta: 'Nous contacter',
      highlighted: false
    }
  ];

  testimonials = [
    {
      name: 'Marie Dubois',
      role: 'Gérante, Pressing Paris 15',
      content: 'SygPress a transformé notre façon de travailler. Plus d\'erreurs de facturation, tout est automatisé !',
      avatar: '👩‍💼'
    },
    {
      name: 'Jean Martin',
      role: 'Propriétaire, Clean Express',
      content: 'Un gain de temps énorme au quotidien. Je recommande vivement cette solution.',
      avatar: '👨‍💼'
    },
    {
      name: 'Sophie Laurent',
      role: 'Responsable, Pressing Moderne',
      content: 'Interface intuitive et support réactif. Exactement ce dont nous avions besoin.',
      avatar: '👩'
    }
  ];

  faqs = [
    {
      question: 'Combien de temps prend l\'installation ?',
      answer: 'Vous pouvez commencer à utiliser SygPress en moins de 5 minutes. Créez votre compte, ajoutez vos services et tarifs, et c\'est parti !'
    },
    {
      question: 'Mes données sont-elles sécurisées ?',
      answer: 'Absolument. Toutes vos données sont cryptées et sauvegardées quotidiennement. Nous respectons le RGPD.'
    },
    {
      question: 'Puis-je migrer mes données existantes ?',
      answer: 'Oui, nous proposons un service d\'import de données depuis vos fichiers Excel ou autres logiciels.'
    },
    {
      question: 'Y a-t-il un engagement ?',
      answer: 'Non, vous pouvez annuler à tout moment. Pas de frais cachés, pas d\'engagement.'
    },
    {
      question: 'Proposez-vous une formation ?',
      answer: 'Oui, nous offrons une formation complète en visio et une documentation détaillée.'
    }
  ];

  currentYear = new Date().getFullYear();
}
