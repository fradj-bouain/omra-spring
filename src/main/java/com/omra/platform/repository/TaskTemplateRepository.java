package com.omra.platform.repository;

import com.omra.platform.entity.TaskTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskTemplateRepository extends JpaRepository<TaskTemplate, Long> {

    List<TaskTemplate> findByAgencyIdAndDeletedAtIsNullOrderByName(Long agencyId);
}
