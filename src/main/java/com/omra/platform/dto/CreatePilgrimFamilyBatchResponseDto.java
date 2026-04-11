package com.omra.platform.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePilgrimFamilyBatchResponseDto {

    private Long familyId;
    private List<PilgrimDto> pilgrims;
}
