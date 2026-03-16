package com.omra.platform.controller;

import com.omra.platform.dto.TaskTemplateDto;
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
@Tag(name = "Task Templates", description = "Types de tâches prédéfinis (ex: Tawaf, Sa'i) avec durée")
public class TaskTemplateController {

    private final TaskTemplateService taskTemplateService;

    @GetMapping
    @Operation(summary = "Liste des types de tâches de l'agence")
    public ResponseEntity<List<TaskTemplateDto>> list() {
        return ResponseEntity.ok(taskTemplateService.listByAgency());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail d'un type de tâche")
    public ResponseEntity<TaskTemplateDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(taskTemplateService.getById(id));
    }

    @PostMapping
    @Operation(summary = "Créer un type de tâche")
    public ResponseEntity<TaskTemplateDto> create(@RequestBody TaskTemplateDto dto) {
        return ResponseEntity.ok(taskTemplateService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un type de tâche")
    public ResponseEntity<TaskTemplateDto> update(@PathVariable Long id, @RequestBody TaskTemplateDto dto) {
        return ResponseEntity.ok(taskTemplateService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un type de tâche (soft)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskTemplateService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
