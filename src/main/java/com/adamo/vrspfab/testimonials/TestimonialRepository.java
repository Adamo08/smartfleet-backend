package com.adamo.vrspfab.testimonials;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestimonialRepository extends JpaRepository<Testimonial, Long> {
    @EntityGraph(attributePaths = {"user", "vehicle"}, type = EntityGraph.EntityGraphType.LOAD)
    Optional<Testimonial> findWithDetailsById(Long id);

    @Query("SELECT t FROM Testimonial t WHERE t.user.id = :userId AND t.isApproved = true")
    List<Testimonial> findApprovedByUserId(Long userId);


    @Query("SELECT t FROM Testimonial t LEFT JOIN FETCH t.user LEFT JOIN FETCH t.vehicle")
    List<Testimonial> findAllWithUserAndVehicle();
}