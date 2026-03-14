# Plateforme SaaS de Gestion Omra

Backend Spring Boot 3 / Java 17 pour la gestion multi-tenant des agences Omra (pèlerins, groupes, vols, hôtels, documents, paiements, notifications).

## Stack

- **Framework:** Spring Boot 3.2, Java 17
- **Base de données:** PostgreSQL
- **Auth:** JWT + Refresh Token
- **Documentation API:** Swagger OpenAPI 3 (`/swagger-ui.html`)
- **Stockage fichiers:** MinIO (S3-compatible) ou stockage local
- **Optionnel:** Redis (cache)

## Structure

```
src/main/java/com/omra/platform/
├── config/          # JPA, Security, MinIO, Storage
├── controller/      # REST APIs
├── dto/
├── entity/          # JPA entities + enums
├── exception/       # GlobalExceptionHandler, erreurs métier
├── mapper/
├── repository/      # JPA repositories
├── security/         # JWT, RefreshToken, JwtAuthFilter, SecurityConfig
├── service/
└── util/            # TenantContext (multi-tenant)
```

## Démarrage rapide

### Prérequis

- Java 17+, Maven 3.8+
- PostgreSQL (ou utilisation du docker-compose)

### Lancer en local (sans Docker)

1. Créer la base PostgreSQL : `omra_db`
2. Optionnel : lancer MinIO (ou laisser le stockage local)
3. Configurer `application.yml` ou variables d’environnement (DB, JWT, storage)
4. Pour créer les tables automatiquement, lancer avec :  
   `-Dspring.jpa.hibernate.ddl-auto=update`  
   ou un profil `dev` avec `ddl-auto: update`
5. Démarrer l’application :

```bash
mvn spring-boot:run
```

- API : http://localhost:8080  
- Swagger : http://localhost:8080/swagger-ui.html  

### Lancer avec Docker

```bash
docker-compose up -d postgres minio
# Puis lancer l’app en local avec les variables pointant vers postgres/minio
# OU construire et lancer le backend dans Docker :
docker-compose up -d
```

Le `docker-compose` inclut : PostgreSQL, MinIO, Redis (optionnel), backend (build depuis le Dockerfile).

## Multi-tenant

- Toutes les entités métier ont un `agency_id`.
- Les requêtes sont filtrées par `TenantContext.getAgencyId()` (rempli depuis le JWT).
- Rôles : `SUPER_ADMIN`, `AGENCY_ADMIN`, `AGENCY_AGENT`, `PILGRIM`.

## APIs principales

| Module        | Exemples d’endpoints                          |
|---------------|------------------------------------------------|
| Auth          | `POST /api/auth/login`, `POST /api/auth/refresh`, `POST /api/auth/logout` |
| Agences       | `GET/POST /api/agencies`, `GET /api/agencies/theme`, `PUT /api/agencies/branding` |
| Pèlerins      | `GET/POST/PUT/DELETE /api/pilgrims` (pagination `?page=1&size=20`) |
| Groupes Omra  | `GET/POST/PUT /api/groups`, `POST /api/groups/{id}/pilgrims` |
| Vols          | `GET/POST/PUT /api/flights`                    |
| Hôtels        | `GET/POST /api/hotels`, `GET /api/hotels/groups/{groupId}` |
| Documents     | `GET/POST/DELETE /api/documents`               |
| Paiements     | `GET/POST/PUT/DELETE /api/payments`             |
| Notifications | `GET /api/notifications`, `POST /api/notifications/{id}/read` |
| Dashboard     | `GET /api/dashboard/stats`                     |
| Pèlerin       | `GET /api/pilgrim/dashboard`                   |
| Fichiers      | `POST /api/files/upload`                      |

## Sécurité

- Validation des entrées (Bean Validation / Hibernate Validator)
- Mots de passe hashés (bcrypt)
- JWT pour l’API, refresh token en base
- Filtrage multi-tenant par `agency_id` côté service

## Qualité

- DTO + mappers (MapStruct / manuels)
- Global Exception Handler
- Soft delete (`deleted_at`) sur les entités concernées
- Audit log (service dédié, à brancher sur les actions sensibles)
