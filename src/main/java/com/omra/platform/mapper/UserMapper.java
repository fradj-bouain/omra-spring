package com.omra.platform.mapper;

import com.omra.platform.dto.UserDto;
import com.omra.platform.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto toDto(User entity) {
        if (entity == null) return null;
        return UserDto.builder()
                .id(entity.getId())
                .agencyId(entity.getAgencyId())
                .name(entity.getName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .role(entity.getRole())
                .status(entity.getStatus())
                .avatar(entity.getAvatar())
                .lastLogin(entity.getLastLogin())
                .emailVerified(entity.getEmailVerified())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
