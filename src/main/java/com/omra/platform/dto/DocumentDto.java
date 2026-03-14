package com.omra.platform.dto;

import com.omra.platform.entity.enums.DocumentStatus;
import com.omra.platform.entity.enums.DocumentType;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentDto {

    private Long id;
    private Long agencyId;
    private Long pilgrimId;
    private Long groupId;
    private DocumentType type;
    private String fileUrl;
    private DocumentStatus status;
    private Instant createdAt;
}
