# Omra Platform — Structure & Cycle

**Stratégie domaine et relations (Agency → GroupTrip → Trip → Pilgrims, Rooms, Flights, Tasks, etc.)** : voir **[docs/STRATEGY.md](STRATEGY.md)**. Ce document décrit le modèle cible, la correspondance avec les tables/API, et pourquoi cette logique n’apparaît pas dans le menu actuel (menu par ressource) tant qu’il n’y a pas de page « Détail groupe » centrée sur le trip.

---

## 1. Project structure

### 1.1 Global layout

```
omra/
├── pom.xml                    # Backend (Spring Boot 3.2, Java 17)
├── src/main/java/com/omra/platform/   # Backend source
├── src/main/resources/        # application.yml, SQL scripts
├── omra-front/                # Frontend (Angular, standalone)
│   ├── src/app/
│   │   ├── auth/              # Login
│   │   ├── core/              # Services, guards, interceptors
│   │   ├── layout/            # Shell (menu, header)
│   │   ├── dashboard/
│   │   └── modules/           # pilgrims, groups, flights, hotels, agencies, users, etc.
│   └── angular.json
└── docs/
```

### 1.2 Backend (Spring Boot)

```
com.omra.platform
├── OmraPlatformApplication.java
├── config/                    # Startup & infra
│   ├── DefaultAdminInitializer.java    # Seed admin (superadmin@…)
│   ├── DefaultSuperAdminInitializer.java
│   ├── RefreshTokenSchemaFix.java      # DB fix for admin tokens
│   ├── MinioConfig.java, StorageProperties.java
│   └── DefaultAdminProperties.java, DefaultSuperAdminProperties.java
├── controller/                # REST API (HTTP → service)
│   ├── AuthController.java            # POST /api/auth/login, refresh, logout
│   ├── AdminAuthController.java       # POST /api/admin/auth/login, refresh, logout
│   ├── AgencyController.java
│   ├── UserController.java, PilgrimController.java
│   ├── UmrahGroupController.java, FlightController.java
│   ├── HotelController.java, DocumentController.java, PaymentController.java
│   ├── DashboardController.java, PilgrimDashboardController.java
│   ├── NotificationController.java, FileUploadController.java
│   └── ...
├── service/                   # Business logic (controller → repository)
│   ├── AuthService.java       # User login, refresh
│   ├── AdminAuthService.java  # Admin login, refresh
│   ├── AgencyService.java    # + creates default user on agency create
│   ├── UserService.java, PilgrimService.java
│   ├── UmrahGroupService.java, FlightService.java
│   ├── HotelService.java, DocumentService.java, PaymentService.java
│   ├── DashboardService.java, NotificationService.java
│   ├── StorageService.java, AuditService.java
│   └── ...
├── repository/                # JPA (service → database)
│   ├── UserRepository.java, AdminRepository.java
│   ├── AgencyRepository.java, PilgrimRepository.java
│   ├── UmrahGroupRepository.java, HotelRepository.java
│   ├── RefreshTokenRepository.java, ...
│   └── ...
├── entity/                     # JPA entities (tables)
│   ├── User.java, Admin.java, Agency.java
│   ├── Pilgrim.java, UmrahGroup.java, Flight.java
│   ├── Hotel.java, GroupHotel.java, Payment.java, Document.java
│   ├── Notification.java, AuditLog.java
│   ├── security/RefreshToken.java
│   └── enums/ (UserRole, AgencyStatus, GroupStatus, …)
├── dto/                        # Request/response objects
│   ├── AuthRequest.java, AuthResponse.java, AdminDto.java
│   ├── AgencyDto.java, UserDto.java, PilgrimDto.java
│   ├── PageResponse.java, ...
│   └── ...
├── mapper/                     # Entity ↔ DTO
│   ├── AgencyMapper.java, UserMapper.java, ...
├── security/
│   ├── SecurityConfig.java    # Public paths, JWT filter, CORS
│   ├── JwtAuthFilter.java     # Extract JWT → TenantContext + SecurityContext
│   ├── JwtService.java        # Generate/parse access & refresh tokens
│   └── JwtProperties.java
├── util/
│   └── TenantContext.java     # ThreadLocal: agencyId, userId, role, isAdmin
└── exception/
    └── GlobalExceptionHandler.java
```

### 1.3 Frontend (Angular)

