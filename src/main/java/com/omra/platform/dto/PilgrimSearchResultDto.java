package com.omra.platform.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PilgrimSearchResultDto {

    private Long id;
    private String firstName;
    private String lastName;
    private String passportNumber;
}
