package com.omra.platform.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PilgrimRegistrationRowDto {

    private PilgrimRegistrationType registrationType;

    /** Non-null uniquement si registrationType=FAMILY. */
    private Long familyId;

    /** 1 pour INDIVIDUAL, >=2 pour FAMILY. */
    private Integer membersCount;

    /** “Chef de famille” / représentant (affiché dans la liste). */
    private PilgrimDto representative;

    /** Membres si FAMILY; vide si INDIVIDUAL. */
    private List<PilgrimDto> members;
}

