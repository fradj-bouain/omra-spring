# Omra/Hajj Management SaaS — Full Analysis & Recommendations

**Scope:** Backend (Spring Boot), Frontend (Angular), Database (PostgreSQL via JPA).  
**No code modified** — analysis and structured proposals only.

---

## 1. Current Project Structure Overview

### 1.1 Repository layout

```
omra/
├── pom.xml                          # Spring Boot 3.2, Java 17
├── src/main/java/com/omra/platform/
│   ├── config/                      # 8 files (initializers, MinIO, properties)
│   ├── controller/                  # 14 REST controllers
│   ├── service/                     # 15 services
│   ├── repository/                  # 14 JPA repositories
│   ├── entity/                      # 13 entities + enums
│   ├── dto/                         # 15+ DTOs
│   ├── mapper/                      # 2 mappers (Agency, User)
│   ├── security/                   # JWT, filter, RefreshToken
│   ├── util/                        # TenantContext
│   └── exception/                   # GlobalExceptionHandler
├── src/main/resources/
│   ├── application.yml
│   └── db/refresh_tokens_fix.sql
└── omra-front/                     # Angular (standalone)
    └── src/app/
        ├── auth/login/
        ├── core/                    # services, guards, interceptors
        ├── layout/
        ├── dashboard/
        ├── modules/                 # 10 feature modules (list + form each)
        └── shared/components/
```

---

## 2. Entities (Tables/Models) — Complete List

| # | Table (Entity)     | Purpose |
|---|--------------------|--------|
| 1 | **agencies**       | Tenant: agency name, email, theme/palette, subscription, status |
| 2 | **admins**         | Platform admins (create agencies, activate/deactivate); separate from users |
| 3 | **users**          | Agency users (admin, service, pilgrimage companion, pilgrim); link to agency_id |
| 4 | **refresh_tokens** | JWT refresh tokens (user_id or admin_id) |
| 5 | **pilgrims**       | Pilgrim profiles (agency_id, passport, visa, contact) |
| 6 | **umrah_groups**   | Group trips (agency_id, dates, capacity, price, status) |
| 7 | **group_pilgrims** | N:N group ↔ pilgrim (unique group_id, pilgrim_id) |
| 8 | **flights**        | Flight info (agency_id, optional group_id) |
| 9 | **hotels**         | Hotel master (agency_id, name, city, stars) |
| 10| **group_hotels**   | Assignment of hotel to group (check-in/out, roomType, city enum) |
| 11| **payments**       | Payments (agency_id, pilgrim_id, group_id, amount, status) |
| 12| **documents**      | Document records (agency_id, pilgrim_id, group_id, type, fileUrl) |
| 13| **notifications**  | In-app notifications (user_id, title, message, read) |
| 14| **audit_logs**     | Action log (user_id, action, entity, entity_id, timestamp) |

### 2.2 Enums

- **UserRole:** SUPER_ADMIN, AGENCY_ADMIN, AGENCY_AGENT, PILGRIM_COMPANION, PILGRIM  
- **UserStatus:** ACTIVE, DISABLED  
- **AgencyStatus:** ACTIVE, SUSPENDED, EXPIRED  
- **GroupStatus:** OPEN, CONFIRMED, CLOSED, COMPLETED  
- **VisaStatus:** PENDING, SUBMITTED, APPROVED, REJECTED  
- **PaymentMethod:** CASH, BANK_TRANSFER, CARD  
- **PaymentStatus:** PENDING, PAID, PARTIAL, REFUNDED  
- **DocumentType:** PASSPORT, VISA, FLIGHT_TICKET, CONTRACT, PROGRAM  
- **DocumentStatus:** UPLOADED, VERIFIED, REJECTED  
- **HotelCity:** (used in GroupHotel for city)  
- **NotificationChannel:** (exists in enums but usage not seen in Notification entity)

---

## 3. Relationships (Current)

