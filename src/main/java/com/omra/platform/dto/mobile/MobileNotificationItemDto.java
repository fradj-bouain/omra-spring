package com.omra.platform.dto.mobile;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MobileNotificationItemDto {

    private Long id;
    private String title;
    private String message;
    private String type;
    private Boolean read;
    private Instant createdAt;
    private String entityType;
    private String entityId;
}
