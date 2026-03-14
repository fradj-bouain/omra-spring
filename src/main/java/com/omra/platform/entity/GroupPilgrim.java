package com.omra.platform.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "group_pilgrims", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"group_id", "pilgrim_id"})
}, indexes = {
    @Index(name = "idx_group_pilgrim_group", columnList = "group_id"),
    @Index(name = "idx_group_pilgrim_pilgrim", columnList = "pilgrim_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupPilgrim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "pilgrim_id", nullable = false)
    private Long pilgrimId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", insertable = false, updatable = false)
    private UmrahGroup group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pilgrim_id", insertable = false, updatable = false)
    private Pilgrim pilgrim;
}
