package com.omra.platform.service;

import com.omra.platform.dto.NotificationDto;
import com.omra.platform.dto.PageResponse;
import com.omra.platform.entity.Notification;
import com.omra.platform.exception.ForbiddenException;
import com.omra.platform.exception.ResourceNotFoundException;
import com.omra.platform.repository.NotificationRepository;
import com.omra.platform.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public PageResponse<NotificationDto> getMyNotifications(Pageable pageable) {
        Long userId = TenantContext.getUserId();
        if (userId == null) throw new ForbiddenException("Not authenticated");
        Page<Notification> page = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        List<NotificationDto> content = page.getContent().stream().map(this::toDto).collect(Collectors.toList());
        return PageResponse.<NotificationDto>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    @Transactional
    public void markAsRead(Long id) {
        Long userId = TenantContext.getUserId();
        Notification n = notificationRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Notification", id));
        if (!n.getUserId().equals(userId)) throw new ForbiddenException("Access denied");
        n.setRead(true);
        notificationRepository.save(n);
    }

    @Transactional(readOnly = true)
    public long countUnread() {
        Long userId = TenantContext.getUserId();
        if (userId == null) return 0;
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    private NotificationDto toDto(Notification e) {
        return NotificationDto.builder()
                .id(e.getId())
                .userId(e.getUserId())
                .agencyId(e.getAgencyId())
                .title(e.getTitle())
                .message(e.getMessage())
                .type(e.getType())
                .channel(e.getChannel())
                .entityType(e.getEntityType())
                .entityId(e.getEntityId())
                .read(e.getRead())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
