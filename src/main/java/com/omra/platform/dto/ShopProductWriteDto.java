package com.omra.platform.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopProductWriteDto {

    private String title;
    private String description;
    private String imageUrl;
    private BigDecimal price;
    private String currency;
    private Boolean inStock;
    private Integer stockQuantity;
}
