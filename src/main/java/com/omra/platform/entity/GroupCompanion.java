package com.omra.platform.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "group_companions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"group_id", "user_id"})
}, indexes = {
    @Index(name = "idx_group_companion_group", columnList = "group_id"),
    @Index(name = "idx_group_companion_user", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupCompanion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", insertable = false, updatable = false)
    private UmrahGroup group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
}
