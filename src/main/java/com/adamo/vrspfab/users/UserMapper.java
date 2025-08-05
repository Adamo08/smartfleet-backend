package com.adamo.vrspfab.users;

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


    User toEntity(UserDto userDto);

    void update(UpdateUserRequest request, @MappingTarget User user);
}
