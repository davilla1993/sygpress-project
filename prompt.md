Planning Frontend Angular - Sygpress App
Phase 1 - Setup & Configuration (1-2h)
Créer le projet Angular (ng new sygpress-app)
Installer et configurer Tailwind CSS
Installer les dépendances (HttpClient, Router, Forms)
Configurer les environnements (API URL)
Créer la structure des dossiers (core, shared, features)
Configurer les interceptors HTTP (JWT, errors)
Phase 2 - Authentification (2-3h)
Service AuthService (login, logout, token management)
Guards (AuthGuard, RoleGuard)
Page de connexion
Interceptor pour ajouter le token JWT
Redirection selon le rôle
Phase 3 - Layout & Navigation (2-3h)
Composant Sidebar avec menu dynamique
Composant Header (user info, logout)
Layout principal avec outlet
Responsive design (mobile menu)
Phase 4 - Dashboards (3-4h)
Dashboard Admin (KPIs, graphiques avec ng2-charts)
Dashboard User/Gérant (alertes, opérations)
Widgets réutilisables (cards, stats)
Phase 5 - Gestion des Factures (5-6h)
Liste des factures (filtres, pagination, recherche)
Formulaire création/modification
Détail facture avec lignes
Impression PDF
Changement de statut de traitement
Modal paiement
Phase 6 - Gestion des Clients (2-3h)
Liste des clients
Formulaire client
Historique factures
Phase 7 - Configuration Admin (4-5h)
CRUD Articles & Catégories
CRUD Services
CRUD Tarification
Configuration entreprise (logo upload)
Gestion utilisateurs
Phase 8 - Rapports (2-3h)
Sélecteur de période
Affichage des rapports
Export PDF
Stack technique :

Angular 17+ (standalone components)
Tailwind CSS
Angular Router
Reactive Forms
HttpClient + Interceptors
ng2-charts (graphiques)
Estimation totale : ~25-30h

État actuel - Toutes les phases sont terminées !
Le planning frontend est complet. Voici les options pour la suite :

1. Tester l'application
Lancer le backend (mvn spring-boot:run dans sygpress-api)
Lancer le frontend (npm start dans sygpress-app)
Tester les fonctionnalités créées
2. Améliorations possibles
Ajouter un CRUD pour les Catégories (manquant dans Admin)
Améliorer la gestion des erreurs (notifications toast)
Ajouter une confirmation avant suppression (modal)
Ajouter des graphiques au dashboard (ng2-charts)
3. Fonctionnalités avancées
Export Excel pour les rapports
Recherche avancée multi-critères
Historique des factures par client
Statistiques détaillées
4. Déploiement
Configuration pour production
Dockerisation de l'application
CI/CD pipeline