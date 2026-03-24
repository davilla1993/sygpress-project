# SygPress - Système de Gestion de Pressing

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-green)
![Angular](https://img.shields.io/badge/Angular-20-red)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-blue)
![Docker](https://img.shields.io/badge/Docker-Ready-blue)

🚀 **Démo en direct : [https://sygpress.gfolly.com](https://sygpress.gfolly.com)**

SygPress est une application web métier complète conçue pour gérer l'ensemble des opérations d'un pressing. Développée et mise en avant pour mon portefolio, cette application démontre mes compétences en développement Fullstack (Spring Boot / Angular), en conception d'architecture logicielle robuste et en bonnes pratiques de code.

## 🎯 Aperçu du Projet

SygPress vise à digitaliser et automatiser les processus de gestion d'un pressing : de la réception des vêtements à la facturation, en passant par le suivi des prestations et l'administration des utilisateurs. 

Ce projet met en évidence ma capacité à :
- Créer une **API RESTful sécurisée**, structurée et performante.
- Développer une **interface utilisateur dynamique**, intuitive et moderne.
- Modéliser et exploiter une base de données relationnelle complexe.
- Containeriser et préparer une application complète pour le déploiement via **Docker**.

## ✨ Fonctionnalités Principales

- **Gestion Clientèle & Commandes :** Enregistrement des clients, suivi de l'historique des commandes et filtrage avancé de la base clients.
- **Catalogue & Tarification :** Gestion des articles, classification par catégories et tarification dynamique selon la prestation (nettoyage à sec, repassage, etc.).
- **Facturation Automatisée :** Calcul automatique des montants (incluant les frais additionnels), paiements et génération de factures au format PDF.
- **Tableau de Bord & Analytics :** Statistiques en temps réel, indicateurs de performance clés (KPIs) et suivi financier visuel.
- **Sécurité & Administration :** Gestion des rôles, protection des routes, et authentification sécurisée par token (JWT).

## 🛠️ Stack Technique

### Backend (API)
- **Java 21 & Spring Boot 3.5.6** : Architecture backend robuste et moderne.
- **Spring Data JPA & PostgreSQL** : Persistance des données et requêtage optimisé.
- **Spring Security & JWT** : Sécurisation globale, authentification stateless et filtrage des accès.
- **OpenPDF & Springdoc OpenAPI** : Génération de documents et documentation interactive (Swagger).

### Frontend (SPA)
- **Angular 20 & TypeScript** : Interface utilisateur réactive et typage fort.
- **Tailwind CSS** : Design system sur-mesure, moderne et 100% responsive.
- **RxJS / ng-select / Chart.js** : Programmation réactive, composants graphiques avancés et visualisations interactives.

### DevOps & Outils
- **Docker** : Architecture Docker multi-stage pour le déploiement.
- **Maven & npm** : Gestionnaires de paquets et dépendances.
- **Git** : Contrôle des versions.

## 🏗️ Structure du Projet

Le projet suit une architecture claire séparant le backend et le frontend, regroupés au sein d'un monorepo facilitant le développement et le déploiement.

```text
SygPress
├── sygpress-api/ (Backend Spring Boot)
│   └── API REST sécurisée, logique métier (Services), accès aux données (Repositories)
└── sygpress-app/ (Frontend Angular)
    └── Interface Single Page Application (SPA), Composants, Guards, Services
```

## 🚀 Guide de Démarrage Rapide

Pour les recruteurs ou développeurs souhaitant tester l'application localement.

### 1. Prérequis
Vous devez disposer de **Java 21**, **Node.js 20+** et **PostgreSQL 14+**.

### 2. Base de données
```sql
CREATE DATABASE sygpress_db;
```
*(Si besoin, modifiez le fichier `sygpress-api/src/main/resources/application.yml` avec vos identifiants PostgreSQL).*

### 3. Lancement du Backend
```bash
cd sygpress-api
./mvnw spring-boot:run
```
> L'API REST est alors disponible sur `http://localhost:8080/api` (Swagger UI : http://localhost:8080/swagger-ui.html)

### 4. Lancement du Frontend
```bash
cd sygpress-app
npm install
npm start
```
> L'Interface est alors disponible sur `http://localhost:4200`


## 👤 Auteur

**Folly Sitou** - *Développeur Fullstack*
- [Mon Profil GitHub](https://github.com/davilla1993)
- [Me contacter sur LinkedIn](https://linkedin.com/in/follygbossou)
- [Voir mon Portefolio](https://gfolly.com)

---
*N'hésitez pas à parcourir le code source (notamment sous `sygpress-api/src/main/java/com/follysitou/sygpress/` pour la logique métier et `/sygpress-app/src/app/` pour la structure front) pour évaluer la qualité, la propreté et l'organisation du code.*
