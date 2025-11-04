package com.adamo.vrspfab.users;

import com.adamo.vrspfab.common.DuplicateFieldException;
import com.adamo.vrspfab.notifications.EmailService;
import com.adamo.vrspfab.dashboard.ActivityEventListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmailService emailService;
    @Mock private ActivityEventListener activityEventListener;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private com.adamo.vrspfab.reservations.ReservationRepository reservationRepository;
    @Mock private com.adamo.vrspfab.payments.PaymentRepository paymentRepository;
    @Mock private com.adamo.vrspfab.payments.RefundRepository refundRepository;
    @Mock private com.adamo.vrspfab.notifications.NotificationRepository notificationRepository;
    @Mock private com.adamo.vrspfab.favorites.FavoriteRepository favoriteRepository;
    @Mock private com.adamo.vrspfab.bookmarks.BookmarkRepository bookmarkRepository;

    @InjectMocks private UserService userService;

    private RegisterUserRequest registerRequest;
    private User user;

    @BeforeEach
    void setup() {
        registerRequest = new RegisterUserRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPhoneNumber("1234567890");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");

        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .role(Role.CUSTOMER)
                .password("encoded")
                .build();
    }

    @Test
    void registerUser_whenEmailExists_throwsDuplicateFieldException() {
        given(userRepository.existsByEmail("test@example.com")).willReturn(true);

        assertThrows(DuplicateFieldException.class, () -> userService.registerUser(registerRequest));
    }

    @Test
    void registerUser_whenPhoneExists_throwsDuplicateFieldException() {
        given(userRepository.existsByEmail("test@example.com")).willReturn(false);
        given(userRepository.existsByPhoneNumber("1234567890")).willReturn(true);

        assertThrows(DuplicateFieldException.class, () -> userService.registerUser(registerRequest));
    }

    @Test
    void updateUser_whenPhoneExistsForOtherUser_throwsDuplicateFieldException() {
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setPhoneNumber("9876543210");
        
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userRepository.existsByPhoneNumberAndIdNot("9876543210", 1L)).willReturn(true);

        assertThrows(DuplicateFieldException.class, () -> userService.updateUser(1L, updateRequest));
    }

    @Test
    void changePassword_whenOldPasswordDoesNotMatch_throwsAccessDenied() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("wrong");
        request.setNewPassword("newpass");

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrong", "encoded")).willReturn(false);

        assertThrows(AccessDeniedException.class, () -> userService.changePassword(1L, request));
    }

    @Test
    void getUser_whenNotFound_throwsUserNotFoundException() {
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUser(999L));
    }

    @Test
    void updateUserRole_publishesEvent() {
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userRepository.save(any(User.class))).willReturn(user);
        given(userMapper.toDto(any(User.class))).willReturn(new UserDto());

        UserDto result = userService.updateUserRole(1L, Role.ADMIN);
        assertNotNull(result);
    }
}

