package com.omra.platform.controller;

import com.omra.platform.service.UiTranslationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/public/translations")
@RequiredArgsConstructor
@Tag(name = "Public — UI translations", description = "Traductions interface (FR / AR), sans authentification")
public class PublicUiTranslationController {

    private final UiTranslationService uiTranslationService;

    @GetMapping
    @Operation(summary = "Obtenir toutes les clés pour une langue (fr par défaut, ar supporté)")
    public ResponseEntity<Map<String, String>> list(@RequestParam(defaultValue = "fr") String locale) {
        return ResponseEntity.ok(uiTranslationService.getMapForLocale(locale));
    }
}
