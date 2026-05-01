package com.lm.hospital.controller;

import com.lm.hospital.model.LMNotification;
import com.lm.hospital.model.LMNotificationType;
import com.lm.hospital.model.LMUser;
import com.lm.hospital.repository.LMNotificationRepository;
import com.lm.hospital.repository.LMUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lm/notifications")
@CrossOrigin(origins = "*", maxAge = 3600)
public class LMNotificationController {

    @Autowired
    private LMNotificationRepository notificationRepository;

    @Autowired
    private LMUserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasRole('PATIENT')")
    public List<LMNotification> getMyNotifications(Authentication authentication) {
        String username = authentication.getName();
        LMUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(user.getId());
    }

    @GetMapping("/unread")
    @PreAuthorize("hasRole('PATIENT')")
    public List<LMNotification> getUnreadNotifications(Authentication authentication) {
        String username = authentication.getName();
        LMUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return notificationRepository.findByRecipientIdAndReadFalseOrderByCreatedAtDesc(user.getId());
    }

    @GetMapping("/unread-count")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication authentication) {
        String username = authentication.getName();
        LMUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        long count = notificationRepository.countByRecipientIdAndReadFalse(user.getId());
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','RECEPTIONIST')")
    public ResponseEntity<LMNotification> sendNotification(
            @RequestBody LMNotification notification,
            Authentication authentication) {

        // Set sender information
        String username = authentication.getName();
        LMUser sender = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        notification.setSenderId(sender.getId());
        notification.setSenderName(sender.getFullName());

        // Validate recipient exists
        LMUser recipient = userRepository.findById(notification.getRecipientId())
                .orElseThrow(() -> new RuntimeException("Recipient not found"));

        notification.setRecipientName(recipient.getFullName());

        return ResponseEntity.ok(notificationRepository.save(notification));
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> markAsRead(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        LMUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return notificationRepository.findById(id).map(notification -> {
            // Ensure the notification belongs to the current user
            if (!notification.getRecipientId().equals(user.getId())) {
                return ResponseEntity.notFound().build();
            }

            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            return ResponseEntity.ok(notificationRepository.save(notification));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/mark-all-read")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> markAllAsRead(Authentication authentication) {
        String username = authentication.getName();
        LMUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<LMNotification> unreadNotifications = notificationRepository
                .findByRecipientIdAndReadFalseOrderByCreatedAtDesc(user.getId());

        unreadNotifications.forEach(notification -> {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
        });

        notificationRepository.saveAll(unreadNotifications);

        return ResponseEntity.ok().body(Map.of("message", "All notifications marked as read"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> deleteNotification(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        LMUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return notificationRepository.findById(id).map(notification -> {
            // Ensure the notification belongs to the current user
            if (!notification.getRecipientId().equals(user.getId())) {
                return ResponseEntity.notFound().build();
            }

            notificationRepository.delete(notification);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }
}