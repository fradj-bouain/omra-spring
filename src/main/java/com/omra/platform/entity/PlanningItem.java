package com.omra.platform.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Élément d'un planning : une référence à un TaskTemplate avec un ordre.
 */
@Entity
@Table(name = "planning_items", indexes = {
    @Index(name = "idx_planning_item_planning", columnList = "planning_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanningItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "planning_id", nullable = false)
    private Long planningId;

    @Column(name = "task_template_id", nullable = false)
    private Long taskTemplateId;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planning_id", insertable = false, updatable = false)
    private Planning planning;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_template_id", insertable = false, updatable = false)
    private TaskTemplate taskTemplate;
}
