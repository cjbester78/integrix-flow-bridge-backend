package com.integrationlab.backend.controller;

import com.integrationlab.shared.dto.RecentMessageDTO;
import com.integrationlab.backend.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @GetMapping("/recent")
    public ResponseEntity<List<RecentMessageDTO>> getRecentMessages(
            @RequestParam(required = false) String businessComponentId,
            @RequestParam(defaultValue = "10") int limit) {
        List<RecentMessageDTO> messages = messageService.getRecentMessages(businessComponentId, limit);
        return ResponseEntity.ok(messages);
    }
}