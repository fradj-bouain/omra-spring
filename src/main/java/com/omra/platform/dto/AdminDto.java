package com.omra.platform.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDto {

    private Long id;
    private String username;
    private String email;
    private String telephone;
    private String cin;
    private Boolean active;
    private Instant createdAt;
}
