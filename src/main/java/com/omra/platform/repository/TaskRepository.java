package com.omra.platform.repository;

import com.omra.platform.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {

    Page<Task> findByAgencyIdAndDeletedAtIsNull(Long agencyId, Pageable pageable);

    Page<Task> findByAgencyIdAndGroupIdAndDeletedAtIsNull(Long agencyId, Long groupId, Pageable pageable);

    Page<Task> findByAgencyIdAndAssignedToUserIdAndDeletedAtIsNull(Long agencyId, Long assignedToUserId, Pageable pageable);
}
