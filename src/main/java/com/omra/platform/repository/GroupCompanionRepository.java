package com.omra.platform.repository;

import com.omra.platform.entity.GroupCompanion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupCompanionRepository extends JpaRepository<GroupCompanion, Long> {

    List<GroupCompanion> findByGroupIdOrderByIdAsc(Long groupId);

    void deleteByGroupId(Long groupId);
}
