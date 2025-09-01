package com.adamo.vrspfab.dashboard;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {
    
    /**
     * Find activities ordered by creation time (most recent first)
     */
    List<Activity> findByOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * Find activities by type ordered by creation time
     */
    List<Activity> findByActivityTypeOrderByCreatedAtDesc(ActivityType activityType, Pageable pageable);
    
    /**
     * Find activities created within a time range
     */
    @Query("SELECT a FROM Activity a WHERE a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    List<Activity> findByCreatedAtBetweenOrderByCreatedAtDesc(
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
    
    /**
     * Find activities for dashboard (recent activities)
     */
    @Query("SELECT a FROM Activity a ORDER BY a.createdAt DESC")
    Page<Activity> findRecentActivities(Pageable pageable);
    
    /**
     * Count activities by type within a date range
     */
    @Query("SELECT COUNT(a) FROM Activity a WHERE a.activityType = :type AND a.createdAt BETWEEN :startDate AND :endDate")
    Long countByActivityTypeAndCreatedAtBetween(
            @Param("type") ActivityType type,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find activities related to a specific entity
     */
    List<Activity> findByRelatedEntityTypeAndRelatedEntityIdOrderByCreatedAtDesc(
            String entityType, Long entityId, Pageable pageable);
}
