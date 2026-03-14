package com.omra.platform.controller;

import com.omra.platform.dto.NotificationDto;
import com.omra.platform.dto.PageResponse;
import com.omra.platform.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification APIs")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Get my notifications (paginated)")
    public ResponseEntity<PageResponse<NotificationDto>> getMy(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(notificationService.getMyNotifications(PageRequest.of(page - 1, size)));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread count")
    public ResponseEntity<Long> unreadCount() {
        return ResponseEntity.ok(notificationService.countUnread());
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "Mark as read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.noContent().build();
    }
}
