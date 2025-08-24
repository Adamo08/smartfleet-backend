package com.adamo.vrspfab.testimonials;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TestimonialMapper {
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "vehicleId", source = "vehicle.id")
    @Mapping(target = "userName", expression = "java(testimonial.getUser().getFirstName() + \" \" + testimonial.getUser().getLastName())") // Concatenate first and last name
    @Mapping(target = "userEmail", source = "user.email")
    @Mapping(target = "vehicleBrand", source = "vehicle.brand.name")
    @Mapping(target = "vehicleModel", source = "vehicle.model.name")
    TestimonialDto toDto(Testimonial testimonial);

    @Mapping(target = "user.id", source = "userId")
    @Mapping(target = "vehicle.id", source = "vehicleId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "approved", ignore = true)
    @Mapping(target = "adminReplyContent", ignore = true)
    Testimonial toEntity(TestimonialDto testimonialDto);

    void updateTestimonialFromDto(TestimonialDto testimonialDto, @MappingTarget Testimonial testimonial);
}
