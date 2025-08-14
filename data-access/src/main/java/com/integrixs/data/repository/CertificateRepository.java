package com.integrixs.data.repository;

import com.integrixs.data.model.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
/**
 * Repository interface for CertificateRepository.
 * Provides CRUD operations and query methods for the corresponding entity.
 */
public interface CertificateRepository extends JpaRepository<Certificate, String> {
}