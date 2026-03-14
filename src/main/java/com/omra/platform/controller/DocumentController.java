package com.omra.platform.controller;

import com.omra.platform.dto.DocumentDto;
import com.omra.platform.dto.PageResponse;
import com.omra.platform.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Tag(name = "Documents", description = "Document management APIs")
public class DocumentController {

    private final DocumentService documentService;

    @GetMapping
    @Operation(summary = "Get documents (paginated)")
    public ResponseEntity<PageResponse<DocumentDto>> getDocuments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(documentService.getDocuments(PageRequest.of(page - 1, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get document by ID")
    public ResponseEntity<DocumentDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.getById(id));
    }

    @GetMapping("/pilgrim/{pilgrimId}")
    @Operation(summary = "Get documents by pilgrim")
    public ResponseEntity<List<DocumentDto>> getByPilgrim(@PathVariable Long pilgrimId) {
        return ResponseEntity.ok(documentService.getByPilgrim(pilgrimId));
    }

    @PostMapping
    @Operation(summary = "Create document (register file)")
    public ResponseEntity<DocumentDto> create(@RequestBody DocumentDto dto) {
        return ResponseEntity.ok(documentService.create(dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete document (soft delete)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        documentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
