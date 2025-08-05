package com.adamo.vrspfab.bookmarks;


import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    @EntityGraph(
            attributePaths = {"user", "reservation"},
            type = EntityGraph.EntityGraphType.LOAD
    )
    Optional<Bookmark> findWithDetailsById(Long id);

    @Query("SELECT b FROM Bookmark b WHERE b.user.id = :userId")
    List<Bookmark> findByUserId(Long userId);

    @Query("SELECT b FROM Bookmark b LEFT JOIN FETCH b.user LEFT JOIN FETCH b.reservation")
    List<Bookmark> findAllWithUserAndReservation();
}
