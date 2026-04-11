package com.omra.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FamilyMemberPayloadDto {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotNull
    private LocalDate dateOfBirth;

    /** CIN / passeport (optionnel). */
    private String passportNumber;

    private String gender;

    /** PERE, MERE, ENFANT, AUTRE */
    private String familyRole;

    /** Notes documents — fusionnées dans l’adresse du dossier. */
    private String documentNotes;
}
