package com.integrationlab.backend.service;

import com.integrationlab.backend.exception.BusinessException;
import com.integrationlab.data.model.CommunicationAdapter;
import com.integrationlab.data.model.SystemLog;
import com.integrationlab.data.repository.CommunicationAdapterRepository;
import com.integrationlab.data.repository.SystemLogRepository;
import com.integrationlab.shared.dto.AdapterStatusDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdapterMonitoringService {
    
    private final CommunicationAdapterRepository adapterRepository;
    private final SystemLogRepository systemLogRepository;
    
    // In-memory status tracking (in production, this would be in a cache or database)
    private final ConcurrentHashMap<String, AdapterStatusDTO> adapterStatuses = new ConcurrentHashMap<>();
    
    @Transactional(readOnly = true)
    public List<AdapterStatusDTO> getAdapterStatuses(String businessComponentId) {
        List<CommunicationAdapter> adapters;
        
        if (businessComponentId != null && !businessComponentId.isEmpty()) {
            // Since we don't have a direct findByBusinessComponentId method, we'll filter in memory
            adapters = adapterRepository.findAll().stream()
                    .filter(adapter -> businessComponentId.equals(adapter.getBusinessComponentId()))
                    .collect(Collectors.toList());
        } else {
            adapters = adapterRepository.findAll();
        }
        
        return adapters.stream()
                .map(this::getOrCreateAdapterStatus)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public AdapterStatusDTO getAdapterStatus(String adapterId) {
        CommunicationAdapter adapter = adapterRepository.findById(adapterId)
                .orElseThrow(() -> new BusinessException("Adapter not found: " + adapterId));
        
        return getOrCreateAdapterStatus(adapter);
    }
    
    @Transactional
    public AdapterStatusDTO startAdapter(String adapterId) {
        CommunicationAdapter adapter = adapterRepository.findById(adapterId)
                .orElseThrow(() -> new BusinessException("Adapter not found: " + adapterId));
        
        AdapterStatusDTO status = getOrCreateAdapterStatus(adapter);
        
        // Simulate starting the adapter
        log.info("Starting adapter: {} ({})", adapter.getName(), adapter.getType());
        status.setStatus("running");
        status.setLoad(10); // Initial load
        
        adapterStatuses.put(adapterId, status);
        return status;
    }
    
    @Transactional
    public AdapterStatusDTO stopAdapter(String adapterId) {
        CommunicationAdapter adapter = adapterRepository.findById(adapterId)
                .orElseThrow(() -> new BusinessException("Adapter not found: " + adapterId));
        
        AdapterStatusDTO status = getOrCreateAdapterStatus(adapter);
        
        // Simulate stopping the adapter
        log.info("Stopping adapter: {} ({})", adapter.getName(), adapter.getType());
        status.setStatus("stopped");
        status.setLoad(0);
        
        adapterStatuses.put(adapterId, status);
        return status;
    }
    
    @Transactional
    public AdapterStatusDTO restartAdapter(String adapterId) {
        // Stop first
        stopAdapter(adapterId);
        
        // Small delay simulation
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Then start
        return startAdapter(adapterId);
    }
    
    private AdapterStatusDTO getOrCreateAdapterStatus(CommunicationAdapter adapter) {
        // Always recalculate statistics to get fresh data
        AdapterStatusDTO status = new AdapterStatusDTO();
        status.setId(adapter.getId());
        status.setName(adapter.getName());
        status.setType(adapter.getType() != null ? adapter.getType().toString() : "UNKNOWN");
        status.setMode(adapter.getMode() != null ? adapter.getMode().toString() : "UNKNOWN");
        
        // Set status based on adapter configuration
        if (adapter.isActive()) {
            status.setStatus("active");
            status.setLoad(25); // Default load for active adapters
        } else {
            status.setStatus("inactive");
            status.setLoad(0);
        }
        
        status.setBusinessComponentId(adapter.getBusinessComponentId());
        // Get business component name from the relation if available
        if (adapter.getBusinessComponent() != null) {
            status.setBusinessComponentName(adapter.getBusinessComponent().getName());
        }
        
        // Calculate real metrics from system logs
        try {
            // Count flow executions for this adapter
            String searchPattern = String.format("adapter: %s", adapter.getName());
            List<SystemLog> adapterLogs = systemLogRepository.findByMessageContainingAndSourceOrderByTimestampDesc(
                searchPattern, "FlowExecutionService"
            );
            
            // Count successful messages
            long successCount = adapterLogs.stream()
                .filter(log -> log.getMessage().contains("SUCCESS") || log.getMessage().contains("success"))
                .count();
            
            // Count errors
            long errorCount = adapterLogs.stream()
                .filter(log -> log.getLevel() == SystemLog.LogLevel.ERROR)
                .count();
            
            status.setMessagesProcessed(successCount);
            status.setErrorsCount(errorCount);
            
            // Get last activity time
            if (!adapterLogs.isEmpty()) {
                SystemLog mostRecentLog = adapterLogs.get(0);
                status.setLastActivity(mostRecentLog.getTimestamp());
            } else {
                // Try to find any log mentioning this adapter
                List<SystemLog> anyAdapterLogs = systemLogRepository.findByMessageContainingOrderByTimestampDesc(
                    adapter.getName()
                );
                if (!anyAdapterLogs.isEmpty()) {
                    status.setLastActivity(anyAdapterLogs.get(0).getTimestamp());
                }
            }
            
        } catch (Exception e) {
            log.error("Error calculating adapter statistics for {}: {}", adapter.getName(), e.getMessage());
            // Set default values on error
            status.setMessagesProcessed(0L);
            status.setErrorsCount(0L);
        }
        
        // Cache the calculated status
        adapterStatuses.put(adapter.getId(), status);
        
        return status;
    }
}