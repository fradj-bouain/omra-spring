package com.omra.platform.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "ui_translations",
        uniqueConstraints = @UniqueConstraint(name = "uk_ui_translations_locale_key", columnNames = {"locale", "msg_key"}),
        indexes = @Index(name = "idx_ui_translations_locale", columnList = "locale"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UiTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String locale;

    @Column(name = "msg_key", nullable = false, length = 255)
    private String msgKey;

    @Column(name = "msg_value", nullable = false, columnDefinition = "TEXT")
    private String msgValue;
}
