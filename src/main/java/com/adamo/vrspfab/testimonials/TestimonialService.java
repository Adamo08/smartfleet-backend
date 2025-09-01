package com.adamo.vrspfab.testimonials;

import com.adamo.vrspfab.common.ResourceNotFoundException;
import com.adamo.vrspfab.common.SecurityUtilsService;
import com.adamo.vrspfab.dashboard.ActivityEventListener;
import com.adamo.vrspfab.users.User;
import com.adamo.vrspfab.users.UserService;
import com.adamo.vrspfab.vehicles.Vehicle;
import com.adamo.vrspfab.vehicles.mappers.VehicleMapper;
import com.adamo.vrspfab.vehicles.VehicleService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import com.adamo.vrspfab.users.Role;

import java.util.Optional;
import java.util.List;


@AllArgsConstructor
@Service
public class TestimonialService {

    private static final Logger logger = LoggerFactory.getLogger(TestimonialService.class);

    private final TestimonialRepository testimonialRepository;
    private final TestimonialMapper testimonialMapper;
    private final UserService userService;
    private final VehicleService vehicleService;
    private final VehicleMapper vehicleMapper;
    private final SecurityUtilsService securityUtilsService;
    private final ActivityEventListener activityEventListener;


    @Transactional
    public TestimonialDto createTestimonial(TestimonialDto testimonialDto) {
        logger.info("Attempting to create testimonial: {}", testimonialDto);

        User authenticatedUser = securityUtilsService.getCurrentAuthenticatedUser();
        // Ensure the user is creating a testimonial for themselves
        if (!authenticatedUser.getId().equals(testimonialDto.getUserId())) {
            logger.warn("User {} attempted to create testimonial for user ID {}. Access denied.", authenticatedUser.getId(), testimonialDto.getUserId());
            throw new AccessDeniedException("User can only create testimonials for themselves.");
        }

        User user = userService.getUserById(testimonialDto.getUserId())
                .orElseThrow(() -> {
                    logger.warn("User not found with ID: {}", testimonialDto.getUserId());
                    return new ResourceNotFoundException("User not found with ID: " + testimonialDto.getUserId(), "User");
                });

        Vehicle vehicle = null;
        if (testimonialDto.getVehicleId() != null) {
            var vehicleDto = vehicleService.getVehicleById(testimonialDto.getVehicleId());

            vehicle = vehicleMapper.toEntity(vehicleDto);

            if (vehicle == null) {
                logger.warn("Vehicle not found with ID: {}", testimonialDto.getVehicleId());
                throw new ResourceNotFoundException("Vehicle not found with ID: " + testimonialDto.getVehicleId(), "Vehicle");
            }
        } else {
            logger.info("Testimonial is not associated with a specific vehicle.");
        }

        // Check for existing testimonial by the same user for the same vehicle
        if (testimonialDto.getVehicleId() != null) { // Only check if vehicleId is provided
            Optional<Testimonial> existingTestimonial = testimonialRepository.findByUserIdAndVehicleId(user.getId(), testimonialDto.getVehicleId());
            if (existingTestimonial.isPresent()) {
                logger.warn("User {} has already submitted a testimonial for vehicle {}.", user.getId(), testimonialDto.getVehicleId());
                throw new DuplicateTestimonialException("You have already submitted a testimonial for this vehicle.");
            }
        }


        Testimonial testimonial = testimonialMapper.toEntity(testimonialDto);
        testimonial.setUser(user);
        testimonial.setVehicle(vehicle);
        testimonial.setApproved(false);
        testimonial.setAdminReplyContent(null);

        Testimonial savedTestimonial = testimonialRepository.save(testimonial);
        logger.info("Testimonial created successfully with ID: {}", savedTestimonial.getId());
        return testimonialMapper.toDto(savedTestimonial);
    }

    @Transactional(readOnly = true)
    public TestimonialDto getTestimonialById(Long id) {
        logger.debug("Fetching testimonial with ID: {}", id);
        Testimonial testimonial = testimonialRepository.findWithDetailsById(id)
                .orElseThrow(() -> {
                    logger.warn("Testimonial not found with ID: {}", id);
                    return new ResourceNotFoundException("Testimonial not found with ID: " + id, "Testimonial");
                });

        User authenticatedUser = securityUtilsService.getCurrentAuthenticatedUser();
        boolean isAdmin = authenticatedUser.getRole().equals(Role.ADMIN);
        boolean isOwner = testimonial.getUser().getId().equals(authenticatedUser.getId());

        logger.debug("Authenticated user ID: {}, Testimonial owner ID: {}, Is Admin: {}, Is Owner: {}",
                authenticatedUser.getId(), testimonial.getUser().getId(), isAdmin, isOwner);

        if (!testimonial.isApproved() && !isOwner && !isAdmin) {
            logger.warn("Access denied for testimonial ID: {}. Not approved and user is not owner or admin.", id);
            throw new AccessDeniedException("You do not have permission to view this testimonial.");
        }

        return testimonialMapper.toDto(testimonial);
    }

