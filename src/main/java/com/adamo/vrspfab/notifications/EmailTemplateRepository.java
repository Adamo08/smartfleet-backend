package com.adamo.vrspfab.notifications;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long>, JpaSpecificationExecutor<EmailTemplate> {
    
    Optional<EmailTemplate> findByName(String name);
    
    Optional<EmailTemplate> findByTemplateFile(String templateFile);
    
    List<EmailTemplate> findByType(NotificationType type);
    
    List<EmailTemplate> findByCategory(String category);
    
    List<EmailTemplate> findByIsActiveTrue();
    
    @Query("SELECT t FROM EmailTemplate t WHERE t.isActive = true AND t.type = :type")
    Optional<EmailTemplate> findActiveByType(@Param("type") NotificationType type);
    
    @Query("SELECT DISTINCT t.category FROM EmailTemplate t")
    List<String> findAllCategories();
    
    @Query("SELECT COUNT(t) FROM EmailTemplate t WHERE t.category = :category")
    Long countByCategory(@Param("category") String category);
    
    @Query("SELECT t FROM EmailTemplate t WHERE " +
           "(:name IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:category IS NULL OR t.category = :category) AND " +
           "(:type IS NULL OR t.type = :type) AND " +
           "(:isActive IS NULL OR t.isActive = :isActive)")
    Page<EmailTemplate> findByFilters(
            @Param("name") String name,
            @Param("category") String category,
            @Param("type") NotificationType type,
            @Param("isActive") Boolean isActive,
            Pageable pageable
    );
}
