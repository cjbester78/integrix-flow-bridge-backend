package com.integrixs.data.repository;

import com.integrixs.data.model.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SystemSettingRepository extends JpaRepository<SystemSetting, UUID> {

    /**
     * Find a system setting by its key
     */
    Optional<SystemSetting> findBySettingKey(String settingKey);

    /**
     * Find all settings by category
     */
    List<SystemSetting> findByCategory(String category);

    /**
     * Find all settings by category ordered by setting key
     */
    List<SystemSetting> findByCategoryOrderBySettingKeyAsc(String category);

    /**
     * Find all non-readonly settings
     */
    List<SystemSetting> findByIsReadonlyFalse();

    /**
     * Find all settings by category that are not readonly
     */
    List<SystemSetting> findByCategoryAndIsReadonlyFalse(String category);

    /**
     * Check if a setting key exists
     */
    boolean existsBySettingKey(String settingKey);

    /**
     * Get setting value by key (returns null if not found)
     * Note: This needs custom implementation in service layer to extract just the value
     */

    /**
     * Get all settings with non-null categories for distinct processing in service layer
     */
    List<SystemSetting> findByCategoryIsNotNullOrderByCategory();
}