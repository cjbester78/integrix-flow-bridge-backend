package com.integrixs.backend.service;

import com.integrixs.shared.dto.ChannelStatusDTO;
import com.integrixs.data.model.CommunicationAdapter;
import com.integrixs.data.repository.CommunicationAdapterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ChannelService {

    private static final Logger logger = LoggerFactory.getLogger(ChannelService.class);

    @Autowired
    private CommunicationAdapterRepository adapterRepository;

    public List<ChannelStatusDTO> getChannelStatuses(String businessComponentId) {
        try {
            logger.info("Getting channel statuses for businessComponentId: {}", businessComponentId);
            
            // Fetch adapters based on business component filter
            List<CommunicationAdapter> adapters;
            
            if (businessComponentId != null) {
                logger.debug("Fetching adapters for business component: {}", businessComponentId);
                adapters = adapterRepository.findByBusinessComponentId(UUID.fromString(businessComponentId));
            } else {
                logger.debug("Fetching all adapters");
                adapters = adapterRepository.findAll();
            }
            
            logger.info("Found {} adapters", adapters.size());

            List<ChannelStatusDTO> result = adapters.stream()
                    .filter(adapter -> adapter.isActive())
                    .map(adapter -> {
                        try {
                            // Convert to DTO within the transaction
                            String status = adapter.isActive() ? "running" : "stopped";
                            int load = adapter.isActive() ? (int)(Math.random() * 80 + 20) : 0;
                            
                            // Safely get business component ID within transaction
                            String bcId = null;
                            if (adapter.getBusinessComponent() != null) {
                                bcId = adapter.getBusinessComponent().getId().toString();
                            }
                            
                            ChannelStatusDTO dto = ChannelStatusDTO.builder()
                                    .name(adapter.getName())
                                    .status(status)
                                    .load(load)
                                    .businessComponentId(bcId)
                                    .build();
                                    
                            logger.debug("Created ChannelStatusDTO: {}", dto);
                            return dto;
                        } catch (Exception e) {
                            logger.error("Error converting adapter to DTO: {}", adapter.getName(), e);
                            throw new RuntimeException("Error converting adapter: " + adapter.getName(), e);
                        }
                    })
                    .collect(Collectors.toList());
                    
            logger.info("Returning {} channel statuses", result.size());
            return result;
            
        } catch (Exception e) {
            logger.error("Error fetching channel statuses", e);
            throw new RuntimeException("Error fetching channel statuses: " + e.getMessage(), e);
        }
    }

    private ChannelStatusDTO convertToChannelStatusDTO(CommunicationAdapter adapter) {
        try {
            // Determine status based on adapter state
            String status = adapter.isActive() ? "running" : "stopped";
            
            // Calculate load based on adapter activity (mock for now)
            int load = adapter.isActive() ? (int)(Math.random() * 80 + 20) : 0;

            // Safely get business component ID
            String bcId = null;
            if (adapter.getBusinessComponent() != null) {
                bcId = adapter.getBusinessComponent().getId().toString();
            }

            return ChannelStatusDTO.builder()
                    .name(adapter.getName())
                    .status(status)
                    .load(load)
                    .businessComponentId(bcId)
                    .build();
        } catch (Exception e) {
            // Log error and return minimal DTO
            e.printStackTrace();
            return ChannelStatusDTO.builder()
                    .name(adapter.getName() != null ? adapter.getName() : "Unknown")
                    .status("error")
                    .load(0)
                    .businessComponentId(null)
                    .build();
        }
    }
}