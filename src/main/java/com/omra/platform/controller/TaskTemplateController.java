package com.omra.platform.controller;

import com.omra.platform.dto.TaskTemplateDto;
import com.omra.platform.dto.TaskTemplateTotalDurationResponse;
import com.omra.platform.service.TaskTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/task-templates")
@RequiredArgsConstructor
@Tag(name = "Task Templates", description = "Types de tâches prédéfinis (hiérarchie : sous-types) avec durée")
public class TaskTemplateController {

    private final TaskTemplateService taskTemplateService;

    @GetMapping
    @Operation(summary = "Liste plate des types de tâches de l'agence (plannings, sélection)")
    public ResponseEntity<List<TaskTemplateDto>> list() {
        return ResponseEntity.ok(taskTemplateService.listByAgency());
    }

    @GetMapping("/tree")
    @Operation(summary = "Arborescence des types de tâches (racines + enfants)")
    public ResponseEntity<List<TaskTemplateDto>> tree() {
        return ResponseEntity.ok(taskTemplateService.getTree());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail plat d'un type de tâche")
    public ResponseEntity<TaskTemplateDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(taskTemplateService.getById(id));
    }

    @GetMapping("/{id}/tree")
    @Operation(summary = "Nœud avec tout le sous-arbre")
    public ResponseEntity<TaskTemplateDto> getByIdWithTree(@PathVariable Long id) {
        return ResponseEntity.ok(taskTemplateService.getByIdAsTree(id));
    }

    @GetMapping("/{id}/total-duration")
    @Operation(summary = "Durée totale (minutes) : ce modèle + sous-types")
    public ResponseEntity<TaskTemplateTotalDurationResponse> totalDuration(@PathVariable Long id) {
        return ResponseEntity.ok(taskTemplateService.getTotalDuration(id));
    }

    @PostMapping
    @Operation(summary = "Créer un type de tâche (racine ou sous-type via parentId)")
    public ResponseEntity<TaskTemplateDto> create(@RequestBody TaskTemplateDto dto) {
        return ResponseEntity.ok(taskTemplateService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un type de tâche")
    public ResponseEntity<TaskTemplateDto> update(@PathVariable Long id, @RequestBody TaskTemplateDto dto) {
        return ResponseEntity.ok(taskTemplateService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un type de tâche et ses sous-types (soft delete)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskTemplateService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
