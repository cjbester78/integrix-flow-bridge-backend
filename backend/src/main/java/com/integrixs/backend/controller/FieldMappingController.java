package com.integrixs.backend.controller;

import com.integrixs.backend.service.FieldMappingService;
import com.integrixs.shared.dto.mapping.FieldMappingDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transformations/{transformationId}/mappings")
public class FieldMappingController {

    @Autowired
    private FieldMappingService mappingService;

    @GetMapping
    public ResponseEntity<List<FieldMappingDTO>> getAllByTransformation(@PathVariable String transformationId) {
        return ResponseEntity.ok(mappingService.getByTransformationId(transformationId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FieldMappingDTO> getById(@PathVariable String transformationId, @PathVariable String id) {
        return mappingService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<FieldMappingDTO> create(@PathVariable String transformationId, @RequestBody FieldMappingDTO mapping) {
        mapping.setTransformationId(transformationId); // Ensure FK is set
        return ResponseEntity.ok(mappingService.save(mapping));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FieldMappingDTO> update(@PathVariable String transformationId, @PathVariable String id, @RequestBody FieldMappingDTO mapping) {
        mapping.setId(id);
        mapping.setTransformationId(transformationId);
        return ResponseEntity.ok(mappingService.save(mapping));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String transformationId, @PathVariable String id) {
        mappingService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