    @Transactional
    public TestimonialDto approveTestimonial(Long id) {
        logger.info("Attempting to approve testimonial with ID: {}", id);
        Testimonial testimonial = testimonialRepository.findWithDetailsById(id)
                .orElseThrow(() -> {
                    logger.warn("Testimonial not found with ID: {}", id);
                    return new ResourceNotFoundException("Testimonial not found with ID: " + id, "Testimonial");
                });
        if (testimonial.isApproved()) {
            logger.warn("Testimonial with ID: {} is already approved.", id);
            throw new IllegalStateException("Testimonial is already approved");
        }
        testimonial.setApproved(true);
        Testimonial updatedTestimonial = testimonialRepository.save(testimonial);
        logger.info("Testimonial with ID: {} approved successfully.", id);
        
        // Record testimonial approval activity
        try {
            User currentUser = securityUtilsService.getCurrentAuthenticatedUser();
            activityEventListener.recordTestimonialAction("approved", updatedTestimonial.getId(), 
                updatedTestimonial.getTitle(), currentUser);
        } catch (Exception e) {
            logger.warn("Could not record testimonial approval activity: {}", e.getMessage());
        }
        
        return testimonialMapper.toDto(updatedTestimonial);
    }

    @Transactional
    public TestimonialDto updateTestimonial(Long id, TestimonialDto testimonialDto) {
        logger.info("Attempting to update testimonial with ID: {}", id);
        Testimonial existingTestimonial = testimonialRepository.findWithDetailsById(id)
                .orElseThrow(() -> {
                    logger.warn("Testimonial not found with ID: {}", id);
                    return new ResourceNotFoundException("Testimonial not found with ID: " + id, "Testimonial");
                });

        testimonialMapper.updateTestimonialFromDto(testimonialDto, existingTestimonial);

        if (testimonialDto.getAdminReplyContent() != null) {
            existingTestimonial.setAdminReplyContent(testimonialDto.getAdminReplyContent());
            logger.info("Admin reply added/updated for testimonial ID: {}", id);
        }
        if (testimonialDto.getContent() != null && !testimonialDto.getContent().isBlank()) {
            existingTestimonial.setContent(testimonialDto.getContent());
            logger.info("Content updated for testimonial ID: {}", id);
        }
        if (testimonialDto.getRating() > 0) {
            existingTestimonial.setRating(testimonialDto.getRating());
            logger.info("Rating updated for testimonial ID: {}", id);
        }
        if (testimonialDto.getTitle() != null && !testimonialDto.getTitle().isBlank()) {
            existingTestimonial.setTitle(testimonialDto.getTitle());
            logger.info("Title updated for testimonial ID: {}", id);
        }


        Testimonial updatedTestimonial = testimonialRepository.save(existingTestimonial);
        logger.info("Testimonial with ID: {} updated successfully.", id);
        return testimonialMapper.toDto(updatedTestimonial);
    }


    @Transactional
    public void deleteTestimonial(Long id) {
        logger.info("Attempting to delete testimonial with ID: {}", id);
        Testimonial testimonial = testimonialRepository.findWithDetailsById(id)
                .orElseThrow(() -> {
                    logger.warn("Testimonial not found with ID: {}", id);
                    return new ResourceNotFoundException("Testimonial not found with ID: " + id, "Testimonial");
                });
        testimonialRepository.delete(testimonial);
        logger.info("Testimonial with ID: {} deleted successfully.", id);
    }

