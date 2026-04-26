package com.omra.platform.entity.converter;

import com.omra.platform.entity.enums.AgencyKind;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maps {@code agency_kind} text to {@link AgencyKind}; unknown or blank DB values become {@link AgencyKind#TRAVEL}
 * so listing agencies never fails on legacy or bad data.
 */
@Converter(autoApply = false)
public class AgencyKindColumnConverter implements AttributeConverter<AgencyKind, String> {

    private static final Logger log = LoggerFactory.getLogger(AgencyKindColumnConverter.class);

    @Override
    public String convertToDatabaseColumn(AgencyKind attribute) {
        if (attribute == null) {
            return AgencyKind.TRAVEL.name();
        }
        return attribute.name();
    }

    @Override
    public AgencyKind convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return AgencyKind.TRAVEL;
        }
        String normalized = dbData.trim().toUpperCase();
        try {
            return AgencyKind.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            log.warn("Unknown agency_kind value '{}', defaulting to TRAVEL", dbData);
            return AgencyKind.TRAVEL;
        }
    }
}