- **Agency** 1 ──< **User** (users.agency_id → agencies.id); no FK in DB (JPA only via @ManyToOne read-only).
- **Agency** 1 ──< **Pilgrim** (pilgrims.agency_id).
- **Agency** 1 ──< **UmrahGroup** (umrah_groups.agency_id).
- **Agency** 1 ──< **Flight** (flights.agency_id).
- **Agency** 1 ──< **Hotel** (hotels.agency_id).
- **Agency** 1 ──< **Payment** (payments.agency_id).
- **Agency** 1 ──< **Document** (documents.agency_id).
- **UmrahGroup** 1 ──< **GroupPilgrim** N ──> **Pilgrim** (group_pilgrims.group_id, pilgrim_id).
- **UmrahGroup** 1 ──< **GroupHotel** N ──> **Hotel** (group_hotels.group_id, hotel_id).
- **Flight** N ──> **UmrahGroup** (optional flights.group_id).
- **Payment** N ──> **Pilgrim**, **UmrahGroup** (optional pilgrim_id, group_id).
- **Document** N ──> **Pilgrim**, **UmrahGroup** (optional pilgrim_id, group_id).
- **User** 1 ──< **Notification** (notifications.user_id).
- **User** 1 ──< **AuditLog** (audit_logs.user_id).
- **Admin** / **User** 1 ──< **RefreshToken** (refresh_tokens.admin_id or user_id).

**Note:** All FKs are logical (columns exist); JPA does not enforce DB-level foreign keys by default (ddl-auto: update). Many entities use `Long agencyId` without `@ManyToOne` to Agency, so referential integrity is not enforced at DB level.

---

## 4. Clean ERD Diagram (Text / Mermaid)

```mermaid
erDiagram
    agencies ||--o{ users : "has"
    agencies ||--o{ pilgrims : "has"
    agencies ||--o{ umrah_groups : "has"
    agencies ||--o{ flights : "has"
    agencies ||--o{ hotels : "has"
    agencies ||--o{ payments : "has"
    agencies ||--o{ documents : "has"

    umrah_groups ||--o{ group_pilgrims : "contains"
    pilgrims ||--o{ group_pilgrims : "member_of"
    umrah_groups ||--o{ group_hotels : "assigned"
    hotels ||--o{ group_hotels : "assigned_to"

    umrah_groups ||--o{ flights : "optional_link"
    pilgrims ||--o{ payments : "pays"
    umrah_groups ||--o{ payments : "for_trip"
    pilgrims ||--o{ documents : "has"
    umrah_groups ||--o{ documents : "for_trip"

    users ||--o{ notifications : "receives"
    users ||--o{ audit_logs : "performs"

    admins ||--o{ refresh_tokens : "admin"
    users ||--o{ refresh_tokens : "user"

    agencies {
        bigint id PK
        varchar name
        varchar email UK
        varchar phone
        varchar country city address
        varchar logoUrl faviconUrl
        varchar primaryColor secondaryColor menuColor buttonColor backgroundColor textColor
        varchar subscriptionPlan
        date subscriptionStartDate subscriptionEndDate
        enum status
        timestamp createdAt
    }

    users {
        bigint id PK
        bigint agency_id FK
        bigint pilgrim_id FK
        varchar name
        varchar email UK
        varchar password
        enum role
        enum status
        timestamp createdAt
        timestamp deleted_at
    }

    pilgrims {
        bigint id PK
        bigint agency_id FK
        varchar firstName lastName
        varchar gender passportNumber nationality phone email address
        date dateOfBirth passportIssueDate passportExpiry
        varchar photoUrl passportScanUrl
        enum visaStatus
        timestamp createdAt deleted_at
    }

    umrah_groups {
        bigint id PK
        bigint agency_id FK
        varchar name description
        date departureDate returnDate
        int maxCapacity
        decimal price
        enum status
        timestamp createdAt deleted_at
    }

    group_pilgrims {
        bigint id PK
        bigint group_id FK
        bigint pilgrim_id FK
    }

    flights {
        bigint id PK
        bigint agency_id FK
        bigint group_id FK
        varchar airline flightNumber departureCity arrivalCity terminal gate
        datetime departureTime arrivalTime
        timestamp createdAt deleted_at
    }

    hotels {
        bigint id PK
        bigint agency_id FK
        varchar name city address contactPhone
        int stars
    }

    group_hotels {
        bigint id PK
        bigint group_id FK
        bigint hotel_id FK
        enum city
        date checkIn checkOut
        varchar roomType
    }

    payments {
        bigint id PK
        bigint agency_id FK
        bigint pilgrim_id FK
        bigint group_id FK
        decimal amount
        varchar currency
        enum paymentMethod status
        date paymentDate
        varchar reference
        timestamp createdAt deleted_at
    }

    documents {
        bigint id PK
        bigint agency_id FK
        bigint pilgrim_id FK
        bigint group_id FK
        enum type status
        varchar fileUrl
        timestamp createdAt deleted_at
    }

    notifications {
        bigint id PK
        bigint user_id FK
        varchar title
        text message
        varchar type channel
        boolean read
        timestamp createdAt
    }

    audit_logs {
        bigint id PK
        bigint user_id FK
        varchar action entity entityId
        timestamp timestamp
    }

    admins {
        bigint id PK
        varchar username UK email UK password
        varchar telephone cin
        boolean active
        timestamp createdAt
    }

    refresh_tokens {
        bigint id PK
        bigint user_id FK
        bigint admin_id FK
        varchar token UK
        timestamp expiryDate createdAt
    }
```

