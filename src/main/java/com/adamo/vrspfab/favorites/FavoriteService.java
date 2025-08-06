package com.adamo.vrspfab.favorites;

import com.adamo.vrspfab.common.ResourceNotFoundException;
import com.adamo.vrspfab.common.SecurityUtilsService;
import com.adamo.vrspfab.users.User;
import com.adamo.vrspfab.users.UserService;
import com.adamo.vrspfab.vehicles.Vehicle;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import com.adamo.vrspfab.users.Role;

import java.util.Optional;


@AllArgsConstructor
@Service
public class FavoriteService {

    private static final Logger logger = LoggerFactory.getLogger(FavoriteService.class);

    private final FavoriteRepository favoriteRepository;
    private final FavoriteMapper favoriteMapper;
    private final UserService userService;
    private final VehicleService vehicleService;
    private final SecurityUtilsService securityUtilsService;


    @Transactional
    public FavoriteDto createFavorite(FavoriteDto favoriteDto) {
        logger.info("Attempting to create favorite: {}", favoriteDto);

        User authenticatedUser = securityUtilsService.getCurrentAuthenticatedUser();
        // Ensure the user is creating a favorite for themselves
        if (!authenticatedUser.getId().equals(favoriteDto.getUserId())) {
            logger.warn("User {} attempted to create favorite for user ID {}. Access denied.", authenticatedUser.getId(), favoriteDto.getUserId());
            throw new AccessDeniedException("User can only add favorites for themselves.");
        }

        User user = userService.getUserById(favoriteDto.getUserId())
                .orElseThrow(() -> {
                    logger.warn("User not found with ID: {}", favoriteDto.getUserId());
                    return new ResourceNotFoundException("User not found with ID: " + favoriteDto.getUserId(), "User");
                });

        Vehicle vehicle = vehicleService.getVehicleById(favoriteDto.getVehicleId());
        if (vehicle == null) {
            logger.warn("Vehicle not found with ID: {}", favoriteDto.getVehicleId());
            throw new ResourceNotFoundException("Vehicle not found with ID: " + favoriteDto.getVehicleId(), "Vehicle");
        }

        // Check for existing favorite by the same user for the same vehicle
        Optional<Favorite> existingFavorite = favoriteRepository.findByUserIdAndVehicleId(user.getId(), vehicle.getId());
        if (existingFavorite.isPresent()) {
            logger.warn("User {} has already favorited vehicle {}.", user.getId(), vehicle.getId());
            throw new DuplicateFavoriteException("You have already favorited this vehicle.");
        }

        Favorite favorite = favoriteMapper.toEntity(favoriteDto);
        favorite.setUser(user);
        favorite.setVehicle(vehicle);

        Favorite savedFavorite = favoriteRepository.save(favorite);
        logger.info("Favorite created successfully with ID: {}", savedFavorite.getId());
        return favoriteMapper.toDto(savedFavorite);
    }

    @Transactional(readOnly = true)
    public FavoriteDto getFavoriteById(Long id) {
        logger.debug("Fetching favorite with ID: {}", id);
        Favorite favorite = favoriteRepository.findWithDetailsById(id)
                .orElseThrow(() -> {
                    logger.warn("Favorite not found with ID: {}", id);
                    return new ResourceNotFoundException("Favorite not found with ID: " + id, "Favorite");
                });

        User authenticatedUser = securityUtilsService.getCurrentAuthenticatedUser();
        boolean isAdmin = authenticatedUser.getRole().equals(Role.ADMIN);

        // Allow access if:
        // 1. User is the owner of the favorite
        // 2. User is an ADMIN
        if (!favorite.getUser().getId().equals(authenticatedUser.getId()) && !isAdmin) {
            logger.warn("Access denied for user {} to favorite {} (not owner, not admin).", authenticatedUser.getId(), id);
            throw new AccessDeniedException("Access denied to this favorite.");
        }

        return favoriteMapper.toDto(favorite);
    }

    @Transactional
    public void deleteFavorite(Long id) {
        logger.info("Attempting to delete favorite with ID: {}", id);
        Favorite favorite = favoriteRepository.findWithDetailsById(id)
                .orElseThrow(() -> {
                    logger.warn("Favorite not found with ID: {}", id);
                    return new ResourceNotFoundException("Favorite not found with ID: " + id, "Favorite");
                });

        User authenticatedUser = securityUtilsService.getCurrentAuthenticatedUser();
        boolean isAdmin = authenticatedUser.getRole().equals(Role.ADMIN);

        // Allow deletion if:
        // 1. User is the owner of the favorite
        // 2. User is an ADMIN
        if (!favorite.getUser().getId().equals(authenticatedUser.getId()) && !isAdmin) {
            logger.warn("Access denied for user {} to delete favorite {} (not owner, not admin).", authenticatedUser.getId(), id);
            throw new AccessDeniedException("Access denied to delete this favorite.");
        }

        favoriteRepository.delete(favorite);
        logger.info("Favorite with ID: {} deleted successfully.", id);
    }

    @Transactional(readOnly = true)
    public Page<FavoriteDto> getAllFavorites(int page, int size, Long userId, String sortBy, String sortDirection) {
        // This method is primarily for ADMINs to view all favorites with filters
        Sort.Direction direction = Sort.Direction.fromString(sortDirection.toUpperCase());
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Favorite> favoritesPage;

        if (userId != null) {
            favoritesPage = favoriteRepository.findByUserId(userId, pageable);
        } else {
            favoritesPage = favoriteRepository.findAll(pageable);
        }

        logger.debug("Fetched {} favorites for page {} with size {}. Total elements: {}",
                favoritesPage.getNumberOfElements(), page, size, favoritesPage.getTotalElements());
        return favoritesPage.map(favoriteMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<FavoriteDto> getMyFavorites(int page, int size, String sortBy, String sortDirection) {
        User authenticatedUser = securityUtilsService.getCurrentAuthenticatedUser();
        Sort.Direction direction = Sort.Direction.fromString(sortDirection.toUpperCase());
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Favorite> favoritesPage = favoriteRepository.findByUserId(authenticatedUser.getId(), pageable);
        logger.debug("Fetched {} favorites for authenticated user ID: {}", favoritesPage.getNumberOfElements(), authenticatedUser.getId());
        return favoritesPage.map(favoriteMapper::toDto);
    }
}
