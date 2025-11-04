package com.adamo.vrspfab.favorites;

import com.adamo.vrspfab.common.ResourceNotFoundException;
import com.adamo.vrspfab.common.SecurityUtilsService;
import com.adamo.vrspfab.notifications.NotificationService;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @Mock private FavoriteRepository favoriteRepository;
    @Mock private FavoriteMapper favoriteMapper;
    @Mock private UserService userService;
    @Mock private VehicleService vehicleService;
    @Mock private VehicleMapper vehicleMapper;
    @Mock private SecurityUtilsService securityUtilsService;
    @Mock private NotificationService notificationService;

    @InjectMocks private FavoriteService favoriteService;

    private User current;
    private FavoriteDto dto;

    @BeforeEach
    void setup() {
        current = User.builder().id(1L).role(Role.CUSTOMER).build();
        dto = new FavoriteDto();
        dto.setUserId(1L);
        dto.setVehicleId(7L);
        given(securityUtilsService.getCurrentAuthenticatedUser()).willReturn(current);
    }

    @Test
    void createFavorite_whenUserMismatch_throwsAccessDenied() {
        dto.setUserId(2L);
        assertThrows(AccessDeniedException.class, () -> favoriteService.createFavorite(dto));
    }

    @Test
    void createFavorite_whenDuplicateExists_throwsDuplicate() {
        given(userService.getUserById(1L)).willReturn(Optional.of(current));
        com.adamo.vrspfab.vehicles.VehicleDto vehicleDto = new com.adamo.vrspfab.vehicles.VehicleDto();
        vehicleDto.setId(7L);
        given(vehicleService.getVehicleById(7L)).willReturn(vehicleDto);
        given(favoriteRepository.findByUserIdAndVehicleId(1L, 7L)).willReturn(Optional.of(new Favorite()));

        assertThrows(DuplicateFavoriteException.class, () -> favoriteService.createFavorite(dto));
    }

    @Test
    void getFavoriteById_whenNotOwnerAndNotAdmin_throwsAccessDenied() {
        User owner = User.builder().id(2L).role(Role.CUSTOMER).build();
        Favorite favorite = new Favorite();
        favorite.setUser(owner);
        given(favoriteRepository.findWithDetailsById(5L)).willReturn(Optional.of(favorite));

        assertThrows(AccessDeniedException.class, () -> favoriteService.getFavoriteById(5L));
    }
}
