# Step-by-Step Commit Plan — Omra Full Feature Implementation

Apply commits in order. Each commit is self-contained where possible.

---

## Phase 1: Database & entities

### Commit 1 — DB migration: new tables and columns
- **Files:** `src/main/resources/db/migration/V2__new_tables_and_agency_columns.sql`
- **Message:** `chore(db): add migration V2 – rooms, flight_seats, buses, tasks, trip_cost_items, agency_id on notifications/audit_logs`
- **Note:** Run migration manually or add Flyway to `pom.xml` and set `spring.jpa.hibernate.ddl-auto: validate` for production.

### Commit 2 — Enums: SeatStatus, TaskStatus, TripCostType
- **Files:** `entity/enums/SeatStatus.java`, `entity/enums/TaskStatus.java`, `entity/enums/TripCostType.java`
- **Message:** `feat(domain): add enums SeatStatus, TaskStatus, TripCostType`

### Commit 3 — Entities: Room, GroupRoomAssignment, FlightSeat, FlightSeatAssignment, Bus, BusSeat, GroupBusAssignment, BusSeatAssignment, Task, Subtask, TripCostItem
- **Files:** All new entity classes under `entity/`
- **Message:** `feat(domain): add entities Room, GroupRoomAssignment, FlightSeat, FlightSeatAssignment, Bus, BusSeat, GroupBusAssignment, BusSeatAssignment, Task, Subtask, TripCostItem`

### Commit 4 — Notifications & AuditLog: add agency_id and entity fields
- **Files:** `entity/Notification.java` (agencyId, entityType, entityId), `entity/AuditLog.java` (agencyId)
- **Message:** `feat(domain): add agency_id and entity link to Notification and AuditLog`

---

## Phase 2: Repositories & DTOs

### Commit 5 — Repositories for new entities
- **Files:** `RoomRepository`, `GroupRoomAssignmentRepository`, `FlightSeatRepository`, `FlightSeatAssignmentRepository`, `BusRepository`, `BusSeatRepository`, `GroupBusAssignmentRepository`, `BusSeatAssignmentRepository`, `TaskRepository`, `SubtaskRepository`, `TripCostItemRepository`
- **Message:** `feat(data): add repositories for rooms, flight seats, buses, tasks, trip cost items`

### Commit 6 — DTOs for new features and dashboard
- **Files:** `RoomDto`, `GroupRoomAssignmentDto`, `FlightSeatDto`, `FlightSeatAssignmentDto`, `BusDto`, `TaskDto`, `TripCostItemDto`, `DashboardGroupKpiDto`, `DashboardChartDto`; update `NotificationDto`
- **Message:** `feat(api): add DTOs for rooms, seats, buses, tasks, trip costs, dashboard KPIs/charts`

### Commit 7 — Payment & GroupPilgrim repository extensions
- **Files:** `PaymentRepository` (sumByGroupIdAndStatus, findByAgencyIdAndStatusAndPaymentDateBetween), `GroupPilgrimRepository` (countByGroupId), `UserRepository` (findByPilgrimIdAndDeletedAtIsNull)
- **Message:** `feat(data): extend Payment, GroupPilgrim, User repositories for dashboard and notifications`

---

## Phase 3: Services & notification producers

### Commit 8 — NotificationProducerService and wiring
- **Files:** `NotificationProducerService.java`; changes in `PaymentService`, `DocumentService`, `PilgrimService` to call notification producer
- **Message:** `feat(notifications): add notification producers for payment, document upload, visa status change`

### Commit 9 — AuditService: set agency_id
- **Files:** `AuditService.java`
- **Message:** `feat(audit): set agency_id on audit log entries`

### Commit 10 — Pilgrim duplicate passport check
- **Files:** `PilgrimRepository` (existsByAgencyIdAndPassportNumberAndDeletedAtIsNullAndIdNot), `PilgrimService` (create/update validation)
- **Message:** `feat(pilgrims): enforce duplicate passport number per agency`

