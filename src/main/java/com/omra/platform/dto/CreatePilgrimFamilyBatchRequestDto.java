package com.omra.platform.dto;

import com.omra.platform.entity.enums.SponsorType;
import com.omra.platform.entity.enums.TravelerType;
import com.omra.platform.entity.enums.VisaStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePilgrimFamilyBatchRequestDto {

    private String nationality;
    private String phone;
    private String email;
    private String address;

    private VisaStatus visaStatus;

    /** Appliqué à chaque membre de la famille (même dossier). */
    private TravelerType travelerType;

    private SponsorType sponsorType;
    private String sponsorLabel;
    private Long referrerPilgrimId;

    @NotEmpty
    @Size(min = 2)
    @Valid
    private List<FamilyMemberPayloadDto> members;
}
