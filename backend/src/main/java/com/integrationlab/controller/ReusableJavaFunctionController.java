package com.integrationlab.controller;

import com.integrationlab.model.ReusableFunction;
import com.integrationlab.service.ReusableJavaFunctionService;
import com.integrationlab.shared.dto.transformation.ReusableJavaFunctionDTO;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reusable-functions")
@Validated
public class ReusableJavaFunctionController {

    private final ReusableJavaFunctionService functionService;

    public ReusableJavaFunctionController(ReusableJavaFunctionService functionService) {
        this.functionService = functionService;
    }

    // DTO to Entity conversion
    private ReusableFunction fromDTO(ReusableJavaFunctionDTO dto) {
        var entity = new ReusableFunction();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setVersion(dto.getVersion());
        entity.setFunctionBody(dto.getFunctionBody());
        entity.setInputTypes(dto.getInputTypes());
        entity.setOutputType(dto.getOutputType());
        entity.setDescription(dto.getDescription());
        return entity;
    }

    // Entity to DTO conversion
    private ReusableJavaFunctionDTO toDTO(ReusableFunction entity) {
        var dto = new ReusableJavaFunctionDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setVersion(entity.getVersion());
        dto.setFunctionBody(entity.getFunctionBody());
        dto.setInputTypes(entity.getInputTypes());
        dto.setOutputType(entity.getOutputType());
        dto.setDescription(entity.getDescription());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    @GetMapping
    public ResponseEntity<List<ReusableJavaFunctionDTO>> getAll() {
        List<ReusableJavaFunctionDTO> dtos = functionService.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReusableJavaFunctionDTO> getById(@PathVariable String id) {
        Optional<ReusableFunction> functionOpt = functionService.findById(id);
        return functionOpt.map(f -> ResponseEntity.ok(toDTO(f)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ReusableJavaFunctionDTO> create(@Valid @RequestBody ReusableJavaFunctionDTO dto) {
        // Ignore incoming ID to allow DB to generate it
        dto.setId(null);
        ReusableFunction saved = functionService.save(fromDTO(dto));
        return new ResponseEntity<>(toDTO(saved), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReusableJavaFunctionDTO> update(@PathVariable String id,
                                                          @Valid @RequestBody ReusableJavaFunctionDTO dto) {
        Optional<ReusableFunction> existing = functionService.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        ReusableFunction entityToUpdate = fromDTO(dto);
        entityToUpdate.setId(id);
        ReusableFunction updated = functionService.save(entityToUpdate);
        return ResponseEntity.ok(toDTO(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        Optional<ReusableFunction> existing = functionService.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        functionService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
