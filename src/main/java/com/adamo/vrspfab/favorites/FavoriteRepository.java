package com.adamo.vrspfab.favorites;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    @EntityGraph(
            attributePaths = {"user", "vehicle"},
            type = EntityGraph.EntityGraphType.LOAD
    )
    Optional<Favorite> findWithDetailsById(Long id);

    @Query("SELECT f FROM Favorite f WHERE f.user.id = :userId")
    List<Favorite> findByUserId(Long userId);


    @Query("SELECT f FROM Favorite f LEFT JOIN FETCH f.user LEFT JOIN FETCH f.vehicle")
    List<Favorite> findAllWithUserAndVehicle();
}