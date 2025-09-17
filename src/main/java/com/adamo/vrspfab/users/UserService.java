package com.adamo.vrspfab.users;


import com.adamo.vrspfab.common.DuplicateFieldException;
import com.adamo.vrspfab.common.SecurityUtilsService;
import com.adamo.vrspfab.notifications.EmailService;
import org.springframework.context.ApplicationEventPublisher;
import com.adamo.vrspfab.dashboard.ActivityEventListener;
import com.adamo.vrspfab.users.events.UserRegisteredEvent;
import com.adamo.vrspfab.users.events.UserRoleChangedEvent;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.adamo.vrspfab.reservations.ReservationRepository;
import com.adamo.vrspfab.reservations.ReservationStatus;
import com.adamo.vrspfab.payments.PaymentRepository;
import com.adamo.vrspfab.payments.PaymentStatus;
import com.adamo.vrspfab.payments.Payment;
import com.adamo.vrspfab.payments.RefundRepository;
import com.adamo.vrspfab.notifications.NotificationRepository;
import com.adamo.vrspfab.favorites.FavoriteRepository;
import com.adamo.vrspfab.bookmarks.BookmarkRepository;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final ActivityEventListener activityEventListener;
    private final ApplicationEventPublisher eventPublisher;
    private final ReservationRepository reservationRepository;
    private final ReservationStatus reservationStatus = com.adamo.vrspfab.reservations.ReservationStatus.PENDING;
    private final PaymentRepository paymentRepository;
    private final PaymentStatus paymentStatus = com.adamo.vrspfab.payments.PaymentStatus.COMPLETED;
    private final RefundRepository refundRepository;
    private final NotificationRepository notificationRepository;
    private final FavoriteRepository favoriteRepository;
    private final BookmarkRepository bookmarkRepository;

    public Page<UserDto> getAllUsers(PageRequest pageable, String searchTerm, String role) {
        UserSpecification spec = new UserSpecification(searchTerm, role);
        return userRepository.findAll(spec, pageable)
                .map(userMapper::toDto);
    }

    public UserDto getUser(Long userId) {
        var user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        return userMapper.toDto(user);
    }

    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    public UserDto registerUser(RegisterUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateFieldException("Email already exists");
        }

        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new DuplicateFieldException("Phone number already exists");
        }

        var user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.CUSTOMER); // Default role for new users
        user.setAuthProvider(AuthProvider.LOCAL); // Default auth provider
        User savedUser = userRepository.save(user);

        // Record user registration activity
        activityEventListener.recordUserRegistration(savedUser);

        // Publish user registered event
        eventPublisher.publishEvent(new UserRegisteredEvent(this, savedUser));

        return userMapper.toDto(savedUser);
    }

    public UserDto updateUser(Long userId, UpdateUserRequest request) {
        var user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);


        // Check if phone is being updated and if it already exists
        if (request.getPhoneNumber() != null &&
            userRepository.existsByPhoneNumberAndIdNot(request.getPhoneNumber(), userId)) {
            throw new DuplicateFieldException("Phone number already exists");
        }


        userMapper.update(request, user);
        userRepository.save(user);

        return userMapper.toDto(user);
    }

    public UserDto updateUserRole(Long userId, Role role) {
        var user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        String oldRole = user.getRole().getName();
        String newRole = role.getName();
        
        user.setRole(role);
        User savedUser = userRepository.save(user);
        
        // Publish user role changed event
        eventPublisher.publishEvent(new UserRoleChangedEvent(this, savedUser, Role.valueOf(oldRole.toUpperCase()), role));
        
        return userMapper.toDto(savedUser);
    }

    public void deleteUser(Long userId) {
        var user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        userRepository.delete(user);
    }

    public void changePassword(Long userId, ChangePasswordRequest request) {
        var user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new AccessDeniedException("Password does not match");
        }

        user.setPassword(request.getNewPassword());
        userRepository.save(user);
    }

    /**
     * Send password reset email to user
     * @param email user's email address
     */
    public void sendPasswordResetEmail(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);
        
        // Generate reset token
        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(java.time.LocalDateTime.now().plusHours(24));
        userRepository.save(user);
        
        // Send email with reset link
        String resetLink = "http://localhost:4200/auth/reset-password?token=" + resetToken;
        emailService.sendResetPasswordEmail(user.getEmail(), resetLink);
    }

    /**
     * Reset user password using reset token
     * @param token reset token
     * @param newPassword new password
     */
    public void resetPassword(String token, String newPassword) {
        var user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid reset token"));
        
        if (user.getResetTokenExpiry().isBefore(java.time.LocalDateTime.now())) {
            throw new IllegalArgumentException("Reset token has expired");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }

    public Long countAllUsers() {
        return userRepository.count();
    }

    public Long countUsersByRole(String role) {
        return userRepository.countByRole(Role.valueOf(role.toUpperCase()));
    }

    public UserStatsDto getCurrentUserStats() {
        User currentUser = userRepository.findByEmail(SecurityUtilsService.getCurrentAuthenticatedUserEmail())
                .orElseThrow(UserNotFoundException::new);

        Long userId = currentUser.getId();

        // Reservations - using efficient repository queries
        Long totalReservations = reservationRepository.countByUserId(userId);
        Long pendingReservations = reservationRepository.countByUserIdAndStatus(userId, ReservationStatus.PENDING);
        Long confirmedReservations = reservationRepository.countByUserIdAndStatus(userId, ReservationStatus.CONFIRMED);
        Long completedReservations = reservationRepository.countByUserIdAndStatus(userId, ReservationStatus.COMPLETED);
        Long cancelledReservations = reservationRepository.countByUserIdAndStatus(userId, ReservationStatus.CANCELLED);

        // Payments - using efficient repository queries
        Long totalPayments = paymentRepository.countByReservationUserIdAndStatus(userId, PaymentStatus.COMPLETED) +
                            paymentRepository.countByReservationUserIdAndStatus(userId, PaymentStatus.FAILED) +
                            paymentRepository.countByReservationUserIdAndStatus(userId, PaymentStatus.PENDING);
        Long completedPayments = paymentRepository.countByReservationUserIdAndStatus(userId, PaymentStatus.COMPLETED);
        Long failedPayments = paymentRepository.countByReservationUserIdAndStatus(userId, PaymentStatus.FAILED);
        BigDecimal totalSpent = paymentRepository.sumAmountByReservationUserIdAndStatus(userId, PaymentStatus.COMPLETED)
                .orElse(BigDecimal.ZERO);

        // Refunds - using efficient repository queries
        Long refundsCount = refundRepository.countByUserId(userId);
        BigDecimal totalRefunded = refundRepository.sumAmountByUserId(userId).orElse(BigDecimal.ZERO);

        // Engagement
        Long favoritesCount = (long) favoriteRepository.findByUserId(userId).size();
        Long bookmarksCount = bookmarkRepository.findByUserId(userId, org.springframework.data.domain.PageRequest.of(0,1)).getTotalElements();
        Long unreadNotifications = notificationRepository.countByUserAndReadFalse(currentUser);

        return UserStatsDto.builder()
                .userId(userId)
                .firstName(currentUser.getFirstName())
                .lastName(currentUser.getLastName())
                .email(currentUser.getEmail())
                .totalReservations(totalReservations)
                .pendingReservations(pendingReservations)
                .confirmedReservations(confirmedReservations)
                .completedReservations(completedReservations)
                .cancelledReservations(cancelledReservations)
                .totalPayments(totalPayments)
                .completedPayments(completedPayments)
                .failedPayments(failedPayments)
                .totalSpent(totalSpent)
                .refundsCount(refundsCount)
                .totalRefunded(totalRefunded)
                .favoritesCount(favoritesCount)
                .bookmarksCount(bookmarksCount)
                .unreadNotifications(unreadNotifications)
                .build();
    }

    public UserActivitySeriesDto getCurrentUserActivitySeries() {
        var user = SecurityUtilsService.getCurrentAuthenticatedUserEmail();
        Long userId = userRepository.findByEmail(user).orElseThrow(UserNotFoundException::new).getId();

        LocalDate now = LocalDate.now();
        List<String> months = new ArrayList<>();
        List<Long> reservationCounts = new ArrayList<>();
        List<BigDecimal> monthlySpending = new ArrayList<>();

        for (int i = 5; i >= 0; i--) {
            LocalDate monthDate = now.minusMonths(i);
            LocalDateTime start = monthDate.withDayOfMonth(1).atStartOfDay();
            LocalDateTime end = monthDate.withDayOfMonth(monthDate.lengthOfMonth()).atTime(23, 59, 59);

            // Reservation count for this user in month - using efficient repository query
            Long count = reservationRepository.countByUserIdAndCreatedAtBetween(userId, start, end);
            reservationCounts.add(count);

            // Spending this month - using efficient repository query
            BigDecimal monthSpent = paymentRepository.sumAmountByReservationUserIdAndStatusAndCreatedAtBetween(
                    userId, PaymentStatus.COMPLETED, start, end).orElse(BigDecimal.ZERO);
            monthlySpending.add(monthSpent);

            months.add(monthDate.format(DateTimeFormatter.ofPattern("MMM")));
        }

        return UserActivitySeriesDto.builder()
                .months(months)
                .monthlyReservationCounts(reservationCounts)
                .monthlySpending(monthlySpending)
                .build();
    }
}