package com.omra.platform.dto;

import com.omra.platform.entity.enums.MarketplaceCatalogType;
import com.omra.platform.entity.enums.MarketplaceStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketplaceWriteDto {

    private String name;
    private String description;
    private MarketplaceStatus status;
    private MarketplaceCatalogType catalogType;
    private String apiBaseUrl;
    private String apiAuthHeader;
    private String apiAuthValue;
}
