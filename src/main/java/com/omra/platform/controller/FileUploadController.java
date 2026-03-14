package com.omra.platform.controller;

import com.omra.platform.service.StorageService;
import com.omra.platform.util.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "Files", description = "File upload (passport, visa, photo, ticket)")
public class FileUploadController {

    private final StorageService storageService;

    @PostMapping("/upload")
    @Operation(summary = "Upload file (passport, visa, photo, ticket)")
    public ResponseEntity<Map<String, String>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", defaultValue = "general") String type) {
        Long agencyId = TenantContext.getAgencyId();
        String prefix = agencyId != null ? "agency-" + agencyId + "/" + type : type;
        String url = storageService.upload(file, prefix);
        Map<String, String> body = new HashMap<>();
        body.put("file_url", url);
        body.put("type", type);
        return ResponseEntity.ok(body);
    }
}
