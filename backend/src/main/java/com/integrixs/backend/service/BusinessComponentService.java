package com.integrixs.backend.service;

import com.integrixs.data.model.BusinessComponent;
import com.integrixs.data.repository.BusinessComponentRepository;
import com.integrixs.shared.dto.business.BusinessComponentDTO;
import com.integrixs.shared.dto.business.BusinessComponentCreateRequestDTO;
import com.integrixs.shared.dto.business.BusinessComponentUpdateRequestDTO;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
@Transactional
public class BusinessComponentService {

    private static final Logger logger = LoggerFactory.getLogger(BusinessComponentService.class);
    private final BusinessComponentRepository repository;

    public BusinessComponentService(BusinessComponentRepository repository) {
        this.repository = repository;
    }

    public BusinessComponentDTO createBusinessComponent(BusinessComponentCreateRequestDTO dto) {
        logger.info("Creating business component with name: {}", dto.getName());
        
        BusinessComponent businessComponent = new BusinessComponent();
        businessComponent.setName(dto.getName());
        businessComponent.setDescription(dto.getDescription());
        businessComponent.setContactEmail(dto.getContactEmail());
        businessComponent.setContactPhone(dto.getContactPhone());
        
        logger.debug("Saving business component: {}", businessComponent.getName());
        BusinessComponent saved = repository.save(businessComponent);
        logger.info("Successfully saved business component with ID: {}", saved.getId());
        
        return toDTO(saved);
    }

    public List<BusinessComponentDTO> getAllBusinessComponents() {
        return repository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<BusinessComponentDTO> getBusinessComponentById(String id) {
        return repository.findById(UUID.fromString(id)).map(this::toDTO);
    }

    public Optional<BusinessComponentDTO> updateBusinessComponent(String id, BusinessComponentUpdateRequestDTO dto) {
        return repository.findById(UUID.fromString(id)).map(businessComponent -> {
            businessComponent.setName(dto.getName());
            businessComponent.setDescription(dto.getDescription());
            businessComponent.setContactEmail(dto.getContactEmail());
            businessComponent.setContactPhone(dto.getContactPhone());
            return toDTO(repository.save(businessComponent));
        });
    }

    public void deleteBusinessComponent(String id) {
        repository.deleteById(UUID.fromString(id));
    }

    private BusinessComponentDTO toDTO(BusinessComponent businessComponent) {
        BusinessComponentDTO dto = new BusinessComponentDTO();
        dto.setId(businessComponent.getId().toString());
        dto.setName(businessComponent.getName());
        dto.setDescription(businessComponent.getDescription());
        dto.setContactEmail(businessComponent.getContactEmail());
        dto.setContactPhone(businessComponent.getContactPhone());
        dto.setCreatedAt(businessComponent.getCreatedAt());
        dto.setUpdatedAt(businessComponent.getUpdatedAt());
        return dto;
    }
}
