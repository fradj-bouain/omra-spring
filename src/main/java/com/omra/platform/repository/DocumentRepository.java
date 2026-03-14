package com.omra.platform.repository;

import com.omra.platform.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    Page<Document> findByAgencyIdAndDeletedAtIsNull(Long agencyId, Pageable pageable);

    List<Document> findByPilgrimIdAndDeletedAtIsNull(Long pilgrimId);

    List<Document> findByGroupIdAndDeletedAtIsNull(Long groupId);
}
