package com.adamo.vrspfab.slots;

import com.adamo.vrspfab.vehicles.Vehicle;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SlotSpecification implements Specification<Slot> {

    private final Long vehicleId;
    private final Boolean isAvailable;
    private final LocalDateTime startDateTime;
    private final LocalDateTime endDateTime;

    public SlotSpecification(Long vehicleId, Boolean isAvailable, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        this.vehicleId = vehicleId;
        this.isAvailable = isAvailable;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
    }

    @Override
    public Predicate toPredicate(
            jakarta.persistence.criteria.Root<Slot> root,
            jakarta.persistence.criteria.CriteriaQuery<?> query,
            jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder) {

        List<Predicate> predicates = new ArrayList<>();

        if (vehicleId != null) {
            Join<Slot, Vehicle> vehicleJoin = root.join("vehicle");
            predicates.add(criteriaBuilder.equal(vehicleJoin.get("id"), vehicleId));
        }

        if (isAvailable != null) {
            predicates.add(criteriaBuilder.equal(root.get("available"), isAvailable));
        }

        if (startDateTime != null && endDateTime != null) {
            // Ensure slot starts before or at end of filter range, and ends after or at start of filter range
            // This correctly finds slots that overlap with the given range
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("startTime"), endDateTime));
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("endTime"), startDateTime));
        }

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
