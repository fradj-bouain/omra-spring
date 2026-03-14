package com.omra.platform.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupRoomAssignmentDto {

    private Long id;
    private Long groupHotelId;
    private Long roomId;
    private Long pilgrimId;
    private Instant createdAt;
}
