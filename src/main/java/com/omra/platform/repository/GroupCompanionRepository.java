package com.omra.platform.repository;

import com.omra.platform.entity.GroupCompanion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupCompanionRepository extends JpaRepository<GroupCompanion, Long> {

    List<GroupCompanion> findByUserIdOrderByIdAsc(Long userId);

    List<GroupCompanion> findByGroupIdOrderByIdAsc(Long groupId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from GroupCompanion g where g.groupId = :groupId")
    void deleteByGroupId(@Param("groupId") Long groupId);
}