---

## 5. Recommended Database Schema (FKs & Normalization)

### 5.1 Add explicit foreign key constraints

So far the schema uses `agency_id`, `group_id`, etc. without DB-level FKs. Recommended:

- Add **referential constraints** for all `*_id` columns pointing to the parent table (e.g. `pilgrims.agency_id → agencies.id`, `group_pilgrims.group_id → umrah_groups.id`).
- Use **ON DELETE** policy per relation:
  - **agencies:** ON DELETE RESTRICT (or CASCADE only if you really want to delete all agency data).
  - **umrah_groups:** ON DELETE CASCADE for group_pilgrims, group_hotels; RESTRICT for payments/documents that reference the group.
  - **pilgrims:** ON DELETE RESTRICT for payments, documents, group_pilgrims.
  - **users:** ON DELETE SET NULL or RESTRICT for notifications, audit_logs.

### 5.2 Normalization and missing tables

- **Trips vs groups:** Current design uses **UmrahGroup** as the “trip”. If you need a higher-level “Trip” (e.g. one trip with multiple groups), add a **trips** table and make **umrah_groups** reference **trips** (trip_id). Otherwise the current model is consistent.
- **Rooms:** **GroupHotel** has `roomType` (string) but no **rooms** table (room number, capacity, price). Recommendation: add **rooms** (hotel_id, roomNumber, capacity, roomType) and optionally **group_room_assignments** (group_hotel_id, room_id, pilgrim_id for allocation).
- **Flight seats:** No **flight_seats** or seat allocation. Recommendation: add **flight_seats** (flight_id, seatNumber, status) and optionally **flight_seat_assignments** (flight_id, pilgrim_id or group_pilgrim_id, seat_id).
- **Bus / bus seats:** Not present. Recommendation: add **buses** (agency_id, plate, capacity) and **bus_seats** (bus_id, seat_number); optionally **group_bus_assignments** (group_id, bus_id) and **bus_seat_assignments** (pilgrim_id, bus_seat_id, trip/group context).
- **Trip costs:** No separate **trip_costs** table. **UmrahGroup** has a single `price`. For itemized costs (flight, hotel, visa, etc.), add **trip_cost_items** (group_id, type, amount, currency, description) or keep a JSON/details column on the group.
- **Tasks / subtasks:** Not present. Recommendation: add **tasks** (agency_id or group_id, title, dueDate, status, assigned_to user_id) and **subtasks** (task_id, title, completed).
- **Notifications:** **notifications** has `user_id` only; no `agency_id`. For multi-tenant filtering and “agency-wide” notifications, add **agency_id** and optionally **entity_type** + **entity_id** for link to pilgrim/group/payment.

