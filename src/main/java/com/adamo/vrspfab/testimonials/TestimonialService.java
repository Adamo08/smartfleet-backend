package com.adamo.vrspfab.testimonials;

import com.adamo.vrspfab.common.ResourceNotFoundException;
import com.adamo.vrspfab.users.User;
import com.adamo.vrspfab.users.UserService;
import com.adamo.vrspfab.vehicles.Vehicle;
import com.adamo.vrspfab.vehicles.VehicleService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@AllArgsConstructor
@Service
public class TestimonialService {

    private final TestimonialRepository testimonialRepository;
    private final TestimonialMapper testimonialMapper;
    private final UserService userService;
    private final VehicleService vehicleService;

    @Transactional
    public TestimonialDto createTestimonial(TestimonialDto testimonialDto) {
        User user = userService.getUserById(testimonialDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Vehicle vehicle = vehicleService.getVehicleById(testimonialDto.getVehicleId());

        Testimonial testimonial = testimonialMapper.toEntity(testimonialDto);
        testimonial.setUser(user);
        testimonial.setVehicle(vehicle);
        testimonial.setApproved(false); // Default to unapproved
        return testimonialMapper.toDto(testimonialRepository.save(testimonial));
    }

    @Transactional(readOnly = true)
    public TestimonialDto getTestimonialById(Long id) {
        Testimonial testimonial = testimonialRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Testimonial not found"));
        return testimonialMapper.toDto(testimonial);
    }

    @Transactional
    public TestimonialDto approveTestimonial(Long id) {
        Testimonial testimonial = testimonialRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Testimonial not found"));
        if (testimonial.isApproved()) {
            throw new IllegalStateException("Testimonial is already approved");
        }
        testimonial.setApproved(true);
        return testimonialMapper.toDto(testimonialRepository.save(testimonial));
    }

    @Transactional
    public void deleteTestimonial(Long id) {
        Testimonial testimonial = testimonialRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Testimonial not found"));
        testimonialRepository.delete(testimonial);
    }

    @Transactional(readOnly = true)
    public List<TestimonialDto> getAllTestimonials(int page, int size, Long userId, Boolean isApproved) {
        List<Testimonial> testimonials = testimonialRepository.findAll();
        if (userId != null) {
            testimonials = testimonialRepository.findApprovedByUserId(userId);
        }
        if (isApproved != null) {
            testimonials = testimonials.stream()
                    .filter(t -> t.isApproved() == isApproved)
                    .collect(Collectors.toList());
        }
        return testimonials.stream()
                .skip((long) page * size)
                .limit(size)
                .map(testimonialMapper::toDto)
                .collect(Collectors.toList());
    }
}