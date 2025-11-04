package com.adamo.vrspfab.testimonials;

import com.adamo.vrspfab.common.ResourceNotFoundException;
import com.adamo.vrspfab.common.SecurityUtilsService;
import com.adamo.vrspfab.dashboard.ActivityEventListener;
import com.adamo.vrspfab.users.Role;
import com.adamo.vrspfab.users.User;
import com.adamo.vrspfab.users.UserService;
import com.adamo.vrspfab.vehicles.VehicleService;
import com.adamo.vrspfab.vehicles.mappers.VehicleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class TestimonialServiceTest {

    @Mock private TestimonialRepository testimonialRepository;
    @Mock private TestimonialMapper testimonialMapper;
    @Mock private UserService userService;
    @Mock private VehicleService vehicleService;
    @Mock private VehicleMapper vehicleMapper;
    @Mock private SecurityUtilsService securityUtilsService;
    @Mock private ActivityEventListener activityEventListener;

    @InjectMocks private TestimonialService testimonialService;

    private User currentUser;
    private TestimonialDto testimonialDto;

    @BeforeEach
    void setup() {
        currentUser = User.builder().id(1L).role(Role.CUSTOMER).build();
        testimonialDto = new TestimonialDto();
        testimonialDto.setUserId(1L);
        testimonialDto.setVehicleId(10L);
        testimonialDto.setContent("Great vehicle!");
        testimonialDto.setRating(5);
        given(securityUtilsService.getCurrentAuthenticatedUser()).willReturn(currentUser);
    }

    @Test
    void createTestimonial_whenUserMismatch_throwsAccessDenied() {
        testimonialDto.setUserId(2L);

        assertThrows(AccessDeniedException.class, () -> testimonialService.createTestimonial(testimonialDto));
    }

    @Test
    void createTestimonial_whenDuplicateExists_throwsDuplicate() {
        given(userService.getUserById(1L)).willReturn(Optional.of(currentUser));
        com.adamo.vrspfab.vehicles.VehicleDto vehicleDto = new com.adamo.vrspfab.vehicles.VehicleDto();
        vehicleDto.setId(10L);
        given(vehicleService.getVehicleById(10L)).willReturn(vehicleDto);
        given(vehicleMapper.toEntity(any())).willReturn(com.adamo.vrspfab.vehicles.Vehicle.builder().id(10L).build());
        given(testimonialRepository.findByUserIdAndVehicleId(1L, 10L)).willReturn(Optional.of(new Testimonial()));

        assertThrows(DuplicateTestimonialException.class, () -> testimonialService.createTestimonial(testimonialDto));
    }

    @Test
    void getTestimonialById_whenNotApprovedAndNotOwnerAndNotAdmin_throwsAccessDenied() {
        User owner = User.builder().id(2L).role(Role.CUSTOMER).build();
        Testimonial testimonial = new Testimonial();
        testimonial.setUser(owner);
        testimonial.setApproved(false);
        given(testimonialRepository.findWithDetailsById(5L)).willReturn(Optional.of(testimonial));

        assertThrows(AccessDeniedException.class, () -> testimonialService.getTestimonialById(5L));
    }

    @Test
    void approveTestimonial_whenAlreadyApproved_throwsIllegalState() {
        Testimonial testimonial = new Testimonial();
        testimonial.setApproved(true);
        given(testimonialRepository.findWithDetailsById(1L)).willReturn(Optional.of(testimonial));

        assertThrows(IllegalStateException.class, () -> testimonialService.approveTestimonial(1L));
    }

    @Test
    void getTestimonialById_whenNotFound_throwsResourceNotFound() {
        given(testimonialRepository.findWithDetailsById(999L)).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> testimonialService.getTestimonialById(999L));
    }
}

