package com.adamo.vrspfab.bookmarks;

import com.adamo.vrspfab.common.ResourceNotFoundException;
import com.adamo.vrspfab.common.SecurityUtilsService;
import com.adamo.vrspfab.notifications.NotificationService;
import com.adamo.vrspfab.reservations.Reservation;
import com.adamo.vrspfab.reservations.ReservationMapper;
import com.adamo.vrspfab.reservations.ReservationService;
import com.adamo.vrspfab.users.Role;
import com.adamo.vrspfab.users.User;
import com.adamo.vrspfab.users.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class BookmarkServiceTest {

    @Mock private BookmarkRepository bookmarkRepository;
    @Mock private BookmarkMapper bookmarkMapper;
    @Mock private UserService userService;
    @Mock private ReservationService reservationService;
    @Mock private SecurityUtilsService securityUtilsService;
    @Mock private ReservationMapper reservationMapper;
    @Mock private NotificationService notificationService;

    @InjectMocks private BookmarkService bookmarkService;

    private User authenticatedUser;
    private BookmarkDto inputDto;

    @BeforeEach
    void setup() {
        authenticatedUser = User.builder().id(1L).role(Role.CUSTOMER).build();
        inputDto = new BookmarkDto();
        inputDto.setUserId(1L);
        inputDto.setReservationId(10L);

        given(securityUtilsService.getCurrentAuthenticatedUser()).willReturn(authenticatedUser);
    }

    @Test
    void createBookmark_whenUserIdMismatch_throwsAccessDenied() {
        inputDto.setUserId(2L); // different from authenticated user id

        assertThrows(AccessDeniedException.class, () -> bookmarkService.createBookmark(inputDto));
        Mockito.verifyNoInteractions(userService, reservationService, bookmarkRepository);
    }

    @Test
    void createBookmark_whenReservationBelongsToAnotherUser_throwsAccessDenied() {
        // user lookup ok
        given(userService.getUserById(1L)).willReturn(Optional.of(authenticatedUser));
        // reservation maps to another user's reservation
        Reservation res = Reservation.builder().id(10L).user(User.builder().id(99L).role(Role.CUSTOMER).build()).build();
        com.adamo.vrspfab.reservations.ReservationDto resDto = new com.adamo.vrspfab.reservations.ReservationDto();
        resDto.setId(10L);
        resDto.setUserId(99L);
        given(reservationService.getReservationById(10L)).willReturn(resDto);
        given(reservationMapper.toEntity(any())).willReturn(res);

        assertThrows(AccessDeniedException.class, () -> bookmarkService.createBookmark(inputDto));
    }

    @Test
    void createBookmark_whenDuplicateExists_throwsDuplicate() {
        given(userService.getUserById(1L)).willReturn(Optional.of(authenticatedUser));
        Reservation res = Reservation.builder().id(10L).user(authenticatedUser).build();
        com.adamo.vrspfab.reservations.ReservationDto resDto = new com.adamo.vrspfab.reservations.ReservationDto();
        resDto.setId(10L);
        resDto.setUserId(1L);
        given(reservationService.getReservationById(10L)).willReturn(resDto);
        given(reservationMapper.toEntity(any())).willReturn(res);
        given(bookmarkRepository.findByUserIdAndReservationId(1L, 10L)).willReturn(Optional.of(new Bookmark()));

        assertThrows(DuplicateBookmarkException.class, () -> bookmarkService.createBookmark(inputDto));
    }

    @Test
    void getBookmarkById_whenNotOwnerAndNotAdmin_throwsAccessDenied() {
        User other = User.builder().id(2L).role(Role.CUSTOMER).build();
        Bookmark bookmark = new Bookmark();
        bookmark.setUser(other);
        given(bookmarkRepository.findWithDetailsById(5L)).willReturn(Optional.of(bookmark));
        given(securityUtilsService.getCurrentAuthenticatedUser()).willReturn(authenticatedUser);

        assertThrows(AccessDeniedException.class, () -> bookmarkService.getBookmarkById(5L));
    }
}


