package com.omra.platform.dto.mobile;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.omra.platform.dto.PlanningDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MobileAccompagnateurGroupDetailDto {

    private MobileAccompagnateurGroupSummaryDto summary;
    private String description;
    private Long planningId;
    /** Présent si includePlanning=true */
    private PlanningDto program;
    /** Présent si includePilgrims=true */
    private List<MobilePilgrimBriefDto> pilgrims;
}
