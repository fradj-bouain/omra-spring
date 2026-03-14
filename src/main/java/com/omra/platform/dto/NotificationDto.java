package com.omra.platform.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDto {

    private Long id;
    private Long userId;
    private Long agencyId;
    private String title;
    private String message;
    private String type;
    private String channel;
    private String entityType;
    private String entityId;
    private Boolean read;
    private Instant createdAt;
}
