# SygPress - Système de Gestion de Pressing

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-green)
![Angular](https://img.shields.io/badge/Angular-20-red)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-blue)
![Docker](https://img.shields.io/badge/Docker-Ready-blue)

SygPress est une application web complète pour la gestion d'un pressing, développée en architecture monorepo avec Spring Boot (backend) et Angular (frontend).

## 📋 Table des matières

- [Fonctionnalités](#-fonctionnalités)
- [Architecture](#-architecture)
- [Technologies](#-technologies)
- [Prérequis](#-prérequis)
- [Installation](#-installation)
- [Configuration](#-configuration)
- [Développement](#-développement)
- [Déploiement](#-déploiement)
- [Documentation](#-documentation)
- [Licence](#-licence)

## ✨ Fonctionnalités

### Gestion des Clients
- Enregistrement et gestion des informations clients
- Historique des commandes par client
- Recherche et filtrage avancés

### Gestion des Articles
- Catalogue des vêtements et articles
- Catégorisation des articles
- Gestion des tarifs par article et service

### Services de Pressing
- Multiples types de services (nettoyage à sec, repassage, etc.)
- Tarification flexible par service et catégorie
- Gestion des frais supplémentaires

### Facturation
- Création et gestion des factures
- Calcul automatique des montants
- Génération de factures PDF
- Historique des paiements

### Tableau de bord
- Vue d'ensemble des activités
- Statistiques en temps réel
- Indicateurs de performance (KPIs)

### Rapports
- Rapports financiers
- Top clients
- Analyses des services
- Exports PDF

### Administration
- Gestion des utilisateurs et rôles
- Authentification JWT sécurisée
- Logs d'audit
- Configuration de l'entreprise

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────┐
│                   SygPress Project                  │
│                   (Monorepo)                        │
├─────────────────────────────────────────────────────┤
│                                                     │
│  ┌──────────────────┐      ┌──────────────────┐   │
│  │   sygpress-app   │      │  sygpress-api    │   │
│  │   (Frontend)     │◄────►│   (Backend)      │   │
│  │                  │      │                  │   │
│  │  • Angular 20    │      │  • Spring Boot   │   │
│  │  • TypeScript    │      │  • Java 21       │   │
│  │  • Tailwind CSS  │      │  • Spring Data   │   │
│  │  • Chart.js      │      │  • Spring Sec.   │   │
│  │  • ng-select     │      │  • JWT Auth      │   │
│  └──────────────────┘      │  • OpenAPI       │   │
│                            └──────────────────┘   │
│                                     │              │
│                                     ▼              │
│                            ┌──────────────────┐   │
│                            │   PostgreSQL     │   │
│                            │   (Database)     │   │
│                            └──────────────────┘   │
└─────────────────────────────────────────────────────┘
```

### Architecture de Déploiement (Monorepo Docker)

```
┌─────────────────────────────────────┐
│   Container Docker (Port 8080)      │
│                                     │
│  ┌───────────────────────────────┐ │
│  │   Spring Boot Application     │ │
│  │   ├─ API REST (/api/*)        │ │
│  │   ├─ Static Files (Angular)   │ │
│  │   ├─ Health Checks            │ │
│  │   └─ Swagger UI               │ │
│  └───────────────────────────────┘ │
└─────────────────────────────────────┘
            │
            ▼
    PostgreSQL Database
    (Externe - Non dockerisé)
```

## 🛠️ Technologies

### Backend
- **Java 21** - Langage de programmation
- **Spring Boot 3.5.6** - Framework backend
- **Spring Data JPA** - Persistence des données
- **Spring Security** - Sécurité et authentification
- **JWT** - JSON Web Tokens pour l'authentification
- **PostgreSQL** - Base de données relationnelle
- **OpenPDF** - Génération de documents PDF
- **Springdoc OpenAPI** - Documentation API (Swagger)
- **Maven** - Gestion des dépendances

### Frontend
- **Angular 20** - Framework frontend
- **TypeScript** - Langage de programmation
- **Tailwind CSS** - Framework CSS
- **Chart.js / ng2-charts** - Graphiques et visualisations
- **ng-select** - Composant de sélection avancé
- **RxJS** - Programmation réactive

### DevOps
- **Docker** - Containerisation
- **Coolify** - Plateforme de déploiement
- **Git** - Contrôle de version

## 📦 Prérequis

### Pour le développement local

#### Backend
- Java 21 (JDK)
- Maven 3.9+
- PostgreSQL 14+

#### Frontend
- Node.js 20+
- npm 10+

### Pour le déploiement (Coolify)
- Docker
- Coolify installé sur VPS
- PostgreSQL (externe, non dockerisé)

## 🚀 Installation

### 1. Cloner le repository

```bash
git clone https://github.com/davilla1993/sygpress-project.git
cd sygpress-project
```

### 2. Configuration de la base de données

```sql
-- Créer la base de données
CREATE DATABASE sygpress_db;

-- Créer un utilisateur (optionnel)
CREATE USER sygpress_user WITH PASSWORD 'votre_mot_de_passe';
GRANT ALL PRIVILEGES ON DATABASE sygpress_db TO sygpress_user;
```

### 3. Configuration du Backend

Modifier le fichier `sygpress-api/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/sygpress_db
    username: postgres
    password: votre_mot_de_passe
```

### 4. Installation des dépendances

#### Backend
```bash
cd sygpress-api
./mvnw clean install
```

#### Frontend
```bash
cd sygpress-app
npm install
```

## ⚙️ Configuration

### Variables d'environnement (Production)

Copiez le fichier `.env.example` et personnalisez les valeurs:

```bash
cp .env.example .env
```

Variables obligatoires:
- `DB_HOST` - Hôte de la base de données
- `DB_NAME` - Nom de la base de données
- `DB_USERNAME` - Utilisateur de la BDD
- `DB_PASSWORD` - Mot de passe de la BDD
- `JWT_SECRET` - Secret pour les tokens JWT

Voir `.env.example` pour la liste complète.

## 💻 Développement

### Démarrer le backend

```bash
cd sygpress-api
./mvnw spring-boot:run
```

Le backend sera accessible sur `http://localhost:8080`

### Démarrer le frontend

```bash
cd sygpress-app
npm start
# ou
ng serve
```

Le frontend sera accessible sur `http://localhost:4200`

### Accès aux outils de développement

- **Application Frontend**: http://localhost:4200
- **API Backend**: http://localhost:8080/api
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/v3/api-docs

### Structure du projet

```
sygpress-project/
├── sygpress-api/              # Backend Spring Boot
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/follysitou/sygpress/
│   │   │   │       ├── controller/    # Contrôleurs REST
│   │   │   │       ├── service/       # Logique métier
│   │   │   │       ├── repository/    # Accès aux données
│   │   │   │       ├── model/         # Entités JPA
│   │   │   │       ├── config/        # Configuration
│   │   │   │       ├── security/      # Sécurité JWT
│   │   │   │       └── dto/           # Data Transfer Objects
│   │   │   └── resources/
│   │   │       ├── application.yml           # Config dev
│   │   │       └── application-prod.yml      # Config prod
│   │   └── test/              # Tests unitaires
│   └── pom.xml                # Dépendances Maven
│
├── sygpress-app/              # Frontend Angular
│   ├── src/
│   │   ├── app/
│   │   │   ├── components/    # Composants Angular
│   │   │   ├── services/      # Services Angular
│   │   │   ├── models/        # Modèles TypeScript
│   │   │   ├── guards/        # Route guards
│   │   │   └── interceptors/  # HTTP interceptors
│   │   ├── environments/      # Configuration environnement
│   │   └── assets/            # Ressources statiques
│   └── package.json           # Dépendances npm
│
├── Dockerfile                 # Build monorepo
├── .dockerignore              # Exclusions Docker
├── .env.example               # Template variables d'env
├── DEPLOYMENT.md              # Guide de déploiement
└── README.md                  # Ce fichier
```

## 🐳 Déploiement

### Déploiement avec Docker (monorepo)

Le projet utilise un Dockerfile multi-stage qui construit le frontend et le backend dans un seul container.

```bash
# Build l'image Docker
docker build -t sygpress:latest .

# Lancer le container
docker run -d \
  -p 8080:8080 \
  -e DB_HOST=votre_db_host \
  -e DB_NAME=sygpress_db \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=votre_password \
  -e JWT_SECRET=votre_jwt_secret \
  --name sygpress \
  sygpress:latest
```

### Déploiement avec Coolify

Consultez le fichier [DEPLOYMENT.md](./DEPLOYMENT.md) pour un guide complet du déploiement sur Coolify.

Étapes rapides:
1. Créer la base de données PostgreSQL sur le VPS
2. Créer un projet dans Coolify
3. Configurer les variables d'environnement
4. Déployer depuis GitHub

## 📚 Documentation

- [Guide de déploiement Coolify](./DEPLOYMENT.md) - Instructions complètes pour déployer sur Coolify
- [Guide utilisateur](./user_guide.md) - Documentation pour les utilisateurs finaux
- [API Documentation](http://localhost:8080/swagger-ui.html) - Documentation interactive de l'API (en local)

## 🔒 Sécurité

### Authentification
L'application utilise JWT (JSON Web Tokens) pour l'authentification:
- Tokens sécurisés avec secret configurable
- Expiration configurable (par défaut 24h)
- Refresh tokens pour sessions longues

### Rôles et permissions
- **ADMIN** - Accès complet à toutes les fonctionnalités
- **USER** - Accès aux fonctionnalités opérationnelles

### Bonnes pratiques
- Changez le `JWT_SECRET` en production
- Utilisez des mots de passe forts
- Activez HTTPS en production
- Configurez un firewall sur le VPS
- Effectuez des sauvegardes régulières de la BDD

## 🧪 Tests

### Backend
```bash
cd sygpress-api
./mvnw test
```

### Frontend
```bash
cd sygpress-app
npm test
```

## 🤝 Contribution

Les contributions sont les bienvenues! Veuillez suivre ces étapes:

1. Fork le projet
2. Créez une branche pour votre fonctionnalité (`git checkout -b feature/AmazingFeature`)
3. Committez vos changements (`git commit -m 'Add some AmazingFeature'`)
4. Poussez vers la branche (`git push origin feature/AmazingFeature`)
5. Ouvrez une Pull Request

## 📝 Changelog

### [Dernière version] - 2025-01-21

#### Ajouté
- Configuration Docker monorepo pour Coolify
- Support des variables d'environnement pour la production
- Health checks via Spring Boot Actuator
- Configuration SPA pour le routing Angular
- Documentation complète de déploiement

## 📄 Licence

Ce projet est sous licence privée. Tous droits réservés.

## 👥 Auteurs

- **Folly Sitou** - Développeur principal

## 📞 Support

Pour toute question ou problème:
- Ouvrir une issue sur GitHub
- Consulter la documentation dans le dossier `/docs`
- Vérifier les logs de l'application

## 🙏 Remerciements

- Spring Boot team pour le framework backend
- Angular team pour le framework frontend
- La communauté open source

---

**Note**: Ce projet est en développement actif. Les fonctionnalités et la documentation sont susceptibles d'évoluer.
