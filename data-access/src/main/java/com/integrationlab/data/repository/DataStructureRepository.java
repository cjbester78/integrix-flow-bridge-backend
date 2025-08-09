package com.integrationlab.data.repository;

import com.integrationlab.data.model.DataStructure;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DataStructureRepository extends JpaRepository<DataStructure, String> {
    
    Optional<DataStructure> findByName(String name);
    
    List<DataStructure> findByType(String type);
    
    List<DataStructure> findByUsage(DataStructure.DataStructureUsage usage);
    
    List<DataStructure> findByBusinessComponentId(String businessComponentId);
    
    List<DataStructure> findByIsActiveTrue();
    
    @Query("SELECT d FROM DataStructure d WHERE " +
           "(:type IS NULL OR d.type = :type) AND " +
           "(:usage IS NULL OR d.usage = :usage) AND " +
           "(:businessComponentId IS NULL OR d.businessComponent.id = :businessComponentId) AND " +
           "(:search IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(d.description) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "d.isActive = true")
    Page<DataStructure> findWithFilters(@Param("type") String type,
                                       @Param("usage") DataStructure.DataStructureUsage usage,
                                       @Param("businessComponentId") String businessComponentId,
                                       @Param("search") String search,
                                       Pageable pageable);
    
    @Query("SELECT DISTINCT d.type FROM DataStructure d WHERE d.isActive = true")
    List<String> findDistinctTypes();
    
    @Query("SELECT d FROM DataStructure d WHERE d.isActive = true AND " +
           "EXISTS (SELECT 1 FROM d.tags t WHERE t IN :tags)")
    List<DataStructure> findByTagsIn(@Param("tags") List<String> tags);
    
    boolean existsByNameAndIdNot(String name, String id);
}