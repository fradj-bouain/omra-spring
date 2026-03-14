package com.omra.platform.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "bus_seat_assignments", indexes = {
    @Index(name = "idx_bus_seat_assign_pilgrim", columnList = "pilgrim_id")
}, uniqueConstraints = {
    @UniqueConstraint(columnNames = {"group_bus_assignment_id", "bus_seat_id"}),
    @UniqueConstraint(columnNames = {"group_bus_assignment_id", "pilgrim_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusSeatAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_bus_assignment_id", nullable = false)
    private Long groupBusAssignmentId;

    @Column(name = "bus_seat_id", nullable = false)
    private Long busSeatId;

    @Column(name = "pilgrim_id", nullable = false)
    private Long pilgrimId;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_bus_assignment_id", insertable = false, updatable = false)
    private GroupBusAssignment groupBusAssignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_seat_id", insertable = false, updatable = false)
    private BusSeat busSeat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pilgrim_id", insertable = false, updatable = false)
    private Pilgrim pilgrim;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
