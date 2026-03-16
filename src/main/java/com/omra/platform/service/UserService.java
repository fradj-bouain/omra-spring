package com.omra.platform.service;

import com.omra.platform.dto.PageResponse;
import com.omra.platform.dto.UserDto;
import com.omra.platform.entity.User;
import com.omra.platform.entity.enums.UserRole;
import com.omra.platform.entity.enums.UserStatus;
import com.omra.platform.exception.BadRequestException;
import com.omra.platform.exception.ForbiddenException;
import com.omra.platform.exception.ResourceNotFoundException;
import com.omra.platform.mapper.UserMapper;
import com.omra.platform.repository.UserRepository;
import com.omra.platform.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public PageResponse<UserDto> getUsers(Pageable pageable, UserRole roleFilter) {
        Long agencyId = TenantContext.getAgencyId();
        if (TenantContext.isSuperAdmin() && agencyId == null) {
            Page<User> page = roleFilter != null
                    ? userRepository.findByAgencyIdIsNullAndRoleAndDeletedAtIsNull(roleFilter, pageable)
                    : userRepository.findByAgencyIdIsNullAndDeletedAtIsNull(pageable);
            return toPageResponse(page);
        }
        if (agencyId == null) throw new ForbiddenException("Agency context required");
        Page<User> page = roleFilter != null
                ? userRepository.findByAgencyIdAndRoleAndDeletedAtIsNull(agencyId, roleFilter, pageable)
                : userRepository.findByAgencyIdAndDeletedAtIsNull(agencyId, pageable);
        return toPageResponse(page);
    }

    @Transactional(readOnly = true)
    public UserDto getById(Long id) {
        User user = findByIdAndContext(id);
        return userMapper.toDto(user);
    }

    @Transactional
    public UserDto create(UserDto dto) {
        Long agencyId = TenantContext.getAgencyId();
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Email already registered");
        }
        if (!TenantContext.isSuperAdmin() && agencyId == null) {
            throw new ForbiddenException("Agency context required to create user");
        }
        UserRole role = dto.getRole() != null ? dto.getRole() : UserRole.AGENCY_AGENT;
        if (role == UserRole.SUPER_ADMIN) {
            throw new BadRequestException("Le rôle Super admin ne peut pas être attribué via ce formulaire");
        }
        User user = User.builder()
                .agencyId(TenantContext.isSuperAdmin() ? dto.getAgencyId() : agencyId)
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .password(passwordEncoder.encode(dto.getPassword() != null ? dto.getPassword() : "change-me"))
                .role(role)
                .status(dto.getStatus() != null ? dto.getStatus() : UserStatus.ACTIVE)
                .avatar(dto.getAvatar())
                .emailVerified(dto.getEmailVerified() != null ? dto.getEmailVerified() : false)
                .build();
        user = userRepository.save(user);
        return userMapper.toDto(user);
    }

    private User findByIdAndContext(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));
        if (user.getDeletedAt() != null) throw new ResourceNotFoundException("User", id);
        Long agencyId = TenantContext.getAgencyId();
        if (!TenantContext.isSuperAdmin() && (agencyId == null || !agencyId.equals(user.getAgencyId()))) {
            throw new ForbiddenException("Access denied to this user");
        }
        return user;
    }

    private PageResponse<UserDto> toPageResponse(Page<User> page) {
        List<UserDto> content = page.getContent().stream().map(userMapper::toDto).collect(Collectors.toList());
        return PageResponse.<UserDto>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