### 5.3 Audit log and multi-tenant

- **audit_logs** has **user_id** but no **agency_id**. Recommendation: add **agency_id** (nullable for platform admin actions) and index (agency_id, timestamp) so each agency can query its own audit trail.

### 5.4 Indexes (already partially present)

Keep/ensure indexes on:

- All foreign keys: `agency_id`, `group_id`, `pilgrim_id`, `user_id`, etc.
- **pilgrims:** (agency_id, deleted_at), (passport_number), (email).
- **payments:** (agency_id, status), (pilgrim_id), (group_id).
- **notifications:** (user_id, read), (created_at).
- **audit_logs:** (user_id), (agency_id if added), (entity, entity_id), (timestamp).

---

## 6. Missing Parts & Inconsistencies (By Area)

### 6.1 Pilgrims management

- **Present:** CRUD, agency-scoped, passport/visa fields, soft delete.
- **Missing / weak:**
  - No duplicate check by passport within agency (repository has `existsByAgencyIdAndPassportNumberAndDeletedAtIsNull` but it may not be used in service).
  - No “assign pilgrim to group” from pilgrim side (only group side: add pilgrim to group).
  - Pilgrim list/detail do not show “groups” or “payments” in a structured way in API (could be enriched DTOs or separate endpoints).

### 6.2 Trips & group trips

- **Present:** UmrahGroup = group trip; GroupPilgrim links pilgrims to groups; capacity and price on group.
- **Missing:**
  - No explicit “trip” entity above group (if one trip = one group, current model is fine).
  - No “trip cost breakdown” (flight, hotel, visa) — only one price per group.
  - No seat/room allocation at group level (see Rooms, Flights, Bus below).

### 6.3 Rooms, flights, bus seats

- **Rooms:** Only **GroupHotel** (group–hotel with checkIn/checkOut, roomType). No **rooms** table, no room-level allocation or capacity.
- **Flights:** **Flight** has no seats; no **flight_seats** or assignment to pilgrims.
- **Bus / bus seats:** Not implemented.

### 6.4 Payments & trip costs

- **Present:** Payment (amount, currency, method, status, pilgrim_id, group_id); Dashboard uses sum of PAID by agency.
- **Missing:**
  - No link from **UmrahGroup.price** to **Payment** (no “trip package price” vs “payments against this trip”).
  - No **trip_cost_items** or cost breakdown per group.
  - No “payment plan” or installments per pilgrim/group.

### 6.5 Tasks & subtasks

- **Missing:** No **tasks** or **subtasks** entities or APIs (e.g. visa follow-up, document collection).

### 6.6 Activity logs

- **Present:** **AuditLog** (user_id, action, entity, entity_id, timestamp); **AuditService.log()** is async.
- **Missing:**
  - No **agency_id** in audit_logs → cannot filter by tenant.
  - Audit is not called consistently from all controllers/services (only where explicitly used).

### 6.7 Notifications

- **Present:** **Notification** (user_id, title, message, type, channel, read); API to list, count unread, mark read.
- **Missing:**
  - **No producer:** Nothing in the codebase creates notifications (e.g. on payment received, visa status change, document upload).
  - No **agency_id** → cannot do “agency-wide” or “group” notifications.
  - No link to entity (e.g. payment_id, pilgrim_id) for deep links.

### 6.8 Dashboards & KPIs

- **Present:** **DashboardService** returns totalPilgrims, activeGroups, pendingVisas, paymentsReceived, totalRevenue (agency-scoped; super admin gets global pilgrims count).
- **Missing:**
  - No KPIs per group (e.g. filled capacity, revenue per group).
  - No charts (e.g. payments over time, visa status distribution) — backend only returns one stats object.
  - Pilgrim dashboard (**PilgrimDashboardController**) exists but scope/usage not fully traced here.

---

## 7. Backend Structure — Current vs Recommended

### 7.1 Current structure (flat)

