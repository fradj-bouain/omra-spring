package com.omra.platform.repository;

import com.omra.platform.entity.TaskTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskTemplateRepository extends JpaRepository<TaskTemplate, Long> {

    List<TaskTemplate> findByAgencyIdAndDeletedAtIsNullOrderByNameAsc(Long agencyId);

    List<TaskTemplate> findByAgencyIdAndParentIsNullAndDeletedAtIsNullOrderByNameAsc(Long agencyId);

    List<TaskTemplate> findByAgencyIdAndParent_IdAndDeletedAtIsNullOrderByNameAsc(Long agencyId, Long parentId);

    @Query("SELECT DISTINCT t FROM TaskTemplate t LEFT JOIN FETCH t.parent WHERE t.agencyId = :agencyId AND t.deletedAt IS NULL")
    List<TaskTemplate> findAllByAgencyWithParent(@Param("agencyId") Long agencyId);

    @Query("SELECT DISTINCT t FROM TaskTemplate t LEFT JOIN FETCH t.parent WHERE t.deletedAt IS NULL")
    List<TaskTemplate> findAllNonDeletedWithParent();
}