```
omra-front/src/app/
├── main.ts, app.config.ts
├── app.routes.ts              # Routes + authGuard, adminGuard
├── auth/login/                # Login page (user or admin by email)
├── core/
│   ├── services/
│   │   ├── api.service.ts     # Base URL + endpoints (auth, adminAuth, agencies, …)
│   │   ├── auth.service.ts    # login(), logout(), refreshToken(), isAdmin(), user/admin signals
│   │   └── notification.service.ts
│   ├── guards/
│   │   ├── auth.guard.ts      # Redirect to /login if not logged in
│   │   └── admin.guard.ts     # Redirect to /dashboard if not admin (for /agencies)
│   └── interceptors/
│       └── auth.interceptor.ts  # Add Bearer token to requests
├── layout/                    # Toolbar + sidebar menu (different for admin vs agency)
├── dashboard/
├── modules/
│   ├── agencies/              # List, new, edit (admin only)
│   ├── pilgrims/, groups/, flights/, hotels/
│   ├── documents/, payments/, users/, notifications/
│   └── settings/
└── shared/components/        # page-header, create-placeholder, ...
```

---

## 2. Request cycle (one HTTP call)

### 2.1 Backend flow

```
HTTP Request (e.g. GET /api/pilgrims?page=1&size=20)
    │
    ▼
SecurityConfig
    ├── Public path? (e.g. /api/auth/login, /api/admin/auth/*) → no JWT
    └── Protected → must have valid JWT (next step)
    │
    ▼
JwtAuthFilter
    ├── Read "Authorization: Bearer <token>"
    ├── JwtService.validateToken(token) + parseAccessToken(token)
    ├── TenantContext.setUserId / setAgencyId / setUserRole / setAdminId (if admin)
    └── SecurityContextHolder.setAuthentication(ROLE_xxx)
    │
    ▼
Controller (e.g. PilgrimController.getPilgrims())
    └── Calls Service with params
    │
    ▼
Service (e.g. PilgrimService.getPilgrims())
    ├── TenantContext.getAgencyId() / isSuperAdmin() for access rules
    ├── Repository.findByAgencyId(...) or similar
    └── Returns DTOs (PageResponse, list, etc.)
    │
    ▼
Repository (JPA)
    └── Database query (PostgreSQL)
    │
    ▼
HTTP Response (JSON)
```

After the request, `TenantContext.clear()` is called (in filter `finally`) so the thread does not keep user/agency data.

### 2.2 Frontend flow (e.g. list pilgrims)

```
User opens /pilgrims
    │
    ▼
authGuard
    └── Not logged in? → redirect /login
    │
    ▼
Layout + pilgrims.routes → PilgrimListComponent
    │
    ▼
Component calls API (e.g. GET /api/pilgrims?page=1&size=20)
    │
    ▼
auth.interceptor
    └── Adds header: Authorization: Bearer <token from AuthService.getToken()>
    │
    ▼
Backend (cycle above) → JSON
    │
    ▼
Component fills table and paginator
```

---

## 3. Authentication cycles

### 3.1 Two types of accounts

| Type        | Table   | Login endpoint              | Use case                          |
|------------|---------|-----------------------------|-----------------------------------|
| Platform   | `admins`| `POST /api/admin/auth/login`| Create agencies, activate/deactivate |
| Agency     | `users` | `POST /api/auth/login`      | Work inside one agency (pilgrims, groups, …) |

### 3.2 Agency user login cycle

```
Frontend: email + password (no "superadmin" in email)
    │
    ▼
POST /api/auth/login
    │
    ▼
AuthService (backend)
    ├── UserRepository.findByEmailAndDeletedAtIsNull(email)
    ├── PasswordEncoder.matches(password, user.password)
    ├── Revoke old refresh tokens for this user
    ├── JwtService.generateAccessToken(userId, agencyId, email, role)
    ├── Create refresh token (table refresh_tokens, user_id)
    └── Return AuthResponse(accessToken, refreshToken, user, agency)
    │
    ▼
Frontend: store tokens + user + agency in localStorage; redirect /dashboard
```

### 3.3 Platform admin login cycle

```
Frontend: email containing "superadmin" (e.g. superadmin@omra.local)
    │
    ▼
POST /api/admin/auth/login
    │
    ▼
AdminAuthService (backend)
    ├── AdminRepository.findByEmail(email)
    ├── PasswordEncoder.matches(password, admin.password)
    ├── Revoke old refresh tokens for this admin
    ├── JwtService.generateAccessTokenForAdmin(adminId, email)  → role SUPER_ADMIN in JWT
    ├── Create refresh token (refresh_tokens, admin_id, user_id=null)
    └── Return AuthResponse(accessToken, refreshToken, admin)
    │
    ▼
Frontend: store tokens + admin; redirect /agencies; menu shows only Dashboard + Agencies
```

