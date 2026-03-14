# Stratégie domaine & relations — Omra Platform

Ce document décrit le **modèle domaine** (stratégie) et comment il est implémenté dans le backend, la base de données et le frontend.

---

## 1. Modèle cible (relations)

- **Agency** a plusieurs : Users, Trips/GroupTrips, Payments.
- **GroupTrip** a plusieurs : Trips (si on sépare Trip de GroupTrip).
- **Trip** (ou groupe de voyage) a plusieurs : Pilgrims, Payments, Rooms, RoomAssignments, FlightSeats, FlightSeatAssignments, BusSeats, BusSeatAssignments, Tasks, ActivityLogs, Notifications.
- **Task** a plusieurs : TaskItems (sous-tâches).
- **Pilgrim** a plusieurs : Documents, Payments, et peut avoir une Room assignée, un FlightSeat, un BusSeat.
- **Notifications** peuvent être liées à : Trip, GroupTrip, Pilgrim, ou Task.
- **ActivityLogs** enregistrent les actions des Users sur n’importe quelle entité.

---

## 2. Correspondance avec l’implémentation actuelle

Dans le code actuel, on n’a **pas** d’entité séparée « Trip » : un **groupe Omra** (`UmrahGroup`) joue le rôle de **groupe de voyage** (équivalent GroupTrip + Trip). Les relations sont les suivantes.

| Concept stratégie | Implémentation (tables / entités) |
|--------------------|-----------------------------------|
| **Agency** | `agencies` |
| Agency → Users | `users.agency_id` |
| Agency → GroupTrips / Trips | `umrah_groups.agency_id` (un groupe = un « trip ») |
| Agency → Payments | `payments.agency_id` |
| **GroupTrip / Trip** | **`umrah_groups`** (un groupe = un voyage) |
| Trip → Pilgrims | `group_pilgrims` (group_id, pilgrim_id) |
| Trip → Payments | `payments.group_id` |
| Trip → Rooms | `group_hotels` + `rooms` (hotel) + `group_room_assignments` |
| Trip → FlightSeats / Assignments | `flights.group_id`, `flight_seats`, `flight_seat_assignments` |
| Trip → BusSeats / Assignments | `group_bus_assignments`, `bus_seats`, `bus_seat_assignments` |
| Trip → Tasks | `tasks.group_id` (optionnel) |
| **Task** | `tasks` |
| Task → TaskItems | `subtasks` |
| **Pilgrim** | `pilgrims` |
| Pilgrim → Documents, Payments | `documents.pilgrim_id`, `payments.pilgrim_id` |
| Pilgrim → Room / FlightSeat / BusSeat | `group_room_assignments`, `flight_seat_assignments`, `bus_seat_assignments` |
| **Notifications** | `notifications` (user_id, agency_id, entity_type, entity_id pour lien optionnel) |
| **ActivityLogs** | `audit_logs` (user_id, agency_id, entity, entity_id) |

Donc la stratégie « Agency → GroupTrip → Trip → Pilgrims, Rooms, Flights, Bus, Tasks… » est bien en base et en backend ; le « Trip » du modèle cible est représenté par **UmrahGroup** (groupe Omra).

---

## 3. Où cette logique est visible

### 3.1 Backend (API)

