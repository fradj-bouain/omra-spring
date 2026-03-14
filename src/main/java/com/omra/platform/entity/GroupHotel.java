package com.omra.platform.entity;

import com.omra.platform.entity.enums.HotelCity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "group_hotels", indexes = {
    @Index(name = "idx_group_hotel_group", columnList = "group_id"),
    @Index(name = "idx_group_hotel_hotel", columnList = "hotel_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupHotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "hotel_id", nullable = false)
    private Long hotelId;

    @Enumerated(EnumType.STRING)
    private HotelCity city;

    private LocalDate checkIn;
    private LocalDate checkOut;
    private String roomType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", insertable = false, updatable = false)
    private UmrahGroup group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", insertable = false, updatable = false)
    private Hotel hotel;
}
