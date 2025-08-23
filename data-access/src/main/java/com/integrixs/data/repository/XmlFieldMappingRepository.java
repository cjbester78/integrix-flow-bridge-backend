package com.integrixs.data.repository;

import com.integrixs.data.model.XmlFieldMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for XmlFieldMapping entities.
 * 
 * @author Integration Team
 * @since 2.0.0
 */
@Repository
public interface XmlFieldMappingRepository extends JpaRepository<XmlFieldMapping, UUID> {

    /**
     * Find all mappings for a transformation ordered by mapping order
     * 
     * @param transformationId the transformation ID
     * @return list of mappings ordered by mapping_order
     */
    List<XmlFieldMapping> findByTransformationIdOrderByMappingOrderAsc(UUID transformationId);

    /**
     * Find mappings by source XPath pattern
     * 
     * @param xpathPattern the XPath pattern to search
     * @return list of matching mappings
     */
    List<XmlFieldMapping> findBySourceXPathContaining(String xpathPattern);

    /**
     * Count mappings for a transformation
     * 
     * @param transformationId the transformation ID
     * @return count of mappings
     */
    long countByTransformationId(UUID transformationId);

    /**
     * Delete all mappings for a transformation
     * 
     * @param transformationId the transformation ID
     */
    void deleteByTransformationId(UUID transformationId);
}