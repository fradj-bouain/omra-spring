package com.omra.platform.schedule;

import com.omra.platform.service.ReferralCampaignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReferralCampaignScheduler {

    private final ReferralCampaignService referralCampaignService;

    /** Ferme les campagnes ACTIVE dont la date de fin est dépassée. */
    @Scheduled(fixedDelayString = "${app.referral-campaigns.close-check-ms:300000}")
    public void closeExpiredCampaigns() {
        try {
            int n = referralCampaignService.closeExpiredCampaigns();
            if (n > 0) {
                log.info("Referral campaigns auto-closed (end date passed): {}", n);
            }
        } catch (Exception e) {
            log.warn("Referral campaign close sweep failed: {}", e.getMessage());
        }
    }
}
