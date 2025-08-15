package com.integrationlab.backend.controller;

import com.integrationlab.data.model.SystemLog;
import com.integrationlab.data.repository.SystemLogRepository;
import com.integrationlab.data.specification.SystemLogSpecifications;
import com.integrationlab.shared.dto.system.SystemLogCreateRequestDTO;
import com.integrationlab.shared.dto.system.SystemLogDTO;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class SystemLogController {

    private final SystemLogRepository systemLogRepository;

    public SystemLogController(SystemLogRepository systemLogRepository) {
        this.systemLogRepository = systemLogRepository;
    }

    @PostMapping("/logs")
    public ResponseEntity<Void> createLog(@RequestBody SystemLogCreateRequestDTO dto) {
        SystemLog log = new SystemLog();
        log.setLevel(SystemLog.LogLevel.valueOf(dto.getLevel()));
        log.setMessage(dto.getMessage());
        log.setDetails(dto.getDetails());
        log.setSource(dto.getSource());
        log.setSourceId(dto.getSourceId());
        log.setSourceName(dto.getSourceName());
        log.setComponent(dto.getComponent());
        log.setComponentId(dto.getComponentId());
        log.setDomainType(dto.getDomainType());
        log.setDomainReferenceId(dto.getDomainReferenceId());
        log.setUserId(dto.getUserId());
        log.setTimestamp(LocalDateTime.now());
        log.setCreatedAt(LocalDateTime.now());

        systemLogRepository.save(log);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/logs/system")
    public ResponseEntity<List<SystemLogDTO>> getLogs(
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String domainType,
            @RequestParam(required = false) String domainReferenceId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
    	SystemLog.LogLevel logLevel = level != null ? SystemLog.LogLevel.valueOf(level) : null;
    	List<SystemLog> logs = systemLogRepository.findAll(
            SystemLogSpecifications.withFilters(source, null, logLevel, userId, from, to)
                .and(SystemLogSpecifications.withDomainType(domainType))
                .and(SystemLogSpecifications.withDomainReferenceId(domainReferenceId))
        );

        List<SystemLogDTO> dtos = logs.stream().map(log -> {
            SystemLogDTO dto = new SystemLogDTO();
            dto.setId(log.getId());
            dto.setLevel(log.getLevel().name());
            dto.setMessage(log.getMessage());
            dto.setDetails(log.getDetails());
            dto.setSource(log.getSource());
            dto.setSourceId(log.getSourceId());
            dto.setSourceName(log.getSourceName());
            dto.setComponent(log.getComponent());
            dto.setComponentId(log.getComponentId());
            dto.setDomainType(log.getDomainType());
            dto.setDomainReferenceId(log.getDomainReferenceId());
            dto.setUserId(log.getUserId());
            dto.setTimestamp(log.getTimestamp());
            dto.setCreatedAt(log.getCreatedAt());
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }
    
    @GetMapping("/system-logs")
    public ResponseEntity<List<SystemLog>> getSystemLogsByCorrelation(
            @RequestParam(required = false) String correlationId,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        
        List<SystemLog> logs;
        
        if (correlationId != null && category != null) {
            logs = systemLogRepository.findByCorrelationIdAndCategoryOrderByTimestampDesc(correlationId, category);
        } else if (correlationId != null) {
            logs = systemLogRepository.findByCorrelationIdOrderByTimestampDesc(correlationId);
        } else if (category != null) {
            logs = systemLogRepository.findByCategoryOrderByTimestampDesc(category);
        } else {
            Pageable pageable = PageRequest.of(0, limit);
            logs = systemLogRepository.findAllByOrderByTimestampDesc(pageable);
        }
        
        // Apply limit (for non-pageable methods)
        if (!logs.isEmpty() && logs.size() > limit && category != null) {
            logs = logs.subList(0, limit);
        }
        
        return ResponseEntity.ok(logs);
    }
}
