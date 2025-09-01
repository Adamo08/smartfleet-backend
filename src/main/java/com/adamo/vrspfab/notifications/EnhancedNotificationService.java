package com.adamo.vrspfab.notifications;

import com.adamo.vrspfab.common.SecurityUtilsService;
import com.adamo.vrspfab.users.Role;
import com.adamo.vrspfab.users.User;
import com.adamo.vrspfab.users.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnhancedNotificationService {
    
    private final NotificationService notificationService;
    private final EmailTemplateRepository emailTemplateRepository;
    private final EmailTemplateMapper emailTemplateMapper;
    private final UserRepository userRepository;
    private final SecurityUtilsService securityUtilsService;
    
    /**
     * Enhanced broadcast with targeting and scheduling
     */
    @Transactional
    public void enhancedBroadcast(EnhancedBroadcastRequest request) {
        log.info("=== Enhanced Broadcast ===");
        log.info("Message: {}", request.getMessage());
        log.info("Type: {}", request.getType());
        log.info("Target: {}", request.getTarget().getType());
        
        // Determine target users
        List<User> targetUsers = getTargetUsers(request.getTarget());
        log.info("Target users count: {}", targetUsers.size());
        
        // Create broadcast history record
        BroadcastHistory broadcastHistory = createBroadcastHistory(request, targetUsers.size());
        
        // Send notifications
        if (request.getSchedule().isImmediate()) {
            sendBroadcastNotifications(targetUsers, request, broadcastHistory);
        } else {
            // Schedule for later (implement scheduling logic)
            scheduleBroadcast(targetUsers, request, broadcastHistory);
        }
    }
    
    private List<User> getTargetUsers(EnhancedBroadcastRequest.BroadcastTarget target) {
        return switch (target.getType()) {
            case "all" -> userRepository.findAll();
            case "role" -> userRepository.findByRole(Role.valueOf(target.getValue().toUpperCase()));
            case "specific" -> userRepository.findAllById(target.getUserIds());
            case "group" -> getUsersByGroup(target.getValue());
            default -> throw new IllegalArgumentException("Invalid target type: " + target.getType());
        };
    }
    
    private List<User> getUsersByGroup(String groupName) {
        // TODO: Implement user groups functionality
        // For now, return all users
        return userRepository.findAll();
    }
    
    private BroadcastHistory createBroadcastHistory(EnhancedBroadcastRequest request, int userCount) {
        User currentUser = securityUtilsService.getCurrentAuthenticatedUser();
        
        return BroadcastHistory.builder()
                .title(request.getTitle())
                .message(request.getMessage())
                .type(request.getType())
                .targetType(request.getTarget().getType())
                .targetValue(request.getTarget().getValue())
                .scheduledAt(request.getSchedule().isImmediate() ? LocalDateTime.now() : request.getSchedule().getScheduledDate())
                .status(request.getSchedule().isImmediate() ? BroadcastHistory.BroadcastStatus.SENT : BroadcastHistory.BroadcastStatus.SCHEDULED)
                .priority(request.getPriority())
                .requiresConfirmation(request.isRequiresConfirmation())
                .trackAnalytics(request.isTrackAnalytics())
                .createdBy(currentUser)
                .sentCount((long) userCount)
                .build();
    }
    
    @Async
    protected void sendBroadcastNotifications(List<User> users, EnhancedBroadcastRequest request, BroadcastHistory broadcastHistory) {
        int successCount = 0;
        int failureCount = 0;
        
        for (User user : users) {
            try {
                notificationService.createAndDispatchNotification(user, request.getType(), request.getMessage());
                successCount++;
            } catch (Exception e) {
                failureCount++;
                log.error("Failed to send notification to user {}: {}", user.getEmail(), e.getMessage());
            }
        }
        
        // Update broadcast history
        broadcastHistory.setDeliveredCount((long) successCount);
        broadcastHistory.setStatus(BroadcastHistory.BroadcastStatus.SENT);
        broadcastHistory.setSentAt(LocalDateTime.now());
        
        log.info("Broadcast completed - Success: {}, Failures: {}", successCount, failureCount);
    }
    
    private void scheduleBroadcast(List<User> users, EnhancedBroadcastRequest request, BroadcastHistory broadcastHistory) {
        // TODO: Implement scheduling logic with Quartz or similar
        log.info("Broadcast scheduled for {} users at {}", users.size(), request.getSchedule().getScheduledDate());
    }
    
    /**
     * Email Template Management
     */
    @Transactional(readOnly = true)
    public Page<EmailTemplateDto> getEmailTemplates(Pageable pageable) {
        return emailTemplateRepository.findAll(pageable)
                .map(emailTemplateMapper::toDto);
    }
    
    @Transactional(readOnly = true)
    public Page<EmailTemplateDto> getEmailTemplatesWithFilters(
            String name, String category, NotificationType type, Boolean isActive, Pageable pageable) {
        return emailTemplateRepository.findByFilters(name, category, type, isActive, pageable)
                .map(emailTemplateMapper::toDto);
    }
    
    @Transactional(readOnly = true)
    public EmailTemplateDto getEmailTemplateById(Long id) {
        EmailTemplate template = emailTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Email template not found"));
        return emailTemplateMapper.toDto(template);
    }
    
    @Transactional
    public EmailTemplateDto createEmailTemplate(EmailTemplateDto dto) {
        User currentUser = securityUtilsService.getCurrentAuthenticatedUser();
        
        EmailTemplate template = emailTemplateMapper.toEntity(dto);
        template.setCreatedBy(currentUser);
        template.setTemplateFile(generateTemplateFileName(dto.getName()));
        
        EmailTemplate saved = emailTemplateRepository.save(template);
        return emailTemplateMapper.toDto(saved);
    }
    
    @Transactional
    public EmailTemplateDto updateEmailTemplate(Long id, EmailTemplateDto dto) {
        EmailTemplate existing = emailTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Email template not found"));
        
        emailTemplateMapper.updateFromDto(dto, existing);
        EmailTemplate updated = emailTemplateRepository.save(existing);
        return emailTemplateMapper.toDto(updated);
    }
    
    @Transactional
    public void deleteEmailTemplate(Long id) {
        emailTemplateRepository.deleteById(id);
    }
    
    @Transactional
    public void toggleTemplateStatus(Long id) {
        EmailTemplate template = emailTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Email template not found"));
        template.setActive(!template.isActive());
        emailTemplateRepository.save(template);
    }
    
    @Transactional
    public EmailTemplateDto duplicateTemplate(Long id) {
        EmailTemplate original = emailTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Email template not found"));
        
        EmailTemplate duplicate = EmailTemplate.builder()
                .name(original.getName() + " (Copy)")
                .type(original.getType())
                .subject(original.getSubject())
                .description(original.getDescription())
                .category(original.getCategory())
                .icon(original.getIcon())
                .color(original.getColor())
                .templateFile(generateTemplateFileName(original.getName() + "-copy"))
                .variables(original.getVariables())
                .isActive(false)
                .createdBy(securityUtilsService.getCurrentAuthenticatedUser())
                .build();
        
        EmailTemplate saved = emailTemplateRepository.save(duplicate);
        return emailTemplateMapper.toDto(saved);
    }
    
    private String generateTemplateFileName(String name) {
        return name.toLowerCase().replaceAll("\\s+", "-") + ".html";
    }
    
    /**
     * Analytics
     */
    @Transactional(readOnly = true)
    public BroadcastAnalytics getBroadcastAnalytics() {
        // TODO: Implement real analytics calculation
        BroadcastAnalytics analytics = new BroadcastAnalytics();
        analytics.setTotalUsers(userRepository.count());
        analytics.setSentCount(1000L);
        analytics.setFailedCount(50L);
        analytics.setReadCount(750L);
        analytics.setClickCount(150L);
        analytics.calculateRates();
        return analytics;
    }
    
    @Transactional(readOnly = true)
    public Long getUsersCountByRole(String role) {
        try {
            return userRepository.countByRole(Role.valueOf(role.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return 0L;
        }
    }
    
    @Transactional(readOnly = true)
    public Long getUsersCountByGroup(String groupName) {
        // TODO: Implement user groups functionality
        return 0L;
    }
}
