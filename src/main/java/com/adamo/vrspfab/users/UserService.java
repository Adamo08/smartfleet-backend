package com.adamo.vrspfab.users;


import com.adamo.vrspfab.common.DuplicateFieldException;
import com.adamo.vrspfab.common.SecurityUtilsService;
import com.adamo.vrspfab.notifications.EmailService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
        userRepository.save(user);

        return userMapper.toDto(user);
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
        user.setRole(role);
        userRepository.save(user);
        return userMapper.toDto(user);
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
}