### 3.4 JWT content and TenantContext

- **User token**: `subject=userId`, claims: `agencyId`, `email`, `role` (e.g. AGENCY_ADMIN).
- **Admin token**: `subject=admin:<id>`, claims: `admin=true`, `adminId`, `email`, `role=SUPER_ADMIN`.

On each request, `JwtAuthFilter` parses the token and fills **TenantContext** (thread-local):

- User: `userId`, `agencyId`, `userRole`.
- Admin: `adminId`, `userRole=SUPER_ADMIN`, no `agencyId` (unless you add it later).

Services then use:

- `TenantContext.getAgencyId()` → filter data by agency.
- `TenantContext.isSuperAdmin()` → allow listing/editing all agencies (and create agency).
- `TenantContext.isAdmin()` → request comes from platform admin (table `admins`).

---

## 4. Multi-tenant (agency) cycle

- **One agency** = one tenant. Data is scoped by `agency_id` (or equivalent).
- **Pilgrims, groups, flights, payments, documents, users, hotels** are linked to an agency (`agency_id`).
- **Flow**:
  1. User logs in → JWT contains `agencyId`.
  2. `JwtAuthFilter` sets `TenantContext.setAgencyId(agencyId)`.
  3. Services use `TenantContext.getAgencyId()` to:
     - **Read**: only entities where `agency_id = getAgencyId()` (or super admin sees all when allowed).
     - **Write**: set `entity.setAgencyId(TenantContext.getAgencyId())` on create.
  4. Super admin (or platform admin) has no agency in token; services treat them via `isSuperAdmin()` (e.g. list all agencies, create agency, activate/deactivate).

---

## 5. Main business cycles

### 5.1 Create agency (platform admin)

```
Admin logged in → POST /api/agencies (body: name, email, …)
    │
    ▼
AgencyService.create()
    ├── Check TenantContext.isSuperAdmin()
    ├── Save Agency
    ├── If no user with agency.getEmail(): create User(agencyId, email=agency.email, name="admin", password="000000", role=AGENCY_ADMIN)
    └── Return AgencyDto
```

First login for that agency: **email = agency email**, **password = 000000** via normal `/api/auth/login`.

### 5.2 Agency user: list/create pilgrims, groups, flights, hotels, payments, documents

- **List**: Service uses `TenantContext.getAgencyId()` (or `isSuperAdmin()`) and repository methods like `findByAgencyId(agencyId, pageable)`.
- **Create**: Service sets `entity.setAgencyId(TenantContext.getAgencyId())` then save.
- **Update/Delete**: Service loads entity and checks that `entity.getAgencyId()` matches `TenantContext.getAgencyId()` (or super admin).

### 5.3 Hotel and group

- **Hotel** has `agency_id`; list/create filtered by agency. **GroupHotel** links a group to a hotel; service checks that group and hotel belong to the same agency when assigning.

---

## 6. Summary diagram

```
                    ┌─────────────────────────────────────────────────────────┐
                    │                     FRONTEND (Angular)                     │
                    │  Login → AuthService (user or admin) → store token        │
                    │  All API calls: Authorization: Bearer <token>             │
                    └───────────────────────────┬──────────────────────────────┘
                                                │ HTTP
                    ┌───────────────────────────▼──────────────────────────────┐
                    │                   BACKEND (Spring Boot)                    │
                    │  SecurityConfig → JwtAuthFilter → TenantContext + Security │
                    │  Controller → Service → Repository → PostgreSQL            │
                    └──────────────────────────────────────────────────────────┘
```

- **Platform admin**: table `admins`, login `/api/admin/auth/login`, menu: Dashboard + Agencies (list/create/edit/activate).
- **Agency**: table `agencies`; each has **users** (roles: Admin, Service, Pilgrimage companion, Pilgrim), **groups**, **hotels**, **flights**, **payments**, **documents**, **pilgrims**; palette in `agencies` (theme/colors).
- **Default agency user** when creating an agency: email = agency email, name = "admin", password = "000000", role = Admin (AGENCY_ADMIN).

This is how the project structure and the main cycles (request, auth, multi-tenant, agency creation, and data access) work end to end.
