package com.integrationlab.service;

import com.integrationlab.shared.dto.ChannelStatusDTO;
import com.integrationlab.model.CommunicationAdapter;
import com.integrationlab.repository.CommunicationAdapterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChannelService {

    @Autowired
    private CommunicationAdapterRepository adapterRepository;

    public List<ChannelStatusDTO> getChannelStatuses(String businessComponentId) {
        List<CommunicationAdapter> adapters = businessComponentId != null
            ? adapterRepository.findByBusinessComponentId(businessComponentId)
            : adapterRepository.findAll();

        return adapters.stream()
                .filter(adapter -> adapter.isActive())
                .map(this::convertToChannelStatusDTO)
                .collect(Collectors.toList());
    }

    private ChannelStatusDTO convertToChannelStatusDTO(CommunicationAdapter adapter) {
        // Determine status based on adapter state
        String status = adapter.isActive() ? "running" : "stopped";
        
        // Calculate load based on adapter activity (mock for now)
        int load = adapter.isActive() ? (int)(Math.random() * 80 + 20) : 0;

        return ChannelStatusDTO.builder()
                .name(adapter.getName())
                .status(status)
                .load(load)
                .businessComponentId(adapter.getBusinessComponentId())
                .build();
    }
}