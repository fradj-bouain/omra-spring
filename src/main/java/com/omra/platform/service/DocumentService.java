package com.omra.platform.service;

import com.omra.platform.dto.DocumentDto;
import com.omra.platform.dto.PageResponse;
import com.omra.platform.entity.Document;
import com.omra.platform.exception.ForbiddenException;
import com.omra.platform.exception.ResourceNotFoundException;
import com.omra.platform.repository.DocumentRepository;
import com.omra.platform.repository.PilgrimRepository;
import com.omra.platform.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final PilgrimRepository pilgrimRepository;
    private final NotificationProducerService notificationProducer;

    private Long requireAgencyId() {
        Long agencyId = TenantContext.getAgencyId();
        if (agencyId == null && !TenantContext.isSuperAdmin()) throw new ForbiddenException("Agency context required");
        return agencyId;
    }

    @Transactional(readOnly = true)
    public PageResponse<DocumentDto> getDocuments(Pageable pageable) {
        Long agencyId = requireAgencyId();
        Page<Document> page = documentRepository.findByAgencyIdAndDeletedAtIsNull(agencyId, pageable);
        return toPageResponse(page);
    }

    @Transactional(readOnly = true)
    public DocumentDto getById(Long id) {
        return toDto(findByIdAndAgency(id));
    }

    @Transactional(readOnly = true)
    public List<DocumentDto> getByPilgrim(Long pilgrimId) {
        requireAgencyId();
        return documentRepository.findByPilgrimIdAndDeletedAtIsNull(pilgrimId).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public DocumentDto create(DocumentDto dto) {
        Long agencyId = requireAgencyId();
        if (agencyId == null) throw new ForbiddenException("Agency required");
        Document doc = Document.builder()
                .agencyId(agencyId)
                .pilgrimId(dto.getPilgrimId())
                .groupId(dto.getGroupId())
                .type(dto.getType())
                .fileUrl(dto.getFileUrl())
                .status(dto.getStatus() != null ? dto.getStatus() : com.omra.platform.entity.enums.DocumentStatus.UPLOADED)
                .build();
        doc = documentRepository.save(doc);
        String pilgrimName = doc.getPilgrimId() != null ? pilgrimRepository.findById(doc.getPilgrimId())
                .map(p -> p.getFirstName() + " " + p.getLastName()).orElse(null) : null;
        notificationProducer.notifyDocumentUploaded(doc.getAgencyId(), doc.getId(), pilgrimName, doc.getType() != null ? doc.getType().name() : null);
        return toDto(doc);
    }

    @Transactional
    public void delete(Long id) {
        Document doc = findByIdAndAgency(id);
        doc.setDeletedAt(Instant.now());
        documentRepository.save(doc);
    }

    private Document findByIdAndAgency(Long id) {
        Document doc = documentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Document", id));
        Long agencyId = TenantContext.getAgencyId();
        if (!TenantContext.isSuperAdmin() && (agencyId == null || !agencyId.equals(doc.getAgencyId()))) {
            throw new ForbiddenException("Access denied");
        }
        if (doc.getDeletedAt() != null) throw new ResourceNotFoundException("Document", id);
        return doc;
    }

    private PageResponse<DocumentDto> toPageResponse(Page<Document> page) {
        List<DocumentDto> content = page.getContent().stream().map(this::toDto).collect(Collectors.toList());
        return PageResponse.<DocumentDto>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    private DocumentDto toDto(Document e) {
        return DocumentDto.builder()
                .id(e.getId())
                .agencyId(e.getAgencyId())
                .pilgrimId(e.getPilgrimId())
                .groupId(e.getGroupId())
                .type(e.getType())
                .fileUrl(e.getFileUrl())
                .status(e.getStatus())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
