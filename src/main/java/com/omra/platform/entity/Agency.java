package com.omra.platform.entity;

import com.omra.platform.entity.enums.AgencyStatus;
import com.omra.platform.entity.enums.ThemeMode;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.Instant;

@Entity
@Table(name = "agencies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Agency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;
    private String country;
    private String city;
    private String address;

    private String logoUrl;
    private String faviconUrl;

    private String primaryColor;
    private String secondaryColor;
    /** Barre latérale (alias historique : {@link #menuColor}). */
    private String sidebarColor;
    private String menuColor;
    private String buttonColor;
    private String backgroundColor;
    private String cardColor;
    private String textColor;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private ThemeMode themeMode;

    private String subscriptionPlan;
    private LocalDate subscriptionStartDate;
    private LocalDate subscriptionEndDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AgencyStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
