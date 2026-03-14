package com.omra.platform.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "group_room_assignments", indexes = {
    @Index(name = "idx_group_room_assign_group_hotel", columnList = "group_hotel_id"),
    @Index(name = "idx_group_room_assign_pilgrim", columnList = "pilgrim_id")
}, uniqueConstraints = {
    @UniqueConstraint(columnNames = {"room_id", "pilgrim_id"}),
    @UniqueConstraint(columnNames = {"group_hotel_id", "pilgrim_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupRoomAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_hotel_id", nullable = false)
    private Long groupHotelId;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "pilgrim_id", nullable = false)
    private Long pilgrimId;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_hotel_id", insertable = false, updatable = false)
    private GroupHotel groupHotel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", insertable = false, updatable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pilgrim_id", insertable = false, updatable = false)
    private Pilgrim pilgrim;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
