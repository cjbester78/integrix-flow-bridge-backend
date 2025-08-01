package com.integrationlab.controller;

import com.integrationlab.shared.dto.ChannelStatusDTO;
import com.integrationlab.service.ChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/channels")
@CrossOrigin(origins = "*")
public class ChannelController {

    @Autowired
    private ChannelService channelService;

    @GetMapping("/status")
    public ResponseEntity<List<ChannelStatusDTO>> getChannelStatuses(
            @RequestParam(required = false) String businessComponentId) {
        List<ChannelStatusDTO> statuses = channelService.getChannelStatuses(businessComponentId);
        return ResponseEntity.ok(statuses);
    }
}