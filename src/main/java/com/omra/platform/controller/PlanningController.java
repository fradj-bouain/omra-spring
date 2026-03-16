package com.omra.platform.controller;

import com.omra.platform.dto.PlanningDto;
import com.omra.platform.service.PlanningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plannings")
@RequiredArgsConstructor
@Tag(name = "Plannings", description = "Plannings = listes ordonnées de tâches pour les groupes Omra")
public class PlanningController {

    private final PlanningService planningService;

    @GetMapping
    @Operation(summary = "Liste des plannings de l'agence")
    public ResponseEntity<List<PlanningDto>> list() {
        return ResponseEntity.ok(planningService.listByAgency());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail d'un planning (avec items ordonnés)")
    public ResponseEntity<PlanningDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(planningService.getById(id));
    }

    @PostMapping
    @Operation(summary = "Créer un planning")
    public ResponseEntity<PlanningDto> create(@RequestBody PlanningDto dto) {
        return ResponseEntity.ok(planningService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un planning")
    public ResponseEntity<PlanningDto> update(@PathVariable Long id, @RequestBody PlanningDto dto) {
        return ResponseEntity.ok(planningService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un planning (soft)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        planningService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
