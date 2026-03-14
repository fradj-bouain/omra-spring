package com.omra.platform.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "hotels", indexes = {
    @Index(name = "idx_hotel_agency_id", columnList = "agency_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agency_id")
    private Long agencyId;

    @Column(nullable = false)
    private String name;

    private String city;
    private String address;
    private Integer stars;
    private String contactPhone;
}
