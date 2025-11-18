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


1) Ajoutes la configuration cors. Le frontend va tourner sur http://localhost:4200
2) Ajoutes une table  de configuration "companie" qui contiendra les informations de l'entreprise, 
   lesquelles informations s'afficheront sur les factures. N'oublie pas le champ logo.
3) Ajoutes la fonctionnalité pour imprimer les factures (invoices)
4) Implémentes les rapports: pour chaque rapport, possibilité de filtrer par date début & date fin; aujourd'hui. Chaque rapport peut être imprimé. 