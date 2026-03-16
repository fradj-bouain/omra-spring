package com.omra.platform.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Type de tâche prédéfinie (ex: Tawaf, Sa'i) avec une durée.
 * L'agence crée son catalogue d'activités ; les plannings sont composés de ces modèles.
 */
@Entity
@Table(name = "task_templates", indexes = {
    @Index(name = "idx_task_template_agency", columnList = "agency_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agency_id", nullable = false)
    private Long agencyId;

    @Column(nullable = false, length = 128)
    private String name;

    /** Durée en minutes (ex: 120 = 2h). */
    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
