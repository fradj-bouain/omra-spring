package com.omra.platform.dto.mobile;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.omra.platform.entity.enums.GroupStatus;
import com.omra.platform.entity.enums.TripType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MobileAccompagnateurGroupSummaryDto {

    private Long id;
    private String name;
    private TripType tripType;
    private LocalDate departureDate;
    private LocalDate returnDate;
    private Integer maxCapacity;
    private BigDecimal price;
    private GroupStatus status;
    private long pilgrimsCount;
}
