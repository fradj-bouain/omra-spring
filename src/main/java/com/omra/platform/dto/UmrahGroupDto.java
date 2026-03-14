package com.omra.platform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.omra.platform.entity.enums.GroupStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

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
    private String description;
    private LocalDate departureDate;
    private LocalDate returnDate;
    private Integer maxCapacity;
    private BigDecimal price;
    private GroupStatus status;
    private Instant createdAt;
}
