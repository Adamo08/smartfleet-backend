package com.adamo.vrspfab.notifications;

import com.adamo.vrspfab.common.SecurityRules;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class EnhancedNotificationController {
    
    private final EnhancedNotificationService enhancedNotificationService;
    
    /**
     * Enhanced broadcast with targeting and scheduling
     */
    @PostMapping("/broadcast-enhanced")
    public ResponseEntity<String> enhancedBroadcast(@RequestBody EnhancedBroadcastRequest request) {
        try {
            enhancedNotificationService.enhancedBroadcast(request);
            return ResponseEntity.ok("Broadcast scheduled successfully");
        } catch (Exception e) {
            log.error("Enhanced broadcast failed: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Broadcast failed: " + e.getMessage());
        }
    }
    
    /**
     * Get broadcast analytics
     */
    @GetMapping("/broadcast-analytics")
    public ResponseEntity<BroadcastAnalytics> getBroadcastAnalytics() {
        try {
            BroadcastAnalytics analytics = enhancedNotificationService.getBroadcastAnalytics();
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            log.error("Failed to get broadcast analytics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get user count by role
     */
    @GetMapping("/users/count-by-role/{role}")
    public ResponseEntity<Long> getUsersCountByRole(@PathVariable String role) {
        try {
            Long count = enhancedNotificationService.getUsersCountByRole(role);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("Failed to get user count by role: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get user count by group
     */
    @GetMapping("/users/count-by-group/{groupName}")
    public ResponseEntity<Long> getUsersCountByGroup(@PathVariable String groupName) {
        try {
            Long count = enhancedNotificationService.getUsersCountByGroup(groupName);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("Failed to get user count by group: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Email Template Management
     */
    @GetMapping("/email-templates")
    public ResponseEntity<Page<EmailTemplateDto>> getEmailTemplates(Pageable pageable) {
        try {
            Page<EmailTemplateDto> templates = enhancedNotificationService.getEmailTemplates(pageable);
            return ResponseEntity.ok(templates);
        } catch (Exception e) {
            log.error("Failed to get email templates: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/email-templates/filtered")
    public ResponseEntity<Page<EmailTemplateDto>> getEmailTemplatesWithFilters(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) NotificationType type,
            @RequestParam(required = false) Boolean isActive,
            Pageable pageable) {
        try {
            Page<EmailTemplateDto> templates = enhancedNotificationService.getEmailTemplatesWithFilters(
                    name, category, type, isActive, pageable);
            return ResponseEntity.ok(templates);
        } catch (Exception e) {
            log.error("Failed to get filtered email templates: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/email-templates/{id}")
    public ResponseEntity<EmailTemplateDto> getEmailTemplateById(@PathVariable Long id) {
        try {
            EmailTemplateDto template = enhancedNotificationService.getEmailTemplateById(id);
            return ResponseEntity.ok(template);
        } catch (Exception e) {
            log.error("Failed to get email template: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/email-templates")
    public ResponseEntity<EmailTemplateDto> createEmailTemplate(@RequestBody EmailTemplateDto dto) {
        try {
            EmailTemplateDto created = enhancedNotificationService.createEmailTemplate(dto);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            log.error("Failed to create email template: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/email-templates/{id}")
    public ResponseEntity<EmailTemplateDto> updateEmailTemplate(
            @PathVariable Long id, @RequestBody EmailTemplateDto dto) {
        try {
            EmailTemplateDto updated = enhancedNotificationService.updateEmailTemplate(id, dto);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Failed to update email template: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/email-templates/{id}")
    public ResponseEntity<Void> deleteEmailTemplate(@PathVariable Long id) {
        try {
            enhancedNotificationService.deleteEmailTemplate(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to delete email template: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PatchMapping("/email-templates/{id}/toggle-status")
    public ResponseEntity<Void> toggleTemplateStatus(@PathVariable Long id) {
        try {
            enhancedNotificationService.toggleTemplateStatus(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to toggle template status: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/email-templates/{id}/duplicate")
    public ResponseEntity<EmailTemplateDto> duplicateTemplate(@PathVariable Long id) {
        try {
            EmailTemplateDto duplicated = enhancedNotificationService.duplicateTemplate(id);
            return ResponseEntity.ok(duplicated);
        } catch (Exception e) {
            log.error("Failed to duplicate template: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
