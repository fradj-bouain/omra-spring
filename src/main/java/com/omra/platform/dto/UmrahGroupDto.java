package com.omra.platform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.omra.platform.entity.enums.GroupStatus;
import com.omra.platform.entity.enums.TripType;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UmrahGroupDto {

    private Long id;
    @JsonProperty("agencyId")
    private Long agencyId;
    private String name;
    /** Hajj ou Omra */
    private TripType tripType;
    private String description;
    private LocalDate departureDate;
    private LocalDate returnDate;
    private Integer maxCapacity;
    private BigDecimal price;
    private Long planningId;
    private GroupStatus status;
    private Instant createdAt;
    /** IDs des utilisateurs accompagnateurs (rôle PILGRIM_COMPANION) affectés au groupe */
    private List<Long> companionIds;
}