    @Transactional(readOnly = true)
    public Page<TestimonialDto> getAllTestimonials(int page, int size, Long userId, Long vehicleId, Boolean isApproved, String sortBy, String sortDirection) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection.toUpperCase());
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Testimonial> testimonialsPage;

        if (userId != null && vehicleId != null && isApproved != null) {
            testimonialsPage = testimonialRepository.findByUserIdAndVehicleIdAndApproved(userId, vehicleId, isApproved, pageable);
        } else if (userId != null && isApproved != null) {
            testimonialsPage = testimonialRepository.findByUserIdAndApproved(userId, isApproved, pageable);
        } else if (vehicleId != null && isApproved != null) {
            testimonialsPage = testimonialRepository.findByVehicleIdAndApproved(vehicleId, isApproved, pageable);
        } else if (userId != null) {
            testimonialsPage = testimonialRepository.findByUserId(userId, pageable);
        } else if (vehicleId != null) {
            testimonialsPage = testimonialRepository.findByVehicleId(vehicleId, pageable);
        } else if (isApproved != null) {
            testimonialsPage = testimonialRepository.findByApproved(isApproved, pageable);
        } else {
            testimonialsPage = testimonialRepository.findAll(pageable);
        }

        logger.debug("Fetched {} testimonials for page {} with size {}. Total elements: {}",
                testimonialsPage.getNumberOfElements(), page, size, testimonialsPage.getTotalElements());
        return testimonialsPage.map(testimonialMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<TestimonialDto> getApprovedTestimonials(int page, int size, Long vehicleId, String sortBy, String sortDirection) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection.toUpperCase());
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Testimonial> testimonialsPage;

        if (vehicleId != null) {
            testimonialsPage = testimonialRepository.findByVehicleIdAndApproved(vehicleId, true, pageable);
        } else {
            testimonialsPage = testimonialRepository.findByApproved(true, pageable);
        }
        logger.debug("Fetched {} approved testimonials for page {} with size {}. Total elements: {}",
                testimonialsPage.getNumberOfElements(), page, size, testimonialsPage.getTotalElements());
        return testimonialsPage.map(testimonialMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<TestimonialDto> getMyTestimonials(int page, int size, String sortBy, String sortDirection) {
        User authenticatedUser = securityUtilsService.getCurrentAuthenticatedUser();
        Sort.Direction direction = Sort.Direction.fromString(sortDirection.toUpperCase());
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Testimonial> testimonialsPage = testimonialRepository.findByUserId(authenticatedUser.getId(), pageable);
        logger.debug("Fetched {} testimonials for authenticated user ID: {}", testimonialsPage.getNumberOfElements(), authenticatedUser.getId());
        return testimonialsPage.map(testimonialMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<TestimonialDto> getAllTestimonials(Long userId, Long vehicleId, Boolean approved, Pageable pageable) {
        Page<Testimonial> testimonialsPage;

        if (userId != null && vehicleId != null && approved != null) {
            testimonialsPage = testimonialRepository.findByUserIdAndVehicleIdAndApproved(userId, vehicleId, approved, pageable);
        } else if (userId != null && approved != null) {
            testimonialsPage = testimonialRepository.findByUserIdAndApproved(userId, approved, pageable);
        } else if (vehicleId != null && approved != null) {
            testimonialsPage = testimonialRepository.findByVehicleIdAndApproved(vehicleId, approved, pageable);
        } else if (userId != null) {
            testimonialsPage = testimonialRepository.findByUserId(userId, pageable);
        } else if (vehicleId != null) {
            testimonialsPage = testimonialRepository.findByVehicleId(vehicleId, pageable);
        } else if (approved != null) {
            testimonialsPage = testimonialRepository.findByApproved(approved, pageable);
        } else {
            testimonialsPage = testimonialRepository.findAll(pageable);
        }

        logger.debug("Fetched {} testimonials with filters. Total elements: {}",
                testimonialsPage.getNumberOfElements(), testimonialsPage.getTotalElements());
        return testimonialsPage.map(testimonialMapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<TestimonialDto> getPendingTestimonials() {
        List<Testimonial> pendingTestimonials = testimonialRepository.findByApprovedFalse();
        logger.debug("Fetched {} pending testimonials", pendingTestimonials.size());
        return pendingTestimonials.stream().map(testimonialMapper::toDto).toList();
    }

    @Transactional
    public TestimonialDto rejectTestimonial(Long id, String reason) {
        logger.info("Rejecting testimonial with ID: {} with reason: {}", id, reason);
        Testimonial testimonial = testimonialRepository.findWithDetailsById(id)
                .orElseThrow(() -> {
                    logger.warn("Testimonial not found with ID: {}", id);
                    return new ResourceNotFoundException("Testimonial not found with ID: " + id, "Testimonial");
                });

        testimonial.setApproved(false);
        
        Testimonial updatedTestimonial = testimonialRepository.save(testimonial);
        logger.info("Testimonial with ID: {} rejected successfully.", id);
        
        // Record testimonial rejection activity
        try {
            User currentUser = securityUtilsService.getCurrentAuthenticatedUser();
            activityEventListener.recordTestimonialAction("rejected", updatedTestimonial.getId(), 
                updatedTestimonial.getTitle(), currentUser);
        } catch (Exception e) {
            logger.warn("Could not record testimonial rejection activity: {}", e.getMessage());
        }
        
        return testimonialMapper.toDto(updatedTestimonial);
    }

    @Transactional
    public TestimonialDto addAdminReply(Long id, String adminReplyContent) {
        logger.info("Adding admin reply to testimonial with ID: {}", id);
        Testimonial testimonial = testimonialRepository.findWithDetailsById(id)
                .orElseThrow(() -> {
                    logger.warn("Testimonial not found with ID: {}", id);
                    return new ResourceNotFoundException("Testimonial not found with ID: " + id, "Testimonial");
                });

        testimonial.setAdminReplyContent(adminReplyContent);
        
        Testimonial updatedTestimonial = testimonialRepository.save(testimonial);
        logger.info("Admin reply added to testimonial with ID: {} successfully.", id);
        return testimonialMapper.toDto(updatedTestimonial);
    }

    @Transactional(readOnly = true)
    public TestimonialStatsDto getTestimonialStats() {
        long totalTestimonials = testimonialRepository.count();
        long approvedTestimonials = testimonialRepository.countByApprovedTrue();
        long pendingTestimonials = testimonialRepository.countByApprovedFalse();
        double averageRating = testimonialRepository.findAverageRatingByApprovedTrue();
        
        return TestimonialStatsDto.builder()
                .totalTestimonials(totalTestimonials)
                .approvedTestimonials(approvedTestimonials)
                .pendingTestimonials(pendingTestimonials)
                .averageRating(averageRating)
                .build();
    }
}
