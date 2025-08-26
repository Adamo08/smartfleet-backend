package com.adamo.vrspfab.favorites;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants; // Import for MappingConstants.ComponentModel.SPRING

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING) // Use MappingConstants for componentModel
public interface FavoriteMapper {
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "vehicleId", source = "vehicle.id")
    @Mapping(target = "userName", expression = "java(favorite.getUser().getFirstName() + \" \" + favorite.getUser().getLastName())") // Concatenate first and last name
    @Mapping(target = "userEmail", source = "user.email")
    @Mapping(target = "vehicleBrand", source = "vehicle.brand.name")
    @Mapping(target = "vehicleModel", source = "vehicle.model.name")
    FavoriteDto toDto(Favorite favorite);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "vehicle", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Favorite toEntity(FavoriteDto favoriteDTO);
}
