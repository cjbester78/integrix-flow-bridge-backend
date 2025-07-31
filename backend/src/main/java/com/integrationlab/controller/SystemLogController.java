package com.integrationlab.controller;

import com.integrationlab.model.SystemLog;
import com.integrationlab.repository.SystemLogRepository;
import com.integrationlab.shared.dto.system.SystemLogCreateRequestDTO;
import com.integrationlab.shared.dto.system.SystemLogDTO;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/logs")
public class SystemLogController {

    private final SystemLogRepository systemLogRepository;

    public SystemLogController(SystemLogRepository systemLogRepository) {
        this.systemLogRepository = systemLogRepository;
    }

    @PostMapping
    public ResponseEntity<Void> createLog(@RequestBody SystemLogCreateRequestDTO dto) {
        SystemLog log = new SystemLog();
        log.setLevel(dto.getLevel());
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

    @GetMapping("/system")
    public ResponseEntity<List<SystemLogDTO>> getLogs(
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
    	List<SystemLog> logs = systemLogRepository.findFiltered(level, source, userId, from, to);

        List<SystemLogDTO> dtos = logs.stream().map(log -> {
            SystemLogDTO dto = new SystemLogDTO();
            dto.setId(log.getId());
            dto.setLevel(log.getLevel());
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
            dto.setTimestamp(log.getTimestamp() != null ? log.getTimestamp().format(DateTimeFormatter.ISO_DATE_TIME) : null);
            dto.setCreatedAt(log.getCreatedAt() != null ? log.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME) : null);
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }
}
