package com.omra.platform.entity;

import com.omra.platform.entity.enums.TransportVehicleType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "transport_vehicles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransportVehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agency_id", nullable = false)
    private Long agencyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false, length = 16)
    private TransportVehicleType vehicleType;

    @Column(name = "seat_count", nullable = false)
    private Integer seatCount;

    @Column(length = 64)
    private String plate;

    @Column(length = 120)
    private String brand;

    @Column(name = "driver_name", length = 140)
    private String driverName;

    @Column(name = "driver_phone", length = 60)
    private String driverPhone;

    @Column(name = "driver_email", length = 255)
    private String driverEmail;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
