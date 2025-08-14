package com.integrixs.backend.controller;

import com.integrixs.shared.dto.ChannelStatusDTO;
import com.integrixs.backend.service.ChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/channels")
@CrossOrigin(origins = "*")
public class ChannelController {

    @Autowired
    private ChannelService channelService;

    @GetMapping
    public ResponseEntity<List<ChannelStatusDTO>> getChannels(
            @RequestParam(required = false) String businessComponentId) {
        try {
            // Use the same service method as status endpoint for now
            List<ChannelStatusDTO> channels = channelService.getChannelStatuses(businessComponentId);
            return ResponseEntity.ok(channels);
        } catch (Exception e) {
            // Log the error and return an empty list to avoid 500 errors
            System.err.println("Error in getChannels: " + e.getMessage());
            e.printStackTrace();
            
            // Return empty list to prevent frontend errors
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    @GetMapping("/status")
    public ResponseEntity<List<ChannelStatusDTO>> getChannelStatuses(
            @RequestParam(required = false) String businessComponentId) {
        try {
            List<ChannelStatusDTO> statuses = channelService.getChannelStatuses(businessComponentId);
            return ResponseEntity.ok(statuses);
        } catch (Exception e) {
            // Log the error and return an empty list to avoid 500 errors
            System.err.println("Error in getChannelStatuses: " + e.getMessage());
            e.printStackTrace();
            
            // Return empty list to prevent frontend errors
            return ResponseEntity.ok(new ArrayList<>());
        }
    }
    
    @GetMapping("/test")
    public ResponseEntity<List<ChannelStatusDTO>> testChannelStatuses() {
        // Test endpoint with mock data
        List<ChannelStatusDTO> mockStatuses = new ArrayList<>();
        mockStatuses.add(ChannelStatusDTO.builder()
                .name("Test Channel 1")
                .status("running")
                .load(50)
                .businessComponentId("test-bc-1")
                .build());
        mockStatuses.add(ChannelStatusDTO.builder()
                .name("Test Channel 2")
                .status("idle")
                .load(10)
                .businessComponentId("test-bc-1")
                .build());
        return ResponseEntity.ok(mockStatuses);
    }
}