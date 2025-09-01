package com.adamo.vrspfab.testimonials;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.lang.NonNull;

import java.util.Optional;

@Repository
public interface TestimonialRepository extends JpaRepository<Testimonial, Long> {
    @EntityGraph(attributePaths = {"user", "vehicle"}, type = EntityGraph.EntityGraphType.LOAD)
    Optional<Testimonial> findWithDetailsById(Long id);

    // Explicitly annotate findAll methods with @NonNull
    @Override
    @NonNull
    @EntityGraph(attributePaths = {"user", "vehicle"}, type = EntityGraph.EntityGraphType.LOAD)
    Page<Testimonial> findAll(@NonNull Pageable pageable);

    @EntityGraph(attributePaths = {"user", "vehicle"}, type = EntityGraph.EntityGraphType.LOAD)
    Page<Testimonial> findByApproved(boolean approved, @NonNull Pageable pageable);

    @EntityGraph(attributePaths = {"user", "vehicle"}, type = EntityGraph.EntityGraphType.LOAD)
    Page<Testimonial> findByUserId(Long userId, @NonNull Pageable pageable);

    @EntityGraph(attributePaths = {"user", "vehicle"}, type = EntityGraph.EntityGraphType.LOAD)
    Page<Testimonial> findByVehicleId(Long vehicleId, @NonNull Pageable pageable);

    @EntityGraph(attributePaths = {"user", "vehicle"}, type = EntityGraph.EntityGraphType.LOAD)
    Page<Testimonial> findByUserIdAndApproved(Long userId, boolean approved, @NonNull Pageable pageable);

    @EntityGraph(attributePaths = {"user", "vehicle"}, type = EntityGraph.EntityGraphType.LOAD)
    Page<Testimonial> findByVehicleIdAndApproved(Long vehicleId, boolean approved, @NonNull Pageable pageable);

    @EntityGraph(attributePaths = {"user", "vehicle"}, type = EntityGraph.EntityGraphType.LOAD)
    Page<Testimonial> findByUserIdAndVehicleIdAndApproved(Long userId, Long vehicleId, boolean approved, @NonNull Pageable pageable);


    @EntityGraph(attributePaths = {"user", "vehicle"}, type = EntityGraph.EntityGraphType.LOAD)
    Optional<Testimonial> findByUserIdAndVehicleId(Long userId, Long vehicleId);

    // Methods for admin functionality
    @EntityGraph(attributePaths = {"user", "vehicle"}, type = EntityGraph.EntityGraphType.LOAD)
    java.util.List<Testimonial> findByApprovedFalse();

    long countByApprovedTrue();
    
    long countByApprovedFalse();
    
    double findAverageRatingByApprovedTrue();
}
