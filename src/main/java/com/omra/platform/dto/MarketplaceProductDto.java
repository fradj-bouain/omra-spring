package com.omra.platform.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketplaceProductDto {

    private Long id;
    private Long marketplaceId;
    private String title;
    private String description;
    private String imageUrl;
    private BigDecimal price;
    private String currency;
    private boolean inStock;
    private Integer stockQuantity;
}
