package com.integrationlab.data.specification;

import com.integrationlab.data.model.SystemLog;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specifications for SystemLog dynamic queries.
 * Provides reusable query predicates for flexible filtering.
 */
public class SystemLogSpecifications {

    /**
     * Creates a specification for filtering system logs with multiple optional parameters.
     * 
     * @param source The source system or component
     * @param category The log category
     * @param level The log level
     * @param userId The user ID
     * @param startDate The start date for timestamp filtering
     * @param endDate The end date for timestamp filtering
     * @return Specification for filtering system logs
     */
    public static Specification<SystemLog> withFilters(String source, 
                                                       String category, 
                                                       SystemLog.LogLevel level, 
                                                       String userId,
                                                       LocalDateTime startDate, 
                                                       LocalDateTime endDate) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (source != null) {
                predicates.add(criteriaBuilder.equal(root.get("source"), source));
            }
            
            if (category != null) {
                predicates.add(criteriaBuilder.equal(root.get("category"), category));
            }
            
            if (level != null) {
                predicates.add(criteriaBuilder.equal(root.get("level"), level));
            }
            
            if (userId != null) {
                predicates.add(criteriaBuilder.equal(root.get("userId"), userId));
            }
            
            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("timestamp"), startDate));
            }
            
            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("timestamp"), endDate));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
    
    /**
     * Creates a specification for filtering by component ID.
     * 
     * @param componentId The component ID to filter by
     * @return Specification for filtering by component ID
     */
    public static Specification<SystemLog> withComponentId(String componentId) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get("componentId"), componentId);
    }
    
    /**
     * Creates a specification for filtering by log level and date.
     * 
     * @param level The log level to filter by
     * @param after The date after which to include logs
     * @return Specification for filtering by level and date
     */
    public static Specification<SystemLog> withLevelAndDateAfter(SystemLog.LogLevel level, LocalDateTime after) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("level"), level),
                criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), after)
            );
    }
    
    /**
     * Creates a specification for filtering by domain type.
     * 
     * @param domainType The domain type to filter by
     * @return Specification for filtering by domain type
     */
    public static Specification<SystemLog> withDomainType(String domainType) {
        return (root, query, criteriaBuilder) -> {
            if (domainType == null || domainType.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("domainType"), domainType);
        };
    }
    
    /**
     * Creates a specification for filtering by domain reference ID.
     * 
     * @param domainReferenceId The domain reference ID to filter by
     * @return Specification for filtering by domain reference ID
     */
    public static Specification<SystemLog> withDomainReferenceId(String domainReferenceId) {
        return (root, query, criteriaBuilder) -> {
            if (domainReferenceId == null || domainReferenceId.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("domainReferenceId"), domainReferenceId);
        };
    }
}