### Commit 11 — Dashboard: group KPIs and chart data
- **Files:** `DashboardService.java`, `DashboardStatsDto` (unchanged), `DashboardController.java`; new DTOs already in Commit 6
- **Message:** `feat(dashboard): add group KPIs and chart data endpoints`

---

## Phase 4: New feature services & controllers

### Commit 12 — RoomService and RoomController
- **Files:** `RoomService.java`, `RoomController.java`
- **Message:** `feat(api): add rooms and group room assignment API`

### Commit 13 — FlightSeatService and FlightSeatController
- **Files:** `FlightSeatService.java`, `FlightSeatController.java`
- **Message:** `feat(api): add flight seat allocation API`

### Commit 14 — BusService and BusController
- **Files:** `BusService.java`, `BusController.java`
- **Message:** `feat(api): add buses and group bus assignment API`

### Commit 15 — TaskService and TaskController
- **Files:** `TaskService.java`, `TaskController.java`
- **Message:** `feat(api): add tasks and subtasks API`

### Commit 16 — TripCostItemService and TripCostItemController
- **Files:** `TripCostItemService.java`, `TripCostItemController.java`
- **Message:** `feat(api): add trip cost items API per group`

---

## Phase 5: Frontend (optional — implement after backend)

### Commit 17 — Frontend API service: new endpoints
- **Files:** `omra-front/src/app/core/services/api.service.ts` — add `rooms`, `flightSeats`, `buses`, `tasks`, `tripCosts`, `dashboard.groupKpis`, `dashboard.chartData`
- **Message:** `feat(front): add API endpoints for rooms, seats, buses, tasks, trip costs, dashboard charts`

### Commit 18 — Frontend: dashboard charts and group KPIs
- **Files:** New dashboard components or update existing to call `dashboard/group-kpis`, `dashboard/chart-data`; display tables/charts
- **Message:** `feat(front): dashboard group KPIs and chart data UI`

### Commit 19 — Frontend: rooms, flight seats, buses, tasks, trip costs modules
- **Files:** New feature modules under `modules/` (e.g. rooms, flight-seats, buses, tasks, trip-costs) with list/form and routing
- **Message:** `feat(front): add modules for rooms, flight seats, buses, tasks, trip costs`

### Commit 20 — Frontend: notifications deep link and multi-tenant
- **Files:** Use `entityType`/`entityId` from notification DTO for navigation; ensure all list components filter by agency (backend already enforces)
- **Message:** `feat(front): notification deep links and agency-scoped data`

---

## Phase 6: Backend refactor (optional)

### Commit 21 — Refactor backend to domain packages
- **Action:** Move controller/service/repository/dto into `domain/agency`, `domain/auth`, `domain/user`, `domain/pilgrim`, `domain/group`, `domain/flight`, `domain/hotel`, `domain/payment`, `domain/document`, `domain/notification`, `domain/audit`, etc. Keep `entity/` central or move per domain. Update all imports.
- **Message:** `refactor(backend): modularize into domain-based packages`

---

## Summary

| Phase | Commits | Description |
|-------|---------|-------------|
| 1     | 1–4     | DB migration, enums, new entities, Notification/AuditLog columns |
| 2     | 5–7     | Repositories, DTOs, repository extensions |
| 3     | 8–11    | Notification producers, audit agency_id, pilgrim check, dashboard KPIs/charts |
| 4     | 12–16   | Room, FlightSeat, Bus, Task, TripCostItem services and controllers |
| 5     | 17–20   | Frontend API, dashboard UI, new modules, notifications |
| 6     | 21      | Optional domain refactor |

**Data integrity:** Migration defines FKs with ON DELETE (CASCADE/RESTRICT/SET NULL) as in `V2__new_tables_and_agency_columns.sql`. No existing data is deleted; new columns on existing tables are nullable or have defaults where needed.