- **config**, **controller**, **service**, **repository**, **entity**, **dto**, **mapper**, **security**, **util**, **exception** under one package.
- Single “module” in terms of packaging; no domain-based split.

### 7.2 Recommended modularization (by domain)

Keep one deployable app but group by domain for clarity and future scalability:

```
com.omra.platform
├── OmraPlatformApplication.java
├── config/                    # Unchanged: global config, initializers, security
├── security/                  # Unchanged: JWT, filter, RefreshToken
├── util/                      # Unchanged: TenantContext
├── exception/                 # Unchanged: GlobalExceptionHandler
│
├── domain/
│   ├── agency/
│   │   ├── AgencyController.java
│   │   ├── AgencyService.java
│   │   ├── AgencyRepository.java
│   │   ├── entity/Agency.java (or keep entity in single folder)
│   │   └── dto/AgencyDto.java, AgencyThemeDto.java
│   ├── auth/
│   │   ├── AuthController.java, AdminAuthController.java
│   │   ├── AuthService.java, AdminAuthService.java
│   │   └── ...
│   ├── user/
│   ├── pilgrim/
│   ├── group/                 # UmrahGroup, GroupPilgrim
│   ├── flight/
│   ├── hotel/                 # Hotel, GroupHotel
│   ├── payment/
│   ├── document/
│   ├── notification/
│   └── audit/
│
├── entity/                    # Optional: keep all entities here for shared use
├── dto/                       # Or split per domain
└── mapper/
```

**Explanation:**

