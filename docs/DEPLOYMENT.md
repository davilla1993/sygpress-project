# Guide de Déploiement Coolify - SygPress

Ce guide explique comment déployer l'application SygPress (monorepo backend + frontend) sur un VPS via Coolify.

## 📋 Prérequis

### Sur le VPS
1. **PostgreSQL** installé et configuré (la BDD n'est PAS dockerisée)
2. **Coolify** installé et configuré
3. **Base de données créée** :
   ```sql
   CREATE DATABASE sygpress_db;
   CREATE USER sygpress_user WITH PASSWORD 'your_secure_password';
   GRANT ALL PRIVILEGES ON DATABASE sygpress_db TO sygpress_user;
   ```

### Sur GitHub
- Repository clonable par Coolify (public ou avec accès SSH configuré)

## 🚀 Configuration Coolify

### 1. Créer un nouveau projet dans Coolify

1. Connectez-vous à Coolify
2. Créer un nouveau projet
3. Sélectionnez **"Deploy from Git"**
4. Entrez l'URL du repository GitHub

### 2. Configuration du Build

Dans les paramètres de l'application Coolify :

- **Build Pack** : Dockerfile
- **Dockerfile Location** : `./Dockerfile` (à la racine du projet)
- **Port** : `8080`
- **Health Check Path** : `/actuator/health` (optionnel mais recommandé)

### 3. Variables d'Environnement

Configurez les variables d'environnement suivantes dans Coolify :

#### Configuration Base de Données (OBLIGATOIRE)
```env
DB_HOST=localhost
DB_PORT=5432
DB_NAME=sygpress_db
DB_USERNAME=sygpress_user
DB_PASSWORD=your_secure_password_here
```

#### Configuration JWT (OBLIGATOIRE)
```env
JWT_SECRET=your_jwt_secret_here_base64_encoded
JWT_EXPIRATION=86400000
```

> ⚠️ **Important** : Générez un nouveau secret JWT pour la production :
> ```bash
> openssl rand -base64 64
> ```

#### Configuration Serveur (OPTIONNEL)
```env
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=prod
```

#### URLs de l'Application (OBLIGATOIRE)
```env
APP_BASE_URL=https://votre-domaine.com
APP_SERVER_URL=https://votre-domaine.com
```

> 📌 **Note** :
> - `APP_BASE_URL` : URL publique de votre application (sans slash à la fin)
> - `APP_SERVER_URL` : URL du serveur API (en mode monorepo, identique à APP_BASE_URL)
> - Ces URLs sont utilisées pour générer des liens absolus (emails, PDFs, redirections, etc.)
> - En production, utilisez toujours HTTPS

#### Configuration Upload (OPTIONNEL)
```env
FILE_UPLOAD_DIR=/app/uploads
MAX_FILE_SIZE=10MB
MAX_REQUEST_SIZE=10MB
```

#### Configuration Hibernate (OPTIONNEL)
```env
HIBERNATE_DDL_AUTO=update
HIBERNATE_SHOW_SQL=false
HIBERNATE_FORMAT_SQL=false
```

#### Configuration Audit (OPTIONNEL)
```env
AUDIT_CLEANUP_ENABLED=true
AUDIT_RETENTION_DAYS=30
AUDIT_CLEANUP_CRON=0 0 2 1 * ?
```

### 4. Volumes Persistants (OPTIONNEL)

Si vous souhaitez persister les uploads entre les redéploiements :

- **Source** : Volume persistant Coolify
- **Destination** : `/app/uploads`

### 5. Network Configuration

- Assurez-vous que l'application peut accéder à PostgreSQL
- Si PostgreSQL est sur le même VPS mais pas dans le même réseau Docker, utilisez `host.docker.internal` comme DB_HOST

## 🔧 Structure du Projet

```
sygpress-project/
├── Dockerfile              # Build monorepo (backend + frontend)
├── .dockerignore          # Exclusions Docker
├── .env.example           # Template des variables d'environnement
├── DEPLOYMENT.md          # Ce fichier
├── sygpress-api/          # Backend Spring Boot
│   ├── pom.xml
│   └── src/
│       └── main/
│           └── resources/
│               ├── application.yml      # Config dev
│               └── application-prod.yml # Config prod (avec env vars)
└── sygpress-app/          # Frontend Angular
    ├── package.json
    └── src/
```

## 🏗️ Processus de Build

Le Dockerfile effectue les étapes suivantes :

1. **Stage 1** : Build du frontend Angular avec Node.js 20
2. **Stage 2** : Build du backend Spring Boot avec Maven
   - Les fichiers Angular buildés sont copiés dans `src/main/resources/static`
   - Spring Boot servira automatiquement ces fichiers statiques
3. **Stage 3** : Image de production finale avec JRE 21
   - Copie du JAR Spring Boot
   - Configuration du healthcheck
   - Exposition du port 8080

## 🌐 Accès à l'Application

Une fois déployée :

- **Application complète** : `http://votre-domaine.com/`
- **API Backend** : `http://votre-domaine.com/api/*`
- **Swagger UI** : `http://votre-domaine.com/swagger-ui.html`
- **Health Check** : `http://votre-domaine.com/actuator/health`

## 🔒 Sécurité

### Avant le déploiement en production :

1. ✅ Changez le `JWT_SECRET` (ne pas utiliser celui par défaut)
2. ✅ Utilisez un mot de passe PostgreSQL fort
3. ✅ Configurez HTTPS via Coolify (Let's Encrypt)
4. ✅ Activez le firewall sur le VPS
5. ✅ Limitez l'accès SSH
6. ✅ Configurez des sauvegardes régulières de la BDD

### Variables sensibles à ne JAMAIS commiter :
- `DB_PASSWORD`
- `JWT_SECRET`
- Tout fichier `.env` avec des vraies valeurs

## 🐛 Dépannage

### L'application ne démarre pas
1. Vérifiez les logs dans Coolify
2. Vérifiez que PostgreSQL est accessible depuis le container
3. Vérifiez que toutes les variables d'environnement sont définies

### Erreur de connexion BDD
```bash
# Testez la connexion depuis le container
psql -h $DB_HOST -p $DB_PORT -U $DB_USERNAME -d $DB_NAME
```

### Problème de mémoire
Ajoutez des options JVM dans Coolify :
```env
JAVA_OPTS=-Xmx512m -Xms256m
```

Modifiez le ENTRYPOINT du Dockerfile si nécessaire :
```dockerfile
ENTRYPOINT ["java", "-Xmx512m", "-Xms256m", "-jar", "app.jar"]
```

## 📝 Notes Importantes

1. **Build Time** : Le premier build peut prendre 5-10 minutes (téléchargement des dépendances)
2. **Rebuild** : Les builds suivants seront plus rapides grâce au cache Docker
3. **Base de données** : N'oubliez pas de créer la BDD avant le premier déploiement
4. **Migrations** : Hibernate est configuré en mode `update` - il créera/mettra à jour les tables automatiquement

## 🔄 Mise à Jour

Pour déployer une nouvelle version :
1. Push le code sur la branche configurée dans Coolify
2. Coolify détectera automatiquement les changements
3. Un nouveau build sera déclenché automatiquement

Ou manuellement dans Coolify :
- Cliquez sur "Redeploy" dans l'interface Coolify

## 📞 Support

Pour toute question ou problème :
1. Consultez les logs dans Coolify
2. Vérifiez la configuration des variables d'environnement
3. Testez la connexion à la base de données
