package com.integrationlab.repository;

import com.integrationlab.model.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemSettingRepository extends JpaRepository<SystemSetting, String> {

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
     */
    @Query("SELECT s.settingValue FROM SystemSetting s WHERE s.settingKey = :settingKey")
    String getSettingValueByKey(@Param("settingKey") String settingKey);

    /**
     * Get all distinct categories
     */
    @Query("SELECT DISTINCT s.category FROM SystemSetting s WHERE s.category IS NOT NULL ORDER BY s.category")
    List<String> findAllCategories();
}