- **Agence** : `GET/POST /api/agencies`, `GET /api/agencies/:id`.
- **Groupe (Trip)** : `GET/POST /api/groups`, `GET/PUT /api/groups/:id`, `GET/POST/DELETE /api/groups/:id/pilgrims`.
- **Vols / sièges** : `GET/POST /api/flights`, `GET/POST /api/flights/:id/seats`, `GET/POST /api/flights/:id/seat-assignments`.
- **Hôtels / chambres** : `GET/POST /api/hotels`, `GET /api/hotels/:id/rooms`, `GET/POST /api/group-room-assignments`, `GET /api/group-hotels/:id/room-assignments`.
- **Bus** : `GET/POST /api/buses`, `GET /api/buses/:id/seats`, `POST /api/buses/assign-group`.
- **Tâches** : `GET/POST /api/tasks` (filtre optionnel `groupId`), `GET/POST /api/tasks/:id/subtasks`.
- **Coûts voyage** : `GET/POST /api/groups/:id/trip-costs`.
- **Paiements** : `GET/POST /api/payments` (filtrés par agence ; `groupId` / `pilgrimId` dans le body).
- **Documents** : `GET/POST /api/documents` (optionnel `pilgrimId`, `groupId`).
- **Notifications** : `GET /api/notifications`, `GET /api/notifications/unread-count`.
- **Logs d’activité** : pas d’API publique dédiée ; utilisés en interne (AuditService).

La stratégie « tout tourne autour du groupe/trip » est donc exposée par ces routes (tout ce qui est lié à un groupe passe par `group_id` ou des sous-ressources `/groups/:id/...`).

### 3.2 Frontend (ce qu’on voit aujourd’hui)

- **Menu actuel** : entrées **par type de ressource** (Dashboard, Pèlerins, Groupes Omra, Vols, Hôtels, Documents, Paiements, Tâches, Bus, Notifications, Utilisateurs, Paramètres). Il n’y a **pas** d’entrée « Stratégie » ni de vue centrée sur un **groupe/trip**.
- **Liste des groupes** : on voit les groupes (nom, dates, capacité, statut) mais **pas** de page « Détail groupe » qui regroupe pèlerins, chambres, vols, bus, tâches, paiements pour ce groupe. Du coup la logique « Trip a beaucoup de X, Y, Z » n’est pas visible en un seul endroit dans l’UI.

C’est pour cela que la stratégie (relations Agency → GroupTrip → Trip → Pilgrims, Rooms, Flights, Bus, Tasks, etc.) **ne se voit pas** dans le front : il manque une **vue centrée sur le groupe/trip** et un lien explicite vers la doc de stratégie.

---

## 4. Ce qu’on a ajouté pour rendre la stratégie visible

1. **Document de stratégie** : ce fichier `docs/STRATEGY.md` (et référence dans `ARCHITECTURE.md`) pour avoir la stratégie écrite au même endroit que le reste de la doc.
2. **Page « Détail groupe »** (frontend) : à partir de la liste des groupes, clic sur un groupe → une page **Détail groupe** qui affiche :
   - Infos du groupe (nom, dates, capacité, prix, statut),
   - Liens / cartes vers :
     - Pèlerins du groupe,
     - Hôtels / chambres (assignations),
     - Vols / sièges,
     - Bus / sièges,
     - Tâches (filtrées par groupe),
     - Paiements (liés au groupe),
     - Coûts voyage (trip-costs).
   Ainsi, la logique « un Trip (groupe) a des Pilgrims, Rooms, FlightSeats, BusSeats, Tasks, Payments » devient **visible** dans l’interface.
3. **Référence dans l’app** : lien « Stratégie » ou « Modèle domaine » dans le menu (ex. Paramètres ou footer) vers ce document (ou une page d’aide qui le résume) pour que l’équipe voie où se trouve la stratégie.

---

## 5. Résumé

- **Stratégie** : Agency → GroupTrips → Trip → Pilgrims, Payments, Rooms, FlightSeats, BusSeats, Tasks, Notifications, ActivityLogs ; Task → TaskItems ; Pilgrim → Documents, Payments, Room/FlightSeat/BusSeat assignés.
- **Implémentation** : « Trip » = **UmrahGroup** ; les relations ci-dessus existent en base et en API (voir tableaux et routes ci-dessus).
- **Pourquoi on ne voyait pas la logique dans le front** : menu par type de ressource (Pèlerins, Vols, etc.) et **pas de vue « Détail groupe »** qui agrège tout. Avec la page Détail groupe et le lien vers ce doc, la stratégie devient visible et traçable.
