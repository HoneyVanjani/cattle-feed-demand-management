package com.amul.cattlefeed.service;

import com.amul.cattlefeed.entity.Notification;
import com.amul.cattlefeed.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final WhatsappService whatsappService;

    /**
     * Create a notification for a specific user (farmer or secretary).
     *
     * @param userId      DB primary key of the user
     * @param title       notification title
     * @param message     notification message body
     * @param type        visual type: "success" | "warning" | "error" | "info"
     * @param phoneNumber WhatsApp number to send to (can be null)
     * @param userType    "FARMER" or "SECRETARY"
     */
    public Notification createNotification(
            Long userId,
            String title,
            String message,
            String type,
            String phoneNumber,
            String userType
    ) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setUserType(userType != null ? userType.toUpperCase() : "FARMER");
        notification.setCreatedAt(LocalDateTime.now());
        notification.setReadStatus(false);

        Notification saved = notificationRepository.save(notification);

        if (phoneNumber != null && !phoneNumber.isBlank()) {
            try {
                whatsappService.sendMessage(phoneNumber, title + "\n" + message);
            } catch (Exception e) {
                System.out.println("WhatsApp sending failed: " + e.getMessage());
            }
        }
        return saved;
    }

    // Overload for backward compatibility (defaults to FARMER)
    public Notification createNotification(
            Long userId, String title, String message, String type, String phoneNumber
    ) {
        return createNotification(userId, title, message, type, phoneNumber, "FARMER");
    }
}