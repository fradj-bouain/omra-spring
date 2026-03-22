package com.omra.platform.controller.mobile;

import com.omra.platform.dto.mobile.*;
import com.omra.platform.service.MobileAccompagnateurService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mobile/accompagnateur")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PILGRIM_COMPANION')")
@Tag(name = "Mobile — Accompagnateur", description = "API app mobile (profil, groupes, notifications)")
public class MobileAccompagnateurController {

    private final MobileAccompagnateurService mobileAccompagnateurService;

    @GetMapping("/me")
    @Operation(summary = "Session courante (profil léger)")
    public ResponseEntity<MobileApiResponse<MobileAccompagnateurProfileDto>> me() {
        MobileAccompagnateurProfileDto data = mobileAccompagnateurService.getMe();
        return ResponseEntity.ok(MobileApiResponse.<MobileAccompagnateurProfileDto>builder()
                .success(true)
                .data(data)
                .build());
    }

    @GetMapping("/profile")
    @Operation(summary = "Profil complet")
    public ResponseEntity<MobileApiResponse<MobileAccompagnateurProfileDto>> getProfile() {
        MobileAccompagnateurProfileDto data = mobileAccompagnateurService.getProfile();
        return ResponseEntity.ok(MobileApiResponse.<MobileAccompagnateurProfileDto>builder()
                .success(true)
                .data(data)
                .build());
    }

    @PutMapping("/profile")
    @Operation(summary = "Mise à jour profil (nom, téléphone, avatar)")
    public ResponseEntity<MobileApiResponse<MobileAccompagnateurProfileDto>> updateProfile(
            @Valid @RequestBody MobileAccompagnateurProfileUpdateDto dto) {
        MobileAccompagnateurProfileDto data = mobileAccompagnateurService.updateProfile(dto);
        return ResponseEntity.ok(MobileApiResponse.<MobileAccompagnateurProfileDto>builder()
                .success(true)
                .data(data)
                .message("Profil mis à jour")
                .build());
    }

    @GetMapping("/groups")
    @Operation(summary = "Groupes où l'accompagnateur est affecté")
    public ResponseEntity<MobileApiResponse<List<MobileAccompagnateurGroupSummaryDto>>> listGroups() {
        List<MobileAccompagnateurGroupSummaryDto> data = mobileAccompagnateurService.listMyGroups();
        return ResponseEntity.ok(MobileApiResponse.<List<MobileAccompagnateurGroupSummaryDto>>builder()
                .success(true)
                .data(data)
                .build());
    }

    @GetMapping("/groups/{id}")
    @Operation(summary = "Détail groupe (pèlerins / programme optionnels)")
    public ResponseEntity<MobileApiResponse<MobileAccompagnateurGroupDetailDto>> getGroup(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean includePilgrims,
            @RequestParam(defaultValue = "true") boolean includePlanning) {
        MobileAccompagnateurGroupDetailDto data = mobileAccompagnateurService.getGroupDetail(id, includePilgrims, includePlanning);
        return ResponseEntity.ok(MobileApiResponse.<MobileAccompagnateurGroupDetailDto>builder()
                .success(true)
                .data(data)
                .build());
    }

    @GetMapping("/notifications")
    @Operation(summary = "Notifications de l'utilisateur connecté")
    public ResponseEntity<MobileApiResponse<List<MobileNotificationItemDto>>> notifications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        MobileApiResponse<List<MobileNotificationItemDto>> body = mobileAccompagnateurService.listNotifications(page, size);
        return ResponseEntity.ok(body);
    }

    @PutMapping("/notifications/{id}/read")
    @Operation(summary = "Marquer une notification comme lue")
    public ResponseEntity<MobileApiResponse<Void>> markNotificationRead(@PathVariable Long id) {
        mobileAccompagnateurService.markNotificationRead(id);
        return ResponseEntity.ok(MobileApiResponse.<Void>builder()
                .success(true)
                .data(null)
                .message("Notification lue")
                .build());
    }
}