- **config:** Global (DB, JWT, CORS, initializers).
- **security:** Cross-cutting (filter, token generation).
- **domain/*:** Each domain has its controller(s), service(s), repository (or references to shared repository). Entities can stay in a single **entity/** folder to avoid circular dependencies, or move under each domain if you prefer.
- **Routes:** Keep REST under `/api/...`; no change needed for URLs.

**Alternative (lighter):** Keep current flat structure but add **subpackages** under **service** and **controller** (e.g. `controller/agency`, `service/agency`) and keep **repository** and **entity** flat. This improves readability without a full domain split.

---

## 8. Frontend Structure — Current vs Recommended

### 8.1 Current structure

- **auth/login** — single login (user or admin by email).
- **core** — api.service, auth.service, guards (auth, admin), interceptor (token).
- **layout** — toolbar, sidebar, menu (admin vs agency).
- **dashboard** — one dashboard (stats cards).
- **modules/** — one folder per feature (pilgrims, groups, flights, hotels, documents, payments, users, notifications, agencies, settings); each has list + form components and routes.
- **shared/components** — page-header, create-placeholder.

### 8.2 Recommended structure (clarity & UX)

- **Keep:** core, layout, auth, dashboard, shared.
- **Group by domain (optional):**
  - **features/** or **modules/** with clear naming: `pilgrims`, `groups`, `flights`, `hotels`, `payments`, `documents`, `users`, `notifications`, `agencies`, `settings`.
  - Each feature: `*list.component`, `*form.component`, `*.routes.ts`; optionally `*detail.component` and `*service.ts` for complex logic.
- **Shared:**
  - Reusable UI: tables (with pagination), form controls, dialogs, pipes (e.g. role label, date).
  - Consider a **data** or **api** folder for type definitions and API client wrappers per domain.
- **UX:**
  - **Pilgrims:** Link to “groups” and “payments” from pilgrim detail or list row (e.g. “View groups” / “View payments”).
  - **Groups:** Show assigned pilgrims count, flights, hotels, and total paid vs price (if you add these to API).
  - **Dashboard:** Add charts (payments over time, visa status) — backend can add endpoints for chart data.
  - **Notifications:** Real-time or polling for unread count; list page with “mark all read”; optional deep link to entity (payment, pilgrim, group).

---

## 9. Multi-Tenant Handling (Agencies)

- **Current:** TenantContext (agencyId, userId, role) from JWT; services use `TenantContext.getAgencyId()` and `isSuperAdmin()` to scope queries and enforce access.
- **Gaps:**
  - **AuditLog:** No agency_id → cannot filter by tenant.
  - **Notification:** No agency_id → cannot list “agency notifications” or scope creation by agency.
  - **Consistency:** All agency-scoped entities (pilgrims, groups, flights, hotels, payments, documents) already use agency_id; only audit and notifications need alignment.

**Recommendation:**

- Add **agency_id** to **audit_logs** and **notifications** (nullable where appropriate).
- When creating notifications, set **agency_id** from the context (e.g. from the entity being notified about).
- When logging audit, set **agency_id** from TenantContext.getAgencyId() (null for platform admin).

---

## 10. Feature Completeness Summary

| Feature                 | Status        | Suggestion |
|-------------------------|---------------|------------|
| Pilgrim CRUD            | Done          | Add duplicate passport check; enrich with groups/payments in API if needed. |
| Group trips (UmrahGroup)| Done          | Optional: trip cost breakdown (trip_cost_items or similar). |
| Group–pilgrim assignment| Done          | — |
| Flights                 | Basic         | Add flight_seats + assignment to pilgrims/groups. |
| Hotels                  | Basic         | Add rooms table + room allocation to group/pilgrim. |
| Group–hotel assignment  | Done          | — |
| Payments                | Done          | Link to “trip price” and optional payment plan. |
| Trip costs              | Partial (price on group) | Add cost breakdown (items or JSON). |
| Documents               | Done          | — |
| Notifications           | Read-only     | Add producers (payment, visa, document); add agency_id and entity link. |
| Activity logs           | Partial       | Add agency_id; call audit from critical operations. |
| Dashboard & KPIs        | Basic stats   | Add per-group KPIs and chart data endpoints. |
| Tasks & subtasks        | Missing       | New entities + API + UI. |
| Bus & bus seats         | Missing       | New entities + API + UI. |
| Seat allocation (flight)| Missing       | flight_seats + assignments. |
| Room allocation         | Missing       | rooms + assignments. |

---

## 11. Recommended Database Schema (FKs Summary)

Add the following constraints (conceptually; implement via migration or ddl):

- **users.**agency_id → agencies.id (RESTRICT or CASCADE by design)
- **pilgrims.**agency_id → agencies.id
- **umrah_groups.**agency_id → agencies.id
- **flights.**agency_id → agencies.id, **flights.**group_id → umrah_groups.id
- **hotels.**agency_id → agencies.id
- **group_pilgrims.**group_id → umrah_groups.id, **group_pilgrims.**pilgrim_id → pilgrims.id
- **group_hotels.**group_id → umrah_groups.id, **group_hotels.**hotel_id → hotels.id
- **payments.**agency_id → agencies.id, pilgrim_id → pilgrims.id, group_id → umrah_groups.id
- **documents.**agency_id → agencies.id, pilgrim_id → pilgrims.id, group_id → umrah_groups.id
- **notifications.**user_id → users.id (and add agency_id → agencies.id)
- **audit_logs.**user_id → users.id (and add agency_id → agencies.id)
- **refresh_tokens.**user_id → users.id, admin_id → admins.id

Ensure unique constraints where needed (e.g. (group_id, pilgrim_id) on group_pilgrims — already present).

---

## 12. Document Summary

- **Section 2–3:** All current entities and relationships.
- **Section 4:** ERD in Mermaid (tables and relations).
- **Section 5:** Recommended schema: FKs, normalization, new tables (rooms, flight_seats, buses, bus_seats, tasks, subtasks, trip_cost_items), and audit/notification improvements.
- **Section 6:** Missing/inconsistent parts per area (pilgrims, trips, rooms, flights, bus, payments, costs, tasks, audit, notifications, dashboards).
- **Section 7–8:** Backend and frontend structure recommendations (modular/domain-oriented).
- **Section 9:** Multi-tenant improvements (agency_id in audit and notifications).
- **Section 10:** Feature completeness table.
- **Section 11:** FK summary for implementation.

This gives a single reference for any developer to implement fixes or extensions without modifying existing code in this step.
