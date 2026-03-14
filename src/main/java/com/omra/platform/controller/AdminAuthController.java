package com.omra.platform.controller;

import com.omra.platform.dto.AuthRequest;
import com.omra.platform.dto.AuthResponse;
import com.omra.platform.service.AdminAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
@Tag(name = "Admin Auth", description = "Platform admin login (create agencies, activate/deactivate)")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    @Operation(summary = "Admin login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(adminAuthService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Admin refresh token")
    public ResponseEntity<AuthResponse> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refresh_token");
        if (refreshToken == null) refreshToken = body.get("refreshToken");
        return ResponseEntity.ok(adminAuthService.refresh(refreshToken));
    }

    @PostMapping("/logout")
    @Operation(summary = "Admin logout")
    public ResponseEntity<Void> logout(@RequestBody(required = false) Map<String, String> body) {
        String refreshToken = body != null ? body.get("refresh_token") : null;
        if (refreshToken == null && body != null) refreshToken = body.get("refreshToken");
        adminAuthService.logout(refreshToken);
        return ResponseEntity.noContent().build();
    }
}
