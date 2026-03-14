package com.omra.platform.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "flight_seat_assignments", indexes = {
    @Index(name = "idx_flight_seat_assign_flight", columnList = "flight_id"),
    @Index(name = "idx_flight_seat_assign_pilgrim", columnList = "pilgrim_id")
}, uniqueConstraints = {
    @UniqueConstraint(columnNames = "flight_seat_id"),
    @UniqueConstraint(columnNames = {"flight_id", "pilgrim_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightSeatAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "flight_id", nullable = false)
    private Long flightId;

    @Column(name = "flight_seat_id", nullable = false)
    private Long flightSeatId;

    @Column(name = "pilgrim_id", nullable = false)
    private Long pilgrimId;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id", insertable = false, updatable = false)
    private Flight flight;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_seat_id", insertable = false, updatable = false)
    private FlightSeat flightSeat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pilgrim_id", insertable = false, updatable = false)
    private Pilgrim pilgrim;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
