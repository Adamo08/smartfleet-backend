package com.adamo.vrspfab.bookmarks;

import com.adamo.vrspfab.common.ResourceNotFoundException;
import com.adamo.vrspfab.common.SecurityUtilsService;
import com.adamo.vrspfab.reservations.ReservationMapper;
import com.adamo.vrspfab.users.User;
import com.adamo.vrspfab.users.UserService;
import com.adamo.vrspfab.reservations.ReservationService;
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


@AllArgsConstructor
@Service
public class BookmarkService {

    private static final Logger logger = LoggerFactory.getLogger(BookmarkService.class);

    private final BookmarkRepository bookmarkRepository;
    private final BookmarkMapper bookmarkMapper;
    private final UserService userService;
    private final ReservationService reservationService;
    private final SecurityUtilsService securityUtilsService;
    private final ReservationMapper reservationMapper;

    @Transactional
    public BookmarkDto createBookmark(BookmarkDto bookmarkDto) {
        logger.info("Attempting to create bookmark: {}", bookmarkDto);

        User authenticatedUser = securityUtilsService.getCurrentAuthenticatedUser();
        // Ensure the user is creating a bookmark for themselves
        if (!authenticatedUser.getId().equals(bookmarkDto.getUserId())) {
            logger.warn("User {} attempted to create bookmark for user ID {}. Access denied.", authenticatedUser.getId(), bookmarkDto.getUserId());
            throw new AccessDeniedException("User can only add bookmarks for themselves.");
        }

        User user = userService.getUserById(bookmarkDto.getUserId())
                .orElseThrow(() -> {
                    logger.warn("User not found with ID: {}", bookmarkDto.getUserId());
                    return new ResourceNotFoundException("User not found with ID: " + bookmarkDto.getUserId(), "User");
                });

        // Fetch the Reservation
        var reservationDto = reservationService.getReservationById(bookmarkDto.getReservationId());
        var reservation = reservationMapper.toEntity(reservationDto);

        if (reservation == null) {
            logger.warn("Reservation not found with ID: {}", bookmarkDto.getReservationId());
            throw new ResourceNotFoundException("Reservation not found with ID: " + bookmarkDto.getReservationId(), "Reservation");
        }

        // Ensure the reservation belongs to the authenticated user
        if (!reservation.getUser().getId().equals(authenticatedUser.getId())) {
            logger.warn("Authenticated user {} attempted to bookmark reservation {} which does not belong to them.", authenticatedUser.getId(), reservation.getId());
            throw new AccessDeniedException("Cannot bookmark a reservation that does not belong to you.");
        }


        // Check for existing bookmark by the same user for the same reservation
        Optional<Bookmark> existingBookmark = bookmarkRepository.findByUserIdAndReservationId(user.getId(), reservation.getId()); // Changed to ReservationId
        if (existingBookmark.isPresent()) {
            logger.warn("User {} has already bookmarked reservation {}.", user.getId(), reservation.getId());
            throw new DuplicateBookmarkException("You have already bookmarked this reservation.");
        }

        Bookmark bookmark = bookmarkMapper.toEntity(bookmarkDto);
        bookmark.setUser(user);
        bookmark.setReservation(reservation);

        Bookmark savedBookmark = bookmarkRepository.save(bookmark);
        logger.info("Bookmark created successfully with ID: {}", savedBookmark.getId());
        return bookmarkMapper.toDto(savedBookmark);
    }



    @Transactional(readOnly = true)
    public BookmarkDto getBookmarkById(Long id) {
        logger.debug("Fetching bookmark with ID: {}", id);
        Bookmark bookmark = bookmarkRepository.findWithDetailsById(id)
                .orElseThrow(() -> {
                    logger.warn("Bookmark not found with ID: {}", id);
                    return new ResourceNotFoundException("Bookmark not found with ID: " + id, "Bookmark");
                });

        User authenticatedUser = securityUtilsService.getCurrentAuthenticatedUser();
        boolean isAdmin = authenticatedUser.getRole().equals(Role.ADMIN);

        // Allow access if:
        // 1. User is the owner of the bookmark
        // 2. User is an ADMIN
        if (!bookmark.getUser().getId().equals(authenticatedUser.getId()) && !isAdmin) {
            logger.warn("Access denied for user {} to bookmark {} (not owner, not admin).", authenticatedUser.getId(), id);
            throw new AccessDeniedException("Access denied to this bookmark.");
        }

        return bookmarkMapper.toDto(bookmark);
    }

    @Transactional
    public void deleteBookmark(Long id) {
        logger.info("Attempting to delete bookmark with ID: {}", id);
        Bookmark bookmark = bookmarkRepository.findWithDetailsById(id)
                .orElseThrow(() -> {
                    logger.warn("Bookmark not found with ID: {}", id);
                    return new ResourceNotFoundException("Bookmark not found with ID: " + id, "Bookmark");
                });

        User authenticatedUser = securityUtilsService.getCurrentAuthenticatedUser();
        boolean isAdmin = authenticatedUser.getRole().equals(Role.ADMIN);

        // Allow deletion if:
        // 1. User is the owner of the bookmark
        // 2. User is an ADMIN
        if (!bookmark.getUser().getId().equals(authenticatedUser.getId()) && !isAdmin) {
            logger.warn("Access denied for user {} to delete bookmark {} (not owner, not admin).", authenticatedUser.getId(), id);
            throw new AccessDeniedException("Access denied to delete this bookmark.");
        }

        bookmarkRepository.delete(bookmark);
        logger.info("Bookmark with ID: {} deleted successfully.", id);
    }

    @Transactional(readOnly = true)
    public Page<BookmarkDto> getAllBookmarks(int page, int size, Long userId, String sortBy, String sortDirection) {
        // This method is primarily for ADMINs to view all bookmarks with filters
        Sort.Direction direction = Sort.Direction.fromString(sortDirection.toUpperCase());
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Bookmark> bookmarksPage;

        if (userId != null) {
            bookmarksPage = bookmarkRepository.findByUserId(userId, pageable);
        } else {
            bookmarksPage = bookmarkRepository.findAll(pageable);
        }

        logger.debug("Fetched {} bookmarks for page {} with size {}. Total elements: {}",
                bookmarksPage.getNumberOfElements(), page, size, bookmarksPage.getTotalElements());
        return bookmarksPage.map(bookmarkMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<BookmarkDto> getMyBookmarks(int page, int size, String sortBy, String sortDirection) {
        User authenticatedUser = securityUtilsService.getCurrentAuthenticatedUser();
        Sort.Direction direction = Sort.Direction.fromString(sortDirection.toUpperCase());
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Bookmark> bookmarksPage = bookmarkRepository.findByUserId(authenticatedUser.getId(), pageable);
        logger.debug("Fetched {} bookmarks for authenticated user ID: {}", bookmarksPage.getNumberOfElements(), authenticatedUser.getId());
        return bookmarksPage.map(bookmarkMapper::toDto);
    }
}
