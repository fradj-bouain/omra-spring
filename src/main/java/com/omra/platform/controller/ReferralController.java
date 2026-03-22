package com.omra.platform.controller;

import com.omra.platform.dto.ReferralDto;
import com.omra.platform.dto.ReferralStatsDto;
import com.omra.platform.service.ReferralService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Referral (Parrainage)", description = "Referral code, link, stats and rewards")
public class ReferralController {

    private final ReferralService referralService;

    @GetMapping("/referral-code")
    @Operation(summary = "Get my referral code (generates if missing)")
    public ResponseEntity<Map<String, String>> getMyReferralCode() {
        String code = referralService.getMyReferralCode();
        return ResponseEntity.ok(Map.of("code", code));
    }

    @GetMapping("/referral-stats")
    @Operation(summary = "Get my referral stats (code, link, counts)")
    public ResponseEntity<ReferralStatsDto> getMyStats() {
        return ResponseEntity.ok(referralService.getMyStats());
    }

    @GetMapping("/referrals")
    @Operation(summary = "List my referrals (as referrer)")
    public ResponseEntity<List<ReferralDto>> getMyReferrals() {
        return ResponseEntity.ok(referralService.getMyReferrals());
    }

    @PostMapping("/referrals/validate")
    @Operation(summary = "Validate a referral code (for signup form)")
    public ResponseEntity<Map<String, Boolean>> validateCode(@RequestBody Map<String, String> body) {
        String code = body != null ? body.get("code") : null;
        boolean valid = referralService.validateCode(code);
        return ResponseEntity.ok(Map.of("valid", valid));
    }

    @PostMapping("/referrals/{id}/grant-reward")
    @Operation(summary = "Grant reward for a referral (marks completed, reward given)")
    public ResponseEntity<ReferralDto> grantReward(@PathVariable Long id) {
        return ResponseEntity.ok(referralService.grantReward(id));
    }
}
