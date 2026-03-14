package com.omra.platform.repository;

import com.omra.platform.entity.GroupPilgrim;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupPilgrimRepository extends JpaRepository<GroupPilgrim, Long> {

    List<GroupPilgrim> findByGroupId(Long groupId);

    List<GroupPilgrim> findByPilgrimId(Long pilgrimId);

    Optional<GroupPilgrim> findByGroupIdAndPilgrimId(Long groupId, Long pilgrimId);

    boolean existsByGroupIdAndPilgrimId(Long groupId, Long pilgrimId);

    void deleteByGroupIdAndPilgrimId(Long groupId, Long pilgrimId);

    long countByGroupId(Long groupId);
}
