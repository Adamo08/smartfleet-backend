package com.adamo.vrspfab.mappers;

import com.adamo.vrspfab.dtos.RegisterUserRequest;
import com.adamo.vrspfab.dtos.UpdateUserRequest;
import com.adamo.vrspfab.dtos.UserDto;
import com.adamo.vrspfab.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);

    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "phoneNumber", source = "phoneNumber")
    User toEntity(RegisterUserRequest request);
    void update(UpdateUserRequest request, @MappingTarget User user);
}
