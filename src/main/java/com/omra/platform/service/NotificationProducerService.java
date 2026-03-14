package com.omra.platform.service;

import com.omra.platform.entity.Notification;
import com.omra.platform.entity.User;
import com.omra.platform.entity.enums.UserRole;
import com.omra.platform.repository.NotificationRepository;
import com.omra.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Creates notifications for payment, document upload, visa status change.
 * Notifications are created for the relevant user(s) and include agency_id and entity link.
 */
@Service
@RequiredArgsConstructor
public class NotificationProducerService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    private static final String CHANNEL_IN_APP = "IN_APP";
    private static final String TYPE_PAYMENT = "PAYMENT";
    private static final String TYPE_DOCUMENT = "DOCUMENT";
    private static final String TYPE_VISA = "VISA";

    /** Notify agency admins/agents when a payment is received (status PAID). */
    @Async
    @Transactional
    public void notifyPaymentReceived(Long agencyId, Long paymentId, String pilgrimName, String amount) {
        if (agencyId == null) return;
        List<User> recipients = userRepository.findByAgencyIdAndDeletedAtIsNull(agencyId, Pageable.unpaged()).getContent();
        recipients = recipients.stream()
                .filter(u -> u.getRole() == UserRole.AGENCY_ADMIN || u.getRole() == UserRole.AGENCY_AGENT)
                .toList();
        String title = "Paiement reçu";
        String message = String.format("Paiement de %s pour le pèlerin %s.", amount, pilgrimName != null ? pilgrimName : "N/A");
        for (User u : recipients) {
            Notification n = Notification.builder()
                    .userId(u.getId())
                    .agencyId(agencyId)
                    .title(title)
                    .message(message)
                    .type(TYPE_PAYMENT)
                    .channel(CHANNEL_IN_APP)
                    .entityType("Payment")
                    .entityId(paymentId != null ? String.valueOf(paymentId) : null)
                    .read(false)
                    .build();
            notificationRepository.save(n);
        }
    }

    /** Notify agency when a document is uploaded (e.g. for a pilgrim). */
    @Async
    @Transactional
    public void notifyDocumentUploaded(Long agencyId, Long documentId, String pilgrimName, String docType) {
        if (agencyId == null) return;
        List<User> recipients = userRepository.findByAgencyIdAndDeletedAtIsNull(agencyId, Pageable.unpaged()).getContent();
        recipients = recipients.stream()
                .filter(u -> u.getRole() == UserRole.AGENCY_ADMIN || u.getRole() == UserRole.AGENCY_AGENT)
                .toList();
        String title = "Document téléchargé";
        String message = String.format("Nouveau document %s pour %s.", docType != null ? docType : "document", pilgrimName != null ? pilgrimName : "pèlerin");
        for (User u : recipients) {
            Notification n = Notification.builder()
                    .userId(u.getId())
                    .agencyId(agencyId)
                    .title(title)
                    .message(message)
                    .type(TYPE_DOCUMENT)
                    .channel(CHANNEL_IN_APP)
                    .entityType("Document")
                    .entityId(documentId != null ? String.valueOf(documentId) : null)
                    .read(false)
                    .build();
            notificationRepository.save(n);
        }
    }

    /** Notify user linked to pilgrim when visa status changes. */
    @Async
    @Transactional
    public void notifyVisaStatusChange(Long agencyId, Long pilgrimId, String pilgrimName, String newStatus) {
        List<User> linked = userRepository.findByPilgrimIdAndDeletedAtIsNull(pilgrimId);
        String title = "Changement de statut visa";
        String message = String.format("Le statut du visa pour %s est maintenant : %s.", pilgrimName != null ? pilgrimName : "pèlerin", newStatus);
        for (User u : linked) {
            Notification n = Notification.builder()
                    .userId(u.getId())
                    .agencyId(agencyId)
                    .title(title)
                    .message(message)
                    .type(TYPE_VISA)
                    .channel(CHANNEL_IN_APP)
                    .entityType("Pilgrim")
                    .entityId(pilgrimId != null ? String.valueOf(pilgrimId) : null)
                    .read(false)
                    .build();
            notificationRepository.save(n);
        }
    }
}
