package com.omra.platform.controller.mobile;

import com.omra.platform.dto.AuthRequest;
import com.omra.platform.dto.mobile.MobileAccompagnateurLoginDataDto;
import com.omra.platform.dto.mobile.MobileApiResponse;
import com.omra.platform.service.AuthService;
import com.omra.platform.service.MobileAccompagnateurService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mobile/accompagnateur")
@RequiredArgsConstructor
@Tag(name = "Mobile — Accompagnateur (auth)", description = "Connexion app mobile accompagnateur")
public class MobileAccompagnateurAuthController {

    private final AuthService authService;
    private final MobileAccompagnateurService mobileAccompagnateurService;

    @PostMapping("/login")
    @Operation(summary = "Connexion (JWT) — réservé au rôle PILGRIM_COMPANION")
    public ResponseEntity<MobileApiResponse<MobileAccompagnateurLoginDataDto>> login(@Valid @RequestBody AuthRequest request) {
        var auth = authService.loginForMobileCompanion(request);
        MobileAccompagnateurLoginDataDto data = mobileAccompagnateurService.toLoginData(auth);
        return ResponseEntity.ok(MobileApiResponse.<MobileAccompagnateurLoginDataDto>builder()
                .success(true)
                .data(data)
                .message("Authentification réussie")
                .build());
    }
}
