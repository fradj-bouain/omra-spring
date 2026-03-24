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

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 128)
    private String city;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "country", length = 128)
    private String country;

    private Integer stars;

    @Column(name = "contact_important", length = 255)
    private String contactImportant;

    @Column(name = "contact_phone", length = 64)
    private String contactPhone;

    @Column(name = "reception_phone", length = 64)
    private String receptionPhone;

    @Column(length = 255)
    private String email;

    /** WGS84 — position choisie sur la carte (Google Maps) */
    private Double latitude;

    private Double longitude;
}
