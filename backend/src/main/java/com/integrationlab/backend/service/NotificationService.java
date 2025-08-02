package com.integrationlab.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service for handling notifications and alerts.
 */
@Slf4j
@Service
public class NotificationService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${notifications.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${notifications.email.from:noreply@integrix.com}")
    private String fromEmail;

    @Value("${notifications.email.admin:admin@integrix.com}")
    private String adminEmail;

    /**
     * Send a system alert notification.
     *
     * @param subject The alert subject
     * @param message The alert message
     */
    @Async
    public void sendSystemAlert(String subject, String message) {
        try {
            if (!emailEnabled || mailSender == null) {
                log.info("Email notifications disabled or mail sender not configured. Alert: {} - {}", subject, message);
                return;
            }

            SimpleMailMessage email = new SimpleMailMessage();
            email.setFrom(fromEmail);
            email.setTo(adminEmail);
            email.setSubject("[Integrix Alert] " + subject);
            email.setText(message);

            mailSender.send(email);
            log.info("System alert sent: {}", subject);

        } catch (Exception e) {
            log.error("Failed to send system alert: {}", e.getMessage(), e);
        }
    }

    /**
     * Send a notification to a specific user.
     *
     * @param userId The user ID
     * @param subject The notification subject
     * @param message The notification message
     */
    @Async
    public void sendUserNotification(String userId, String subject, String message) {
        // Implementation would lookup user email and send notification
        log.info("User notification for {}: {} - {}", userId, subject, message);
    }
}