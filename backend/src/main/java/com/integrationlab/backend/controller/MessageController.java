package com.integrationlab.backend.controller;

import com.integrationlab.shared.dto.RecentMessageDTO;
import com.integrationlab.shared.dto.MessageDTO;
import com.integrationlab.shared.dto.MessageStatsDTO;
import com.integrationlab.backend.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*")
public class MessageController {

    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    @Autowired
    private MessageService messageService;

    @GetMapping("/recent")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'VIEWER')")
    public ResponseEntity<List<RecentMessageDTO>> getRecentMessages(
            @RequestParam(required = false) String businessComponentId,
            @RequestParam(defaultValue = "10") int limit) {
        List<RecentMessageDTO> messages = messageService.getRecentMessages(businessComponentId, limit);
        return ResponseEntity.ok(messages);
    }
    
    /**
     * Get messages with optional filtering
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'VIEWER')")
    public ResponseEntity<Map<String, Object>> getMessages(
            @RequestParam(required = false) List<String> status,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String target,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        logger.debug("Getting messages with filters - status: {}, source: {}, target: {}", status, source, target);
        
        Map<String, Object> filters = new HashMap<>();
        if (status != null && !status.isEmpty()) filters.put("status", status);
        if (source != null) filters.put("source", source);
        if (target != null) filters.put("target", target);
        if (type != null) filters.put("type", type);
        if (dateFrom != null) filters.put("dateFrom", dateFrom);
        if (dateTo != null) filters.put("dateTo", dateTo);
        if (search != null) filters.put("search", search);
        
        Map<String, Object> result = messageService.getMessages(filters, page, size);
        return ResponseEntity.ok(result);
    }
    
    /**
     * Get a specific message by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'VIEWER')")
    public ResponseEntity<MessageDTO> getMessage(@PathVariable String id) {
        logger.debug("Getting message with id: {}", id);
        MessageDTO message = messageService.getMessageById(id);
        return ResponseEntity.ok(message);
    }
    
    /**
     * Get message statistics
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'VIEWER')")
    public ResponseEntity<MessageStatsDTO> getMessageStats(
            @RequestParam(required = false) List<String> status,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String target,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo) {
        
        logger.debug("Getting message statistics");
        
        Map<String, Object> filters = new HashMap<>();
        if (status != null && !status.isEmpty()) filters.put("status", status);
        if (source != null) filters.put("source", source);
        if (target != null) filters.put("target", target);
        if (type != null) filters.put("type", type);
        if (dateFrom != null) filters.put("dateFrom", dateFrom);
        if (dateTo != null) filters.put("dateTo", dateTo);
        
        MessageStatsDTO stats = messageService.getMessageStats(filters);
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Reprocess a failed message
     */
    @PostMapping("/{id}/reprocess")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR')")
    public ResponseEntity<MessageDTO> reprocessMessage(@PathVariable String id) {
        logger.info("Reprocessing message with id: {}", id);
        MessageDTO message = messageService.reprocessMessage(id);
        return ResponseEntity.ok(message);
    }
}