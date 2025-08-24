package com.adamo.vrspfab.settings;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Repository
public interface OpeningHoursRepository extends JpaRepository<OpeningHours, Long> {

    Optional<OpeningHours> findByDayOfWeek(DayOfWeek dayOfWeek);

    List<OpeningHours> findByIsActiveTrue();

    @Query("SELECT oh FROM OpeningHours oh WHERE oh.isActive = true ORDER BY oh.dayOfWeek")
    List<OpeningHours> findAllActiveOrdered();

    boolean existsByDayOfWeek(DayOfWeek dayOfWeek);
}
