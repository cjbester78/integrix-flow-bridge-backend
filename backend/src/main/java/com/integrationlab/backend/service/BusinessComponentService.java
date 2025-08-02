package com.integrationlab.backend.service;

import com.integrationlab.data.model.BusinessComponent;
import com.integrationlab.data.repository.BusinessComponentRepository;
import com.integrationlab.shared.dto.business.BusinessComponentDTO;
import com.integrationlab.shared.dto.business.BusinessComponentCreateRequestDTO;
import com.integrationlab.shared.dto.business.BusinessComponentUpdateRequestDTO;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        return repository.findById(id).map(this::toDTO);
    }

    public Optional<BusinessComponentDTO> updateBusinessComponent(String id, BusinessComponentUpdateRequestDTO dto) {
        return repository.findById(id).map(businessComponent -> {
            businessComponent.setName(dto.getName());
            businessComponent.setDescription(dto.getDescription());
            businessComponent.setContactEmail(dto.getContactEmail());
            businessComponent.setContactPhone(dto.getContactPhone());
            return toDTO(repository.save(businessComponent));
        });
    }

    public void deleteBusinessComponent(String id) {
        repository.deleteById(id);
    }

    private BusinessComponentDTO toDTO(BusinessComponent businessComponent) {
        BusinessComponentDTO dto = new BusinessComponentDTO();
        dto.setId(businessComponent.getId());
        dto.setName(businessComponent.getName());
        dto.setDescription(businessComponent.getDescription());
        dto.setContactEmail(businessComponent.getContactEmail());
        dto.setContactPhone(businessComponent.getContactPhone());
        dto.setCreatedAt(businessComponent.getCreatedAt());
        dto.setUpdatedAt(businessComponent.getUpdatedAt());
        return dto;
    }
}
