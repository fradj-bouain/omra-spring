package com.omra.platform.repository;

import com.omra.platform.entity.UiTranslation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UiTranslationRepository extends JpaRepository<UiTranslation, Long> {

    List<UiTranslation> findByLocaleOrderByMsgKeyAsc(String locale);
}
