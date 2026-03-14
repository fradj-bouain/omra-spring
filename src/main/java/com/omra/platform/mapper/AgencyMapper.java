package com.omra.platform.mapper;

import com.omra.platform.dto.AgencyDto;
import com.omra.platform.dto.AgencyThemeDto;
import com.omra.platform.entity.Agency;
import org.springframework.stereotype.Component;

@Component
public class AgencyMapper {

    public AgencyDto toDto(Agency entity) {
        if (entity == null) return null;
        return AgencyDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .country(entity.getCountry())
                .city(entity.getCity())
                .address(entity.getAddress())
                .logoUrl(entity.getLogoUrl())
                .faviconUrl(entity.getFaviconUrl())
                .primaryColor(entity.getPrimaryColor())
                .secondaryColor(entity.getSecondaryColor())
                .menuColor(entity.getMenuColor())
                .buttonColor(entity.getButtonColor())
                .backgroundColor(entity.getBackgroundColor())
                .textColor(entity.getTextColor())
                .subscriptionPlan(entity.getSubscriptionPlan())
                .subscriptionStartDate(entity.getSubscriptionStartDate())
                .subscriptionEndDate(entity.getSubscriptionEndDate())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public AgencyThemeDto toThemeDto(Agency entity) {
        if (entity == null) return null;
        return AgencyThemeDto.builder()
                .logoUrl(entity.getLogoUrl())
                .faviconUrl(entity.getFaviconUrl())
                .primaryColor(entity.getPrimaryColor())
                .secondaryColor(entity.getSecondaryColor())
                .menuColor(entity.getMenuColor())
                .buttonColor(entity.getButtonColor())
                .backgroundColor(entity.getBackgroundColor())
                .textColor(entity.getTextColor())
                .build();
    }
}
