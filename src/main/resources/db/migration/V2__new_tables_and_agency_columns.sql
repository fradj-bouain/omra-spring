-- Add agency_id to notifications and audit_logs (multi-tenant)
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS agency_id BIGINT REFERENCES agencies(id) ON DELETE SET NULL;
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS agency_id BIGINT REFERENCES agencies(id) ON DELETE SET NULL;
CREATE INDEX IF NOT EXISTS idx_notification_agency_id ON notifications(agency_id);
CREATE INDEX IF NOT EXISTS idx_audit_agency_id ON audit_logs(agency_id);

-- Optional: entity reference for notification deep links
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS entity_type VARCHAR(64);
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS entity_id VARCHAR(64);

-- rooms (per hotel)
CREATE TABLE IF NOT EXISTS rooms (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL REFERENCES hotels(id) ON DELETE CASCADE,
    room_number VARCHAR(32) NOT NULL,
    capacity INT NOT NULL DEFAULT 2,
    room_type VARCHAR(64),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    UNIQUE(hotel_id, room_number)
);
CREATE INDEX IF NOT EXISTS idx_room_hotel_id ON rooms(hotel_id);

-- group_room_assignments (pilgrim assigned to room for a group_hotel)
CREATE TABLE IF NOT EXISTS group_room_assignments (
    id BIGSERIAL PRIMARY KEY,
    group_hotel_id BIGINT NOT NULL REFERENCES group_hotels(id) ON DELETE CASCADE,
    room_id BIGINT NOT NULL REFERENCES rooms(id) ON DELETE CASCADE,
    pilgrim_id BIGINT NOT NULL REFERENCES pilgrims(id) ON DELETE RESTRICT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(room_id, pilgrim_id),
    UNIQUE(group_hotel_id, pilgrim_id)
);
CREATE INDEX IF NOT EXISTS idx_group_room_assign_group_hotel ON group_room_assignments(group_hotel_id);
CREATE INDEX IF NOT EXISTS idx_group_room_assign_pilgrim ON group_room_assignments(pilgrim_id);

-- flight_seats
CREATE TABLE IF NOT EXISTS flight_seats (
    id BIGSERIAL PRIMARY KEY,
    flight_id BIGINT NOT NULL REFERENCES flights(id) ON DELETE CASCADE,
    seat_number VARCHAR(16) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'AVAILABLE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(flight_id, seat_number)
);
CREATE INDEX IF NOT EXISTS idx_flight_seat_flight_id ON flight_seats(flight_id);

-- flight_seat_assignments (flight_id denormalized for unique constraint)
CREATE TABLE IF NOT EXISTS flight_seat_assignments (
    id BIGSERIAL PRIMARY KEY,
    flight_id BIGINT NOT NULL REFERENCES flights(id) ON DELETE CASCADE,
    flight_seat_id BIGINT NOT NULL REFERENCES flight_seats(id) ON DELETE CASCADE,
    pilgrim_id BIGINT NOT NULL REFERENCES pilgrims(id) ON DELETE RESTRICT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(flight_seat_id),
    UNIQUE(flight_id, pilgrim_id)
);
CREATE INDEX IF NOT EXISTS idx_flight_seat_assign_flight ON flight_seat_assignments(flight_id);
CREATE INDEX IF NOT EXISTS idx_flight_seat_assign_pilgrim ON flight_seat_assignments(pilgrim_id);

-- buses
CREATE TABLE IF NOT EXISTS buses (
    id BIGSERIAL PRIMARY KEY,
    agency_id BIGINT NOT NULL REFERENCES agencies(id) ON DELETE CASCADE,
    plate VARCHAR(32) NOT NULL,
    capacity INT NOT NULL DEFAULT 50,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ
);
CREATE INDEX IF NOT EXISTS idx_bus_agency_id ON buses(agency_id);

-- bus_seats
CREATE TABLE IF NOT EXISTS bus_seats (
    id BIGSERIAL PRIMARY KEY,
    bus_id BIGINT NOT NULL REFERENCES buses(id) ON DELETE CASCADE,
    seat_number VARCHAR(16) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(bus_id, seat_number)
);
CREATE INDEX IF NOT EXISTS idx_bus_seat_bus_id ON bus_seats(bus_id);

-- group_bus_assignments (which bus serves which group)
CREATE TABLE IF NOT EXISTS group_bus_assignments (
    id BIGSERIAL PRIMARY KEY,
    group_id BIGINT NOT NULL REFERENCES umrah_groups(id) ON DELETE CASCADE,
    bus_id BIGINT NOT NULL REFERENCES buses(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(group_id, bus_id)
);
CREATE INDEX IF NOT EXISTS idx_group_bus_group ON group_bus_assignments(group_id);

-- bus_seat_assignments (pilgrim assigned to bus seat for a group)
CREATE TABLE IF NOT EXISTS bus_seat_assignments (
    id BIGSERIAL PRIMARY KEY,
    group_bus_assignment_id BIGINT NOT NULL REFERENCES group_bus_assignments(id) ON DELETE CASCADE,
    bus_seat_id BIGINT NOT NULL REFERENCES bus_seats(id) ON DELETE CASCADE,
    pilgrim_id BIGINT NOT NULL REFERENCES pilgrims(id) ON DELETE RESTRICT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(group_bus_assignment_id, bus_seat_id),
    UNIQUE(group_bus_assignment_id, pilgrim_id)
);
CREATE INDEX IF NOT EXISTS idx_bus_seat_assign_pilgrim ON bus_seat_assignments(pilgrim_id);

-- tasks
CREATE TABLE IF NOT EXISTS tasks (
    id BIGSERIAL PRIMARY KEY,
    agency_id BIGINT NOT NULL REFERENCES agencies(id) ON DELETE CASCADE,
    group_id BIGINT REFERENCES umrah_groups(id) ON DELETE SET NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    due_date DATE,
    status VARCHAR(32) NOT NULL DEFAULT 'TODO',
    assigned_to_user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ
);
CREATE INDEX IF NOT EXISTS idx_task_agency ON tasks(agency_id);
CREATE INDEX IF NOT EXISTS idx_task_group ON tasks(group_id);
CREATE INDEX IF NOT EXISTS idx_task_assigned ON tasks(assigned_to_user_id);

-- subtasks
CREATE TABLE IF NOT EXISTS subtasks (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_subtask_task ON subtasks(task_id);

-- trip_cost_items (cost breakdown per group)
CREATE TABLE IF NOT EXISTS trip_cost_items (
    id BIGSERIAL PRIMARY KEY,
    group_id BIGINT NOT NULL REFERENCES umrah_groups(id) ON DELETE CASCADE,
    type VARCHAR(64) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    currency VARCHAR(8) NOT NULL DEFAULT 'MAD',
    description VARCHAR(512),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ
);
CREATE INDEX IF NOT EXISTS idx_trip_cost_group ON trip_cost_items(group_id);
