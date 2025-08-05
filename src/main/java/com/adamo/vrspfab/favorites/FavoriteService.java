package com.adamo.vrspfab.favorites;

import com.adamo.vrspfab.common.ResourceNotFoundException;
import com.adamo.vrspfab.users.User;
import com.adamo.vrspfab.users.UserService;
import com.adamo.vrspfab.vehicles.Vehicle;
import com.adamo.vrspfab.vehicles.VehicleService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@AllArgsConstructor
@Service
public class FavoriteService {


    private final FavoriteRepository favoriteRepository;
    private final FavoriteMapper favoriteMapper;
    private final UserService userService;
    private final VehicleService vehicleService;

    @Transactional
    public FavoriteDto createFavorite(FavoriteDto favoriteDto) {
        User user = userService.getUserById(favoriteDto.getUserId()).orElse(null);

        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }

        Vehicle vehicle = vehicleService.getVehicleById(favoriteDto.getVehicleId());

        Favorite favorite = favoriteMapper.toEntity(favoriteDto);
        favorite.setUser(user);
        favorite.setVehicle(vehicle);
        return favoriteMapper.toDto(favoriteRepository.save(favorite));
    }

    @Transactional(readOnly = true)
    public FavoriteDto getFavoriteById(Long id) {
        Favorite favorite = favoriteRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Favorite not found"));
        return favoriteMapper.toDto(favorite);
    }

    @Transactional
    public void deleteFavorite(Long id) {
        Favorite favorite = favoriteRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Favorite not found"));
        favoriteRepository.delete(favorite);
    }

    @Transactional(readOnly = true)
    public List<FavoriteDto> getAllFavorites(int page, int size, Long userId) {
        List<Favorite> favorites = favoriteRepository.findAllWithUserAndVehicle();
        if (userId != null) {
            favorites = favoriteRepository.findByUserId(userId);
        }
        return favorites.stream()
                .skip((long) page * size)
                .limit(size)
                .map(favoriteMapper::toDto)
                .collect(Collectors.toList());
    }
}